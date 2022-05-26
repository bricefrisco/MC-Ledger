package com.ledger;

import com.ledger.api.HttpServer;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.repositories.*;
import com.ledger.plugin.commands.Commands;
import com.ledger.plugin.listeners.EconomyListener;
import com.ledger.plugin.schedulers.DataPurger;
import com.ledger.plugin.schedulers.PlayerBalanceUpdater;
import com.ledger.plugin.schedulers.ServerBalanceUpdater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.logging.Logger;

public class Ledger extends JavaPlugin {
    private static File dataFolder;
    private static Logger logger;
    private static FileConfiguration config;

    private HttpServer server = null;

    public Ledger() {
        logger = getLogger();
        config = getConfig();
        dataFolder = getDataFolder();
    }

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Database and repository initialization
        LedgerDB database;
        TransactionRepository transactionRepository;
        PlayerRepository playerRepository;
        PlayerBalanceRepository playerBalanceRepository;
        ServerBalanceRepository serverBalanceRepository;
        SchedulerRepository schedulerRepository;
        try {
            database = new LedgerDB(dataFolder);
            transactionRepository = new TransactionRepository(database);
            playerRepository = new PlayerRepository(database);
            playerBalanceRepository = new PlayerBalanceRepository(database);
            serverBalanceRepository = new ServerBalanceRepository(database);
            schedulerRepository = new SchedulerRepository(database);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Economy initialization
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        Economy economy = rsp.getProvider();

        // Event listener initialization
        getServer().getPluginManager().registerEvents(new EconomyListener(this, playerRepository, transactionRepository), this);

        // Scheduler initialization
        new ServerBalanceUpdater(this, playerRepository, serverBalanceRepository, schedulerRepository).startScheduler();
        new PlayerBalanceUpdater(this, playerBalanceRepository, schedulerRepository, transactionRepository).startScheduler();
        new DataPurger(this, playerBalanceRepository, serverBalanceRepository, transactionRepository).startScheduler();

        // HTTP Server initialization
        try {
            if (server == null) {
                server = new HttpServer(playerRepository, playerBalanceRepository, serverBalanceRepository, transactionRepository);
            }
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Command initialization
        getCommand("ledger").setExecutor(new Commands(this, playerRepository));
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
        }

        getServer().getScheduler().cancelTasks(this);
    }

    public static Configuration getConfiguration() {
        return config;
    }

    public static Logger getBukkitLogger() {
        return logger;
    }
}
