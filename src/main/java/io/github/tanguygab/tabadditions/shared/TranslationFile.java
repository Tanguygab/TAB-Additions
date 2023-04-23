package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.shared.config.file.YamlConfigurationFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TranslationFile extends YamlConfigurationFile {

    public final String actionBarOn;
    public final String actionBarOff;

    public final String titleOn;
    public final String titleOff;


    public TranslationFile(InputStream source, File destination) throws IOException {
        super(source,destination);
        actionBarOn = getString("actionbars_on", "&aYou will now receive new actionbars!");
        actionBarOff = getString("actionbars_off", "&cYou won't receive any new actionbars!");
        titleOn = getString("titles_on", "&aYou will now receive new titles!");
        titleOff = getString("titles_off", "&cYou won't receive any new titles!");
    }
}
