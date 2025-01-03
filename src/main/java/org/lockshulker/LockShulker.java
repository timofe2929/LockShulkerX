package org.lockshulker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.lockshulker.event.ShulkerTabCompleter;
import org.lockshulker.listeners.*;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LockShulker extends JavaPlugin implements CommandExecutor {

    private static LockShulker instance;
    public static NamespacedKey PANEL_KEY;
    public static NamespacedKey BARRIER_KEY;
    private List<String> bannedItems;
    private List<String> allowedItems;

    @Override
    public void onEnable() {
        instance = this;

        BARRIER_KEY = new NamespacedKey(this, "shulker_barrier");
        PANEL_KEY = new NamespacedKey(this, "shulker-panel");

        saveDefaultConfig();

        allowedItems = getConfig().getStringList("settings.items-pickup");

        getServer().getPluginManager().registerEvents(new ShulkerOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new ShulkerLimitListener(this), this);
        getServer().getPluginManager().registerEvents(new ShulkerBoxListener(), this);
        getServer().getPluginManager().registerEvents(new ShulkerAutoPickUp(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveListener(), this);

        this.getCommand("lockshulker").setExecutor(this);
        this.getCommand("lockshulker").setTabCompleter(new ShulkerTabCompleter());

        this.getCommand("lockshulker").setExecutor(this);

        getLogger().info("LockShulker включен!");

        Bukkit.getConsoleSender().sendMessage("§b==============================");
        Bukkit.getConsoleSender().sendMessage("§fTILock LockShulkerX был включен!");
        Bukkit.getConsoleSender().sendMessage("§fСпасибо, что пользуетесь моим плагином :)");
        Bukkit.getConsoleSender().sendMessage("§fЕсли нужна помощь, обращайтесь в тг или дс");
        Bukkit.getConsoleSender().sendMessage("§b");
        Bukkit.getConsoleSender().sendMessage("§b  ████████╗██╗██╗░░░░░░█████╗░░█████╗░██╗░░██╗ ");
        Bukkit.getConsoleSender().sendMessage("§b  ╚══██╔══╝██║██║░░░░░██╔══██╗██╔══██╗██║░██╔╝ ");
        Bukkit.getConsoleSender().sendMessage("§b  ░░░██║░░░██║██║░░░░░██║░░██║██║░░██║█████═╝░ ");
        Bukkit.getConsoleSender().sendMessage("§b  ░░░██║░░░██║██║░░░░░██║░░██║██║░░██║██╔═██╗░ ");
        Bukkit.getConsoleSender().sendMessage("§b  ░░░██║░░░██║███████╗╚█████╔╝╚█████╔╝██║░╚██╗ ");
        Bukkit.getConsoleSender().sendMessage("§b  ░░░╚═╝░░░╚═╝╚══════╝░╚════╝░░╚════╝░╚═╝░░╚═╝ ");
        Bukkit.getConsoleSender().sendMessage("§b");
        Bukkit.getConsoleSender().sendMessage("§b==============================");
    }

    @Override
    public void onDisable() {
        getLogger().info("LockShulker выключен!");
    }

    public static LockShulker getInstance() {
        return instance;
    }

    public List<String> getAllowedItems() {
        return allowedItems;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lockshulker")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("lockshulker.admin")) {
                    reloadConfig();
                    sender.sendMessage(getConfig().getString("messages.reload-success").replace("&", "§"));
                    return true;
                } else {
                    sender.sendMessage(getConfig().getString("messages.no-permission").replace("&", "§"));
                    return true;
                }
            } else if (args.length == 4 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("shulker")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Эту команду могут использовать только игроки.");
                    return true;
                }

                String targetPlayerName = args[2];
                int level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Неверный уровень шалкера. Используйте: /lockshulker give shulker <ник> [1-4]");
                    return true;
                }

                if (level < 1 || level > 4) {
                    sender.sendMessage("Неверный уровень шалкера. Используйте: /lockshulker give shulker <ник> [1-4]");
                    return true;
                }

                Player targetPlayer = getServer().getPlayer(targetPlayerName);
                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    sender.sendMessage("Игрок " + targetPlayerName + " не найден или не в сети.");
                    return true;
                }

                ItemStack shulker = createShulkerBox(level);
                targetPlayer.getInventory().addItem(shulker);
                targetPlayer.sendMessage("Вы получили шалкер уровня " + level);
                sender.sendMessage("Выдали шалкер уровня " + level + " игроку " + targetPlayerName);
                return true;
            }

            sender.sendMessage("§cНеверное использование. Используйте: /lockshulker reload или /lockshulker give shulker <ник> [1-4]");
            return true;
        }
        return false;
    }

    private ItemStack createShulkerBox(int level) {
        Material shulkerType = Material.SHULKER_BOX;
        ItemStack shulker = new ItemStack(shulkerType);
        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        ShulkerBox shulkerBox = (ShulkerBox) meta.getBlockState();

        String displayName = getConfig().getString("shulkers.level-" + level + ".name").replace("&", "§");
        String[] descriptionLines = getConfig().getStringList("shulkers.level-" + level + ".description").stream()
                .map(line -> line.replace("&", "§"))
                .toArray(String[]::new);

        shulkerBox.setCustomName(displayName);

        meta.setDisplayName(displayName);

        meta.setLore(Arrays.asList(descriptionLines));

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        switch (level) {
            case 1:
                setGlassSlots(shulkerBox, new int[]{0, 1, 2, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 24, 25, 26}, level);
                break;
            case 2:
                setGlassSlots(shulkerBox, new int[]{0, 1, 7, 8, 9, 10, 16, 17, 18, 19, 25, 26}, level);
                break;
            case 3:
                setGlassSlots(shulkerBox, new int[]{0, 8, 9, 17, 18, 26}, level);
                break;
            case 4:
                break;
            default:
                break;
        }

        meta.setBlockState(shulkerBox);
        shulker.setItemMeta(meta);

        return shulker;
    }

    private void setGlassSlots(ShulkerBox shulkerBox, int[] slots, int level) {
        String panelName = LockShulker.getInstance().getConfig().getString("settings.panel-name", "&bLockHub");
        panelName = panelName.replace("&", "§");

        String panelMaterialName = LockShulker.getInstance().getConfig().getString("settings.shulker-panel-material", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material panelMaterial = Material.matchMaterial(panelMaterialName);

        if (panelMaterial == null) {
            panelMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }

        ItemStack panelItem = new ItemStack(panelMaterial);
        ItemMeta panelMeta = panelItem.getItemMeta();
        panelMeta.setDisplayName(panelName);
        panelMeta.getPersistentDataContainer().set(LockShulker.PANEL_KEY, PersistentDataType.STRING, "lockshulker_panel");
        panelItem.setItemMeta(panelMeta);

        for (int slot : slots) {
            shulkerBox.getInventory().setItem(slot, panelItem);
        }
    }

    public ItemStack getShulkerByBarrier(ItemStack barrierItem) {
        if (barrierItem == null || !barrierItem.hasItemMeta()) {
            return null;
        }

        String shulkerLevelString = barrierItem.getItemMeta().getPersistentDataContainer().get(BARRIER_KEY, PersistentDataType.STRING);
        if (shulkerLevelString == null) {
            return null;
        }

        int shulkerLevel;
        try {
            shulkerLevel = Integer.parseInt(shulkerLevelString);
        } catch (NumberFormatException e) {
            return null;
        }

        return createShulkerBox(shulkerLevel);
    }
}