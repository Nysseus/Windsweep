package me.nysseus;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class Windsweep extends AirAbility implements AddonAbility {

    @Attribute(Attribute.COOLDOWN)
    private long cooldownJump;

    @Attribute(Attribute.SPEED)
    private double velocity;

    @Attribute(Attribute.SPEED)
    private double velocityBack;

    @Attribute(Attribute.SPEED)
    private double velocityBackHeight;

    private int speedAmplifier;

    private boolean isJump;
    private boolean isSprinting;
    private boolean isBackwards;

    private Listener listener;

    public Windsweep(Player player, boolean isJump, boolean isBackwards) {
        super(player);

        cooldownJump = ConfigManager.getConfig().getLong("ExtraAbilities.Nysseus.Windsweep.Cooldown");
        velocity = ConfigManager.getConfig().getDouble("ExtraAbilities.Nysseus.Windsweep.Velocity");
        speedAmplifier = ConfigManager.getConfig().getInt("ExtraAbilities.Nysseus.Windsweep.Speed");
        velocityBack = ConfigManager.getConfig().getDouble("ExtraAbilities.Nysseus.Windsweep.VelocityBackwards");
        velocityBackHeight = ConfigManager.getConfig().getDouble("ExtraAbilities.Nysseus.Windsweep.BackwardsJumpHeight");

        this.isJump = isJump;
        this.isBackwards = isBackwards;

        if(isJump || isBackwards)
            bPlayer.addCooldown(this);

        hasLeverage();
        start();
    }

    private boolean hasLeverage() {
        Location location = player.getLocation();
        if (location.getBlock().getRelative(BlockFace.NORTH).getType().isSolid()) {
            return true;
        } else if (location.getBlock().getRelative(BlockFace.SOUTH).getType().isSolid()) {
            return true;
        } else if (location.getBlock().getRelative(BlockFace.WEST).getType().isSolid()) {
            return true;
        } else if (location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            return true;
        } else if (location.getBlock().getRelative(BlockFace.EAST).getType().isSolid()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public long getCooldown() {
        return cooldownJump;
    }

    @Override
    public String getName() {
        return "Windsweep";
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void load() {
        listener = new WindsweepListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);

        ProjectKorra.plugin.getServer().getPluginManager().addPermission(new Permission("bending.ability.Windsweep"));

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.Velocity", 3);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.Speed", 6);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.Cooldown", 500);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.VelocityBackwards", 2);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.BackwardsJumpHeight", 1);
        ConfigManager.defaultConfig.save();


        ProjectKorra.plugin.getLogger().info(getName() + " " + getVersion() + " by " + getAuthor() + " has been successfully enabled.");
    }

    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreCooldowns(this)) {
            remove();
            return;
        }
        // the leap
        if (isJump) {
            if (!hasLeverage()) {
                remove();
                return;
            } else {
                getAirbendingParticles().display(player.getLocation(), 30, (Math.random()), 0.3, (Math.random()));
                Vector direction = player.getEyeLocation().getDirection();
                player.setVelocity(direction.clone().multiply(velocity));
                playAirbendingSound(player.getLocation());
                remove();
            }
        }
        // the sprint boost
        if(!isJump && isSprinting && !isBackwards) {
            getAirbendingParticles().display(player.getLocation(), 2, 0, 1.1, 0);
            getAirbendingParticles().display(player.getLocation(), 2, 0, 1, 0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2, speedAmplifier));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 2, 3));
            if ((new Random()).nextInt(4) == 0) {
                playAirbendingSound(this.player.getLocation());
            }
        } else {
            remove();
        }
        // the backwards jump
        if(!isJump && isBackwards) {
            if(!hasLeverage()) {
                remove();
                return;
            } else {
                getAirbendingParticles().display(player.getLocation(), 30, (Math.random()), 0.3, (Math.random()));
                Vector direction = player.getEyeLocation().getDirection().multiply(-velocityBack);
                direction.setY(velocityBackHeight);
                player.setVelocity(direction);
                playAirbendingSound(player.getLocation());
                remove();
            }
        }
    }

    public void setSprinting(boolean isSprinting) {
        this.isSprinting = isSprinting;
    }

    @Override
    public void stop() {
        remove();
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission("bending.ability.Windsweep");
    }

    @Override
    public String getAuthor() {
        return "Nysseus";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "This basic airbending technique allows airbenders to jump from any surface where they might have leverage, such as the ground, but also walls. They're also able to aid their own movements on the ground";
    }

    @Override
    public String getInstructions() {
        return "Tap sneak whenever on the ground, or right next to a solid block. Click to jump backwards. Sprint while having the ability selected for a boost to your agility.";
    }
}