import React, { useState, useEffect } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import Balance from "./Balance";

const Balances = ({ session, fetchWithAuth }) => {
  const [balances, setBalances] = useState();
  const [numBalances, setNumBalances] = useState();
  const [page, setPage] = useState(0);

  const fetchPlayerBalances = () => {
    const url = new URL(process.env.REACT_APP_BACKEND_API + "/balances");
    url.searchParams.append("page", page);

    fetchWithAuth(url).then((res) => {
      setNumBalances(res.totalCount);
      if (!page) {
        setBalances(res.balances);
      } else {
        setBalances([...balances, res.balances]);
      }
    });
  };

  useEffect(() => {
    document.title = "Ledger | Balances";
  }, []);

  useEffect(() => {
    if (!session || !session.permissions.includes("ledger.balances.view")) {
      return;
    }
    fetchPlayerBalances();
  }, [page]);

  if (!session || !session.permissions.includes("ledger.balances.view")) {
    return <div className="unauthorized">Unauthorized to view this page.</div>;
  }

  return (
    <>
      {balances && (
        <InfiniteScroll
          dataLength={balances.length}
          next={() => setPage((page) => page + 1)}
          hasMore={balances.length !== numBalances}
        >
          <table border={0} cellSpacing={0} style={{ width: "100%" }}>
            <thead>
              <tr>
                <th align="center">#</th>
                <th align="left">Player</th>
                <th align="left">Balance</th>
              </tr>
            </thead>

            <tbody>
              {balances.map((balance, idx) => (
                <Balance
                  key={idx}
                  idx={idx}
                  playerId={balance.playerId}
                  playerName={balance.playerName}
                  balance={balance.balance}
                />
              ))}
            </tbody>
          </table>
        </InfiniteScroll>
      )}

      {balances && !balances.length && (
        <span className="no-results">No results found.</span>
      )}
    </>
  );
};

export default Balances;
