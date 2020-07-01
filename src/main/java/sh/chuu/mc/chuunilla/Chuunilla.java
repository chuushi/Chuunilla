package sh.chuu.mc.chuunilla;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import sh.chuu.mc.chuunilla.listeners.*;

import java.util.List;

public class Chuunilla extends JavaPlugin {
    private static Chuunilla instance = null;
    private OpenShulker openShulker = null;

    public static Chuunilla getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Chuunilla.instance = this;
        saveDefaultConfig();

        double radius = getConfig().getDouble("instant-item-pickup-radius", 0.0);
        if (radius > 0.0)
            getServer().getPluginManager().registerEvents(new InstantPickup(radius), this);

        if (getConfig().getBoolean("disable-thunder-fire", false))
            getServer().getPluginManager().registerEvents(new ThunderFire(), this);

        if (getConfig().getBoolean("crop-auto-plant", false))
            getServer().getPluginManager().registerEvents(new CropAutoPlant(), this);

        if (getConfig().getBoolean("beacon-mob-grief-protection", false))
            getServer().getPluginManager().registerEvents(new BeaconNoGrief(), this);

        if (getConfig().getBoolean("wandering-trader-rare-trade", false))
            getServer().getPluginManager().registerEvents(new WanderingTraderMod(), this);

        if (getConfig().getBoolean("timber-tree", false))
            getServer().getPluginManager().registerEvents(new Timber(), this);

        if (getConfig().getBoolean("open-shulker-as-item", false)) {
            this.openShulker = new OpenShulker();
            getServer().getPluginManager().registerEvents(this.openShulker, this);
        }
    }

    @Override
    public void onDisable() {
        if (this.openShulker != null) {
            this.openShulker.onDisable();
            this.openShulker = null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("Modules: disable-thunder-fire: " +
                getConfig().getBoolean("disable-thunder-fire", false) + ", crop-auto-plant:" +
                getConfig().getBoolean("crop-auto-plant", false) + ", beacon-mob-grief-protection:" +
                getConfig().getBoolean("beacon-mob-grief-protection", false) + "; wandering-trader-rare-trade:" +
                getConfig().getBoolean("wandering-trader-rare-trade", false) + "; timber-tree:" +
                getConfig().getBoolean("timber-tree", false) + "; open-shulker-as-item:" +
                getConfig().getBoolean("open-shulker-as-item", false)
        );
        return true;
    }
}
