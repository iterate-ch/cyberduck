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

import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLocale;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledSleepPreventer;
import ch.cyberduck.core.DisabledTerminalService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.aquaticprime.DonationKeyFactory;
import ch.cyberduck.core.date.DefaultUserDateFormatter;
import ch.cyberduck.core.diagnostics.DefaultInetAddressReachability;
import ch.cyberduck.core.formatter.DecimalSizeFormatter;
import ch.cyberduck.core.io.watchservice.NIOEventWatchService;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.local.DefaultWorkingDirectoryFinder;
import ch.cyberduck.core.local.DisabledApplicationBadgeLabeler;
import ch.cyberduck.core.local.DisabledApplicationFinder;
import ch.cyberduck.core.local.DisabledApplicationLauncher;
import ch.cyberduck.core.local.DisabledBrowserLauncher;
import ch.cyberduck.core.local.DisabledIconService;
import ch.cyberduck.core.local.DisabledQuarantineService;
import ch.cyberduck.core.local.NativeLocalTrashFeature;
import ch.cyberduck.core.local.NullFileDescriptor;
import ch.cyberduck.core.local.NullLocalSymlinkFeature;
import ch.cyberduck.core.local.WorkingDirectoryFinderFactory;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.random.DefaultSecureRandomProvider;
import ch.cyberduck.core.resources.DisabledIconCache;
import ch.cyberduck.core.serializer.impl.dd.HostPlistReader;
import ch.cyberduck.core.serializer.impl.dd.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.dd.PlistSerializer;
import ch.cyberduck.core.serializer.impl.dd.PlistWriter;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.dd.TransferPlistReader;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.DisabledActionOperationBatcher;
import ch.cyberduck.core.threading.DisabledAlertCallback;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.updater.DisabledPeriodicUpdater;
import ch.cyberduck.core.urlhandler.DisabledSchemeHandler;
import ch.cyberduck.core.vault.DisabledVault;
import ch.cyberduck.core.webloc.InternetShortcutFileWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Holding all application preferences. Default values get overwritten when loading
 * the <code>PREFERENCES_FILE</code>.
 * Singleton class.
 */
public abstract class Preferences {
    private static final Logger log = Logger.getLogger(Preferences.class);

    protected final Map<String, String> defaults
            = new HashMap<String, String>();

    /*
      TTL for DNS queries
     */
    static {
        Security.setProperty("networkaddress.cache.ttl", "10");
        Security.setProperty("networkaddress.cache.negative.ttl", "5");
    }

    /**
     * Called after the defaults have been set.
     */
    protected void post() {
        // Ticket #2539
        if(this.getBoolean("connection.dns.ipv6")) {
            System.setProperty("java.net.preferIPv6Addresses", String.valueOf(true));
        }
    }

    /**
     * Update the given property with a string value.
     *
     * @param property The name of the property to create or update
     * @param v        The new or updated value
     */
    public abstract void setProperty(final String property, String v);

    /**
     * Update the given property with a list value
     *
     * @param property The name of the property to create or update
     * @param values   The new or updated value
     */
    public abstract void setProperty(final String property, List<String> values);

    /**
     * Remove a user customized property from the preferences.
     *
     * @param property Property name
     */
    public abstract void deleteProperty(final String property);

    /**
     * Internally always saved as a string.
     *
     * @param property The name of the property to create or update
     * @param v        The new or updated value
     */
    public void setProperty(final String property, final boolean v) {
        this.setProperty(property, v ? String.valueOf(true) : String.valueOf(false));
    }

    /**
     * Internally always saved as a string.
     *
     * @param property The name of the property to create or update
     * @param v        The new or updated value
     */
    public void setProperty(final String property, final int v) {
        this.setProperty(property, String.valueOf(v));
    }

    /**
     * Internally always saved as a string.
     *
     * @param property The name of the property to create or update
     * @param v        The new or updated value
     */
    public void setProperty(final String property, final float v) {
        this.setProperty(property, String.valueOf(v));
    }

    /**
     * Internally always saved as a string.
     *
     * @param property The name of the property to create or update
     * @param v        The new or updated value
     */
    public void setProperty(final String property, final long v) {
        this.setProperty(property, String.valueOf(v));
    }

    /**
     * Internally always saved as a string.
     *
     * @param property The name of the property to create or update
     * @param v        The new or updated value
     */
    public void setProperty(final String property, final double v) {
        this.setProperty(property, String.valueOf(v));
    }

    private static final class Version {
        /**
         * @return The <code>Specification-Version</code> in the JAR manifest.
         */
        public static String getSpecification() {
            Package pkg = Version.class.getPackage();
            return (pkg == null) ? null : pkg.getSpecificationVersion();
        }

        /**
         * @return The <code>Implementation-Version</code> in the JAR manifest.
         */
        public static String getImplementation() {
            Package pkg = Version.class.getPackage();
            return (pkg == null) ? null : pkg.getImplementationVersion();
        }

        /**
         * A simple main method that prints the version and exits
         */
        public static void main(String[] args) {
            System.out.println("Version: " + getSpecification());
            System.out.println("Implementation: " + getImplementation());
        }
    }

    /**
     * setting the default prefs values
     */
    protected void setDefaults() {
        defaults.put("application.version", Version.getSpecification());
        defaults.put("application.revision", Version.getImplementation());

        defaults.put("tmp.dir", System.getProperty("java.io.tmpdir"));

        /*
          How many times the application was launched
         */
        defaults.put("uses", "0");
        /*
          True if donation dialog will be displayed before quit
         */
        defaults.put("donate.reminder", String.valueOf(-1));
        defaults.put("donate.reminder.interval", String.valueOf(20)); // in days
        defaults.put("donate.reminder.date", String.valueOf(new Date(0).getTime()));

        defaults.put("defaulthandler.reminder", String.valueOf(true));

        defaults.put("mail.feedback", "mailto:support@cyberduck.io");

        defaults.put("website.donate", "https://cyberduck.io/donate/");
        defaults.put("website.home", "https://cyberduck.io/");
        defaults.put("website.help", "https://help.cyberduck.io/" + this.locale());
        defaults.put("website.bug", "https://trac.cyberduck.io/newticket?version={0}");
        defaults.put("website.crash", "https://crash.cyberduck.io/report");
        defaults.put("website.cli", "https://duck.sh/");
        defaults.put("website.license", "https://cyberduck.io/license");
        defaults.put("website.acknowledgments", "https://cyberduck.io/acknowledgments");

        defaults.put("rendezvous.enable", String.valueOf(true));
        defaults.put("rendezvous.loopback.suppress", String.valueOf(true));
        defaults.put("rendezvous.notification.limit", String.valueOf(30));

        defaults.put("growl.enable", String.valueOf(true));

        defaults.put("path.symboliclink.resolve", String.valueOf(false));
        /*
          Normalize path names
         */
        defaults.put("path.normalize", String.valueOf(true));
        defaults.put("path.normalize.unicode", String.valueOf(false));

        defaults.put("local.user.home", System.getProperty("user.home"));
        defaults.put("local.symboliclink.resolve", String.valueOf(false));
        defaults.put("local.normalize.prefix", String.valueOf(false));
        defaults.put("local.normalize.unicode", String.valueOf(true));
        defaults.put("local.normalize.tilde", String.valueOf(true));
        defaults.put("local.list.native", String.valueOf(true));
        defaults.put("local.delimiter", File.separator);
        defaults.put("local.temporaryfiles.shortening.threshold", String.valueOf(240));

        /*
          Prompt to resolve bookmark of file outside of sandbox with choose panel
         */
        defaults.put("local.bookmark.resolve.prompt", String.valueOf(false));

        defaults.put("application.name", "Cyberduck");
        final String support = SupportDirectoryFinderFactory.get().find().getAbsolute();
        defaults.put("application.support.path", support);
        defaults.put("application.receipt.path", support);

        // Default bundled profiles location
        final Local resources = ApplicationResourcesFinderFactory.get().find();
        defaults.put("application.bookmarks.path", String.format("%s/bookmarks", resources.getAbsolute()));
        defaults.put("application.profiles.path", String.format("%s/profiles", resources.getAbsolute()));

        /*
          Lowercase folder name to use when looking for bookmarks in user support directory
         */
        defaults.put("bookmarks.folder.name", "Bookmarks");
        /*
          Lowercase folder name to use when looking for profiles in user support directory
         */
        defaults.put("profiles.folder.name", "Profiles");

        /*
          Maximum number of directory listings to cache using a most recently used implementation
         */
        defaults.put("browser.cache.size", String.valueOf(1000));
        defaults.put("transfer.cache.size", String.valueOf(100));
        defaults.put("icon.cache.size", String.valueOf(200));

        /*
          Caching NS* proxy instances.
         */
        defaults.put("browser.model.cache.size", String.valueOf(10000));

        /*
          Callback threshold
         */
        defaults.put("browser.list.limit.directory", String.valueOf(5000));
        defaults.put("browser.list.limit.container", String.valueOf(100));

        defaults.put("info.toolbar.selected", String.valueOf(0));
        defaults.put("preferences.toolbar.selected", String.valueOf(0));

        /*
          Current default browser view is outline view (0-List view, 1-Outline view, 2-Column view)
         */
        defaults.put("browser.view", "1");
        /*
          Save browser sessions when quitting and restore upon relaunch
         */
        defaults.put("browser.serialize", String.valueOf(true));

        defaults.put("browser.font.size", String.valueOf(12f));

        defaults.put("browser.view.autoexpand", String.valueOf(true));
        defaults.put("browser.view.autoexpand.useDelay", String.valueOf(true));
        defaults.put("browser.view.autoexpand.delay", "1.0"); // in seconds

        defaults.put("browser.hidden.regex", "\\..*");

        defaults.put("browser.open.untitled", String.valueOf(true));
        defaults.put("browser.open.bookmark.default", null);

        /*
          Confirm closing the browsing connection
         */
        defaults.put("browser.disconnect.confirm", String.valueOf(false));
        defaults.put("browser.disconnect.bookmarks.show", String.valueOf(false));

        /*
          Display only one info panel and change information according to selection in browser
         */
        defaults.put("browser.info.inspector", String.valueOf(true));

        defaults.put("browser.sort.ascending", String.valueOf(true));

        defaults.put("browser.alternatingRows", String.valueOf(false));
        defaults.put("browser.verticalLines", String.valueOf(false));
        defaults.put("browser.horizontalLines", String.valueOf(true));
        /*
          Show hidden files in browser by default
         */
        defaults.put("browser.showHidden", String.valueOf(false));
        defaults.put("browser.charset.encoding", "UTF-8");
        /*
          Edit double clicked files instead of downloading
         */
        defaults.put("browser.doubleclick.edit", String.valueOf(false));
        /*
          Rename files when return or enter key is pressed
         */
        defaults.put("browser.enterkey.rename", String.valueOf(true));

        /*
          Enable inline editing in browser
         */
        defaults.put("browser.editable", String.valueOf(true));

        /*
          Warn before renaming files
         */
        defaults.put("browser.move.confirm", String.valueOf(true));


        defaults.put("browser.transcript.open", String.valueOf(false));
        defaults.put("browser.transcript.size.height", String.valueOf(200));

        /*
          Filename (Short Date Format)Extension
         */
        defaults.put("browser.duplicate.format", "{0} ({1}){2}");

        /*
          Use octal or decimal file sizes
         */
        defaults.put("browser.filesize.decimal", String.valueOf(false));
        defaults.put("browser.date.natural", String.valueOf(true));

        defaults.put("browser.delete.concurrency", String.valueOf(10));


        defaults.put("info.toggle.permission", String.valueOf(1));
        defaults.put("info.toggle.distribution", String.valueOf(0));
        defaults.put("info.toggle.s3", String.valueOf(0));

        defaults.put("connection.toggle.options", String.valueOf(0));
        defaults.put("bookmark.toggle.options", String.valueOf(0));

        defaults.put("alert.toggle.transcript", String.valueOf(0));

        defaults.put("transfer.toggle.details", String.valueOf(1));

        /*
          Default editor
         */
        defaults.put("editor.bundleIdentifier", "com.apple.TextEdit");
        defaults.put("editor.alwaysUseDefault", String.valueOf(false));

        defaults.put("editor.upload.permissions.change", String.valueOf(true));
        defaults.put("editor.upload.symboliclink.resolve", String.valueOf(true));

        /*
          Save bookmarks in ~/Library
         */
        defaults.put("favorites.save", String.valueOf(true));

        defaults.put("queue.removeItemWhenComplete", String.valueOf(false));
        /*
          The maximum number of concurrent transfers
         */
        defaults.put("queue.maxtransfers", String.valueOf(2));
        /*
          Default transfer connection handling
         */
        defaults.put("queue.transfer.type.enabled", String.format("%s %s %s",
                String.valueOf(Host.TransferType.browser.name()),
                String.valueOf(Host.TransferType.newconnection.name()),
                String.valueOf(Host.TransferType.concurrent.name())
        ));
        defaults.put("queue.transfer.type", String.valueOf(Host.TransferType.concurrent.name()));
        /*
          Warning when number of transfers in queue exceeds limit
         */
        defaults.put("queue.size.warn", String.valueOf(20));
        /*
          Bring transfer window to front
         */
        defaults.put("queue.window.open.default", String.valueOf(false));
        defaults.put("queue.window.open.transfer.start", String.valueOf(true));
        defaults.put("queue.window.open.transfer.stop", String.valueOf(false));

        /*
          Action when duplicate file exists
         */
        defaults.put("queue.download.action", TransferAction.callback.name());
        defaults.put("queue.upload.action", TransferAction.callback.name());
        defaults.put("queue.copy.action", TransferAction.callback.name());
        /*
          When triggered manually using 'Reload' in the Transfer window
         */
        defaults.put("queue.download.reload.action", TransferAction.callback.name());
        defaults.put("queue.upload.reload.action", TransferAction.callback.name());
        defaults.put("queue.copy.reload.action", TransferAction.callback.name());

        defaults.put("queue.upload.permissions.change", String.valueOf(false));
        defaults.put("queue.upload.permissions.default", String.valueOf(false));
        defaults.put("queue.upload.permissions.file.default", String.valueOf(644));
        defaults.put("queue.upload.permissions.folder.default", String.valueOf(755));

        defaults.put("queue.upload.timestamp.change", String.valueOf(false));
        /*
          Keep existing headers
         */
        defaults.put("queue.upload.file.metadata.change", String.valueOf(true));
        defaults.put("queue.upload.file.encryption.change", String.valueOf(true));
        defaults.put("queue.upload.file.redundancy.change", String.valueOf(true));

        defaults.put("queue.upload.skip.enable", String.valueOf(true));
        defaults.put("queue.upload.skip.regex.default",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS");
        defaults.put("queue.upload.skip.regex",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS");

        defaults.put("queue.upload.priority.regex", "");

        /*
          Create temporary filename with an UUID and rename when upload is complete
         */
        defaults.put("queue.upload.file.temporary", String.valueOf(false));
        /*
          Format string for temporary filename. Default to filename-uuid
         */
        defaults.put("queue.upload.file.temporary.format", "{0}-{1}");

        defaults.put("queue.upload.file.rename.format", "{0} ({1}){2}");
        defaults.put("queue.download.file.rename.format", "{0} ({1}){2}");

        defaults.put("queue.download.permissions.change", String.valueOf(true));
        defaults.put("queue.download.permissions.default", String.valueOf(false));
        defaults.put("queue.download.permissions.file.default", String.valueOf(644));
        defaults.put("queue.download.permissions.folder.default", String.valueOf(755));

        defaults.put("queue.download.timestamp.change", String.valueOf(true));
        defaults.put("queue.download.checksum", String.valueOf(true));

        defaults.put("queue.download.skip.enable", String.valueOf(true));
        defaults.put("queue.download.skip.regex.default",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs|\\.file-segments");
        defaults.put("queue.download.skip.regex",
                ".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs|\\.file-segments");

        defaults.put("queue.download.priority.regex", "");

        defaults.put("queue.download.folder", WorkingDirectoryFinderFactory.get().find().getAbsolute());
        // Security scoped bookmark
        defaults.put("queue.download.folder.bookmark", null);

        defaults.put("queue.download.quarantine", String.valueOf(true));
        defaults.put("queue.download.wherefrom", String.valueOf(true));

        // Segmented concurrent downloads
        defaults.put("queue.download.segments", String.valueOf(false));
        defaults.put("queue.download.segments.threshold", String.valueOf(100L * 1024L * 1024L));
        defaults.put("queue.download.segments.size", String.valueOf(50L * 1024L * 1024L));

        /*
          Open completed downloads
         */
        defaults.put("queue.download.complete.open", String.valueOf(false));

        defaults.put("queue.dock.badge", String.valueOf(false));

        defaults.put("queue.sleep.prevent", String.valueOf(true));

        /*
          Bandwidth throttle options
         */
        final StringBuilder options = new StringBuilder();
        options.append(5 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(10 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(20 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(50 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(100 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(150 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(200 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(500 * DecimalSizeFormatter.KILO.multiple()).append(",");
        options.append(1 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(2 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(5 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(10 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(15 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(20 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(50 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        options.append(100 * DecimalSizeFormatter.MEGA.multiple()).append(",");
        defaults.put("queue.bandwidth.options", options.toString());

        /*
          Bandwidth throttle upload stream
         */
        defaults.put("queue.upload.bandwidth.bytes", String.valueOf(-1));
        /*
          Bandwidth throttle download stream
         */
        defaults.put("queue.download.bandwidth.bytes", String.valueOf(-1));

        /*
          While downloading, update the icon of the downloaded file as a progress indicator
         */
        defaults.put("queue.download.icon.update", String.valueOf(true));
        defaults.put("queue.download.icon.threshold", String.valueOf(TransferStatus.MEGA * 5));

        /*
          Default synchronize action selected in the sync dialog
         */
        defaults.put("queue.prompt.sync.action.default", TransferAction.mirror.name());
        defaults.put("queue.prompt.download.action.default", TransferAction.overwrite.name());
        defaults.put("queue.prompt.upload.action.default", TransferAction.overwrite.name());
        defaults.put("queue.prompt.copy.action.default", TransferAction.overwrite.name());
        defaults.put("queue.prompt.move.action.default", TransferAction.overwrite.name());

        defaults.put("queue.transcript.open", String.valueOf(false));
        defaults.put("queue.transcript.size.height", String.valueOf(200));

        defaults.put("http.compression.enable", String.valueOf(true));

        /*
          HTTP routes to maximum number of connections allowed for those routes
         */
        defaults.put("http.connections.route", String.valueOf(10));
        defaults.put("http.connections.reuse", String.valueOf(true));
        /*
          Total number of connections in the pool
         */
        defaults.put("http.connections.total", String.valueOf(Integer.MAX_VALUE));
        defaults.put("http.connections.retry", String.valueOf(1));

        defaults.put("http.manager.timeout", String.valueOf(0)); // Infinite
        defaults.put("http.socket.buffer", String.valueOf(8192));
        defaults.put("http.credentials.charset", "ISO-8859-1");

        /*
          Enable or disable verification that the remote host taking part
          of a data connection is the same as the host to which the control
          connection is attached.
         */
        defaults.put("ftp.datachannel.verify", String.valueOf(false));
        defaults.put("ftp.socket.buffer", String.valueOf(0));

        defaults.put("ftp.parser.multiline.strict", String.valueOf(false));
        defaults.put("ftp.parser.reply.strict", String.valueOf(false));

        /*
          Send LIST -a
         */
        defaults.put("ftp.command.lista", String.valueOf(true));
        defaults.put("ftp.command.stat", String.valueOf(true));
        defaults.put("ftp.command.mlsd", String.valueOf(true));

        /*
          Fallback to active or passive mode respectively
         */
        defaults.put("ftp.connectmode.fallback", String.valueOf(true));
        /*
          Protect the data channel by default. For TLS, the data connection
          can have one of two security levels.
         1) Clear (requested by 'PROT C')
         2) Private (requested by 'PROT P')
         */
        defaults.put("ftp.tls.datachannel", "P"); //C
        defaults.put("ftp.tls.session.requirereuse", String.valueOf(true));
        defaults.put("ftp.ssl.session.cache.size", String.valueOf(100));

        /*
          Try to determine the timezone automatically using timestamp comparison from MLST and LIST
         */
        defaults.put("ftp.timezone.auto", String.valueOf(false));
        defaults.put("ftp.timezone.default", TimeZone.getDefault().getID());

        defaults.put("ftp.symlink.absolute", String.valueOf(false));

        /*
          Authentication header version
         */
//        defaults.put("s3.signature.version", "AWS2");
        defaults.put("s3.signature.version", "AWS4HMACSHA256");
        /*
          Default bucket location
         */
        defaults.put("s3.location", "us-east-1");
        defaults.put("s3.bucket.virtualhost.disable", String.valueOf(false));
        defaults.put("s3.bucket.requesterpays", String.valueOf(true));
        defaults.put("s3.domain", "amazonaws.com");
        defaults.put("s3.hostname.default", "s3.amazonaws.com");

        defaults.put("s3.bucket.acl.default", "public-read");
        //defaults.put("s3.bucket.acl.default", "private");
        defaults.put("s3.key.acl.default", "public-read");
        //defaults.put("s3.key.acl.default", "private");

        /*
          Default redundancy level
         */
        defaults.put("s3.storage.class", "STANDARD");
        //defaults.put("s3.encryption.algorithm", "AES256");
        defaults.put("s3.encryption.algorithm", StringUtils.EMPTY);

        /*
          Validity for public S3 URLs
         */
        defaults.put("s3.url.expire.seconds", String.valueOf(24 * 60 * 60));

        defaults.put("s3.mfa.serialnumber", StringUtils.EMPTY);

        defaults.put("s3.listing.chunksize", String.valueOf(1000));

        /*
          Show revisions as hidden files in browser
         */
        defaults.put("s3.revisions.enable", String.valueOf(true));

        defaults.put("s3.upload.md5", String.valueOf(true));

        defaults.put("s3.upload.multipart", String.valueOf(true));
        defaults.put("s3.upload.multipart.concurrency", String.valueOf(10));
        defaults.put("s3.upload.multipart.partsize.minimum", String.valueOf(5L * 1024L * 1024L));
        /*
          Threshold in bytes. Only use multipart uploads for files more than 100MB
         */
        defaults.put("s3.upload.multipart.threshold", String.valueOf(100L * 1024L * 1024L));
        defaults.put("s3.upload.multipart.required.threshold", String.valueOf(5L * 1024L * 1024L * 1024L));
        // Maximum number of parts is 10'000. With 10MB segements this gives a maximum object size of 100GB
        // Must be a multiple of org.cryptomator.cryptolib.v1.Constants.PAYLOAD_SIZE when using Cryptomator Vaults
        defaults.put("s3.upload.multipart.size", String.valueOf(10L * 1024L * 1024L)); // 10MB

        defaults.put("s3.upload.expect-continue", String.valueOf(true));

        /*
          Transfer thresholds for qloudsonic.io
         */
        defaults.put("s3.download.udt.threshold", String.valueOf(Long.MAX_VALUE));
        defaults.put("s3.upload.udt.threshold", String.valueOf(Long.MAX_VALUE));

        defaults.put("s3.accelerate.prompt", String.valueOf(false));

        /*
          A prefix to apply to log file names
         */
        defaults.put("s3.logging.prefix", "logs/");
        defaults.put("google.logging.prefix", "log");
        defaults.put("cloudfront.logging.prefix", "logs/");

        defaults.put("googlestorage.oauth.clientid", "996125414232.apps.googleusercontent.com");
        defaults.put("googlestorage.oauth.secret", "YdaFjo2t74-Q0sThsXgeTv3l");
        defaults.put("googlestorage.oauth.redirecturi", "urn:ietf:wg:oauth:2.0:oob");
//        defaults.put("googlestorage.oauth.redirecturi", "x-cyberduck-action:oauth");

        defaults.put("hubic.oauth.clientid", "api_hubic_Hoh1lpzGzsLxUV6VKI3BuiFcyJECBEPH");
        defaults.put("hubic.oauth.secret", "IIm0EkjdyPquS9SpIZXAdNlGbcf3mL9s3UiOFLnWLeTxLosjvAHGIbomvAcBZQb2");
        defaults.put("hubic.oauth.redirecturi", "https://cyberduck.io/oauth");
//        defaults.put("hubic.oauth.redirecturi", "x-cyberduck-action:oauth");

        defaults.put("onedrive.oauth.clientid", "372770ba-bb24-436b-bbd4-19bc86310c0e");
        defaults.put("onedrive.oauth.secret", "mJjWVkmfD9FVHNFTpbrdowv");
        defaults.put("onedrive.oauth.redirecturi", "https://cyberduck.io/oauth/");

        defaults.put("onedrive.upload.multipart.partsize.minimum", String.valueOf(320 * 1024));

        final int month = 60 * 60 * 24 * 30; //30 days in seconds
        defaults.put("s3.cache.seconds", String.valueOf(month));

        /*
          Default metadata for uploads. Format must be "key1=value1 key2=value2"
         */
        defaults.put("s3.metadata.default", StringUtils.EMPTY);

        defaults.put("s3.lifecycle.transition.options", "1 7 10 30 60 180 360 720");
        defaults.put("s3.lifecycle.delete.options", "1 7 10 30 60 180 360 720");

        defaults.put("s3.delete.multiple.partition", String.valueOf(1000));

        defaults.put("azure.metadata.default", StringUtils.EMPTY);
        defaults.put("azure.listing.chunksize", String.valueOf(1000));
        defaults.put("azure.upload.md5", String.valueOf(false));

        // Legacy authentication
//        defaults.put("openstack.authentication.context", "/v1.0");
        // Keystone authentication
        defaults.put("openstack.authentication.context", "/v2.0/tokens");
        defaults.put("openstack.upload.metadata.md5", String.valueOf(false));
        defaults.put("openstack.metadata.default", StringUtils.EMPTY);
        defaults.put("openstack.list.container.limit", String.valueOf(100));
        defaults.put("openstack.list.object.limit", String.valueOf(10000));
        defaults.put("openstack.account.preload", String.valueOf(true));
        defaults.put("openstack.cdn.preload", String.valueOf(true));
        defaults.put("openstack.container.size.preload", String.valueOf(true));

        defaults.put("openstack.upload.md5", String.valueOf(true));

        defaults.put("openstack.upload.largeobject", String.valueOf(true));
        defaults.put("openstack.upload.largeobject.concurrency", String.valueOf(5));
        defaults.put("openstack.upload.largeobject.segments.prefix", ".file-segments/");
        defaults.put("openstack.upload.largeobject.threshold", String.valueOf(2L * 1024L * 1024L * 1024L)); // 2GB
        defaults.put("openstack.upload.largeobject.required.threshold", String.valueOf(5L * 1024L * 1024L * 1024L)); // 5GB
        defaults.put("openstack.upload.largeobject.size", String.valueOf(1000L * 1024L * 1024L)); // 1GB
        // Each segment, except for the final one, must be at least 1 megabyte
        defaults.put("openstack.upload.largeobject.size.minimum", String.valueOf(1 * 1024L * 1024L)); // 1MB
        // Remove segments when deleting large object manifest
        defaults.put("openstack.upload.largeobject.cleanup", String.valueOf(true));

        defaults.put("openstack.delete.multiple.partition", String.valueOf(10000));

        defaults.put("googledrive.oauth.clientid", "996125414232.apps.googleusercontent.com");
        defaults.put("googledrive.oauth.clientsecret", "YdaFjo2t74-Q0sThsXgeTv3l");
        defaults.put("googledrive.oauth.redirecturi", "urn:ietf:wg:oauth:2.0:oob");
//        defaults.put("googledrive.oauth.redirecturi", "x-cyberduck-action:oauth");

        defaults.put("googledrive.list.limit", String.valueOf(1000));

        defaults.put("b2.bucket.acl.default", "allPrivate");
        defaults.put("b2.listing.chunksize", String.valueOf(100));
        defaults.put("b2.upload.checksum.verify", String.valueOf(true));

        defaults.put("b2.upload.largeobject", String.valueOf(true));
        defaults.put("b2.upload.largeobject.concurrency", String.valueOf(5));
        defaults.put("b2.upload.largeobject.required.threshold", String.valueOf(5L * 1024L * 1024L * 1024L)); // 5GB
        // When uploading files larger than 200MB, use the large files support to break up the files into parts and upload the parts in parallel.
        defaults.put("b2.upload.largeobject.threshold", String.valueOf(200 * 1024L * 1024L)); // 200MB
        // Each part can be anywhere from 100MB to 5GB in size
        defaults.put("b2.upload.largeobject.size", String.valueOf(100 * 1024L * 1024L));
        defaults.put("b2.upload.largeobject.size.minimum", String.valueOf(5 * 1024L * 1024L));

        defaults.put("b2.metadata.default", StringUtils.EMPTY);

        /*
          NTLM Windows Domain
         */
        defaults.put("webdav.ntlm.domain", StringUtils.EMPTY);
        defaults.put("webdav.ntlm.workstation", StringUtils.EMPTY);

        /*
          Enable preemptive authentication if valid credentials are found
         */
        defaults.put("webdav.basic.preemptive", String.valueOf(true));

        /*
          Enable Expect-Continue handshake
         */
        defaults.put("webdav.expect-continue", String.valueOf(true));
        defaults.put("webdav.redirect.GET.follow", String.valueOf(true));
        defaults.put("webdav.redirect.HEAD.follow", String.valueOf(true));
        defaults.put("webdav.redirect.PUT.follow", String.valueOf(false));
        defaults.put("webdav.redirect.PROPFIND.follow", String.valueOf(true));

        defaults.put("webdav.upload.md5", String.valueOf(false));
        defaults.put("webdav.metadata.default", StringUtils.EMPTY);

        defaults.put("analytics.provider.qloudstat.setup", "https://qloudstat.com/configuration/add");
        defaults.put("analytics.provider.qloudstat.iam.policy",
                "{\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Action\": [\n" +
                        "                \"s3:GetObject\", \n" +
                        "                \"s3:ListBucket\"\n" +
                        "            ], \n" +
                        "            \"Condition\": {\n" +
                        "                \"Bool\": {\n" +
                        "                    \"aws:SecureTransport\": \"true\"\n" +
                        "                }\n" +
                        "            }, \n" +
                        "            \"Effect\": \"Allow\", \n" +
                        "            \"Resource\": \"arn:aws:s3:::%s/*\"\n" +
                        "        }, \n" +
                        "        {\n" +
                        "            \"Action\": [\n" +
                        "                \"s3:ListAllMyBuckets\", \n" +
                        "                \"s3:GetBucketLogging\", \n" +
                        "                \"s3:GetBucketLocation\"\n" +
                        "            ], \n" +
                        "            \"Effect\": \"Allow\", \n" +
                        "            \"Resource\": \"arn:aws:s3:::*\"\n" +
                        "        }, \n" +
                        "        {\n" +
                        "            \"Action\": [\n" +
                        "                \"cloudfront:GetDistribution\", \n" +
                        "                \"cloudfront:GetDistributionConfig\", \n" +
                        "                \"cloudfront:ListDistributions\", \n" +
                        "                \"cloudfront:GetStreamingDistribution\", \n" +
                        "                \"cloudfront:GetStreamingDistributionConfig\", \n" +
                        "                \"cloudfront:ListStreamingDistributions\"\n" +
                        "            ], \n" +
                        "            \"Condition\": {\n" +
                        "                \"Bool\": {\n" +
                        "                    \"aws:SecureTransport\": \"true\"\n" +
                        "                }\n" +
                        "            }, \n" +
                        "            \"Effect\": \"Allow\", \n" +
                        "            \"Resource\": \"*\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}\n"
        );

        /*
         * Session pool
         */
        defaults.put("connection.pool.minidle", String.valueOf(1));
        defaults.put("connection.pool.maxidle", String.valueOf(5));
        defaults.put("connection.pool.maxtotal", String.valueOf(Integer.MAX_VALUE));

        /*
          Default login name
         */
        defaults.put("connection.login.name", StringUtils.EMPTY);
        defaults.put("connection.login.anon.name", "anonymous");
        defaults.put("connection.login.anon.pass", "cyberduck@example.net");
        /*
          Search for passphrases in Keychain
         */
        defaults.put("connection.login.keychain", String.valueOf(true));
        /*
         * Save passwords for vaults in Keychain
         */
        defaults.put("vault.keychain", String.valueOf(false));

        defaults.put("connection.port.default", String.valueOf(21));
        defaults.put("connection.protocol.default", Scheme.ftp.name());

        /*
          SO_KEEPALIVE
         */
        defaults.put("connection.socket.keepalive", String.valueOf(true));
        /*
          SO_LINGER
         */
        defaults.put("connection.socket.linger", String.valueOf(false));
        /*
          Socket timeout
         */
        defaults.put("connection.timeout.seconds", String.valueOf(30));
        /*
          Retry to connect after a I/O failure automatically
         */
        defaults.put("connection.retry", String.valueOf(1));
        /*
          In seconds
         */
        defaults.put("connection.retry.delay", String.valueOf(0));
        defaults.put("connection.retry.backoff.enable", String.valueOf(false));

        defaults.put("connection.hostname.default", StringUtils.EMPTY);
        /*
          Convert hostname to Punycode
         */
        defaults.put("connection.hostname.idn", String.valueOf(true));

        /*
          java.net.preferIPv6Addresses
         */
        defaults.put("connection.dns.ipv6", String.valueOf(false));

        /*
          Read proxy settings from system preferences
         */
        defaults.put("connection.proxy.enable", String.valueOf(true));
        defaults.put("connection.proxy.ntlm.domain", StringUtils.EMPTY);

        /*
          Warning when opening connections sending credentials in plaintext
         */
        defaults.put(String.format("connection.unsecure.warning.%s", Scheme.ftp), String.valueOf(true));
        defaults.put(String.format("connection.unsecure.warning.%s", Scheme.http), String.valueOf(true));

        defaults.put("connection.ssl.provider.bouncycastle.position", String.valueOf(1));
        defaults.put("connection.ssl.protocols", "TLSv1.2,TLSv1.1,TLSv1");
        defaults.put("connection.ssl.cipher.blacklist", StringUtils.EMPTY);

        defaults.put("connection.ssl.x509.revocation.online", String.valueOf(false));

        defaults.put("connection.ssl.keystore.type", null);
        defaults.put("connection.ssl.keystore.provider", null);

        // Default secure random strong algorithm
        defaults.put("connection.ssl.securerandom.algorithm", "NativePRNG");
        defaults.put("connection.ssl.securerandom.provider", "SUN");

        /*
          Transfer read buffer size
         */
        defaults.put("connection.chunksize", String.valueOf(32768));
        /*
          Buffer size for wrapped buffered streams
         */
        defaults.put("connection.buffer", String.valueOf(8192));
        /*
          SO_SNDBUF
         */
        defaults.put("connection.buffer.send", String.valueOf(0));
        /*
          SO_RCVBUF
         */
        defaults.put("connection.buffer.receive", String.valueOf(0));

        defaults.put("disk.unmount.timeout", String.valueOf(2));

        /*
          Read favicon from Web URL
         */
        defaults.put("bookmark.favicon.download", String.valueOf(true));

        /*
          Default to large icon size
         */
        defaults.put("bookmark.icon.size", String.valueOf(64));
        defaults.put("bookmark.menu.icon.size", String.valueOf(16));

        /*
          Location of the openssh known_hosts file
         */
        defaults.put("ssh.knownhosts", "~/.ssh/known_hosts");
        defaults.put("ssh.knownhosts.bookmark", StringUtils.EMPTY);

        defaults.put("ssh.authentication.publickey.default.enable", String.valueOf(false));
        defaults.put("ssh.authentication.publickey.default.rsa", "~/.ssh/id_rsa");
        defaults.put("ssh.authentication.publickey.default.dsa", "~/.ssh/id_dsa");

        defaults.put("ssh.authentication.agent.enable", String.valueOf(true));

        defaults.put("ssh.heartbeat.provider", "keep-alive");
        defaults.put("ssh.heartbeat.seconds", String.valueOf(60));

        /*
          Enable ZLIB compression
         */
        defaults.put("ssh.compression", "zlib");

        defaults.put("ssh.algorithm.cipher.blacklist", StringUtils.EMPTY);
        defaults.put("ssh.algorithm.mac.blacklist", StringUtils.EMPTY);
        defaults.put("ssh.algorithm.kex.blacklist", StringUtils.EMPTY);
        defaults.put("ssh.algorithm.signature.blacklist", StringUtils.EMPTY);

        defaults.put("sftp.symlink.absolute", String.valueOf(false));

        defaults.put("sftp.read.maxunconfirmed", String.valueOf(64));
        defaults.put("sftp.write.maxunconfirmed", String.valueOf(64));

        defaults.put("archive.default", "tar.gz");

        /*
          Archiver
         */
        defaults.put("archive.command.create.tar", "cd {2}; tar -cpPf {0}.tar {1}");
        defaults.put("archive.command.create.tar.gz", "cd {2}; tar -czpPf {0}.tar.gz {1}");
        defaults.put("archive.command.create.tar.bz2", "cd {2}; tar -cjpPf {0}.tar.bz2 {1}");
        defaults.put("archive.command.create.zip", "cd {2}; zip -qr {0}.zip {1}");
        defaults.put("archive.command.create.gz", "gzip -qr {1}");
        defaults.put("archive.command.create.bz2", "bzip2 -zk {1}");

        /*
          Unarchiver
         */
        defaults.put("archive.command.expand.tar", "tar -xpPf {0} -C {1}");
        defaults.put("archive.command.expand.tar.gz", "tar -xzpPf {0} -C {1}");
        defaults.put("archive.command.expand.tar.bz2", "tar -xjpPf {0} -C {1}");
        defaults.put("archive.command.expand.zip", "unzip -qn {0} -d {1}");
        defaults.put("archive.command.expand.gz", "gzip -d {0}");
        defaults.put("archive.command.expand.bz2", "bzip2 -dk {0}");

        defaults.put("update.check", String.valueOf(true));
        final int day = 60 * 60 * 24;
        defaults.put("update.check.interval", String.valueOf(day)); // periodic update check in seconds
        // Last update check in milliseconds
        defaults.put("update.check.timestamp", String.valueOf(0));

        defaults.put("terminal.bundle.identifier", "com.apple.Terminal");
        defaults.put("terminal.command", "do script \"{0}\"");
        defaults.put("terminal.command.ssh", "ssh -t {0} {1}@{2} -p {3} \"cd {4} && exec \\$SHELL\"");

        defaults.put("network.interface.blacklist", StringUtils.EMPTY);

        defaults.put("threading.pool.size.max", String.valueOf(20));
        defaults.put("threading.pool.keepalive.seconds", String.valueOf(60L));

        defaults.put("dropbox.oauth.clientid", "rjqgs45ntjp1va9");
        defaults.put("dropbox.oauth.clientsecret", "yg1uopbf5c1h1rk");
        defaults.put("dropbox.oauth.redirecturi", "https://cyberduck.io/oauth");
//        defaults.put("dropbox.oauth.redirecturi", "x-cyberduck-action:oauth");

        defaults.put("cryptomator.enable", String.valueOf(true));
    }

    protected void setLogging() {
        // Enable all logging levels to pass through bridge
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.ALL);
        // Call only once during initialization time of your application
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        final URL configuration;
        final String file = defaults.get("logging.config");
        if(null == file) {
            configuration = Preferences.class.getClassLoader().getResource("log4j-default.xml");
        }
        else {
            configuration = Preferences.class.getClassLoader().getResource(file);
        }
        if(null != configuration) {
            DOMConfigurator.configure(configuration);
        }
        else {
            // Default if no logging configuration is found
            final Logger root = Logger.getRootLogger();
            root.setLevel(Level.ERROR);
        }
        if(StringUtils.isNotBlank(this.getProperty("logging"))) {
            // Allow to override default logging level
            final Logger root = Logger.getRootLogger();
            root.setLevel(Level.toLevel(this.getProperty("logging"), Level.ERROR));
        }
    }

    /**
     * Default value for a given property.
     *
     * @param property The property to query.
     * @return A default value if any or null if not found.
     */
    public String getDefault(final String property) {
        String value = defaults.get(property);
        if(null == value) {
            log.warn(String.format("No property with key '%s'", property));
        }
        return value;
    }

    /**
     * @param property The property to query.
     * @return The configured values determined by a whitespace separator.
     */
    public List<String> getList(final String property) {
        final String value = this.getProperty(property);
        if(StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("(?<!\\\\)\\p{javaWhitespace}+"));
    }

    public Map<String, String> getMap(final String property) {
        final List<String> list = this.getList(property);
        final Map<String, String> table = new HashMap<String, String>();
        for(String m : list) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String key = m.substring(0, split);
            if(StringUtils.isBlank(key)) {
                log.warn(String.format("Missing key in %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in %s", m));
                continue;
            }
            table.put(key, value);
        }
        return table;
    }

    /**
     * Give value in user settings or default value if not customized.
     *
     * @param property The property to query.
     * @return The user configured value or default.
     */
    public abstract String getProperty(String property);

    public int getInteger(final String property) {
        final String v = this.getProperty(property);
        if(null == v) {
            return -1;
        }
        try {
            return Integer.parseInt(v);
        }
        catch(NumberFormatException e) {
            return (int) this.getDouble(property);
        }
    }

    public float getFloat(final String property) {
        final String v = this.getProperty(property);
        if(null == v) {
            return -1;
        }
        try {
            return Float.parseFloat(v);
        }
        catch(NumberFormatException e) {
            return (float) this.getDouble(property);
        }
    }

    public long getLong(final String property) {
        final String v = this.getProperty(property);
        if(null == v) {
            return -1;
        }
        try {
            return Long.parseLong(v);
        }
        catch(NumberFormatException e) {
            return (long) this.getDouble(property);
        }
    }

    public double getDouble(final String property) {
        final String v = this.getProperty(property);
        if(null == v) {
            return -1;
        }
        try {
            return Double.parseDouble(v);
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }

    public boolean getBoolean(final String property) {
        final String v = this.getProperty(property);
        if(null == v) {
            return false;
        }
        if(v.equalsIgnoreCase(String.valueOf(true))) {
            return true;
        }
        if(v.equalsIgnoreCase(String.valueOf(false))) {
            return false;
        }
        if(v.equalsIgnoreCase(String.valueOf(1))) {
            return true;
        }
        if(v.equalsIgnoreCase(String.valueOf(0))) {
            return false;
        }
        return v.equalsIgnoreCase("yes");
    }

    protected void setFactories() {
        defaults.put("factory.serializer.class", PlistSerializer.class.getName());
        defaults.put("factory.deserializer.class", PlistDeserializer.class.getName());
        defaults.put("factory.reader.profile.class", ProfilePlistReader.class.getName());
        defaults.put("factory.writer.profile.class", PlistWriter.class.getName());
        defaults.put("factory.reader.transfer.class", TransferPlistReader.class.getName());
        defaults.put("factory.writer.transfer.class", PlistWriter.class.getName());
        defaults.put("factory.reader.host.class", HostPlistReader.class.getName());
        defaults.put("factory.writer.host.class", PlistWriter.class.getName());

        defaults.put("factory.locale.class", DisabledLocale.class.getName());
        defaults.put("factory.local.class", Local.class.getName());
        defaults.put("factory.certificatestore.class", DisabledCertificateStore.class.getName());
        defaults.put("factory.logincallback.class", DisabledLoginCallback.class.getName());
        defaults.put("factory.passwordcallback.class", DisabledPasswordCallback.class.getName());
        defaults.put("factory.alertcallback.class", DisabledAlertCallback.class.getName());
        defaults.put("factory.hostkeycallback.class", DisabledHostKeyCallback.class.getName());
        defaults.put("factory.transfererrorcallback.class", DisabledTransferErrorCallback.class.getName());
        defaults.put("factory.temporaryfiles.class", DefaultTemporaryFileService.class.getName());
        defaults.put("factory.touch.class", DefaultLocalTouchFeature.class.getName());
        defaults.put("factory.autorelease.class", DisabledActionOperationBatcher.class.getName());
        defaults.put("factory.schemehandler.class", DisabledSchemeHandler.class.getName());
        defaults.put("factory.iconservice.class", DisabledIconService.class.getName());
        defaults.put("factory.iconcache.class", DisabledIconCache.class.getName());
        defaults.put("factory.notification.class", DisabledNotificationService.class.getName());
        defaults.put("factory.sleeppreventer.class", DisabledSleepPreventer.class.getName());
        defaults.put("factory.quarantine.class", DisabledQuarantineService.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            defaults.put(String.format("factory.transferpromptcallback.%s.class", t.name()), DisabledTransferPrompt.class.getName());
        }
        defaults.put("factory.supportdirectoryfinder.class", TemporarySupportDirectoryFinder.class.getName());
        defaults.put("factory.applicationresourcesfinder.class", TemporaryApplicationResourcesFinder.class.getName());
        defaults.put("factory.workingdirectory.class", DefaultWorkingDirectoryFinder.class.getName());
        defaults.put("factory.watchservice.class", NIOEventWatchService.class.getName());
        defaults.put("factory.proxy.class", DisabledProxyFinder.class.getName());
        defaults.put("factory.passwordstore.class", DisabledPasswordStore.class.getName());
        defaults.put("factory.dateformatter.class", DefaultUserDateFormatter.class.getName());
        defaults.put("factory.trash.class", NativeLocalTrashFeature.class.getName());
        defaults.put("factory.symlink.class", NullLocalSymlinkFeature.class.getName());
        defaults.put("factory.licensefactory.class", DonationKeyFactory.class.getName());
        defaults.put("factory.badgelabeler.class", DisabledApplicationBadgeLabeler.class.getName());
        defaults.put("factory.filedescriptor.class", NullFileDescriptor.class.getName());
        defaults.put("factory.terminalservice.class", DisabledTerminalService.class.getName());
        defaults.put("factory.applicationfinder.class", DisabledApplicationFinder.class.getName());
        defaults.put("factory.applicationlauncher.class", DisabledApplicationLauncher.class.getName());
        defaults.put("factory.browserlauncher.class", DisabledBrowserLauncher.class.getName());
        defaults.put("factory.reachability.class", DefaultInetAddressReachability.class.getName());
        defaults.put("factory.updater.class", DisabledPeriodicUpdater.class.getName());
        defaults.put("factory.threadpool.class", DefaultThreadPool.class.getName());
        defaults.put("factory.urlfilewriter.class", InternetShortcutFileWriter.class.getName());
        defaults.put("factory.vault.class", DisabledVault.class.getName());
        defaults.put("factory.securerandom.class", DefaultSecureRandomProvider.class.getName());
    }

    /**
     * Store preferences
     */
    public abstract void save();

    /**
     * Overriding the default values with preferences from the last session.
     */
    public abstract void load();

    /**
     * @return The preferred locale of all localizations available
     * in this application bundle
     */
    public String locale() {
        return this.applicationLocales().iterator().next();
    }

    /**
     * The localizations available in this application bundle
     * sorted by preference by the user.
     *
     * @return Available locales in application bundle
     */
    public abstract List<String> applicationLocales();

    /**
     * @return Available locales in system
     */
    public abstract List<String> systemLocales();

    /**
     * @param locale ISO Language identifier
     * @return Human readable language name in the target language
     */
    public String getDisplayName(final String locale) {
        java.util.Locale l;
        if(StringUtils.contains(locale, "_")) {
            l = new java.util.Locale(locale.split("_")[0], locale.split("_")[1]);
        }
        else {
            l = new java.util.Locale(locale);
        }
        return StringUtils.capitalize(l.getDisplayName(l));
    }
}
