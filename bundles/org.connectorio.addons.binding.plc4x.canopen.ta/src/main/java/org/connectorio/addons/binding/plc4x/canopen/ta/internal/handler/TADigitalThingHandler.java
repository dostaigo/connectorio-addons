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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.DigitalValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class TADigitalThingHandler extends TABaseObjectThingHandler<DigitalValue, DigitalUnit, DigitalObjectConfig> {

  public TADigitalThingHandler(Thing thing) {
    super(thing, DigitalObjectConfig.class, DigitalValue.class);
  }

  @Override
  protected DigitalUnit determineUnit(Thing thing) {
    return DigitalUnit.OPEN_CLOSED;
  }

  @Override
  protected void registerInput(int writeObjectIndex, TADevice device) {
    device.addDigitalInput(writeObjectIndex, new TADigitalInput(device, writeObjectIndex, DigitalUnit.OPEN_CLOSED.getIndex()));
  }

  @Override
  protected void registerOutput(int readObjectIndex, TADevice device) {
    device.addDigitalOutput(readObjectIndex, new TADigitalOutput(device, readObjectIndex, DigitalUnit.OPEN_CLOSED.getIndex(), false));
  }

  protected DigitalValue createValue(Command command) {
    if (command == OnOffType.ON || command == OpenClosedType.OPEN) {
      return new DigitalValue(true, unit);
    }
    return new DigitalValue(false, unit);
  }

  protected State createState(DigitalValue value) {
    return value.getValue() ? OnOffType.ON : OnOffType.OFF;
  }

}