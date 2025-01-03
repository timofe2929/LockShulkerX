package org.lockshulker.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.lockshulker.LockShulker;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        Material barrierMaterial = Material.matchMaterial(
                LockShulker.getInstance().getConfig().getString("settings.shulker-barrier-material", "BARRIER")
        );

        if (item.getType() == barrierMaterial &&
                item.getItemMeta().getPersistentDataContainer().has(LockShulker.BARRIER_KEY, PersistentDataType.STRING)) {

            event.setCancelled(true);
            player.sendMessage("§cВы не можете перемещать барьер.");
            return;
        }

        if (event.getView().getTitle().equals("Shulker Box") && item.getType() == barrierMaterial) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете помещать барьер в инвентарь шалкера.");
            return;
        }

        if (event.getView().getTitle().equals("Shulker Box") && event.getClick().isKeyboardClick()) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете перемещать барьер с помощью горячих клавиш.");
            return;
        }

        Material panelMaterial = Material.matchMaterial(
                LockShulker.getInstance().getConfig().getString("settings.shulker-panel-material", "STAINED_GLASS_PANE")
        );
        if (item.getType() == panelMaterial &&
                item.getItemMeta().getPersistentDataContainer().has(LockShulker.PANEL_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }

        if (player.getInventory().getItemInOffHand().getType() == barrierMaterial) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Shulker Box")) {
            ItemStack[] items = event.getInventory().getContents();

            Material barrierMaterial = Material.matchMaterial(
                    LockShulker.getInstance().getConfig().getString("settings.shulker-barrier-material", "BARRIER")
            );

            for (int i = 0; i < items.length; i++) {
                ItemStack currentItem = items[i];
                if (currentItem != null && currentItem.getType() == barrierMaterial &&
                        currentItem.getItemMeta().getPersistentDataContainer().has(LockShulker.BARRIER_KEY, PersistentDataType.STRING)) {

                    items[i] = null;
                }
            }

            event.getInventory().setContents(items);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        Material barrierMaterial = Material.matchMaterial(
                LockShulker.getInstance().getConfig().getString("settings.shulker-barrier-material", "BARRIER")
        );

        if (offHandItem.getType() == barrierMaterial &&
                offHandItem.getItemMeta().getPersistentDataContainer().has(LockShulker.BARRIER_KEY, PersistentDataType.STRING)) {

            ItemStack shulkerItem = LockShulker.getInstance().getShulkerByBarrier(offHandItem);

            event.getDrops().remove(offHandItem);
            event.getDrops().add(shulkerItem);
        }
    }
}
