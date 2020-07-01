package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import sh.chuu.mc.chuunilla.Chuunilla;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class OpenShulker implements Listener {
    private static final String PERMISSION_NODE = "chuunilla.openshulker";
    private final Chuunilla plugin = Chuunilla.getInstance();
    private HashMap<HumanEntity, InventoryShulkerData> shulkerOpened = new LinkedHashMap<>();

    @EventHandler
    public void placeShulkerInAir(PlayerInteractEvent ev) {
        Player p = ev.getPlayer();
        if (!p.hasPermission(PERMISSION_NODE) || p.isSneaking() || !ev.hasItem() || ev.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        int slot;
        if (ev.getHand() == EquipmentSlot.OFF_HAND) {
            if (isShulkerBox(p.getInventory().getItemInMainHand()))
                return;
            slot = 40;
        } else {
            slot = p.getInventory().getHeldItemSlot();
        }

        if (openShulkerBox(p, ev.getItem(), slot))
            ev.setCancelled(true);

    }

    @EventHandler(ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent ev) {
        HumanEntity p = ev.getView().getPlayer();
        if (!p.hasPermission(PERMISSION_NODE) || !(ev.getClickedInventory() instanceof PlayerInventory))
            return;

        InventoryShulkerData isd = shulkerOpened.get(p);
        if (isd != null && (isd.slot == ev.getSlot() || isd.slot == ev.getHotbarButton())) {

            ev.setCancelled(true);
            return;
        }

        if (!ev.isRightClick())
            return;
        if (openShulkerBox(p, ev.getCurrentItem(), ev.getSlot()))
            ev.setCancelled(true);
    }

    @EventHandler
    public void shulkerExit(InventoryCloseEvent ev) {
        InventoryShulkerData isd = shulkerOpened.remove(ev.getPlayer());
        if (isd == null) return;
        closeShulker(isd);
    }

    private boolean isShulkerBox(ItemStack item) {
        if (item == null) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof BlockStateMeta)) return false;
        return ((BlockStateMeta) itemMeta).getBlockState() instanceof ShulkerBox;
    }

    private boolean openShulkerBox(HumanEntity p, ItemStack item, int slot) {
        if (item == null) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof BlockStateMeta)) return false;
        BlockState blockState = ((BlockStateMeta) itemMeta).getBlockState();
        if (!(blockState instanceof ShulkerBox)) return false;
        ShulkerBox shulker = (ShulkerBox) blockState;

        Inventory tempinv;
        if (itemMeta.hasDisplayName())
            tempinv = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, itemMeta.getDisplayName());
        else
            tempinv = Bukkit.createInventory(null, InventoryType.SHULKER_BOX);

        tempinv.setContents(shulker.getInventory().getContents());

        p.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            shulkerOpened.put(p, new InventoryShulkerData(p, item, tempinv, slot));
            p.openInventory(tempinv);
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
        }, 1L);
        return true;
    }

    private void closeShulker(InventoryShulkerData isd) {
        BlockStateMeta blockState = (BlockStateMeta) isd.item.getItemMeta();
        ShulkerBox shulker = (ShulkerBox) blockState.getBlockState();
        shulker.getInventory().setContents(isd.tempInv.getContents());
        blockState.setBlockState(shulker);
        isd.item.setItemMeta(blockState);
        isd.player.closeInventory();
        isd.player.getWorld().playSound(isd.player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
    }

    public void onDisable() {
        shulkerOpened.values().forEach(this::closeShulker);
        shulkerOpened.clear();
    }

    private class InventoryShulkerData {
        private final HumanEntity player;
        private final ItemStack item;
        private final Inventory tempInv;
        private final int slot;

        private InventoryShulkerData(HumanEntity player, ItemStack item, Inventory tempInv, int slot) {
            this.player = player;
            this.item = item;
            this.tempInv = tempInv;
            this.slot = slot;
        }
    }
}
