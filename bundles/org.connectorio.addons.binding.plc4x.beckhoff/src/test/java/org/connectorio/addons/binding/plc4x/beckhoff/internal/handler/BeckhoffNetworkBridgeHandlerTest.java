/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.plc4x.beckhoff.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.connectorio.addons.binding.config.Configuration;
import org.connectorio.addons.binding.handler.GenericBridgeHandler;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.config.BeckhoffAmsAdsConfiguration;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.config.BeckhoffNetworkConfiguration;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.discovery.DiscoverySender;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.discovery.RouteReceiver;
import org.connectorio.addons.binding.plc4x.osgi.internal.OsgiDriverManager;
import org.connectorio.addons.binding.test.BridgeMock;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.junit.jupiter.api.Test;

class BeckhoffNetworkBridgeHandlerTest {

  DiscoverySender sender = mock(DiscoverySender.class);
  RouteReceiver routeReceiver = mock(RouteReceiver.class);

  @Test
  void testHandlerInitializationWithNoConfig() {
    Bridge bridge = new BridgeMock<>()
      .withConfig(new BeckhoffNetworkConfiguration())
      .create();

    BeckhoffNetworkBridgeHandler handler = new BeckhoffNetworkBridgeHandler(bridge, new OsgiDriverManager(Collections.emptyList()), sender, routeReceiver);
    handler.initialize();

    CompletableFuture<PlcConnection> initializer = handler.getPlcConnection();
    assertThat(initializer).isNotCompleted();
  }

  @Test
  void testHandlerInitializationWithConfig() {
    BeckhoffNetworkConfiguration cfg = new BeckhoffNetworkConfiguration();
    cfg.host = "127.0.0.1";
    cfg.port = 4040;
    cfg.targetAmsId = "0.0.0.0.0.0";
    cfg.targetAmsPort = 4040;

    BeckhoffAmsAdsConfiguration amsCfg = new BeckhoffAmsAdsConfiguration();
    amsCfg.sourceAmsId = "0.0.0.0.0.0";
    amsCfg.sourceAmsPort = 4040;
    amsCfg.ipAddress = "10.10.10.10";

    BridgeMock<GenericBridgeHandler<Configuration>, Configuration> amsBridgeMock = new BridgeMock<>("ams-network")
      .withId(new ThingUID(BeckhoffBindingConstants.THING_TYPE_AMS, "amsads-1"))
      .withConfig(amsCfg)
      .mockHandler(BeckhoffAmsAdsBridgeHandler.class);
    Bridge amsBridge = amsBridgeMock.create();

    when(((BeckhoffAmsAdsBridgeHandler) amsBridgeMock.getHandler()).getBridgeConfig()).thenReturn(Optional.of(amsCfg));

    BridgeMock<GenericBridgeHandler<Configuration>, Configuration> bridgeMock = new BridgeMock<>("ads-connection")
      .withConfig(cfg)
      .withBridge(amsBridge);

    BeckhoffAmsAdsBridgeHandler parentHandler = (BeckhoffAmsAdsBridgeHandler) bridgeMock.getHandler();

    Bridge bridge = bridgeMock.create();

    BeckhoffNetworkBridgeHandler handler = new BeckhoffNetworkBridgeHandler(bridge, new OsgiDriverManager(Collections.emptyList()), sender, routeReceiver);
    handler.setCallback(bridgeMock.getCallback());
    handler.initialize();

    CompletableFuture<PlcConnection> initializer = handler.getPlcConnection();

    AbstractThrowableAssert<?, ? extends Throwable> thrownBy = assertThatThrownBy(initializer::join);
    thrownBy.isInstanceOf(CompletionException.class)
      .hasMessageContaining("Error creating channel.");

    thrownBy = thrownBy.getCause();
    thrownBy.isInstanceOf(PlcConnectionException.class);

    thrownBy = thrownBy.getCause();
    thrownBy.isInstanceOf(ConnectException.class)
      .hasMessageContaining("Connection refused: /%s:%d", cfg.host, cfg.port);
  }

}