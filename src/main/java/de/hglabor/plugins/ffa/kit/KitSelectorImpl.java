package de.hglabor.plugins.ffa.kit;


import com.google.common.collect.ImmutableMap;
import de.hglabor.plugins.ffa.FFA;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.kits.NoneKit;
import de.hglabor.plugins.kitapi.kit.selector.KitSelector;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class KitSelectorImpl extends KitSelector {

    public KitSelectorImpl() {
        super(ChatColor.GOLD.toString() + ChatColor.BOLD + "Kit Selector");
    }

    @EventHandler
    public void onKitSelectorClick(PlayerInteractEvent event) {
        if (event.getItem() != null && isKitSelectorItem(event.getItem())) {
            FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(event.getPlayer());
            //InventoryClickEvent fires PlayerInteractEvent lol
            if (event.getPlayer().getOpenInventory().getTitle().contains(KIT_SELECTOR_TITLE)) {
                return;
            }
            if (ffaPlayer.isInKitSelection()) {
                openFirstPage(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String inventoryTitle = event.getView().getTitle();

        if (clickedItem == null) {
            return;
        }

        if (inventoryTitle.contains(KIT_SELECTOR_TITLE)) {
            event.setCancelled(true);
            if (nextPage(inventoryTitle, clickedItem, player)) {
                return;
            }
            if (lastPage(inventoryTitle, clickedItem, player)) {
                return;
            }
            ItemStack kitSelector = getKitSelectorInHand(player);
            AbstractKit kit = KitApi.getInstance().byItem(clickedItem);
            if (kitSelector != null && isKitSelectorItem(kitSelector) && kit != null) {
                int index = Integer.parseInt(kitSelector.getItemMeta().getDisplayName().substring(kitSelector.getItemMeta().getDisplayName().length() - 1)) - 1;
                FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);
                ffaPlayer.setKit(kit, index);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                player.sendMessage(Localization.INSTANCE.getMessage("kitSelection.pickMessage",
                        ImmutableMap.of("kitName", kit.getName()), ChatUtils.locale(player)));
                player.closeInventory();
                if (ffaPlayer.getKits().stream().noneMatch(kits -> kits.equals(NoneKit.INSTANCE))) {
                    FFA.getArenaManager().teleportToArena(player);
                }
            }
        }
    }

    private ItemStack getKitSelectorInHand(Player player) {
        for (ItemStack kitSelectorItem : kitSelectorItems) {
            if (kitSelectorItem.isSimilar(player.getInventory().getItemInMainHand())) {
                return player.getInventory().getItemInMainHand();
            } else if (kitSelectorItem.isSimilar(player.getInventory().getItemInOffHand())) {
                return player.getInventory().getItemInOffHand();
            }
        }
        return null;
    }
}
