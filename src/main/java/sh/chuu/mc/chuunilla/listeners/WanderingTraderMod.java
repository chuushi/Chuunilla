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
    private final MerchantRecipe bush;
    private final MerchantRecipe totem;
    private final MerchantRecipe hots;
    private final MerchantRecipe elytra;
    private final MerchantRecipe beacon;
    private final MerchantRecipe trident;

    public WanderingTraderMod() {
        this.plugin = Chuunilla.getInstance();
        this.bush = new MerchantRecipe(new ItemStack(Material.DEAD_BUSH, 10), 2);
        this.bush.addIngredient(new ItemStack(Material.EMERALD, 3));
        this.totem = new MerchantRecipe(new ItemStack(Material.TOTEM_OF_UNDYING, 1), 1);
        this.totem.addIngredient(new ItemStack(Material.EMERALD, 57));
        this.hots = new MerchantRecipe(new ItemStack(Material.HEART_OF_THE_SEA, 1), 1);
        this.hots.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 12));
        this.hots.addIngredient(new ItemStack(Material.NAUTILUS_SHELL, 5));
        this.elytra = new MerchantRecipe(new ItemStack(Material.ELYTRA, 1), 1);
        this.elytra.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 12));
        this.elytra.addIngredient(new ItemStack(Material.PHANTOM_MEMBRANE, 42));
        this.beacon = new MerchantRecipe(new ItemStack(Material.BEACON, 1), 1);
        this.beacon.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 12));
        this.beacon.addIngredient(new ItemStack(Material.OBSIDIAN, 56));
        this.trident = new MerchantRecipe(new ItemStack(Material.TRIDENT, 1), 1);
        this.trident.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 12));
        this.trident.addIngredient(new ItemStack(Material.DIAMOND, 32));
    }

    @EventHandler(ignoreCancelled = true)
    public void spawn(CreatureSpawnEvent ev) {
        if (ev.getEntityType() != EntityType.WANDERING_TRADER)
            return;

        WanderingTrader trader = (WanderingTrader) ev.getEntity();
        int rng = new Random().nextInt(8);
        List<MerchantRecipe> trades = new ArrayList<>(trader.getRecipes());

        if (rng == 0) trades.add(trident);
        else if (rng == 1) trades.add(hots);
        else if (rng == 2) trades.add(elytra);
        else if (rng == 3) trades.add(beacon);
        else if (rng <= 5) trades.add(totem);
        else trades.add(bush);

        trader.setRecipes(trades);
    }
}
