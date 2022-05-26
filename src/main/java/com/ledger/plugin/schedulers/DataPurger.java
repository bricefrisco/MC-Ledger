package com.ledger.plugin.schedulers;

import com.ledger.Ledger;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.database.repositories.ServerBalanceRepository;
import com.ledger.api.database.repositories.TransactionRepository;
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
    private final PlayerBalanceRepository playerBalanceRepository;
    private final ServerBalanceRepository serverBalanceRepository;
    private final TransactionRepository transactionRepository;

    public DataPurger(Plugin plugin, PlayerBalanceRepository playerBalanceRepository, ServerBalanceRepository serverBalanceRepository, TransactionRepository transactionRepository) {
        this.plugin = plugin;
        this.playerBalanceRepository = playerBalanceRepository;
        this.serverBalanceRepository = serverBalanceRepository;
        this.transactionRepository = transactionRepository;
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

        scheduler.scheduleAtFixedRate(task, new Random().nextInt(10 - 1) + 1, 10, TimeUnit.MINUTES);
    }

    public void run() {
        Ledger.getBukkitLogger().info("Ledger data purger scheduler started...");
//        playerBalanceRepository.purge();
//        serverBalanceRepository.purge();
//
//        int transactionLogRetention = Ledger.getConfiguration().getInt("transaction-log-retention-days");
//        transactionRepository.purgeBefore(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(transactionLogRetention));

        SessionService.purgeOldSessions();
        SessionService.purgeOldAuthorizations();
        Ledger.getBukkitLogger().info("Ledger data purger ran successfully.");
    }
}
