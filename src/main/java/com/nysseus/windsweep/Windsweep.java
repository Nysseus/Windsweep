package com.nysseus.windsweep;

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

    public enum usageType {
        LEAP, BACKWARDS, SPRINT
    }

    private usageType UsageType;

    @Attribute(Attribute.COOLDOWN)
    private long cooldownJump;

    public boolean backJumpEnabled;
    public boolean sprintBoostEnabled;
    public boolean jumpEnabled;

    @Attribute(Attribute.SPEED)
    private double velocity;

    @Attribute(Attribute.SPEED)
    private double velocityBack;

    @Attribute(Attribute.SPEED)
    private double velocityBackHeight;

    private int speedAmplifier;

    private double duration;

    private Listener listener;

    public Windsweep(Player player, usageType Usage_Type) {
        super(player);

        cooldownJump = ConfigManager.getConfig().getLong("ExtraAbilities.Nysseus.Windsweep.Cooldown");
        velocity = ConfigManager.getConfig().getDouble("ExtraAbilities.Nysseus.Windsweep.Velocity");
        speedAmplifier = ConfigManager.getConfig().getInt("ExtraAbilities.Nysseus.Windsweep.Speed");
        velocityBack = ConfigManager.getConfig().getDouble("ExtraAbilities.Nysseus.Windsweep.VelocityBackwards");
        velocityBackHeight = ConfigManager.getConfig().getDouble("ExtraAbilities.Nysseus.Windsweep.BackwardsJumpHeight");

        backJumpEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Nysseus.Windsweep.BackJumpEnabled");
        sprintBoostEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Nysseus.Windsweep.SprintBoostEnabled");
        jumpEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Nysseus.Windsweep.JumpEnabled");

        this.UsageType = Usage_Type;

        if (UsageType.equals(usageType.LEAP) || UsageType.equals(usageType.BACKWARDS)) {
            bPlayer.addCooldown(this);
        }

        if (UsageType.equals(usageType.SPRINT) && !sprintBoostEnabled) {
            remove();
        }
        if (UsageType.equals(usageType.BACKWARDS) && !backJumpEnabled) {
            remove();
        }
        if (UsageType.equals(usageType.LEAP) && !jumpEnabled) {
            remove();
        }

        if (!bPlayer.canBendIgnoreCooldowns(this)
        || ((UsageType.equals(usageType.BACKWARDS) || UsageType.equals(usageType.LEAP)) && !hasLeverage())
        ) {
            remove();
            return;
        }

        switch (this.UsageType) {
            case LEAP:
                this.Leap();
                break;
            case BACKWARDS:
                this.Backwards();
                break;
        }
        start();
    }

    private boolean hasLeverage() {
        Location location = player.getLocation();
        return location.getBlock().getRelative(BlockFace.NORTH).getType().isSolid()
        || location.getBlock().getRelative(BlockFace.SOUTH).getType().isSolid()
        || location.getBlock().getRelative(BlockFace.WEST).getType().isSolid()
        || location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()
        || location.getBlock().getRelative(BlockFace.EAST).getType().isSolid();
    }

    private boolean isOnGround() {
        Location location = player.getLocation();
        return location.getBlock().getRelative(BlockFace.NORTH).getType().isSolid();
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

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.VelocityBackwards", 2);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.BackwardsJumpHeight", 1);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.Cooldown", 700);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.BackJumpEnabled", true);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.SprintBoostEnabled", true);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Windsweep.JumpEnabled", true);
        ConfigManager.defaultConfig.save();


        ProjectKorra.plugin.getLogger().info(getName() + " " + getVersion() + " by " + getAuthor() + " has been successfully enabled.");
    }

    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreCooldowns(this)) {
            remove();
            return;
        }

        switch (this.UsageType) {
            case LEAP:
                this.LeapEffects();
                break;
            case BACKWARDS:
                this.BackwardsEffects();
                break;
            case SPRINT:
                this.Sprint();
                break;
        }
    }

    @Override
    public void stop() {
        remove();
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission("bending.ability.Windsweep");
    }

    // the leap
    private void Leap() {
        if (!hasLeverage()) {
            remove();
        } else {
            getAirbendingParticles().display(player.getLocation(), 30, (Math.random()), 0.3, (Math.random()));
            Vector direction = player.getEyeLocation().getDirection();
            player.setVelocity(direction.clone().multiply(velocity));
            playAirbendingSound(player.getLocation());
        }
    }

    private void LeapEffects() {
        // code for the particle effects of the leap
        this.duration = 300;
        getAirbendingParticles().display(player.getLocation(), 10, (Math.random()), 0.3, (Math.random()));
        if ((new Random()).nextInt(3) == 0) {
            playAirbendingSound(this.player.getLocation());
        }
        if ((System.currentTimeMillis() > this.getStartTime() + duration) || isOnGround()) {
            remove();
        }
    }


    // the backwards jump
    private void Backwards() {
        if (!hasLeverage()) {
            remove();
        } else {
            getAirbendingParticles().display(player.getLocation(), 30, (Math.random()), 0.3, (Math.random()));
            Vector direction = player.getEyeLocation().getDirection().multiply(-velocityBack);
            direction.setY(velocityBackHeight);
            player.setVelocity(direction);
            playAirbendingSound(player.getLocation());
        }
    }

    private void BackwardsEffects() {
        // code for the particle effects of the backwards leap
        this.duration = 150;
        getAirbendingParticles().display(player.getLocation(), 10, (Math.random()), 0.3, (Math.random()));
        if ((new Random()).nextInt(3) == 0) {
            playAirbendingSound(this.player.getLocation());
        }
        if ((System.currentTimeMillis() > this.getStartTime() + duration) || isOnGround()) {
            remove();
        }
    }

    // the sprint boost
    private void Sprint() {
        getAirbendingParticles().display(player.getLocation(), 1, 0, 1.1, 0);
        getAirbendingParticles().display(player.getLocation(), 1, 0, 1, 0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2, speedAmplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 2, 3));
        if ((new Random()).nextInt(6) == 0) {
            playAirbendingSound(this.player.getLocation());
        }
        if (!player.isSprinting()) {
            remove();
        }
    }

    @Override
    public String getAuthor() {
        return "Nysseus";
    }

    @Override
    public String getVersion() {
        return "1.2.0";
    }

    @Override
    public String getDescription() {
        return "This basic airbending technique allows airbenders to jump from any surface where they might have leverage, such as the ground, but also walls. They're also able to aid their own movements on the ground by sprinting.";
    }

    @Override
    public String getInstructions() {
        return "Tap sneak whenever on the ground or right next to a solid block to jump in the direction you are looking. Click to jump backwards instead. Sprint while having the ability selected for a boost to your agility.";
    }
}