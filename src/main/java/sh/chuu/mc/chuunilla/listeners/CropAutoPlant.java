package sh.chuu.mc.chuunilla.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import sh.chuu.mc.chuunilla.Chuunilla;

public class CropAutoPlant implements Listener {
    private static final String PERMISSION_NODE = "chuunilla.autocrop";

    @EventHandler(ignoreCancelled = true)
    public void harvestEvent(BlockBreakEvent ev) {
        Player p = ev.getPlayer();

        // TODO Check if player has permission and disabled the feature
        if (!p.hasPermission(PERMISSION_NODE) || p.isSneaking())
            return;

        Block b = ev.getBlock();
        if (b.getBlockData() instanceof Ageable) {
            Ageable age = (Ageable) b.getBlockData();
            Material crop = b.getType();
            Material seed;

            switch (crop) {
                case WHEAT:
                    seed = Material.WHEAT_SEEDS;
                    break;
                case BEETROOTS:
                    seed = Material.BEETROOT_SEEDS;
                    break;
                case CARROTS:
                    seed = Material.CARROT;
                    break;
                case POTATOES:
                    seed = Material.POTATO;
                    break;
                case NETHER_WART:
                    seed = crop;
                    break;
                default:
                    return;
            }

            if (age.getAge() != age.getMaximumAge()) {
                return;
            }

            PlayerInventory inv = p.getInventory();

            // apparently offhand is not part of the inventory
            if (inv.first(seed) == -1 && inv.getItemInOffHand().getType() != seed) return;

            Bukkit.getScheduler().runTaskLater(Chuunilla.getInstance(), () -> {
                if (b.getType() != Material.AIR)
                    return;

                Block land = b.getRelative(BlockFace.DOWN);
                if (crop == Material.NETHER_WART) {
                    if (land.getType() != Material.SOUL_SAND)
                        return;
                } else if (land.getType() != Material.FARMLAND) {
                    return;
                }

                int slot;
                ItemStack item;
                slot = inv.getHeldItemSlot();
                if ((item = inv.getItem(slot)) == null || item.getType() != seed) {
                    slot = 40;
                    if ((item = inv.getItem(slot)) == null || item.getType() != seed) {
                        slot = inv.first(seed);
                        if (slot == -1) return;
                        else item = inv.getItem(slot);
                    }
                }

                BlockState oldState = b.getState();
                b.setType(crop);

                //noinspection ConstantConditions for "item" - if slot exists, item exists
                BlockPlaceEvent nev = new BlockPlaceEvent(b, oldState, land, item, p, true, EquipmentSlot.HAND);
                Bukkit.getPluginManager().callEvent(nev);
                if (nev.isCancelled()) {
                    b.setBlockData(oldState.getBlockData(), false);
                    return;
                }

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Crop auto-planted"));
                item.setAmount(item.getAmount() - 1);
                inv.setItem(slot, item);
                b.getWorld().playSound(b.getLocation(), Sound.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0f, 0.75f);
            }, 5);
        }
    }
}
