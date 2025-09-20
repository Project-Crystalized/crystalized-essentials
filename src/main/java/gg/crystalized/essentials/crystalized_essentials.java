package gg.crystalized.essentials;

import gg.crystalized.essentials.CustomEntity.AntiairTotem;
import gg.crystalized.essentials.CustomEntity.DefenceTotem;
import gg.crystalized.essentials.CustomEntity.KnockoutOrb;
import gg.crystalized.essentials.CustomEntity.KnockoutOrbDeflectListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class crystalized_essentials extends JavaPlugin {

	//Might be messed up if someone leaves and rejoines (spoiler alert, it was)
	List<PlayerData> playerDatas = new ArrayList<>();
	ItemStack WingedOrbElytra = new ItemStack(Material.ELYTRA); //Had to put this here, ItemMeta just didn't work on CustomCoalBasedItems
	public List<AntiairTotem> antiairTotemList = new ArrayList<>();
	public List<DefenceTotem> defenceTotemList = new ArrayList<>();
	public List<KnockoutOrb> knockoutOrbList = new ArrayList<>();

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Crystalized Essentials Plugin Enabled!");
		ArrowData.particle_trails();
		this.getServer().getPluginManager().registerEvents(new CustomSwords(), this);
		this.getServer().getPluginManager().registerEvents(new CustomCoalBasedItems(), this);
		this.getServer().getPluginManager().registerEvents(new CrystalizedLinks(), this);
		this.getServer().getPluginManager().registerEvents(new CustomBows(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new KnockoutOrbDeflectListener(), this);
		playerDatas.clear();

		ItemMeta WingedOrbElytra_im = WingedOrbElytra.getItemMeta();
		WingedOrbElytra_im.setUnbreakable(true);
		WingedOrbElytra.setItemMeta(WingedOrbElytra_im);

        setupRecipes();
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Crystalized Essentials Plugin Disabled!");
	}

	public static crystalized_essentials getInstance() {
		return getPlugin(crystalized_essentials.class);
	}

	public int getRandomNumber(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
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
			if (e.equals(a.entity) || e.equals(a.turret) || e.equals(a.turretBase)) {
				return a;
			}
		}
		return null;
	}

	public DefenceTotem getDefenceTotemByEntity(ArmorStand e) {
		for (DefenceTotem a : defenceTotemList) {
			if (e.equals(a.entity)) {
				return a;
			}
		}
		return null;
	}

	public KnockoutOrb getKnockoutOrbByEntity(ArmorStand e) {
		for (KnockoutOrb a : knockoutOrbList) {
			if (e.equals(a.entity)) {
				return a;
			}
		}
		return null;
	}


	public void useWingedOrb(Player player) {
		PlayerData pd = crystalized_essentials.getInstance().getPlayerData(player.getName());
		if (pd.isUsingWingedOrb) {return;}

		new BukkitRunnable() {
			int timer = 2;
			public void run() {

				switch (timer) {
					case 2 -> {
						player.setVelocity(new Vector(
								0,
								1.9,
								0)
						);
					}
					case 1 -> {
						if (player.isOnGround()) {cancel(); return;}
						pd.isUsingWingedOrb = true;
						pd.lastChestPlateBeforeWingedOrb = player.getInventory().getChestplate();
						player.getInventory().setChestplate(crystalized_essentials.getInstance().WingedOrbElytra);
						player.setGliding(true);
					}
					case 0 -> {
						cancel();
					}
				}

				timer--;
			}
		}.runTaskTimer(this, 0, 13);
	}

	//TODO unfinished and untested code, do not touch.
	// This will return null if the display name doesn't get changed
	// This is also designed to work only in Knockoff and Crystal Blitz, where both plugins change the
	// display name to the same format of "[symbol] [playerName]". USE AT YOUR OWN RISK ANYWHERE ELSE!!
	public List<String> getAllies(Player p) {
		if (PlainTextComponentSerializer.plainText().serialize(p.displayName()).equals(p.getName())) {
			return null;
		}

		List<String> output = new ArrayList<>();
		List<Component> outputComponent = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			//Bukkit.getServer().sendMessage(text("start"));
			Component displayName = player.displayName();
			Iterator<Component> iterator = displayName.iterator(ComponentIteratorType.BREADTH_FIRST);
			List<Component> temp = new ArrayList<>();
			while (iterator.hasNext()) {
				Component iteratorC = iterator.next();
				//Bukkit.getServer().sendMessage(iteratorC);
				temp.add(iteratorC);
			}

			outputComponent.add(temp.getLast()); //make sure to not add any other weird shit to the name otherwise this will fuck up :)

			//Bukkit.getServer().sendMessage(text("end"));
		}

		//Do same shit as above but with player's name to compare
		Iterator<Component> iterator = p.displayName().iterator(ComponentIteratorType.BREADTH_FIRST);
		List<Component> temp = new ArrayList<>();
		while (iterator.hasNext()) {
			Component iteratorC = iterator.next();
			//Bukkit.getServer().sendMessage(iteratorC);
			temp.add(iteratorC);
		}
		Component playerDisplayName = temp.getLast();

		for (Component c : outputComponent) {
			if (c.color().equals(playerDisplayName.color())) {
				output.add(PlainTextComponentSerializer.plainText().serialize(c));
			}
		}

		//Bukkit.getServer().sendMessage(text(output.toString()));
		return output;
	}

	//Use at your own risk anywhere outside KO and CB!!
	public List<String> getHostiles(Player p) {
		List<String> temp = getAllies(p);
		List<String> output = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!temp.contains(player.getName())) {
				output.add(player.getName());
			}
		}
		return output;
	}


	//Custom Recipe stuff

    ShapedRecipe Recipe_PufferfishSword;
    ShapedRecipe Recipe_SlimeSword;
    ShapedRecipe Recipe_ChargedCrossbow;
	ShapedRecipe Recipe_MarksmanBow;
	ShapedRecipe Recipe_RicochetBow;
	ShapedRecipe Recipe_ExplosiveBow;
	ShapelessRecipe Recipe_DragonArrow;
	ShapelessRecipe Recipe_ExplosiveArrow;
    ItemStack PufferfishSword = new ItemStack(Material.STONE_SWORD);
    ItemStack SlimeSword = new ItemStack(Material.STONE_SWORD);
    ItemStack ChargedCrossbow = new ItemStack(Material.CROSSBOW);
	ItemStack MarksmanBow = new ItemStack(Material.BOW);
	ItemStack RicochetBow = new ItemStack(Material.BOW);
	ItemStack ExplosiveBow = new ItemStack(Material.BOW);
	ItemStack DragonArrow = new ItemStack(Material.ARROW);
	ItemStack ExplosiveArrow = new ItemStack(Material.ARROW);

    public void setupRecipes() {
        //Set up ItemStacks first
        setupItemStack(PufferfishSword, translatable("crystalized.sword.pufferfish.desc"), translatable("crystalized.sword.pufferfish.name"), new NamespacedKey("crystalized", "pufferfish_sword"));
        setupItemStack(SlimeSword, translatable("crystalized.sword.slime.desc1").append(text(" ")).append(translatable("crystalized.sword.slime.desc2")), translatable("crystalized.sword.slime.name"), new NamespacedKey("crystalized", "slime_sword"));
        setupItemStack(ChargedCrossbow, translatable("crystalized.crossbow.charged.desc"), translatable("crystalized.crossbow.charged.name"), new NamespacedKey("crystalized", "charged_crossbow"));
		setupItemStack(MarksmanBow, translatable("crystalized.bow.marksman.desc"), translatable("crystalized.bow.marksman.name"), new NamespacedKey("crystalized", "marksman_bow"));
		setupItemStack(RicochetBow, translatable("crystalized.bow.ricochet.desc"), translatable("crystalized.bow.ricochet.name"), new NamespacedKey("crystalized", "ricochet_bow"));
		setupItemStack(ExplosiveBow, translatable("crystalized.bow.explosive.desc1").append(text(" ")).append(translatable("crystalized.bow.explosive.desc2")), translatable("crystalized.bow.explosive.name"), new NamespacedKey("crystalized", "explosive_bow"));
		setupItemStack(DragonArrow, translatable("crystalized.item.dragonarrow.desc"), translatable("crystalized.item.dragonarrow.name"), new NamespacedKey("crystalized", "dragon_arrow"));
		setupItemStack(ExplosiveArrow, translatable("crystalized.item.explosivearrow.desc"), translatable("crystalized.item.explosivearrow.name"), new NamespacedKey("crystalized", "explosive_arrow"));

        //Then recipes
        Recipe_PufferfishSword = new ShapedRecipe(new NamespacedKey("crystalized", "pufferfish_sword"), PufferfishSword);
        Recipe_PufferfishSword.shape("P", "P", "S");
        Recipe_PufferfishSword.setIngredient('P', Material.PUFFERFISH);
        Recipe_PufferfishSword.setIngredient('S', Material.STICK);

        Recipe_SlimeSword = new ShapedRecipe(new NamespacedKey("crystalized", "slime_sword"), SlimeSword);
        Recipe_SlimeSword.shape("a", "a", "S");
        Recipe_SlimeSword.setIngredient('a', Material.SLIME_BALL);
        Recipe_SlimeSword.setIngredient('S', Material.STICK);

        Recipe_ChargedCrossbow = new ShapedRecipe(new NamespacedKey("crystalized", "charged_crossbow"), ChargedCrossbow);
        Recipe_ChargedCrossbow.shape("DRT", "RCS", "TS ");
        Recipe_ChargedCrossbow.setIngredient('D', Material.DISPENSER);
        Recipe_ChargedCrossbow.setIngredient('R', Material.REDSTONE);
        Recipe_ChargedCrossbow.setIngredient('T', Material.REDSTONE_TORCH);
        Recipe_ChargedCrossbow.setIngredient('C', Material.CROSSBOW);
        Recipe_ChargedCrossbow.setIngredient('S', Material.SOUL_TORCH);

		Recipe_MarksmanBow = new ShapedRecipe(new NamespacedKey("crystalized", "marksman_bow"), MarksmanBow);
		Recipe_MarksmanBow.shape(" IS", "I S", " IS");
		Recipe_MarksmanBow.setIngredient('I', Material.IRON_INGOT);
		Recipe_MarksmanBow.setIngredient('S', Material.STRING);

		Recipe_RicochetBow = new ShapedRecipe(new NamespacedKey("crystalized", "ricochet_bow"), RicochetBow);
		Recipe_RicochetBow.shape(" Sa", "S a", " Sa");
		Recipe_RicochetBow.setIngredient('S', Material.SLIME_BALL);
		Recipe_RicochetBow.setIngredient('a', Material.STRING);

		Recipe_ExplosiveBow = new ShapedRecipe(new NamespacedKey("crystalized", "explosive_bow"), ExplosiveBow);
		Recipe_ExplosiveBow.shape(" TS", "T S", " TS");
		Recipe_ExplosiveBow.setIngredient('T', Material.TNT);
		Recipe_ExplosiveBow.setIngredient('S', Material.STRING);

		Recipe_DragonArrow = new ShapelessRecipe(new NamespacedKey("crystalized", "dragon_arrow"), DragonArrow);
		Recipe_DragonArrow.addIngredient(Material.ARROW);
		Recipe_DragonArrow.addIngredient(Material.DRAGON_BREATH);

		Recipe_ExplosiveArrow = new ShapelessRecipe(new NamespacedKey("crystalized", "explosive_arrow"), ExplosiveArrow);
		Recipe_ExplosiveArrow.addIngredient(Material.ARROW);
		Recipe_ExplosiveArrow.addIngredient(Material.TNT);

        getServer().addRecipe(Recipe_PufferfishSword, true);
        getServer().addRecipe(Recipe_SlimeSword, true);
        getServer().addRecipe(Recipe_ChargedCrossbow, true);
		getServer().addRecipe(Recipe_MarksmanBow, true);
		getServer().addRecipe(Recipe_RicochetBow, true);
		getServer().addRecipe(Recipe_ExplosiveBow, true);
		getServer().addRecipe(Recipe_DragonArrow, true);
		getServer().addRecipe(Recipe_ExplosiveArrow, true);

    }

    public void setupItemStack(ItemStack source, Component description, Component name, NamespacedKey itemModel) {
        ItemMeta meta = source.getItemMeta();
        meta.displayName(name.color(WHITE).decoration(TextDecoration.ITALIC, false));
        meta.setItemModel(itemModel);
        List<Component> lore = new ArrayList<>();
        lore.add(description.color(WHITE).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        source.setItemMeta(meta);
    }
}
