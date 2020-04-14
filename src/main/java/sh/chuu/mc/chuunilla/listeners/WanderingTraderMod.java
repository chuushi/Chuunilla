package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import sh.chuu.mc.chuunilla.Chuunilla;

import java.util.*;

public class WanderingTraderMod implements Listener {
    private final Chuunilla plugin;
    private final MerchantRecipe totem;
    private final MerchantRecipe hots;
    private final MerchantRecipe elytra;
    private final MerchantRecipe beacon;

    public WanderingTraderMod() {
        this.plugin = Chuunilla.getInstance();
        this.totem = new MerchantRecipe(new ItemStack(Material.TOTEM_OF_UNDYING, 1), 1);
        this.totem.addIngredient(new ItemStack(Material.EMERALD, 57));
        this.hots = new MerchantRecipe(new ItemStack(Material.HEART_OF_THE_SEA, 1), 1);
        this.hots.addIngredient(new ItemStack(Material.EMERALD, 60));
        this.hots.addIngredient(new ItemStack(Material.DIAMOND, 1));
        this.elytra = new MerchantRecipe(new ItemStack(Material.ELYTRA, 1), 1);
        this.elytra.addIngredient(new ItemStack(Material.EMERALD, 55));
        this.elytra.addIngredient(new ItemStack(Material.PHANTOM_MEMBRANE, 2));
        this.beacon = new MerchantRecipe(new ItemStack(Material.BEACON, 1), 1);
        this.beacon.addIngredient(new ItemStack(Material.EMERALD, 52));
        this.beacon.addIngredient(new ItemStack(Material.GLASS, 1));
    }

    @EventHandler(ignoreCancelled = true)
    public void spawn(CreatureSpawnEvent ev) {
        if (ev.getEntityType() != EntityType.WANDERING_TRADER)
            return;

        WanderingTrader trader = (WanderingTrader) ev.getEntity();
        int rng = new Random().nextInt(7);
        List<MerchantRecipe> trades = new ArrayList<>(trader.getRecipes());

        if (rng == 0) trades.add(totem);
        else if (rng == 1) trades.add(hots);
        else if (rng == 2) trades.add(elytra);
        else if (rng == 3) trades.add(beacon);
        else {
            trades.add(head(randomPlayer(null)));
        }
        trades.add(head(randomPlayer(ev.getLocation())));

        trader.setRecipes(trades);
    }

    private MerchantRecipe head(OfflinePlayer owner) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        //noinspection ConstantConditions this is head and is never not null whatchu talking about
        meta.setOwningPlayer(owner);
        head.setItemMeta(meta);
        MerchantRecipe a = new MerchantRecipe(head, owner.isOnline() ? 2 : 1);
        a.addIngredient(new ItemStack(Material.EMERALD, owner.isOnline() ? 48 : 56));
        return a;
    }

    private OfflinePlayer randomPlayer(Location near) {
        if (near != null) {
            OfflinePlayer ret = null;
            double dist = Double.MAX_VALUE;
            for (Player p : Bukkit.getOnlinePlayers()) {
                double d = near.distanceSquared(p.getLocation());
                if (dist > d) {
                    ret = p;
                    dist = d;
                }
            }
            return ret;
        }

        List<String> heads = plugin.getConfig().getStringList("wandering-trader.heads");
        return Bukkit.getOfflinePlayer(UUID.fromString(heads.get(new Random().nextInt(heads.size()))));
    }
}
