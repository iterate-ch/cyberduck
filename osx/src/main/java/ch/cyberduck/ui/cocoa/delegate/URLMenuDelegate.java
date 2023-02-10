package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class URLMenuDelegate extends AbstractMenuDelegate {

    protected abstract SessionPool getSession();

    /**
     * @return Path selected in the browser or current working directory.
     */
    protected abstract List<Path> getSelected();

    @Override
    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        final List<Path> selected = this.getSelected();
        if(selected.isEmpty()) {
            return new NSInteger(1);
        }
        // Number of URLs for a single path
        int urls = this.getURLs(selected.iterator().next()).size();
        if(0 == urls) {
            return new NSInteger(1);
        }
        return new NSInteger(urls * 2);
    }

    protected abstract List<DescriptiveUrl> getURLs(Path selected);

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        final List<Path> selected = this.getSelected();
        if(selected.isEmpty() || this.getURLs(selected.iterator().next()).isEmpty()) {
            item.setTitle(LocaleFactory.localizedString("None"));
            item.setEnabled(false);
            item.setAction(null);
            item.setTarget(null);
            item.setImage(null);
        }
        else {
            boolean label = index.intValue() % 2 == 0;
            if(label) {
                item.setEnabled(true);
                item.setTarget(this.id());
                item.setAction(Foundation.selector("menuItemClicked:"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("site.tiff", 16));
                Iterator<Path> iter = selected.iterator();
                final DescriptiveUrl url = this.getURLs(iter.next()).get(index.intValue() / 2);
                item.setTitle(url.getHelp());
                if(url.getType().equals(DescriptiveUrl.Type.provider)) {
                    this.setShortcut(item, this.getKeyEquivalent(), this.getModifierMask());
                }
                else {
                    this.clearShortcut(item);
                }
            }
            else {
                // Dummy menu item to preview URL only
                final Set<String> target = this.getURLs(index, selected).stream().map(DescriptiveUrl::getPreview).collect(Collectors.toSet());
                item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(
                        StringUtils.join(target, '\n'), BundleController.MENU_HELP_FONT_ATTRIBUTES));
            }
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    private List<DescriptiveUrl> getURLs(final NSInteger index, final List<Path> selected) {
        List<DescriptiveUrl> list = new ArrayList<DescriptiveUrl>();
        for(final Path file : selected) {
            final List<DescriptiveUrl> urls = this.getURLs(file);
            final DescriptiveUrl url = urls.get(index.intValue() / 2);
            list.add(url);
        }
        return list;
    }

    @Action
    public void menuItemClicked(final NSMenuItem item) {
        this.handle(this.getURLs(item.menu().indexOfItem(item), this.getSelected()));
    }

    /**
     * @param selected URLs of selected files.
     */
    public abstract void handle(final List<DescriptiveUrl> selected);

    @Override
    public boolean validateMenuItem(final NSMenuItem item) {
        final List<Path> selected = this.getSelected();
        if(selected.isEmpty()) {
            return false;
        }
        final Selector action = item.action();
        if(action.equals(this.getDefaultAction())) {
            return StringUtils.isNotBlank(item.representedObject());
        }
        return true;
    }

    @Override
    protected ID getTarget() {
        return this.id();
    }

    @Override
    public Selector getDefaultAction() {
        return null;
    }
}
