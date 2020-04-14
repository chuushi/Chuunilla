package sh.chuu.mc.chuunilla;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import sh.chuu.mc.chuunilla.listeners.*;

import java.util.List;

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

        if (getConfig().getBoolean("wandering-trader.rare-trade", false))
            getServer().getPluginManager().registerEvents(new WanderingTraderMod(), this);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("Modules: disable-thunder-fire: " +
                    getConfig().getBoolean("disable-thunder-fire", false) + ", crop-auto-plant:" +
                    getConfig().getBoolean("crop-auto-plant", false) + ", beacon-mob-grief-protection:" +
                    getConfig().getBoolean("beacon-mob-grief-protection", false) + "; wandering-trader.rare-trade:" +
                    getConfig().getBoolean("wandering-trader.rare-trade", false)
            );
            return true;
        }
        if (args[0].equalsIgnoreCase("wanderinghead")) {
            if (args.length == 1) {
                sender.sendMessage("missing: <list|add|remove>");
                return true;
            }
            List<String> ids = getConfig().getStringList("wandering-trader.heads");
            if (args[1].equalsIgnoreCase("list")) {
                // TODO
            }
            if (args[1].equalsIgnoreCase("remove")) {
                // TODO
            }
            if (args[1].equalsIgnoreCase("add")) {
                // TODO
            }
            sender.sendMessage("Head list config mod not yet implemented");
            return true;
        }
        return true;
    }
}
