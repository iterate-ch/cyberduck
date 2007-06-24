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

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPTransferType;

import ch.cyberduck.ui.cocoa.CDBrowserTableDataSource;
import ch.cyberduck.ui.cocoa.CDPortablePreferencesImpl;
import ch.cyberduck.ui.cocoa.CDPreferencesImpl;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

    private Map defaults;

    static {
        System.setProperty("networkaddress.cache.ttl", "10");
        System.setProperty("networkaddress.cache.negative.ttl", "5");
    }

    private static final Object lock = new Object();

    /**
     * @return The singleton instance of me.
     */
    public static Preferences instance() {
        synchronized(lock) {
            if (null == current) {
                if(null == NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path")) {
                    current = new CDPreferencesImpl();
                }
                else {
                    current = new CDPortablePreferencesImpl();
                }
                current.load();
                current.setDefaults();
                current.legacy();
            }
            return current;
        }
    }

    /**
     * Updates any legacy custom set preferences which are not longer
     * valid as of this version
     */
    protected void legacy() {
        ;
    }

//    public abstract void addObserver(String property, PreferencesObserver observer);
//
//    public abstract void removeObserver(PreferencesObserver observer);

    /**
     * @param property The name of the property to overwrite
     * @param value    The new vlaue
     */
    public abstract void setProperty(String property, Object value);

    public abstract void deleteProperty(String property);

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public void setProperty(String property, boolean v) {
        this.setProperty(property, v ? String.valueOf(true) : String.valueOf(false));
    }

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public void setProperty(String property, int v) {
        this.setProperty(property, String.valueOf(v));
    }

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public void setProperty(String property, float v) {
        this.setProperty(property, String.valueOf(v));
    }

    /**
     * setting the default prefs values
     */
    protected void setDefaults() {
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
        defaults.put("logging", "ERROR");
        /**
         * How many times the application was launched
         */
        defaults.put("uses", "0");
        /**
         * True if donation dialog will be displayed before quit
         */
        defaults.put("donate.reminder", String.valueOf(true));

        defaults.put("defaulthandler.reminder", String.valueOf(true));

        defaults.put("mail.feedback", "mailto:feedback@cyberduck.ch");

        defaults.put("website.donate", "http://cyberduck.ch/donate/");
        defaults.put("website.update", "http://update.cyberduck.ch/");
        defaults.put("website.home", "http://cyberduck.ch/");
        defaults.put("website.forum", "http://forum.cyberduck.ch/");
        defaults.put("website.help", "http://cyberduck.ch/help/");
        defaults.put("website.bug", "http://trac.cyberduck.ch/newticket/");

        defaults.put("rendezvous.enable", String.valueOf(true));
        defaults.put("rendezvous.loopback.supress", String.valueOf(true));

        defaults.put("growl.enable", String.valueOf(true));

        /**
         * Current default browser view is outline view (0-List view, 1-Outline view, 2-Column view)
         */
        defaults.put("browser.view", "1");
        /**
         * Save browser sessions when quitting and restore upon relaunch
         */
        defaults.put("browser.serialize", String.valueOf(true));

        defaults.put("browser.view.autoexpand", String.valueOf(true));
        defaults.put("browser.view.autoexpand.useDelay", String.valueOf(true));
        defaults.put("browser.view.autoexpand.delay", "1.0"); // in seconds

        defaults.put("browser.openUntitled", String.valueOf(true));
        defaults.put("browser.defaultBookmark", NSBundle.localizedString("None", ""));

        defaults.put("browser.markInaccessibleFolders", String.valueOf(true));
        /**
         * Confirm closing the browsing connection
         */
        defaults.put("browser.confirmDisconnect", String.valueOf(true));
        /**
         * Display only one info panel and change information according to selection in browser
         */
        defaults.put("browser.info.isInspector", String.valueOf(true));

        defaults.put("browser.columnKind", String.valueOf(false));
        defaults.put("browser.columnSize", String.valueOf(true));
        defaults.put("browser.columnModification", String.valueOf(true));
        defaults.put("browser.columnOwner", String.valueOf(false));
        defaults.put("browser.columnPermissions", String.valueOf(false));

        defaults.put("browser.sort.column", CDBrowserTableDataSource.FILENAME_COLUMN);
        defaults.put("browser.sort.ascending", String.valueOf(true));

        defaults.put("browser.alternatingRows", String.valueOf(false));
        defaults.put("browser.verticalLines", String.valueOf(false));
        defaults.put("browser.horizontalLines", String.valueOf(true));
        /**
         * Show hidden files in browser by default
         */
        defaults.put("browser.showHidden", String.valueOf(false));
        defaults.put("browser.charset.encoding", "UTF-8");
        /**
         * Edit double clicked files instead of downloading
         */
        defaults.put("browser.doubleclick.edit", String.valueOf(false));
        /**
         * Rename files when return or enter key is pressed
         */
        defaults.put("browser.enterkey.rename", String.valueOf(true));

        /**
         * Enable inline editing in browser
         */
        defaults.put("browser.editable", String.valueOf(true));
        /**
         * Bookmark drawer should be opened for new browser windows
         */
        defaults.put("browser.bookmarkDrawer.isOpen", String.valueOf(false));
        defaults.put("browser.bookmarkDrawer.smallItems", String.valueOf(false));
        /**
         * Close bookmark drawer upon opening a connection
         */
        defaults.put("browser.closeDrawer", String.valueOf(false));
        /**
         * Warn before renaming files
         */
        defaults.put("browser.confirmMove", String.valueOf(false));

        /**
         * Default editor
         */
        defaults.put("editor.name", "TextMate");
        defaults.put("editor.bundleIdentifier", "com.macromates.textmate");

        defaults.put("filetype.text.regex",
                ".*\\.txt|.*\\.cgi|.*\\.htm|.*\\.html|.*\\.shtml|.*\\.xml|.*\\.xsl|.*\\.php|.*\\.php3|" +
                        ".*\\.js|.*\\.css|.*\\.asp|.*\\.java|.*\\.c|.*\\.cp|.*\\.cpp|.*\\.m|.*\\.h|.*\\.pl|.*\\.py|" +
                        ".*\\.rb|.*\\.sh");
        defaults.put("filetype.binary.regex",
                ".*\\.pdf|.*\\.ps|.*\\.exe|.*\\.bin|.*\\.jpeg|.*\\.jpg|.*\\.jp2|.*\\.gif|.*\\.tif|.*\\.ico|" +
                        ".*\\.icns|.*\\.tiff|.*\\.bmp|.*\\.pict|.*\\.sgi|.*\\.tga|.*\\.png|.*\\.psd|" +
                        ".*\\.hqx|.*\\.sea|.*\\.dmg|.*\\.zip|.*\\.sit|.*\\.tar|.*\\.gz|.*\\.tgz|.*\\.bz2|" +
                        ".*\\.avi|.*\\.qtl|.*\\.bom|.*\\.pax|.*\\.pgp|.*\\.mpg|.*\\.mpeg|.*\\.mp3|.*\\.m4p|" +
                        ".*\\.m4a|.*\\.mov|.*\\.avi|.*\\.qt|.*\\.ram|.*\\.aiff|.*\\.aif|.*\\.wav|.*\\.wma|" +
                        ".*\\.doc|.*\\.xls|.*\\.ppt");

        /**
         * Save bookmarks in ~/Library
         */
        defaults.put("favorites.save", String.valueOf(true));

        defaults.put("queue.openByDefault", String.valueOf(false));
        defaults.put("queue.save", String.valueOf(true));
        defaults.put("queue.removeItemWhenComplete", String.valueOf(false));
        /**
         * The maximum number of concurrent transfers
         */
        defaults.put("queue.maxtransfers", String.valueOf(5));

        /**
         * Open completed downloads
         */
        defaults.put("queue.postProcessItemWhenComplete", String.valueOf(false));
        defaults.put("queue.orderFrontOnStart", String.valueOf(true));
        defaults.put("queue.orderBackOnStop", String.valueOf(false));

        defaults.put("queue.download.folder", "~/Desktop");
        /**
         * Action when duplicate file exists
         */
        defaults.put("queue.download.fileExists", TransferAction.ACTION_CALLBACK);
        defaults.put("queue.upload.fileExists", TransferAction.ACTION_CALLBACK);
        /**
         * When triggered manually using 'Reload' in the Transfer window
         */
        defaults.put("queue.download.reload.fileExists", TransferAction.ACTION_CALLBACK);
        defaults.put("queue.upload.reload.fileExists", TransferAction.ACTION_CALLBACK);

        defaults.put("queue.upload.changePermissions", String.valueOf(true));
        defaults.put("queue.upload.permissions.useDefault", String.valueOf(false));
        defaults.put("queue.upload.permissions.file.default", String.valueOf(644));
        defaults.put("queue.upload.permissions.folder.default", String.valueOf(755));

        defaults.put("queue.upload.preserveDate", String.valueOf(true));

        defaults.put("queue.upload.skip.enable", String.valueOf(true));
        defaults.put("queue.upload.skip.regex.default",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS");
        defaults.put("queue.upload.skip.regex",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS");

        defaults.put("queue.download.changePermissions", String.valueOf(true));
        defaults.put("queue.download.permissions.useDefault", String.valueOf(false));
        defaults.put("queue.download.permissions.file.default", String.valueOf(644));
        defaults.put("queue.download.permissions.folder.default", String.valueOf(755));

        defaults.put("queue.download.preserveDate", String.valueOf(true));

        defaults.put("queue.download.skip.enable", String.valueOf(true));
        defaults.put("queue.download.skip.regex.default",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS");
        defaults.put("queue.download.skip.regex",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS");

        /**
         * Bandwidth throttle upload stream
         */
        defaults.put("queue.upload.bandwidth.bytes", String.valueOf(-1));
        /**
         * Bandwidth throttle download stream
         */
        defaults.put("queue.download.bandwidth.bytes", String.valueOf(-1));

        /**
         * While downloading, update the icon of the downloaded file as a progress indicator
         */
        defaults.put("queue.download.updateIcon", String.valueOf(true));
        /**
         * If size is equals, do not compare the timestamp
         */
        defaults.put("queue.sync.timestamp.ignore", String.valueOf("true"));

        //ftp properties
        defaults.put("ftp.anonymous.name", "anonymous");
        defaults.put("ftp.anonymous.pass", "cyberduck@example.net");

        defaults.put("ftp.connectmode", FTPConnectMode.PASV.toString());
        defaults.put("ftp.transfermode", FTPTransferType.BINARY.toString());

        /**
         * Line seperator to use for ASCII transfers
         */
        defaults.put("ftp.line.separator", "unix");
        /**
         * Send LIST -a
         */
        defaults.put("ftp.sendExtendedListCommand", String.valueOf(true));
        /**
         * Fallback to active or passive mode respectively
         */
        defaults.put("ftp.connectmode.fallback", String.valueOf(true));
        /**
         * Protect the data channel by default
         */
        defaults.put("ftp.tls.datachannel", "P"); //C
        /**
         * Still open connection if securing data channel fails
         */
        defaults.put("ftp.tls.datachannel.failOnError", String.valueOf(false));
        /**
         * Do not accept certificates that can't be found in the Keychain
         */
        defaults.put("ftp.tls.acceptAnyCertificate", String.valueOf(false));
        /**
         * Maximum concurrent connections to the same host
         * Unlimited by default
         */
        defaults.put("connection.host.max", String.valueOf(-1));
        /**
         * Default login name
         */
        defaults.put("connection.login.name", System.getProperty("user.name"));
        /**
         * Search for passphrases in Keychain
         */
        defaults.put("connection.login.useKeychain", String.valueOf(true));

        defaults.put("connection.buffer", String.valueOf(32768)); //in bytes
        defaults.put("connection.buffer.default", String.valueOf(32768));

        defaults.put("connection.port.default", String.valueOf(21));
        defaults.put("connection.protocol.default", "ftp");

        defaults.put("connection.timeout.seconds", String.valueOf(30));
        /**
         * Send no operation commands to the server
         */
        defaults.put("connection.keepalive", String.valueOf(false));
        defaults.put("connection.keepalive.interval", String.valueOf(30000));
        /**
         * Retry to connect after a I/O failure automatically
         */
        defaults.put("connection.retry", String.valueOf(1));
        defaults.put("connection.retry.delay", String.valueOf(10));
        /**
         * Try to resolve the hostname when entered in connection dialog
         */
        defaults.put("connection.hostname.check", String.valueOf(true)); //Check hostname reachability using NSNetworkDiagnostics
        /**
         * Normalize path names
         */
        defaults.put("path.normalize", String.valueOf(true));
        /**
         * Use the SFTP subsystem or a SCP channel for file transfers over SSH
         */
        defaults.put("ssh.transfer", Session.SFTP); // Session.SCP
        /**
         * Location of the openssh known_hosts file
         */
        defaults.put("ssh.knownhosts",  "~/.ssh/known_hosts");

        defaults.put("ssh.CSEncryption", "blowfish-cbc"); //client -> server encryption cipher
        defaults.put("ssh.SCEncryption", "blowfish-cbc"); //server -> client encryption cipher
        defaults.put("ssh.CSAuthentication", "hmac-md5"); //client -> server message authentication
        defaults.put("ssh.SCAuthentication", "hmac-md5"); //server -> client message authentication
        defaults.put("ssh.publickey", "ssh-rsa");
        defaults.put("ssh.compression", "none"); //zlib

        defaults.put("update.check", String.valueOf(true));
        final int DAY = 60*60*24;
        defaults.put("update.check.interval", String.valueOf(DAY)); // periodic update check in seconds
    }

    /**
     * Should be overriden by the implementation and only called if the property
     * can't be found in the users's defaults table
     *
     * @param property The property to query.
     * @return The value of the property
     */
    public Object getObject(String property) {
        Object value = defaults.get(property);
        if (null == value) {
            log.warn("No property with key '" + property + "'");
        }
        return value;
    }

    public String getProperty(String property) {
        return this.getObject(property).toString();
    }

    public int getInteger(String property) {
        return Integer.parseInt(this.getObject(property).toString());
    }

    public float getFloat(String property) {
        return Float.parseFloat(this.getObject(property).toString());
    }

    public double getDouble(String property) {
        return Double.parseDouble(this.getObject(property).toString());
    }

    public boolean getBoolean(String property) {
        String value = this.getObject(property).toString();
        if(value.equalsIgnoreCase(String.valueOf(true))) {
            return true;
        }
        if(value.equalsIgnoreCase(String.valueOf(false))) {
            return false;
        }
        try {
            return value.equalsIgnoreCase("yes") || Integer.parseInt(value) == 1;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * Store preferences; ensure perisistency
     */
    public abstract void save();

    /**
     * Overriding the default values with prefs from the last session.
     */
    protected abstract void load();
}
