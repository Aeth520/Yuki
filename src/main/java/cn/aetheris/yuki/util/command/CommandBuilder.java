package cn.aetheris.yuki.util.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class CommandBuilder {

    
    public static void registerCommand(JavaPlugin plugin, CommandExecutor commandExecutor, String description, String... aliases) {
        String name = aliases[0];
        
        
        
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            command = getCommand(name, plugin);
            if (command == null) {
                return;
            }
            List<String> aliasList = new ArrayList<>();
            for (int i = 1; i < aliases.length; i++) {
                aliasList.add(aliases[i]);
            }
            if (!aliasList.isEmpty()) {
                command.setAliases(aliasList);
            }
            command.setDescription(description);
            command.setPermission("yuki.commands");

            CommandMap commandMap = getCommandMap();
            if (commandMap == null) {
                return;
            }
            commandMap.register(plugin.getDescription().getName(), command);
        }
        command.setExecutor(commandExecutor);
        if (commandExecutor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) commandExecutor);
        }
    }

    
    private static PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            command = constructor.newInstance(name, plugin);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return command;
    }


    
    private static CommandMap getCommandMap() {
        CommandMap commandMap;
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return commandMap;
    }
}
