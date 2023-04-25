package io.github.tanguygab.tabadditions.shared.features.chat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiManager;
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
import java.util.LinkedHashMap;
import java.util.Map;

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
                    channel,
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
                        config.getBoolean("emojis./toggleemoji",true)
        ) : null;

        clearchatEnabled = config.getBoolean("clearchat.enabled",false);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");
    }

    @Override
    public void unload() {
        if (emojiManager != null) emojiManager.unload();
    }

    public ChatFormat getFormat(TabPlayer player) {
        for (ChatFormat format : formats.values())
            if (format.isConditionMet(player))
                return format;
        return null;
    }

    @Override
    public void onJoin(TabPlayer player) {
        if (emojiManager != null && emojiManager.isAutoCompleteEnabled() && !emojiManager.hasEmojisToggled(player))
            emojiManager.loadAutoComplete(player);
    }

    @Override
    public boolean onCommand(TabPlayer p, String cmd) {
        if (cmd.startsWith("/emojis") || cmd.equals("/toggleemojis"))
            return emojiManager != null && emojiManager.onCommand(p,cmd);

        if (cmd.equals("/clearchat")) {
            if (!clearchatEnabled || !p.hasPermission("tabadditions.chat.clearchat")) return false;

            String linebreaks = ("\n"+clearChatLine)
                    .repeat(clearChatAmount)
                    +"\n"+plugin.getTranslation().getChatCleared(p);
            for (TabPlayer all : tab.getOnlinePlayers())
                all.sendMessage(linebreaks,false);
            return true;
        }

        return false;
    }

    public void onChat(TabPlayer sender, String message) {
        if (isMuted(sender)) {
            return;
        }

        tab.sendConsoleMessage(sender.getName()+"» "+message,true);
        chatPlaceholder.updateValue(sender,message);
        TAB.getInstance().getCPUManager().runTaskLater(msgPlaceholderStay,this,"update %rel_chat% for "+sender.getName(),()->{
            if (chatPlaceholder.getLastValue(sender).equals(message))
                chatPlaceholder.updateValue(sender,"");
        });

        ChatFormat format = getFormat(sender);
        String text = format.getText();

        for (TabPlayer viewer : tab.getOnlinePlayers()) {
            String output = plugin.parsePlaceholders(text,sender,viewer).replace("%msg%", process(message,sender,viewer));
            output = ChatUtils.toMMColors(output);
            if (plugin.getPlatform().isPluginEnabled("InteractiveChat"))
                try {output = InteractiveChatAPI.markSender(output,sender.getUniqueId());}
                catch (IllegalStateException ignored) {}

            Component c = mm.deserialize(output);
            plugin.getPlatform().getAudience(viewer).sendMessage(c);
        }
    }

    private String process(String message, TabPlayer sender, TabPlayer viewer) {
        //comment MM tags
        if (emojiManager != null) message = emojiManager.process(message,sender,viewer);
        message = chatFormatter.process(sender,message);
        return message;
    }
    public boolean isMuted(TabPlayer p) {
        if (!plugin.getPlatform().isPluginEnabled("AdvancedBan")) return false;
        return PunishmentManager.get().isMuted(UUIDManager.get().getMode() != UUIDManager.FetcherMode.DISABLED
                ? p.getUniqueId().toString().replace("-", "")
                : p.getName().toLowerCase());
    }
}
