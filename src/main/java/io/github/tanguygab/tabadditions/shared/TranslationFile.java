package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiCategory;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TranslationFile extends YamlConfigurationFile {

    public final String providePlayer = getString("player.provide","&cYou have to provide a player name!");
    public final String playerNotFound = getString("player.offline", "&cNo online player found with the name \"%player%\"");

    public final String actionBarOn = getString("actionbars.on", "&aYou will now receive new actionbars!");
    public final String actionBarOff = getString("actionbars.off", "&cYou won't receive any new actionbar!");
    public final String titleOn = getString("titles.on", "&aYou will now receive new titles!");
    public final String titleOff = getString("titles.off", "&cYou won't receive any new title!");

    public final String emojisOn = getString("emojis.on", "&aYou will now see emojis in chat!");
    public final String emojisOff = getString("emojis.off", "&cYou won't see any new emoji in chat!");
    private final String emojiCategoryHeader = getString("emojis.categories.header","&7All categories of emojis you have access to &8(%amount%, Emojis: %owned%/%max%)&7:");
    private final String emojiCategory = getString("emojis.categories.category","&7 - &8%category% &8(%owned%/%max%)");
    public final String emojiCategoryNotFound = getString("emojis.category.not_found","&7This category doesn't exist.");
    private final String emojiHeader = getString("emojis.category.header","&7All emojis in this category &8(%owned%/%max%)&7:");
    private final String emoji = getString("emojis.category.emoji", "&7 - %emojiraw%&8: &r%emoji%");

    public final String chatOn = getString("chat.on", "&cYou will now receive new chat messages!");
    public final String chatOff = getString("chat.off", "&aYou won't receive any new chat message!");
    private final String chatCooldown = getString("chat.cooldown", "&cYou have to wait %seconds% more seconds!");
    private final String chatCleared = getString("chat.cleared", "&aChat cleared by %name%!");
    private final String CHAT_CMD_JOIN = getString("chat.commands-formats.join", "&7You joined %name%!");
    private final String CHAT_CMD_LEAVE = getString("chat.commands-formats.leave", "&7You left %name%!");

    private final String ignoreOn = getString("ignore.on", "&cYou won't receive any new message from %name%!");
    private final String ignoreOff = getString("ignore.off", "&aYou will now receive new messages from %name%!");
    public final String cantIgnoreSelf = getString("ignore.self", "&cYou can't ignore yourself!");
    public final String isIgnored = getString("ignored.is", "&cThis player ignores you");

    public final String mentionOn = getString("mentions.on", "&aMentions enabled.");
    public final String mentionOff = getString("mentions.off", "&cMentions disabled.");

    public final String pmOn = getString("msg.on", "&cYou will now receive new private messages!");
    public final String pmOff = getString("msg.off", "&aYou won't receive any new private message!");
    private final String pmCooldown = getString("msg.cooldown", "&cYou have to wait %seconds% more seconds!");
    public final String cantPmSelf = getString("msg.self", "&cYou can't message yourself!");
    public final String pmEmpty = getString("msg.empty", "&7You have to provide a message!");
    public final String noPlayerToReplyTo = getString("msg.no_reply", "&cYou don't have any player to reply to!");
    public final String hasPmOff = getString("msg.has_off", "&cThis player doesn't accept private messages");


    public final String socialSpyOn = getString("socialspy.on", "&aSocialSpy enabled.");
    public final String socialSpyOff = getString("socialspy.off", "&cSocialSpy disabled.");

    public final String cantSwear = getString("msg.cant-swear","&cYou are not allowed to swear on this server!");

    public TranslationFile(InputStream source, File destination) throws IOException {
        super(source,destination);
    }

    public String getChatCleared(TabPlayer p) {
        return chatCleared.replace("%name%",p.getName());
    }

    public String getCooldown(double time) {
        return chatCooldown.replace("%seconds%", String.valueOf(time));
    }
    public String getIgnore(String ignored, boolean on) {
        return (on ? ignoreOn : ignoreOff).replace("%name%",ignored);
    }
    public String getPmCooldown(double time) {
        return pmCooldown.replace("%seconds%", String.valueOf(time));
    }
    public String getPlayerNotFound(String player) {
        return playerNotFound.replace("%player%", player);
    }

    public String getEmojiCategory(TabPlayer p, EmojiCategory category) {
        return emojiCategory
                .replace("%category%",category.getName())
                .replace("%owned%", String.valueOf(category.ownedEmojis(p)))
                .replace("%max%", String.valueOf(category.getEmojis().size()));
    }

    public String getEmojiCategoryHeader(int amount, int owned, int total) {
        return emojiCategoryHeader
                .replace("%amount%", String.valueOf(amount))
                .replace("%owned%", String.valueOf(owned))
                .replace("%max%", String.valueOf(total));
    }
    public String getEmoji(String emojiraw, String emoji) {
        return this.emoji.replace("%emojiraw%", emojiraw)
                .replace("%emoji%", emoji);
    }

    public String getEmojiHeader(int owned, int max) {
        return emojiHeader.replace("%owned%", String.valueOf(owned))
                .replace("%max%", String.valueOf(max));
    }

    public String getChatCmdJoin(String name) {
        return CHAT_CMD_JOIN.replace("%name%",name);
    }
    public String getChatCmdLeave(String name) {
        return CHAT_CMD_LEAVE.replace("%name%",name);
    }

}
