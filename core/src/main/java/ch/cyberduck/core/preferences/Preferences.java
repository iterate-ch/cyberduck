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

import ch.cyberduck.core.*;
import ch.cyberduck.core.aquaticprime.DonationKeyFactory;
import ch.cyberduck.core.date.DefaultUserDateFormatter;
import ch.cyberduck.core.diagnostics.DefaultInetAddressReachability;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.formatter.DecimalSizeFormatter;
import ch.cyberduck.core.i18n.Locales;
import ch.cyberduck.core.io.watchservice.NIOEventWatchService;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.local.DefaultWorkingDirectoryFinder;
import ch.cyberduck.core.local.DisabledApplicationBadgeLabeler;
import ch.cyberduck.core.local.DisabledApplicationFinder;
import ch.cyberduck.core.local.DisabledApplicationLauncher;
import ch.cyberduck.core.local.DisabledBrowserLauncher;
import ch.cyberduck.core.local.DisabledFilesystemBookmarkResolver;
import ch.cyberduck.core.local.DisabledIconService;
import ch.cyberduck.core.local.DisabledQuarantineService;
import ch.cyberduck.core.local.NativeLocalTrashFeature;
import ch.cyberduck.core.local.NullFileDescriptor;
import ch.cyberduck.core.local.NullLocalSymlinkFeature;
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
import ch.cyberduck.core.updater.DisabledUpdateCheckerArguments;
import ch.cyberduck.core.urlhandler.DisabledSchemeHandler;
import ch.cyberduck.core.vault.DisabledVault;
import ch.cyberduck.core.webloc.InternetShortcutFileWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.google.common.collect.ImmutableMap;

/**
 * Holding all application preferences. Default values get overwritten when loading the <code>PREFERENCES_FILE</code>.
 * Singleton class.
 */
public abstract class Preferences implements Locales {
    private static final Logger log = Logger.getLogger(Preferences.class);

    protected static final String LIST_SEPERATOR = StringUtils.SPACE;

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
    public void setProperty(final String property, List<String> values) {
        this.setProperty(property, StringUtils.join(values, LIST_SEPERATOR));
    }

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

    public abstract String getDefault(String property);

    public abstract void setDefault(String property, String value);

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

    protected void setDefaults(final Properties properties) {
        for(Map.Entry<Object, Object> property : properties.entrySet()) {
            this.setDefault(property.getKey().toString(), property.getValue().toString());
        }
    }

    protected void setDefaults(final Local defaults) {
        if(defaults.exists()) {
            final Properties props = new Properties();
            try (final InputStream in = defaults.getInputStream()) {
                props.load(in);
            }
            catch(AccessDeniedException | IOException e) {
                // Ignore failure loading configuration
            }
            for(Map.Entry<Object, Object> entry : props.entrySet()) {
                this.setDefault(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    /**
     * setting the default prefs values
     */
    protected void setDefaults() {
        // TTL for DNS queries
        Security.setProperty("networkaddress.cache.ttl", "10");
        Security.setProperty("networkaddress.cache.negative.ttl", "5");

        this.setDefault("application.version", Version.getSpecification());
        this.setDefault("application.revision", Version.getImplementation());

        this.setDefault("tmp.dir", System.getProperty("java.io.tmpdir"));

        /*
          How many times the application was launched
         */
        this.setDefault("uses", "0");
        /*
          Last version prompt was shown
         */
        this.setDefault("donate.reminder", String.valueOf(-1));
        this.setDefault("donate.reminder.suppress.enable", String.valueOf(false));
        this.setDefault("donate.reminder.interval", String.valueOf(0)); // in days
        this.setDefault("donate.reminder.date", String.valueOf(new Date(0).getTime()));

        this.setDefault("defaulthandler.reminder", String.valueOf(true));

        this.setDefault("mail.feedback", "mailto:support@cyberduck.io");

        this.setDefault("website.donate", "https://cyberduck.io/donate/");
        this.setDefault("website.home", "https://cyberduck.io/");
        this.setDefault("website.help", "https://help.cyberduck.io/" + this.locale());
        this.setDefault("website.bug", "https://trac.cyberduck.io/newticket?version={0}");
        this.setDefault("website.crash", "https://crash.cyberduck.io/report");
        this.setDefault("website.cli", "https://duck.sh/");
        this.setDefault("website.license", "https://cyberduck.io/license");
        this.setDefault("website.acknowledgments", "https://cyberduck.io/acknowledgments");

        this.setDefault("rendezvous.enable", String.valueOf(true));
        this.setDefault("rendezvous.loopback.suppress", String.valueOf(true));
        this.setDefault("rendezvous.notification.limit", String.valueOf(0));

        this.setDefault("growl.enable", String.valueOf(true));

        this.setDefault("path.symboliclink.resolve", String.valueOf(false));
        /*
          Normalize path names
         */
        this.setDefault("path.normalize", String.valueOf(true));
        this.setDefault("path.normalize.unicode", String.valueOf(false));

        this.setDefault("local.user.home", System.getProperty("user.home"));
        this.setDefault("local.alias.resolve", String.valueOf(true));
        this.setDefault("local.symboliclink.resolve", String.valueOf(false));
        this.setDefault("local.normalize.prefix", String.valueOf(false));
        this.setDefault("local.normalize.unicode", String.valueOf(true));
        this.setDefault("local.normalize.tilde", String.valueOf(true));
        this.setDefault("local.delimiter", File.separator);
        this.setDefault("local.temporaryfiles.shortening.threshold", String.valueOf(240));

        this.setDefault("application.identifier", "io.cyberduck");
        this.setDefault("application.name", "Cyberduck");
        this.setDefault("application.container.name", "duck");
        this.setDefault("application.container.teamidentifier", "G69SCX94XU");
        this.setDefault("application.datafolder.name", "duck");

        /*
          Lowercase folder name to use when looking for bookmarks in user support directory
         */
        this.setDefault("bookmarks.folder.name", "Bookmarks");
        /*
          Lowercase folder name to use when looking for profiles in user support directory
         */
        this.setDefault("profiles.folder.name", "Profiles");

        /*
          Maximum number of directory listings to cache using a most recently used implementation
         */
        this.setDefault("browser.cache.size", String.valueOf(1000));
        this.setDefault("transfer.cache.size", String.valueOf(100));
        this.setDefault("icon.cache.size", String.valueOf(200));
        this.setDefault("preferences.cache.size", String.valueOf(1000));

        /*
          Caching NS* proxy instances.
         */
        this.setDefault("browser.model.cache.size", String.valueOf(10000));

        /*
          Callback threshold
         */
        this.setDefault("browser.list.limit.directory", String.valueOf(5000));
        this.setDefault("browser.list.limit.container", String.valueOf(100));

        this.setDefault("info.toolbar.selected", String.valueOf(0));
        this.setDefault("preferences.toolbar.selected", String.valueOf(0));

        /*
          Current default browser view is outline view (0-List view, 1-Outline view, 2-Column view)
         */
        this.setDefault("browser.view", "1");
        /*
          Save browser sessions when quitting and restore upon relaunch
         */
        this.setDefault("browser.serialize", String.valueOf(true));

        this.setDefault("browser.font.size", String.valueOf(12f));

        this.setDefault("browser.view.autoexpand", String.valueOf(true));
        this.setDefault("browser.view.autoexpand.delay.enable", String.valueOf(true));
        this.setDefault("browser.view.autoexpand.delay", "1.0"); // in seconds

        this.setDefault("browser.hidden.regex", "\\..*");

        this.setDefault("browser.open.untitled", String.valueOf(true));
        this.setDefault("browser.open.bookmark.default", null);

        /*
          Confirm closing the browsing connection
         */
        this.setDefault("browser.disconnect.confirm", String.valueOf(false));
        this.setDefault("browser.disconnect.bookmarks.show", String.valueOf(false));

        /*
          Display only one info panel and change information according to selection in browser
         */
        this.setDefault("browser.info.inspector", String.valueOf(true));

        this.setDefault("browser.sort.ascending", String.valueOf(true));

        this.setDefault("browser.alternatingRows", String.valueOf(false));
        this.setDefault("browser.verticalLines", String.valueOf(false));
        this.setDefault("browser.horizontalLines", String.valueOf(true));
        /*
          Show hidden files in browser by default
         */
        this.setDefault("browser.showHidden", String.valueOf(false));
        this.setDefault("browser.charset.encoding", StandardCharsets.UTF_8.name());
        /*
          Edit double clicked files instead of downloading
         */
        this.setDefault("browser.doubleclick.edit", String.valueOf(false));
        /*
          Rename files when return or enter key is pressed
         */
        this.setDefault("browser.enterkey.rename", String.valueOf(true));

        /*
          Enable inline editing in browser
         */
        this.setDefault("browser.editable", String.valueOf(true));

        /*
          Warn before renaming files
         */
        this.setDefault("browser.move.confirm", String.valueOf(true));
        this.setDefault("browser.copy.confirm", String.valueOf(false));


        this.setDefault("browser.transcript.open", String.valueOf(false));
        this.setDefault("browser.transcript.size.height", String.valueOf(200));

        /*
          Filename (Short Date Format)Extension
         */
        this.setDefault("browser.duplicate.format", "{0} ({1}){2}");

        /*
          Use octal or decimal file sizes
         */
        this.setDefault("browser.filesize.decimal", String.valueOf(false));
        this.setDefault("browser.date.natural", String.valueOf(true));

        this.setDefault("bookmark.toggle.options", String.valueOf(false));
        this.setDefault("transfer.toggle.details", String.valueOf(true));

        /*
          Default editor
         */
        this.setDefault("editor.bundleIdentifier", "com.apple.TextEdit");
        this.setDefault("editor.alwaysUseDefault", String.valueOf(false));

        this.setDefault("editor.upload.permissions.change", String.valueOf(true));
        this.setDefault("editor.upload.symboliclink.resolve", String.valueOf(true));

        /*
          Save bookmarks in ~/Library
         */
        this.setDefault("favorites.save", String.valueOf(true));

        this.setDefault("queue.removeItemWhenComplete", String.valueOf(false));
        /*
          Default transfer connection handling
         */
        this.setDefault("queue.transfer.type.enabled", String.format("%s %s %s",
            Host.TransferType.browser.name(),
            Host.TransferType.newconnection.name(),
            Host.TransferType.concurrent.name()
        ));
        this.setDefault("queue.transfer.type", Host.TransferType.concurrent.name());
        /*
          Warning when number of transfers in queue exceeds limit
         */
        this.setDefault("queue.size.warn", String.valueOf(20));
        /*
          Bring transfer window to front
         */
        this.setDefault("queue.window.open.default", String.valueOf(false));
        this.setDefault("queue.window.open.transfer.start", String.valueOf(true));
        this.setDefault("queue.window.open.transfer.stop", String.valueOf(false));

        /*
          Action when duplicate file exists
         */
        this.setDefault("queue.download.action", TransferAction.callback.name());
        this.setDefault("queue.upload.action", TransferAction.callback.name());
        this.setDefault("queue.copy.action", TransferAction.callback.name());
        /*
          When triggered manually using 'Reload' in the Transfer window
         */
        this.setDefault("queue.download.reload.action", TransferAction.callback.name());
        this.setDefault("queue.upload.reload.action", TransferAction.callback.name());
        this.setDefault("queue.copy.reload.action", TransferAction.callback.name());

        this.setDefault("queue.upload.permissions.change", String.valueOf(false));
        this.setDefault("queue.upload.permissions.default", String.valueOf(false));
        this.setDefault("queue.upload.permissions.file.default", String.valueOf(644));
        this.setDefault("queue.upload.permissions.folder.default", String.valueOf(755));

        this.setDefault("queue.upload.timestamp.change", String.valueOf(false));
        /*
          Keep existing headers
         */
        this.setDefault("queue.upload.file.metadata.change", String.valueOf(true));
        this.setDefault("queue.upload.file.encryption.change", String.valueOf(true));
        this.setDefault("queue.upload.file.redundancy.change", String.valueOf(true));

        this.setDefault("queue.upload.checksum.calculate", String.valueOf(false));

        this.setDefault("queue.upload.skip.enable", String.valueOf(true));
        this.setDefault("queue.upload.skip.regex.default",
            ".*~\\..*|\\.DS_Store|\\.svn|CVS|\\.git|\\.gitignore|\\.gitattributes|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags");
        this.setDefault("queue.upload.skip.regex",
            ".*~\\..*|\\.DS_Store|\\.svn|CVS|\\.git|\\.gitignore|\\.gitattributes|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags");

        this.setDefault("queue.upload.priority.regex", "");

        /*
          Create temporary filename with an UUID and rename when upload is complete
         */
        this.setDefault("queue.upload.file.temporary", String.valueOf(false));
        /*
          Format string for temporary filename. Default to filename-uuid
         */
        this.setDefault("queue.upload.file.temporary.format", "{0}-{1}");

        this.setDefault("queue.upload.file.rename.format", "{0} ({1}){2}");
        this.setDefault("queue.download.file.rename.format", "{0} ({1}){2}");

        this.setDefault("queue.download.permissions.change", String.valueOf(true));
        this.setDefault("queue.download.permissions.default", String.valueOf(false));
        this.setDefault("queue.download.permissions.file.default", String.valueOf(644));
        this.setDefault("queue.download.permissions.folder.default", String.valueOf(755));

        this.setDefault("queue.download.timestamp.change", String.valueOf(true));
        this.setDefault("queue.download.checksum.calculate", String.valueOf(false));

        this.setDefault("queue.download.skip.enable", String.valueOf(true));
        this.setDefault("queue.download.skip.regex.default",
            ".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs|\\.file-segments");
        this.setDefault("queue.download.skip.regex",
            ".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs|\\.file-segments");

        this.setDefault("queue.download.priority.regex", "");

        this.setDefault("queue.download.folder", System.getProperty("user.dir"));
        // Security scoped bookmark
        this.setDefault("queue.download.folder.bookmark", null);

        this.setDefault("queue.download.quarantine", String.valueOf(true));
        this.setDefault("queue.download.wherefrom", String.valueOf(true));

        // Segmented concurrent downloads
        this.setDefault("queue.download.segments", String.valueOf(true));
        this.setDefault("queue.download.segments.threshold", String.valueOf(10L * 1024L * 1024L));
        this.setDefault("queue.download.segments.size", String.valueOf(5L * 1024L * 1024L));

        /*
          Open completed downloads
         */
        this.setDefault("queue.download.complete.open", String.valueOf(false));

        this.setDefault("queue.dock.badge", String.valueOf(false));

        this.setDefault("queue.sleep.prevent", String.valueOf(true));

        /*
          Bandwidth throttle options
         */
        {
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
            this.setDefault("queue.bandwidth.options", options.toString());
        }
        /*
          Bandwidth throttle upload stream
         */
        this.setDefault("queue.upload.bandwidth.bytes", String.valueOf(-1));
        /*
          Bandwidth throttle download stream
         */
        this.setDefault("queue.download.bandwidth.bytes", String.valueOf(-1));

        /*
         * Concurrent connections for single transfer and maximum number of concurrent transfers in transfer list
         */
        this.setDefault("queue.connections.limit", String.valueOf(2));
        {
            final StringBuilder options = new StringBuilder();
            options.append(1).append(",");
            options.append(2).append(",");
            options.append(3).append(",");
            options.append(4).append(",");
            options.append(5).append(",");
            options.append(10).append(",");
            options.append(15).append(",");
            options.append(20).append(",");
            this.setDefault("queue.connections.options", options.toString());
        }

        /*
          While downloading, update the icon of the downloaded file as a progress indicator
         */
        this.setDefault("queue.download.icon.update", String.valueOf(true));
        this.setDefault("queue.download.icon.threshold", String.valueOf(TransferStatus.MEGA * 5));

        /*
          Default synchronize action selected in the sync dialog
         */
        this.setDefault("queue.prompt.sync.action.default", TransferAction.mirror.name());
        this.setDefault("queue.prompt.download.action.default", TransferAction.overwrite.name());
        this.setDefault("queue.prompt.upload.action.default", TransferAction.overwrite.name());
        this.setDefault("queue.prompt.copy.action.default", TransferAction.overwrite.name());
        this.setDefault("queue.prompt.move.action.default", TransferAction.overwrite.name());

        this.setDefault("queue.transcript.open", String.valueOf(false));
        this.setDefault("queue.transcript.size.height", String.valueOf(200));

        this.setDefault("http.compression.enable", String.valueOf(true));

        /*
          HTTP routes to maximum number of connections allowed for those routes
         */
        this.setDefault("http.connections.route", String.valueOf(10));
        this.setDefault("http.connections.reuse", String.valueOf(true));
        this.setDefault("http.connections.stale.check.ms", String.valueOf(5000));
        /*
          Total number of connections in the pool
         */
        this.setDefault("http.connections.total", String.valueOf(Integer.MAX_VALUE));
        this.setDefault("http.connections.retry", String.valueOf(1));

        this.setDefault("http.manager.timeout", String.valueOf(0)); // Infinite
        this.setDefault("http.socket.buffer", String.valueOf(8192));
        this.setDefault("http.credentials.charset", "ISO-8859-1");
        this.setDefault("http.request.uri.normalize", String.valueOf(false));

        /*
          Enable or disable verification that the remote host taking part
          of a data connection is the same as the host to which the control
          connection is attached.
         */
        this.setDefault("ftp.datachannel.verify", String.valueOf(false));
        this.setDefault("ftp.socket.buffer", String.valueOf(0));

        this.setDefault("ftp.parser.multiline.strict", String.valueOf(false));
        this.setDefault("ftp.parser.reply.strict", String.valueOf(false));
        this.setDefault("ftp.parser.mlsd.perm.enable", String.valueOf(false));

        /*
          Send LIST -a
         */
        this.setDefault("ftp.command.lista", String.valueOf(true));
        this.setDefault("ftp.command.stat", String.valueOf(true));
        this.setDefault("ftp.command.mlsd", String.valueOf(true));

        /*
          Fallback to active or passive mode respectively
         */
        this.setDefault("ftp.connectmode.fallback", String.valueOf(false));
        /*
          Protect the data channel by default. For TLS, the data connection
          can have one of two security levels.
         1) Clear (requested by 'PROT C')
         2) Private (requested by 'PROT P')
         */
        this.setDefault("ftp.tls.datachannel", "P"); //C
        this.setDefault("ftp.tls.session.requirereuse", String.valueOf(true));
        this.setDefault("ftp.ssl.session.cache.size", String.valueOf(100));

        /*
          Try to determine the timezone automatically using timestamp comparison from MLST and LIST
         */
        this.setDefault("ftp.timezone.auto", String.valueOf(false));
        this.setDefault("ftp.timezone.default", TimeZone.getDefault().getID());

        /*
          Authentication header version
         */
        //this.setDefault("s3.signature.version", "AWS2");
        this.setDefault("s3.signature.version", "AWS4HMACSHA256");
        /*
          Default bucket location
         */
        this.setDefault("s3.location", "us-east-1");
        this.setDefault("s3.bucket.virtualhost.disable", String.valueOf(false));
        this.setDefault("s3.bucket.requesterpays", String.valueOf(true));
        this.setDefault("s3.domain", "amazonaws.com");
        this.setDefault("s3.hostname.default", "s3.amazonaws.com");

        //this.setDefault("s3.bucket.acl.default", "public-read");
        this.setDefault("s3.bucket.acl.default", "private");

        /*
          Default redundancy level
         */
        this.setDefault("s3.storage.class", "STANDARD");
        //this.setDefault("s3.encryption.algorithm", "AES256");
        this.setDefault("s3.encryption.algorithm", StringUtils.EMPTY);

        /*
          Validity for public S3 URLs
         */
        this.setDefault("s3.url.expire.seconds", String.valueOf(24 * 60 * 60));

        this.setDefault("s3.listing.chunksize", String.valueOf(1000));
        this.setDefault("s3.listing.concurrency", String.valueOf(25));

        this.setDefault("s3.upload.multipart", String.valueOf(true));
        this.setDefault("s3.upload.multipart.concurrency", String.valueOf(10));
        this.setDefault("s3.upload.multipart.partsize.minimum", String.valueOf(5L * 1024L * 1024L));
        /*
          Threshold in bytes. Only use multipart uploads for files more than 100MB
         */
        this.setDefault("s3.upload.multipart.threshold", String.valueOf(100L * 1024L * 1024L));
        this.setDefault("s3.upload.multipart.required.threshold", String.valueOf(5L * 1024L * 1024L * 1024L));
        // Maximum number of parts is 10'000. With 10MB segements this gives a maximum object size of 100GB
        // Must be a multiple of org.cryptomator.cryptolib.v1.Constants.PAYLOAD_SIZE when using Cryptomator Vaults
        this.setDefault("s3.upload.multipart.size", String.valueOf(10L * 1024L * 1024L)); // 10MB
        this.setDefault("s3.copy.multipart.size", String.valueOf(100L * 1024L * 1024L)); // 100MB

        this.setDefault("s3.upload.expect-continue", String.valueOf(true));

        /*
          Transfer thresholds for qloudsonic.io
         */
        this.setDefault("s3.download.udt.threshold", String.valueOf(Long.MAX_VALUE));
        this.setDefault("s3.upload.udt.threshold", String.valueOf(Long.MAX_VALUE));

        this.setDefault("s3.accelerate.prompt", String.valueOf(false));

        this.setDefault("s3.versioning.enable", String.valueOf(true));

        /*
          A prefix to apply to log file names
         */
        this.setDefault("s3.logging.prefix", "logs/");
        this.setDefault("google.logging.prefix", "log");
        this.setDefault("cloudfront.logging.prefix", "logs/");

        this.setDefault("googlestorage.listing.chunksize", String.valueOf(1000));
        this.setDefault("googlestorage.metadata.default", StringUtils.EMPTY);
        this.setDefault("googlestorage.storage.class", "multi_regional");


        this.setDefault("onedrive.listing.chunksize", String.valueOf(1000));
        /*
         * The size of each byte range MUST be a multiple of 320 KiB (327,680 bytes). Using a fragment size that does not
         * divide evenly by 320 KiB will result in errors committing some files.
         */
        this.setDefault("onedrive.upload.multipart.partsize.minimum", String.valueOf(320 * 1024));

        final int month = 60 * 60 * 24 * 30; //30 days in seconds
        this.setDefault("s3.cache.seconds", String.valueOf(month));

        /*
          Default metadata for uploads. Format must be "key1=value1 key2=value2"
         */
        this.setDefault("s3.metadata.default", StringUtils.EMPTY);

        this.setDefault("s3.lifecycle.transition.options", "1 7 10 30 60 180 360 720");
        this.setDefault("s3.lifecycle.delete.options", "1 7 10 30 60 180 360 720");

        this.setDefault("s3.delete.multiple.partition", String.valueOf(1000));

        this.setDefault("azure.metadata.default", StringUtils.EMPTY);
        this.setDefault("azure.listing.chunksize", String.valueOf(1000));
        this.setDefault("azure.upload.md5", String.valueOf(false));
        this.setDefault("azure.upload.snapshot", String.valueOf(false));
        this.setDefault("azure.upload.blobtype", "APPEND_BLOB");

        // Legacy authentication
//        this.setDefault("openstack.authentication.context", "/v1.0");
        // Keystone authentication
        this.setDefault("openstack.authentication.context", "/v2.0/tokens");
        this.setDefault("openstack.metadata.default", StringUtils.EMPTY);
        this.setDefault("openstack.list.container.limit", String.valueOf(100));
        this.setDefault("openstack.list.object.limit", String.valueOf(10000));
        this.setDefault("openstack.account.preload", String.valueOf(true));
        this.setDefault("openstack.cdn.preload", String.valueOf(true));
        this.setDefault("openstack.container.size.preload", String.valueOf(true));

        this.setDefault("openstack.upload.largeobject", String.valueOf(true));
        this.setDefault("openstack.upload.largeobject.concurrency", String.valueOf(5));
        this.setDefault("openstack.upload.largeobject.segments.prefix", ".file-segments/");
        this.setDefault("openstack.upload.largeobject.threshold", String.valueOf(2L * 1024L * 1024L * 1024L)); // 2GB
        this.setDefault("openstack.upload.largeobject.required.threshold", String.valueOf(5L * 1024L * 1024L * 1024L)); // 5GB
        this.setDefault("openstack.upload.largeobject.size", String.valueOf(1000L * 1024L * 1024L)); // 1GB
        // Each segment, except for the final one, must be at least 1 megabyte
        this.setDefault("openstack.upload.largeobject.size.minimum", String.valueOf(1 * 1024L * 1024L)); // 1MB
        // Remove segments when deleting large object manifest
        this.setDefault("openstack.upload.largeobject.cleanup", String.valueOf(true));

        this.setDefault("openstack.delete.multiple.partition", String.valueOf(10000));

        this.setDefault("googledrive.list.limit", String.valueOf(1000));
        this.setDefault("googledrive.teamdrive.enable", String.valueOf(true));
        this.setDefault("googledrive.delete.trash", String.valueOf(true));
        // Limit the number of requests to 10 per second which is equal the user quota
        this.setDefault("googledrive.limit.requests.second", String.valueOf(100));

        this.setDefault("b2.bucket.acl.default", "allPrivate");
        this.setDefault("b2.listing.chunksize", String.valueOf(1000));
        this.setDefault("b2.upload.checksum.verify", String.valueOf(true));

        this.setDefault("b2.upload.largeobject", String.valueOf(true));
        this.setDefault("b2.upload.largeobject.concurrency", String.valueOf(5));
        this.setDefault("b2.upload.largeobject.required.threshold", String.valueOf(5L * 1024L * 1024L * 1024L)); // 5GB
        // When uploading files larger than 200MB, use the large files support to break up the files into parts and upload the parts in parallel.
        this.setDefault("b2.upload.largeobject.threshold", String.valueOf(200 * 1024L * 1024L)); // 200MB
        // Each part can be anywhere from 100MB to 5GB in size
        this.setDefault("b2.upload.largeobject.size", String.valueOf(100 * 1024L * 1024L));
        this.setDefault("b2.upload.largeobject.size.minimum", String.valueOf(5 * 1024L * 1024L));

        this.setDefault("b2.metadata.default", StringUtils.EMPTY);

        this.setDefault("sds.version.lts", "4.12");
        this.setDefault("sds.listing.chunksize", String.valueOf(500));
        this.setDefault("sds.upload.multipart.chunksize", String.valueOf(2 * 1024L * 1024L));
        // Run missing file keys in bulk feature after upload
        this.setDefault("sds.encryption.missingkeys.upload", String.valueOf(true));
        this.setDefault("sds.encryption.missingkeys.scheduler.period", String.valueOf(120000)); // 2 minutes
        this.setDefault("sds.encryption.keys.ttl", String.valueOf(3600000)); // 1 hour
        this.setDefault("sds.useracount.ttl", String.valueOf(3600000)); // 1 hour
        this.setDefault("sds.delete.dataroom.enable", String.valueOf(true));
        this.setDefault("sds.upload.sharelinks.keep", String.valueOf(true));

        this.setDefault("spectra.retry.delay", String.valueOf(60)); // 1 minute

        this.setDefault("storegate.listing.chunksize", String.valueOf(500));
        this.setDefault("storegate.upload.multipart.chunksize", String.valueOf(0.5 * 1024L * 1024L));
        this.setDefault("storegate.lock.ttl", String.valueOf(24 * 3600000)); // 24 hours

        this.setDefault("brick.pairing.nickname.configure", String.valueOf(false));
        this.setDefault("brick.pairing.hostname.configure", String.valueOf(true));
        this.setDefault("brick.pairing.interval.ms", String.valueOf(1000L));

        this.setDefault("dropbox.upload.chunksize", String.valueOf(150 * 1024L * 1024L));

        /*
          NTLM Windows Domain
         */
        this.setDefault("webdav.ntlm.domain", StringUtils.EMPTY);
        this.setDefault("webdav.ntlm.workstation", StringUtils.EMPTY);

        /**
         * Enable Integrated Windows Authentication (IWA) for target server authentication
         */
        this.setDefault("webdav.ntlm.windows.authentication.enable", String.valueOf(false));

        /*
          Enable preemptive authentication if valid credentials are found
         */
        this.setDefault("webdav.basic.preemptive", String.valueOf(true));

        /*
          Enable Expect-Continue handshake
         */
        this.setDefault("webdav.expect-continue", String.valueOf(true));
        this.setDefault("webdav.redirect.GET.follow", String.valueOf(true));
        this.setDefault("webdav.redirect.HEAD.follow", String.valueOf(true));
        this.setDefault("webdav.redirect.PUT.follow", String.valueOf(false));
        this.setDefault("webdav.redirect.PROPFIND.follow", String.valueOf(true));

        this.setDefault("webdav.metadata.default", StringUtils.EMPTY);

        this.setDefault("webdav.microsoftiis.header.translate", String.valueOf(true));

        this.setDefault("webdav.list.handler.sax", String.valueOf(true));

        this.setDefault("analytics.provider.qloudstat.setup", "https://qloudstat.com/configuration/add");
        this.setDefault("analytics.provider.qloudstat.iam.policy",
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
        this.setDefault("connection.pool.minidle", String.valueOf(1));
        this.setDefault("connection.pool.maxidle", String.valueOf(5));
        this.setDefault("connection.pool.maxtotal", String.valueOf(Integer.MAX_VALUE));

        /*
          Default login name
         */
        this.setDefault("connection.login.name", StringUtils.EMPTY);
        this.setDefault("connection.login.anon.name", "anonymous");
        this.setDefault("connection.login.anon.pass", "cyberduck@example.net");
        /*
          Search for passphrases in Keychain
         */
        this.setDefault("connection.login.keychain", String.valueOf(true));
        /*
         * Save passwords for vaults in Keychain
         */
        this.setDefault("vault.keychain", String.valueOf(false));

        this.setDefault("connection.port.default", String.valueOf(21));
        this.setDefault("connection.protocol.default", Scheme.ftp.name());

        /*
          SO_KEEPALIVE
         */
        this.setDefault("connection.socket.keepalive", String.valueOf(true));
        /*
          SO_LINGER
         */
        this.setDefault("connection.socket.linger", String.valueOf(false));
        /*
          Socket timeout
         */
        this.setDefault("connection.timeout.seconds", String.valueOf(30));
        /*
          Retry to connect after a I/O failure automatically
         */
        this.setDefault("connection.retry", String.valueOf(1));
        // Specific setting for transfer worker
        this.setDefault("transfer.connection.retry", String.valueOf(1));
        this.setDefault("connection.retry.max", String.valueOf(20));
        /*
          In seconds
         */
        this.setDefault("connection.retry.delay", String.valueOf(0));
        // Specific setting for transfer worker
        this.setDefault("transfer.connection.retry.delay", String.valueOf(0));
        this.setDefault("connection.retry.backoff.enable", String.valueOf(false));

        /**
         * Enable login prompt in connect retry
         */
        this.setDefault("connection.retry.login.enable", String.valueOf(true));

        this.setDefault("connection.hostname.default", StringUtils.EMPTY);
        /*
          Convert hostname to Punycode
         */
        this.setDefault("connection.hostname.idn", String.valueOf(true));

        /*
          java.net.preferIPv6Addresses
         */
        this.setDefault("connection.dns.ipv6", String.valueOf(false));
        // Ticket #2539
        if(this.getBoolean("connection.dns.ipv6")) {
            System.setProperty("java.net.preferIPv6Addresses", String.valueOf(true));
        }

        /*
          Read proxy settings from system preferences
         */
        this.setDefault("connection.proxy.enable", String.valueOf(true));
        this.setDefault("connection.proxy.ntlm.domain", StringUtils.EMPTY);
        /*
          Integrated Windows Authentication (IWA)
         */
        this.setDefault("connection.proxy.windows.authentication.enable", String.valueOf(false));

        /*
          Warning when opening connections sending credentials in plaintext
         */
        this.setDefault(String.format("connection.unsecure.warning.%s", Scheme.ftp), String.valueOf(true));
        this.setDefault(String.format("connection.unsecure.warning.%s", Scheme.http), String.valueOf(true));

        this.setDefault("connection.ssl.provider.bouncycastle.position", String.valueOf(1));
        // Failure loading default key store with bouncycastle provider
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        // Register bouncy castle as preferred provider. Used in Cyptomator, SSL and SSH
        final int position = this.getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        // Add missing factory. http://bouncy-castle.1462172.n4.nabble.com/Keychain-issue-as-of-version-1-53-follow-up-tc4659509.html
        provider.put("Alg.Alias.SecretKeyFactory.PBE", "PBEWITHSHAAND3-KEYTRIPLEDES-CBC");
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);
        this.setDefault("connection.ssl.protocols", "TLSv1.2,TLSv1.1,TLSv1");
        this.setDefault("connection.ssl.cipher.blacklist", StringUtils.EMPTY);

        this.setDefault("connection.ssl.x509.revocation.online", String.valueOf(false));

        this.setDefault("connection.ssl.keystore.type", null);
        this.setDefault("connection.ssl.keystore.provider", null);

        // Default secure random strong algorithm
        this.setDefault("connection.ssl.securerandom.algorithm", "NativePRNG");
        this.setDefault("connection.ssl.securerandom.provider", "SUN");

        /*
          Transfer read buffer size
         */
        this.setDefault("connection.chunksize", String.valueOf(32768));
        /*
          Buffer size for wrapped buffered streams
         */
        this.setDefault("connection.buffer", String.valueOf(8192));
        /*
          SO_SNDBUF
         */
        this.setDefault("connection.buffer.send", String.valueOf(0));
        /*
          SO_RCVBUF
         */
        this.setDefault("connection.buffer.receive", String.valueOf(0));

        this.setDefault("disk.unmount.timeout", String.valueOf(2));

        /*
          Read favicon from Web URL
         */
        this.setDefault("bookmark.favicon.download", String.valueOf(true));

        /*
          Default to large icon size
         */
        this.setDefault("bookmark.icon.size", String.valueOf(64));
        this.setDefault("bookmark.menu.icon.size", String.valueOf(64));

        /*
          Location of the openssh known_hosts file
         */
        this.setDefault("ssh.knownhosts", "~/.ssh/known_hosts");
        this.setDefault("ssh.knownhosts.hostname.hash", String.valueOf(false));
        this.setDefault("ssh.knownhosts.bookmark", StringUtils.EMPTY);

        this.setDefault("ssh.authentication.publickey.default.enable", String.valueOf(false));
        this.setDefault("ssh.authentication.publickey.default.rsa", "~/.ssh/id_rsa");
        this.setDefault("ssh.authentication.publickey.default.dsa", "~/.ssh/id_dsa");

        this.setDefault("ssh.authentication.agent.enable", String.valueOf(true));

        this.setDefault("ssh.heartbeat.provider", "keep-alive");
        this.setDefault("ssh.heartbeat.seconds", String.valueOf(60));

        /*
          Enable ZLIB compression
         */
        this.setDefault("ssh.compression", "zlib");

        this.setDefault("ssh.algorithm.cipher.blacklist", StringUtils.EMPTY);
        this.setDefault("ssh.algorithm.mac.blacklist", StringUtils.EMPTY);
        this.setDefault("ssh.algorithm.kex.blacklist", StringUtils.EMPTY);
        this.setDefault("ssh.algorithm.signature.blacklist", StringUtils.EMPTY);

        this.setDefault("sftp.read.maxunconfirmed", String.valueOf(64));
        this.setDefault("sftp.write.maxunconfirmed", String.valueOf(64));

        this.setDefault("archive.default", "tar.gz");

        /*
          Archiver
         */
        this.setDefault("archive.command.create.tar", "cd {2}; tar -cpPf {0}.tar {1}");
        this.setDefault("archive.command.create.tar.gz", "cd {2}; tar -czpPf {0}.tar.gz {1}");
        this.setDefault("archive.command.create.tar.bz2", "cd {2}; tar -cjpPf {0}.tar.bz2 {1}");
        this.setDefault("archive.command.create.zip", "cd {2}; zip -qr {0}.zip {1}");
        this.setDefault("archive.command.create.gz", "gzip -qr {1}");
        this.setDefault("archive.command.create.bz2", "bzip2 -zk {1}");

        /*
          Unarchiver
         */
        this.setDefault("archive.command.expand.tar", "tar -xpPf {0} -C {1}");
        this.setDefault("archive.command.expand.tar.gz", "tar -xzpPf {0} -C {1}");
        this.setDefault("archive.command.expand.tar.bz2", "tar -xjpPf {0} -C {1}");
        this.setDefault("archive.command.expand.zip", "unzip -qn {0} -d {1}");
        this.setDefault("archive.command.expand.gz", "gzip -d {0}");
        this.setDefault("archive.command.expand.bz2", "bzip2 -dk {0}");

        this.setDefault("update.feed", "release");
        this.setDefault("update.feed.nightly.enable", String.valueOf(true));
        this.setDefault("update.feed.beta.enable", String.valueOf(true));

        this.setDefault("update.check", String.valueOf(true));
        final int day = 60 * 60 * 24;
        this.setDefault("update.check.interval", String.valueOf(day)); // periodic update check in seconds
        // Last update check in milliseconds
        this.setDefault("update.check.timestamp", String.valueOf(0));

        this.setDefault("terminal.bundle.identifier", "com.apple.Terminal");
        this.setDefault("terminal.command", "do script \"{0}\"");
        this.setDefault("terminal.command.ssh", "ssh -t {0} {1}@{2} -p {3} \"cd {4} && exec \\$SHELL --login\"");

        this.setDefault("network.interface.blacklist", StringUtils.EMPTY);

        this.setDefault("threading.pool.size.max", String.valueOf(20));
        this.setDefault("threading.pool.keepalive.seconds", String.valueOf(60L));

        this.setDefault("cryptomator.enable", String.valueOf(true));
        this.setDefault("cryptomator.vault.autodetect", String.valueOf(true));
    }

    protected void setLogging() {
        this.setLogging(this.getProperty("logging"));
    }

    /**
     * Reconfigure logging configuration
     *
     * @param level Log level
     */
    public void setLogging(final String level) {
        this.setProperty("logging", level);
        // Call only once during initialization time of your application
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        // Apply default configuration
        final URL configuration;
        final String file = this.getDefault("logging.config");
        if(null == file) {
            configuration = Preferences.class.getClassLoader().getResource("log4j-default.xml");
        }
        else {
            configuration = Preferences.class.getClassLoader().getResource(file);
        }
        LogManager.resetConfiguration();
        final Logger root = Logger.getRootLogger();
        if(null != configuration) {
            DOMConfigurator.configure(configuration);
        }
        // Allow to override default logging level
        root.setLevel(Level.toLevel(level, Level.ERROR));
        // Map logging level to pass through bridge
        final ImmutableMap<Level, java.util.logging.Level> map = new ImmutableMap.Builder<Level, java.util.logging.Level>()
            .put(Level.ALL, java.util.logging.Level.ALL)
            .put(Level.DEBUG, java.util.logging.Level.FINE)
            .put(Level.ERROR, java.util.logging.Level.SEVERE)
            .put(Level.FATAL, java.util.logging.Level.SEVERE)
            .put(Level.INFO, java.util.logging.Level.INFO)
            .put(Level.OFF, java.util.logging.Level.OFF)
            .put(Level.TRACE, java.util.logging.Level.FINEST)
            .put(Level.WARN, java.util.logging.Level.WARNING)
            .build();
        java.util.logging.Logger.getLogger("").setLevel(map.get(root.getLevel()));
        final Enumeration loggers = LogManager.getCurrentLoggers();
        while(loggers.hasMoreElements()) {
            final Logger logger = (Logger) loggers.nextElement();
            if(logger.getLevel() != null) {
                java.util.logging.Logger.getLogger(logger.getName()).setLevel(map.get(logger.getLevel()));
            }
        }
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
        this.setDefault("factory.serializer.class", PlistSerializer.class.getName());
        this.setDefault("factory.deserializer.class", PlistDeserializer.class.getName());
        this.setDefault("factory.reader.profile.class", ProfilePlistReader.class.getName());
        this.setDefault("factory.writer.profile.class", PlistWriter.class.getName());
        this.setDefault("factory.reader.transfer.class", TransferPlistReader.class.getName());
        this.setDefault("factory.writer.transfer.class", PlistWriter.class.getName());
        this.setDefault("factory.reader.host.class", HostPlistReader.class.getName());
        this.setDefault("factory.writer.host.class", PlistWriter.class.getName());

        this.setDefault("factory.locale.class", DisabledLocale.class.getName());
        this.setDefault("factory.local.class", Local.class.getName());
        this.setDefault("factory.certificatestore.class", DisabledCertificateStore.class.getName());
        this.setDefault("factory.logincallback.class", DisabledLoginCallback.class.getName());
        this.setDefault("factory.passwordcallback.class", DisabledPasswordCallback.class.getName());
        this.setDefault("factory.certificatetrustcallback.class", DisabledCertificateTrustCallback.class.getName());
        this.setDefault("factory.certificateidentitycallback.class", DisabledCertificateIdentityCallback.class.getName());
        this.setDefault("factory.alertcallback.class", DisabledAlertCallback.class.getName());
        this.setDefault("factory.hostkeycallback.class", DisabledHostKeyCallback.class.getName());
        this.setDefault("factory.transfererrorcallback.class", DisabledTransferErrorCallback.class.getName());
        this.setDefault("factory.temporaryfiles.class", DefaultTemporaryFileService.class.getName());
        this.setDefault("factory.touch.class", DefaultLocalTouchFeature.class.getName());
        this.setDefault("factory.autorelease.class", DisabledActionOperationBatcher.class.getName());
        this.setDefault("factory.schemehandler.class", DisabledSchemeHandler.class.getName());
        this.setDefault("factory.iconservice.class", DisabledIconService.class.getName());
        this.setDefault("factory.iconcache.class", DisabledIconCache.class.getName());
        this.setDefault("factory.notification.class", DisabledNotificationService.class.getName());
        this.setDefault("factory.sleeppreventer.class", DisabledSleepPreventer.class.getName());
        this.setDefault("factory.quarantine.class", DisabledQuarantineService.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            this.setDefault(String.format("factory.transferpromptcallback.%s.class", t.name()), DisabledTransferPrompt.class.getName());
        }
        this.setDefault("factory.supportdirectoryfinder.class", TemporarySupportDirectoryFinder.class.getName());
        this.setDefault("factory.localsupportdirectoryfinder.class", TemporarySupportDirectoryFinder.class.getName());
        this.setDefault("factory.applicationresourcesfinder.class", TemporaryApplicationResourcesFinder.class.getName());
        this.setDefault("factory.workingdirectory.class", DefaultWorkingDirectoryFinder.class.getName());
        this.setDefault("factory.bookmarkresolver.class", DisabledFilesystemBookmarkResolver.class.getName());
        this.setDefault("factory.watchservice.class", NIOEventWatchService.class.getName());
        this.setDefault("factory.proxy.class", DisabledProxyFinder.class.getName());
        this.setDefault("factory.passwordstore.class", DisabledPasswordStore.class.getName());
        this.setDefault("factory.proxycredentialsstore.class", PreferencesProxyCredentialsStore.class.getName());
        this.setDefault("factory.dateformatter.class", DefaultUserDateFormatter.class.getName());
        this.setDefault("factory.trash.class", NativeLocalTrashFeature.class.getName());
        this.setDefault("factory.symlink.class", NullLocalSymlinkFeature.class.getName());
        this.setDefault("factory.licensefactory.class", DonationKeyFactory.class.getName());
        this.setDefault("factory.badgelabeler.class", DisabledApplicationBadgeLabeler.class.getName());
        this.setDefault("factory.filedescriptor.class", NullFileDescriptor.class.getName());
        this.setDefault("factory.terminalservice.class", DisabledTerminalService.class.getName());
        this.setDefault("factory.applicationfinder.class", DisabledApplicationFinder.class.getName());
        this.setDefault("factory.applicationlauncher.class", DisabledApplicationLauncher.class.getName());
        this.setDefault("factory.browserlauncher.class", DisabledBrowserLauncher.class.getName());
        this.setDefault("factory.reachability.class", DefaultInetAddressReachability.class.getName());
        this.setDefault("factory.updater.class", DisabledPeriodicUpdater.class.getName());
        this.setDefault("factory.updater.arguments.class", DisabledUpdateCheckerArguments.class.getName());
        this.setDefault("factory.threadpool.class", DefaultThreadPool.class.getName());
        this.setDefault("factory.urlfilewriter.class", InternetShortcutFileWriter.class.getName());
        this.setDefault("factory.vault.class", DisabledVault.class.getName());
        this.setDefault("factory.securerandom.class", DefaultSecureRandomProvider.class.getName());
        this.setDefault("factory.providerhelpservice.class", DefaultProviderHelpService.class.getName());
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
     * @return The preferred locale of all localizations available in this application bundle
     */
    public String locale() {
        return this.applicationLocales().iterator().next();
    }

    /**
     * The localizations available in this application bundle sorted by preference by the user.
     *
     * @return Available locales in application bundle
     */
    @Override
    public abstract List<String> applicationLocales();

    /**
     * @return Available locales in system
     */
    @Override
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
