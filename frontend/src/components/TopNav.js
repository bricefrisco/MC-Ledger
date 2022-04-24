import { Menu } from "@mui/material";
import React, { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";

const TopNav = ({ session }) => {
  const [currentPage, setCurrentPage] = useState();
  const [menuAnchor, setMenuAnchor] = useState();
  const location = useLocation();

  useEffect(() => {
    switch (location.pathname) {
      case "/balances":
        setCurrentPage("Balances");
        break;
      case "/transactions":
        setCurrentPage("Transactions");
        break;
      case "/player-charts":
        setCurrentPage("Player Charts");
        break;
      case "/server-chart":
        setCurrentPage("Server Chart");
        break;
      case "/login":
        setCurrentPage("Login");
        break;
      default:
        break;
    }
  }, [location]);

  const openMenu = (event) => {
    setMenuAnchor(event.currentTarget);
  };

  const closeMenu = () => {
    setMenuAnchor(null);
  };

  console.log("session:" + JSON.stringify(session));

  return (
    <div className="top-nav">
      <div className="logo-container">
        <img src="/logo.png" alt="hi" className="logo-img" />
        <span className="logo-text">Ledger</span>
        <span className="current-page">{currentPage}</span>
      </div>
      <div className="nav-item-container">
        {session && (
          <>
            {session.permissions.includes("ledger.balances.view") && (
              <Link to="/balances" className="nav-item">
                Balances
              </Link>
            )}

            {(session.permissions.includes("ledger.transactions.view-own") ||
              session.permissions.includes("ledger.transactions.view-all")) && (
              <Link to="/transactions" className="nav-item">
                Transactions
              </Link>
            )}

            {(session.permissions.includes("ledger.player-charts.view-own") ||
              session.permissions.includes(
                "ledger.player-charts.view-all"
              )) && (
              <Link to="/player-charts" className="nav-item">
                Player Charts
              </Link>
            )}

            {session.permissions.includes("ledger.server-chart.view") && (
              <Link to="/server-chart" className="nav-item">
                Server Chart
              </Link>
            )}
            <img
              src={`https://mc-heads.net/avatar/${session.playerId}/30`}
              alt="Player avatar"
              style={{
                marginLeft: 15,
                borderRadius: "5px",
                cursor: "pointer",
              }}
              onClick={openMenu}
            />
            <Menu
              anchorEl={menuAnchor}
              open={Boolean(menuAnchor)}
              onClose={closeMenu}
            >
              <div style={{ padding: 5 }}>
                <span>
                  Logged in as <strong>{session.playerName}</strong>
                </span>
                <br />
                <br />
                <span style={{ fontWeight: "bold" }}>Permissions:</span>
                <ul>
                  {session.permissions.map((permission) => (
                    <li key={permission}>{permission}</li>
                  ))}
                </ul>
              </div>
            </Menu>
          </>
        )}
      </div>
    </div>
  );
};

export default TopNav;
