package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.MessageFile;

public class TranslationFile {

    private final MessageFile tabMsgs;

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


    public TranslationFile(ConfigurationFile file) {
        tabMsgs = TAB.getInstance().getConfiguration().getMessages();

        providePlayer = file.getString("provide_player","&cYou have to provide a player name!");
        chatCleared = file.getString("chat_cleared", "&aChat cleared by %name%!");
        cooldown = file.getString("message_cooldown", "&cYou have to wait %seconds% more seconds!");

        actionBarOn = file.getString("actionbars_on", "&aYou will now receive new actionbars!");
        actionBarOff = file.getString("actionbars_off", "&cYou won't receive any new actionbars!");

        titleOn = file.getString("titles_on", "&aYou will now receive new titles!");
        titleOff = file.getString("titles_off", "&cYou won't receive any new titles!");

        emojiOn = file.getString("emojis_on", "&aEmojis enabled.");
        emojiOff = file.getString("emojis_off", "&cEmojis disabled.");
        emojiCategoryNotFound = file.getString("emojis_category_not_found","&7This category doesn't exist.");
        emojiCategory = file.getString("emojis_category","&7 - &8%category% (%owned%/%max%)");
        emojiCategoryHeader = file.getString("emojis_categories_header","&7All categories of emojis you have access to (%amount%, Emojis: %owned%/%max%):");
        emoji = file.getString("emojis_emoji", "&7 - %emojiraw%&8: &r%emoji%");
        emojiHeader = file.getString("emojis_emoji_header","&7All emojis in this category (%owned%/%max%):");

        socialSpyOn = file.getString("socialspy_on", "&aSocialSpy enabled.");
        socialSpyOff = file.getString("socialspy_off", "&cSocialSpy disabled.");

        mentionOn = file.getString("chat_mention_on", "&aMentions enabled.");
        mentionOff = file.getString("chat_mention_off", "&cMentions disabled.");

        pmOn = file.getString("pm_on", "&aYou will now receive new private messages!");
        pmOff = file.getString("pm_off", "&cYou won't receive any new private messages!");
        pmCooldown = file.getString("pm_cooldown", "&cYou have to wait %seconds% more seconds!");
        cantPmSelf = file.getString("cant_pm_self", "&cYou can't message yourself!");
        pmEmpty = file.getString("pm_empty", "&7You have to provide a message!");
        hasPmOff = file.getString("has_pm_off", "&cThis player doesn't accept private messages");

        ignoreOn = file.getString("ignore_on", "&cYou won't receive any new private messages from %name%!");
        ignoreOff = file.getString("ignore_off", "&aYou will now receive new private messages from %name%!");
        isIgnored = file.getString("is_ignored", "&cThis player ignores you");

        cmdJoin = file.getString("cmd_join","&7You joined %name%!");
        cmdLeave = file.getString("cmd_leave","&7You left %name%!");


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
        return tabMsgs.getPlayerNotFound(name);
    }

    public String getNoPermission() {
        return tabMsgs.getNoPermission();
    }

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
