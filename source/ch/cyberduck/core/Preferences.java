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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.BrowserTableDataSource;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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

    protected Map<String, String> defaults
            = new HashMap<String, String>();

    /**
     * TTL for DNS queries
     */
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
            if(null == current) {
                current = PreferencesFactory.createPreferences();
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

    public void setProperty(String property, long v) {
        this.setProperty(property, String.valueOf(v));
    }

    public void setProperty(String property, double v) {
        this.setProperty(property, String.valueOf(v));
    }

    /**
     * setting the default prefs values
     */
    protected void setDefaults() {
        defaults.put("tmp.dir", System.getProperty("java.io.tmpdir"));

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
        defaults.put("donate.reminder", String.valueOf(-1));
        defaults.put("donate.reminder.interval", String.valueOf(30)); // in days
        defaults.put("donate.reminder.date", String.valueOf(new Date(0).getTime()));

        defaults.put("defaulthandler.reminder", String.valueOf(true));

        defaults.put("mail.feedback", "mailto:feedback@cyberduck.ch");

        defaults.put("website.donate", "http://cyberduck.ch/donate/");
        defaults.put("website.home", "http://cyberduck.ch/");
        defaults.put("website.forum", "http://forum.cyberduck.ch/");
        defaults.put("website.help", "http://help.cyberduck.ch/" + this.locale());
        defaults.put("website.bug", "http://trac.cyberduck.ch/newticket/");

        defaults.put("rendezvous.enable", String.valueOf(true));
        defaults.put("rendezvous.loopback.supress", String.valueOf(true));

        defaults.put("growl.enable", String.valueOf(true));
        defaults.put("growl.limit", String.valueOf(10));

        /**
         * Normalize path names
         */
        defaults.put("path.normalize", String.valueOf(true));
        defaults.put("path.normalize.unicode", String.valueOf(false));

        defaults.put("local.symboliclink.resolve", String.valueOf(true));

        /**
         * Maximum number of directory listings to cache using a most recently used implementation
         */
        defaults.put("browser.cache.size", String.valueOf(1000));
        defaults.put("transfer.cache.size", String.valueOf(50));
        defaults.put("icon.cache.size", String.valueOf(50));

        /**
         * Caching NS* proxy instances.
         */
        defaults.put("browser.model.cache.size", String.valueOf(200));
        defaults.put("bookmark.model.cache.size", String.valueOf(50));
        defaults.put("queue.model.cache.size", String.valueOf(20));

        /**
         * Current default browser view is outline view (0-List view, 1-Outline view, 2-Column view)
         */
        defaults.put("browser.view", "1");
        /**
         * Save browser sessions when quitting and restore upon relaunch
         */
        defaults.put("browser.serialize", String.valueOf(true));

        defaults.put("browser.font.size", String.valueOf(12f));

        defaults.put("browser.view.autoexpand", String.valueOf(true));
        defaults.put("browser.view.autoexpand.useDelay", String.valueOf(true));
        defaults.put("browser.view.autoexpand.delay", "1.0"); // in seconds

        defaults.put("browser.hidden.regex", "\\..*");

        defaults.put("browser.openUntitled", String.valueOf(true));
        defaults.put("browser.defaultBookmark", Locale.localizedString("None"));

        defaults.put("browser.markInaccessibleFolders", String.valueOf(true));
        /**
         * Confirm closing the browsing connection
         */
        defaults.put("browser.confirmDisconnect", String.valueOf(false));
        defaults.put("browser.disconnect.showBookmarks", String.valueOf(false));

        /**
         * Display only one info panel and change information according to selection in browser
         */
        defaults.put("browser.info.isInspector", String.valueOf(true));

        defaults.put("browser.columnKind", String.valueOf(false));
        defaults.put("browser.columnSize", String.valueOf(true));
        defaults.put("browser.columnModification", String.valueOf(true));
        defaults.put("browser.columnOwner", String.valueOf(false));
        defaults.put("browser.columnGroup", String.valueOf(false));
        defaults.put("browser.columnPermissions", String.valueOf(false));

        defaults.put("browser.sort.column", BrowserTableDataSource.FILENAME_COLUMN);
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
         * Warn before renaming files
         */
        defaults.put("browser.confirmMove", String.valueOf(false));

        defaults.put("browser.logDrawer.isOpen", String.valueOf(false));
        defaults.put("browser.logDrawer.size.height", String.valueOf(200));

        defaults.put("info.toggle.permission", String.valueOf(1));
        defaults.put("info.toggle.distribution", String.valueOf(0));
        defaults.put("info.toggle.s3", String.valueOf(0));

        defaults.put("connection.toggle.options", String.valueOf(0));
        defaults.put("bookmark.toggle.options", String.valueOf(0));

        defaults.put("alert.toggle.transcript", String.valueOf(0));

        defaults.put("transfer.toggle.details", String.valueOf(1));

        /**
         * Default editor
         */
        defaults.put("editor.name", "TextMate");
        defaults.put("editor.bundleIdentifier", "com.macromates.textmate");
        /**
         * Editor for the current selected file. Used to set the shortcut key in the menu delegate
         */
        defaults.put("editor.kqueue.enable", "false");
        defaults.put("editor.tmp.directory", System.getProperty("java.io.tmpdir"));

        defaults.put("editor.file.trash", "true");

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
                        ".*\\.doc|.*\\.iso|.*\\.xls|.*\\.ppt");

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

        if(LocalFactory.createLocal("~/Downloads").exists()) {
            // For 10.5 this usually exists and should be preferrred
            defaults.put("queue.download.folder", "~/Downloads");
        }
        else {
            defaults.put("queue.download.folder", "~/Desktop");
        }
        /**
         * Action when duplicate file exists
         */
        defaults.put("queue.download.fileExists", TransferAction.ACTION_CALLBACK.toString());
        defaults.put("queue.upload.fileExists", TransferAction.ACTION_CALLBACK.toString());
        /**
         * When triggered manually using 'Reload' in the Transfer window
         */
        defaults.put("queue.download.reload.fileExists", TransferAction.ACTION_CALLBACK.toString());
        defaults.put("queue.upload.reload.fileExists", TransferAction.ACTION_CALLBACK.toString());

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
                ".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs");
        defaults.put("queue.download.skip.regex",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs");

        defaults.put("queue.download.quarantine", String.valueOf(true));
        defaults.put("queue.download.wherefrom", String.valueOf(true));

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
         * Default synchronize action selected in the sync dialog
         */
        defaults.put("queue.sync.action.default", SyncTransfer.ACTION_UPLOAD.toString());
        defaults.put("queue.prompt.action.default", TransferAction.ACTION_OVERWRITE.toString());

        defaults.put("queue.logDrawer.isOpen", String.valueOf(false));
        defaults.put("queue.logDrawer.size.height", String.valueOf(200));

        defaults.put("ftp.transfermode", com.enterprisedt.net.ftp.FTPTransferType.BINARY.toString());
        /**
         * Line seperator to use for ASCII transfers
         */
        defaults.put("ftp.line.separator", "unix");
        /**
         * Send LIST -a
         */
        defaults.put("ftp.sendExtendedListCommand", String.valueOf(true));
        defaults.put("ftp.sendStatListCommand", String.valueOf(true));
        defaults.put("ftp.sendMlsdListCommand", String.valueOf(true));

        /**
         * Fallback to active or passive mode respectively
         */
        defaults.put("ftp.connectmode.fallback", String.valueOf(true));
        /**
         * Protect the data channel by default. For TLS, the data connection
         * can have one of two security levels.
         1) Clear (requested by 'PROT C')
         2) Private (requested by 'PROT P')
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
         * If the parser should not trim whitespace from filenames
         */
        defaults.put("ftp.parser.whitespaceAware", String.valueOf(true));

        /**
         * Try to determine the timezone automatically using timestamp comparison from MLST and LIST
         */
        defaults.put("ftp.timezone.auto", String.valueOf(false));
        defaults.put("ftp.timezone.default", TimeZone.getDefault().getID());

        /**
         * Default bucket location
         */
        defaults.put("s3.location", "US");
        /**
         * Validaty for public S3 URLs
         */
        defaults.put("s3.url.expire.seconds", String.valueOf(24 * 60 * 60)); //expiry time for public URL
        /**
         * Generate publicy accessible URLs when copying URLs in S3 browser
         */
        defaults.put("s3.url.public", String.valueOf(false));
        defaults.put("s3.tls.acceptAnyCertificate", String.valueOf(false));

//        defaults.put("s3.crypto.algorithm", "PBEWithMD5AndDES");
        /**
         * A prefix to apply to log file names
         */
        defaults.put("s3.logging.prefix", "logs/");
        defaults.put("cloudfront.logging.prefix", "logs/");

        final int MONTH = 60 * 60 * 24 * 30; //30 days in seconds
        defaults.put("s3.cache.seconds", String.valueOf(MONTH));

        defaults.put("webdav.followRedirects", String.valueOf(true));
        defaults.put("webdav.tls.acceptAnyCertificate", String.valueOf(false));

        defaults.put("cf.tls.acceptAnyCertificate", String.valueOf(false));
        defaults.put("cf.list.limit", String.valueOf(10000));

        /**
         * NTLM Windows Domain
         */
        defaults.put("webdav.ntlm.domain", "");

        /**
         * Maximum concurrent connections to the same host
         * Unlimited by default
         */
        defaults.put("connection.host.max", String.valueOf(-1));
        /**
         * Default login name
         */
        defaults.put("connection.login.name", System.getProperty("user.name"));
        defaults.put("connection.login.anon.name", "anonymous");
        defaults.put("connection.login.anon.pass", "cyberduck@example.net");
        /**
         * Search for passphrases in Keychain
         */
        defaults.put("connection.login.useKeychain", String.valueOf(true));
        /**
         * Add to Keychain option is checked in login prompt
         */
        defaults.put("connection.login.addKeychain", String.valueOf(true));

        defaults.put("connection.port.default", String.valueOf(21));
        defaults.put("connection.protocol.default", "ftp");

        defaults.put("connection.timeout.seconds", String.valueOf(30));
        /**
         * Retry to connect after a I/O failure automatically
         */
        defaults.put("connection.retry", String.valueOf(1));
        defaults.put("connection.retry.delay", String.valueOf(10));

        defaults.put("connection.hostname.default", "");
        /**
         * Try to resolve the hostname when entered in connection dialog
         */
        defaults.put("connection.hostname.check", String.valueOf(true)); //Check hostname reachability using NSNetworkDiagnostics
        defaults.put("connection.hostname.idn", String.valueOf(true)); //Convert hostnames to Punycode

        /**
         * java.net.preferIPv6Addresses
         */
        defaults.put("connection.dns.ipv6", String.valueOf(false));

        defaults.put("transcript.length", String.valueOf(1000));

        /**
         * Read favicon from Web URL
         */
        defaults.put("bookmark.favicon.download", String.valueOf(true));

        defaults.put("bookmark.icon.size", String.valueOf(32));

        /**
         * Use the SFTP subsystem or a SCP channel for file transfers over SSH
         */
        defaults.put("ssh.transfer", Protocol.SFTP.getIdentifier()); // Session.SCP
        /**
         * Location of the openssh known_hosts file
         */
        defaults.put("ssh.knownhosts", "~/.ssh/known_hosts");

        defaults.put("ssh.CSEncryption", "blowfish-cbc"); //client -> server encryption cipher
        defaults.put("ssh.SCEncryption", "blowfish-cbc"); //server -> client encryption cipher
        defaults.put("ssh.CSAuthentication", "hmac-md5"); //client -> server message authentication
        defaults.put("ssh.SCAuthentication", "hmac-md5"); //server -> client message authentication
        defaults.put("ssh.publickey", "ssh-rsa");
        defaults.put("ssh.compression", "none"); //zlib

        defaults.put("archive.default", "tar.gz");

        /**
         * Archiver
         */
        defaults.put("archive.command.create.tar", "tar -cvpPf {0}.tar {1}");
        defaults.put("archive.command.create.tar.gz", "tar -czvpPf {0}.tar.gz {1}");
        defaults.put("archive.command.create.tar.bz2", "tar -cjvpPf {0}.tar.bz2 {1}");
        defaults.put("archive.command.create.zip", "zip -rv {0}.zip {1}");
        defaults.put("archive.command.create.gz", "gzip -rv {1}");
        defaults.put("archive.command.create.bz2", "bzip2 -zvk {1}");

        /**
         * Unarchiver
         */
        defaults.put("archive.command.expand.tar", "tar -xvpPf {0} -C {1}");
        defaults.put("archive.command.expand.tar.gz", "tar -xzvpPf {0} -C {1}");
        defaults.put("archive.command.expand.tar.bz2", "tar -xjvpPf {0} -C {1}");
        defaults.put("archive.command.expand.zip", "unzip -n {0} -d {1}");
        defaults.put("archive.command.expand.gz", "gzip -dv {0}");
        defaults.put("archive.command.expand.bz2", "bzip2 -dvk {0}");

        defaults.put("update.check", String.valueOf(true));
        final int DAY = 60 * 60 * 24;
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
        if(null == value) {
            log.warn("No property with key '" + property + "'");
        }
        return value;
    }

    public String getProperty(String property) {
        final Object v = this.getObject(property);
        if(null == v) {
            return null;
        }
        return v.toString();
    }

    public int getInteger(String property) {
        final Object v = this.getObject(property);
        if(null == v) {
            return -1;
        }
        return Integer.parseInt(v.toString());
    }

    public float getFloat(String property) {
        final Object v = this.getObject(property);
        if(null == v) {
            return -1;
        }
        return Float.parseFloat(v.toString());
    }

    public long getLong(String property) {
        final Object v = this.getObject(property);
        if(null == v) {
            return -1;
        }
        return Long.parseLong(v.toString());
    }

    public double getDouble(String property) {
        final Object v = this.getObject(property);
        if(null == v) {
            return -1;
        }
        return Double.parseDouble(v.toString());
    }

    public boolean getBoolean(String property) {
        final Object v = this.getObject(property);
        if(null == v) {
            return false;
        }
        String value = v.toString();
        if(value.equalsIgnoreCase(String.valueOf(true))) {
            return true;
        }
        if(value.equalsIgnoreCase(String.valueOf(false))) {
            return false;
        }
        if(value.equalsIgnoreCase(String.valueOf(1))) {
            return true;
        }
        if(value.equalsIgnoreCase(String.valueOf(0))) {
            return false;
        }
        try {
            return value.equalsIgnoreCase("yes");
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

    /**
     * @return The preferred locale of all available in this application bundle
     *         for the currently logged in user
     */
    protected abstract String locale();
}
