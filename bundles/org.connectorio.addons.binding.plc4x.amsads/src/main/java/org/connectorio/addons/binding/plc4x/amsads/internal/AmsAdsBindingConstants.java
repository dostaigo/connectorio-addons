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
package org.connectorio.addons.binding.plc4x.amsads.internal;

import org.connectorio.addons.binding.plc4x.Plc4xBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AmsAdsBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface AmsAdsBindingConstants extends Plc4xBindingConstants {

  String BINDING_ID = Plc4xBindingConstants.protocol("amsads");

  ThingTypeUID THING_TYPE_AMS = new ThingTypeUID(BINDING_ID, "ams");

  ThingTypeUID THING_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, "network");

  ThingTypeUID THING_TYPE_SERIAL = new ThingTypeUID(BINDING_ID, "serial");

  ThingTypeUID THING_TYPE_ADS = new ThingTypeUID(BINDING_ID, "ads");

  // List of all Channel types
  String SWITCH = "switch";

  Long DEFAULT_REFRESH_INTERVAL = 1000L;

}
