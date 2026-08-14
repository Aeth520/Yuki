package cn.aetheris.yuki.functionality.code;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.fake.FakeAntiCheatUtils;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;

@AllArgsConstructor
public class ExpressAPI {
    PlayerData playerData;
    String original;
    int vl;
    Check check;
    String alertString;
    String verbose;

    public void executeCommand(String command) {
        String cmd = playerData.punishmentManager.replaceAlertPlaceholders(command, vl, check, alertString, verbose);

        String randomKey = FakeAntiCheatUtils.getRandomName();
        String colorCode = FakeAntiCheatUtils.getColorCode(randomKey);
        String antiCheatName = FakeAntiCheatUtils.getName(randomKey);
        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd
                .replace("%anticheat_color%", colorCode)
                .replace("%anticheat%", antiCheatName))
        );
    }

    public boolean hasPermission(String permission) {
        if (playerData.getBukkitPlayer() != null) {
            return playerData.getBukkitPlayer().hasPermission(permission);
        }
        return false;
    }
}
