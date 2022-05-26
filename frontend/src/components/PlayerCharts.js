import React, { useState, useEffect } from "react";
import { Button, FormControl, MenuItem, Select } from "@mui/material";
import { Area, AreaChart, Tooltip, XAxis, YAxis, CartesianGrid } from "recharts";
import moment from "moment-timezone";

function getWindowDimensions() {
  const { innerWidth: width, innerHeight: height } = window;
  return {
    width,
    height,
  };
}

function useWindowDimensions() {
  const [windowDimensions, setWindowDimensions] = useState(getWindowDimensions());

  useEffect(() => {
    function handleResize() {
      setWindowDimensions(getWindowDimensions());
    }

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return windowDimensions;
}

const getValueAtTime = (time, data) => {
  let num = 0;
  for (let i = 0; i < data.length; i++) {
    if (time >= data[i].date) {
      num = i;
    }
  }

  return data[num].balance;
};

const dailyBucket = () => {
  const result = [];

  const now = Date.now();
  let temp = moment().tz("GMT").startOf("day");
  result.push(temp);

  while (temp <= now) {
    temp = moment(temp).add(10, "m").toDate();
    result.push(temp);
  }

  return result;
};

const weeklyBucket = () => {
  const result = [];

  const now = Date.now();
  let temp = moment().tz("GMT").subtract(7, "days");
  temp.set("hour", 0);
  temp.set("minute", 0);
  temp.set("second", 0);
  temp.set("millisecond", 0);

  while (temp <= now) {
    temp = moment(temp).add(1, "h").toDate();
    result.push(temp);
  }

  return result;
};

const monthlyBucket = () => {
  const result = [];

  const now = Date.now();
  let temp = moment().tz("GMT").subtract(1, "month");
  temp.set("hour", 0);
  temp.set("minute", 0);
  temp.set("second", 0);
  temp.set("millisecond", 0);

  while (temp <= now) {
    temp = moment(temp).add(4, "h").toDate();
    result.push(temp);
  }

  return result;
};

const allTimeBucket = (start) => {
  const result = [];

  const now = Date.now();
  let temp = moment(start);
  temp.set("hour", 0);
  temp.set("minute", 0);
  temp.set("second", 0);
  temp.set("millisecond", 0);

  while (temp <= now) {
    temp = moment(temp).add(1, "d").toDate();
    result.push(temp);
  }

  return result;
};

const transformDailyStats = (data) => {
  if (!data) return data;
  const result = [];

  const buckets = dailyBucket();
  for (const bucket of buckets) {
    result.push({
      date: bucket,
      balance: getValueAtTime(bucket, data),
    });
  }

  return result;
};

const transformWeeklyStats = (data) => {
  const result = [];

  const buckets = weeklyBucket();
  for (const bucket of buckets) {
    result.push({
      date: bucket,
      balance: getValueAtTime(bucket, data),
    });
  }

  return result;
};

const transformMonthlyStats = (data) => {
  const result = [];
  const buckets = monthlyBucket();
  for (const bucket of buckets) {
    result.push({
      date: bucket,
      balance: getValueAtTime(bucket, data),
    });
  }

  return result;
};

const transformAllTimeStats = (data) => {
  const result = [];
  const buckets = allTimeBucket(data[0].date);
  for (const bucket of buckets) {
    result.push({
      date: bucket,
      balance: getValueAtTime(bucket, data),
    });
  }

  return result;
};

const getMinValue = (data) => {
  let min = 99999999999999999;
  for (const d of data) {
    if (d.balance < min) {
      min = d.balance;
    }
  }
  return min;
};

const getMaxValue = (data) => {
  let max = 0;
  for (const d of data) {
    if (d.balance > max) {
      max = d.balance;
    }
  }
  return max;
};

const PlayerCharts = ({ session, fetchWithAuth }) => {
  const [chart, setChart] = useState({
    data: undefined,
    start: undefined,
    now: undefined,
    min: undefined,
    max: undefined,
  });
  const [players, setPlayers] = useState();
  const [selectedPlayer, setSelectedPlayer] = useState(session ? session.playerId : "");
  const [historyType, setHistoryType] = useState("daily");
  const { width, height } = useWindowDimensions();

  const fetchPlayerIds = () => {
    fetchWithAuth(`${process.env.REACT_APP_BACKEND_API}/player-ids`).then((res) => {
      const tempPlayers = {};

      for (const pid of res) {
        if (!session.permissions.includes("ledger.player-charts.view-all") && session.playerId !== pid.id) {
          continue;
        }

        tempPlayers[pid.id] = pid.name;
      }

      setPlayers(tempPlayers);
    });
  };

  const fetchBalances = (uuid = undefined) => {
    const url = new URL(process.env.REACT_APP_BACKEND_API + "/players");
    if (!uuid) {
      url.searchParams.append("uuid", selectedPlayer);
    } else {
      url.searchParams.append("uuid", uuid);
    }

    url.searchParams.append("historyType", historyType);

    fetchWithAuth(url).then((res) => {
      const data = [];
      const length = res.data.length;

      switch (historyType) {
        case "daily":
          data.push({
            date: dailyBucket()[0],
            balance: res.start,
          });
          break;
        case "weekly":
          data.push({
            date: weeklyBucket()[0],
            balance: res.start,
          });
          break;
        case "monthly":
          data.push({
            date: monthlyBucket()[0],
            balance: res.start,
          });
          break;
        case "all_time":
          data.push({
            date: allTimeBucket(res.data[0] - 2 * 24 * 60 * 60 * 1000)[0],
            balance: res.start,
          });
          break;
        default:
      }

      for (let i = 0; i < length; i++) {
        if (i === 0 || i % 2 === 0) {
          data.push({
            date: new Date(res.data[i]),
            balance: res.data[i + 1],
          });
        }
      }

      let ds = undefined;

      switch (historyType) {
        case "daily":
          ds = transformDailyStats(data);
          break;
        case "weekly":
          ds = transformWeeklyStats(data);
          break;
        case "monthly":
          ds = transformMonthlyStats(data);
          break;
        case "all_time":
          ds = transformAllTimeStats(data);
          break;
        default:
          break;
      }

      setChart({
        data: ds,
        current: res.current,
        start: res.start,
        diff: res.current - res.start,
        min: getMinValue(ds),
        max: getMaxValue(ds),
      });
    });
  };

  useEffect(() => {
    if (
      !session ||
      (!session.permissions.includes("ledger.player-charts.view-own") &&
        !session.permissions.includes("ledger.player-charts.view-all"))
    ) {
      return;
    }

    fetchPlayerIds();
    if (!selectedPlayer) {
      setSelectedPlayer(session.playerId);
      fetchBalances(session.playerId);
    } else {
      fetchBalances();
    }
  }, [session, historyType, selectedPlayer]);

  if (
    !session ||
    (!session.permissions.includes("ledger.player-charts.view-own") &&
      !session.permissions.includes("ledger.player-charts.view-all"))
  ) {
    return <div className="unauthorized">Unauthorized to view this page.</div>;
  }

  const getVerbiage = () => {
    if (historyType === "daily") return "today";
    if (historyType === "weekly") return "past 7 days";
    if (historyType === "monthly") return "past month";
    return "all time";
  };

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
          <Select value={selectedPlayer} onChange={(e) => setSelectedPlayer(e.target.value)}>
            {players &&
              Object.keys(players).map((playerId) => (
                <MenuItem key={playerId} value={playerId}>
                  {players[playerId]}
                </MenuItem>
              ))}
          </Select>
        </FormControl>

        <Button
          variant={historyType === "daily" ? "contained" : "outlined"}
          sx={{ maxHeight: "35px", fontWeight: "bold", marginRight: "10px" }}
          onClick={() => setHistoryType("daily")}
        >
          1D
        </Button>

        <Button
          variant={historyType === "weekly" ? "contained" : "outlined"}
          sx={{ maxHeight: "35px", fontWeight: "bold", marginRight: "10px" }}
          onClick={() => setHistoryType("weekly")}
        >
          1W
        </Button>

        <Button
          variant={historyType === "monthly" ? "contained" : "outlined"}
          sx={{ maxHeight: "35px", fontWeight: "bold", marginRight: "10px" }}
          onClick={() => setHistoryType("monthly")}
        >
          1M
        </Button>

        <Button
          variant={historyType === "all_time" ? "contained" : "outlined"}
          sx={{ maxHeight: "35px", fontWeight: "bold", marginRight: "10px" }}
          onClick={() => setHistoryType("all_time")}
        >
          Max
        </Button>
      </div>

      {selectedPlayer && players && chart.data && (
        <>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              margin: 15,
              marginLeft: 30,
            }}
          >
            <img
              style={{ marginRight: 15 }}
              src={`https://mc-heads.net/avatar/${selectedPlayer}/64`}
              alt="Player avatar"
            />
            <div>
              <span
                style={{
                  marginBottom: "5px",
                  fontSize: "1.3rem",
                  display: "block",
                }}
              >
                {players[selectedPlayer]}
              </span>
              <span>
                $
                {chart.current.toLocaleString(undefined, {
                  maximumFractionDigits: 2,
                })}
              </span>
            </div>
          </div>

          {chart.diff >= 0 ? (
            <div style={{ margin: 15, marginLeft: 30, color: "#28a745" }}>
              +
              {chart.diff.toLocaleString(undefined, {
                maximumFractionDigits: 2,
              })}{" "}
              (+
              {((chart.current / chart.start - 1) * 100).toLocaleString(undefined, {
                maximumFractionDigits: 2,
              })}
              %) <strong>↑</strong> {getVerbiage()}
            </div>
          ) : (
            <div style={{ margin: 15, marginLeft: 30, color: "#dc3545" }}>
              {chart.diff.toLocaleString(undefined, {
                maximumFractionDigits: 2,
              })}{" "}
              (
              {((chart.current / chart.start - 1) * 100).toLocaleString(undefined, {
                maximumFractionDigits: 2,
              })}
              %) <strong>↓</strong> {getVerbiage()}
            </div>
          )}
        </>
      )}

      {chart && chart.data ? (
        <AreaChart
          data={chart.data}
          height={244}
          width={684}
          style={{ margin: 10, marginLeft: 20, overflow: "hidden" }}
        >
          <defs>
            <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#8884d8" stopOpacity={0.12} />
              <stop offset="20%" stopColor="#8884d8" stopOpacity={0.09} />
              <stop offset="30%" stopColor="#8884d8" stopOpacity={0.04} />
              <stop offset="100%" stopColor="#8884d8" stopOpacity={0} />
            </linearGradient>
          </defs>
          <Area type="monotone" dataKey="balance" stroke="#8884d8" fillOpacity={1} fill="url(#colorBalance)" />
          <CartesianGrid vertical={false} stroke="rgba(255, 255, 255, 0.13)" strokeDasharray="1 1" />
          <XAxis
            dataKey="date"
            tickFormatter={(value) => {
              if (historyType === "daily") {
                return moment(value).format("ha");
              } else if (historyType === "all_time") {
                return moment(value).format("MM/DD/YY");
              } else {
                return moment(value).format("MM/DD");
              }
            }}
          />
          <YAxis
            type="number"
            tick={{ fontSize: 14 }}
            domain={[chart.min * 0.95, chart.max * 1.05]}
            axisLine={false}
            tickFormatter={(value) => {
              if (value >= 1000000) {
                return (
                  "$" +
                  (value / 1000000.0).toLocaleString(undefined, {
                    maximumFractionDigits: 1,
                  }) +
                  "M"
                );
              } else if (value >= 1000) {
                return (
                  "$" +
                  (value / 1000.0).toLocaleString(undefined, {
                    maximumFractionDigits: 1,
                  }) +
                  "K"
                );
              }
              return value;
            }}
          />
          <Tooltip
            content={(props) => {
              if (!props.payload || !props.payload.length) return null;
              return (
                <div>
                  <span>{moment(props.payload[0].payload.date).format("MM/DD/YYYY hh:mm a")}</span>
                  <br />
                  <span style={{ color: "#28a745", fontWeight: "bold" }}>
                    ${props.payload[0].payload.balance.toLocaleString(undefined, { maximumFractionDigits: 0 })}
                  </span>
                </div>
              );
            }}
          />
        </AreaChart>
      ) : null}

      {!chart && <div style={{ margin: 15 }}>Loading...</div>}
    </>
  );
};

export default PlayerCharts;
