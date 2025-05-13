package com.nysseus.windsweep;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;


public class WindsweepListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (Suffocate.isBreathbent(player)) {
            event.setCancelled(true);
            return;
        }
        if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
            event.setCancelled(true);
            return;
        }
        if (bPlayer.isChiBlocked()) {
            event.setCancelled(true);
            return;
        }

        if (!event.getPlayer().isSneaking()) return;

        if (bPlayer.canBend(CoreAbility.getAbility(Windsweep.class))) {
            player.setSprinting(false);
            new Windsweep(player, Windsweep.usageType.LEAP);
        }
    }

    @EventHandler
    public void onSprint (PlayerToggleSprintEvent event){
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (Suffocate.isBreathbent(player)) {
            event.setCancelled(true);
            return;
        }

        if (bPlayer.isChiBlocked()) {
            event.setCancelled(true);
            return;
        }

        if (player.isSprinting()) {
            event.setCancelled(true);
            player.setSprinting(false);
            return;
        }

        if (bPlayer.canBendIgnoreCooldowns(CoreAbility.getAbility(Windsweep.class))) {
            new Windsweep(player, Windsweep.usageType.SPRINT);
        }
    }

    @EventHandler
    public void onClick (PlayerInteractEvent event){
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (Suffocate.isBreathbent(player)) {
            event.setCancelled(true);
            return;
        }
        if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
            event.setCancelled(true);
            return;
        }
        if (bPlayer.isChiBlocked()) {
            event.setCancelled(true);
            return;
        }


        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.isCancelled()) return;

        if (bPlayer.canBend(CoreAbility.getAbility(Windsweep.class))) {
            player.setSprinting(false);
            new Windsweep(player, Windsweep.usageType.BACKWARDS);
        }
    }
}