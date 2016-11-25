package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.pool.SessionPool;

import java.util.ArrayList;
import java.util.List;

public abstract class OpenURLMenuDelegate extends URLMenuDelegate {

    @Override
    protected String getKeyEquivalent() {
        return "b";
    }

    @Override
    protected int getModifierMask() {
        return NSEvent.NSCommandKeyMask | NSEvent.NSShiftKeyMask;
    }

    @Override
    protected List<DescriptiveUrl> getURLs(final Path selected) {
        final ArrayList<DescriptiveUrl> list = new ArrayList<DescriptiveUrl>();
        final SessionPool pool = this.getSession();
        final UrlProvider provider = pool.getFeature(UrlProvider.class);
        if(provider != null) {
            list.addAll(provider.toUrl(selected).filter(
                    DescriptiveUrl.Type.http, DescriptiveUrl.Type.cname, DescriptiveUrl.Type.cdn,
                    DescriptiveUrl.Type.signed, DescriptiveUrl.Type.authenticated, DescriptiveUrl.Type.torrent));
        }
        final DistributionConfiguration feature = pool.getFeature(DistributionConfiguration.class);
        if(feature != null) {
            list.addAll(feature.toUrl(selected));
        }
        return list;
    }

    @Override
    public void handle(final List<DescriptiveUrl> selected) {
        for(DescriptiveUrl url : selected) {
            BrowserLauncherFactory.get().open(url.getUrl());
        }
    }
}