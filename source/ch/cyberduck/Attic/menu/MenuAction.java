package ch.cyberduck.menu;

/*
 *  ch.cyberduck.menu.MenuAction.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
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
 *  dkocher@mac.com
 *
 *
 * Sample code.
 * Permission is given to use or modify this code in your own code.
 * Lee Ann Rucker
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
MenuAction is an AbstractAction designed to be shared amongst several JMenuBars;
	it controls JMenus which contain shared MenuItemActions
 */
public class MenuAction extends AbstractAction {
    Object[] items;
    int count = 0;

    /**
        * Constructs a new <code>MenuAction</code> which can be used to create a
     * <code>JMenu</code> with the supplied string as its text
     *
     *
     * @param s  the text for the menu label
     * @param items the contents of the menu
     */
    MenuAction(String s, Object[] items) {
        super(s);
        this.items = items;
    }

    /** Convenience method to create a JMenuBar from an array of MenuActions.
        Each JFrame needs its own JMenuBar

        * @param menus the MenuActions that will create the JMenuBar
        */
    /*
    public static JMenuBar makeMenuBar(MenuAction[] menus) {
        JMenuBar mb = new JMenuBar();
        for (int i = 0; i < menus.length; i++)
            mb.add(menus[i].createMenu());
        return mb;
    }
     */

    /** Create a <code>JMenu</code> that contains the provided items
        and knows how to handle adding and removing them
        */
    public JMenu makeMenu() {
        JActionMenu menu = new JActionMenu(this);
        addItems(menu);
        return menu;
    }

    /**
        * Add the items to this MenuAction's JMenu
     */
    protected void addItems(JMenu menu) {
        if (items != null) {
            synchronized (menu.getTreeLock()) {
                for (int i = 0; i < items.length; i++) {
                    if (items[i] instanceof MenuItemAction)
                        menu.add(((MenuItemAction)items[i]).createMenuItem());
                    else if (items[i] instanceof JPopupMenu.Separator)
                        menu.add(new JPopupMenu.Separator());
                    else if (items[i] instanceof JSeparator)
                        menu.add(new JSeparator());
                    // if the item is a JSeparator, make a new one, if we reuse
                    // the one from the array, it'll be removed from the last menu we made
                }
            }
        }
    }

    /** Does nothing, since JMenus are usually containers for JMenuItems
        */
    public void actionPerformed(ActionEvent e) {}

    /**
        Add an item to this MenuAction and all the JMenu instances that use it
     */
    public void add(MenuItemAction action) {
        int oldLength = items.length;
        Object[] newItems = new Object[oldLength + 1];
        System.arraycopy(items, 0, newItems, 0, oldLength);
        items = newItems;
        items[oldLength] = action;

        firePropertyChange("MenuAction.addAction", null, action);
    }

    /**
        Remove an item from this MenuAction and all the JMenu instances that use it
     */
    public void remove(MenuItemAction action) {
        // Find the index of this action
        // and remove based on index
        int index = -1;
        for (int i = 0; i < items.length; i++) {
            if (action.equals(items[i])) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            int oldLength = items.length;
            Object[] newItems = new Object[oldLength - 1];
            System.arraycopy(items, 0, newItems, 0, index);
            System.arraycopy(items, index + 1,
                             newItems, index,
                             oldLength - index - 1);
            items = newItems;
            firePropertyChange("MenuAction.remove", null, new Integer(index));
        }
    }

    /** Subclass of JMenu which handles adding and removing MenuActions
        */
    class JActionMenu extends JMenu {
        boolean fAddAll = false;
        JActionMenu(Action action) {
            super(action);
            action.addPropertyChangeListener(new ActionItemsChangedListener());
        }

        JMenu getMenu() {return this;}

        private class ActionItemsChangedListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent e) {
                String propertyName = e.getPropertyName();
                if (e.getPropertyName().equals("MenuAction.addAction")) {
                    getMenu().add(((MenuItemAction) e.getNewValue()).createMenuItem());
                }
                else if (e.getPropertyName().equals("MenuAction.remove")) {
                    getMenu().remove(((Integer) e.getNewValue()).intValue());
                }
            }
        }
    }
}
