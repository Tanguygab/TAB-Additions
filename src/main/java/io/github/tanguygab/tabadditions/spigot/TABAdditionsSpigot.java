package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.platforms.bukkit.event.TabLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class TABAdditionsSpigot extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new SpigotPlatform(this), this,getDataFolder()));
        TABAdditions.getInstance().load();
        getCommand("tabadditions").setExecutor(new MainCmd());
    }

    @EventHandler
    public void onTABLoad(TabLoadEvent e) {
        TABAdditions.getInstance().getPlatform().reload();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        TabAPI tab = TabAPI.getInstance();
        if (!tab.getFeatureManager().isFeatureEnabled("Chat")) return;
        e.setCancelled(true);
        ((ChatManager)tab.getFeatureManager().getFeature("Chat")).onChat(tab.getPlayer(e.getPlayer().getUniqueId()),e.getMessage());
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

    public String itemStack(ItemStack item) {
        String pack = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + pack + ".ItemStack")
                    .getMethod("save", Class.forName("net.minecraft.server." + pack + ".NBTTagCompound"))
                    .invoke(Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack")
                                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item),
                            Class.forName("net.minecraft.server." + pack + ".NBTTagCompound")
                                    .getConstructor().newInstance()).toString();
        } catch (Exception e) {
            try {
                return Class.forName("net.minecraft.world.item.ItemStack")
                        .getMethod("save", Class.forName("net.minecraft.nbt.NBTTagCompound"))
                        .invoke(Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack")
                                        .getMethod("asNMSCopy", ItemStack.class).invoke(null, item),
                                Class.forName("net.minecraft.nbt.NBTTagCompound")
                                        .getConstructor().newInstance()).toString();
            } catch (Exception e2) {
                try {
                    return Class.forName("net.minecraft.world.item.ItemStack")
                            .getMethod("b", Class.forName("net.minecraft.nbt.NBTTagCompound"))
                            .invoke(Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack")
                                            .getMethod("asNMSCopy", ItemStack.class).invoke(null, item),
                                    Class.forName("net.minecraft.nbt.NBTTagCompound")
                                            .getConstructor().newInstance()).toString();
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }
}
