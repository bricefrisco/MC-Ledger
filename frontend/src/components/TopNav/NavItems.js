import React, { useMemo } from "react";
import NavItem from "./NavItem";
import routes from "../../routes";

import styles from "./TopNav.module.css";

const NavItems = ({ permissions }) => {
  const permissedNavItems = useMemo(() => {
    return routes.filter((route) => !!route.permissions.find((permission) => permissions.includes(permission)));
  }, [permissions]);

  return (
    <div className={styles.navItemContainer}>
      {permissedNavItems.map((navItem) => (
        <NavItem path={navItem.path} text={navItem.text} />
      ))}
    </div>
  );
};

export default NavItems;
