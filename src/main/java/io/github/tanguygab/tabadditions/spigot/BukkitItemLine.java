package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.BukkitArmorStand;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.lang.reflect.Method;

public class BukkitItemLine extends BukkitArmorStand {

    public Property prop;
    public int entityId;
    public boolean glow;

    public BukkitItemLine(int entityId, TabPlayer owner, Property property, double yOffset, boolean staticOffset, boolean glow) {
        super(entityId, owner, property, yOffset, staticOffset, false);
        this.entityId = entityId;
        prop = property;
        this.glow = glow;
    }

    @Override
    public Object[] getSpawnPackets(TabPlayer viewer) {
        Material mat = Material.getMaterial(prop.getFormat(viewer));
        if (mat == null) mat = Material.AIR;
        ItemStack itemStack = new ItemStack(mat);
        Player p = (Player) viewer.getPlayer();
        if (glow)
            itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

        try {
            Method getloc = BukkitArmorStand.class.getDeclaredMethod("getLocation");
            getloc.setAccessible(true);
            Location loc = (Location) getloc.invoke(this);
            Class<?> itemClass = Class.forName("net.minecraft.world.entity.item.EntityItem");
            Object item = Class.forName("net.minecraft.world.entity.item.EntityItem")
                    .getConstructor(Class.forName("net.minecraft.world.level.World"),double.class,double.class,double.class,Class.forName("net.minecraft.world.item.ItemStack"))
                    .newInstance(Class.forName("org.bukkit.craftbukkit.v1_17_R1.CraftWorld").getMethod("getHandle").invoke(p.getWorld()), loc.getX(), loc.getY(), loc.getZ(), Class.forName("org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null,itemStack));
            //EntityItem item = new EntityItem(((CraftWorld) p.getWorld()).getHandle(), CraftItemStack.asNMSCopy(itemStack));

            itemClass.getMethod("e", int.class).invoke(item,getEntityId());
            //item.e(getEntityId());
            itemClass.getMethod("setMot", Class.forName("net.minecraft.world.phys.Vec3D")).invoke(item,Class.forName("net.minecraft.world.phys.Vec3D").getConstructor(double.class,double.class,double.class).newInstance(0,0,0));
            //item.setMot(new Vec3D(0, 0, 0));
            itemClass.getMethod("setNoGravity", boolean.class).invoke(item,true);
            //item.setNoGravity(true);

            Object spawnItem = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity")
                    .getConstructor(Class.forName("net.minecraft.world.entity.Entity"))
                    .newInstance(item);
            Object data = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata")
                    .getConstructor(int.class, Class.forName("net.minecraft.network.syncher.DataWatcher"), boolean.class)
                    .newInstance(itemClass.getMethod("getId").invoke(item),itemClass.getMethod("getDataWatcher").invoke(item),true);
            Object velocity = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity")
                    .getConstructor(Class.forName("net.minecraft.world.entity.Entity"))
                    .newInstance(item);

            /*
            PacketPlayOutSpawnEntity spawnItem = new PacketPlayOutSpawnEntity(item);
            PacketPlayOutEntityMetadata data = new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true);
            PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(item);
             */


            return new Object[]{spawnItem,data,velocity};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setOffset(double offset) {
        if (getOffset() == offset) return;
        super.setOffset(offset);
        teleport();
    }

    @Override
    public void spawn(TabPlayer viewer) {
        for (Object packet : getSpawnPackets(viewer)) {
            viewer.sendPacket(packet);
        }
    }

    @Override
    public void destroy(TabPlayer viewer) {
        getNearbyPlayers().remove(viewer);
        try {
            Method method = BukkitArmorStand.class.getDeclaredMethod("getDestroyPacket");
            method.setAccessible(true);
            viewer.sendPacket(method.invoke(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void teleport(TabPlayer viewer) {
        try {
            Method method = BukkitArmorStand.class.getDeclaredMethod("getTeleportPacket", TabPlayer.class);
            method.setAccessible(true);
            viewer.sendPacket(method.invoke(this,viewer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void teleport() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            teleport(p);
    }

    @Override
    public void destroy() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            destroy(p);
    }

    @Override
    public void sneak(boolean sneaking) {}

}
