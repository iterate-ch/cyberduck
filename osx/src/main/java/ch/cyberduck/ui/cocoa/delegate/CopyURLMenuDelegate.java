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

import ch.cyberduck.binding.application.NSEvent;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pool.SessionPool;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CopyURLMenuDelegate extends URLMenuDelegate {
    private static final Logger log = Logger.getLogger(CopyURLMenuDelegate.class);

    @Override
    protected String getKeyEquivalent() {
        return "c";
    }

    @Override
    protected int getModifierMask() {
        return NSEvent.NSCommandKeyMask | NSEvent.NSShiftKeyMask;
    }

    @Override
    protected List<DescriptiveUrl> getURLs(final Path selected) {
        final ArrayList<DescriptiveUrl> list = new ArrayList<DescriptiveUrl>();
        final SessionPool pool = this.getSession();
        final Session<?> session;
        try {
            session = pool.borrow();
        }
        catch(BackgroundException e) {
            return list;
        }
        try {
            final UrlProvider provider = session.getFeature(UrlProvider.class);
            if(provider != null) {
                list.addAll(provider.toUrl(selected));
            }
            final DistributionConfiguration feature = session.getFeature(DistributionConfiguration.class);
            if(feature != null) {
                list.addAll(feature.toUrl(selected));
            }
            return list;
        }
        finally {
            pool.release(session, null);
        }
    }

    @Override
    public void handle(final List<DescriptiveUrl> selected) {
        final StringBuilder url = new StringBuilder();
        for(Iterator<DescriptiveUrl> iter = selected.iterator(); iter.hasNext(); ) {
            url.append(iter.next().getUrl());
            if(iter.hasNext()) {
                url.append("\n");
            }
        }
        final NSPasteboard pboard = NSPasteboard.generalPasteboard();
        pboard.declareTypes(NSArray.arrayWithObject(NSString.stringWithString(NSPasteboard.StringPboardType)), null);
        if(!pboard.setStringForType(url.toString(), NSPasteboard.StringPboardType)) {
            log.error(String.format("Error writing URL to %s", NSPasteboard.StringPboardType));
        }
    }
}
