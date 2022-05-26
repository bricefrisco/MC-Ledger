import React from "react";
import ProfileMenu from "./ProfileMenu";
import NavItems from "./NavItems";
import Logo from "./Logo";
import PageName from "./PageName";

import styles from "./TopNav.module.css";

const TopNav = ({ session }) => {
  return (
    <div className={styles.topNav}>
      <div className={styles.topNavSection}>
        <Logo />
        <PageName />
      </div>

      {session && (
        <div className={styles.topNavSection}>
          <NavItems permissions={session.permissions} />
          <ProfileMenu playerName={session.playerName} playerId={session.playerId} permissions={session.permissions} />
        </div>
      )}
    </div>
  );
};

export default TopNav;
