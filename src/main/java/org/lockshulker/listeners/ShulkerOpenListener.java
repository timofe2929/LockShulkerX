package org.lockshulker.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lockshulker.LockShulker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShulkerOpenListener implements Listener {

    private final LockShulker plugin;
    private final Map<UUID, ItemStack> openShulkers = new HashMap<>();
    private final Map<UUID, ItemStack[]> shulkerInitialContents = new HashMap<>();
    private final Map<UUID, Long> lastOpenTime = new HashMap<>();

    public ShulkerOpenListener(LockShulker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShulkerOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        int delay = plugin.getConfig().getInt("settings.shulker-open-delay", 5);
        long currentTime = System.currentTimeMillis();
        long lastTimeOpened = lastOpenTime.getOrDefault(playerId, 0L);

        if ((currentTime - lastTimeOpened) < delay * 1000) {
            player.sendMessage("§cПодождите " + (delay - (currentTime - lastTimeOpened) / 1000) + " секунд перед повторным открытием.");
            event.setCancelled(true);
            return;
        }

        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        ItemStack shulkerItem = null;
        boolean isOffHand = false;

        if (mainHandItem != null && mainHandItem.getType().name().contains("SHULKER_BOX")) {
            shulkerItem = mainHandItem;
        } else if (offHandItem != null && offHandItem.getType().name().contains("SHULKER_BOX")) {
            shulkerItem = offHandItem;
            isOffHand = true;
        }

        if (shulkerItem == null) {
            return;
        }

        boolean requirePermission = plugin.getConfig().getBoolean("settings.require-permission-to-open-air", true);
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            if (requirePermission && !player.hasPermission("lockshulker.open.air")) {
                player.sendMessage("§cУ вас нет прав для открытия шалкера в воздухе.");
                return;
            }

            BlockStateMeta shulkerMeta = (BlockStateMeta) shulkerItem.getItemMeta();
            ShulkerBox shulkerBox = (ShulkerBox) shulkerMeta.getBlockState();
            ItemStack[] shulkerContents = shulkerBox.getInventory().getContents();

            Inventory shulkerInventory = Bukkit.createInventory(player, 27, "Shulker Box");
            shulkerInventory.setContents(shulkerContents);
            player.openInventory(shulkerInventory);

            openShulkers.put(playerId, shulkerItem);
            shulkerInitialContents.put(playerId, shulkerContents.clone());
            lastOpenTime.put(playerId, currentTime);

            ItemStack barrier = new ItemStack(Material.matchMaterial(plugin.getConfig().getString("settings.shulker-barrier-material")));
            ItemMeta meta = barrier.getItemMeta();
            meta.setDisplayName(plugin.getConfig().getString("settings.shulker-barrier-name").replace("&", "§"));
            meta.getPersistentDataContainer().set(LockShulker.BARRIER_KEY, PersistentDataType.STRING, "lockshulker_barrier");
            barrier.setItemMeta(meta);

            if (isOffHand) {
                player.getInventory().setItemInOffHand(barrier);
            } else {
                player.getInventory().setItemInMainHand(barrier);
            }

            player.playSound(player.getLocation(), plugin.getConfig().getString("settings.shulker-open-sound"), 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onShulkerClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        Inventory inventory = event.getInventory();

        if (event.getView().getTitle().equals("Shulker Box") && openShulkers.containsKey(playerId)) {
            ItemStack shulkerItem = openShulkers.get(playerId);

            BlockStateMeta shulkerMeta = (BlockStateMeta) shulkerItem.getItemMeta();
            ShulkerBox shulkerBox = (ShulkerBox) shulkerMeta.getBlockState();
            ItemStack[] updatedContents = inventory.getContents();

            long openTime = lastOpenTime.getOrDefault(playerId, 0L);
            if (System.currentTimeMillis() - openTime < 100) {
                player.sendMessage("§cВы слишком быстро закрыли шалкер, изменения не будут сохранены.");
                updatedContents = shulkerInitialContents.getOrDefault(playerId, updatedContents);
            }

            shulkerBox.getInventory().setContents(updatedContents);
            shulkerMeta.setBlockState(shulkerBox);
            shulkerItem.setItemMeta(shulkerMeta);

            if (player.getInventory().getItemInOffHand().getType() == Material.matchMaterial(plugin.getConfig().getString("settings.shulker-barrier-material"))) {
                player.getInventory().setItemInOffHand(shulkerItem);
            } else {
                player.getInventory().setItemInMainHand(shulkerItem);
            }

            player.playSound(player.getLocation(), plugin.getConfig().getString("settings.shulker-close-sound"), 1.0f, 1.0f);

            openShulkers.remove(playerId);
            shulkerInitialContents.remove(playerId);
        }
    }
}
