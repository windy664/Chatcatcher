package org.windy.chatcatcher;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class Chatcatcher extends JavaPlugin implements Listener {
    private File configFile;
    private FileConfiguration config;
    private Map<UUID, CatcherData> catcherDataMap;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            this.getServer().getConsoleSender().sendMessage(Texts.depend);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        saveDefaultConfig();

        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        catcherDataMap = new HashMap<>();
        getCommand("chatcatcher").setExecutor(this);
        getCommand("setcatcher").setExecutor(this);
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
    }

    @Override
    public void onDisable() {
        saveConfig();
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chatcatcher")) {
            if (args.length == 2) {
                String catcherName = args[0];
                String playerName = args[1];

                ConfigurationSection catcherConfig = config.getConfigurationSection("catchers." + catcherName);
                if (catcherConfig != null) {
                    String catcherType = catcherConfig.getString("type", "CHAT");

                    catcherDataMap.put(Bukkit.getPlayerExact(playerName).getUniqueId(), new CatcherData(catcherName, catcherType));

                    catcherConfig.getStringList("start").forEach(sender::sendMessage);
                } else {
                    sender.sendMessage("找不到指定的捕获器：" + catcherName);
                }
            } else {
                sender.sendMessage("用法: /chatcatcher [捕获器名称] [玩家]");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("setcatcher") && sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                String catcherName = args[0];

                ConfigurationSection catcherConfig = config.getConfigurationSection("catchers." + catcherName);
                if (catcherConfig != null) {
                    String catcherType = catcherConfig.getString("type", "CHAT");

                    catcherDataMap.put(player.getUniqueId(), new CatcherData(catcherName, catcherType));

                    catcherConfig.getStringList("start").forEach(player::sendMessage);
                } else {
                    player.sendMessage("找不到指定的捕获器：" + catcherName);
                }
            } else {
                sender.sendMessage("用法: /setcatcher [捕获器名称]");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (catcherDataMap.containsKey(playerUUID)) {
            CatcherData catcherData = catcherDataMap.get(playerUUID);
            String catcherName = catcherData.getCatcherName();
            String catcherType = catcherData.getCatcherType();

            ConfigurationSection catcherConfig = config.getConfigurationSection("catchers." + catcherName);
            if (catcherConfig != null) {
                String type = catcherConfig.getString("type", "CHAT");

                if (type.equalsIgnoreCase(catcherType)) {
                    catcherConfig.getStringList("cancel").forEach(player::sendMessage);

                    String inputVariable = "{input:" + event.getMessage() + "}";

                    List<String> endMessages = catcherConfig.getStringList("end").stream()
                            .map(message -> PlaceholderAPI.setPlaceholders(player, message.replace("{meta:input}", inputVariable)))
                            .collect(Collectors.toList());

                    endMessages.forEach(player::sendMessage);

                    endMessages.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

                    catcherDataMap.remove(playerUUID);
                } else {
                    player.sendMessage("捕获器类型不匹配。");
                }
            }
        }
    }

    private static class CatcherData {
        private final String catcherName;
        private final String catcherType;

        public CatcherData(String catcherName, String catcherType) {
            this.catcherName = catcherName;
            this.catcherType = catcherType;
        }

        public String getCatcherName() {
            return catcherName;
        }

        public String getCatcherType() {
            return catcherType;
        }
    }
}
