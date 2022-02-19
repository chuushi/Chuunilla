package sh.chuu.mc.chuunilla.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PetOwner implements Listener {
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPetRightClick(PlayerInteractEntityEvent ev) {
        if (ev.getRightClicked() instanceof Tameable tamed) {
            Player p = ev.getPlayer();
            AnimalTamer owner = tamed.getOwner();

            if (owner == null || owner == p)
                return;

            String customName = tamed.getCustomName();
            String name = customName == null ? tamed.getName() : "[" + customName + "]";

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(owner.getName() + "'s " + name));
        }
    }
}
