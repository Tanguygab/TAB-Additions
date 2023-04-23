package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.shared.config.file.YamlConfigurationFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TranslationFile extends YamlConfigurationFile {

    public final String actionBarOn = getString("actionbars_on", "&aYou will now receive new actionbars!");
    public final String actionBarOff = getString("actionbars_off", "&cYou won't receive any new actionbars!");
    public final String titleOn = getString("titles_on", "&aYou will now receive new titles!");
    public final String titleOff = getString("titles_off", "&cYou won't receive any new titles!");


    public TranslationFile(InputStream source, File destination) throws IOException {
        super(source,destination);
    }
}
