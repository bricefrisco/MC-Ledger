package com.ledger;

import com.ledger.api.HttpServer;
import com.ledger.api.database.repositories.PlayerBalanceHistoryRepository;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.database.repositories.ServerBalanceHistoryRepository;
import com.ledger.api.database.repositories.TransactionLogRepository;
import com.ledger.plugin.commands.Commands;
import com.ledger.plugin.listeners.EconomyListener;
import com.ledger.plugin.schedulers.DataPurger;
import com.ledger.plugin.schedulers.PlayerBalanceUpdater;
import com.ledger.plugin.schedulers.ServerBalanceUpdater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class Ledger extends JavaPlugin {
    private static File dataFolder;
    private static Logger logger;
    private static FileConfiguration config;
    private static HttpServer server;
    private static Economy economy = null;

    public Ledger() {
        logger = getLogger();
        config = getConfig();
        dataFolder = getDataFolder();
    }

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults();
        saveDefaultConfig();

        this.initializeDatabases();
        this.initializeEconomy();
        this.registerEventListeners();
        this.startSchedulers();
        this.startServer();
        this.registerCommands();
    }

    @Override
    public void onDisable() {
        this.stopServer();
        getServer().getScheduler().cancelTasks(this);
    }

    public static File loadFile(String fileName) {
        File file = new File(dataFolder, fileName);
        return loadFile(file);
    }

    public static File loadFile(File file) {
        if (!file.exists()) {
            try {
                if (file.getParent() != null) {
                    file.getParentFile().mkdirs();
                }

                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static Configuration getConfiguration() {
        return config;
    }

    private void initializeEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }

    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new EconomyListener(this, economy), this);
    }

    private void initializeDatabases() {
        TransactionLogRepository.load();
        PlayerBalanceRepository.load();
        PlayerBalanceHistoryRepository.load();
        ServerBalanceHistoryRepository.load();
    }

    private void startSchedulers() {
        new ServerBalanceUpdater(this).startScheduler();
        new PlayerBalanceUpdater(this, economy).startScheduler();
        new DataPurger(this).startScheduler();
    }

    private void startServer() {
        try {
            if (server == null) {
                server = new HttpServer();
            }
            server.start();
        } catch (Exception e) {
            logger.warning("Could not start web server: " + e.getMessage());
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("ledger")).setExecutor(new Commands(this));
    }

    public static Logger getBukkitLogger() {
        return logger;
    }
}
