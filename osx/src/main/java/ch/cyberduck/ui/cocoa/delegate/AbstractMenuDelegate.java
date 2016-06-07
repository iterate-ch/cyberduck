package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
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

import ch.cyberduck.binding.application.NSEvent;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.ui.cocoa.ProxyController;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

public abstract class AbstractMenuDelegate extends ProxyController implements NSMenu.Delegate, NSMenu.Validation {
    private static final Logger log = Logger.getLogger(AbstractMenuDelegate.class);

    /**
     * Menu needs revalidation
     */
    private boolean update = true;

    /**
     * Called to let you update a menu item before it is displayed. If your
     * numberOfItemsInMenu delegate method returns a positive value,
     * then your menuUpdateItemAtIndex method is called for each item in the menu.
     * You can then update the menu title, image, and so forth for the menu item.
     * Return true to continue the process. If you return false, your menuUpdateItemAtIndex
     * is not called again. In that case, it is your responsibility to trim any extra items from the menu.
     */
    @Override
    public boolean menu_updateItem_atIndex_shouldCancel(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        return this.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    /**
     * @param cancel Set to YES if, due to some user action, the menu no longer needs to be
     *               displayed before all the menu items have been updated. You can ignore this flag, return YES,
     *               and continue; or you can save your work (to save time the next time your delegate is called)
     *               and return NO to stop the updating.
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        if(log.isTraceEnabled()) {
            log.trace("menuUpdateItemAtIndex:" + index.intValue());
        }
        if(index.intValue() == this.numberOfItemsInMenu(menu).intValue() - 1) {
            // Collection fully populated
            this.setNeedsUpdate(false);
        }
        return !cancel;
    }

    public abstract Selector getDefaultAction();

    /**
     * Keyboard shortcut target
     *
     * @return Target for item actions with a keyboard shortcut.
     */
    protected ID getTarget() {
        return this.id();
    }

    /**
     * Shortcut for the first item in the menu.
     *
     * @return Lowercase shortcut key or null if no key equivalent for any menu item
     */
    protected String getKeyEquivalent() {
        return null;
    }

    /**
     * Shortcut for the first item in the menu.
     *
     * @return Modifier mask of the shortcut
     */
    protected int getModifierMask() {
        return NSEvent.NSCommandKeyMask;
    }

    /**
     * @return True if this menu has an item that has registerd a keyboard shortcut
     *         equivalent to the event characters.
     */
    public boolean menuHasKeyEquivalent_forEvent(NSMenu menu, NSEvent event) {
        if(log.isDebugEnabled()) {
            log.debug("menuHasKeyEquivalent_forEvent:" + menu);
        }
        if(StringUtils.isBlank(this.getKeyEquivalent())) {
            return false;
        }
        if((event.modifierFlags() & this.getModifierMask()) == this.getModifierMask()) {
            return event.charactersIgnoringModifiers().equalsIgnoreCase(this.getKeyEquivalent());
        }
        return false;
    }

    public ID menuKeyEquivalentTarget_forEvent(NSMenu menu, NSEvent event) {
        if(log.isDebugEnabled()) {
            log.debug("menuKeyEquivalentTarget_forEvent:" + menu);
        }
        return this.getTarget();
    }

    public Selector menuKeyEquivalentAction_forEvent(NSMenu menu, NSEvent event) {
        if(log.isDebugEnabled()) {
            log.debug("menuKeyEquivalentAction_forEvent:" + menu);
        }
        return this.getDefaultAction();
    }

    /**
     * Menu needs revalidation before being displayed the next time
     */
    protected void setNeedsUpdate(boolean u) {
        if(log.isDebugEnabled()) {
            log.trace("setNeedsUpdate:" + u);
        }
        update = u;
    }

    /**
     * @return True if the menu is populated and needs no update.
     */
    protected boolean isPopulated() {
        return !update;
    }

    /**
     * @return Separator menu item
     */
    protected NSMenuItem seperator() {
        return NSMenuItem.separatorItem();
    }

    /**
     * Validate menu item
     *
     * @return False if menu item should be disabled.
     */
    @Override
    public boolean validateMenuItem(NSMenuItem item) {
        return true;
    }

    protected void clearShortcut(NSMenuItem item) {
        this.setShortcut(item, StringUtils.EMPTY, 0);
    }

    protected void setShortcut(NSMenuItem item, String key) {
        this.setShortcut(item, key, 0);
    }

    protected void setShortcut(NSMenuItem item, String key, int modifier) {
        item.setKeyEquivalent(key);
        if(log.isDebugEnabled()) {
            if(!item.keyEquivalent().equals(key)) {
                log.error("Failed to attach key equivalent to menu item:" + key);
            }
        }
        item.setKeyEquivalentModifierMask(modifier);
    }
}
