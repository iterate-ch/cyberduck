package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSButtonCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.HashMap;
import java.util.Map;

import static ch.cyberduck.ui.cocoa.TransferToolbarFactory.TransferToolbarItem.search;

public class TransferToolbarFactory extends AbstractToolbarFactory implements ToolbarFactory {

    private TransferController controller;

    public TransferToolbarFactory(final TransferController controller) {
        this.controller = controller;
    }

    /**
     * Keep reference to weak toolbar items
     */
    private Map<String, NSToolbarItem> toolbarItems
            = new HashMap<String, NSToolbarItem>();

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
            public Selector action() {
                return Foundation.selector("trashButtonClicked:");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Move to Trash");
            }
        },
        log {
            @Override
            public Selector action() {
                return Foundation.selector("toggleLogDrawer:");
            }

            @Override
            public String tooltip() {
                return LocaleFactory.localizedString("Toggle Log Drawer");
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
            toolbarItems.put(identifier, CDToolbarItem.itemWithIdentifier(identifier));
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
                default: {
                    item.setLabel(type.label());
                    item.setPaletteLabel(type.label());
                    item.setToolTip(type.tooltip());
                    item.setImage(type.image());
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
                TransferToolbarItem.resume.name(),
                TransferToolbarItem.stop.name(),
                TransferToolbarItem.reload.name(),
                TransferToolbarItem.remove.name(),
                TransferToolbarItem.reveal.name(),
                NSToolbarItem.NSToolbarFlexibleItemIdentifier,
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
                TransferToolbarItem.log.name(),
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarSpaceItemIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }
}
