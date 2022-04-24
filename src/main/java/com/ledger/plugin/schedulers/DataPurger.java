package com.ledger.plugin.schedulers;

import com.ledger.Ledger;
import com.ledger.api.database.repositories.PlayerBalanceHistoryRepository;
import com.ledger.api.database.repositories.ServerBalanceHistoryRepository;
import com.ledger.api.database.repositories.TransactionLogRepository;
import com.ledger.api.services.SessionService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataPurger {
    private final Plugin plugin;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DataPurger(Plugin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        Bukkit.getServicesManager().register(DataPurger.class, this, plugin, ServicePriority.Normal);
        final Runnable task = () -> {
            if (!plugin.isEnabled()) {
                scheduler.shutdown();
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::run);
        };

        scheduler.scheduleAtFixedRate(task, new Random().nextInt(3 - 1) + 1, 3, TimeUnit.HOURS);
    }

    public void run() {
        Ledger.getBukkitLogger().info("Ledger data purger scheduler started...");
        int transactionLogRetention = Ledger.getConfiguration().getInt("transaction-log-retention-days");
        int playerBalanceHistoryRetention = Ledger.getConfiguration().getInt("player-balance-history-retention-days");
        int serverBalanceHistoryRetention = Ledger.getConfiguration().getInt("server-balance-history-retention-days");

        TransactionLogRepository.purgeBefore(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(transactionLogRetention));
        PlayerBalanceHistoryRepository.purgeBefore(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(playerBalanceHistoryRetention));
        ServerBalanceHistoryRepository.purgeBefore(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(serverBalanceHistoryRetention));

        SessionService.purgeOldSessions();
        SessionService.purgeOldAuthorizations();
        Ledger.getBukkitLogger().info("Ledger data purger ran successfully.");
    }
}
