package com.ledger.plugin.schedulers;

import com.ledger.Ledger;
import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.entities.Player;
import com.ledger.api.database.entities.ServerBalance;
import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.database.repositories.SchedulerRepository;
import com.ledger.api.database.repositories.ServerBalanceRepository;
import com.ledger.api.utils.TimeBucketRetriever;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerBalanceUpdater {
    private final Plugin plugin;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final PlayerRepository playerRepository;
    private final ServerBalanceRepository serverBalanceRepository;
    private final SchedulerRepository schedulerRepository;

    public ServerBalanceUpdater(Plugin plugin, PlayerRepository playerRepository, ServerBalanceRepository serverBalanceRepository, SchedulerRepository schedulerRepository) {
        this.plugin = plugin;
        this.playerRepository = playerRepository;
        this.serverBalanceRepository = serverBalanceRepository;
        this.schedulerRepository = schedulerRepository;
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

        scheduler.scheduleAtFixedRate(submitTask, new Random().nextInt(10 - 1) + 1, 10, TimeUnit.MINUTES);
    }

    public void updateDailyBalances() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minute - (minute % 10));


        List<Player> players = playerRepository.queryForAll();
        double balance = 0.00;
        for (Player p : players) {
            balance += p.getBalance();
        }

        ServerBalance sb = new ServerBalance();
        sb.setId(HistoryType.DAILY + "-" + calendar.getTimeInMillis());
        sb.setTimestamp(calendar.getTimeInMillis());
        sb.setBalance(balance);
        sb.setNumberOfPlayersTracked(players.size());
        sb.setHistoryType(HistoryType.DAILY);

        serverBalanceRepository.create(sb);
    }

    public void updateWeeklyBalances() {
        long lastRun = schedulerRepository.getLastRun("server", HistoryType.WEEKLY);
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsWeekly(lastRun)) {
            ServerBalance at = serverBalanceRepository.queryBalanceAt(tb.getEnd());
            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.WEEKLY + "-" + tb.getStart());
            sb.setHistoryType(HistoryType.WEEKLY);
            sb.setTimestamp(tb.getStart());
            sb.setBalance(at == null ? 0 : at.getBalance());
            sb.setNumberOfPlayersTracked(at == null ? 0 : at.getNumberOfPlayersTracked());
            serverBalanceRepository.createOrUpdate(sb);
            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("server", HistoryType.WEEKLY, lastRun);
    }

    public void updateMonthlyBalances() {
        long lastRun = schedulerRepository.getLastRun("server", HistoryType.MONTHLY);
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsMonthly(lastRun)) {
            ServerBalance at = serverBalanceRepository.queryBalanceAt(tb.getEnd());
            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.MONTHLY + "-" + tb.getStart());
            sb.setHistoryType(HistoryType.MONTHLY);
            sb.setTimestamp(tb.getStart());
            sb.setBalance(at == null ? 0 : at.getBalance());
            sb.setNumberOfPlayersTracked(at == null ? 0 : at.getNumberOfPlayersTracked());
            serverBalanceRepository.createOrUpdate(sb);
            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("server", HistoryType.MONTHLY, lastRun);
    }

    public void updateAllTimeBalances() {
        long lastRun = schedulerRepository.getLastRun("server", HistoryType.ALL_TIME);
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsAllTime(lastRun)) {
            ServerBalance at = serverBalanceRepository.queryBalanceAt(tb.getEnd());
            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.ALL_TIME + "-" + tb.getStart());
            sb.setHistoryType(HistoryType.ALL_TIME);
            sb.setTimestamp(tb.getStart());
            sb.setBalance(at == null ? 0 : at.getBalance());
            sb.setNumberOfPlayersTracked(at == null ? 0 : at.getNumberOfPlayersTracked());
            serverBalanceRepository.createOrUpdate(sb);
            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("server", HistoryType.ALL_TIME, lastRun);
    }

    private void run() {
        Ledger.getBukkitLogger().info("Ledger scheduler to update server balances has started...");
        updateDailyBalances();
        updateWeeklyBalances();
        updateMonthlyBalances();
        updateAllTimeBalances();
        Ledger.getBukkitLogger().info("Ledger scheduler successfully updated server balance.");
    }
}
