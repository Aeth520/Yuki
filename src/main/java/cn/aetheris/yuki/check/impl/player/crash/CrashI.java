package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.google.gson.*;

@CheckData(name = "CrashI",
        configName = "CrashI",
        description = "Sign Crasher",
        type = CheckType.CRASH,
        decay = 0.85,
        experimental = true)
public final class CrashI extends Check implements PacketCheck {
    private static final Gson GSON = new GsonBuilder().create();
    private static final char COLOR_CHAR = '§';

    private final boolean isLegacyClient;
    private final boolean isLegacyServer;

    public CrashI(PlayerData player) {
        super(player);
        this.isLegacyClient = player.getClientVersion().isOlderThan(ClientVersion.V_1_9);
        this.isLegacyServer = Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isLegacyServer || event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN) {
            return;
        }

        WrapperPlayClientUpdateSign sign = new WrapperPlayClientUpdateSign(event);
        boolean flaggedColorSign = false;

        try {
            for (String line : sign.getTextLines()) {
                String parsedLine = parseSignLine(line);

                if (parsedLine == null) {
                    cancelEvent(event, "Type= Custom");
                    return;
                }

                if (!flaggedColorSign && parsedLine.indexOf(COLOR_CHAR) != -1) {
                    handleColorSign(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    flaggedColorSign = true;
                }
            }
        } catch (Exception | StackOverflowError e) {
            cancelEvent(event, "failed to get json");
        }
    }

    private String parseSignLine(String line) {
        try {
            JsonElement json = GSON.fromJson(line, JsonElement.class);

            if (isLegacyClient && json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return json.getAsString();
            }

            if (!isLegacyClient && json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonElement textElement = jsonObject.get("text");

                if (textElement != null && textElement.isJsonPrimitive() && textElement.getAsJsonPrimitive().isString()) {
                    return textElement.getAsString();
                }
            }
        } catch (JsonSyntaxException ignored) {
        }

        return null;
    }

    private void handleColorSign(PacketReceiveEvent event) {
        if (flagAndAlert("Type= HasColor") && shouldModifyPackets()) {
            kickPlayer();
            cancelEvent(event, null);
        }
    }

    private void cancelEvent(PacketReceiveEvent event, String alertMessage) {
        event.setCancelled(true);
        player.onPacketCancel();
        if (alertMessage != null) {
            alert(alertMessage);
        }
    }
}