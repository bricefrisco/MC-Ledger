package com.ledger.plugin.listeners;

import com.ledger.api.database.entities.Player;
import com.ledger.api.database.entities.Transaction;
import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.database.repositories.TransactionRepository;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import java.math.BigDecimal;

public class EconomyListener implements Listener {
    private final Plugin plugin;
    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;

    public EconomyListener(Plugin plugin, PlayerRepository playerRepository, TransactionRepository transactionRepository) {
        this.plugin = plugin;
        this.playerRepository = playerRepository;
        this.transactionRepository = transactionRepository;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void logTransaction(UserBalanceUpdateEvent event) {
        Transaction transaction = new Transaction();
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setCause(event.getCause().name());
        transaction.setPlayerId(event.getPlayer().getUniqueId().toString());
        BigDecimal oldAmount = event.getOldBalance();
        BigDecimal newAmount = event.getNewBalance();
        BigDecimal diff = newAmount.subtract(oldAmount);
        transaction.setAmount(diff.doubleValue());
        transaction.setBalance(event.getNewBalance().doubleValue());

        Player player = new Player();
        player.setPlayerId(event.getPlayer().getUniqueId().toString());
        player.setPlayerName(event.getPlayer().getName());
        player.setBalance(event.getNewBalance().doubleValue());
        player.setLastSeen(System.currentTimeMillis());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerRepository.createOrUpdate(player);
            transactionRepository.create(transaction);
        });
    }
}
