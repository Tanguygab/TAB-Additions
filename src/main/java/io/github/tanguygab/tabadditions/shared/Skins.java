package io.github.tanguygab.tabadditions.shared;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Skins {

    public static Skins instance;
    public static Map<String,String[]> map = new HashMap<>();

    public Skins() {
        instance = this;
    }

    public static Skins getInstance() {
        return instance;
    }

    public String[] getPropPlayer(String name) {
        if (containsConfig("player-head:"+name))
            return getConfig("player-head:"+name);
        try {
            URL url_0 = new URL("https://api.ashcon.app/mojang/v2/user/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            JsonElement parser = new JsonParser().parse(reader_0);
            if (!parser.isJsonObject())
                return new String[]{"null","null"};

            JsonObject skin = parser.getAsJsonObject().get("textures").getAsJsonObject().get("raw").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            String[] finalskin = new String[]{value,signature};
            setConfig("player-head:"+name,finalskin);
            return finalskin;
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getPropSkin(int id) {
        if (containsConfig("mineskin:"+id))
            return getConfig("mineskin:"+id);
        try {
            URL url_0 = new URL("https://api.mineskin.org/get/id/" + id);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            JsonElement parser = new JsonParser().parse(reader_0);
            if (!parser.isJsonObject())
                return new String[]{"null","null"};
            JsonObject skin = parser.getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            String[] finalskin = new String[]{value,signature};
            setConfig("mineskin:"+id,finalskin);
            return finalskin;
        } catch (Exception e) {
            return null;
        }
    }

    public String[] generateFromTexture(String texture) {
        if (containsConfig("texture:"+texture))
            return getConfig("texture:"+texture);
        try {
            URL url = new URL("https://api.mineskin.org/generate/url/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "ExampleApp/v1.0");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            String jsonInputString = "{\"variant\":\"classic\",\"name\":\"string\",\"visibility\":0,\"url\":\"http://textures.minecraft.net/texture/"+texture+"\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            InputStreamReader reader = new InputStreamReader(con.getInputStream());
            JsonElement parser = new JsonParser().parse(reader);
            if (!parser.isJsonObject())
                return new String[]{"null","null"};
            JsonObject skin = parser.getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            String[] finalskin = new String[]{value,signature};
            setConfig("texture:"+texture,finalskin);
            return finalskin;
        } catch (Exception e) {
            return null;
        }
    }

    public Object getIcon(String icon, TabPlayer p) {
        icon = TABAdditions.getInstance().parsePlaceholders(icon, p);
        Object skin = null;
        String[] props = null;
        if (icon == null || icon.equals("")) return null;
        if (icon.startsWith("texture:")) {
            icon = icon.replace("texture:","");
            props = generateFromTexture(icon);
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
        if (skin == null && props != null && props.length >= 2 && !props[0].equals("") && !props[1].equals("")) {
            skin = TABAdditions.getInstance().getPlatform().getSkin(props);
        }
        return skin;
    }

    public boolean containsConfig(String key) {
        return map.containsKey(key) || TABAdditions.getInstance().getConfig(ConfigType.SKINS).hasConfigOption(key);
    }

    public String[] getConfig(String key) {
        if (map.containsKey(key))
            return map.get(key);

        if (containsConfig(key)) {
            Object output = TABAdditions.getInstance().getConfig(ConfigType.SKINS).getObject(key);
            if (output instanceof String[])
                return (String[]) output;
            if (output instanceof ArrayList)
                return ((ArrayList<String>) output).toArray(new String[0]);
        }
        return null;
    }

    public void setConfig(String key, String[] value) {
        map.put(key,value);
    }

    public void unload() {
        for (String key : map.keySet())
            TABAdditions.getInstance().getConfig(ConfigType.SKINS).set(key, new ArrayList<>(Arrays.asList(map.get(key))));
    }
}
