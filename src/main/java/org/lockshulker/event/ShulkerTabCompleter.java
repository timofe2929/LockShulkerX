package org.lockshulker.event;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShulkerTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("lockshulker")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions = Arrays.asList("reload", "give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions = Collections.singletonList("shulker");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("shulker")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("shulker")) {
            completions = Arrays.asList("1", "2", "3", "4");
        }

        return filterByInput(completions, args[args.length - 1]);
    }

    private List<String> filterByInput(List<String> options, String input) {
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
