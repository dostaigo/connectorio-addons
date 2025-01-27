/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.handler.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.connectorio.addons.binding.plc4x.config.CommonChannelConfiguration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTask implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(ReadTask.class);
  private final ThingHandlerCallback callback;
  private final PlcReadRequest request;
  private final Channel channel;
  private final CommonChannelConfiguration channelConfig;

  public ReadTask(PlcConnection connection, ThingHandlerCallback callback, Channel channel) {
    this.callback = callback;
    this.channel = channel;
    this.channelConfig = channel.getConfiguration()
      .as(CommonChannelConfiguration.class);

    this.request = connection.readRequestBuilder()
      .addItem(getUID(), channelConfig.field)
      .build();
  }

  @Override
  public void run() {
    logger.info("Running task to fetch field {}", channelConfig.field);
    try {
      CompletableFuture<? extends PlcReadResponse> execute = request.execute();
      // we should be able to fetch data within short time period, if not then it is a
      // transmission problem and we should not block this task any more.
      execute.whenComplete(this::handle).get();
    } catch (PlcRuntimeException e) {
      logger.warn("Could not fetch field {} from PLC", channelConfig.field, e);
    } catch (InterruptedException | ExecutionException e) {
      logger.debug("Error while waiting for PLC response", e);
    }
  }

  private void handle(PlcReadResponse response, Throwable throwable) {
    if (throwable != null) {
      logger.info("Could not read field {}, {}", channelConfig.field, throwable);
      return;
    }

    Object object = response.getObject(getUID());
    State state = fromPlc(object);

    logger.debug("Read value {} mapped to {}", object, state);
    callback.stateUpdated(channel.getUID(), state);
  }

  private State fromPlc(Object object) {
    if (object instanceof Short) {
      return new DecimalType((Short) object);
    } else if (object instanceof Integer) {
      return new DecimalType((Integer) object);
    } else if (object instanceof Long) {
      return new DecimalType((Long) object);
    } else if (object instanceof Boolean && CoreItemFactory.SWITCH.equalsIgnoreCase(channel.getAcceptedItemType())) {
      boolean state = (boolean) object;
      return state ? OnOffType.ON : OnOffType.OFF;
    } else if (object instanceof Boolean && CoreItemFactory.CONTACT.equalsIgnoreCase(channel.getAcceptedItemType())) {
      boolean state = (boolean) object;
      return state ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    } else if (object instanceof Double) {
      return new DecimalType((Double) object);
    } else if (object instanceof LocalDateTime) {
      return new DateTimeType(((LocalDateTime) object).atZone(ZoneId.systemDefault()));
    } else if (object instanceof LocalDate) {
      LocalDate date = (LocalDate) object;
      return new DateTimeType(date.atStartOfDay(ZoneId.systemDefault()));
    } else if (object instanceof LocalTime) {
      LocalTime time = (LocalTime) object;
      return new DateTimeType(time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()));
    }
    return new StringType(object.toString());
  }

  private String getUID() {
    return channel.getUID().getAsString();
  }
}