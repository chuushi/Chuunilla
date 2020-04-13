package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class ThunderFire implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void disableThunderFire(BlockIgniteEvent ev) {
        if (ev.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING)
            ev.setCancelled(true);
    }
}
