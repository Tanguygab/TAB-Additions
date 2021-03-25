package io.github.tanguygab.tabadditions.shared.features;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.packet.RawPacketListener;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UnlimitedItemLines implements RawPacketListener {

    private final TabFeature feature;

    private final List<Integer> list = new ArrayList<>();

    public UnlimitedItemLines(TabFeature feature) {
        feature.setDisplayName("&aUnlimited Item Lines");
        this.feature = feature;
    }

    @Override
    public Object onPacketReceive(TabPlayer tabPlayer, Object packet) {
        return packet;
    }

    @Override
    public void onPacketSend(TabPlayer receiver, Object packet) {
        NMSStorage nms = NMSStorage.getInstance();

        try {
            if (!nms.PacketPlayOutSpawnEntityLiving.isInstance(packet) || nms.PacketPlayOutSpawnEntityLiving_ENTITYTYPE.getInt(packet) != 1)
                return;

            int id = nms.PacketPlayOutSpawnEntityLiving_ENTITYID.getInt(packet);
            ArmorStand as = null;
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                for (ArmorStand armorStand : p.getArmorStandManager().getArmorStands()) {
                    if (id == armorStand.getEntityId()) {
                        as = armorStand;
                        break;
                    }
                }
            }
            if (as == null || !as.getProperty().get().startsWith("ITEM:")) return;
            String itemtype = as.getProperty().get().replace("ITEM:", "").toUpperCase().replace(" ", "_");
            Material mat = Material.getMaterial(itemtype);
            if (mat == null) return;
            as.destroy();

            List<Double> pos = new ArrayList<>();
            pos.add(nms.PacketPlayOutSpawnEntityLiving_X.getDouble(packet));
            pos.add(nms.PacketPlayOutSpawnEntityLiving_Y.getDouble(packet));
            pos.add(nms.PacketPlayOutSpawnEntityLiving_Z.getDouble(packet));

            id = id + 1000;
            /*if (list.contains(id)) {
                Player p = (Player) receiver.getPlayer();
                EntityItem item = (EntityItem) ((CraftWorld)p.getWorld()).getHandle().getEntity(id);
                if (item == null) return;

                item.setPosition(pos.get(0), pos.get(1), pos.get(2));
                PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(item);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(tp);
            }
            else {*/
                list.add(id);


                ItemStack itemStack = new ItemStack(mat, 1);
                Player p = (Player) receiver.getPlayer();

                EntityItem item = new EntityItem(((CraftWorld) p.getWorld()).getHandle(), pos.get(0), pos.get(1), pos.get(2), CraftItemStack.asNMSCopy(itemStack));
                item.e(id);
                item.setMot(new Vec3D(0, 0, 0));
                item.velocityChanged = true;


                PacketPlayOutSpawnEntity spawnItem = new PacketPlayOutSpawnEntity(item);

                DataWatcher dataWatcher = item.getDataWatcher();
                Field gravity = Entity.class.getDeclaredField("at");
                gravity.setAccessible(true);
                dataWatcher.set((DataWatcherObject<Boolean>) gravity.get(null), true);
                PacketPlayOutEntityMetadata data = new PacketPlayOutEntityMetadata(item.getId(), dataWatcher, true);

                PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(item);


                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(spawnItem);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(data);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(velocity);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
