package ch.cyberduck.core;

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

import ch.cyberduck.ui.cocoa.CDBrowserTableDataSource;
import ch.cyberduck.ui.cocoa.CDPreferencesImpl;
import ch.cyberduck.ui.cocoa.CDPortablePreferencesImpl;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.io.File;

/**
 * Holding all application preferences. Default values get overwritten when loading
 * the <code>PREFERENCES_FILE</code>.
 * Singleton class.
 *
 * @version $Id$
 */
public abstract class Preferences {
    private static Logger log = Logger.getLogger(Preferences.class);

    private static Preferences current = null;

    private HashMap defaults;

    static {
        System.setProperty("networkaddress.cache.ttl", "10");
        System.setProperty("networkaddress.cache.negative.ttl", "5");
    }

    /**
     * @return The singleton instance of me.
     */
    public static Preferences instance() {
        if (null == current) {
            if(null == NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path")) {
                current = new CDPreferencesImpl();
            }
            else {
                current = new CDPortablePreferencesImpl();
            }
            current.setDefaults();
            current.load();
        }
        return current;
    }

    /**
     * @param property The name of the property to overwrite
     * @param value    The new vlaue
     */
    public abstract void setProperty(String property, String value);

    public abstract void deleteProperty(String property);

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public void setProperty(String property, boolean v) {
        if (log.isDebugEnabled()) {
            log.debug("setProperty(" + property + ", " + v + ")");
        }
        String value = "false";
        if (v) {
            value = "true";
        }
        this.setProperty(property, value);
    }

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public void setProperty(String property, int v) {
        if (log.isDebugEnabled()) {
            log.debug("setProperty(" + property + ", " + v + ")");
        }
        String value = String.valueOf(v);
        this.setProperty(property, value);
    }

    /**
     * setting the default prefs values
     */
    public void setDefaults() {
        this.defaults = new HashMap();

        File APP_SUPPORT_DIR = null;
        if(null == NSBundle.mainBundle().objectForInfoDictionaryKey("application.support.path")) {
            APP_SUPPORT_DIR = new File(
                    NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck"));
            APP_SUPPORT_DIR.mkdirs();
        }
        else {
            APP_SUPPORT_DIR = new File(
                    NSPathUtilities.stringByExpandingTildeInPath(
                            (String)NSBundle.mainBundle().objectForInfoDictionaryKey("application.support.path")));
        }
        APP_SUPPORT_DIR.mkdirs();
        
        defaults.put("application.support.path", APP_SUPPORT_DIR.getAbsolutePath());

        /**
         * The logging level (DEBUG, INFO, WARN, ERROR)
         */
        defaults.put("logging", "WARN");
        /**
         * How many times the application was launched
         */
        defaults.put("uses", "0");
        /**
         * True if donation dialog will be displayed before quit
         */
        defaults.put("donate.reminder", "true");

        defaults.put("mail.feedback", "mailto:feedback@cyberduck.ch");

        defaults.put("website.donate", "http://cyberduck.ch/donate/");
        defaults.put("website.update", "http://update.cyberduck.ch/");
        defaults.put("website.home", "http://cyberduck.ch/");
        defaults.put("website.forum", "http://forum.cyberduck.ch/");
        defaults.put("website.help", "http://cyberduck.ch/help/");
        defaults.put("website.bug", "http://trac.cyberduck.ch/newticket/");

        defaults.put("rendezvous.enable", "true");
        defaults.put("rendezvous.loopback.supress", "false");

        defaults.put("growl.enable", "true");

        /**
         * Current default browser view is outline view (0-List view, 1-Outline view, 2-Column view)
         */
        defaults.put("browser.view", "1");
        defaults.put("browser.view.autoexpand", "true");
//        defaults.put("browser.view.autoexpand.delay", "1000"); // in milliseconds
        defaults.put("browser.openUntitled", "true");
        defaults.put("browser.defaultBookmark", NSBundle.localizedString("None", ""));

        /**
         * Confirm closing the browsing connection
         */
        defaults.put("browser.confirmDisconnect", "true");
        /**
         * Display only one info panel and change information according to selection in browser
         */
        defaults.put("browser.info.isInspector", "true");

        defaults.put("browser.columnKind", "false");
        defaults.put("browser.columnSize", "true");
        defaults.put("browser.columnModification", "true");
        defaults.put("browser.columnOwner", "false");
        defaults.put("browser.columnPermissions", "false");

        defaults.put("browser.sort.column", CDBrowserTableDataSource.FILENAME_COLUMN);
        defaults.put("browser.sort.ascending", "true");

        defaults.put("browser.alternatingRows", "false");
        defaults.put("browser.verticalLines", "false");
        defaults.put("browser.horizontalLines", "true");
        /**
         * Show hidden files in browser by default
         */
        defaults.put("browser.showHidden", "false");
        defaults.put("browser.charset.encoding", "UTF-8");
        /**
         * Edit double clicked files in browser instead of downloading
         */
        defaults.put("browser.doubleclick.edit", "false");

        /**
         * Enable inline editing in browser
         */
        defaults.put("browser.editable", "true");
        /**
         * Bookmark drawer should be opened for new browser windows
         */
        defaults.put("browser.bookmarkDrawer.isOpen", "false");
        /**
         * Close bookmark drawer upon opening a connection
         */
        defaults.put("browser.closeDrawer", "false");
        /**
         * Open log drawer in browser window upon error
         */
        defaults.put("browser.logDrawer.openOnError", "true");
        defaults.put("browser.logDrawer.isOpen", "false");

        /**
         * Default editor
         */
        defaults.put("editor.name", "TextMate");
        defaults.put("editor.bundleIdentifier", "com.macromates.textmate");
        defaults.put("editor.disabledFiles", "pdf ps exe bin jpeg jpg jp2 gif tif ico icns tiff bmp pict sgi tga png psd " +
                "hqx sea dmg zip sit tar gz tgz bz2 avi qtl bom pax pgp" +
                "mpg mpeg mp3 m4p m4a mov avi qt ram aiff aif wav wma jar war ear doc xls ppt");

        /**
         * Save bookmarks in ~/Library
         */
        defaults.put("favorites.save", "true");

        defaults.put("queue.openByDefault", "false");
        defaults.put("queue.save", "true");
        defaults.put("queue.removeItemWhenComplete", "false");

        /**
         * Open completed downloads
         */
        defaults.put("queue.postProcessItemWhenComplete", "false");
        defaults.put("queue.orderFrontOnStart", "true");
        defaults.put("queue.orderBackOnStop", "false");
        /**
         * Open log drawer in transfer window upon error
         */
        defaults.put("queue.logDrawer.openOnError", "true");
        defaults.put("queue.logDrawer.isOpen", "false");

        defaults.put("queue.download.folder", System.getProperty("user.home") + "/Desktop");
        /**
         * Action when duplicate file exists
         */
        defaults.put("queue.download.fileExists", Validator.ASK);
        defaults.put("queue.upload.fileExists", Validator.ASK);

        defaults.put("queue.upload.changePermissions", "true");
        /**
         * If false, apply the permissions of the local file
         */
        defaults.put("queue.upload.permissions.useDefault", "false");
        defaults.put("queue.upload.permissions.default", "rw-r--r--");
        defaults.put("queue.upload.preserveDate", "true");
        defaults.put("queue.upload.preserveDate.fallback", "false");
        defaults.put("queue.upload.skip", ".DS_Store");

        defaults.put("queue.download.changePermissions", "true");
        /**
         * If false, apply the permissions of the remote file
         */
        defaults.put("queue.download.permissions.useDefault", "false");
        defaults.put("queue.download.permissions.default", "rw-r--r--");
        defaults.put("queue.download.preserveDate", "true");
        defaults.put("queue.download.updateIcon", "true");
        defaults.put("queue.download.skip", ".DS_Store");

        //ftp properties
        defaults.put("ftp.anonymous.name", "anonymous");
        defaults.put("ftp.anonymous.pass", "cyberduck@example.net");
        defaults.put("ftp.connectmode", "passive");
        defaults.put("ftp.transfermode", "binary");
        defaults.put("ftp.transfermode.ascii.extensions", "txt cgi htm html shtml xml xsl php php3 js css asp java c cp cpp m h pl py rb sh");
        defaults.put("ftp.line.separator", "unix");
        /**
         * Send SYST
         */
        defaults.put("ftp.sendSystemCommand", "true");
        /**
         * Send LIST -a
         */
        defaults.put("ftp.sendExtendedListCommand", "true");

        defaults.put("ftp.tls.datachannel", "P"); //C
        defaults.put("ftp.tls.datachannel.failOnError", "false");
        defaults.put("ftp.tls.acceptAnyCertificate", "false");

        defaults.put("connection.pool.max", "10"); // maximumum concurrent connections to the same host
        defaults.put("connection.pool.force", "false"); // force to close an existing connection if the pool is too small
        defaults.put("connection.pool.timeout", "180"); // in seconds
        defaults.put("connection.login.name", System.getProperty("user.name"));
        defaults.put("connection.login.useKeychain", "true");
        defaults.put("connection.buffer", "16384"); //in bytes, is 128kbit
        defaults.put("connection.buffer.default", "16384");
        defaults.put("connection.port.default", "21");
        defaults.put("connection.protocol.default", "ftp");
        defaults.put("connection.timeout", "30000"); //in milliseconds
        defaults.put("connection.keepalive", "false");
        defaults.put("connection.keepalive.interval", "30000");

        defaults.put("connection.hostname.check", "true"); //Check hostname reachability using NSNetworkDiagnostics

        /**
         * Locatoin of the openssh known_hosts file
         */
        defaults.put("ssh.knownhosts", System.getProperty("user.home") + "/.ssh/known_hosts");

        defaults.put("ssh.CSEncryption", "blowfish-cbc"); //client -> server encryption cipher
        defaults.put("ssh.SCEncryption", "blowfish-cbc"); //server -> client encryption cipher
        defaults.put("ssh.CSAuthentication", "hmac-md5"); //client -> server message authentication
        defaults.put("ssh.SCAuthentication", "hmac-md5"); //server -> client message authentication
        defaults.put("ssh.publickey", "ssh-rsa");
        defaults.put("ssh.compression", "none"); //zlib
    }

    /**
     * Should be overriden by the implementation and only called if the property
     * can't be found in the users's defaults table
     *
     * @param property The property to query.
     * @return The value of the property
     */
    public String getProperty(String property) {
        String value = (String) defaults.get(property);
        if (null == value) {
            log.warn("No property with key '" + property + "'");
        }
        return value;
    }

    public int getInteger(String property) {
        return Integer.parseInt(this.getProperty(property));
    }

    public boolean getBoolean(String property) {
        return this.getProperty(property).equals("true");
    }

    /**
     * Store preferences; ensure perisistency
     */
    public abstract void save();

    /**
     * Overriding the default values with prefs from the last session.
     */
    public abstract void load();
}
