package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.util.Vector;

public class InstantPickup implements Listener {
    private final double rangeSquared;

    public InstantPickup(double range) {
        this.rangeSquared = range * range;
    }

    @EventHandler(ignoreCancelled = true)
    public void instantPickup(BlockDropItemEvent ev) {
        Player p = ev.getPlayer();

        if (p.isSneaking())
            return;

        // why -0.5d? humans are set with coordinates on their feet.  -0.5d offsets that difference.
        if (ev.getBlock().getLocation().add(0.5d,-0.5d,0.5d).distanceSquared(p.getLocation()) > rangeSquared)
            return;

        for (Item i : ev.getItems()) {
            i.setPickupDelay(0);
            i.setVelocity(new Vector(0.0, 0.2, 0.0));
            i.teleport(p);
        }
    }
}
