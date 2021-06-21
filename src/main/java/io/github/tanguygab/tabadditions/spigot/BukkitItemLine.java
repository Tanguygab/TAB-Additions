package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.BukkitArmorStand;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BukkitItemLine extends BukkitArmorStand {

    public Property prop;
    public int entityId;
    public boolean glow;

    public BukkitItemLine(int entityId, TabPlayer owner, Property property, double yOffset, boolean staticOffset, boolean glow) {
        super(entityId, owner, property, yOffset, staticOffset);
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
            EntityItem item = new EntityItem(((CraftWorld) p.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(itemStack));
            item.e(getEntityId());
            item.setMot(new Vec3D(0, 0, 0));
            item.setNoGravity(true);


            PacketPlayOutSpawnEntity spawnItem = new PacketPlayOutSpawnEntity(item);
            PacketPlayOutEntityMetadata data = new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true);
            PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(item);

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
