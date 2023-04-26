package io.github.tanguygab.tabadditions.shared.features.chat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiManager;
import io.github.tanguygab.tabadditions.shared.features.chat.mentions.MentionManager;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.features.types.CommandListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Chat extends TabFeature implements UnLoadable, JoinListener, CommandListener {

    @Getter private final String featureName = "Chat";
    private final TABAdditions plugin = TABAdditions.getInstance();
    private final TAB tab = TAB.getInstance();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String,ChatFormat> formats = new LinkedHashMap<>();
    private final PlayerPlaceholderImpl chatPlaceholder;
    private final int msgPlaceholderStay;
    private final ChatFormatter chatFormatter;
    private final EmojiManager emojiManager;
    private final MentionManager mentionManager;
    private final MsgManager msgManager;
    protected final SocialSpyManager socialSpyManager;

    public Double cooldownTime;
    public Map<UUID, LocalDateTime> cooldown = new HashMap<>();

    private final boolean toggleCmd;
    private final List<UUID> toggled;

    private final boolean ignoreCmd;
    private final Map<UUID,List<UUID>> ignored = new HashMap<>();

    private final boolean clearchatEnabled;
    private final int clearChatAmount;
    private final String clearChatLine;

    public Chat(ConfigurationFile config) {
        chatFormatter = new ChatFormatter(config);

        chatPlaceholder = (PlayerPlaceholderImpl) tab.getPlaceholderManager().registerPlayerPlaceholder("%chat%",-1,p->"");
        msgPlaceholderStay = config.getInt("msg-placeholder-stay",3000);

        Map<String,Map<String,Object>> formats = config.getConfigurationSection("formats");
        formats.forEach((name,format)->{
            String displayName = (String) format.getOrDefault("name",name);
            String condition = (String) format.get("condition");
            String viewCondition = (String) format.get("view-condition");
            String channel = (String) format.get("channel");
            Map<String,Map<String,Object>> display = (Map<String, Map<String, Object>>) format.get("display");
            this.formats.put(name,new ChatFormat(displayName,
                    AdvancedConditions.getCondition(condition),
                    AdvancedConditions.getCondition(viewCondition),
                    channel == null ? "" : channel,
                    ChatUtils.componentsToMM(display)));
        });
        PlaceholderManager pm = tab.getPlaceholderManager();
        pm.registerPlayerPlaceholder("%chat-format%",1000,p->getFormat((TabPlayer) p).getName());

        emojiManager = config.getBoolean("emojis.enabled",false)
                ? new EmojiManager(this,
                        ChatUtils.componentToMM(config.getConfigurationSection("emojis.output")),
                        config.getBoolean("emojis.block-without-permission",false),
                        config.getBoolean("emojis.auto-complete",true),
                        config.getConfigurationSection("emojis.categories"),
                        config.getBoolean("emojis./emojis",true),
                        config.getBoolean("emojis./toggleemoji",true))
                : null;
        mentionManager = config.getBoolean("mention.enabled",true)
                ? new MentionManager(this,
                        config.getString("mention.input","@%player%"),
                        ChatUtils.componentToMM(config.getConfigurationSection("mention.output")),
                        config.getString("mention.sound","BLOCK_NOTE_BLOCK_PLING"),
                        config.getBoolean("mention./togglemention",true),
                        config.getBoolean("mention.output-for-everyone",true),
                        config.getConfigurationSection("mention.custom-mentions"))
            : null;
        msgManager = config.getBoolean("msg.enabled",true)
                ? new MsgManager(this,ChatUtils.componentToMM(config.getConfigurationSection("msg.sender")),
                        ChatUtils.componentToMM(config.getConfigurationSection("msg.viewer")),
                        config.getDouble("msg.cooldown",0),
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

        cooldownTime = config.getDouble("cooldown",0);

        toggleCmd = config.getBoolean("/togglechat",true);
        toggled = ChatUtils.registerToggleCmd(toggleCmd,"chat-off","togglechat","chat-status",p->hasChatToggled((TabPlayer) p) ? "Off" : "No");

        ignoreCmd = config.getBoolean("/ignore",true);
        if (ignoreCmd) {
            Map<String, List<String>> ignored = plugin.getPlayerData().getConfigurationSection("ignored");
            ignored.forEach((player, ignoredList) -> this.ignored.put(UUID.fromString(player), ignoredList.stream().map(UUID::fromString).collect(Collectors.toCollection(ArrayList::new))));
        }

        clearchatEnabled = config.getBoolean("clearchat.enabled",false);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");
    }

    /**
     * TODO:
     * /emojis
     * Custom Interactions
     */

    @Override
    public void unload() {
        if (emojiManager != null) emojiManager.unload();
        if (mentionManager != null) mentionManager.unload();
        if (msgManager != null) msgManager.unload();
        if (socialSpyManager != null) socialSpyManager.unload();
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
    public void onJoin(TabPlayer player) {
        if (emojiManager != null && emojiManager.isAutoCompleteEnabled() && !emojiManager.hasCmdToggled(player))
            emojiManager.loadAutoComplete(player);
    }

    @Override
    public boolean onCommand(TabPlayer p, String cmd) {
        if (cmd.startsWith("/emojis") || cmd.equals("/toggleemojis")) return emojiManager != null && emojiManager.onCommand(p,cmd);
        if (cmd.equals("/togglemention")) return mentionManager != null && mentionManager.onCommand(p,cmd);
        if (cmd.equals("/togglemsg") || cmd.startsWith("/reply") || cmd.startsWith("/r") || cmd.startsWith("/msg"))
            return msgManager != null && msgManager.onCommand(p,cmd);
        if (cmd.equals("/socialspy")) return p.hasPermission("tabadditions.chat.clearchat") && socialSpyManager != null && socialSpyManager.onCommand(p,cmd);

        TranslationFile msgs = plugin.getTranslation();
        if (cmd.equals("/togglechat")) return plugin.toggleCmd(toggleCmd,p,toggled,msgs.chatOn,msgs.chatOff);
        if (cmd.equals("/clearchat")) {
            if (!clearchatEnabled || !p.hasPermission("tabadditions.chat.clearchat")) return false;

            String linebreaks = ("\n"+clearChatLine)
                    .repeat(clearChatAmount)
                    +"\n"+msgs.getChatCleared(p);
            for (TabPlayer all : tab.getOnlinePlayers())
                all.sendMessage(linebreaks,false);
            return true;
        }
        if (cmd.startsWith("/ignore")) {
            if (!ignoreCmd) return false;
            if (!cmd.startsWith("/ignore ")) {
                p.sendMessage(msgs.providePlayer, true);
                return true;
            }
            String player = cmd.substring(cmd.indexOf(8)).toLowerCase();
            if (p.getName().equalsIgnoreCase(player)) {
                p.sendMessage(msgs.cantIgnoreSelf,true);
                return true;
            }
            UUID playerUUID = plugin.getPlayer(player).getUniqueId();
            List<UUID> ignored = this.ignored.computeIfAbsent(p.getUniqueId(), uuid->new ArrayList<>());
            if (ignored.contains(playerUUID))
                ignored.remove(playerUUID);
            else ignored.add(playerUUID);
            p.sendMessage(msgs.getIgnore(player,ignored.contains(playerUUID)), true);
            return true;
        }

        return false;
    }

    public void onChat(TabPlayer sender, String message) {
        if (isMuted(sender)) return;

        if (cooldown.containsKey(sender.getUniqueId())) {
            long time = ChronoUnit.SECONDS.between(cooldown.get(sender.getUniqueId()),LocalDateTime.now());
            if (time < cooldownTime) {
                sender.sendMessage(plugin.getTranslation().getCooldown(cooldownTime-time), true);
                return;
            }
        }
        if (cooldownTime != 0 && !sender.hasPermission("tabadditions.chat.bypass.cooldown"))
            cooldown.put(sender.getUniqueId(),LocalDateTime.now());

        tab.sendConsoleMessage(sender.getName()+"» "+message,true);
        chatPlaceholder.updateValue(sender,message);
        TAB.getInstance().getCPUManager().runTaskLater(msgPlaceholderStay,this,"update %rel_chat% for "+sender.getName(),()->{
            if (chatPlaceholder.getLastValue(sender).equals(message))
                chatPlaceholder.updateValue(sender,"");
        });

        ChatFormat format = getFormat(sender);
        if (format == null) return;
        String text = format.getText();
        if (plugin.getPlatform().isPluginEnabled("InteractiveChat"))
            try {text = InteractiveChatAPI.markSender(text,sender.getUniqueId());}
            catch (IllegalStateException ignored) {}

        for (TabPlayer viewer : tab.getOnlinePlayers()) {
            if (socialSpyManager != null) socialSpyManager.process(sender,viewer,message,socialSpyManager.isSpying(sender,viewer,format));
            if (canSee(sender,viewer,format)) sendMessage(viewer, createMessage(sender, viewer, message, text));
        }
    }

    public void sendMessage(TabPlayer player, Component component) {
        plugin.getPlatform().getAudience(player).sendMessage(component);
    }
    public Component createMessage(TabPlayer sender, TabPlayer viewer, String message, String text) {
        String output = plugin.parsePlaceholders(text,sender,viewer).replace("%msg%", process(sender,viewer,message));
        output = ChatUtils.toMMColors(output);
        return mm.deserialize(output);
    }

    private String process(TabPlayer sender, TabPlayer viewer, String message) {
        //message = commentMMTags(message);
        if (emojiManager != null) message = emojiManager.process(sender,viewer,message);
        if (mentionManager != null) message = mentionManager.process(sender,viewer,message);
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

    public boolean canSee(TabPlayer sender, TabPlayer viewer, ChatFormat f) {
        if (sender == viewer) return true;
        if (viewer == null) return f.getChannel().equals("") && !f.hasViewCondition();
        if (!f.getChannel().equals(getFormat(viewer).getChannel())) return false;
        return f.isViewConditionMet(sender, viewer);
    }
}
