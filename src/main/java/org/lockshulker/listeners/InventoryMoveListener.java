package org.lockshulker.listeners;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lockshulker.LockShulker;

public class InventoryMoveListener implements Listener {

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        Material barrierMaterial = Material.matchMaterial(
                LockShulker.getInstance().getConfig().getString("settings.shulker-barrier-material", "BARRIER")
        );

        if (item != null && item.getType() == barrierMaterial &&
                item.getItemMeta() != null &&
                item.getItemMeta().getPersistentDataContainer().has(LockShulker.BARRIER_KEY, PersistentDataType.STRING)) {

            if (event.getSource().getHolder() instanceof BlockState blockState) {
                event.setCancelled(true);

                if (blockState.getWorld() != null && blockState.getLocation() != null) {
                    blockState.getWorld().playSound(blockState.getLocation(), "block.note_block.hat", 1.0f, 1.0f);
                }
            }
        }
    }
}
