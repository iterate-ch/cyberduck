package ch.cyberduck.core.urlhandler;/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Rococoa;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceSchemeHandler extends AbstractSchemeHandler {
    private static final Logger log = LogManager.getLogger(WorkspaceSchemeHandler.class);

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();
    private final ApplicationFinder finder;

    public WorkspaceSchemeHandler() {
        this(ApplicationFinderFactory.get());
    }

    public WorkspaceSchemeHandler(final ApplicationFinder finder) {
        this.finder = finder;
    }

    @Override
    public void setDefaultHandler(final Application application, final List<String> schemes) {
        for(String scheme : schemes) {
            final String path = workspace.absolutePathForAppBundleWithIdentifier(application.getIdentifier());
            if(null != path) {
                workspace.setDefaultApplicationAtURL_toOpenURLsWithScheme_completionHandler(
                        NSURL.fileURLWithPath(path), scheme, null);
            }
        }
    }

    @Override
    public Application getDefaultHandler(final String scheme) {
        final NSURL url = workspace.URLForApplicationToOpenURL(NSURL.URLWithString(String.format("%s:/", scheme)));
        if(url != null) {
            final NSBundle bundle = NSBundle.bundleWithPath(url.path());
            if(null == bundle) {
                log.warn(String.format("Failure loading bundle for path %s", url.path()));
                return Application.notfound;
            }
            final Application application = finder.getDescription(bundle.bundleIdentifier());
            if(finder.isInstalled(application)) {
                return application;
            }
        }
        return Application.notfound;
    }

    @Override
    public List<Application> getAllHandlers(final String scheme) {
        final List<Application> applications = new ArrayList<>();
        final NSArray urls = workspace.URLsForApplicationsToOpenURL(NSURL.URLWithString(String.format("%s:/", scheme)));
        NSEnumerator enumerator = urls.objectEnumerator();
        NSObject next;
        while((next = enumerator.nextObject()) != null) {
            final NSURL url = Rococoa.cast(next, NSURL.class);
            final NSBundle bundle = NSBundle.bundleWithPath(url.path());
            if(null == bundle) {
                log.warn(String.format("Failure loading bundle for path %s", url.path()));
                continue;
            }
            final Application application = finder.getDescription(bundle.bundleIdentifier());
            if(finder.isInstalled(application)) {
                applications.add(application);
            }
        }
        return applications;
    }
}
