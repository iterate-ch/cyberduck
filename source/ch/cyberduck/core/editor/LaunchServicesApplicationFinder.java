package ch.cyberduck.core.editor;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Native;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class LaunchServicesApplicationFinder implements ApplicationFinder {
    private static Logger log = Logger.getLogger(ApplicationFinder.class);

    public static void register() {
        ApplicationFinderFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends ApplicationFinderFactory {
        @Override
        protected ApplicationFinder create() {
            return new LaunchServicesApplicationFinder();
        }
    }

    private LaunchServicesApplicationFinder() {
        //
    }

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("LaunchServicesApplicationFinder");
        }
        return JNI_LOADED;
    }

    /**
     * Uses LSGetApplicationForInfo
     *
     * @param extension File extension
     * @return Null if not found
     */
    private native String find(String extension);

    /**
     * Uses LSCopyAllRoleHandlersForContentType
     *
     * @param extension File extension
     * @return Empty array if none found
     */
    private native String[] findAll(String extension);

    /**
     * Caching map between application bundle identifier and
     * display name of application
     */
    private static Map<String, String> applicationNameCache
            = Collections.<String, String>synchronizedMap(new LRUMap(20) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     *
     */
    private static Map<String, String> defaultApplicationCache
            = Collections.<String, String>synchronizedMap(new LRUMap(20) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     * Caching map between application bundle identifiers and
     * file type extensions.
     */
    private static Map<String, List<String>> defaultApplicationListCache
            = Collections.<String, List<String>>synchronizedMap(new LRUMap(20) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    @Override
    public List<String> findAll(final Local file) {
        if(!loadNative()) {
            return Collections.emptyList();
        }
        final String extension = file.getExtension();
        if(StringUtils.isEmpty(extension)) {
            return Collections.emptyList();
        }
        if(!defaultApplicationListCache.containsKey(extension)) {
            final List<String> applications = new ArrayList<String>(Arrays.asList(this.findAll(extension)));
            // Because of the different API used the default opening application may not be included
            // in the above list returned. Always add the default application anyway.
            final String defaultApplication = this.find(file);
            if(null != defaultApplication) {
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
     * @return The bundle identifier of the default application to open the
     *         file of this type or null if unknown
     */
    @Override
    public String find(final Local file) {
        if(!loadNative()) {
            return null;
        }
        final String extension = file.getExtension();
        if(!defaultApplicationCache.containsKey(extension)) {
            if(StringUtils.isEmpty(extension)) {
                return null;
            }
            final String path = this.find(extension);
            if(StringUtils.isEmpty(path)) {
                defaultApplicationCache.put(extension, null);
            }
            else {
                NSBundle bundle = NSBundle.bundleWithPath(path);
                if(null == bundle) {
                    log.error("Loading bundle failed:" + path);
                    defaultApplicationCache.put(extension, null);
                }
                else {
                    defaultApplicationCache.put(extension, bundle.bundleIdentifier());
                }
            }
        }
        return defaultApplicationCache.get(extension);
    }

    @Override
    public String getName(final String bundleIdentifier) {
        if(!applicationNameCache.containsKey(bundleIdentifier)) {
            log.debug("getApplicationName:" + bundleIdentifier);
            final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier);
            String name = null;
            if(StringUtils.isNotBlank(path)) {
                NSBundle app = NSBundle.bundleWithPath(path);
                if(null == app) {
                    log.error("Loading bundle failed:" + path);
                }
                else {
                    NSDictionary dict = app.infoDictionary();
                    if(null == dict) {
                        log.error("Loading application dictionary failed:" + path);
                        applicationNameCache.put(bundleIdentifier, null);
                        return null;
                    }
                    else {
                        final NSObject bundlename = dict.objectForKey("CFBundleName");
                        if(null == bundlename) {
                            log.warn(String.format("No CFBundleName for %s", bundleIdentifier));
                        }
                        else {
                            name = bundlename.toString();
                        }
                    }
                }
                if(null == name) {
                    name = FilenameUtils.removeExtension(LocalFactory.createLocal(path).getDisplayName());
                }
            }
            else {
                log.warn(String.format("Cannot determine installation path for %s", bundleIdentifier));
            }
            applicationNameCache.put(bundleIdentifier, name);
        }
        return applicationNameCache.get(bundleIdentifier);
    }

    /**
     * @return True if the editor application is running
     */
    @Override
    public boolean isOpen(final String bundleIdentifier) {
        final NSEnumerator apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
        NSObject next;
        while(((next = apps.nextObject()) != null)) {
            NSDictionary app = Rococoa.cast(next, NSDictionary.class);
            final NSObject identifier = app.objectForKey("NSApplicationBundleIdentifier");
            if(identifier.toString().equals(bundleIdentifier)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Found open application %s", bundleIdentifier));
                }
                return true;
            }
        }
        return false;
    }
}