package org.lockshulker.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.ShulkerBox;
import org.lockshulker.LockShulker;

import java.util.List;

public class ShulkerAutoPickUp implements Listener {

    private final List<String> allowedItems;
    private final Sound pickupSound;
    private final float volume;
    private final float pitch;

    public ShulkerAutoPickUp(LockShulker plugin) {
        FileConfiguration config = plugin.getConfig();
        allowedItems = config.getStringList("settings.items-pickup");

        // Загружаем настройки звука
        String soundName = config.getString("settings.pickup-sound.name", "ENTITY_ITEM_PICKUP");
        pickupSound = Sound.valueOf(soundName.toUpperCase());
        volume = (float) config.getDouble("settings.pickup-sound.volume", 1.0);
        pitch = (float) config.getDouble("settings.pickup-sound.pitch", 1.0);
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();
        ItemStack pickedUpItem = event.getItem().getItemStack();

        if (!isAllowedItem(pickedUpItem.getType())) {
            return;
        }

        for (ItemStack item : inventory.getContents()) {
            if (isShulkerBox(item)) {
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                ShulkerBox shulker = (ShulkerBox) meta.getBlockState();

                if (shulker.getInventory().firstEmpty() != -1) {
                    shulker.getInventory().addItem(pickedUpItem);
                    meta.setBlockState(shulker);
                    item.setItemMeta(meta);
                    event.getItem().remove();
                    event.setCancelled(true);

                    event.getPlayer().playSound(event.getPlayer().getLocation(), pickupSound, volume, pitch);
                    break;
                }
            }
        }
    }

    private boolean isAllowedItem(Material material) {
        return allowedItems.contains(material.toString());
    }

    private boolean isShulkerBox(ItemStack item) {
        if (item != null && item.getItemMeta() instanceof BlockStateMeta) {
            return item.getType().toString().contains("SHULKER_BOX");
        }
        return false;
    }
}