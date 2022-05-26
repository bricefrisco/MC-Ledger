import React, { useState, useEffect } from "react";
import { Button } from "@mui/material";
import { Area, AreaChart, Tooltip, XAxis, YAxis } from "recharts";
import Moment from "react-moment";
import moment from "moment";

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

const ServerChart = ({ session, fetchWithAuth }) => {
  const [chart, setChart] = useState({
    data: undefined,
    min: undefined,
    max: undefined,
  });
  const [historyType, setHistoryType] = useState("daily");

  const fetchServerBalance = () => {
    const url = new URL(process.env.REACT_APP_BACKEND_API + "/server");
    url.searchParams.append("historyType", historyType);

    fetchWithAuth(url).then((res) => {
      const data = [];
      const length = res.length;

      for (let i = 0; i < length; i++) {
        if (i === 0 || i % 3 === 0) {
          data.push({
            date: new Date(res[i]),
            numPlayersTracked: res[i + 1],
            balance: res[i + 2],
          });
        }
      }

      setChart({
        data: data,
        start: data[0].balance,
        current: data[data.length - 1].balance,
        diff: data[data.length - 1].balance - data[0].balance,
        min: getMinValue(data),
        max: getMaxValue(data),
      });
    });
  };

  useEffect(() => {
    if (!session || !session.permissions.includes("ledger.server-chart.view")) {
      return;
    }
    fetchServerBalance();
  }, [session, historyType]);

  if (!session || !session.permissions.includes("ledger.server-chart.view")) {
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

      {chart.data && (
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
              style={{ marginRight: 15, height: 64, width: 64 }}
              src={"https://minecraft-mp.com/images/favicon/284521.png?ts=1620908417"}
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
                Theatria
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

      {chart.data && chart.data.length ? (
        <AreaChart data={chart.data} height={244} width={684} style={{ margin: 10, overflow: "hidden" }}>
          <defs>
            <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#28a745" stopOpacity={0.3} />
              <stop offset="90%" stopColor="#28a745" stopOpacity={0} />
            </linearGradient>
          </defs>

          <Area type="monotone" dataKey="balance" stroke="#28a745" fillOpacity={1} fill="url(#colorBalance)" />

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
                  <Moment date={props.payload[0].payload.date} format="MM/DD/YYYY hh:mm a" />
                  <br />
                  <span>{props.payload[0].payload.numPlayersTracked} players tracked</span>
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

      {chart.data && chart.data.length === 0 && (
        <div className="no-results" style={{ marginTop: "15px" }}>
          No results found in that date range.
        </div>
      )}

      {!chart.data && <div style={{ margin: 15 }}>Loading...</div>}
    </>
  );
};

export default ServerChart;
