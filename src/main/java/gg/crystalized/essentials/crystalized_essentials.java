package gg.crystalized.essentials;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class crystalized_essentials extends JavaPlugin {

	//Might be messed up if someone leaves and rejoines (spoiler alert, it was)
	List<PlayerData> playerDatas = new ArrayList<>();
	ItemStack WingedOrbElytra = new ItemStack(Material.ELYTRA); //Had to put this here, ItemMeta just didn't work on CustomCoalBasedItems

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

		this.getServer().getMessenger().registerIncomingPluginChannel(this, "crystalized:essentials", new PluginMessaging());

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
}

class PluginMessaging implements PluginMessageListener {

	public PluginMessaging() {
		new BukkitRunnable() {
			public void run() {
				//putting this here, idk how plugin messages work but I think I should keep an instance of this class
			}
		}.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
	}


	//I have no idea how this works or if this does work
	@Override
	public void onPluginMessageReceived(@NonNull String channel, @NonNull Player player, byte @NonNull [] message) {
		if (!channel.equals("crystalized:essentials")) {return;}
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String message1 = in.readUTF();

		switch (message1) {
			case "BreezeDagger_DisableRecharging:true" -> {
				Bukkit.getServer().getLogger().log(Level.INFO, "[Crystalized Essentials] A plugin has requested we disable the Breeze Dagger's recharging");
				for (Player p : Bukkit.getOnlinePlayers()) {
					PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
					pd.BreezeDaggerDisableRecharge = true;
				}
			}
			case "BreezeDagger_DisableRecharging:false" -> {
				Bukkit.getServer().getLogger().log(Level.INFO, "[Crystalized Essentials] A plugin has requested we (re)enable the Breeze Dagger's recharging");
				for (Player p : Bukkit.getOnlinePlayers()) {
					PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
					pd.BreezeDaggerDisableRecharge = false;
				}
			}
			default -> {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "[Crystalized Essentials] A plugin has sent the message \"" + message1 + "\" but we have no idea what to do with it, this is likely a bug, please report it to crystalized admins.");
			}
		}
	}
}
