package io.github.tanguygab.tabadditions.spigot.Features;


import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class NMS {

    public static Object asPropertyMap(String[] props) {
        PropertyMap properties = new PropertyMap();
        Property property = new Property("textures",props[0],props[1]);
        properties.put("textures",property);
        return properties;
    }

}