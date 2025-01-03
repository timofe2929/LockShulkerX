package org.lockshulker.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lockshulker.LockShulker;
import org.lockshulker.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class ShulkerLimitListener implements Listener {

    private final int maxShulkerBoxes;
    private final String maxShulkerMessage;
    private final boolean dropExcessOnExit;
    private final String shulkerDroppedMessage;

    public ShulkerLimitListener(LockShulker plugin) {
        this.maxShulkerBoxes = plugin.getConfig().getInt("settings.max-shulker-boxes", 5);
        this.maxShulkerMessage = ColorUtil.colorize(plugin.getConfig().getString("messages.max-shulker-reached", "&cВы не можете хранить более %max% шалкеров в инвентаре!"));
        this.shulkerDroppedMessage = ColorUtil.colorize(plugin.getConfig().getString("messages.shulker-dropped", "&eВаши лишние шалкеры были выброшены на землю при выходе с сервера."));
        this.dropExcessOnExit = plugin.getConfig().getBoolean("behavior.drop-excess-on-exit", true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (isShulkerBox(currentItem) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            int shulkerCount = countShulkerBoxes(player.getInventory().getContents());

            if (shulkerCount >= maxShulkerBoxes) {
                event.setCancelled(true);
                player.sendMessage(maxShulkerMessage.replace("%max%", String.valueOf(maxShulkerBoxes)));
            }
        }

        if (isShulkerBox(cursorItem) && (event.getAction() == InventoryAction.PICKUP_ALL ||
                event.getAction() == InventoryAction.PICKUP_ONE ||
                event.getAction() == InventoryAction.PICKUP_HALF ||
                event.getAction() == InventoryAction.PICKUP_SOME ||
                event.getAction() == InventoryAction.COLLECT_TO_CURSOR)) {
            int shulkerCount = countShulkerBoxes(player.getInventory().getContents());

            if (shulkerCount >= maxShulkerBoxes) {
                event.setCancelled(true);
                player.sendMessage(maxShulkerMessage.replace("%max%", String.valueOf(maxShulkerBoxes)));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        for (ItemStack item : event.getNewItems().values()) {
            if (isShulkerBox(item)) {
                int shulkerCount = countShulkerBoxes(player.getInventory().getContents());

                if (shulkerCount >= maxShulkerBoxes) {
                    event.setCancelled(true);
                    player.sendMessage(maxShulkerMessage.replace("%max%", String.valueOf(maxShulkerBoxes)));
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        if (isShulkerBox(item)) {
            int shulkerCount = countShulkerBoxes(player.getInventory().getContents());

            if (shulkerCount >= maxShulkerBoxes) {
                event.setCancelled(true);
                player.sendMessage(maxShulkerMessage.replace("%max%", String.valueOf(maxShulkerBoxes)));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        List<ItemStack> excessShulkers = new ArrayList<>();

        int shulkerCount = countShulkerBoxes(contents);

        // Собираем лишние шалкеры
        if (shulkerCount > maxShulkerBoxes) {
            int excess = shulkerCount - maxShulkerBoxes;
            for (ItemStack item : contents) {
                if (isShulkerBox(item)) {
                    excessShulkers.add(item);
                    excess--;
                    if (excess <= 0) {
                        break;
                    }
                }
            }

            for (ItemStack shulker : excessShulkers) {
                inventory.remove(shulker);
            }

            if (dropExcessOnExit) {
                for (ItemStack shulker : excessShulkers) {
                    player.getWorld().dropItemNaturally(player.getLocation(), shulker);
                }
                player.sendMessage(shulkerDroppedMessage);
            }
        }
    }

    private int countShulkerBoxes(ItemStack[] contents) {
        int count = 0;
        for (ItemStack item : contents) {
            if (isShulkerBox(item)) {
                count++;
            }
        }
        return count;
    }

    private boolean isShulkerBox(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getType().name().contains("SHULKER_BOX");
    }
}