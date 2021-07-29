package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Layout2 {

    public String name;
    private YamlConfigurationFile config;
    protected final List<TabPlayer> players = new ArrayList<>();

    protected final Map<Integer, PacketPlayOutPlayerInfo.PlayerInfoData> fakeplayers = new HashMap<>();
    private final Map<Integer,Map<String,Object>> placeholders = new HashMap<>();
    private final Map<Object, List<Integer>> playersets = new HashMap<>();
    private final Map<Object, List<Integer>> lists = new HashMap<>();



}
