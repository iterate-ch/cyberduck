package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.PreferencesFactory;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * Concrete subclass using the Cocoa Preferences classes. The NSUserDefaults class is thread-safe.
 *
 * @version $Id$
 * @see ch.cyberduck.ui.cocoa.foundation.NSUserDefaults
 */
public class UserDefaultsPreferences extends Preferences {
    private static Logger log = Logger.getLogger(Preferences.class);

    public static void register() {
        PreferencesFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends PreferencesFactory {
        @Override
        protected Preferences create() {
            if(null == NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path")) {
                return new UserDefaultsPreferences();
            }
            return new UserDefaultsPortablePreferences();
        }
    }

    private NSUserDefaults props;

    @Override
    public Object getObject(final String property) {
        NSObject value = props.objectForKey(property);
        if(null == value) {
            return super.getObject(property);
        }
        return value;
    }

    @Override
    public void setProperty(final String property, final String value) {
        log.info("setProperty:" + property + "," + value);
        if(StringUtils.isNotEmpty(value)) {
            // Sets the value of the default identified by defaultName in the standard application domain.
            // Setting a default has no effect on the value returned by the objectForKey method if
            // the same key exists in a domain that precedes the application domain in the search list.
            this.props.setObjectForKey(NSString.stringWithString(value), property);
        }
        else {
            this.props.setObjectForKey(null, property);
        }
        this.save();
    }

    @Override
    public void deleteProperty(final String property) {
        log.debug("deleteProperty:" + property);
        this.props.removeObjectForKey(property);
        this.save();
    }

    /**
     * Overwrite the default values with user props if any.
     */
    @Override
    protected void load() {
        this.props = NSUserDefaults.standardUserDefaults();
    }

    /**
     * Properties that must be accessible in NSUserDefaults with default values
     */
    @Override
    protected void legacy() {
        _init("browser.view.autoexpand.useDelay");
        _init("browser.view.autoexpand.delay");

        _init("queue.maxtransfers");

        _init("connection.retry");
        _init("connection.retry.delay");
        _init("connection.timeout.seconds");

        _init("bookmark.icon.size");

        if(this.getBoolean("update.check")) {
            // Will override SUCheckAtStartup
            this.props.setInteger_forKey(new NSInteger(Long.parseLong(super.getProperty("update.check.interval"))),
                    "SUScheduledCheckInterval");
        }
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        Local APP_SUPPORT_DIR;
        if(null == NSBundle.mainBundle().objectForInfoDictionaryKey("application.support.path")) {
            APP_SUPPORT_DIR = LocalFactory.createLocal("~/Library/Application Support/Cyberduck");
        }
        else {
            APP_SUPPORT_DIR = LocalFactory.createLocal(NSBundle.mainBundle().objectForInfoDictionaryKey("application.support.path").toString());
        }
        APP_SUPPORT_DIR.mkdir(true);

        defaults.put("application.support.path", APP_SUPPORT_DIR.getAbbreviatedPath());
        defaults.put("application",
                NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleName").toString());
        defaults.put("version",
                NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleShortVersionString").toString());
    }

    /**
     * Setting default values that must be accessible using [NSUserDefaults standardUserDefaults]
     *
     * @param property
     */
    private void _init(final String property) {
        if(null == props.objectForKey(property)) {
            // Set the default value
            this.setProperty(property, super.getProperty(property));
        }
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
        this.props.synchronize();
    }

    @Override
    protected String locale() {
        String locale = "en";
        NSArray preferredLocalizations = NSBundle.mainBundle().preferredLocalizations();
        if(null == preferredLocalizations) {
            log.warn("No localizations found in main bundle");
            return locale;
        }
        if(preferredLocalizations.count().intValue() > 0) {
            locale = preferredLocalizations.objectAtIndex(new NSUInteger(0)).toString();
        }
        return locale;
    }
}