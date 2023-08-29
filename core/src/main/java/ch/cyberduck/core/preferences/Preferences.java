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
import ch.cyberduck.core.diagnostics.DisabledReachability;
import ch.cyberduck.core.exception.AccessDeniedException;
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
import ch.cyberduck.core.notification.DisabledNotificationFilterService;
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
import ch.cyberduck.core.updater.DisabledPeriodicUpdater;
import ch.cyberduck.core.updater.DisabledUpdateCheckerArguments;
import ch.cyberduck.core.urlhandler.DisabledSchemeHandler;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.DisabledVault;
import ch.cyberduck.core.webloc.InternetShortcutFileWriter;
import ch.cyberduck.ui.quicklook.ApplicationLauncherQuicklook;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.DeleteAction;
import org.apache.logging.log4j.core.appender.rolling.action.IfAccumulatedFileCount;
import org.apache.logging.log4j.core.appender.rolling.action.IfFileName;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PathSortByModificationTime;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.NullConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.Deflater;

import com.google.common.collect.ImmutableMap;

/**
 * Holding all application preferences. Default values get overwritten when loading the <code>PREFERENCES_FILE</code>.
 * Singleton class.
 */
public abstract class Preferences implements Locales, PreferencesReader {
    private static final Logger log = LogManager.getLogger(Preferences.class);

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

    public static final class Version {
        private final Package pkg;

        public Version() {
            this(Version.class.getPackage());
        }

        public Version(final Package pkg) {
            this.pkg = pkg;
        }

        /**
         * @return The <code>Specification-Version</code> in the JAR manifest.
         */
        public String getSpecification() {
            return (pkg == null) ? null : pkg.getSpecificationVersion();
        }

        /**
         * @return The <code>Implementation-Version</code> in the JAR manifest.
         */
        public String getImplementation() {
            return (pkg == null) ? null : pkg.getImplementationVersion();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Version{");
            sb.append("version='").append(getSpecification()).append('\'');
            sb.append(", hash='").append(getImplementation()).append('\'');
            sb.append('}');
            return sb.toString();
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
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            catch(IllegalArgumentException | AccessDeniedException | IOException e) {
                // Ignore failure loading configuration
            }
            for(Map.Entry<Object, Object> entry : props.entrySet()) {
                this.setDefault(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    private void loadDefaults(final String name) {
        final InputStream in = Preferences.class.getResourceAsStream(String.format("/%s", name));
        if(in != null) {
            try {
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                this.setDefaults(properties);
            }
            catch(IOException e) {
                //
            }
            finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    /**
     * setting the default prefs values
     */
    protected void setDefaults() {
        this.loadDefaults("default.properties");

        this.setDefault("os.name", System.getProperty("os.name"));
        this.setDefault("os.version", System.getProperty("os.version"));
        this.setDefault("os.arch", System.getProperty("os.arch"));

        final Version version = new Version();
        this.setDefault("application.version", StringUtils.substringBeforeLast(version.getSpecification(), "."));
        this.setDefault("application.revision", StringUtils.substringAfterLast(version.getSpecification(), "."));
        this.setDefault("application.hash", version.getImplementation());

        this.setDefault("tmp.dir", System.getProperty("java.io.tmpdir"));
        this.setDefault("local.user.home", System.getProperty("user.home"));
        this.setDefault("local.delimiter", File.separator);
        this.setDefault("queue.download.folder", System.getProperty("user.dir"));
        this.setDefault("ftp.timezone.default", TimeZone.getDefault().getID());
        // Ticket #2539
        if(this.getBoolean("connection.dns.ipv6")) {
            System.setProperty("java.net.preferIPv6Addresses", String.valueOf(true));
        }
        this.setDefault(String.format("connection.unsecure.warning.%s", Scheme.ftp), String.valueOf(true));
        this.setDefault(String.format("connection.unsecure.warning.%s", Scheme.http), String.valueOf(true));

        // TTL for DNS queries
        Security.setProperty("networkaddress.cache.ttl", "10");
        Security.setProperty("networkaddress.cache.negative.ttl", "5");
        // Failure loading default key store with bouncycastle provider
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        // Allow parsing of malformed ASN.1 integers in a similar fashion to what BC 1.56 did
        System.setProperty("org.bouncycastle.asn1.allow_unsafe_integer", String.valueOf(true));
        // Register bouncy castle as preferred provider. Used in Cyptomator, SSL and SSH
        final int position = this.getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        // Add missing factory. http://bouncy-castle.1462172.n4.nabble.com/Keychain-issue-as-of-version-1-53-follow-up-tc4659509.html
        provider.put("Alg.Alias.SecretKeyFactory.PBE", "PBEWITHSHAAND3-KEYTRIPLEDES-CBC");
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);

        System.setProperty("jdk.tls.useExtendedMasterSecret", String.valueOf(false));
        // If true, the client will send a session ticket extension in the ClientHello for TLS 1.2 and earlier.
        // Set to false as statless session resumption breaks session reuse in FTPS
        System.setProperty("jdk.tls.client.enableSessionTicketExtension", String.valueOf(false));
    }

    /**
     * Set new log level and reconfigure logging configuration appropriately
     *
     * @param level Log level
     */
    public void setLogging(final String level) {
        this.setProperty("logging", level);
        this.configureLogging(level);
    }

    /**
     * Reconfigure logging configuration
     *
     * @param level Log level
     */
    protected void configureLogging(final String level) {
        // Call only once during initialization time of your application
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        // Apply default configuration
        final LoggerContext context = Configurator.initialize(new DefaultConfiguration());
        final InputStream config = this.getLogConfiguration();
        if(null != config) {
            try {
                context.initialize();
                Configurator.initialize(null, new ConfigurationSource(config));
            }
            catch(IOException e) {
                log.error("Failure configuring log4j", e);
            }
        }
        // Allow to override default logging level
        Configurator.setRootLevel(Level.toLevel(level, Level.ERROR));
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
        java.util.logging.Logger.getLogger("").setLevel(map.get(LogManager.getRootLogger().getLevel()));
        final LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
        final Collection<LoggerConfig> loggerConfigs = logContext.getConfiguration().getLoggers().values();
        for(LoggerConfig loggerConfig : loggerConfigs) {
            if(loggerConfig.getLevel() != null) {
                java.util.logging.Logger.getLogger(loggerConfig.getName()).setLevel(map.get(loggerConfig.getLevel()));
            }
        }
        this.configureAppenders(level);
    }

    private InputStream getLogConfiguration() {
        final Local folder = SupportDirectoryFinderFactory.get().find();
        if(folder.exists()) {
            try {
                for(Local log4jxml : folder.list().filter(new NullFilter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "log4j.xml".equals(file.getName());
                    }
                })) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Using log4j configuration from %s", log4jxml));
                    }
                    return log4jxml.getInputStream();
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Unable to list %s", folder), e);
            }
        }
        final URL configuration;
        final String file = this.getDefault("logging.config");
        if(null == file) {
            configuration = Preferences.class.getClassLoader().getResource("log4j.xml");
        }
        else {
            configuration = Preferences.class.getClassLoader().getResource(file);
        }
        if(null != configuration) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Using log4j configuration from %s", configuration));
            }
            try {
                return configuration.openStream();
            }
            catch(IOException e) {
                log.error(String.format(String.format("Unable to load log4j configuration from %s", configuration)), e);
            }
        }
        return null;
    }

    protected void configureAppenders(final String level) {
        final String logfolder = LogDirectoryFinderFactory.get().find().getAbsolute();
        final String appname = StringUtils.replaceChars(StringUtils.lowerCase(this.getProperty("application.name")), StringUtils.SPACE, StringUtils.EMPTY);
        final Local active = LocalFactory.get(logfolder, String.format("%s.log", appname));
        final Local archives = LocalFactory.get(logfolder, String.format("%s-%%d{yyyy-MM-dd'T'HHmmss}.log.zip", appname));
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final DeleteAction deleteAction = DeleteAction.createDeleteAction(logfolder, false, 1, false,
                PathSortByModificationTime.createSorter(true),
                new PathCondition[]{
                        IfFileName.createNameCondition(String.format("%s-*.log.zip", appname), null, IfAccumulatedFileCount.createFileCountCondition(this.getInteger("logging.archives")))
                },
                null, new NullConfiguration());
        final Appender appender = RollingFileAppender.newBuilder()
                .setName(RollingFileAppender.class.getName())
                .withFileName(active.getAbsolute())
                .withFilePattern(archives.getAbsolute())
                .withPolicy(Level.DEBUG.toString().equals(level) ? SizeBasedTriggeringPolicy.createPolicy("100MB") : SizeBasedTriggeringPolicy.createPolicy("10MB"))
                .withStrategy(DefaultRolloverStrategy.newBuilder().
                        withCompressionLevelStr(String.valueOf(Deflater.BEST_COMPRESSION)).
                        withCustomActions(new Action[]{new AbstractAction() {
                            @Override
                            public boolean execute() {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Running version %s", getVersion()));
                                }
                                return true;
                            }
                        }, deleteAction}).build())
                .setLayout(PatternLayout.newBuilder().withConfiguration(config).withPattern("%d [%t] %-5p %c - %m%n").withCharset(StandardCharsets.UTF_8).build())
                .build();
        appender.start();
        config.addAppender(appender);
        config.getRootLogger().addAppender(appender, null, null);
        ctx.updateLoggers();
    }

    @Override
    public List<String> getList(final String property) {
        final String value = this.getProperty(property);
        return PreferencesReader.toList(value);
    }

    @Override
    public int getInteger(final String key) {
        final String v = this.getProperty(key);
        return PreferencesReader.toInteger(v);
    }

    @Override
    public float getFloat(final String key) {
        final String v = this.getProperty(key);
        return PreferencesReader.toFloat(v);
    }

    @Override
    public long getLong(final String key) {
        final String v = this.getProperty(key);
        return PreferencesReader.toLong(v);
    }

    @Override
    public double getDouble(final String key) {
        final String v = this.getProperty(key);
        return PreferencesReader.toDouble(v);
    }

    @Override
    public boolean getBoolean(final String key) {
        final String v = this.getProperty(key);
        return PreferencesReader.toBoolean(v);
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
        this.setDefault("factory.notification.filter.class", DisabledNotificationFilterService.class.getName());
        this.setDefault("factory.sleeppreventer.class", DisabledSleepPreventer.class.getName());
        this.setDefault("factory.quarantine.class", DisabledQuarantineService.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            this.setDefault(String.format("factory.transferpromptcallback.%s.class", t.name()), DisabledTransferPrompt.class.getName());
        }
        this.setDefault("factory.supportdirectoryfinder.class", TemporarySupportDirectoryFinder.class.getName());
        this.setDefault("factory.logdirectoryfinder.class", SupportDirectoryLogDirectoryFinder.class.getName());
        this.setDefault("factory.localsupportdirectoryfinder.class", TemporarySupportDirectoryFinder.class.getName());
        this.setDefault("factory.applicationresourcesfinder.class", TemporaryApplicationResourcesFinder.class.getName());
        this.setDefault("factory.applicationloginregistry.class", DisabledApplicationLoginRegistry.class.getName());
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
        this.setDefault("factory.reachability.class", DisabledReachability.class.getName());
        this.setDefault("factory.updater.class", DisabledPeriodicUpdater.class.getName());
        this.setDefault("factory.updater.arguments.class", DisabledUpdateCheckerArguments.class.getName());
        this.setDefault("factory.threadpool.class", DefaultThreadPool.class.getName());
        this.setDefault("factory.urlfilewriter.class", InternetShortcutFileWriter.class.getName());
        this.setDefault("factory.vault.class", DisabledVault.class.getName());
        this.setDefault("factory.vaultregistry.class", DefaultVaultRegistry.class.getName());
        this.setDefault("factory.securerandom.class", DefaultSecureRandomProvider.class.getName());
        this.setDefault("factory.providerhelpservice.class", DefaultProviderHelpService.class.getName());
        this.setDefault("factory.quicklook.class", ApplicationLauncherQuicklook.class.getName());
        this.setDefault("factory.connectiontimeout.class", DefaultConnectionTimeout.class.getName());
        this.setDefault("factory.authorizationcodeprovider.class", "ch.cyberduck.core.oauth.BrowserOAuth2AuthorizationCodeProvider");
        this.setDefault("factory.s3.pathcontainerservice.class", "ch.cyberduck.core.s3.S3PathContainerService");
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

    public String getVersion() {
        return String.format("%s.%s (%s)",
                this.getProperty("application.version"),
                this.getProperty("application.revision"),
                this.getProperty("application.hash"));
    }
}
