package cn.aetheris.yuki.check.type;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.util.update.VehiclePositionUpdate;

public interface VehicleCheck extends AbstractCheck {

    void process(final VehiclePositionUpdate vehicleUpdate);
}
