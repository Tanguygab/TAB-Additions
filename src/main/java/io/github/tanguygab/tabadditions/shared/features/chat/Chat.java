package io.github.tanguygab.tabadditions.shared.features.chat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import io.github.tanguygab.tabadditions.shared.features.chat.commands.CommandManager;
import io.github.tanguygab.tabadditions.shared.features.chat.commands.FormatCommand;
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiManager;
import io.github.tanguygab.tabadditions.shared.features.chat.mentions.MentionManager;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Chat extends RefreshableFeature implements UnLoadable, JoinListener {

    @Getter private final String featureName = "Chat";
    @Getter private final String refreshDisplayName = "&aChat&r";
    private final TABAdditions plugin = TABAdditions.getInstance();
    private final TAB tab = TAB.getInstance();
    public final MiniMessage mm = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
    private final Map<String,ChatFormat> formats = new LinkedHashMap<>();

    private final String chatPlaceholderFormat;
    private final boolean chatPlaceholderRelational;
    private PlayerPlaceholderImpl chatPlaceholder;
    private RelationalPlaceholderImpl relChatPlaceholder;
    private final int chatPlaceholderStay;

    private final ChatFormatter chatFormatter;
    private final EmojiManager emojiManager;
    private final MentionManager mentionManager;
    private final MsgManager msgManager;
    protected final SocialSpyManager socialSpyManager;
    private final CommandManager commandsManager;

    public Double cooldownTime;
    public Map<UUID, LocalDateTime> cooldown = new HashMap<>();

    private final boolean toggleCmd;
    private List<UUID> toggled;
    private PlayerPlaceholderImpl toggleChatPlaceholder;
    public final List<PlayerPlaceholderImpl> placeholders = new ArrayList<>();

    private final boolean ignoreCmd;
    private final Map<UUID,List<UUID>> ignored = new HashMap<>();

    private final boolean clearchatEnabled;
    private final int clearChatAmount;
    private final String clearChatLine;

    private final String discordFormat;
    private final boolean discordEssX;
    private final boolean discordSRV;

    @Getter
    private final boolean bukkitBridgeChatEnabled;

    public Chat(ConfigurationFile config) {
        chatFormatter = new ChatFormatter(config);

        ConfigurationSection formats = config.getConfigurationSection("formats");
        formats.getKeys().forEach( key -> {
            String name = key.toString();
            ConfigurationSection format = formats.getConfigurationSection(name);

            String displayName = format.getString("name",name);
            String condition = format.getString("condition");
            String viewCondition = format.getString("view-condition");
            String channel = format.getString("channel");
            ConfigurationSection display = format.getConfigurationSection("display");
            this.formats.put(name,new ChatFormat(name,
                    displayName,
                    AdvancedConditions.getCondition(condition),
                    AdvancedConditions.getCondition(viewCondition),
                    channel == null ? "" : channel,
                    ChatUtils.componentsToMM(display)));
        });
        PlaceholderManagerImpl pm = tab.getPlaceholderManager();
        placeholders.add(pm.registerPlayerPlaceholder("%chat-format%",1000,p->getFormat((TabPlayer) p).getDisplayName()));

        emojiManager = config.getBoolean("emojis.enabled",false)
                ? new EmojiManager(this,
                        ChatUtils.componentToMM(config.getConfigurationSection("emojis.output")),
                        config.getBoolean("emojis.block-without-permission",false),
                        config.getBoolean("emojis.auto-complete",true),
                        config.getConfigurationSection("emojis.categories"),
                        config.getBoolean("emojis./emojis",true),
                        config.getBoolean("emojis./toggleemojis",true))
                : null;
        mentionManager = config.getBoolean("mention.enabled",true)
                ? new MentionManager(this,
                        config.getString("mention.input","@%player%"),
                        ChatUtils.componentToMM(config.getConfigurationSection("mention.output")),
                        config.getString("mention.sound","BLOCK_NOTE_BLOCK_PLING"),
                        config.getBoolean("mention./togglementions",true),
                        config.getBoolean("mention.output-for-everyone",true),
                        config.getConfigurationSection("mention.custom-mentions"))
            : null;
        msgManager = config.getBoolean("msg.enabled",true)
                ? new MsgManager(this,ChatUtils.componentToMM(config.getConfigurationSection("msg.sender")),
                        ChatUtils.componentToMM(config.getConfigurationSection("msg.viewer")),
                        ChatUtils.getDouble(config.getObject("msg.cooldown")),
                        config.getStringList("msg./msg-aliases",Arrays.asList("tell","whisper","w","m")),
                        config.getBoolean("msg.msg-self",true),
                        config.getBoolean("msg./togglemsg",true),
                        config.getBoolean("msg./reply",true),
                        config.getBoolean("msg.save-last-sender-for-reply",true))
                : null;
        socialSpyManager = config.getBoolean("socialspy.enabled",true)
                ? new SocialSpyManager(this,
                        config.getBoolean("socialspy.msgs.spy",true),
                ChatUtils.componentToMM(config.getConfigurationSection("socialspy.msgs.output")),
                config.getBoolean("socialspy.channels.spy",true),
                ChatUtils.componentToMM(config.getConfigurationSection("socialspy.channels.output")),
                config.getBoolean("socialspy.view-conditions.spy",true),
                ChatUtils.componentToMM(config.getConfigurationSection("socialspy.view-conditions.output")))
                : null;
        commandsManager = config.hasConfigOption("commands-formats")
                && !config.getConfigurationSection("commands-formats").getKeys().isEmpty()
                ? new CommandManager(this,config.getConfigurationSection("commands-formats"))
                : null;

        cooldownTime = ChatUtils.getDouble(config.getObject("cooldown"));

        toggleCmd = config.getBoolean("/togglechat",true);
        if (toggleCmd) {
            plugin.getPlatform().registerCommand("togglechat");
            toggleChatPlaceholder = pm.registerPlayerPlaceholder("%chat-status%",-1,p->hasChatToggled((TabPlayer)p) ? "Off" : "On");
            placeholders.add(toggleChatPlaceholder);
            toggled = plugin.loadData("chat-off",true);
        }

        ignoreCmd = config.getBoolean("/ignore",true);
        if (ignoreCmd) {
            ConfigurationSection ignored = plugin.getPlayerData().getConfigurationSection("ignored");
            ignored.getKeys().forEach(key -> {
                String player = key.toString();
                List<UUID> list = ignored.getStringList(player, List.of()).stream().map(UUID::fromString).collect(Collectors.toCollection(ArrayList::new));
                this.ignored.put(UUID.fromString(player), list);
            });
        }

        clearchatEnabled = config.getBoolean("clearchat.enabled",false);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");

        discordFormat = config.getString("discord-support.format","%msg%");
        discordEssX = config.getBoolean("discord-support.EssentialsX",true);
        discordSRV = config.getBoolean("discord-support.DiscordSRV",true);

        chatPlaceholderFormat = config.getString("chat-placeholder.format","%msg%");
        chatPlaceholderRelational = config.getBoolean("chat-placeholder.relational",false);
        if (chatPlaceholderRelational)
            relChatPlaceholder = pm.registerRelationalPlaceholder("%rel_chat%",-1,(v,t)->"");
        else placeholders.add(chatPlaceholder = pm.registerPlayerPlaceholder("%chat%",-1,p->""));
        chatPlaceholderStay = config.getInt("chat-placeholder.stay",3000);

        bukkitBridgeChatEnabled = plugin.getPlatform().isProxy() && config.getBoolean("chat-from-bukkit-bridge",false);

        for (TabPlayer player : tab.getOnlinePlayers()) loadProperties(player);
    }

    private void loadProperties(TabPlayer player) {
        player.loadPropertyFromConfig(this,"chatprefix", "");
        player.loadPropertyFromConfig(this,"customchatname", player.getName());
        player.loadPropertyFromConfig(this,"chatsuffix", "");
        TabExpansion exp = tab.getPlaceholderManager().getTabExpansion();
        placeholders.forEach(placeholder -> exp.setPlaceholderValue(player,placeholder.getIdentifier(), placeholder.getLastValue(player)));
    }

    @Override
    public void refresh(@NotNull TabPlayer player, boolean force) {}

    @Override
    public void unload() {
        if (emojiManager != null) emojiManager.unload();
        if (mentionManager != null) mentionManager.unload();
        if (msgManager != null) msgManager.unload();
        if (socialSpyManager != null) socialSpyManager.unload();
        if (commandsManager != null) commandsManager.unload();
    }

    public ChatFormat getFormat(TabPlayer player) {
        for (ChatFormat format : formats.values())
            if (format.isConditionMet(player))
                return format;
        return null;
    }
    public boolean hasChatToggled(TabPlayer player) {
        return toggled.contains(player.getUniqueId());
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        loadProperties(player);
        if (emojiManager != null && emojiManager.isAutoCompleteEnabled() && !emojiManager.hasCmdToggled(player))
            emojiManager.loadAutoComplete(player);
    }

    public boolean onCommand(@NotNull TabPlayer p, @NotNull String cmd) {
        if (cmd.startsWith("/emojis") || cmd.equals("/toggleemojis")) return emojiManager != null && emojiManager.onCommand(p,cmd);
        if (cmd.equals("/togglementions")) return mentionManager != null && mentionManager.onCommand(p,cmd);
        if (msgManager != null && (cmd.equals("/togglemsg") || msgManager.isReplyCmd(cmd,false) || msgManager.isMsgCmd(cmd,false)))
            return msgManager.onCommand(p,cmd);
        if (cmd.equals("/socialspy")) return p.hasPermission("tabadditions.chat.socialspy") && socialSpyManager != null && socialSpyManager.onCommand(p,cmd);

        TranslationFile msgs = plugin.getTranslation();
        if (cmd.equals("/togglechat")) return plugin.toggleCmd(toggleCmd,p,toggled,toggleChatPlaceholder,msgs.chatOn,msgs.chatOff,false);
        if (cmd.equals("/clearchat")) {
            if (!clearchatEnabled || !p.hasPermission("tabadditions.chat.clearchat")) return false;

            String lineBreaks = ("\n"+clearChatLine)
                    .repeat(clearChatAmount)
                    +"\n"+msgs.getChatCleared(p);
            for (TabPlayer all : tab.getOnlinePlayers()) all.sendMessage(lineBreaks);
            return true;
        }
        if (cmd.startsWith("/ignore")) {
            if (!ignoreCmd) return false;
            if (!cmd.startsWith("/ignore ")) {
                p.sendMessage(msgs.providePlayer);
                return true;
            }
            String player = cmd.substring(8).toLowerCase();
            if (p.getName().equalsIgnoreCase(player)) {
                p.sendMessage(msgs.cantIgnoreSelf);
                return true;
            }
            TabPlayer tabPlayer = plugin.getPlayer(player);
            if (tabPlayer == null) {
                p.sendMessage(msgs.getPlayerNotFound(player));
                return true;
            }
            UUID playerUUID = tabPlayer.getUniqueId();
            List<UUID> ignored = this.ignored.computeIfAbsent(p.getUniqueId(), uuid->new ArrayList<>());
            if (ignored.contains(playerUUID))
                ignored.remove(playerUUID);
            else ignored.add(playerUUID);
            p.sendMessage(msgs.getIgnore(player,ignored.contains(playerUUID)));
            return true;
        }

        return commandsManager != null && commandsManager.onCommand(p,cmd);
    }

    public void onChat(TabPlayer sender, String message) {
        if (isMuted(sender)) return;

        if (cooldown.containsKey(sender.getUniqueId())) {
            long time = ChronoUnit.SECONDS.between(cooldown.get(sender.getUniqueId()),LocalDateTime.now());
            if (time < cooldownTime) {
                sender.sendMessage(plugin.getTranslation().getCooldown(cooldownTime-time));
                return;
            }
        }
        if (cooldownTime != 0 && !sender.hasPermission("tabadditions.chat.bypass.cooldown"))
            cooldown.put(sender.getUniqueId(),LocalDateTime.now());

        if (chatFormatter.shouldBlock(message,sender)) {
            sender.sendMessage(plugin.getTranslation().cantSwear);
            return;
        }

        ChatFormat format = null;
        if (commandsManager != null) {
            format = commandsManager.getFromPrefix(sender,message);
            if (format != null)
                message = message.substring(((FormatCommand) format).getPrefix().length());

            else if (commandsManager.contains(sender))
                format = commandsManager.getFormat(sender);
        }
        if (format == null) format = getFormat(sender);
        if (format == null) return;
        String text = format.getText();
        plugin.getPlatform().audience(null).sendMessage(createMessage(sender,null,message,text));

        if (plugin.getPlatform().isPluginEnabled("InteractiveChat"))
            try {text = InteractiveChatAPI.markSender(text,sender.getUniqueId());}
            catch (IllegalStateException ignored) {}

        CpuManager cpu = tab.getCpu();
        if (!chatPlaceholderRelational) {
            String placeholderMsg = legacySerializer.serialize(createMessage(sender,null,message,chatPlaceholderFormat));
            chatPlaceholder.updateValue(sender, placeholderMsg);

            cpu.getProcessingThread().executeLater(new TimedCaughtTask(cpu,() -> {
                if (chatPlaceholder.getLastValue(sender).equals(placeholderMsg))
                    chatPlaceholder.updateValue(sender, "");
            }, featureName, "update %chat% for " + sender.getName()), chatPlaceholderStay);
        }

        for (TabPlayer viewer : tab.getOnlinePlayers()) {
            if (socialSpyManager != null) socialSpyManager.process(sender,viewer,message,socialSpyManager.isSpying(sender,viewer,format));
            if (!canSee(sender,viewer,format)) continue;
            sendMessage(viewer, createMessage(sender, viewer, message, text));
            if (!chatPlaceholderRelational) continue;
            String placeholderMsg = legacySerializer.serialize(createMessage(sender,viewer,message,chatPlaceholderFormat));
            relChatPlaceholder.updateValue(viewer, sender, placeholderMsg);

            cpu.getProcessingThread().executeLater(new TimedCaughtTask(cpu,() -> {
                if (relChatPlaceholder.getLastValue(viewer,sender).equals(placeholderMsg))
                    relChatPlaceholder.updateValue(viewer,sender, "");
            }, featureName, "update %rel_chat% for "+viewer.getName()+" and "+ sender.getName()), chatPlaceholderStay);
        }

        List<String> discord = new ArrayList<>(2);
        if (discordSRV) discord.add("DiscordSRV");
        if (discordEssX) discord.add("EssentialsX");
        if (discord.isEmpty()) return;
        String msgToDiscord = plainTextSerializer.serialize(createMessage(sender,null,message,discordFormat));
        if (format.hasNoViewCondition()) plugin.getPlatform().sendToDiscord(sender,msgToDiscord,format.getChannel(),discord);
    }

    public void sendMessage(TabPlayer player, Component component) {
        plugin.getPlatform().audience(player).sendMessage(component);
    }
    public Component createMessage(TabPlayer sender, TabPlayer viewer, String message, String text) {
        String output = plugin.parsePlaceholders(text,sender,viewer).replace("%msg%", process(sender,viewer,message));
        output = ChatUtils.toMMColors(output);
        return mm.deserialize(output);
    }

    private String process(TabPlayer sender, TabPlayer viewer, String message) {
        message = mm.escapeTags(message);
        if (emojiManager != null) message = emojiManager.process(sender,viewer,message);
        if (mentionManager != null && viewer != null) message = mentionManager.process(sender,viewer,message);
        message = chatFormatter.process(message,sender);
        return message;
    }

    public boolean isMuted(TabPlayer p) {
        return plugin.getPlatform().isPluginEnabled("AdvancedBan")
                && PunishmentManager.get().isMuted(UUIDManager.get().getMode() != UUIDManager.FetcherMode.DISABLED
                ? p.getUniqueId().toString().replace("-", "")
                : p.getName().toLowerCase());
    }
    public boolean isIgnored(TabPlayer sender, TabPlayer viewer) {
        return !sender.hasPermission("tabadditions.chat.bypass.ignore") && ignored.getOrDefault(viewer.getUniqueId(),List.of()).contains(sender.getUniqueId());
    }

    private boolean canSee(TabPlayer sender, TabPlayer viewer, ChatFormat f) {
        if (sender == viewer) return true;
        if (viewer == null) return f.getChannel().isEmpty() && f.hasNoViewCondition();
        if (!f.getChannel().equals(getFormat(viewer).getChannel())) return false;
        return f.isViewConditionMet(sender, viewer);
    }

}
