import React from "react";
import { Link } from "react-router-dom";

import styles from "./TopNav.module.css";

const NavItem = ({ path, text }) => {
  return (
    <Link to={path} className={styles.navItem}>
      {text}
    </Link>
  );
};

export default NavItem;
