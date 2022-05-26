import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import routes from "../../routes";

import styles from "./TopNav.module.css";

const PageName = () => {
  const [currentPage, setCurrentPage] = useState();
  const location = useLocation();

  useEffect(() => {
    const pageName = routes.find((route) => route.path === location.pathname)?.text;

    setCurrentPage(pageName);
    document.title = "Ledger | " + pageName;
  }, [location]);

  return <span className={styles.currentPage}>{currentPage}</span>;
};

export default PageName;
