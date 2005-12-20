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

import org.apache.log4j.Logger;

import java.util.HashMap;

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
            current = new ch.cyberduck.ui.cocoa.CDPreferencesImpl();
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

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public abstract void setProperty(String property, boolean v);

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public abstract void setProperty(String property, int v);

    /**
     * setting the default prefs values
     */
    public void setDefaults() {
        this.defaults = new HashMap();

        /**
         * The logging level (DEBUG, INFO, WARN, ERROR)
         */
        defaults.put("logging", "WARN");
        /**
         * How many times the application was launched
         */
        defaults.put("uses", "0");
        /**
         * True if donation dialog will be displayed at startup
         */
        defaults.put("donate", "true");
        defaults.put("mail", "mailto:feedback@cyberduck.ch");
        defaults.put("website.donate", "http://cyberduck.ch/donate/");
        defaults.put("website.update.xml", "http://update.cyberduck.ch/versionlist.xml");
        defaults.put("website.update", "http://update.cyberduck.ch/");
        defaults.put("website.home", "http://cyberduck.ch/");
        defaults.put("website.forum", "http://cyberduck.ch/forum/");
        defaults.put("website.help", "http://cyberduck.ch/help/");
        defaults.put("website.bug", "http://trac.cyberduck.ch/newticket/");

        defaults.put("rendezvous.enable", "true");

        defaults.put("growl.enable", "true");

        /**
         * Check for newer version on application launch in background thread
         */
        defaults.put("update.check", "true");

        /**
         * Bookmark drawer should be opened for new browser windows
         */
        defaults.put("bookmarkDrawer.isOpen", "false");

        /**
         * Current default browser view is outline view (0-List view, 1-Outline view, 2-Column view)
         */
        defaults.put("browser.view", "1");
        /**
         * Confirm closing the browsing connection
         */
        defaults.put("browser.confirmDisconnect", "true");
        /**
         * Display only one info panel and change information according to selection in browser
         */
        defaults.put("browser.info.isInspector", "true");

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
         * Close bookmark drawer upon opening a connection
         */
        defaults.put("browser.closeDrawer", "false");

        /**
         * Default editor
         */
        defaults.put("editor.name", "SubEthaEdit");
        defaults.put("editor.bundleIdentifier", "de.codingmonkeys.SubEthaEdit");
        defaults.put("editor.disabledFiles", "pdf ps exe bin jpeg jpg jp2 gif tif ico icns tiff bmp pict sgi tga png psd " +
                "hqx sea dmg zip sit tar gz tgz bz2 avi qtl bom pax pgp" +
                "mpg mpeg mp3 m4p m4a mov qt ram aiff aif wav wma jar war ear doc xls ppt");

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
        defaults.put("queue.download.folder", System.getProperty("user.home") + "/Desktop");
        /**
         * Action when duplicate file exists
         */
        defaults.put("queue.fileExists", "ask");

        defaults.put("queue.upload.changePermissions", "true");
        defaults.put("queue.upload.permissions.useDefault", "false");
        defaults.put("queue.upload.permissions.default", "rw-r--r--");
        defaults.put("queue.upload.preserveDate", "true");
        defaults.put("queue.upload.preserveDate.fallback", "false");

        defaults.put("queue.download.changePermissions", "false");
        defaults.put("queue.download.permissions.useDefault", "false");
        defaults.put("queue.download.permissions.default", "rw-r--r--");
        defaults.put("queue.download.preserveDate", "true");
        defaults.put("queue.download.updateIcon", "true");

        defaults.put("queue.sync.ignore.hour", "false");
        defaults.put("queue.sync.ignore.minute", "false");
        defaults.put("queue.sync.ignore.second", "true");
        defaults.put("queue.sync.ignore.millisecond", "true");
        defaults.put("queue.sync.timezone", java.util.TimeZone.getDefault().getID());

        defaults.put("queue.transformer.useTransformer", "false");
        defaults.put("queue.transformer.maxLength", "-1");
        defaults.put("queue.transformer.keepsFilenameExtensions", "true");
        defaults.put("queue.transformer.prefixString", "");
        defaults.put("queue.transformer.appendString", "");
        defaults.put("queue.transformer.replaceSearchString", "");
        defaults.put("queue.transformer.replaceWithString", "");
        defaults.put("queue.transformer.replaceAllOccurances", "true");
        defaults.put("queue.transformer.illegalCharacters", "");
        defaults.put("queue.transformer.substituteCharacter", "");

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

        defaults.put("connection.pool.max", "5"); // maximumum concurrent connections to the same host
        defaults.put("connection.pool.force", "false"); // force to close an existing connection if the pool is too small
        defaults.put("connection.pool.timeout", "180"); // in seconds
        defaults.put("connection.login.name", System.getProperty("user.name"));
        defaults.put("connection.login.useKeychain", "true");
        defaults.put("connection.buffer", "16384"); //in bytes, is 128kbit
        defaults.put("connection.buffer.default", "16384");
        defaults.put("connection.port.default", "21");
        defaults.put("connection.protocol.default", "ftp");
        defaults.put("connection.timeout", "30000");
        defaults.put("connection.keepalive", "false");
        defaults.put("connection.keepalive.interval", "30000");

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
        if (value == null) {
            throw new IllegalArgumentException("No property with key '" + property + "'");
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
