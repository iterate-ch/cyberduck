package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostFilter;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.controller.MainController;
import ch.cyberduck.ui.cocoa.view.BookmarkCell;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;

public class BookmarkMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = LogManager.getLogger(BookmarkMenuDelegate.class);

    private static final int BOOKMARKS_INDEX = 8;

    private final Preferences preferences
        = PreferencesFactory.get();

    private final AbstractHostCollection bookmarks;
    private final int index;

    private final MenuCallback callback;

    private final NSMenu historyMenu = NSMenu.menu();
    @Delegate
    private final HistoryMenuDelegate historyMenuDelegate;

    private final NSMenu rendezvousMenu = NSMenu.menu();
    @Delegate
    private final RendezvousMenuDelegate rendezvousMenuDelegate;

    public BookmarkMenuDelegate() {
        this(new HistoryMenuDelegate(), new RendezvousMenuDelegate());
    }

    public BookmarkMenuDelegate(final HistoryMenuDelegate history, final RendezvousMenuDelegate rendezvous) {
        this(new MenuCallback() {
            @Override
            public void selected(final NSMenuItem sender) {
                MainController.newDocument().mount(BookmarkCollection.defaultCollection().lookup(sender.representedObject()));
            }
        }, history, rendezvous);
    }

    public BookmarkMenuDelegate(final MenuCallback callback) {
        this(callback, new HistoryMenuDelegate(), new RendezvousMenuDelegate());
    }

    public BookmarkMenuDelegate(final MenuCallback callback, final HistoryMenuDelegate history, final RendezvousMenuDelegate rendezvous) {
        this(BookmarkCollection.defaultCollection(), BOOKMARKS_INDEX, callback, history, rendezvous);
    }

    public BookmarkMenuDelegate(final AbstractHostCollection bookmarks, final int index,
                                final MenuCallback callback, final HistoryMenuDelegate history, final RendezvousMenuDelegate rendezvous) {
        super(bookmarks);
        this.bookmarks = bookmarks;
        this.index = index;
        this.historyMenuDelegate = history;
        this.rendezvousMenuDelegate = rendezvous;
        this.historyMenu.setDelegate(historyMenuDelegate.id());
        this.rendezvousMenu.setDelegate(rendezvousMenuDelegate.id());
        this.callback = callback;
    }

    @Override
    public NSInteger numberOfItemsInMenu(final NSMenu menu) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean menuUpdateItemAtIndex(final NSMenu menu, final NSMenuItem item, final NSInteger index, final boolean cancel) {
        throw new UnsupportedOperationException();
    }

    public void menuNeedsUpdate(final NSMenu menu) {
        if(!this.isPopulated()) {
            if(log.isTraceEnabled()) {
                log.trace(String.format("Build menu %s", menu));
            }
            for(int i = menu.numberOfItems().intValue() - 1; i >= BOOKMARKS_INDEX; i--) {
                menu.removeItemAtIndex(new NSInteger(i));
            }
            {
                final NSMenuItem item = NSMenuItem.itemWithTitle(LocaleFactory.get().localize("History", "Localizable"), null, StringUtils.EMPTY);
                item.setEnabled(true);
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("history.tiff", 16));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("historyMenuClicked:"));
                historyMenu.setSupermenu(null);
                item.setSubmenu(historyMenu);
                menu.addItem(item);
            }
            {
                final NSMenuItem item = NSMenuItem.itemWithTitle(LocaleFactory.get().localize("Bonjour", "Main"), null, StringUtils.EMPTY);
                item.setEnabled(true);
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("rendezvous.tiff", 16));
                rendezvousMenu.setSupermenu(null);
                item.setSubmenu(rendezvousMenu);
                menu.addItem(item);
            }
            menu.addItem(NSMenuItem.separatorItem());
            bookmarks.groups(HostFilter.NONE).forEach((label, bookmarks) -> {
                final NSMenu submenu;
                if(StringUtils.isNotBlank(label)) {
                    final NSMenuItem group = NSMenuItem.itemWithTitle(label, null, StringUtils.EMPTY);
                    final NSMutableAttributedString title = NSMutableAttributedString.create(label);
                    title.appendAttributedString(NSAttributedString.attributedStringWithAttributes(
                        String.format("\n%s", MessageFormat.format(LocaleFactory.localizedString("{0} Bookmarks", "Localizable"),
                            bookmarks.size())), BundleController.MENU_HELP_FONT_ATTRIBUTES));
                    group.setAttributedTitle(title);
                    switch(preferences.getInteger("bookmark.menu.icon.size")) {
                        default:
                            group.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSFolder", CollectionMenuDelegate.SMALL_ICON_SIZE));
                            break;
                        case BookmarkCell.MEDIUM_BOOKMARK_SIZE:
                            group.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSFolder", CollectionMenuDelegate.MEDIUM_ICON_SIZE));
                            break;
                        case BookmarkCell.LARGE_BOOKMARK_SIZE:
                            group.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSFolder", CollectionMenuDelegate.LARGE_ICON_SIZE));
                            break;
                    }
                    submenu = NSMenu.menu();
                    group.setSubmenu(submenu);
                    menu.addItem(group);
                }
                else {
                    submenu = menu;
                }
                for(Host h : bookmarks) {
                    submenu.addItem(build(h));
                }
            });
        }
    }

    private NSMenuItem build(final Host h) {
        final NSMenuItem item = NSMenuItem.itemWithTitle(BookmarkNameProvider.toString(h), this.getDefaultAction(), StringUtils.EMPTY);
        final NSMutableAttributedString title = NSMutableAttributedString.create(BookmarkNameProvider.toString(h));
        if(preferences.getInteger("bookmark.menu.icon.size") >= BookmarkCell.MEDIUM_BOOKMARK_SIZE) {
            title.appendAttributedString(NSAttributedString.attributedStringWithAttributes(
                String.format("\n%s", h.getHostname()), BundleController.MENU_HELP_FONT_ATTRIBUTES));
        }
        if(preferences.getInteger("bookmark.menu.icon.size") >= BookmarkCell.LARGE_BOOKMARK_SIZE) {
            title.appendAttributedString(NSAttributedString.attributedStringWithAttributes(
                String.format("\n%s", StringUtils.isNotBlank(h.getCredentials().getUsername()) ? h.getCredentials().getUsername() : StringUtils.EMPTY), BundleController.MENU_HELP_FONT_ATTRIBUTES));
        }
        item.setAttributedTitle(title);
        item.setTitle(BookmarkNameProvider.toString(h));
        switch(preferences.getInteger("bookmark.menu.icon.size")) {
            default:
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), CollectionMenuDelegate.SMALL_ICON_SIZE));
                break;
            case BookmarkCell.MEDIUM_BOOKMARK_SIZE:
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), CollectionMenuDelegate.MEDIUM_ICON_SIZE));
                break;
            case BookmarkCell.LARGE_BOOKMARK_SIZE:
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), CollectionMenuDelegate.LARGE_ICON_SIZE));
                break;
        }
        item.setTarget(this.id());
        item.setAction(this.getDefaultAction());
        item.setRepresentedObject(h.getUuid());
        return item;
    }

    @Action
    public void bookmarkMenuItemClicked(final NSMenuItem sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Menu item clicked %s", sender));
        }
        callback.selected(sender);
    }

    @Action
    public void historyMenuClicked(NSMenuItem sender) {
        ApplicationLauncherFactory.get().open(HistoryCollection.defaultCollection().getFolder());
    }

    @Override
    public Selector getDefaultAction() {
        return Foundation.selector("bookmarkMenuItemClicked:");
    }
}
