package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.YamlConfigurationFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TranslationFile extends YamlConfigurationFile {

    public final String providePlayer;
    private final String chatCleared;
    private final String cooldown;

    public final String actionBarOn;
    public final String actionBarOff;

    public final String titleOn;
    public final String titleOff;

    public final String emojiOn;
    public final String emojiOff;
    public final String emojiCategoryNotFound;
    private final String emojiCategory;
    private final String emojiCategoryHeader;
    private final String emoji;
    private final String emojiHeader;

    public final String socialSpyOn;
    public final String socialSpyOff;

    public final String mentionOn;
    public final String mentionOff;

    public final String pmOn;
    public final String pmOff;
    private final String pmCooldown;
    public final String cantPmSelf;
    public final String pmEmpty;
    public final String hasPmOff;

    public final String ignoreOn;
    public final String ignoreOff;
    public final String isIgnored;

    private final String cmdJoin;
    private final String cmdLeave;

    public final String chatOn;
    public final String chatOff;


    public TranslationFile(InputStream source, File destination) throws IOException {
        super(source,destination);

        providePlayer = getString("provide_player","&cYou have to provide a player name!");
        chatCleared = getString("chat_cleared", "&aChat cleared by %name%!");
        cooldown = getString("message_cooldown", "&cYou have to wait %seconds% more seconds!");

        actionBarOn = getString("actionbars_on", "&aYou will now receive new actionbars!");
        actionBarOff = getString("actionbars_off", "&cYou won't receive any new actionbars!");

        titleOn = getString("titles_on", "&aYou will now receive new titles!");
        titleOff = getString("titles_off", "&cYou won't receive any new titles!");

        emojiOn = getString("emojis_on", "&aEmojis enabled.");
        emojiOff = getString("emojis_off", "&cEmojis disabled.");
        emojiCategoryNotFound = getString("emojis_category_not_found","&7This category doesn't exist.");
        emojiCategory = getString("emojis_category","&7 - &8%category% (%owned%/%max%)");
        emojiCategoryHeader = getString("emojis_categories_header","&7All categories of emojis you have access to (%amount%, Emojis: %owned%/%max%):");
        emoji = getString("emojis_emoji", "&7 - %emojiraw%&8: &r%emoji%");
        emojiHeader = getString("emojis_emoji_header","&7All emojis in this category (%owned%/%max%):");

        socialSpyOn = getString("socialspy_on", "&aSocialSpy enabled.");
        socialSpyOff = getString("socialspy_off", "&cSocialSpy disabled.");

        mentionOn = getString("chat_mention_on", "&aMentions enabled.");
        mentionOff = getString("chat_mention_off", "&cMentions disabled.");

        pmOn = getString("pm_on", "&aYou will now receive new private messages!");
        pmOff = getString("pm_off", "&cYou won't receive any new private messages!");
        pmCooldown = getString("pm_cooldown", "&cYou have to wait %seconds% more seconds!");
        cantPmSelf = getString("cant_pm_self", "&cYou can't message yourself!");
        pmEmpty = getString("pm_empty", "&7You have to provide a message!");
        hasPmOff = getString("has_pm_off", "&cThis player doesn't accept private messages");

        ignoreOn = getString("ignore_on", "&cYou won't receive any new private messages from %name%!");
        ignoreOff = getString("ignore_off", "&aYou will now receive new private messages from %name%!");
        isIgnored = getString("is_ignored", "&cThis player ignores you");

        cmdJoin = getString("cmd_join","&7You joined %name%!");
        cmdLeave = getString("cmd_leave","&7You left %name%!");

        chatOn = getString("chat_on", "&aYou will now receive chat messages!");
        chatOff = getString("chat_off", "&cYou won't receive any new chat messages!");
    }

    public String getChatCleared(TabPlayer p) {
        return chatCleared.replace("%name%",p.getName());
    }

    public String getEmojiCategory(String category, int owned, int max) {
        return emojiCategory
                .replace("%category%",category)
                .replace("%owned%",owned+"")
                .replace("%max%",max+"");
    }

    public String getEmojiCategoryHeader(int amount, int owned, int max) {
        return emojiCategoryHeader
                .replace("%amount%",amount+"")
                .replace("%owned%",owned+"")
                .replace("%max%",max+"");
    }

    public String getIgnoreOn(String ignoredP) {
        return ignoreOn.replace("%name%", ignoredP);
    }

    public String getIgnoreOff(String ignoredP) {
        return ignoreOff.replace("%name%", ignoredP);
    }

    public String getCooldown(long time) {
        return cooldown.replace("%seconds%",time+"");
    }

    public String getPmCooldown(long time) {
        return pmCooldown.replace("%seconds%",time+"");
    }

    public String getPlayerNotFound(String name) {
        return getString("player-not-online", "&cNo online player found with the name \"%player%\"").replace("%player%", name);
    }

    public String NO_PERMISSIONS = getString("no-permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");

    public String getCmdJoin(String name) {
        return cmdJoin.replace("%name%",name);
    }

    public String getCmdLeave(String name) {
        return cmdLeave.replace("%name%",name);
    }

    public String getEmoji(String emojiraw, String emoji) {
        return this.emoji.replace("%emojiraw%", emojiraw)
                .replace("%emoji%", emoji);
    }

    public String getEmojiHeader(int owned, int max) {
        return emojiHeader.replace("%owned%",owned+"")
                .replace("%max%",max+"");
    }
}
