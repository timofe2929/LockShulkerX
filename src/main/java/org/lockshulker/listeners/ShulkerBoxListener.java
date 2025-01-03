package org.lockshulker.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.ShulkerBox;
import org.bukkit.Bukkit;

public class ShulkerBoxListener implements Listener {

    @EventHandler
    public void onShulkerBoxOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof ShulkerBox) {
            Player player = (Player) event.getPlayer();
            ItemStack shulkerItem = player.getInventory().getItemInMainHand();

            if (shulkerItem != null && shulkerItem.getType().toString().contains("SHULKER_BOX")) {
                ItemMeta meta = shulkerItem.getItemMeta();

                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();

                    event.setCancelled(true);
                    player.closeInventory();

                    Inventory newInventory = Bukkit.createInventory(player, event.getInventory().getSize(), displayName);

                    newInventory.setContents(event.getInventory().getContents());

                    player.openInventory(newInventory);
                }
            }
        }
    }
}
