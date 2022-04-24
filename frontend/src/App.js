import React, { useState } from "react";
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom";
import Login from "./components/Login";
import TopNav from "./components/TopNav";
import Transactions from "./components/Transactions";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import Balances from "./components/Balances";
import PlayerCharts from "./components/PlayerCharts";
import ServerChart from "./components/ServerChart";
import AuthProvider, { AuthContext } from "./contexts/AuthProvider";

const darkTheme = createTheme({
  palette: {
    mode: "dark",
  },
});

export const App = () => {
  return (
    <ThemeProvider theme={darkTheme}>
      <div className="App">
        <BrowserRouter>
          <AuthProvider>
            <AuthContext.Consumer>
              {({ session, fetchWithAuth }) => (
                <>
                  <TopNav session={session} />
                  <Switch>
                    <Route exact path="/balances">
                      <Balances
                        session={session}
                        fetchWithAuth={fetchWithAuth}
                      />
                    </Route>

                    <Route exact path="/transactions">
                      <Transactions
                        session={session}
                        fetchWithAuth={fetchWithAuth}
                      />
                    </Route>

                    <Route exact path="/player-charts">
                      <PlayerCharts
                        session={session}
                        fetchWithAuth={fetchWithAuth}
                      />
                    </Route>

                    <Route exact path="/server-chart">
                      <ServerChart
                        session={session}
                        fetchWithAuth={fetchWithAuth}
                      />
                    </Route>

                    <Route exact path="/login">
                      <Login />
                    </Route>

                    <Route path="/">
                      <Redirect to="/balances" />
                    </Route>
                  </Switch>
                </>
              )}
            </AuthContext.Consumer>
          </AuthProvider>
        </BrowserRouter>
      </div>
    </ThemeProvider>
  );
};

export default App;
