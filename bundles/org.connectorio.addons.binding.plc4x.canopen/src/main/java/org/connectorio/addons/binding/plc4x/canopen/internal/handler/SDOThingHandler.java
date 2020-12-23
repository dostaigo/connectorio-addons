package org.connectorio.addons.binding.plc4x.canopen.internal.handler;

import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.handler.polling.common.BasePollingBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.internal.config.SDOConfig;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xThingHandler;
import org.openhab.core.thing.Thing;

public class SDOThingHandler extends PollingPlc4xThingHandler<PlcConnection, CANOpenGenericBridgeHandler, SDOConfig> {

  public SDOThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return getBridgeHandler().map(BasePollingBridgeHandler::getRefreshInterval)
      .orElse(CANopenBindingConstants.DEFAULT_POLLING_INTERVAL);
  }

}