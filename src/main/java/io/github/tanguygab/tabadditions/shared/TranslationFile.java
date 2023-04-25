package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TranslationFile extends YamlConfigurationFile {

    public final String actionBarOn = getString("actionbars_on", "&aYou will now receive new actionbars!");
    public final String actionBarOff = getString("actionbars_off", "&cYou won't receive any new actionbar!");
    public final String titleOn = getString("titles_on", "&aYou will now receive new titles!");
    public final String titleOff = getString("titles_off", "&cYou won't receive any new title!");
    public final String emojisOn = getString("emojis_on", "&aYou will now see emojis in chat!");
    public final String emojisOff = getString("emojis_off", "&cYou won't see any emoji in chat!");
    private final String chatCleared = getString("chat_cleared", "&aChat cleared by %name%!");


    public TranslationFile(InputStream source, File destination) throws IOException {
        super(source,destination);
    }

    public String getChatCleared(TabPlayer p) {
        return chatCleared.replace("%name%",p.getName());
    }
}
