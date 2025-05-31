package gg.crystalized.essentials;

import gg.crystalized.essentials.CustomEntity.AntiairTotem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
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

        setupRecipes();
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
			if (e.equals(a.entity) || e.equals(a.turret) || e.equals(a.turretBase)) {
				return a;
			}
		}

		return null;
	}

    ShapedRecipe Recipe_PufferfishSword;
    ShapedRecipe Recipe_SlimeSword;
    ShapedRecipe Recipe_ChargedCrossbow;
    ItemStack PufferfishSword = new ItemStack(Material.STONE_SWORD);
    ItemStack SlimeSword = new ItemStack(Material.STONE_SWORD);
    ItemStack ChargedCrossbow = new ItemStack(Material.CROSSBOW);

    public void setupRecipes() {
        //Set up ItemStacks first
        setupItemStack(PufferfishSword, translatable("crystalized.sword.pufferfish.desc"), translatable("crystalized.sword.pufferfish.name"), new NamespacedKey("crystalized", "pufferfish_sword"));
        setupItemStack(SlimeSword, translatable("crystalized.sword.slime.desc1").append(text(" ")).append(translatable("crystalized.sword.slime.desc2")), translatable("crystalized.sword.slime.name"), new NamespacedKey("crystalized", "slime_sword"));
        setupItemStack(ChargedCrossbow, translatable("crystalized.crossbow.charged.desc"), translatable("crystalized.crossbow.charged.name"), new NamespacedKey("crystalized", "charged_crossbow"));

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
        Recipe_ChargedCrossbow.shape(
                "DRT",
                "RCS",
                "TS ");
        Recipe_ChargedCrossbow.setIngredient('D', Material.DISPENSER);
        Recipe_ChargedCrossbow.setIngredient('R', Material.REDSTONE);
        Recipe_ChargedCrossbow.setIngredient('T', Material.REDSTONE_TORCH);
        Recipe_ChargedCrossbow.setIngredient('C', Material.CROSSBOW);
        Recipe_ChargedCrossbow.setIngredient('S', Material.SOUL_TORCH);

        getServer().addRecipe(Recipe_PufferfishSword, true);
        getServer().addRecipe(Recipe_SlimeSword, true);
        getServer().addRecipe(Recipe_ChargedCrossbow, true);

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
