package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import sh.chuu.mc.chuunilla.Chuunilla;

public class CropAutoPlant implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void harvestEvent(BlockBreakEvent ev) {
        Player p = ev.getPlayer();

        // TODO Check if player has permission and disabled the feature
        if (p.isSneaking())
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

                Material landType = b.getRelative(BlockFace.DOWN).getType();
                if (crop == Material.NETHER_WART) {
                    if (landType != Material.SOUL_SAND)
                        return;
                } else if (landType != Material.FARMLAND) {
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

                //noinspection ConstantConditions if slot exists, item exists
                item.setAmount(item.getAmount() - 1);
                inv.setItem(slot, item);
                b.setType(crop);
            }, 5);
        }
    }
}
