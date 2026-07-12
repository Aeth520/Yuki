package cn.aetheris.yuki.check.impl.misc.visual.manager;

import cn.aetheris.yuki.Yuki;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public final class MetaDataManager {
    public static int HEALTH;
    public static int ABSORPTION;
    public static int TAMABLE_TAMED;
    public static int TAMABLE_OWNER;

    public static void register() {
        final ServerVersion version = Yuki.getInstance().getPacketEventsManager().getServerVersion();

        HEALTH = version.isNewerThanOrEquals(ServerVersion.V_1_17) ? 9 :
                version.isNewerThanOrEquals(ServerVersion.V_1_14) ? 8 :
                        version.isNewerThanOrEquals(ServerVersion.V_1_10) ? 7 : 6;

        ABSORPTION = version.isNewerThanOrEquals(ServerVersion.V_1_17) ? 15 :
                version.isNewerThanOrEquals(ServerVersion.V_1_15) ? 14 :
                        version.isNewerThanOrEquals(ServerVersion.V_1_14) ? 13 :
                                version.isNewerThanOrEquals(ServerVersion.V_1_10) ? 11 :
                                        version.isNewerThanOrEquals(ServerVersion.V_1_9) ? 10 : 17;

        TAMABLE_TAMED = version.isNewerThanOrEquals(ServerVersion.V_1_17) ? 17 :
                version.isNewerThanOrEquals(ServerVersion.V_1_15) ? 16 :
                        version.isNewerThanOrEquals(ServerVersion.V_1_14) ? 15 :
                                version.isNewerThanOrEquals(ServerVersion.V_1_12) ? 13 : 16;

        TAMABLE_OWNER = version.isNewerThanOrEquals(ServerVersion.V_1_17) ? 18 :
                version.isNewerThanOrEquals(ServerVersion.V_1_15) ? 17 :
                        version.isNewerThanOrEquals(ServerVersion.V_1_14) ? 16 :
                                version.isNewerThanOrEquals(ServerVersion.V_1_12) ? 14 : 17;
    }
}
