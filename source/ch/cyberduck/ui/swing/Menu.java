package ch.cyberduck.ui.swing;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import javax.swing.*;

import ch.cyberduck.ui.swing.action.ActionMap;

/**
 * Singleton available application wide
 */
public class Menu extends JMenuBar {

    private static Menu instance = null;

    public static final int BOOKMARK_MENU = 0;
    public static final int SESSION_MENU = 1;
    public static final int FTP_MENU = 2;
    public static final int WINDOW_MENU = 3;
    public static final int HELP_MENU = 4;

    private static final int NO_MENUS = 5;
    
    private JMenu[] menus = new JMenu[NO_MENUS];
//    private MenuAction[] menus = new MenuAction[NO_MENUS];
  
    public static Menu instance() {
        if(instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    /*
    public static JMenuBar makeMenuBar(MenuAction[] menuActions) {
        JMenuBar menubar = new JMenuBar();
        for (int i = 0; i < menuActions.length; i++)
            menubar.add(menuActions[i].makeMenu());
        return menubar;        
    }
     */
    
    private Menu() {
        java.util.Map actions = ActionMap.instance();
        JMenuItem[] ftpMenuItems = {
            /*
            menuItemBuilder((Action)actions.get("New Connection")),
            null,
            menuItemBuilder((Action)actions.get("Refresh")),
            //null,
            menuItemBuilder((Action)actions.get("New Folder")),
            menuItemBuilder((Action)actions.get("Rename")),
            menuItemBuilder((Action)actions.get("Delete")),
            menuItemBuilder((Action)actions.get("Set Permissions")),
            null,
            menuItemBuilder((Action)actions.get("Disconnect"))
             */
        };
        JMenuItem[] connectionMenuItems = {
/*            menuItemBuilder((Action)actions.get("Connect All")),
            menuItemBuilder((Action)actions.get("Connect Selected")),
            null,
            menuItemBuilder((Action)actions.get("Stop All")),
            menuItemBuilder((Action)actions.get("Stop Selected")),
	    */
        };
        JMenuItem[] bookmarkMenuItems = {
//            menuItemBuilder((Action)actions.get("New Bookmark")),
//            null,
//            menuItemBuilder((Action)actions.get("New Bookmark File...")),
//            menuItemBuilder((Action)actions.get("Delete Bookmark File...")),
//            menuItemBuilder((Action)actions.get("Rename Bookmark File...")),
//            null,
//            menuItemBuilder((Action)actions.get("Open Dialog")),
//            null,
//            menuItemBuilder((Action)actions.get("Delete All")),
//            menuItemBuilder((Action)actions.get("Delete Selected")),
//            menuItemBuilder((Action)actions.get("Delete Completed")),
//            null,
//            menuItemBuilder((Action)actions.get("Import...")),
//            menuItemBuilder((Action)actions.get("Export..."))
        };
        JMenuItem[] windowMenuItems = {
            menuItemBuilder((Action)actions.get("Show Transcript")),
            menuItemBuilder((Action)actions.get("Show Log")),
            null,
            menuItemBuilder((Action)actions.get("Preferences")),
        };
        JMenuItem[] helpMenuItems = {
            menuItemBuilder((Action)actions.get("About")),
            null,
            menuItemBuilder((Action)actions.get("Help")),
            menuItemBuilder((Action)actions.get("Website"))
        };
         
        menus[BOOKMARK_MENU]= menuBuilder("Bookmarks", bookmarkMenuItems);
        menus[SESSION_MENU] = menuBuilder("Session", connectionMenuItems);
        menus[FTP_MENU] = menuBuilder("Ftp", ftpMenuItems);
        menus[WINDOW_MENU] = menuBuilder("Window", windowMenuItems);
        menus[HELP_MENU] = menuBuilder("Help", helpMenuItems);

        this.add(menus[BOOKMARK_MENU]);
        this.add(menus[SESSION_MENU]);
        this.add(menus[FTP_MENU]);
        this.add(menus[WINDOW_MENU]);
        this.add(menus[HELP_MENU]);
    }

    public JMenuItem menuItemBuilder(Action action) {
        if(action == null)
            return null;
        JMenuItem item = new JMenuItem(action);
        item.setText((String)action.getValue(Action.NAME));
        item.setAccelerator((KeyStroke)action.getValue(Action.ACCELERATOR_KEY));
        return item;
    }

    public JMenu menuBuilder(String name, JMenuItem[] items) {
        JMenu menu = new JMenu(name);
        for (int i = 0; i < items.length; i++) {
            if(items[i] == null)
                menu.addSeparator();
            else
                menu.add(items[i]);
        }
        return menu;
    }
}
