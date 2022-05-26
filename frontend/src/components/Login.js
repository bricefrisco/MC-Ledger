import React, { useEffect } from "react";

const Login = () => {
  return (
    <div style={{ padding: 15 }}>
      <span
        style={{ display: "block", fontWeight: "600", marginBottom: "5px" }}
      >
        Your login link or session has expired or is invalid.
      </span>
      <span style={{ lineHeight: "1.5rem" }}>
        They can only be used for a single session and expire quickly after not
        being used. <br />
        Don't worry, it's simple to generate another one!
      </span>
      <ol>
        <li style={{ marginBottom: 10 }}>
          In-game, type{" "}
          <span
            style={{
              backgroundColor: "rgb(34,43,54)",
              padding: "5px 10px",
              borderRadius: 10,
              fontWeight: 500,
            }}
          >
            /ledger
          </span>
        </li>
        <li>Click on the link provided</li>
      </ol>
    </div>
  );
};

export default Login;
