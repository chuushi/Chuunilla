package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import sh.chuu.mc.chuunilla.Chuunilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WanderingTraderMod implements Listener {
    private final MerchantRecipe hots;
    private final MerchantRecipe elytra;
    private final MerchantRecipe beacon;

    public WanderingTraderMod() {
        this.hots = new MerchantRecipe(new ItemStack(Material.HEART_OF_THE_SEA, 1), 1);
        this.hots.addIngredient(new ItemStack(Material.EMERALD, 60));
        this.elytra = new MerchantRecipe(new ItemStack(Material.ELYTRA, 1), 1);
        this.elytra.addIngredient(new ItemStack(Material.EMERALD, 55));
        this.beacon = new MerchantRecipe(new ItemStack(Material.BEACON, 1), 1);
        this.beacon.addIngredient(new ItemStack(Material.EMERALD, 52));
    }

    @EventHandler(ignoreCancelled = true)
    public void spawn(CreatureSpawnEvent ev) {
        if (ev.getEntityType() != EntityType.WANDERING_TRADER)
            return;

        WanderingTrader trader = (WanderingTrader) ev.getEntity();
        int rng = new Random().nextInt(3);
        List<MerchantRecipe> trades = new ArrayList<>(trader.getRecipes());

        if (rng == 0) trades.add(hots);
        else if (rng == 1) trades.add(elytra);
        else trades.add(beacon);

        trader.setRecipes(trades);
    }
}
