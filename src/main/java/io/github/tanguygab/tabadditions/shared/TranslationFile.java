package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TranslationFile extends YamlConfigurationFile {

    public final String providePlayer = getString("provide_player","&cYou have to provide a player name!");

    public final String actionBarOn = getString("actionbars.on", "&aYou will now receive new actionbars!");
    public final String actionBarOff = getString("actionbars.off", "&cYou won't receive any new actionbar!");
    public final String titleOn = getString("titles.on", "&aYou will now receive new titles!");
    public final String titleOff = getString("titles.off", "&cYou won't receive any new title!");

    public final String emojisOn = getString("emojis.on", "&aYou will now see emojis in chat!");
    public final String emojisOff = getString("emojis.off", "&cYou won't see any new emoji in chat!");
    public final String chatOn = getString("chat.on", "&cYou will now receive new chat messages!");
    public final String chatOff = getString("chat.off", "&aYou won't receive any new chat message!");
    private final String chatCooldown = getString("chat.cooldown", "&cYou have to wait %seconds% more seconds!");
    private final String chatCleared = getString("chat.cleared", "&aChat cleared by %name%!");

    private final String ignoreOn = getString("ignore.on", "&cYou won't receive any new private message from %name%!");
    private final String ignoreOff = getString("ignore.off", "&aYou will now receive new private messages from %name%!");
    public final String cantIgnoreSelf = getString("ignore.self", "&cYou can't ignore yourself!");
    public final String isIgnored = getString("ignored.is", "&cThis player ignores you");

    public final String mentionOn = getString("mentions.on", "&aMentions enabled.");
    public final String mentionOff = getString("mentions.off", "&cMentions disabled.");


    public TranslationFile(InputStream source, File destination) throws IOException {
        super(source,destination);
    }

    public String getChatCleared(TabPlayer p) {
        return chatCleared.replace("%name%",p.getName());
    }

    public String getCooldown(double time) {
        return chatCooldown.replace("%seconds%",time+"");
    }
    public String getIgnore(String ignored, boolean on) {
        return (on ? ignoreOn : ignoreOff).replace("%name%",ignored);
    }
}
