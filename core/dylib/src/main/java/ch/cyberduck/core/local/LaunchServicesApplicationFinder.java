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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.library.Native;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class LaunchServicesApplicationFinder implements ApplicationFinder {
    private static final Logger log = Logger.getLogger(LaunchServicesApplicationFinder.class);

    static {
        Native.load("core");
    }

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
    private static Map<String, Application> applicationNameCache
            = Collections.<String, Application>synchronizedMap(new LRUMap(20));

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Application> defaultApplicationCache
            = Collections.<String, Application>synchronizedMap(new LRUMap(20));

    /**
     * Caching map between application bundle identifiers and
     * file type extensions.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, List<Application>> defaultApplicationListCache
            = Collections.<String, List<Application>>synchronizedMap(new LRUMap(20));

    @Override
    public List<Application> findAll(final String filename) {
        final String extension = FilenameUtils.getExtension(filename);
        if(StringUtils.isEmpty(extension)) {
            return Collections.emptyList();
        }
        if(!defaultApplicationListCache.containsKey(extension)) {
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
        final String extension = FilenameUtils.getExtension(filename);
        if(!defaultApplicationCache.containsKey(extension)) {
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
                    log.error(String.format("Loading bundle %s failed", path));
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
        if(applicationNameCache.containsKey(search)) {
            return applicationNameCache.get(search);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find application for %s", search));
        }
        final String identifier;
        final String name;
        synchronized(NSWorkspace.class) {
            final NSWorkspace workspace = NSWorkspace.sharedWorkspace();
            final String path;
            if(null != workspace.absolutePathForAppBundleWithIdentifier(search)) {
                path = workspace.absolutePathForAppBundleWithIdentifier(search);
            }
            else {
                log.warn(String.format("Cannot determine installation path for bundle identifier %s. Try with name.", search));
                path = workspace.fullPathForApplication(search);
            }
            if(StringUtils.isNotBlank(path)) {
                final NSBundle app = NSBundle.bundleWithPath(path);
                if(null == app) {
                    log.error(String.format("Loading bundle %s failed", path));
                    identifier = search;
                    name = FilenameUtils.removeExtension(LocalFactory.get(path).getDisplayName());
                }
                else {
                    NSDictionary dict = app.infoDictionary();
                    if(null == dict) {
                        log.error(String.format("Loading application dictionary for bundle %s failed", path));
                        applicationNameCache.put(search, Application.notfound);
                        return null;
                    }
                    else {
                        final NSObject bundlename = dict.objectForKey("CFBundleName");
                        if(null == bundlename) {
                            log.warn(String.format("No CFBundleName in bundle %s", path));
                            name = FilenameUtils.removeExtension(LocalFactory.get(path).getDisplayName());
                        }
                        else {
                            name = bundlename.toString();
                        }
                        final NSObject bundleIdentifier = dict.objectForKey("CFBundleIdentifier");
                        if(null == bundleIdentifier) {
                            log.warn(String.format("No CFBundleName in bundle %s", path));
                            identifier = search;
                        }
                        else {
                            identifier = bundleIdentifier.toString();
                        }

                    }
                }
            }
            else {
                log.warn(String.format("Cannot determine installation path for %s", search));
                return Application.notfound;
            }
        }
        final Application application = new Application(identifier, name);
        applicationNameCache.put(identifier, application);
        return application;
    }

    @Override
    public boolean isInstalled(final Application application) {
        synchronized(NSWorkspace.class) {
            if(Application.notfound.equals(application)) {
                return false;
            }
            return NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
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