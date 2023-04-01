package io.github.tanguygab.tabadditions.shared.features.chat.mentions;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.chat.Manager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MentionManager extends Manager {

    private final String input;
    private final String output;
    private final String sound;
    private final boolean toggleMentionCmd;
    private final boolean outputForEveryone;
    private final Map<String,CustomMention> mentions = new HashMap<>();
    public final List<String> toggleMention = new ArrayList<>();

    public MentionManager(ChatManager cm, String input, String output, String sound, boolean toggleMentionCmd, boolean outputForEveryone, Map<String,Map<String,String>> customMentions) {
        super(cm);
        this.input = input;
        this.output = output;
        this.sound = sound;
        this.toggleMentionCmd = toggleMentionCmd;
        PlaceholderManager pm = tab.getPlaceholderManager();
        if (toggleMentionCmd) {
            toggleMention.addAll(tab.getPlayerCache().getStringList("togglemention", new ArrayList<>()));
            pm.registerPlayerPlaceholder("%chat-mentions%",1000,p->hasMentionsToggled(p) ? "Off" : "On");
            instance.getPlatform().registerCommand("togglemention",true);
        }
        this.outputForEveryone = outputForEveryone;
        if (customMentions != null)
            customMentions.forEach((mention,cfg)-> mentions.put(mention,new CustomMention(cfg.get("input"),cfg.get("output"),Condition.getCondition(cfg.get("condition")),cfg.get("sound"))));
    }

    public void unload() {
        if (toggleMentionCmd)
            tab.getPlayerCache().set("togglemention", toggleMention);
    }

    public boolean isToggleMentionCmd() {
        return toggleMentionCmd;
    }

    public boolean hasMentionsToggled(TabPlayer viewer) {
        return toggleMention.contains(viewer.getName().toLowerCase());
    }

    public boolean isMentioned(String msg, TabPlayer sender, TabPlayer viewer) {
        String input = instance.parsePlaceholders(this.input,viewer);
        if (input.equals("") || viewer == null) return false;
        if (!sender.hasPermission("tabadditions.chat.bypass.togglemention") && hasMentionsToggled(viewer)) return false;
        if (!sender.hasPermission("tabadditions.chat.bypass.ignore") && cm.cmds.isIgnored(sender,viewer)) return false;
        return msg.toLowerCase().contains(input.toLowerCase());
    }

    //is there a better way to do this? I have no fucking clue as to what I've just done
    public String process(String msg, TabPlayer sender, TabPlayer viewer, String hoverclick) {
        //checking custom mentions
        String finalMsg = msg;
        List<CustomMention> mentions = this.mentions.values().stream().filter(mention->mention.matches(finalMsg)).collect(Collectors.toList());
        List<TabPlayer> mentioned = new ArrayList<>();
        for (TabPlayer p : tab.getOnlinePlayers()) {
            CustomMention mention = mentions.stream().findFirst().orElse(null);
            if (mention == null || !mention.isConditionMet(p)) continue;
            mentioned.add(p);
            instance.getPlatform().sendSound(p,mention.getSound());
        }
        for (CustomMention mention : mentions) msg = mention.replace(msg);

        //checking player mentions
        boolean check = false;
        if (outputForEveryone)
            for (TabPlayer p : tab.getOnlinePlayers())
                if (isMentioned(msg,sender,p)) {
                    check = true;
                    break;
                }
        else if (isMentioned(msg,sender,viewer)) {
                    check = true;
                    instance.getPlatform().sendSound(p,sound);
                }

        return check ? msg
                : msg.replaceAll("(?i)"+ Pattern.quote(instance.parsePlaceholders(input,viewer)),
                    Matcher.quoteReplacement(hoverclick+instance.parsePlaceholders(cm.removeSpaces(this.output),sender,viewer)+"{"));
    }
}
