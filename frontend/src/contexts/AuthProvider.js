import React, { useState, useEffect } from "react";
import { useHistory, useLocation } from "react-router-dom";
import routes from "../routes";

export const AuthContext = React.createContext({});

const AuthProvider = ({ children }) => {
  const [session, setSession] = useState();
  const history = useHistory();
  const location = useLocation();

  const authorize = async (id) => {
    const url = process.env.REACT_APP_BACKEND_API + "/sessions";
    const res = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ id }),
    });

    if (res.status !== 200) {
      history.push("/login");
      return;
    }

    const data = await res.json();
    sessionStorage.setItem("session", JSON.stringify(data));
    setSession(data);
  };

  const fetchWithAuth = async (url) => {
    if (!session) {
      throw new Error("No session present.");
    }

    const res = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: session.id,
      },
    });

    if (res.status === 401) {
      setSession(undefined);
      sessionStorage.removeItem("session");
      history.push("/login");
    }

    return await res.json();
  };

  const sessionNeedsRestore = () => {
    return !session && sessionStorage.getItem("session");
  };

  const isOnUnauthorizedRoute = () => {
    const route = routes.find((r) => r.path === location.pathname);
    if (!route) return true; // if the route user is on is not specified
    if (route.permissions.length === 0) return false; // if the route requires no permissions
    if (!session) return true; // if the route requires permissions but user is not yet signed in

    for (const permission of route.permissions) {
      if (session.permissions.includes(permission)) {
        return false;
      }
    }

    return true;
  };

  useEffect(() => {
    if (sessionNeedsRestore()) {
      setSession(JSON.parse(sessionStorage.getItem("session")));
      return;
    }

    if (location.pathname.startsWith("/sessions")) {
      const id = location.pathname.replace("/sessions/", "");
      authorize(id);
      return;
    }

    if (isOnUnauthorizedRoute()) {
      history.push("/login");
      return;
    }
  }, [location.pathname]);

  if (sessionNeedsRestore()) {
    return null;
  }

  return <AuthContext.Provider value={{ session, fetchWithAuth }}>{children}</AuthContext.Provider>;
};

export default AuthProvider;
