package de.hglabor.plugins.ffa.kit;

import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.KitManager;
import de.hglabor.plugins.kitapi.kit.events.KitEventHandler;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class KitAbilityListener extends KitEventHandler implements Listener {
    public KitAbilityListener() {
        super(PlayerList.getInstance());
    }

    @EventHandler
    public void onPlayerAttacksLivingEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer((Player) event.getDamager());
            useKit(event, kitPlayer, kit -> kit.onPlayerAttacksLivingEntity(event, kitPlayer, (LivingEntity) event.getEntity()));
        }
    }

    @EventHandler
    public void onNinjaSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer(event.getPlayer());
            useKit(event, kitPlayer, kit -> kit.onNinjaSneak(event));
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        KitPlayer kitPlayer = playerSupplier.getKitPlayer((Player) event.getInventory().getViewers().get(0));
        useKit(event, kitPlayer, kit -> kit.onCraftItem(event));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer((Player) event.getEntity());
            useKit(event, kitPlayer, kit -> kit.onEntityDamage(event));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer((Player) event.getEntity());
            useKit(event, kitPlayer, kit -> kit.onEntityDeath(event));
        }
    }

    @EventHandler
    public void onPlayerRightClickPlayerWithKitItem(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer(event.getPlayer());
            useKitItem(event, kitPlayer, kit -> kit.onPlayerRightClickPlayerWithKitItem(event));
        }
    }

    @EventHandler
    public void onPlayerKillsLivingEntity(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer(killer);
            if (event.getEntity() instanceof Player) {
                useKit(event, kitPlayer, kit -> kit.onPlayerKillsLivingEntity(event));
            }
        }
    }


    @EventHandler
    public void onPlayerKillsPlayer(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            Bukkit.broadcastMessage(event.getEntity().getName());
            KitPlayer kitPlayer = playerSupplier.getKitPlayer(killer);
            useKit(event, kitPlayer, kit -> kit.onPlayerKillsPlayer(
                    KitManager.getInstance().getPlayer(killer),
                    KitManager.getInstance().getPlayer(event.getEntity())));
        }
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer((Player) event.getEntity());
            useKit(event, kitPlayer, kit -> kit.onEntityResurrect(event));
        }
    }

    @EventHandler
    public void onPlayerRightClickKitItem(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            KitPlayer kitPlayer = playerSupplier.getKitPlayer(event.getPlayer());
            useKitItem(event, kitPlayer, kit -> kit.onPlayerRightClickKitItem(event));
        }
    }

    public void useKit(Event event, KitPlayer kitPlayer, KitExecutor kitExecutor) {
        kitPlayer.getKits().stream().filter(kit -> canUseKit(event, kitPlayer, kit)).forEach(kitExecutor::execute);
    }

    public void useKitItem(Event event, KitPlayer kitPlayer, KitExecutor kitExecutor) {
        kitPlayer.getKits().stream().filter(kit -> canUseKitItem(event, kitPlayer, kit)).forEach(kitExecutor::execute);
    }

    @FunctionalInterface
    interface KitExecutor {
        void execute(AbstractKit kit);
    }
}
