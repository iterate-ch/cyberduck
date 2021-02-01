package ch.cyberduck.core.urlhandler;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;

import org.apache.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for the handler functions in ApplicationServices.h
 */
public final class LaunchServicesSchemeHandler extends AbstractSchemeHandler {
    private static final Logger log = Logger.getLogger(LaunchServicesSchemeHandler.class);

    static {
        Native.load("core");
    }

    private final ApplicationFinder applicationFinder;

    public LaunchServicesSchemeHandler() {
        this(ApplicationFinderFactory.get());
    }

    public LaunchServicesSchemeHandler(final ApplicationFinder applicationFinder) {
        this.applicationFinder = applicationFinder;
    }

    @Override
    public void setDefaultHandler(final Application application, final List<String> schemes) {
        for(String scheme : schemes) {
            if(0 != LaunchServicesLibrary.library.LSSetDefaultHandlerForURLScheme(scheme, application.getIdentifier())) {
                log.error(String.format("Failure setting default handler for scheme %s", scheme));
            }
        }
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyDefaultHandlerForURLScheme
     *
     * @param scheme The protocol identifier
     * @return The bundle identifier for the application registered as the default handler for this scheme
     */
    @Override
    public Application getDefaultHandler(final String scheme) {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSURL url = LaunchServicesLibrary.library.LSCopyDefaultApplicationURLForURL(NSURL.URLWithString(String.format("%s:/", scheme)),
            LaunchServicesLibrary.kLSRolesAll, error);
        if(url != null) {
            final Application application = applicationFinder.getDescription(NSBundle.bundleWithPath(url.path()).bundleIdentifier());
            if(applicationFinder.isInstalled(application)) {
                return application;
            }
        }
        return Application.notfound;
    }

    @Override
    public List<Application> getAllHandlers(final String scheme) {
        final List<Application> handlers = new ArrayList<Application>();
        final NSArray applications = LaunchServicesLibrary.library.LSCopyApplicationURLsForURL(NSURL.URLWithString(String.format("%s:/", scheme)), LaunchServicesLibrary.kLSRolesAll);
        NSEnumerator ordered = applications.objectEnumerator();
        NSObject next;
        while(((next = ordered.nextObject()) != null)) {
            NSURL url = Rococoa.cast(next, NSURL.class);
            final Application application = applicationFinder.getDescription(NSBundle.bundleWithPath(url.path()).bundleIdentifier());
            if(applicationFinder.isInstalled(application)) {
                handlers.add(application);
            }
        }
        return handlers;
    }
}
