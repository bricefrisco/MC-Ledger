import React from "react";

import styles from "./TopNav.module.css";

const Logo = () => {
  return (
    <div className={styles.logoContainer}>
      <img src="/logo.png" alt="hi" className={styles.logoImg} />
      <span className={styles.logoText}>Ledger</span>
    </div>
  );
};

export default Logo;
