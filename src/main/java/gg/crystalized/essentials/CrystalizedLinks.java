package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import org.bukkit.ServerLinks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;

public class CrystalizedLinks implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ServerLinks Crystalized = crystalized_essentials.getInstance().getServer().getServerLinks().copy();

        //TODO fix these spamming the server links menu each time you rejoin
        Crystalized.addLink(Component.text("Discord"), URI.create("https://discord.gg/4H8ADwFZyk"));
        Crystalized.addLink(Component.text("YouTube"), URI.create("https://exmaple.com")); //Placeholders for when LadyCat gives me (Callum) these links
        Crystalized.addLink(Component.text("Bluesky"), URI.create("https://bsky.app/profile/projectcrystalized.bsky.social"));
        Crystalized.addLink(Component.text("GitHub"), URI.create("https://github.com/Project-Crystalized"));

        player.sendLinks(Crystalized);
    }
}
