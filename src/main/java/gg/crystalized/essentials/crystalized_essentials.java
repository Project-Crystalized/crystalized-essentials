package gg.crystalized.essentials;

import gg.crystalized.essentials.CustomEntity.AntiairTotem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class crystalized_essentials extends JavaPlugin {

	//Might be messed up if someone leaves and rejoines (spoiler alert, it was)
	List<PlayerData> playerDatas = new ArrayList<>();
	ItemStack WingedOrbElytra = new ItemStack(Material.ELYTRA); //Had to put this here, ItemMeta just didn't work on CustomCoalBasedItems
	public List<AntiairTotem> antiairTotemList = new ArrayList<>();

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Crystalized Essentials Plugin Enabled!");
		ArrowData.particle_trails();
		this.getServer().getPluginManager().registerEvents(new CustomSwords(), this);
		this.getServer().getPluginManager().registerEvents(new CustomCoalBasedItems(), this);
		this.getServer().getPluginManager().registerEvents(new CrystalizedLinks(), this);
		this.getServer().getPluginManager().registerEvents(new CustomBows(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		playerDatas.clear();

		ItemMeta WingedOrbElytra_im = WingedOrbElytra.getItemMeta();
		WingedOrbElytra_im.setUnbreakable(true);
		WingedOrbElytra.setItemMeta(WingedOrbElytra_im);
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Crystalized Essentials Plugin Disabled!");
	}

	public static crystalized_essentials getInstance() {
		return getPlugin(crystalized_essentials.class);
	}

	public PlayerData getPlayerData(String p) {
		for (PlayerData pd : playerDatas) {
			if (pd.player.equals(p)) {
				return pd;
			}
		}

		Bukkit.getLogger().log(Level.SEVERE, "[Crystalized Essentials] getPlayerData failed with user \"" + p + "\".");
		return null;
	}

	public void addPlayerToList(Player p)  {
		PlayerData data = new PlayerData(p);
		playerDatas.add(data);
	}

	public void DisconnectPlayerToList(String p) {
		playerDatas.remove(getPlayerData(p));
	}

	public AntiairTotem getAntiAirTotemByEntity(ArmorStand e) {
		for (AntiairTotem a : antiairTotemList) {
			if (a.entity.equals(e)) {
				return a;
			}
		}

		return null;
	}
}
