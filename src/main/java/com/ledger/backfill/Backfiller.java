package com.ledger.backfill;

import com.google.gson.Gson;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.entities.*;
import com.ledger.api.database.repositories.*;
import com.ledger.api.utils.TimeBucketRetriever;
import com.ledger.plugin.schedulers.PlayerBalanceUpdater;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.time.Duration;
import java.util.*;

public class Backfiller {
    private static final HashMap<String, List<Record>> records = new HashMap<>(); // playerName : List<Transaction>
    private static final HashMap<String, String> playerIds = new HashMap<>(); // playerName : uuid

    private static TransactionRepository transactionRepository;
    private static PlayerRepository playerRepository;
    private static ServerBalanceRepository serverBalanceRepository;
    private static PlayerBalanceRepository playerBalanceRepository;
    private static SchedulerRepository schedulerRepository;

    private static long firstTransactionTime;

    private static final long DAY = 24 * 60 * 60 * 1000;
    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        createDatabasesAndRepositories();
        parsePlayerIdsCache();
        parseRecordsFromLog();
        fetchUuids();
        backfillTransactions();
        backfillPlayers();
        backfillPlayerBalances();
        backfillServerBalances();
    }

    private static void backfillTransactions() {
        System.out.println("Backfilling transactions...");
        List<Transaction> transactions = new ArrayList<>();

        for (List<Record> records : records.values()) {
            for (Record record : records) {
                String playerId = playerIds.get(record.getPlayerName());
                if (playerId == null) continue;
                if (record.getAmount() > 5000000) continue;
                if (record.getBalance() > 50000000) continue;
                Transaction t = new Transaction();
                t.setPlayerId(playerId);
                t.setTimestamp(record.getTimestamp());
                t.setBalance(record.getBalance());
                t.setAmount(record.getAmount());
                t.setCause("HISTORICAL");
                transactions.add(t);
            }
        }

        transactionRepository.create(transactions);
        System.out.println("Successfully backfilled " + transactions.size() + " transactions.");
    }

    private static void backfillPlayers() {
        System.out.println("Backfilling players...");

        List<Player> players = new ArrayList<>();
        System.out.println("NUM PLAYERS: " + playerIds.keySet().size());
        for (String playerName : playerIds.keySet()) {
            String uuid = playerIds.get(playerName);

            Transaction t = transactionRepository.queryLastTransaction(uuid, System.currentTimeMillis());
            if (t == null) {
                System.out.println("NO TRANSACTION FOUND FOR PLAYER: " + playerName + "(" + uuid + ")");
                continue;
            }

            Player player = new Player();
            player.setPlayerId(uuid);
            player.setPlayerName(playerName);
            player.setLastSeen(t.getTimestamp());
            player.setBalance(t.getBalance());
            players.add(player);
        }

        playerRepository.create(players);
        System.out.println("Successfully backfilled " + players.size() + " players.");
    }

    private static void backfillPlayerBalances() {
        System.out.println("Backilling player balances...");
        PlayerBalanceUpdater updater = new PlayerBalanceUpdater(null, playerBalanceRepository, schedulerRepository, transactionRepository);
        updater.updateAllTimeBalances(firstTransactionTime);
        updater.updateMonthlyBalances();
        updater.updateWeeklyBalances();
        updater.updateDailyBalances();
        System.out.println("Successfully backfilled player balances.");
    }

    private static void backfillServerBalances() {
        System.out.println("Backfilling server balances...");

        // all time balances
        long lastRun = firstTransactionTime;
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsAllTime(firstTransactionTime)) {
            double balance = 0.00;
            int count = 0;
            for (String playerId : playerIds.values()) {
                double val = transactionRepository.queryBalanceAt(playerId, tb.getEnd());
                if (val > 0) {
                    count++;
                    balance += val;
                }
            }

            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.ALL_TIME + "-" + tb.getStart());
            sb.setTimestamp(tb.getStart());
            sb.setNumberOfPlayersTracked(count);
            sb.setBalance(balance);
            sb.setHistoryType(HistoryType.ALL_TIME);
            serverBalanceRepository.createOrUpdate(sb);

            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("server", HistoryType.ALL_TIME, lastRun);

        // monthly balance
        lastRun = firstTransactionTime;
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsMonthly(0L)) {
            double balance = 0.00;
            int count = 0;
            for (String playerId : playerIds.values()) {
                double val = transactionRepository.queryBalanceAt(playerId, tb.getEnd());
                if (val > 0) {
                    count++;
                    balance += val;
                }
            }

            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.MONTHLY + "-" + tb.getStart());
            sb.setTimestamp(tb.getStart());
            sb.setNumberOfPlayersTracked(count);
            sb.setBalance(balance);
            sb.setHistoryType(HistoryType.MONTHLY);
            serverBalanceRepository.createOrUpdate(sb);

            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("server", HistoryType.MONTHLY, lastRun);

        // weekly balance
        lastRun = firstTransactionTime;
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsWeekly(0L)) {
            double balance = 0.00;
            int count = 0;
            for (String playerId : playerIds.values()) {
                double val = transactionRepository.queryBalanceAt(playerId, tb.getEnd());
                if (val > 0) {
                    count++;
                    balance += val;
                }
            }

            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.WEEKLY + "-" + tb.getStart());
            sb.setTimestamp(tb.getStart());
            sb.setNumberOfPlayersTracked(count);
            sb.setBalance(balance);
            sb.setHistoryType(HistoryType.WEEKLY);
            serverBalanceRepository.createOrUpdate(sb);

            lastRun = tb.getStart();
        }
        schedulerRepository.setLastRun("server", HistoryType.WEEKLY, lastRun);

        // daily balance
        lastRun = firstTransactionTime;
        for (TimeBucketRetriever.TimeBucket tb : TimeBucketRetriever.retrieveTimeBucketsDaily(0L)) {
            double balance = 0.00;
            int count = 0;
            for (String playerId : playerIds.values()) {
                double val = transactionRepository.queryBalanceAt(playerId, tb.getEnd());
                if (val > 0) {
                    count++;
                    balance += val;
                }
            }

            ServerBalance sb = new ServerBalance();
            sb.setId(HistoryType.DAILY + "-" + tb.getStart());
            sb.setTimestamp(tb.getStart());
            sb.setNumberOfPlayersTracked(count);
            sb.setBalance(balance);
            sb.setHistoryType(HistoryType.DAILY);
            serverBalanceRepository.createOrUpdate(sb);

            lastRun = tb.getStart();
        }

        schedulerRepository.setLastRun("server", HistoryType.DAILY, lastRun);
        System.out.println("Successfully backfilled server balances.");
    }

    private static void createDatabasesAndRepositories() throws Exception {
        File directory = new File("C:/Users/Brice/Desktop");
        LedgerDB db = new LedgerDB(directory);
        transactionRepository = new TransactionRepository(db);
        playerRepository = new PlayerRepository(db);
        serverBalanceRepository = new ServerBalanceRepository(db);
        playerBalanceRepository = new PlayerBalanceRepository(db);
        schedulerRepository = new SchedulerRepository(db);
    }

    private static void parsePlayerIdsCache() throws Exception {
        String json = Files.readString(Path.of("C:/Users/Brice/Desktop/usercache.json"));
        UserCache[] cache = GSON.fromJson(json, UserCache[].class);
        for (UserCache u : cache) {
            if (u.getUuid() != null && !"null".equalsIgnoreCase(u.getUuid())) {
                playerIds.put(u.getName(), u.getUuid());
            }
        }

        System.out.println("NUM PLAYERS: " + playerIds.keySet().size());
    }

    private static void parseRecordsFromLog() throws Exception {
        System.out.println("Parsing records from transaction log...");
        HashMap<String, Record> tempRecords = new HashMap<>();
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Brice/Desktop/trade.txt"));
        String line = reader.readLine();

        int count = 0;

        while (line != null) {
            String[] elements = line.split(",");

            if (!"Update".equals(elements[0]) || !"Set".equals(elements[1]) || !"API".equals(elements[2])) {
                line = reader.readLine();
                continue;
            }

            if (!elements[5].contains("at")) {
                elements[5] = new StringBuilder(elements[5]).insert(5, " at").toString();
            }

            String date = elements[3].replace("\"", "") + "," + elements[4] + "," + elements[5].replace("\"", "");
            date = date.replace("UTC", "Coordinated Universal Time");

            long timestamp = format.parse(date).getTime();
            if (count == 0) {
                firstTransactionTime = timestamp;
            }
            String playerName = elements[6].replaceAll("\"", "");

            String balanceStr = elements[17].replaceAll("\"", "");
            if (balanceStr.isEmpty()) {
                balanceStr = elements[7].replaceAll("\"", "");
            }

            double balance = Double.parseDouble(balanceStr);

            if (playerName.length() < 3 || playerName.length() > 16) {
                line = reader.readLine();
                continue;
            }

            Record record = new Record();
            record.setTimestamp(timestamp);
            record.setPlayerName(playerName);
            record.setBalance(balance);

            Record existingRecord = tempRecords.get(playerName);
            tempRecords.put(playerName, record);
            double amount;
            if (existingRecord != null) {
                amount = balance - existingRecord.getBalance();
            } else {
                amount = balance;
            }

            record.setAmount(amount);
            List<Record> rl = records.computeIfAbsent(playerName, k -> new ArrayList<>());
            rl.add(record);

            count++;
            line = reader.readLine();
        }

        reader.close();
        System.out.println("Successfully parsed " + count + " transactions from " + records.keySet().size() + " players.");
    }

    private static void fetchUuids() throws Exception {
        System.out.println("Fetching " + records.keySet().size() + " player UUIDs (this may take some time - throttling requests to 1/s to avoid rate limiting)");
        Set<String> changedUsernames = new HashSet<>();
        Map<String, String> changedPlayerIds = new HashMap<>();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        long lastRequestTime = -1;
        int count = 1;

        for (String playerName : records.keySet()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + playerName))
                    .header("Content-Type", "application/json")
                    .GET().build();

            System.out.println("\t[" + count + "/" + records.keySet().size() + "] Fetching UUID of player " + playerName);

            String uuid = playerIds.get(playerName);
            if (uuid != null) {
                System.out.println("\t\tFound UUID in cache: " + uuid);
            } else {
                if (lastRequestTime != -1 && System.currentTimeMillis() - lastRequestTime < 500) {
                    Thread.sleep(500 - (System.currentTimeMillis() - lastRequestTime));
                }

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                lastRequestTime = System.currentTimeMillis();
                if (response.statusCode() == 204) {
                    System.out.println("\t\tUUID of player " + playerName + " was not found. They changed their name.");
                    changedUsernames.add(playerName);
                } else if (response.statusCode() == 200) {
                    PlayerIDResponse playerIDResponse = GSON.fromJson(response.body(), PlayerIDResponse.class);
                    System.out.println("\t\tUUID of player " + playerName + ": " + convertUuid(playerIDResponse.getId()));
                    uuid = convertUuid(playerIDResponse.getId());
                    playerIds.put(playerIDResponse.getName(), convertUuid(playerIDResponse.getId()));
                }
            }

            if (uuid != null && changedUsernames.size() > 0) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mojang.com/user/profiles/" + uuid + "/names"))
                        .header("Content-Type", "application/json")
                        .GET().build();

                if (System.currentTimeMillis() - lastRequestTime < 500) {
                    Thread.sleep(500 - (System.currentTimeMillis() - lastRequestTime));
                }

                System.out.println("\t\tFetching username history of " + playerName + " and matching against " + changedUsernames.size() + " changed usernames");

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                lastRequestTime = System.currentTimeMillis();
                PlayerIDHistoryResponse[] playerIDHistoryResponses;
                try {
                    playerIDHistoryResponses = GSON.fromJson(response.body(), PlayerIDHistoryResponse[].class);
                } catch (Exception e) {
                    System.out.println("Unexpected mojang response: " + response.body());
                    throw e;
                }

                boolean found = false;
                for (PlayerIDHistoryResponse pidhr : playerIDHistoryResponses) {
                    if (changedUsernames.contains(pidhr.getName())) {
                        changedPlayerIds.put(pidhr.getName(), uuid);
                        // playerIds.put(pidhr.getName(), uuid);
                        found = true;
                        System.out.println("\t\tFound changed username: " + pidhr.getName() + " was changed to " + playerName);
                        changedUsernames.remove(pidhr.getName());
                    }
                }
                if (!found) {
                    System.out.println("\t\tNo matches found.");
                }
            }

            count++;
        }

        System.out.println("Found " + changedUsernames.size() + " unresolved player names.");
        System.out.println("This means they have changed their username and have not played on the server since.");
        System.out.println("Removing transactions from these players - UUID is required for compatibility");
        int totalRemoved = 0;
        for (String username : changedUsernames) {
            totalRemoved += records.get(username).size();
            records.remove(username);
        }

        System.out.println("Removed " + totalRemoved + " transactions from " + changedUsernames.size() + " players.");

        for (List<Record> rl : records.values()) {
            for (Record r : rl) {
                String uuid = playerIds.get(r.getPlayerName());
                if (uuid == null) {
                    uuid = changedPlayerIds.get(r.getPlayerName());
                }
                r.setPlayerId(uuid);
            }
        }
    }

    private static String convertUuid(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, "-");
        sb.insert(13, "-");
        sb.insert(18, "-");
        sb.insert(23, "-");
        return sb.toString();
    }
}
