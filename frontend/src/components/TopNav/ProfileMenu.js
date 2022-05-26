import React, { useState } from "react";
import { Menu } from "@mui/material";

import styles from "./TopNav.module.css";

const ProfileMenu = ({ playerName, playerId, permissions }) => {
  const [menuAnchor, setMenuAnchor] = useState();

  const openMenu = (event) => {
    setMenuAnchor(event.currentTarget);
  };

  const closeMenu = () => {
    setMenuAnchor(null);
  };

  return (
    <>
      <img
        src={`https://mc-heads.net/avatar/${playerId}/30`}
        alt="Player avatar"
        className={styles.profileIcon}
        onClick={openMenu}
      />

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={closeMenu}>
        <div className={styles.profileMenu}>
          <span className={styles.loggedInAs}>
            Logged in as <strong>{playerName}</strong>
          </span>
          <strong>Permissions:</strong>
          <ul>
            {permissions.map((permission) => (
              <li key={permission}>{permission}</li>
            ))}
          </ul>
        </div>
      </Menu>
    </>
  );
};

export default ProfileMenu;
