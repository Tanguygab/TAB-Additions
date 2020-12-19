package io.github.tanguygab.tabadditions.spigot;

import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class NMS {

    public static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);

    public static Object getPropPlayer(String name) {
        try {
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

            URL url_1 = new URL("https://api.mineskin.org/generate/user/" + uuid);
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject skin = new JsonParser().parse(reader_1).getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            PropertyMap properties = new PropertyMap();
            Property property = new Property("textures",value,signature);
            properties.put("textures",property);
            return properties;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getPropSkin(int id) {
        try {
            URL url_0 = new URL("https://api.mineskin.org/get/id/" + id);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            JsonObject skin = new JsonParser().parse(reader_0).getAsJsonObject().get("data").getAsJsonObject().get("texture").getAsJsonObject();
            String value = skin.get("value").getAsString();
            String signature = skin.get("signature").getAsString();

            PropertyMap properties = new PropertyMap();
            Property property = new Property("textures",value,signature);
            properties.put("textures",property);
            return properties;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}