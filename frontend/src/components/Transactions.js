import React, { useState, useEffect } from "react";
import Transaction from "./Transaction";
import { Button, FormControl, MenuItem, Select } from "@mui/material";
import DateTimePicker from "react-datetime-picker";
import InfiniteScroll from "react-infinite-scroll-component";

const Transactions = ({ session, fetchWithAuth }) => {
  const getInitialSelectedPlayer = () => {
    if (!session) return "";
    if (!session.permissions.includes("ledger.player-charts.view-all"))
      return session.playerId;
    return "AP";
  };

  const [transactions, setTransactions] = useState();
  const [numTransactions, setNumTransactions] = useState();
  const [players, setPlayers] = useState({});
  const [selectedPlayer, setSelectedPlayer] = useState(
    getInitialSelectedPlayer()
  );
  const [page, setPage] = useState(0);
  const [date, setDate] = useState(new Date());
  const [ascDesc, setAscDesc] = useState("desc");

  const fetchPlayerIds = () => {
    fetchWithAuth(`${process.env.REACT_APP_BACKEND_API}/player-ids`).then(
      (res) => {
        const tempPlayers = {};

        for (const pid of res) {
          if (
            !session.permissions.includes("ledger.player-charts.view-all") &&
            session.pid !== pid
          ) {
            continue;
          }

          tempPlayers[pid.id] = pid.name;
        }

        setPlayers(tempPlayers);
      }
    );
  };

  const fetchTransactions = (searchButtonClicked = false) => {
    if (searchButtonClicked && page) {
      setPage(0);
      return;
    }

    const url = new URL(process.env.REACT_APP_BACKEND_API + "/transactions");
    if (selectedPlayer && selectedPlayer !== "AP") {
      url.searchParams.append("playerId", selectedPlayer);
    }

    url.searchParams.append("page", page);
    url.searchParams.append("ascending", ascDesc === "asc");
    url.searchParams.append("timestamp", date.getTime());

    fetchWithAuth(url).then((res) => {
      setNumTransactions(res.totalCount);
      if (!page) {
        setTransactions(res.transactions);
      } else {
        setTransactions([...transactions, ...res.transactions]);
      }
    });
  };

  useEffect(() => {
    document.title = "Ledger | Transactions";
    if (!session) return;
    fetchPlayerIds();
  }, []);

  useEffect(() => {
    if (
      !session ||
      (!session.permissions.includes("ledger.transactions.view-own") &&
        !sessionStorage.permissions.includes("ledger.transactions.view-all"))
    ) {
      return;
    }
    fetchTransactions();
  }, [page]);

  if (
    !session ||
    (!session.permissions.includes("ledger.transactions.view-own") &&
      !sessionStorage.permissions.includes("ledger.transactions.view-all"))
  ) {
    return <div className="unauthorized">Unauthorized to view this page.</div>;
  }

  return (
    <>
      <div
        style={{
          borderBottom: "1px solid rgba(255, 255, 255, 0.3)",
          padding: 15,
          paddingLeft: 20,
          display: "flex",
          alignItems: "center",
        }}
      >
        <FormControl variant="standard">
          <Select
            value={selectedPlayer}
            onChange={(e) => setSelectedPlayer(e.target.value)}
          >
            {session.permissions.includes("ledger.transactions.view-all") && (
              <MenuItem value="AP">All Players</MenuItem>
            )}

            {players &&
              Object.keys(players).map((playerId) => (
                <MenuItem key={playerId} value={playerId}>
                  {players[playerId]}
                </MenuItem>
              ))}
          </Select>
        </FormControl>

        <FormControl variant="standard">
          <Select value={ascDesc} onChange={(e) => setAscDesc(e.target.value)}>
            <MenuItem value="desc">Before</MenuItem>
            <MenuItem value="asc">After</MenuItem>
          </Select>
        </FormControl>

        <DateTimePicker
          value={date}
          onChange={setDate}
          disableClock
          clearIcon={false}
          disableCalendar
        />

        <Button
          variant="outlined"
          sx={{ maxHeight: "35px", fontWeight: "bold" }}
          onClick={() => fetchTransactions(true)}
        >
          Search
        </Button>
      </div>

      {transactions && players && (
        <InfiniteScroll
          dataLength={transactions.length}
          next={() => setPage((page) => page + 1)}
          hasMore={transactions.length !== numTransactions}
        >
          <table border={0} cellSpacing={0} style={{ width: "100%" }}>
            <thead>
              <tr>
                <th align="center">#</th>
                <th align="left">Player/Time</th>
                <th align="left">Cause</th>
                <th align="left">Amount</th>
                <th align="left">Balance</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction, idx) => (
                <Transaction
                  key={idx}
                  idx={idx}
                  playerId={transaction.playerId}
                  playerName={players["b0773db1-1ca3-44fc-9240-229d6e16a945"]}
                  amount={transaction.amount}
                  timestamp={transaction.timestamp}
                  cause={transaction.cause}
                  balance={transaction.balance}
                />
              ))}
            </tbody>
          </table>
        </InfiniteScroll>
      )}

      {transactions && !transactions.length && (
        <span className="no-results">No results found.</span>
      )}
    </>
  );
};

export default Transactions;
