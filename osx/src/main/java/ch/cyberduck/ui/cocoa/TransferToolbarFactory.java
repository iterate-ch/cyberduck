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

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;

import java.util.HashMap;
import java.util.Map;

public class TransferToolbarFactory implements ToolbarFactory {

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
        },
        reload,
        stop,
        remove,
        cleanup {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Clean Up");
            }
        },
        open,
        show,
        trash,
        log,
        search;

        public String label() {
            return LocaleFactory.localizedString(StringUtils.capitalize(this.name()));
        }
    }

    @Override
    public NSToolbarItem create(final String identifier) {
        if(!toolbarItems.containsKey(identifier)) {
            toolbarItems.put(identifier, NSToolbarItem.itemWithIdentifier(identifier));
        }
        final NSToolbarItem item = toolbarItems.get(identifier);
        switch(TransferToolbarItem.valueOf(identifier)) {
            case resume:
                item.setLabel(TransferToolbarItem.resume.label());
                item.setPaletteLabel(TransferToolbarItem.resume.label());
                item.setToolTip(TransferToolbarItem.resume.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("resume.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("resumeButtonClicked:"));
                return item;
            case reload:
                item.setLabel(TransferToolbarItem.reload.label());
                item.setPaletteLabel(TransferToolbarItem.reload.label());
                item.setToolTip(TransferToolbarItem.reload.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("reload.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("reloadButtonClicked:"));
                return item;
            case stop:
                item.setLabel(TransferToolbarItem.stop.label());
                item.setPaletteLabel(TransferToolbarItem.stop.label());
                item.setToolTip(TransferToolbarItem.stop.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("stop.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("stopButtonClicked:"));
                return item;
            case remove:
                item.setLabel(TransferToolbarItem.remove.label());
                item.setPaletteLabel(TransferToolbarItem.remove.label());
                item.setToolTip(TransferToolbarItem.remove.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("clean.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("deleteButtonClicked:"));
                return item;
            case cleanup:
                item.setLabel(TransferToolbarItem.cleanup.label());
                item.setPaletteLabel(TransferToolbarItem.cleanup.label());
                item.setToolTip(TransferToolbarItem.cleanup.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("cleanall.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("clearButtonClicked:"));
                return item;
            case open:
                item.setLabel(TransferToolbarItem.open.label());
                item.setPaletteLabel(TransferToolbarItem.open.label());
                item.setToolTip(TransferToolbarItem.open.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("open.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("openButtonClicked:"));
                return item;
            case show:
                item.setLabel(TransferToolbarItem.show.label());
                item.setPaletteLabel(LocaleFactory.localizedString("Show in Finder"));
                item.setToolTip(LocaleFactory.localizedString("Show in Finder"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("reveal.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("revealButtonClicked:"));
                return item;
            case trash:
                item.setLabel(TransferToolbarItem.trash.label());
                item.setPaletteLabel(TransferToolbarItem.trash.label());
                item.setToolTip(LocaleFactory.localizedString("Move to Trash"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("trash.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("trashButtonClicked:"));
                return item;
            case log:
                item.setLabel(TransferToolbarItem.log.label());
                item.setPaletteLabel(TransferToolbarItem.log.label());
                item.setToolTip(LocaleFactory.localizedString("Toggle Log Drawer"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("log.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("toggleLogDrawer:"));
                break;
            case search:
                item.setLabel(TransferToolbarItem.search.label());
                item.setPaletteLabel(TransferToolbarItem.search.label());
                item.setView(controller.getFilterField());
                return item;
        }
        // Identifier refered to a toolbar item that is not provide or supported.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    @Override
    public NSArray getDefault() {
        return NSArray.arrayWithObjects(
                TransferToolbarItem.resume.name(),
                TransferToolbarItem.stop.name(),
                TransferToolbarItem.reload.name(),
                TransferToolbarItem.remove.name(),
                TransferToolbarItem.show.name(),
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
                TransferToolbarItem.show.name(),
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
