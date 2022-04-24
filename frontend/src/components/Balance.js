import React from "react";

const Balance = ({ idx, playerId, playerName, balance }) => {
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
        <span>{playerName}</span>
      </td>

      <td>${balance.toLocaleString()}</td>
    </tr>
  );
};

export default Balance;
