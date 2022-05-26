package com.ledger.plugin.schedulers;

import com.ledger.Ledger;
import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.entities.Player;
import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.database.repositories.SchedulerRepository;
import com.ledger.api.database.repositories.TransactionRepository;
import com.ledger.api.utils.TimeBucketRetriever;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerBalanceUpdater {
    private final Plugin plugin;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final PlayerBalanceRepository playerBalanceRepository;
    private final SchedulerRepository schedulerRepository;
    private final TransactionRepository transactionRepository;

    public PlayerBalanceUpdater(Plugin plugin, PlayerBalanceRepository playerBalanceRepository, SchedulerRepository schedulerRepository, TransactionRepository transactionRepository) {
        this.plugin = plugin;
        this.playerBalanceRepository = playerBalanceRepository;
        this.schedulerRepository = schedulerRepository;
        this.transactionRepository = transactionRepository;
    }

    public void startScheduler() {
        Bukkit.getServicesManager().register(PlayerBalanceUpdater.class, this, plugin, ServicePriority.Normal);

        final Runnable submitTask = () -> {
            if (!plugin.isEnabled()) {
                scheduler.shutdown();
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::run);
        };

        scheduler.scheduleAtFixedRate(submitTask, new Random().nextInt(10 - 1) + 1, 10, TimeUnit.MINUTES);
    }

    public void updateDailyBalances() {
        long lastRun = schedulerRepository.getLastRun("player", HistoryType.DAILY);
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsDaily(lastRun)) {
            for (String playerId : transactionRepository.queryPlayersChanged(tb.getStart(), tb.getEnd())) {
                PlayerBalance pb = new PlayerBalance();
                pb.setId(HistoryType.DAILY + "-" + playerId + "-" + tb.getStart());
                pb.setPlayerId(playerId);
                pb.setTimestamp(tb.getStart());
                pb.setBalance(transactionRepository.queryBalanceAt(playerId, tb.getEnd()));
                pb.setHistoryType(HistoryType.DAILY);
                playerBalanceRepository.createOrUpdate(pb);
            }
            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("player", HistoryType.DAILY, lastRun);
    }

    public void updateWeeklyBalances() {
        long lastRun = schedulerRepository.getLastRun("player", HistoryType.WEEKLY);
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsWeekly(lastRun)) {
            for (String playerId : transactionRepository.queryPlayersChanged(tb.getStart(), tb.getEnd())) {
                PlayerBalance pb = new PlayerBalance();
                pb.setId(HistoryType.WEEKLY + "-" + playerId + "-" + tb.getStart());
                pb.setPlayerId(playerId);
                pb.setTimestamp(tb.getStart());
                pb.setBalance(transactionRepository.queryBalanceAt(playerId, tb.getEnd()));
                pb.setHistoryType(HistoryType.WEEKLY);
                playerBalanceRepository.createOrUpdate(pb);
            }
            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("player", HistoryType.WEEKLY, lastRun);
    }

    public void updateMonthlyBalances() {
        long lastRun = schedulerRepository.getLastRun("player", HistoryType.MONTHLY);
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsMonthly(lastRun)) {
            for (String playerId : transactionRepository.queryPlayersChanged(tb.getStart(), tb.getEnd())) {
                PlayerBalance pb = new PlayerBalance();
                pb.setId(HistoryType.MONTHLY + "-" + playerId + "-" + tb.getStart());
                pb.setPlayerId(playerId);
                pb.setTimestamp(tb.getStart());
                pb.setBalance(transactionRepository.queryBalanceAt(playerId, tb.getEnd()));
                pb.setHistoryType(HistoryType.MONTHLY);
                playerBalanceRepository.createOrUpdate(pb);
            }
            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("player", HistoryType.MONTHLY, lastRun);
    }

    public void updateAllTimeBalances() {
        updateAllTimeBalances(0L);
    }

    public void updateAllTimeBalances(long since) {
        long lastRun;

        if (since == 0L) {
            lastRun = schedulerRepository.getLastRun("player", HistoryType.ALL_TIME);
        } else {
            lastRun = since;
        }

        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsAllTime(lastRun)) {
            for (String playerId : transactionRepository.queryPlayersChanged(tb.getStart(), tb.getEnd())) {
                PlayerBalance pb = new PlayerBalance();
                pb.setId(HistoryType.ALL_TIME + "-" + playerId + "-" + tb.getStart());
                pb.setPlayerId(playerId);
                pb.setTimestamp(tb.getStart());
                double balance = transactionRepository.queryBalanceAt(playerId, tb.getEnd());
                pb.setBalance(balance);
                pb.setHistoryType(HistoryType.ALL_TIME);
                playerBalanceRepository.createOrUpdate(pb);
            }
            lastRun = tb.getStart();
        }

        schedulerRepository.setLastRun("player", HistoryType.ALL_TIME, lastRun);
    }

    private void run() {
        Ledger.getBukkitLogger().info("Ledger scheduler to update player balances has started...");
        updateDailyBalances();
        updateWeeklyBalances();
        updateMonthlyBalances();
        updateAllTimeBalances();
        Ledger.getBukkitLogger().info("Ledger scheduler successfully updated player balances.");
    }
}
