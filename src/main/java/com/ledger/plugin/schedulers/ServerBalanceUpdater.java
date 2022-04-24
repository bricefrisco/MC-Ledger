package com.ledger.plugin.schedulers;

import com.ledger.Ledger;
import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.database.entities.ServerBalanceHistory;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.database.repositories.ServerBalanceHistoryRepository;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerBalanceUpdater {
    private final Plugin plugin;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ServerBalanceUpdater(Plugin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        Bukkit.getServicesManager().register(ServerBalanceUpdater.class, this, plugin, ServicePriority.Normal);

        final Runnable submitTask = () -> {
            if (!plugin.isEnabled()) {
                scheduler.shutdown();
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::run);
        };

        scheduler.scheduleAtFixedRate(submitTask, new Random().nextInt(30 - 1) + 1, 30, TimeUnit.MINUTES);
    }

    private void run() {
        Ledger.getBukkitLogger().info("Ledger scheduler to update server balance has started...");
        ServerBalanceHistory history = new ServerBalanceHistory();
        history.setTimestamp(System.currentTimeMillis());

        double result = 0.00;
        List<PlayerBalance> balances = PlayerBalanceRepository.queryForAll();
        if (balances == null) return;
        history.setNumberOfPlayersTracked(balances.size());

        for (PlayerBalance balance : balances) {
            result += balance.getBalance();
        }

        history.setBalance(result);
        ServerBalanceHistoryRepository.create(history);
        Ledger.getBukkitLogger().info("Ledger scheduler successfully updated server balance.");
    }
}
