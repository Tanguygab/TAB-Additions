package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.*;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;
import me.neznamy.tab.shared.TAB;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class TABAdditionsSpigot extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {

    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new SpigotPlatform(this), this,getDataFolder()));
        TABAdditions.getInstance().load();
    }

    @EventHandler
    public void onTABLoad(BukkitTABLoadEvent e) {
        TABAdditions.getInstance().getPlatform().reload();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        TAB tab = TAB.getInstance();
        if (!tab.getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString())) return;
        e.setCancelled(((ChatManager)tab.getFeatureManager().getFeature(TAFeature.CHAT.toString())).onChat(tab.getPlayer(e.getPlayer().getUniqueId()),e.getMessage()));
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = "~Console~";
        if (sender instanceof Player) name = sender.getName();
        Cmd.getMain(name,args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission("tabadditions.admin"))
            return Cmd.getTabComplete(args);
        return null;
    }

    public String itemStack(ItemStack item) {
        try {
            String pack = getServer().getClass().getPackage().getName().split("\\.")[3];
            return Class.forName("net.minecraft.server." + pack + ".ItemStack")
                    .getMethod("save", Class.forName("net.minecraft.server." + pack + ".NBTTagCompound"))
                    .invoke(Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack")
                                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item),
                            Class.forName("net.minecraft.server." + pack + ".NBTTagCompound")
                                    .getConstructor().newInstance()).toString();
        } catch (Exception e) {
            return null;
        }
    }
}
