package com.ledger.plugin.schedulers;

import com.ledger.Ledger;
import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.database.entities.PlayerBalanceHistory;
import com.ledger.api.database.repositories.PlayerBalanceHistoryRepository;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerBalanceUpdater {
    private final Plugin plugin;
    private final Economy economy;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public PlayerBalanceUpdater(Plugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
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

        scheduler.scheduleAtFixedRate(submitTask, new Random().nextInt(30 - 1) + 1, 30, TimeUnit.MINUTES);
    }

    private void run() {
        Ledger.getBukkitLogger().info("Ledger scheduler to update player balances has started...");
        long now = System.currentTimeMillis();

        List<PlayerBalanceHistory> histories = new ArrayList<>();
        List<PlayerBalance> balances = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            double balance = economy.getBalance(p);

            PlayerBalanceHistory pbh = new PlayerBalanceHistory();
            pbh.setTimestamp(now);
            pbh.setPlayerId(p.getUniqueId().toString());
            pbh.setBalance(balance);
            histories.add(pbh);

            PlayerBalance pb = new PlayerBalance();
            pb.setPlayerId(p.getUniqueId().toString());
            pb.setPlayerName(p.getName());
            pb.setBalance(balance);
            balances.add(pb);
        }

        PlayerBalanceHistoryRepository.createAll(histories);

        for (PlayerBalance balance : balances) {
            PlayerBalanceRepository.createOrUpdate(balance);
        }
        Ledger.getBukkitLogger().info("Ledger scheduler successfully updated player balances.");
    }
}
