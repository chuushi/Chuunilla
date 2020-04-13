package sh.chuu.mc.chuunilla;

import org.bukkit.plugin.java.JavaPlugin;
import sh.chuu.mc.chuunilla.listeners.*;

public class Chuunilla extends JavaPlugin {
    private static Chuunilla instance = null;

    public static Chuunilla getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Chuunilla.instance = this;
        saveDefaultConfig();

        double radius = getConfig().getDouble("instant-item-pickup-radius", 0);
        if (radius > 0)
            getServer().getPluginManager().registerEvents(new InstantPickup(radius), this);

        if (getConfig().getBoolean("disable-thunder-fire", false))
            getServer().getPluginManager().registerEvents(new ThunderFire(), this);

        if (getConfig().getBoolean("crop-auto-plant", false))
            getServer().getPluginManager().registerEvents(new CropAutoPlant(), this);

        if (getConfig().getBoolean("beacon-mob-grief-protection", false))
            getServer().getPluginManager().registerEvents(new BeaconNoGrief(), this);

        if (getConfig().getBoolean("wandering-trader-rare-trade", false))
            getServer().getPluginManager().registerEvents(new WanderingTraderMod(), this);
    }

    @Override
    public void onDisable() {

    }
}
