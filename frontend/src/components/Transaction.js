import React from "react";
import Moment from "react-moment";

const Transaction = ({
  idx,
  playerId,
  playerName,
  timestamp,
  cause,
  amount,
  balance,
}) => {
  return (
    <tr>
      <td style={{ fontWeight: "bold" }} align="center">
        {idx + 1}.
      </td>

      <td style={{ display: "flex", alignItems: "center" }}>
        <img
          src={`https://mc-heads.net/avatar/${playerId}/35`}
          alt="Player avatar"
          style={{ marginRight: 5 }}
        />
        <div style={{ marginRight: 20 }}>
          <span style={{ fontWeight: "bold" }}>{playerName}</span>
          <br />
          <Moment unix format="MM/DD/YYYY hh:mm a">
            {timestamp / 1000}
          </Moment>
        </div>
      </td>

      <td>{cause}</td>

      <td>
        {amount >= 0 ? (
          <span style={{ color: "#28a745", fontWeight: "bold" }}>
            +${amount.toLocaleString()}
          </span>
        ) : (
          <span style={{ color: "#dc3545", fontWeight: "bold" }}>
            -${Math.abs(amount).toLocaleString()}
          </span>
        )}
      </td>

      <td>${balance.toLocaleString()}</td>
    </tr>
  );
};

export default Transaction;
