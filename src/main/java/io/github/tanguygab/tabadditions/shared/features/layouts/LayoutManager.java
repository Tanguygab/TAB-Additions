package io.github.tanguygab.tabadditions.shared.features.layouts;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.spigot.Features.NMS;
import io.github.tanguygab.tabadditions.spigot.SpigotTA;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutManager {

    private static LayoutManager instance;
    private final Map<String, Layout> layouts = new HashMap<>();
    private final Map<String,Object> icons = new HashMap<>();
    private final Map<TabPlayer,String> players = new HashMap<>();
    private Integer task;
    public final Map<TabPlayer,String> toAdd = new HashMap<>();
    public final Map<TabPlayer,String> toRemove = new HashMap<>();

    public LayoutManager() {
        instance = this;
        for (Object layout : SharedTA.layoutConfig.getConfigurationSection("layouts").keySet())
            layouts.put(layout.toString(),new Layout(layout.toString()));
        refresh();
    }

    public static LayoutManager getInstance() {
        return instance;
    }
    public String getLayout(TabPlayer p) {
        String layout = SharedTA.layoutConfig.getString("default-layout","");
        if (layout.equalsIgnoreCase("")) return "null";

        Layout l = layouts.get(layout);
        while (l != null && !l.isConditionMet(p)) {
            l = layouts.get(l.getChildLayout());
            if (l == null) return "null";
            layout = l.getName();
        }
        return layout;
    }

    public void unregister() {
        SharedTA.platform.cancelTask(task);
        removeLayoutAll();
        removeLayout();
    }

    private void refresh() {
        task = SharedTA.platform.AsyncTask(() -> {
            for (TabPlayer p : Shared.getPlayers()) {
                if (players.containsKey(p) && !players.get(p).equals(getLayout(p))) {
                    toRemove.put(p, players.get(p));
                    toAdd.put(p,getLayout(p));
                }
            }
            removeLayout();
            showLayout();
            for (Layout layout : layouts.values()) {
                layout.refreshPlaceholders();
                layout.refreshSets();
            }
        },0L,10L);
    }

    public void showLayout() {
        List<TabPlayer> list = new ArrayList<>(toAdd.keySet());
        for (TabPlayer p : list) {
            Layout layout = layouts.get(toAdd.get(p));
            List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(layout.fakeplayers.values());
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
            players.put(p, layout.getName());
            layout.players.add(p);
            toAdd.remove(p);
        }
    }

    public void removeLayout() {
        List<TabPlayer> list = new ArrayList<>(toRemove.keySet());
        for (TabPlayer p : list) {
            Layout layout = layouts.get(toRemove.get(p));
            List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(layout.fakeplayers.values());
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
            players.remove(p);
            layout.players.remove(p);
            toRemove.remove(p);
        }
    }
    public void showLayoutAll() {
        for (TabPlayer p : Shared.getPlayers())
            toAdd.put(p,getLayout(p));
    }

    public void removeLayoutAll() {
        for (TabPlayer p : Shared.getPlayers())
            toRemove.put(p,getLayout(p));
    }

    protected Object getIcon(String icon,TabPlayer p) {
        icon = Shared.platform.replaceAllPlaceholders(icon, p);
        if (icons.containsKey(icon))
            return icons.get(icon);
        String deficon = icon;
        Object skin = null;
        if (icon.startsWith("player-head:")) {
            icon = icon.replace("player-head:", "");
            if (TABAPI.getPlayer(icon) != null)
                skin = TABAPI.getPlayer(icon).getSkin();
            else if (SharedTA.platform instanceof SpigotTA)
                skin = NMS.getPropPlayer(icon);
        }
        else if (icon.startsWith("mineskin:")) {
            icon = icon.replace("mineskin:", "");
            try {
                int mineskinid = Integer.parseInt(icon);
                if (SharedTA.platform instanceof SpigotTA)
                    skin = NMS.getPropSkin(mineskinid);
            }
            catch (NumberFormatException ignored) {}
        }
        icons.put(deficon,skin);
        return skin;
    }
}
