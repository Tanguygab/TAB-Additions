package io.github.tanguygab.tabadditions.shared.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Skins {

    public static Skins instance;
    public final Map<String,Object> icons = new HashMap<>();

    public boolean task = false;
    public int attempts = 3;

    public Skins() {
        instance = this;
    }

    public static Skins getInstance() {
        return instance;
    }

    public String[] getPropPlayer(String name) {
        try {
            if (attempts < 1) {
                if (!task) {
                    task = true;
                    TABAdditions.getInstance().getPlatform().AsyncTask(() -> {
                        attempts = 3;
                        task = false;
                    }, 1000);
                }
                return new String[]{"",""};
            }
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            attempts = attempts-1;
            JsonElement parser = new JsonParser().parse(reader_0);
            if (!parser.isJsonObject())
                return new String[]{"null","null"};
            String uuid = parser.getAsJsonObject().get("id").getAsString();

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

    public String[] getPropSkin(int id) {
        try {
            URL url_0 = new URL("https://api.mineskin.org/get/id/" + id);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            JsonElement parser = new JsonParser().parse(reader_0);
            if (!parser.isJsonObject())
                return new String[]{"null","null"};
            JsonObject skin = parser.getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            return new String[]{value,signature};
        } catch (Exception e) {
            return null;
        }
    }

    public Object getIcon(String icon, TabPlayer p) {
        icon = TABAdditions.getInstance().parsePlaceholders(icon, p);
        if (icons.containsKey(icon) && icons.get(icon) != null)
            return icons.get(icon);
        String deficon = icon;
        Object skin = null;
        String[] props = null;
        if (icon == null || icon.equals("")) return null;
        if (icon.startsWith("base64:")) {
            icon = icon.replace("base64:","");
            props = new String[]{icon,"base64"};
        }
        if (icon.startsWith("player-head:")) {
            icon = icon.replace("player-head:", "");
            if (TAB.getInstance().getPlayer(icon) != null)
                skin = TAB.getInstance().getPlayer(icon).getSkin();
            props = getPropPlayer(icon);

        }
        else if (icon.startsWith("mineskin:")) {
            icon = icon.replace("mineskin:", "");
            try {
                int mineskinid = Integer.parseInt(icon);
                props = getPropSkin(mineskinid);
            }
            catch (NumberFormatException ignored) {}
        }
        if (skin == null && props != null && !props[0].equals("") && !props[1].equals("")) {
            skin = TABAdditions.getInstance().getPlatform().getSkin(props);
        }
        icons.put(deficon,skin);
        return skin;
    }
}
