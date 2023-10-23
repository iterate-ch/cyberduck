package ch.cyberduck.core.preferences;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSLocale;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.binding.foundation.NSUserDefaults;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.sparkle.Sandbox;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Rococoa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.mac.SystemB;

/**
 * Concrete subclass using the Cocoa Preferences classes. The NSUserDefaults class is thread-safe.
 *
 * @see ch.cyberduck.binding.foundation.NSUserDefaults
 */
public class UserDefaultsPreferences extends DefaultPreferences {
    private static final Logger log = LogManager.getLogger(UserDefaultsPreferences.class);

    private final NSBundle bundle = new BundleApplicationResourcesFinder().bundle();

    private final LRUCache<String, String> cache = LRUCache.usingLoader(this::loadProperty, 1000);

    private static final String MISSING_PROPERTY = String.valueOf(StringUtils.INDEX_NOT_FOUND);

    private NSUserDefaults store;

    /**
     * Additionally look for default values in Info.plist of application bundle.
     *
     * @param property The property to query.
     * @return A default value if any or null if not found.
     */
    @Override
    public String getDefault(final String property) {
        // Lookup in the default map
        final String value = super.getDefault(property);
        if(null == value) {
            // Missing in default. Lookup in Info.plist
            NSObject plist = bundle.infoDictionary().objectForKey(property);
            if(null == plist) {
                if(log.isTraceEnabled()) {
                    log.trace(String.format("No default value for property %s", property));
                }
                return null;
            }
            return plist.toString();
        }
        // Default value of property found
        return value;
    }

    @Override
    public String getProperty(final String property) {
        final String value = cache.get(property);
        return StringUtils.equals(MISSING_PROPERTY, value) ? null : value;
    }

    /**
     * Load and convert from native storage into cache
     */
    private String loadProperty(final String property) {
        final NSObject value = store.objectForKey(property);
        if(null == value) {
            final String d = this.getDefault(property);
            return null == d ? MISSING_PROPERTY : d;
        }
        // Customized property found
        if(value.isKindOfClass(NSString.CLASS)) {
            return value.toString();
        }
        if(value.isKindOfClass(NSArray.CLASS)) {
            return StringUtils.join(this.toList(Rococoa.cast(value, NSArray.class)), LIST_SEPERATOR);
        }
        log.warn(String.format("Unknown type for property %s", property));
        return value.toString();
    }

    @Override
    public void setProperty(final String property, final String value) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Set property %s for key %s", value, property));
        }
        if(StringUtils.isNotEmpty(value)) {
            // Sets the value of the default identified by defaultName in the standard application domain.
            // Setting a default has no effect on the value returned by the objectForKey method if
            // the same key exists in a domain that precedes the application domain in the search list.
            store.setObjectForKey(NSString.stringWithString(value), property);
            cache.put(property, value);
        }
        else {
            this.deleteProperty(property);
        }
    }

    @Override
    public void setDefault(final String property, final String value) {
        super.setDefault(property, value);
        cache.remove(property);
    }

    @Override
    public void setProperty(final String property, final List<String> value) {
        // Sets the value of the default identified by defaultName in the standard application domain.
        // Setting a default has no effect on the value returned by the objectForKey method if
        // the same key exists in a domain that precedes the application domain in the search list.
        store.setObjectForKey(NSArray.arrayWithObjects(value.toArray(new String[value.size()])), property);
    }

    @Override
    public void deleteProperty(final String property) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Delete property %s", property));
        }
        store.removeObjectForKey(property);
        cache.remove(property);
    }

    /**
     * Overwrite the default values with user props if any.
     */
    @Override
    public void load() {
        store = NSUserDefaults.standardUserDefaults();
    }

    @Override
    protected void setDefaults() {
        // Parent defaults
        super.setDefaults();

        if(Sandbox.get().isSandboxed()) {
            // Set actual home directory outside of sandbox
            this.setDefault("local.user.home", SystemB.INSTANCE.getpwuid(LibC.INSTANCE.getuid()).pw_dir);
        }

        if(null != bundle) {
            if(bundle.objectForInfoDictionaryKey("CFBundleName") != null) {
                this.setDefault("application.name", bundle.objectForInfoDictionaryKey("CFBundleName").toString());
            }
            if(bundle.objectForInfoDictionaryKey("NSHumanReadableCopyright") != null) {
                this.setDefault("application.copyright", bundle.objectForInfoDictionaryKey("NSHumanReadableCopyright").toString());
            }
            if(bundle.objectForInfoDictionaryKey("CFBundleIdentifier") != null) {
                final String bundleIdentifier = bundle.objectForInfoDictionaryKey("CFBundleIdentifier").toString();
                this.setDefault("application.identifier", bundleIdentifier);
                // Append bundle identifier to tmp dir
                final Local directory = LocalFactory.get(FoundationKitFunctions.library.NSTemporaryDirectory(), bundleIdentifier);
                try {
                    directory.mkdir();
                    this.setDefault("tmp.dir", directory.getAbsolute());
                }
                catch(AccessDeniedException e) {
                    log.warn(String.format("Failure creating temporary directory %s", directory));
                }
            }
            if(bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") != null) {
                this.setDefault("application.version", bundle.objectForInfoDictionaryKey("CFBundleShortVersionString").toString());
            }
            if(bundle.objectForInfoDictionaryKey("CFBundleVersion") != null) {
                this.setDefault("application.revision", bundle.objectForInfoDictionaryKey("CFBundleVersion").toString());
            }
            this.setDefault("application.receipt.path", String.format("%s/Contents/_MASReceipt", bundle.bundlePath()));
        }
        this.setDefault("oauth.handler.scheme",
            String.format("x-%s-action", StringUtils.deleteWhitespace(StringUtils.lowerCase(this.getProperty("application.name")))));

        this.setDefault("update.feed.release", "https://version.cyberduck.io/changelog.rss");
        this.setDefault("update.feed.beta", "https://version.cyberduck.io/beta/changelog.rss");
        this.setDefault("update.feed.nightly", "https://version.cyberduck.io/nightly/changelog.rss");

        this.setDefault("bookmark.import.filezilla.location", "~/.config/filezilla/sitemanager.xml");
        this.setDefault("bookmark.import.fetch.location", "~/Library/Preferences/com.fetchsoftworks.Fetch.Shortcuts.plist");
        this.setDefault("bookmark.import.flow.location", "~/Library/Application Support/Flow/Bookmarks.plist");
        this.setDefault("bookmark.import.interarchy.location", "~/Library/Application Support/Interarchy/Bookmarks.plist");
        this.setDefault("bookmark.import.transmit3.location", "~/Library/Preferences/com.panic.Transmit.plist");
        this.setDefault("bookmark.import.transmit4.location", "~/Library/Application Support/Transmit/Favorites/Favorites.xml");
        this.setDefault("bookmark.import.transmit5.location", "~/Library/Application Support/Transmit/Metadata");
        this.setDefault("bookmark.import.crossftp.location", "~/.crossftp/sites.xml");
        this.setDefault("bookmark.import.fireftp.location", "~/Library/Application Support/Firefox/Profiles");
        this.setDefault("bookmark.import.expandrive3.location", "~/Library/Application Support/ExpanDrive/favorites.js");
        this.setDefault("bookmark.import.expandrive4.location", "~/Library/Application Support/ExpanDrive/expandrive4.favorites.js");
        this.setDefault("bookmark.import.expandrive5.location", "~/Library/Application Support/ExpanDrive/expandrive5.favorites.js");
        this.setDefault("bookmark.import.expandrive6.location", "~/Library/Application Support/ExpanDrive/expandrive6.favorites.js");
        this.setDefault("bookmark.import.cloudmounter.location", "~/Library/Preferences/com.eltima.cloudmounter.plist");

        this.setDefault("browser.filesize.decimal", String.valueOf(true));

        // SSL Keystore
        this.setDefault("connection.ssl.keystore.type", "KeychainStore");
        this.setDefault("connection.ssl.keystore.provider", "Apple");

        this.setDefault("browser.window.tabbing.identifier", "browser.window.tabbing.identifier");
        // Allow to show transfers in browser window as tab
        this.setDefault("queue.window.tabbing.identifier", "browser.window.tabbing.identifier");

        this.setDefault("terminal.command.iterm2", "set t to (create window with default profile)\n" +
            "tell t\n" +
            "set s to (current session)\n" +
            "tell s\n" +
            "write text \"{0}\"\n" +
            "end tell\n" +
            "end tell");
        this.setDefault("terminal.command.default", "do script \"{0}\"");

        // Workaround for #11508
        this.deleteProperty("__NSDisableSharingTextTabInstance");
    }

    @Override
    public void save() {
        // Saves any modifications to the persistent domains and updates all
        // persistent domains that were not modified to  what is on disk.
        // Returns false if it could not save data to disk. Because synchronize
        // is automatically invoked at periodic intervals, use this method only
        // if you cannot wait for the automatic synchronization (for example, if
        // your application is about to exit) or if you want to update user props
        // to what is on disk even though you have not made any changes.
        store.synchronize();
    }

    @Override
    public String locale() {
        final NSObject value = store.objectForKey("AppleLanguages");
        if(null == value) {
            return super.locale();
        }
        if(value.isKindOfClass(NSArray.CLASS)) {
            final Iterator<String> languages = this.toList(Rococoa.cast(value, NSArray.class)).iterator();
            if(languages.hasNext()) {
                return languages.next();
            }
        }
        return super.locale();
    }

    @Override
    public List<String> applicationLocales() {
        return this.toList(bundle.localizations());
    }

    @Override
    public List<String> systemLocales() {
        // Language ordering in system preferences. Can be overridden
        // using the "AppleLanguages" user default
        return this.toList(NSLocale.preferredLanguages());
    }

    /**
     * Convert collection
     *
     * @param array List of properties
     * @return Collection
     */
    private List<String> toList(final NSArray array) {
        if(null == array) {
            return Collections.emptyList();
        }
        final List<String> list = new ArrayList<>();
        NSEnumerator ordered = array.objectEnumerator();
        NSObject next;
        while(((next = ordered.nextObject()) != null)) {
            list.add(next.toString());
        }
        return list;
    }
}
