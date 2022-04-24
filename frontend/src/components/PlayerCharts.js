import React, { useState, useEffect } from "react";
import { Button, FormControl, MenuItem, Select } from "@mui/material";
import { Area, AreaChart, Tooltip, XAxis, YAxis } from "recharts";
import Moment from "react-moment";
import moment from "moment";

function getWindowDimensions() {
  const { innerWidth: width, innerHeight: height } = window;
  return {
    width,
    height,
  };
}

function useWindowDimensions() {
  const [windowDimensions, setWindowDimensions] = useState(
    getWindowDimensions()
  );

  useEffect(() => {
    function handleResize() {
      setWindowDimensions(getWindowDimensions());
    }

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return windowDimensions;
}

const PlayerCharts = ({ session, fetchWithAuth }) => {
  const [data, setData] = useState();
  const [players, setPlayers] = useState();
  const [selectedPlayer, setSelectedPlayer] = useState(
    session ? session.playerId : ""
  );
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [year, setYear] = useState(2022);
  const { width, height } = useWindowDimensions();

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

  const fetchBalances = () => {
    const url = new URL(process.env.REACT_APP_BACKEND_API + "/players");
    url.searchParams.append("uuid", selectedPlayer);
    url.searchParams.append("month", month);

    fetchWithAuth(url).then((res) => {
      const data = [];
      const length = res.length;
      for (let i = 0; i < length; i++) {
        if (i === 0 || i % 2 === 0) {
          data.push({
            date: new Date(res[i]),
            balance: res[i + 1],
          });
        }
      }

      setData(data);
    });
  };

  useEffect(() => {
    document.title = "Ledger | Player Charts";
    if (
      !session ||
      (!session.permissions.includes("ledger.player-charts.view-own") &&
        !session.permissions.includes("ledger.player-charts.view-all"))
    ) {
      return;
    }
    fetchPlayerIds();
    fetchBalances();
  }, []);

  if (
    !session ||
    (!session.permissions.includes("ledger.player-charts.view-own") &&
      !session.permissions.includes("ledger.player-charts.view-all"))
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
            {players &&
              Object.keys(players).map((playerId) => (
                <MenuItem key={playerId} value={playerId}>
                  {players[playerId]}
                </MenuItem>
              ))}
          </Select>
        </FormControl>

        <FormControl variant="standard">
          <Select value={month} onChange={(e) => setMonth(e.target.value)}>
            <MenuItem value={1}>January</MenuItem>
            <MenuItem value={2}>February</MenuItem>
            <MenuItem value={3}>March</MenuItem>
            <MenuItem value={4}>April</MenuItem>
            <MenuItem value={5}>May</MenuItem>
            <MenuItem value={6}>June</MenuItem>
            <MenuItem value={7}>July</MenuItem>
            <MenuItem value={8}>August</MenuItem>
            <MenuItem value={9}>September</MenuItem>
            <MenuItem value={10}>October</MenuItem>
            <MenuItem value={11}>November</MenuItem>
            <MenuItem value={12}>December</MenuItem>
          </Select>
        </FormControl>

        <FormControl variant="standard">
          <Select value={year} onChange={(e) => setYear(e.target.value)}>
            <MenuItem value={2022}>2022</MenuItem>
          </Select>
        </FormControl>

        <Button
          variant="outlined"
          sx={{ maxHeight: "35px", fontWeight: "bold" }}
          onClick={fetchBalances}
        >
          Search
        </Button>
      </div>

      {data && data.length ? (
        <AreaChart
          data={data}
          height={height - 150}
          width={width - 50}
          style={{ margin: 10, overflow: "hidden" }}
        >
          <defs>
            <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#8884d8" stopOpacity={0.3} />
              <stop offset="90%" stopColor="#8884d8" stopOpacity={0} />
            </linearGradient>
          </defs>
          <Area
            type="monotone"
            dataKey="balance"
            stroke="#8884d8"
            fillOpacity={1}
            fill="url(#colorBalance)"
          />

          <XAxis
            dataKey="date"
            tickFormatter={(value) => moment(value).format("Do")}
          />
          <YAxis type="number" tick={{ fontSize: 14 }} />
          <Tooltip
            content={(props) => {
              if (!props.payload || !props.payload.length) return null;
              return (
                <div>
                  <Moment
                    date={props.payload[0].payload.date}
                    format="MM/DD/YYYY hh:mm a"
                  />
                  <br />
                  <span style={{ color: "#28a745", fontWeight: "bold" }}>
                    ${props.payload[0].payload.balance.toLocaleString()}
                  </span>
                </div>
              );
            }}
          />
        </AreaChart>
      ) : null}

      {data && data.length === 0 && (
        <div className="no-results" style={{ marginTop: "15px" }}>
          No results found in that date range.
        </div>
      )}

      {!data && <div style={{ margin: 15 }}>Loading...</div>}
    </>
  );
};

export default PlayerCharts;
