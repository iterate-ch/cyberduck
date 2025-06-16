package ch.cyberduck.ui.cocoa.toolbar;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSButtonCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSearchToolbarItem;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.transfer.TransferConnectionLimiter;
import ch.cyberduck.ui.cocoa.controller.TransferController;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSSize;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static ch.cyberduck.binding.application.NSButton.NSTexturedRoundedBezelStyle;
import static ch.cyberduck.ui.cocoa.toolbar.TransferToolbarFactory.TransferToolbarItem.*;

public class TransferToolbarFactory extends AbstractToolbarFactory implements ToolbarFactory {

    private final TransferController controller;

    public Preferences preferences = PreferencesFactory.get();

    public TransferToolbarFactory(final TransferController controller) {
        this.controller = controller;
    }

    /**
     * Keep reference to weak toolbar items
     */
    private final Map<String, NSToolbarItem> toolbarItems
        = new HashMap<>();

    public enum TransferToolbarItem {
        resume {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Resume", "Transfer");
            }

            @Override
            public Selector action() {
                return Foundation.selector("resumeButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("transferresume.pdf");
            }
        },
        reload {
            @Override
            public Selector action() {
                return Foundation.selector("reloadButtonClicked:");
            }
        },
        stop {
            @Override
            public Selector action() {
                return Foundation.selector("stopButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("transferstop.pdf");
            }
        },
        remove {
            @Override
            public Selector action() {
                return Foundation.selector("deleteButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("transferremove.pdf");
            }
        },
        cleanup {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Clean Up");
            }

            @Override
            public Selector action() {
                return Foundation.selector("clearButtonClicked:");
            }
        },
        open {
            @Override
            public Selector action() {
                return Foundation.selector("openButtonClicked:");
            }
        },
        reveal {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Show in Finder");
            }

            @Override
            public Selector action() {
                return Foundation.selector("revealButtonClicked:");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("showinfinder.pdf");
            }
        },
        trash {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize(this.name()), "Transfer");
            }

            @Override
            public Selector action() {
                return Foundation.selector("trashButtonClicked:");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Move to Trash");
            }
        },
        bandwidth {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize(this.name()), "Preferences");
            }

            @Override
            public Selector action() {
                return Foundation.selector("bandwidthPopupChanged:");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Bandwidth", "Preferences");
            }

            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("bandwidth.tiff", 16);
            }
        },
        connections {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Connections", "Transfer");
            }

            @Override
            public Selector action() {
                return Foundation.selector("connectionsPopupChanged:");
            }
            @Override
            public NSImage image() {
                return IconCacheFactory.<NSImage>get().iconNamed("connection.tiff", 16);
            }
        },
        search {
            @Override
            public Selector action() {
                return null;
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

    @Override
    public NSToolbarItem create(final String identifier) {
        if(!toolbarItems.containsKey(identifier)) {
            switch(TransferToolbarFactory.TransferToolbarItem.valueOf(identifier)) {
                default:
                    toolbarItems.put(identifier, CDToolbarItem.itemWithIdentifier(identifier));
                    break;
                case search:
                    toolbarItems.put(identifier, NSSearchToolbarItem.itemWithIdentifier(identifier));
            }
        }
        final NSToolbarItem item = toolbarItems.get(identifier);
        try {
            final TransferToolbarItem type = TransferToolbarItem.valueOf(identifier);
            switch(type) {
                case search: {
                    item.setLabel(search.label());
                    item.setPaletteLabel(search.label());
                    item.setView(controller.getFilterField());
                    return item;
                }
                case bandwidth: {
                    item.setLabel(bandwidth.label());
                    item.setPaletteLabel(bandwidth.label());
                    item.setToolTip(bandwidth.tooltip());
                    // Add a menu representation for text mode of toolbar
                    NSMenuItem toolbarMenu = NSMenuItem.itemWithTitle(bandwidth.label(), bandwidth.action(), StringUtils.EMPTY);
                    NSMenu bandwidthMenu = NSMenu.menu();
                    bandwidthMenu.setAutoenablesItems(true);
                    bandwidthMenu.setDelegate(controller.getBandwidthMenuDelegate().id());
                    final NSMenuItem unlimited = bandwidthMenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("Unlimited Bandwidth", "Transfer"),
                        bandwidth.action(), StringUtils.EMPTY);
                    unlimited.setImage(bandwidth.image());
                    unlimited.setRepresentedObject(String.valueOf(BandwidthThrottle.UNLIMITED));
                    bandwidthMenu.addItem(NSMenuItem.separatorItem());
                    final StringTokenizer options = new StringTokenizer(preferences.getProperty("queue.bandwidth.options"), ",");
                    while(options.hasMoreTokens()) {
                        final String bytes = options.nextToken();
                        final NSMenuItem m = bandwidthMenu.addItemWithTitle_action_keyEquivalent(SizeFormatterFactory.get().format(Integer.parseInt(bytes)) + "/s",
                            bandwidth.action(), StringUtils.EMPTY);
                        // Mark as throttled
                        final NSImage image = IconCacheFactory.<NSImage>get().iconNamed("turtle.tiff", 16);
                        image.setTemplate(true);
                        m.setImage(image);
                        m.setRepresentedObject(bytes);
                    }
                    toolbarMenu.setSubmenu(bandwidthMenu);
                    item.setMenuFormRepresentation(toolbarMenu);
                    final NSPopUpButton button = NSPopUpButton.buttonPullsDown(false);
                    button.setBezelStyle(NSTexturedRoundedBezelStyle);
                    button.setImage(bandwidth.image());
                    button.setMenu(bandwidthMenu);
                    button.setTarget(controller.id());
                    button.setAction(bandwidth.action());
                    item.setView(button);
                    item.setMaxSize(new NSSize(button.frame().size.width.doubleValue(), button.frame().size.height.doubleValue()));
                    return item;
                }
                case connections: {
                    item.setLabel(connections.label());
                    item.setPaletteLabel(connections.label());
                    item.setToolTip(connections.tooltip());
                    // Add a menu representation for text mode of toolbar
                    NSMenuItem toolbarMenu = NSMenuItem.itemWithTitle(connections.label(), connections.action(), StringUtils.EMPTY);
                    NSMenu connectionsMenu = NSMenu.menu();
                    connectionsMenu.setAutoenablesItems(true);
                    final StringTokenizer options = new StringTokenizer(preferences.getProperty("queue.connections.options"), ",");
                    while(options.hasMoreTokens()) {
                        final int connections = Integer.parseInt(options.nextToken());
                        final NSMenuItem menu;
                        if(TransferConnectionLimiter.AUTO == connections) {
                            menu = connectionsMenu.addItemWithTitle_action_keyEquivalent(
                                MessageFormat.format(LocaleFactory.localizedString("Auto"), connections),
                                TransferToolbarItem.connections.action(), StringUtils.EMPTY);
                        }
                        else {
                            menu = connectionsMenu.addItemWithTitle_action_keyEquivalent(
                                MessageFormat.format(LocaleFactory.localizedString("{0} Connections", "Transfer"), connections),
                                TransferToolbarItem.connections.action(), StringUtils.EMPTY);
                        }
                        menu.setImage(TransferToolbarItem.connections.image());
                        menu.setRepresentedObject(String.valueOf(connections));
                    }
                    toolbarMenu.setSubmenu(connectionsMenu);
                    item.setMenuFormRepresentation(toolbarMenu);
                    final NSPopUpButton button = NSPopUpButton.buttonPullsDown(false);
                    button.setBezelStyle(NSTexturedRoundedBezelStyle);
                    button.setImage(connections.image());
                    button.setMenu(connectionsMenu);
                    button.setTarget(controller.id());
                    button.setAction(connections.action());
                    button.selectItemAtIndex(button.indexOfItemWithRepresentedObject(String.valueOf(preferences.getInteger("queue.connections.limit"))));
                    item.setView(button);
                    item.setMaxSize(new NSSize(button.frame().size.width.doubleValue(), button.frame().size.height.doubleValue()));
                    return item;
                }
                default: {
                    item.setLabel(type.label());
                    item.setPaletteLabel(type.label());
                    item.setToolTip(type.tooltip());
                    item.setImage(type.image());
                    item.setTarget(controller.id());
                    item.setAction(type.action());
                    final NSButton button = NSButton.buttonWithImageTargetAction(type.image(), controller.id(), type.action());
                    button.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                    button.sizeToFit();
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
            TransferToolbarItem.resume.name(),
            TransferToolbarItem.stop.name(),
            TransferToolbarItem.reload.name(),
            TransferToolbarItem.remove.name(),
            TransferToolbarItem.reveal.name(),
            NSToolbarItem.NSToolbarFlexibleItemIdentifier,
            TransferToolbarItem.bandwidth.name(),
            TransferToolbarItem.connections.name(),
            TransferToolbarItem.search.name()
        );
    }

    @Override
    public NSArray getAllowed() {
        return NSArray.arrayWithObjects(
            TransferToolbarItem.resume.name(),
            TransferToolbarItem.reload.name(),
            TransferToolbarItem.stop.name(),
            TransferToolbarItem.remove.name(),
            TransferToolbarItem.cleanup.name(),
            TransferToolbarItem.reveal.name(),
            TransferToolbarItem.open.name(),
            TransferToolbarItem.trash.name(),
            TransferToolbarItem.search.name(),
            TransferToolbarItem.bandwidth.name(),
            TransferToolbarItem.connections.name(),
            NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
            NSToolbarItem.NSToolbarSpaceItemIdentifier,
            NSToolbarItem.NSToolbarSeparatorItemIdentifier,
            NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }
}
