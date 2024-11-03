package ch.cyberduck.core.local;

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

import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.library.Native;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LaunchServicesApplicationFinder implements ApplicationFinder {
    private static final Logger log = LogManager.getLogger(LaunchServicesApplicationFinder.class);

    static {
        Native.load("core");
    }

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    public LaunchServicesApplicationFinder() {
        //
    }

    /**
     * Uses LSGetApplicationForInfo
     *
     * @param extension File extension
     * @return Null if not found
     */
    private native String findForType(String extension);

    /**
     * Uses LSCopyAllRoleHandlersForContentType
     *
     * @param extension File extension
     * @return Empty array if none found
     */
    private native String[] findAllForType(String extension);

    /**
     * Uses LSRegisterURL
     *
     * @param path Location of application bundle
     */
    private native boolean register(String path);

    /**
     * Caching map between application bundle identifier and
     * display name of application
     */
    @SuppressWarnings("unchecked")
    private static final LRUCache<String, Application> applicationNameCache
        = LRUCache.build(20);

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static final LRUCache<String, Application> defaultApplicationCache
        = LRUCache.build(20);

    /**
     * Caching map between application bundle identifiers and
     * file type extensions.
     */
    @SuppressWarnings("unchecked")
    private static final LRUCache<String, List<Application>> defaultApplicationListCache
        = LRUCache.build(20);

    @Override
    public List<Application> findAll(final String filename) {
        final String extension = Path.getExtension(filename);
        if(StringUtils.isEmpty(extension)) {
            return Collections.emptyList();
        }
        if(!defaultApplicationListCache.contains(extension)) {
            final List<Application> applications = new ArrayList<Application>();
            for(String identifier : this.findAllForType(extension)) {
                applications.add(this.getDescription(identifier));
            }
            // Because of the different API used the default opening application may not be included
            // in the above list returned. Always add the default application anyway.
            final Application defaultApplication = this.find(filename);
            if(this.isInstalled(defaultApplication)) {
                if(!applications.contains(defaultApplication)) {
                    applications.add(defaultApplication);
                }
            }
            defaultApplicationListCache.put(extension, applications);
        }
        return defaultApplicationListCache.get(extension);
    }


    /**
     * The default application for this file as set by the launch services
     *
     * @param filename Filename
     * @return The bundle identifier of the default application to open the
     * file of this type or null if unknown
     */
    @Override
    public Application find(final String filename) {
        final String extension = Path.getExtension(filename);
        if(!defaultApplicationCache.contains(extension)) {
            if(StringUtils.isEmpty(extension)) {
                return Application.notfound;
            }
            final String path = this.findForType(extension);
            if(StringUtils.isEmpty(path)) {
                defaultApplicationCache.put(extension, Application.notfound);
            }
            else {
                final NSBundle bundle = NSBundle.bundleWithPath(path);
                if(null == bundle) {
                    log.error("Loading bundle {} failed", path);
                    defaultApplicationCache.put(extension, Application.notfound);
                }
                else {
                    defaultApplicationCache.put(extension, this.getDescription(bundle.bundleIdentifier()));
                }
            }
        }
        return defaultApplicationCache.get(extension);
    }

    /**
     * Determine the human readable application name for a given bundle identifier.
     *
     * @param search Bundle identifier
     * @return Application human readable name
     */
    @Override
    public Application getDescription(final String search) {
        if(applicationNameCache.contains(search)) {
            return applicationNameCache.get(search);
        }
        if(log.isDebugEnabled()) {
            log.debug("Find application for {}", search);
        }
        final String identifier;
        final String name;
        synchronized(NSWorkspace.class) {
            final String path;
            if(null != workspace.absolutePathForAppBundleWithIdentifier(search)) {
                path = workspace.absolutePathForAppBundleWithIdentifier(search);
            }
            else {
                log.warn("Cannot determine installation path for bundle identifier {}. Try with name.", search);
                path = workspace.fullPathForApplication(search);
            }
            if(StringUtils.isNotBlank(path)) {
                final NSBundle app = NSBundle.bundleWithPath(path);
                if(null == app) {
                    log.error("Loading bundle {} failed", path);
                    identifier = search;
                    name = FilenameUtils.removeExtension(new FinderLocal(path).getDisplayName());
                }
                else {
                    NSDictionary dict = app.infoDictionary();
                    if(null == dict) {
                        log.error("Loading application dictionary for bundle {} failed", path);
                        applicationNameCache.put(search, Application.notfound);
                        return null;
                    }
                    else {
                        final NSObject bundlename = dict.objectForKey("CFBundleName");
                        if(null == bundlename) {
                            log.warn("No CFBundleName in bundle {}", path);
                            name = FilenameUtils.removeExtension(new FinderLocal(path).getDisplayName());
                        }
                        else {
                            name = bundlename.toString();
                        }
                        final NSObject bundleIdentifier = dict.objectForKey("CFBundleIdentifier");
                        if(null == bundleIdentifier) {
                            log.warn("No CFBundleName in bundle {}", path);
                            identifier = search;
                        }
                        else {
                            identifier = bundleIdentifier.toString();
                        }

                    }
                }
            }
            else {
                log.warn("Cannot determine installation path for {}", search);
                applicationNameCache.put(search, Application.notfound);
                return Application.notfound;
            }
        }
        final Application application = new Application(identifier, name);
        applicationNameCache.put(search, application);
        return application;
    }

    @Override
    public boolean isInstalled(final Application application) {
        synchronized(NSWorkspace.class) {
            if(Application.notfound.equals(application)) {
                return false;
            }
            return workspace.absolutePathForAppBundleWithIdentifier(
                application.getIdentifier()) != null;
        }
    }

    /**
     * Register application in launch services database
     *
     * @param application Bundle identifier
     */
    public boolean register(final Local application) {
        synchronized(NSWorkspace.class) {
            if(!application.exists()) {
                return false;
            }
            return this.register(application.getAbsolute());
        }
    }
}
