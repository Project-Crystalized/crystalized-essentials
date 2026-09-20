package gg.crystalized.essentials;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Color.*;
import static org.bukkit.Particle.*;

import com.destroystokyo.paper.ParticleBuilder;

public class ArrowData {

	//I checked light strike and some bow types like multi shot are not here yet - Mish
		//So they will just take the default crosbow damage of 8
	enum bowType {
		marksman,
		ricochet,
		charged,
		normal,
		normalCrossbow,
		explosive,
		grapplingBow,
		preciseCrossbow
	}

	enum arrowType {
		//For now supportive arrows uses the wind arrow crossbow as place holder, will be replaced and called 5
		//The reason is because they both kinda blue and wind arrow is not in LS.
		supportive(4),
		wind(4),
		dragon(3),
		explosive(2),
		spectral(1),
		normal(0),
		;

		int cmd; //For crossbows, custom model data to show when the crossbow is loaded, giving it the textures of the arrow
		arrowType(int cmd) {
			this.cmd = cmd;
		}
	}

	public LivingEntity shooter;
	public EquipmentSlot hand;
	public bowType type;
	public arrowType arrType;
	public int timesBounced;
	//The damage value which will be tracked for consistant damage - Mish
	public double damage;
	//This PDC is set by the game plugin so Essentials can know which players are teammates
	public static final NamespacedKey TEAM_KEY = new NamespacedKey("crystalized", "team");

	public ArrowData(LivingEntity shooter, bowType type, arrowType arrType, int timesBounced, double damage) {
		this.shooter = shooter;
		this.type = type;
		this.timesBounced = timesBounced;
		this.arrType = arrType;
		//Added damage to the constructor - Mish
		this.damage = damage;
	}

	public static void particle_trails() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (AbstractArrow arrow : Bukkit.getWorld("world").getEntitiesByClass(AbstractArrow.class)) {
					ArrowData arrow_data = CustomBows.arrows.get(arrow);
					if (arrow_data == null) {
						return;
					}

					ParticleBuilder builder = null;
					ParticleBuilder builder2 = null;
					if (arrow_data.type != ArrowData.bowType.normal || arrow_data.type != ArrowData.bowType.charged) {
						if (arrow_data.type == ArrowData.bowType.marksman) {
							builder = new ParticleBuilder(DUST);
							builder.color(ORANGE);
							builder.count(5);
						} else if (arrow_data.type == ArrowData.bowType.ricochet) {
							builder = new ParticleBuilder(DUST);
							builder.color(LIME);
							builder.count(5);
						}
					}
					//Made supportive and and dragon arrows have team constinat particles
					if (arrow_data.arrType == ArrowData.arrowType.supportive) {
						spawnTeamArrowTrail(arrow, arrow_data, AQUA, Color.fromRGB(0, 70, 180));
					}
					else if (arrow_data.arrType == ArrowData.arrowType.dragon) {
						spawnTeamArrowTrail(arrow, arrow_data, PURPLE, Color.fromRGB(0, 120, 50));
					}
					else if (arrow_data.arrType != ArrowData.arrowType.normal) {
						builder2 = new ParticleBuilder(DUST);
						if (arrow_data.arrType == ArrowData.arrowType.explosive) {
							builder2.color(RED);
						} else if (arrow_data.arrType == arrowType.wind) {
							builder2.color(WHITE);
						}
						//should allways be yellow
						else if (arrow_data.arrType == arrowType.spectral) {
							builder2.color(YELLOW);
						}

					}

					if (builder != null) {
						builder.location(arrow.getLocation());
						builder.offset(0, 0, 0);
						builder.extra(0);
						builder.spawn();
					}
					if (builder2 != null) {
						builder2.location(arrow.getLocation());
						builder2.count(5);
						builder2.offset(0, 0, 0);
						builder2.spawn();
					}
				}
			}
		}.runTaskTimer(crystalized_essentials.getInstance(), 1, 1);
		// }.runTaskTimerAsynchronously(crystalized_essentials.getInstance(), 0, 1);
	}
	//Spwans the trail depdning on the team
	private static void spawnTeamArrowTrail(AbstractArrow arrow, ArrowData arrowData, Color friendlyColour, Color enemyColour) {
		//gets the shooters team
		String shooterTeam = getPlayerTeam(arrowData.shooter);

		if (shooterTeam == null) {
			return;
		}
		//goes through all the players and spawns particle
		for (Player viewer : arrow.getWorld().getPlayers()) {
			//gets the viewers team
			String viewerTeam = viewer.getPersistentDataContainer().get(TEAM_KEY, PersistentDataType.STRING);

			//gets the color of the arrow and spawns the particles
			Color colour = getTeamParticleColour(shooterTeam, viewerTeam, friendlyColour, enemyColour);
			viewer.spawnParticle(DUST, arrow.getLocation(), 5, 0.0, 0.0, 0.0, 0.0,
					new DustOptions(colour, 1.0F));
		}
	}
	//This is to determing what the color of particle must be
	public static Color getTeamParticleColour(String effectOwnerTeam, String viewerTeam, Color friendlyColour, Color enemyColour) {
		if (viewerTeam == null) {
			//Spectators will allways see breakers as friendly and everything else as enemy as in games with more than 2 teams it doen't matter
			//TODO: In the future if we have more games with only two teams like LS, this can be expanded
			if (effectOwnerTeam.equals("breaker")) {
				return friendlyColour;
			} else {
				return enemyColour;
			}
		}
		//This is what determines the color for the team, if the viewers team and the owner team matchers than it is friendly
		if (viewerTeam.equals(effectOwnerTeam)) {
			return friendlyColour;
		}//if doesn't match enemy
		else {
			return enemyColour;
		}
	}
	//This gets the players team
	public static String getPlayerTeam(LivingEntity entity) {
		if (!(entity instanceof Player)) {
			return null;
		}
		Player player = (Player) entity;
		return player.getPersistentDataContainer().get(TEAM_KEY, PersistentDataType.STRING);
	}

}
