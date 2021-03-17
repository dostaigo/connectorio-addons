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

import java.util.concurrent.atomic.AtomicReference;
import javax.measure.Quantity;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.RASValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.Quantities;

public class TARASValueThingHandler extends TABaseObjectThingHandler<RASValue, AnalogUnit, AnalogObjectConfig> {

  private final Logger logger = LoggerFactory.getLogger(TARASValueThingHandler.class);
  private AtomicReference<RASValue> rasValue = new AtomicReference<>();

  public TARASValueThingHandler(Thing thing) {
    super(thing, AnalogObjectConfig.class, RASValue.class);
  }

  @Override
  protected AnalogUnit determineUnit(Thing thing) {
    return AnalogUnit.TEMPERATURE_REGULATOR;
  }

  @Override
  protected void registerInput(int writeObjectIndex, TADevice device) {
    device.addAnalogInput(writeObjectIndex, new TAAnalogInput(device, (short) writeObjectIndex, (short) AnalogUnit.TEMPERATURE_REGULATOR.getIndex()));
  }

  @Override
  protected void registerOutput(int readObjectIndex, TADevice device) {
    device.addAnalogOutput(readObjectIndex, new TAAnalogOutput(device, readObjectIndex, AnalogUnit.TEMPERATURE_REGULATOR.getIndex(), (short) 0));
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (config.writeObjectIndex == 0) {
      logger.warn("Ignoring write request {} to channel {} cause write object index is not set", command, channelUID);
      return;
    }

    // push update to controller
    device.write(config.writeObjectIndex, createValue(command));
  }

  @Override
  protected RASValue createValue(Command command) {
    RASValue value = this.rasValue.get();
    if (value == null) {
      value = new RASValue(Quantities. getQuantity(0, AnalogUnit.CELSIUS.getUnit()), 0, AnalogUnit.TEMPERATURE_REGULATOR);
    }

    if (command instanceof QuantityType) {
      // temperature channel
      QuantityType<?> type = (QuantityType<?>) command;
      Quantity<?> quantity = Quantities.getQuantity(((QuantityType<?>) command).doubleValue(), type.getUnit());
      value = new RASValue(quantity, value.getMode(), AnalogUnit.TEMPERATURE_REGULATOR);
    } else if (command instanceof DecimalType) {
      // mode
      int mode = ((DecimalType) command).intValue();
      value = new RASValue(value.getValue(), mode, AnalogUnit.TEMPERATURE_REGULATOR);
    }

    this.rasValue.set(value);
    return value;
  }

  @Override
  public void accept(int index, RASValue value) {
    rasValue.set(value);

    ThingHandlerCallback callback = getCallback();
    if (callback != null) {
      Thing thing = getThing();
      Channel mode = thing.getChannel(thing.getThingTypeUID().getId());
      Channel temperature = thing.getChannel(TACANopenBindingConstants.TA_ANALOG_TEMPERATURE);

      if (mode != null) {
        callback.stateUpdated(mode.getUID(), createState(value));
      }
      if (temperature != null) {
        callback.stateUpdated(temperature.getUID(), createTemperatureState(value));
      }
    } else {
      logger.warn("Ignoring state update {} for {}, handler not ready", value, config.readObjectIndex);
    }
  }

  protected State createState(RASValue value) {
    return new DecimalType(value.getMode());
  }

  private State createTemperatureState(RASValue value) {
    Quantity<?> quantity = value.getValue();
    return QuantityType.valueOf(quantity.getValue().doubleValue(), quantity.getUnit());
  }

}
