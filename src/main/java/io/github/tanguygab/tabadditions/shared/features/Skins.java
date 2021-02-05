package io.github.tanguygab.tabadditions.shared.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;

public class Skins {

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
}
