package io.github.tanguygab.tabadditions.shared.features.chat.mentions;

import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.sound.Sound;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MentionManager extends ChatManager {

    private final String input;
    private final String output;
    private final Sound sound;
    private final boolean outputForEveryone;
    private final Map<String,CustomMention> mentions = new HashMap<>();

    public MentionManager(Chat chat, String input, String output, String sound, boolean toggleCmd, boolean outputForEveryone, Map<String,Map<String,String>> customMentions) {
        super(chat,toggleCmd,"mentions-off","togglementions","chat-mentions");
        this.input = input;
        this.output = output;
        this.sound = ChatUtils.getSound(sound);
        this.outputForEveryone = outputForEveryone;
        if (customMentions != null)
            customMentions.forEach((mention,cfg)-> mentions.put(mention,new CustomMention(cfg.get("input"),cfg.get("output"),Condition.getCondition(cfg.get("condition")),ChatUtils.getSound(cfg.get("sound")))));
    }

    public boolean isMentioned(String msg, TabPlayer sender, TabPlayer viewer) {
        String input = plugin.parsePlaceholders(this.input,viewer);
        if (input.equals("") || viewer == null) return false;
        if (!sender.hasPermission("tabadditions.chat.bypass.togglemention") && hasCmdToggled(viewer)) return false;
        if (chat.isIgnored(sender,viewer)) return false;
        return msg.toLowerCase().contains(input.toLowerCase());
    }

    //is there a better way to do this? I have no fucking clue as to what I've just done
    public String process(TabPlayer sender, TabPlayer viewer, String msg) {
        //checking custom mentions
        String finalMsg = msg;
        List<CustomMention> mentions = this.mentions.values().stream().filter(mention->mention.matches(finalMsg)).collect(Collectors.toList());
        for (TabPlayer p : tab.getOnlinePlayers()) {
            CustomMention mention = mentions.stream().findFirst().orElse(null);
            if (mention != null && mention.isConditionMet(p) && mention.getSound() != null)
                playSound(p,mention.getSound());
        }
        for (CustomMention mention : mentions) msg = mention.replace(msg);

        //checking player mentions
        boolean check = false;
        if (outputForEveryone)
            for (TabPlayer p : tab.getOnlinePlayers()) {
                if (isMentioned(msg, sender, p)) {
                    check = true;
                    break;
                }
            }
        else if (isMentioned(msg,sender,viewer)) {
            check = true;
            playSound(viewer,sound);
        }
        return !check ? msg
                : msg.replaceAll("(?i)"+ Pattern.quote(plugin.parsePlaceholders(input,viewer)),
                Matcher.quoteReplacement(plugin.parsePlaceholders(this.output,sender,viewer)));
    }

    private void playSound(TabPlayer player, Sound sound) {
        chat.kyori.player(player.getUniqueId()).playSound(sound);
    }

    public boolean onCommand(TabPlayer sender, String message) {
        return plugin.toggleCmd(toggleCmd,sender,toggled,translation.mentionOn,translation.mentionOff);
    }

}
