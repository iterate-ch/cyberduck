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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
            current.legacy();
        }
        return current;
    }

    /**
     * Updates any legacy custom set preferences which are not longer
     * valid as of this version
     */
    protected void legacy() {
        ;
    }

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
        this.setProperty(property, v ? "true" : "false");
    }

    /**
     * @param property The name of the property to overwrite
     * @param v        The new vlaue
     */
    public void setProperty(String property, int v) {
        this.setProperty(property, String.valueOf(v));
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
        defaults.put("logging", "ERROR");
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
        /**
         * Save browser sessions when quitting and restore upon relaunch
         */
        defaults.put("browser.serialize", "true");

        defaults.put("browser.view.autoexpand", "true");
        defaults.put("browser.view.autoexpand.useDelay", "true");
        defaults.put("browser.view.autoexpand.delay", "1.0"); // in seconds

        defaults.put("browser.openUntitled", "true");
        defaults.put("browser.defaultBookmark", NSBundle.localizedString("None", ""));

        defaults.put("browser.markInaccessibleFolders", "true");
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
         * Edit double clicked files instead of downloading
         */
        defaults.put("browser.doubleclick.edit", "false");
        /**
         * Rename files when return or enter key is pressed
         */
        defaults.put("browser.enterkey.rename", "true");

        /**
         * Enable inline editing in browser
         */
        defaults.put("browser.editable", "true");
        /**
         * Bookmark drawer should be opened for new browser windows
         */
        defaults.put("browser.bookmarkDrawer.isOpen", "false");
        defaults.put("browser.bookmarkDrawer.smallItems", "false");
        /**
         * Close bookmark drawer upon opening a connection
         */
        defaults.put("browser.closeDrawer", "false");

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

        defaults.put("queue.download.folder", "~/Desktop");
        /**
         * Action when duplicate file exists
         */
        defaults.put("queue.download.fileExists", Validator.ASK);
        defaults.put("queue.upload.fileExists", Validator.ASK);
        /**
         * When triggered manually using 'Reload' in the
         *  window
         */
        defaults.put("queue.download.reload.fileExists", Validator.ASK);
        defaults.put("queue.upload.reload.fileExists", Validator.ASK);

        defaults.put("queue.upload.changePermissions", "true");
        /**
         * If false, apply the permissions of the local file
         */
        defaults.put("queue.upload.permissions.useDefault", "false");
        defaults.put("queue.upload.permissions.default", "rw-r--r--");
        defaults.put("queue.upload.preserveDate", "true");
        defaults.put("queue.upload.preserveDate.fallback", "false");
        defaults.put("queue.upload.skip.enable", "true");
        defaults.put("queue.upload.skip.regex.default",
                ".*~\\..*|\\.DS_Store|.*\\.svn|CVS");
        defaults.put("queue.upload.skip.regex",
                ".*~\\..*|\\.DS_Store|.*\\.svn|CVS");

        defaults.put("queue.download.changePermissions", "true");
        defaults.put("queue.download.permissions.useDefault", "false");
        defaults.put("queue.download.permissions.default", "rw-r--r--");
        defaults.put("queue.download.preserveDate", "true");
        defaults.put("queue.download.skip.enable", "true");
        defaults.put("queue.download.skip.regex.default",
                ".*~\\..*|\\.DS_Store|.*\\.svn|CVS");
        defaults.put("queue.download.skip.regex",
                ".*~\\..*|\\.DS_Store|.*\\.svn|CVS");
        /**
         * While downloading, update the icon of the downloaded file as a progress indicator
         */
        defaults.put("queue.download.updateIcon", "true");

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
        defaults.put("ftp.sendExtendedListCommand", "true");
        /**
         * Fallback to active or passive mode respectively
         */
        defaults.put("ftp.connectmode.fallback", "true");
        /**
         * Protect the data channel by default
         */
        defaults.put("ftp.tls.datachannel", "P"); //C
        /**
         * Still open connection if securing data channel fails
         */
        defaults.put("ftp.tls.datachannel.failOnError", "false");
        /**
         * Do not accept certificates that can't be found in the Keychain
         */
        defaults.put("ftp.tls.acceptAnyCertificate", "false");
        /**
         * Maximum concurrent connections to the same host
         * Unlimited by default
         */
        defaults.put("connection.host.max", "-1");
        /**
         * Default login name
         */
        defaults.put("connection.login.name", System.getProperty("user.name"));
        /**
         * Search for passphrases in Keychain
         */
        defaults.put("connection.login.useKeychain", "true");

        defaults.put("connection.buffer", "32768"); //in bytes, is 256kbit
        defaults.put("connection.buffer.default", "32768");

        defaults.put("connection.port.default", "21");
        defaults.put("connection.protocol.default", "ftp");

        defaults.put("connection.timeout", "30000"); //in milliseconds
        /**
         * Send no operation commands to the server
         */
        defaults.put("connection.keepalive", "false");
        defaults.put("connection.keepalive.interval", "30000");
        /**
         * Try to resolve the hostname when entered in connection dialog
         */
        defaults.put("connection.hostname.check", "true"); //Check hostname reachability using NSNetworkDiagnostics
        /**
         * Normalize path names
         */
        defaults.put("path.normalize", "true");
        /**
         * The permission to apply when creating a new folder
         */
        defaults.put("permission.directory.default", "755");
        /**
         * The permission to apply when creating a new plain file
         */
        defaults.put("permission.file.default", "644");
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
    }

    /**
     * Should be overriden by the implementation and only called if the property
     * can't be found in the users's defaults table
     *
     * @param property The property to query.
     * @return The value of the property
     */
    public Object getObject(String property) {
        String value = (String) defaults.get(property);
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

    public boolean getBoolean(String property) {
        String value = this.getObject(property).toString();
        try {
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || Integer.parseInt(value) == 1;
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
    public abstract void load();
}
