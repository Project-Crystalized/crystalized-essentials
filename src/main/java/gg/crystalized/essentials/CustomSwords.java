package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomSwords implements Listener {

	///The first attempt didn't do it much better as each hit canceled the extra damage. So this is V2
	//This for a new pufferifish damage effect, as it was a popular demand to change how it works.
	//The point is to by pass regular poision as no one likes it, and apply custom damage over time like bleeding
	//Or poisioned blood, setting health directly, to avoid any damage iframes knockback etc.

	///Important: If you want to play around with values they are here no need to modify the methods - Mish

	//This is the amount of how many times the player will be repetedly damage with a puffer sword.
	private static final int PUFFER_DAMAGES_REPETION_NUMBER = 5;
	//The delay after the hit that the puffer sword effect starts
	private static final long DELAY_BEFORE_STARTING_PUFFER_DAMAGE = 5L;
	//This is a delay before the puffer health reducticion happens again, trying to match poision roughly
	private static final long DELAY_BEFORE_REPETING_PUFFER_DAMAGE = 15L;
	//1hp/half a heart per one cycle of puffer blood poision effect
	private static final double DAMAGE_BY_PUFFER_BLEEDING_EFFECT = 1.0;
	//This is extra damage for both puffer and slime by default, now they deal 5.75 on initial hit close to iron
	private static final double PUFFER_AND_SLIME_EXTRA_DAMAGE = 0.75;
	//This stores the player's UUID and the task that will be damaging them
	private final Map<UUID, BukkitTask> currentBleedingPuffer = new HashMap<>();
	//Stores how many bleeds/puffers are left per each entity. If you have 3 left it will store it, and refresh it to 5 on new hit
	private final Map<UUID, Integer> remainingPufferBleedingDamages = new HashMap<>();

	//This is the method which applies the new poision blood/bleeding effect to the player/or a living entity
	private void applyNewPufferSwordBleeding(LivingEntity livingEntity){
		//Takes in the UUID
		UUID liviningEntityId = livingEntity.getUniqueId();
		//OLD taks from V1
			//BukkitTask previousTask = currentBleedingPuffer.remove(liviningEntityId);
			/*
			if(previousTask != null){
				previousTask.cancel();
			}*/
		//So here it checks it it is already contained.
		if (currentBleedingPuffer.containsKey(liviningEntityId)) {
			//refreshes the number back to "5" repetions, after a new hit
			remainingPufferBleedingDamages.put(liviningEntityId, PUFFER_DAMAGES_REPETION_NUMBER);
			//returns to not create a new task
			return;
		}
		//If it is not contained then it puts it in, with the default repetion number.
		remainingPufferBleedingDamages.put(liviningEntityId, PUFFER_DAMAGES_REPETION_NUMBER);

		//Creates the task that will be reaped
		BukkitTask pufferBleedTask = Bukkit.getScheduler().runTaskTimer(crystalized_essentials.getInstance(),
				new Runnable() {
					//From the previous implemetation V1
						//private int remainingBleedTicks = THE_LENGHT_OF_PUFFER_BLEEDING_IN_TICKS;

					@Override
					public void run() {
						//The logic that will be repeated is here.
						//Ensures that the player or livinint entity is alive, and player not offline
						if (livingEntity.isDead() || !livingEntity.isValid() || (livingEntity instanceof Player player && !player.isOnline())) {
							//If they are dead, offline etc the task stops
							stopPufferBleeding(liviningEntityId);
							return;
						}
						//It reads the current number of reapetion from the map, if nothing will be zero
						int remainingPufferDamages = remainingPufferBleedingDamages.getOrDefault(liviningEntityId, 0);
						//If it is less or equal to zero then stops the task ofcourse
						if (remainingPufferDamages <= 0) {
							stopPufferBleeding(liviningEntityId);
							return;
						}
						//Here is where the calculation happens, this by pases any damage events, direct health calculation.
						//So should not give any I-frames or knockback etc, so what people have complained about this solves
						//Should stop at 1hp, but in the method there is an extra safety check
						if (livingEntity.getHealth() > 1.0) {
							//TODO: If too op after play tests the armor and resistanse effect can be checked here
							//sets the damage from the value, so it can easily be tweaked without having to go here
							double pufferDemageEffect = DAMAGE_BY_PUFFER_BLEEDING_EFFECT;
							//Sets the health exactly, math.max ensures that it will not kill the player
							double newHealth = Math.max(1.0, livingEntity.getHealth() - pufferDemageEffect);
							//sets the health of the entity directly without the damage effects taken place, this is basicly custom damage - Mish
							livingEntity.setHealth(newHealth);
						}

						//Another check to ensure that the entity is dead and valid otherwise sound might happen
						if(!livingEntity.isDead() && livingEntity.isValid()){

							//Plays the sound like the poision.
							livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.35F, 1.35F);
							//Plays the hurth animation, so the player will still turn red
							livingEntity.playHurtAnimation(0.0F);
							//Spawns lime particles to simulate poision effect.
							livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0, 1.0, 0),
									8,
									0.3,
									0.5,
									0.3,
									0.0,
									new Particle.DustOptions(Color.LIME, 1.0F)
							);
							//As there is no way to make the hearts green, sends the action bar while blood is poisioned
							if (livingEntity instanceof Player player) {
								player.sendActionBar(Component.text("Poisoned blood ☠ ", NamedTextColor.GREEN));
							}
						}
						//Takes away one puffer damage.
						remainingPufferBleedingDamages.put(liviningEntityId, remainingPufferDamages - 1);
					}
					//The delays after first hit, and when it reapets
				}, DELAY_BEFORE_STARTING_PUFFER_DAMAGE, DELAY_BEFORE_REPETING_PUFFER_DAMAGE
		);

		//This stores the task, so it can be found later to be canceled, assotiated bu player/living entity ID
		currentBleedingPuffer.put(liviningEntityId, pufferBleedTask);
	}
	//This method stops the bleeding task, says player id but could be an enttity for future mini games
	private void stopPufferBleeding(UUID playerId) {
		//removes it and sets the task
		BukkitTask task = currentBleedingPuffer.remove(playerId);
		//if it is not null then the task is caneled
		if (task != null) {
			task.cancel();
		}
		//Removes from the counter map as well.
		remainingPufferBleedingDamages.remove(playerId);
		//This is to makes sure that the action bar is empty as soon as the poision is caneled
		Player player = Bukkit.getPlayer(playerId);

		if (player != null && player.isOnline()) {
			player.sendActionBar(Component.empty());
		}
	}
	//This is not an actual event it just takes in an event to simplify coding.
	//Doesn't have event handler
	//It is used to apply damage buff for slime sword and puffer sword
	public void extraDamageForSlimeAndPuffer(EntityDamageByEntityEvent e){
		//If not critical just adds the damage
		if (!e.isCritical()) {
			e.setDamage(e.getDamage() + PUFFER_AND_SLIME_EXTRA_DAMAGE);
		}
		else {
			//if critcal, multiples the bonus by critical as well creating the same number
			//Ex 5 * 1.5 = 7.5  + (0.75 * 1.5) = 8.625 otherwise matching the 5.75 * 1.5 = 8.625
			e.setDamage(e.getDamage() + (PUFFER_AND_SLIME_EXTRA_DAMAGE * 1.5));
		}
	}
	//Makes sure that the tasks clears on death. It is safe
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		stopPufferBleeding(event.getEntity().getUniqueId());
	}
	//Makes sure that the tasks clears on quting. It is safe
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		stopPufferBleeding(event.getPlayer().getUniqueId());
	}
	//When you eat the puffer bleeding stop, anything is meant to cure it, any pottion any apple etc.
	@EventHandler
	public void onConsumbelStopPufferDamage(PlayerItemConsumeEvent event){
		stopPufferBleeding(event.getPlayer().getUniqueId());
	}


	//FIXED: found a bug where when you are attacking in pre round the effects apply
	//For slime and puffer sword.
	//Made sure that it happens last so LS can propely cancel it, and not let the effects apply
	//TODO: Check in crystal blitz.
	@EventHandler(priority = EventPriority.HIGH)
	public void onLeftClick(EntityDamageByEntityEvent e) {
		//Checks specificly seperately that the event is canceled and returns if it was.
		if(e.isCancelled()){
			return;
		}
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
			return;
		}
		ItemStack held_item = ((Player) e.getDamager()).getInventory().getItemInMainHand();
		if (held_item.getType().toString().toLowerCase().contains("sword") && held_item.getItemMeta().hasItemModel()) {

			NamespacedKey item_model = held_item.getItemMeta().getItemModel();

			if (item_model.equals(new NamespacedKey("crystalized", "slime_sword"))) {
				//The damage buff from 5 to 5.75 to be closer to iron sword\
				//With accurate crits
				extraDamageForSlimeAndPuffer(e);
				//The same slowness as before
				((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 4 * 20, 0));
				//Buff to the slime sword, gives the attacking player speed and jump boost for 2 seconds, each hit
				//To remove just comment it out
				((Player) e.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2 * 20, 0));
				((Player) e.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 2 * 20, 0));
				crystalized_essentials.getInstance()
						.getLogger().info("Slime Initial Damage: " + e.getDamage() + "Slime Final Damage " + e.getFinalDamage());
			} else if (item_model.equals(new NamespacedKey("crystalized", "pufferfish_sword"))) {
				//The damage buff from 5 to 5.75 to be closer to iron sword
				extraDamageForSlimeAndPuffer(e);
				//Aplies the new puffer fish damage, to remove just comment out
				applyNewPufferSwordBleeding((LivingEntity) e.getEntity());
				crystalized_essentials.getInstance()
						.getLogger().info("Puffer Initial Damage: " + e.getDamage() + " :Puffer Final Damage " + e.getFinalDamage());
				//Old poision logic, if you want it back un comment it and comment applyNewPufferSwordBleeding.
				//((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 3 * 20, 0));
			}

			///Major underdog bug discovered, so minecraft adds extra damage on top of the crit.
			//As it calculates (og_damage * crit) + extra_damage
			//Hence the bonuce needs to be multipled by crit value *1.5
			//Changed it so it calulates the bonuce first, and then applies to the sword
			if (item_model.equals(new NamespacedKey("crystalized", "underdog_sword"))) {
				int item_custom_model= held_item.getItemMeta().getCustomModelData();
				//The bonuce variable
				double damageBonuce = 0;
				//changes depened on the model as before
				if (item_custom_model == 1) {
					Bukkit.getLogger().severe("1");
					damageBonuce = 0.5;
				} else if (item_custom_model == 2) {
					Bukkit.getLogger().severe("2");
					damageBonuce = 1;
				} else if (item_custom_model == 3) {
					Bukkit.getLogger().severe("3");
					damageBonuce = 1.5;
				} else if (item_custom_model == 4) {
					Bukkit.getLogger().severe("4");
					damageBonuce = 2.0;
				}
				//If critcal multiples the bonuce, so it will be correct
				//Ex for underdog sword (+1 damage) (5*1.5) + (1*1.5) = 9
				//Ex for the real iron sword (6*1.5) = 9, so they match
				//Old code example (5*1.5) + 1 = 8.5 they don't match
				if (e.isCritical()) {
					//When critical multiples the bonunce by critical
					damageBonuce = damageBonuce * 1.5;
				}
				//Applies the damage
				e.setDamage(e.getDamage() + damageBonuce);
				//Logs the damage so feel free to test on practise
				crystalized_essentials.getInstance().getLogger().info("Underdog did this raw damage: + " + e.getDamage() +
						" Final Damage:" + e.getFinalDamage());

			}


			//Original under dog implementation.
			/*
			if (item_model.equals(new NamespacedKey("crystalized", "underdog_sword"))) {
				int item_custom_model= held_item.getItemMeta().getCustomModelData();
				if (item_custom_model == 1) {
					Bukkit.getLogger().severe("1");
					e.setDamage(e.getDamage() + 0.5);
				} else if (item_custom_model == 2) {
					Bukkit.getLogger().severe("2");
					e.setDamage(e.getDamage() + 1);
				} else if (item_custom_model == 3) {
					Bukkit.getLogger().severe("3");
					e.setDamage(e.getDamage() + 1.5);
				} else if (item_custom_model == 4) {
					Bukkit.getLogger().severe("4");
					e.setDamage(e.getDamage() + 2);
				}
			}

			 */
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
		ItemStack held_item = p.getInventory().getItemInMainHand();
		if (!e.getAction().isRightClick()) {return;}
		if (!held_item.hasItemMeta()) {return;} //should return if nothing in hand
		if (held_item.getItemMeta().hasItemModel()) {
			NamespacedKey item_model = held_item.getItemMeta().getItemModel();
			if (item_model.equals(new NamespacedKey("crystalized", "breeze_dagger"))) {
				if (pd.BreezeDaggerDashes != 0) {
					double y = 0;
					if (!p.isOnGround()) { //Fixes a bug where dashing while on ground lifts you up in the air slightly
						y = 0.60;
					} else {
						y = p.getVelocity().getY();
					}
					p.setVelocity(new Vector(
							p.getLocation().getDirection().getX() * 1.05, //0.60
							y,
							p.getLocation().getDirection().getZ() * 1.05)
					);
					//p.setVelocity(p.getLocation().getDirection().multiply(1.05));
					p.playSound(p, "minecraft:item.armor.equip_elytra", 50, 1); //TODO placeholder sound. Breeze Dagger use
					pd.UseBreezeDaggerDash();

				}
			}
		}
	}
}
