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
        // Plugin startup logic
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            this.getServer().getConsoleSender().sendMessage(Texts.depend);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // 保存默认配置文件
        saveDefaultConfig();

        // 加载配置文件
        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // 初始化捕获器数据Map
        catcherDataMap = new HashMap<>();

        // 注册命令
        Objects.requireNonNull(getCommand("chatcatcher")).setExecutor(this);
        Objects.requireNonNull(getCommand("setcatcher")).setExecutor(this);
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
    }

    @Override
    public void onDisable() {
        // 在插件卸载时保存配置文件
        saveConfig();
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chatcatcher")) {
            if (args.length == 2) {
                String catcherName = args[0];
                String playerName = args[1];

                // 获取捕获器配置
                ConfigurationSection catcherConfig = config.getConfigurationSection("catchers." + catcherName);
                if (catcherConfig != null) {
                    String catcherType = catcherConfig.getString("type", "CHAT");

                    // 初始化捕获器数据
                    catcherDataMap.put(Bukkit.getPlayerExact(playerName).getUniqueId(), new CatcherData(catcherName, catcherType));

                    // 发送开始信息
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

                // 检查配置中是否有该捕获器
                ConfigurationSection catcherConfig = config.getConfigurationSection("catchers." + catcherName);
                if (catcherConfig != null) {
                    // 获取捕获器配置
                    String catcherType = catcherConfig.getString("type", "CHAT");

                    // 初始化捕获器数据
                    catcherDataMap.put(player.getUniqueId(), new CatcherData(catcherName, catcherType));

                    // 发送开始信息
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

            // 获取捕获器配置
            ConfigurationSection catcherConfig = config.getConfigurationSection("catchers." + catcherName);
            if (catcherConfig != null) {
                // 获取捕获器类型配置
                String type = catcherConfig.getString("type", "CHAT");

                if (type.equalsIgnoreCase(catcherType)) {
                    // 执行捕获器配置的操作
                    catcherConfig.getStringList("cancel").forEach(player::sendMessage);

                    // 动态生成变量
                    String inputVariable = "{input:" + event.getMessage() + "}";

                    // 替换配置中的变量
                    List<String> endMessages = catcherConfig.getStringList("end").stream()
                            .map(message -> PlaceholderAPI.setPlaceholders(player, message.replace("{meta:input}", inputVariable)))
                            .collect(Collectors.toList());

                    // 发送结束信息
                    endMessages.forEach(player::sendMessage);

                    // 执行命令
                    endMessages.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

                    // 移除捕获器数据
                    catcherDataMap.remove(playerUUID);
                } else {
                    // 发送捕获器类型不匹配的消息
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
