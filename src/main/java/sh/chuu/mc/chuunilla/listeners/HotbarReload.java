package sh.chuu.mc.chuunilla.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import sh.chuu.mc.chuunilla.Chuunilla;

import java.util.Locale;
import java.util.Random;

public class HotbarReload implements Listener {
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void checkLastBlockOfItemStack(PlayerInteractEvent ev) {
        ItemStack item = ev.getItem();

        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK
                || ev.useItemInHand() == Event.Result.DENY
                || ev.getHand() != EquipmentSlot.HAND
                || item == null
                || !item.getType().isBlock()
                || item.getAmount() != 1
        ) return;

        Player p = ev.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();
        ItemStack sampleItem = item.clone();

        Bukkit.getScheduler().runTaskLater(Chuunilla.getInstance(), () -> {
            if (item.getAmount() == 0) {
                refill(p, sampleItem, slot);
            }
        }, 1L);
    }

    private void refill(Player p, ItemStack sampleItem, int slot) {
        PlayerInventory inv = p.getInventory();
        ItemStack[] invArray = inv.getContents();

        ItemMeta sampleMeta = sampleItem.getItemMeta();
        if (sampleMeta == null) return; // Every item should have a meta...

        int from = invArray.length - 1;
        for (; from >= 0; from--) {
            if (from != slot
                    && invArray[from] != null
                    && invArray[from].isSimilar(sampleItem)
            ) break;
        }

        if (from == -1)
            return; // Similar item not found

        BaseComponent reloadMsg = sampleMeta.hasDisplayName()
                ? new TextComponent("[" + sampleMeta.getDisplayName() + "]")
                : new TranslatableComponent("block.minecraft." + sampleItem.getType().name().toLowerCase(Locale.ROOT));
        reloadMsg.addExtra(" reloaded from inventory");

        ItemStack slotItem = inv.getItem(slot);
        inv.setItem(slot, inv.getItem(from));
        inv.setItem(from, slotItem);
        p.playSound(p, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, new Random().nextFloat(1.0f, 2.0f));
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, reloadMsg);
    }
}
