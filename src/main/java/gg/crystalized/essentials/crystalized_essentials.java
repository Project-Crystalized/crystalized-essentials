package gg.crystalized.essentials;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class crystalized_essentials extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Crystalized Essentials Plugin Enabled!");
        this.getServer().getPluginManager().registerEvents(new CustomSwords(), this);
        this.getServer().getPluginManager().registerEvents(new CustomCoalBasedItems(), this);
        this.getServer().getPluginManager().registerEvents(new CrystalizedLinks(), this);
        this.getServer().getPluginManager().registerEvents(new CustomBows(), this);
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Crystalized Essentials Plugin Disabled!");
    }

    public static crystalized_essentials getInstance() {
        return getPlugin(crystalized_essentials.class);
    }
}
