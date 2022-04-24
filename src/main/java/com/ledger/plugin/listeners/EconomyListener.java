package com.ledger.plugin.listeners;

import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.database.entities.PlayerBalanceHistory;
import com.ledger.api.database.entities.TransactionLog;
import com.ledger.api.database.repositories.PlayerBalanceHistoryRepository;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.database.repositories.TransactionLogRepository;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

public class EconomyListener implements Listener {
    private final Plugin plugin;
    private final Economy economy;

    public EconomyListener(Plugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void logTransaction(UserBalanceUpdateEvent event) {
        TransactionLog log = new TransactionLog();
        log.setTimestamp(System.currentTimeMillis());
        log.setCause(event.getCause().name());
        log.setPlayerId(event.getPlayer().getUniqueId().toString());
        BigDecimal oldAmount = event.getOldBalance();
        BigDecimal newAmount = event.getNewBalance();
        BigDecimal diff = newAmount.subtract(oldAmount);
        log.setAmount(diff.doubleValue());
        log.setBalance(event.getNewBalance().doubleValue());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> TransactionLogRepository.create(log));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void recordBalanceHistory(PlayerQuitEvent event) {
        long timestamp = System.currentTimeMillis();
        double balance = economy.getBalance(event.getPlayer());

        PlayerBalanceHistory pbh = new PlayerBalanceHistory();
        pbh.setPlayerId(event.getPlayer().getUniqueId().toString());
        pbh.setTimestamp(timestamp);
        pbh.setBalance(balance);

        PlayerBalance pb = new PlayerBalance();
        pb.setPlayerId(event.getPlayer().getUniqueId().toString());
        pb.setPlayerName(event.getPlayer().getName());
        pb.setBalance(balance);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerBalanceHistoryRepository.create(pbh);
            PlayerBalanceRepository.createOrUpdate(pb);
        });
    }
}
