package com.ledger.plugin.commands;

import com.ledger.Ledger;
import com.ledger.api.services.SessionService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {
    private static final ArrayList<String> PERMISSIONS = new ArrayList<>() {
        {
            add("ledger.balances.view");
            add("ledger.server-chart.view");
            add("ledger.transactions.view-own");
            add("ledger.player-charts.view-own");
            add("ledger.transactions.view-all");
            add("ledger.player-charts.view-all");
        }
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        List<String> permissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (player.hasPermission(permission)) {
                permissions.add(permission);
            }
        }

        if (permissions.size() == 0) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        String host = Ledger.getConfiguration().getString("server-url");

        String authId = SessionService.createAuthorization(player.getUniqueId().toString(), player.getName(), permissions);
        player.sendMessage(ChatColor.YELLOW + "[Ledger] One-time login: " + ChatColor.RESET + ChatColor.AQUA + ChatColor.UNDERLINE + "http://" + host + "/sessions/" + authId);
        return true;
    }

}
