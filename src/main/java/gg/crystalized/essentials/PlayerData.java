package gg.crystalized.essentials;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerData {

    public Player player;
    public boolean isUsingWingedOrb = false;
    public ItemStack lastChestPlateBeforeWingedOrb = null; //This is to give the player after the Winged Orb ends

    public PlayerData(Player p) {
        player = p;
    }
}
