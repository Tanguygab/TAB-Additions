package io.github.tanguygab.tabadditions.shared.features.chat.commands;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandManager extends ChatManager {

    private final Map<String,FormatCommand> commands = new HashMap<>();
    private final Map<UUID,FormatCommand> players = new HashMap<>();

    public CommandManager(Chat chat, ConfigurationSection commands) {
        super(chat);

        commands.getKeys().forEach(key -> {
            String name = key.toString();
            ConfigurationSection command = commands.getConfigurationSection(name);

            String displayName = command.getString("name",name);
            String condition = command.getString("condition");
            String viewCondition = command.getString("view-condition");
            String channel = command.getString("channel");
            boolean save = Boolean.TRUE.equals(command.getBoolean("keep-on-reload"));
            String prefix = command.getString("prefix");
            ConfigurationSection display = command.getConfigurationSection("display");
            this.commands.put(name,new FormatCommand(name,
                    displayName,
                    AdvancedConditions.getCondition(condition),
                    AdvancedConditions.getCondition(viewCondition),
                    channel == null ? "" : channel,
                    ChatUtils.componentsToMM(display),
                    save,
                    prefix));
            plugin.getPlatform().registerCommand(name);
        });
        Map<String,String> data = plugin.getPlayerData().getMap("chat-commands-formats");
        data.forEach((uuid,cmd)->{
            if (this.commands.containsKey(cmd))
                players.put(UUID.fromString(uuid),this.commands.get(cmd));
        });
    }

    @Override
    public void unload() {
        Map<String,String> data = new HashMap<>();
        players.forEach((uuid,cmd)->{
            if (cmd.saveOnReload())
                data.put(uuid.toString(),cmd.getName());
        });
        plugin.getPlayerData().set("chat-commands-formats",data.isEmpty() ? null : data);
    }

    public boolean contains(TabPlayer player) {
        return players.containsKey(player.getUniqueId());
    }

    public FormatCommand getFormat(TabPlayer player) {
        return players.get(player.getUniqueId());
    }

    public FormatCommand getFromPrefix(TabPlayer sender, String message) {
        for (FormatCommand cmd : commands.values())
            if (message.startsWith(cmd.getPrefix()) && cmd.isConditionMet(sender))
                return cmd;
        return null;
    }

    @Override
    public boolean onCommand(TabPlayer sender, String command) {
        FormatCommand cmd = commands.get(command.substring(1));
        if (cmd == null) return false;

        if (!cmd.isConditionMet(sender)) {
            sender.sendMessage(tab.getConfiguration().getMessages().getNoPermission(), true);
            return true;
        }
        String name = cmd.getDisplayName();
        if (players.containsKey(sender.getUniqueId())) {
            players.remove(sender.getUniqueId());
            sender.sendMessage(translation.getChatCmdLeave(name), true);
            return true;
        }
        players.put(sender.getUniqueId(), cmd);
        sender.sendMessage(translation.getChatCmdJoin(name), true);
        return true;
    }
}