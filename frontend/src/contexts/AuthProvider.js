import React, { useState, useEffect } from "react";
import { useHistory, useLocation } from "react-router-dom";

export const AuthContext = React.createContext({});
const UNAUTHORIZED_ROUTES = ["/sessions", "/unauthorized"];

const AuthProvider = ({ children }) => {
  const [session, setSession] = useState();
  const history = useHistory();
  const location = useLocation();

  const authorize = (id) => {
    fetch(`${process.env.REACT_APP_BACKEND_API}/sessions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ id }),
    })
      .then((res) => {
        if (!res.ok) {
          throw Error("Something bad happened");
        }
        return res.json();
      })
      .then((res) => {
        sessionStorage.setItem("session", JSON.stringify(res));
        setSession(() => {
          history.push("/balances");
          return res;
        });
      });
  };

  const fetchWithAuth = (url) => {
    return fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: session.id,
      },
    }).then((res) => {
      if (res.status === 401) {
        setSession(null);
        history.push("/login");
      }
      return res.json();
    });
  };

  const isOnUnauthorizedRoute = (pathname) => {
    for (const route of UNAUTHORIZED_ROUTES) {
      if (pathname.startsWith(route)) return false;
    }
    return true;
  };

  useEffect(() => {
    if (!session) {
      const sessionStrg = sessionStorage.getItem("session");
      if (sessionStrg) {
        setSession(JSON.parse(sessionStrg));
      } else if (isOnUnauthorizedRoute(location.pathname)) {
        history.push("/login");
      } else if (location.pathname.startsWith("/sessions")) {
        const id = location.pathname.replace("/sessions/", "");
        authorize(id);
      }
    }
  }, [location.pathname, session]);

  return (
    <AuthContext.Provider value={{ session, fetchWithAuth }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
