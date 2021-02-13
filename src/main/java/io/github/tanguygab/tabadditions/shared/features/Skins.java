package io.github.tanguygab.tabadditions.shared.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Skins {

    public final static Map<String,Object> icons = new HashMap<>();

    public static String[] getPropPlayer(String name) {
        try {
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

            URL url_1 = new URL("https://api.mineskin.org/generate/user/" + uuid);
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject skin = new JsonParser().parse(reader_1).getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            return new String[]{value,signature};
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] getPropSkin(int id) {
        try {
            URL url_0 = new URL("https://api.mineskin.org/get/id/" + id);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            JsonObject skin = new JsonParser().parse(reader_0).getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            return new String[]{value,signature};
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getIcon(String icon, TabPlayer p) {
        icon = TABAdditions.getInstance().parsePlaceholders(icon, p);
        if (icons.containsKey(icon))
            return icons.get(icon);
        String deficon = icon;
        Object skin = null;
        String[] props = null;
        if (icon == null || icon.equals("")) return null;
        if (icon.startsWith("player-head:")) {
            icon = icon.replace("player-head:", "");
            if (TAB.getInstance().getPlayer(icon) != null)
                skin = TAB.getInstance().getPlayer(icon).getSkin();
            props = Skins.getPropPlayer(icon);

        }
        else if (icon.startsWith("mineskin:")) {
            icon = icon.replace("mineskin:", "");
            try {
                int mineskinid = Integer.parseInt(icon);
                props = Skins.getPropSkin(mineskinid);
            }
            catch (NumberFormatException ignored) {}
        }
        if (skin == null && props != null) {
            skin = TABAdditions.getInstance().getPlatform().getSkin(props);
        }
        icons.put(deficon,skin);
        return skin;
    }
}
