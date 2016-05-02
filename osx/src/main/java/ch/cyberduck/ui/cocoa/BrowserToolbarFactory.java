package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSButtonCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSegmentedControl;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.DefaultCharsetProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.quicklook.QuickLook;
import ch.cyberduck.ui.cocoa.quicklook.QuickLookFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.HashMap;
import java.util.Map;

import static ch.cyberduck.ui.cocoa.BrowserToolbarFactory.BrowserToolbarItem.*;

public class BrowserToolbarFactory extends AbstractToolbarFactory implements ToolbarFactory {

    private final ApplicationFinder applicationFinder = ApplicationFinderFactory.get();

    public enum BrowserToolbarItem {
        browserview {
            @Override
            public String label() {
                return LocaleFactory.localizedString("View");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Switch Browser View");
            }

            @Override
            public Selector action() {
                return Foundation.selector("browserSwitchMenuClicked:");
            }
        },
        transfers {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Show Transfers window");
            }

            @Override
            public Selector action() {
                return Foundation.selector("showTransferQueueClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("filetransfer.pdf");
            }
        },
        connect {
            @Override
            public String label() {
                return LocaleFactory.localizedString("New Connection");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Connect to server");
            }

            @Override
            public Selector action() {
                return Foundation.selector("connectButtonClicked:");
            }
        },
        quickconnect {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Quick Connect");
            }

            @Override
            public Selector action() {
                return null;
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Connect to server");
            }
        },
        tools {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Action");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("NSActionTemplate");
            }

            @Override
            public Selector action() {
                return null;
            }
        },
        reload {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Refresh");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Refresh directory listing");
            }

            @Override
            public Selector action() {
                return Foundation.selector("reloadButtonClicked:");
            }
        },
        encoding {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Character Encoding");
            }

            @Override
            public Selector action() {
                return Foundation.selector("encodingMenuClicked:");
            }
        },
        synchronize {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Synchronize files");
            }

            @Override
            public Selector action() {
                return Foundation.selector("syncButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("sync.pdf");
            }
        },
        download {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Download file");
            }

            @Override
            public Selector action() {
                return Foundation.selector("downloadButtonClicked:");
            }

        },
        upload {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Upload local file to the remote host");
            }

            @Override
            public Selector action() {
                return Foundation.selector("uploadButtonClicked:");
            }
        },
        edit {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Edit file in external editor");
            }

            @Override
            public Selector action() {
                return Foundation.selector("editButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("editor.pdf");
            }
        },
        delete {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Delete file");
            }

            @Override
            public Selector action() {
                return Foundation.selector("deleteFileButtonClicked:");
            }
        },
        newfolder {
            @Override
            public String label() {
                return LocaleFactory.localizedString("New Folder");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Create New Folder");
            }

            @Override
            public Selector action() {
                return Foundation.selector("createFolderButtonClicked:");
            }
        },
        addbookmark {
            @Override
            public String label() {
                return LocaleFactory.localizedString("New Bookmark");
            }

            @Override
            public Selector action() {
                return Foundation.selector("addBookmarkButtonClicked:");
            }
        },
        info {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Get Info");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Show file attributes");
            }

            @Override
            public Selector action() {
                return Foundation.selector("infoButtonClicked:");
            }
        },
        webbrowser {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Open");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Open in Web Browser");
            }

            @Override
            public Selector action() {
                return Foundation.selector("openBrowserButtonClicked:");
            }
        },
        stop {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Cancel current operation in progress");
            }

            @Override
            public Selector action() {
                return Foundation.selector("disconnectButtonClicked:");
            }
        },
        disconnect {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Disconnect from server");
            }

            @Override
            public Selector action() {
                return Foundation.selector("disconnectButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("eject.pdf");
            }
        },
        terminal {
            @Override
            public Selector action() {
                return Foundation.selector("openTerminalButtonClicked:");
            }
        },
        unarchive {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Unarchive", "Archive");
            }

            @Override
            public Selector action() {
                return Foundation.selector("unarchiveButtonClicked:");
            }
        },
        archive {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Archive", "Archive");
            }

            @Override
            public Selector action() {
                return Foundation.selector("archiveButtonClicked:");
            }
        },
        quicklook {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Quick Look");
            }

            @Override
            public Selector action() {
                return Foundation.selector("quicklookButtonClicked:");
            }
        },
        log {
            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Toggle Log Drawer");
            }

            @Override
            public Selector action() {
                return Foundation.selector("toggleLogDrawer:");
            }
        };

        public String label() {
            return LocaleFactory.localizedString(StringUtils.capitalize(this.name()));
        }

        public String tooltip() {
            // No tooltip by default
            return null;
        }

        public abstract Selector action();

        public NSImage image() {
            return IconCacheFactory.<NSImage>get().iconNamed(String.format("%s.pdf", this.name()));
        }
    }

    private Preferences preferences
            = PreferencesFactory.get();

    private final QuickLook quicklook = QuickLookFactory.get();

    private BrowserController controller;

    /**
     * Keep reference to weak toolbar items. A toolbar may ask again for a kind of toolbar
     * item already supplied to it, in which case this method may return the same toolbar
     * item it returned before
     */
    private Map<String, NSToolbarItem> toolbarItems
            = new HashMap<String, NSToolbarItem>();


    public BrowserToolbarFactory(final BrowserController controller) {
        this.controller = controller;
    }

    @Override
    public NSToolbarItem create(final String identifier) {
        if(!toolbarItems.containsKey(identifier)) {
            toolbarItems.put(identifier, CDToolbarItem.itemWithIdentifier(identifier));
        }
        final NSToolbarItem item = toolbarItems.get(identifier);
        try {
            final BrowserToolbarItem type = BrowserToolbarItem.valueOf(identifier);
            switch(type) {
                case browserview: {
                    item.setLabel(browserview.label());
                    item.setPaletteLabel(browserview.label());
                    item.setToolTip(browserview.tooltip());
                    final NSSegmentedControl control = controller.getBrowserSwitchView();
                    control.setSegmentStyle(NSSegmentedControl.NSSegmentStyleCapsule);
                    item.setView(control);
                    // Add a menu representation for text mode of toolbar
                    NSMenuItem viewMenu = NSMenuItem.itemWithTitle(browserview.label(), null, StringUtils.EMPTY);
                    NSMenu viewSubmenu = NSMenu.menu();
                    viewSubmenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("List"),
                            browserview.action(), StringUtils.EMPTY);
                    viewSubmenu.itemWithTitle(LocaleFactory.localizedString("List")).setTag(0);
                    viewSubmenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("Outline"),
                            browserview.action(), StringUtils.EMPTY);
                    viewSubmenu.itemWithTitle(LocaleFactory.localizedString("Outline")).setTag(1);
                    viewMenu.setSubmenu(viewSubmenu);
                    item.setMenuFormRepresentation(viewMenu);
                    return item;
                }
                case tools: {
                    item.setLabel(tools.label());
                    item.setPaletteLabel(tools.label());
                    final NSInteger index = new NSInteger(0);
                    final NSPopUpButton button = controller.getActionPopupButton();
                    button.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                    button.insertItemWithTitle_atIndex(StringUtils.EMPTY, index);
                    button.itemAtIndex(index).setImage(tools.image());
                    item.setView(button);
                    // Add a menu representation for text mode of toolbar
                    NSMenuItem toolMenu = NSMenuItem.itemWithTitle(tools.label(), null, StringUtils.EMPTY);
                    NSMenu toolSubmenu = NSMenu.menu();
                    for(int i = 1; i < button.menu().numberOfItems().intValue(); i++) {
                        NSMenuItem template = button.menu().itemAtIndex(new NSInteger(i));
                        toolSubmenu.addItem(NSMenuItem.itemWithTitle(template.title(),
                                template.action(),
                                template.keyEquivalent()));
                    }
                    toolMenu.setSubmenu(toolSubmenu);
                    item.setMenuFormRepresentation(toolMenu);
                    return item;
                }
                case quickconnect:
                    item.setLabel(quickconnect.label());
                    item.setPaletteLabel(quickconnect.label());
                    item.setToolTip(quickconnect.tooltip());
                    item.setView(controller.getQuickConnectPopup());
                    return item;
                case encoding:
                    item.setLabel(encoding.label());
                    item.setPaletteLabel(encoding.label());
                    item.setToolTip(encoding.tooltip());
                    item.setView(controller.getEncodingPopup());
                    // Add a menu representation for text mode of toolbar
                    NSMenuItem encodingMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString(encoding.label()),
                            encoding.action(), StringUtils.EMPTY);
                    String[] charsets = new DefaultCharsetProvider().availableCharsets();
                    NSMenu charsetMenu = NSMenu.menu();
                    for(String charset : charsets) {
                        charsetMenu.addItemWithTitle_action_keyEquivalent(charset, encoding.action(), StringUtils.EMPTY);
                    }
                    encodingMenu.setSubmenu(charsetMenu);
                    item.setMenuFormRepresentation(encodingMenu);
                    item.setMinSize(controller.getEncodingPopup().frame().size);
                    item.setMaxSize(controller.getEncodingPopup().frame().size);
                    return item;
                case edit: {
                    item.setLabel(edit.label());
                    item.setPaletteLabel(edit.label());
                    item.setToolTip(edit.tooltip());
                    item.setTarget(controller.id());
                    item.setAction(edit.action());
                    // Add a menu representation for text mode of toolbar
                    NSMenuItem toolbarMenu = NSMenuItem.itemWithTitle(edit.label(), edit.action(), StringUtils.EMPTY);
                    NSMenu editMenu = NSMenu.menu();
                    editMenu.setAutoenablesItems(true);
                    editMenu.setDelegate(controller.getEditMenuDelegate().id());
                    toolbarMenu.setSubmenu(editMenu);
                    final NSButton button = NSButton.buttonWithFrame(new NSRect(0, 0));
                    button.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                    button.setImage(edit.image());
                    button.sizeToFit();
                    button.setTarget(controller.id());
                    button.setAction(edit.action());
                    item.setView(button);
                    item.setMenuFormRepresentation(toolbarMenu);
                    return item;
                }
                case terminal: {
                    final Application application = applicationFinder.getDescription(preferences.getProperty("terminal.bundle.identifier"));
                    item.setLabel(application.getName());
                    item.setPaletteLabel(application.getName());
                    item.setTarget(controller.id());
                    item.setAction(terminal.action());
                    final NSButton button = NSButton.buttonWithFrame(new NSRect(0, 0));
                    button.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                    button.setImage(terminal.image());
                    button.sizeToFit();
                    button.setTarget(controller.id());
                    button.setAction(terminal.action());
                    item.setView(button);
                    return item;
                }
                case quicklook: {
                    item.setLabel(BrowserToolbarItem.quicklook.label());
                    item.setPaletteLabel(BrowserToolbarItem.quicklook.label());
                    if(quicklook.isAvailable()) {
                        final NSButton button = NSButton.buttonWithFrame(new NSRect(0, 0));
                        button.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                        button.setImage(BrowserToolbarItem.quicklook.image());
                        button.sizeToFit();
                        button.setTarget(controller.id());
                        button.setAction(BrowserToolbarItem.quicklook.action());
                        item.setView(button);
                    }
                    else {
                        item.setEnabled(false);
                        item.setImage(IconCacheFactory.<NSImage>get().iconNamed("notfound.tiff", 32));
                    }
                    return item;
                }
                default: {
                    item.setLabel(type.label());
                    item.setPaletteLabel(LocaleFactory.localizedString(type.label()));
                    item.setToolTip(type.tooltip());
                    item.setTarget(controller.id());
                    item.setAction(type.action());
                    final NSButton button = NSButton.buttonWithFrame(new NSRect(0, 0));
                    button.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                    button.setImage(type.image());
                    button.sizeToFit();
                    button.setTarget(controller.id());
                    button.setAction(type.action());
                    item.setView(button);
                    return item;
                }
            }
        }
        catch(IllegalArgumentException e) {
            // Returning null will inform the toolbar this kind of item is not supported.
            return null;
        }
    }

    @Override
    public NSArray getDefault() {
        return NSArray.arrayWithObjects(
                connect.name(),
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                quickconnect.name(),
                tools.name(),
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                reload.name(),
                edit.name(),
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier,
                disconnect.name()
        );
    }

    @Override
    public NSArray getAllowed() {
        return NSArray.arrayWithObjects(
                connect.name(),
                browserview.name(),
                transfers.name(),
                quickconnect.name(),
                tools.name(),
                reload.name(),
                encoding.name(),
                synchronize.name(),
                download.name(),
                upload.name(),
                edit.name(),
                delete.name(),
                newfolder.name(),
                addbookmark.name(),
                info.name(),
                webbrowser.name(),
                terminal.name(),
                archive.name(),
                BrowserToolbarItem.quicklook.name(),
                log.name(),
                disconnect.name(),
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarSpaceItemIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }
}
