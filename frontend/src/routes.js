const routes = [
  {
    path: "/sessions",
    permissions: [],
    text: "Login",
    redirectWhenLoggedIn: true,
  },
  {
    path: "/login",
    permissions: [],
    text: "Login",
    redirectWhenLoggedIn: true,
  },
  {
    path: "/balances",
    permissions: ["ledger.balances.view"],
    text: "Balances",
  },
  {
    path: "/transactions",
    permissions: ["ledger.transactions.view-own", "ledger.transactions.view-all"],
    text: "Transactions",
  },
  {
    path: "/player-charts",
    permissions: ["ledger.player-charts.view-own", "ledger.player-charts.view-all"],
    text: "Player Charts",
  },
  {
    path: "/server-chart",
    permissions: ["ledger.server-chart.view"],
    text: "Server Chart",
  },
];

export default routes;
