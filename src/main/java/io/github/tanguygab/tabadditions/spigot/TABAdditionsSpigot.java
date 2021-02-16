package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.commands.*;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFP;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import io.github.tanguygab.tabadditions.spigot.Features.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;

public class TABAdditionsSpigot extends JavaPlugin implements CommandExecutor, TabCompleter {

    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new SpigotTA(this), this));
        reload();
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

    public void reload() {
        TABAdditions.getInstance().reload(getDataFolder());

        HandlerList.unregisterAll(this);
        Bukkit.getServer().getPluginManager().registerEvents(new BukkitEvents(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            new TABAdditionsExpansion(this).register();
        TABAdditions.getInstance().floodgate = Bukkit.getPluginManager().getPlugin("Floodgate") != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = "~Console~";
        TABAdditions instance = TABAdditions.getInstance();
        if (sender instanceof Player) name = sender.getName();

        if (args.length < 1 || args[0].equalsIgnoreCase("help"))
            new HelpCmd(name,getDescription().getVersion());
        else switch (args[0].toLowerCase()) {
            case "reload": {
                reload();
                instance.sendMessage(name, "&aConfig reloaded!");
                break;
            }
            case "actionbar": {
                if (!instance.getConfig("").getBoolean("features.actionbars"))
                    instance.sendMessage(name, "&cActionbar feature is not enabled, therefore this command cannot be used");
                else if (args.length < 2)
                    instance.sendMessage(name, "&cYou have to provide an actionbar!");
                else {
                    Map<String, String> section = instance.actionbarConfig.getConfigurationSection("bars.");
                    if (!section.containsKey(args[1]))
                        instance.sendMessage(name, "&cThis actionbar doesn't exist!");
                    else
                        new ActionBarCmd(name, args, section.get(args[1]));
                }
                break;
            }
            case "title": {
                if (!instance.getConfig("").getBoolean("features.titles"))
                    instance.sendMessage(name,"&cTitle feature is not enabled, therefore this command cannot be used");
                else if (args.length < 2)
                    instance.sendMessage(name,"&cYou have to provide a title!");
                else {
                    Map<String, String> titleSection = instance.titleConfig.getConfigurationSection("titles." + args[1]);
                    if (titleSection.isEmpty()) {
                        instance.sendMessage(name,"&cThis title doesn't exist!");
                    } else {
                        List<Object> titleProperties = new ArrayList<>();
                        for (Object property : titleSection.keySet())
                            titleProperties.add(titleSection.get(property));
                        new TitleCmd(name, args, titleProperties);
                    }
                }
                break;
            }
            case "fp": {
                if (instance.rfpEnabled) {
                    if (args.length < 2)
                        instance.sendMessage(name,"You have to provide add, remove, edit, info or list.");
                    else if (!args[1].equalsIgnoreCase("list") && args.length < 3)
                        instance.sendMessage(name,"You have to provide a fake player name.");
                    else if (args[1].equalsIgnoreCase("edit") && args.length < 4)
                        instance.sendMessage(name,"You have to provide an action.");
                    else new RealFakePlayerCmd(name, args);
                }
                break;
            }
            case "tags": {
                new TagsCmd(name, args);
                break;
            }
            case "test": {
                instance.sendMessage(name,"&7Nothing to see here :D");
                break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return new ArrayList<>(Arrays.asList("help","reload","actionbar","title","tags","fp"));
        if (args.length >= 2) {
            switch (args[0]) {
                case "actionbar":
                    if (args.length == 2)
                        return TABAdditions.getInstance().actionbars;
                case "tags": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("hide","show","toggle"));
                }
                case "fp": {
                    if (!TABAdditions.getInstance().rfpEnabled)
                        return null;
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("add","remove","edit","list","info"));
                    if (!args[1].equalsIgnoreCase("list") && args.length == 3) {
                        List<RFP> rfps = RFPManager.getInstance().getRFPS();
                        List<String> rfpnames = new ArrayList<>();
                        for (RFP rfp : rfps)
                            rfpnames.add(rfp.getConfigName());
                        if (args[1].equalsIgnoreCase("remove"))
                            rfpnames.add("_ALL_");
                        return rfpnames;
                    }
                    if (args[1].equalsIgnoreCase("edit") && args.length == 4)
                        return new ArrayList<>(Arrays.asList("name","skin","latency","group","prefix","suffix"));
                }
                case "title": {
                    if (args.length == 2)
                        return TABAdditions.getInstance().titles;
                }
            }
        }
        return null;
    }

    public String itemStack(ItemStack item) {
        try {
            Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
            Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, item);

            Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
            Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
            Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

            Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);

            return itemAsJsonObject.toString();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
