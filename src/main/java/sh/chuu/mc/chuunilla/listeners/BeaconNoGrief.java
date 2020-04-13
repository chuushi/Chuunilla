package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import sh.chuu.mc.chuunilla.AreaSearch;

public class BeaconNoGrief implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void creeperBreak(EntityExplodeEvent ev) {
        if (ev.getEntityType() != EntityType.CREEPER)
            return;

        if (AreaSearch.isNearBeacon(ev.getLocation())) {
            ev.blockList().clear();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void endermanBreak(EntityChangeBlockEvent ev) {
        if (ev.getEntityType() != EntityType.ENDERMAN)
            return;

        if (AreaSearch.isNearBeacon(ev.getEntity().getLocation()))
            ev.setCancelled(true);
    }
}
