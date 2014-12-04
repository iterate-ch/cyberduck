package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DisabledLocale;
import ch.cyberduck.core.DisabledProxyFinder;
import ch.cyberduck.core.DisabledRendezvous;
import ch.cyberduck.core.DisabledSleepPreventer;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.MemoryPreferences;
import ch.cyberduck.core.SystemConfigurationProxy;
import ch.cyberduck.core.SystemConfigurationReachability;
import ch.cyberduck.core.aquaticprime.DonationKeyFactory;
import ch.cyberduck.core.date.DefaultUserDateFormatter;
import ch.cyberduck.core.editor.DefaultEditorFactory;
import ch.cyberduck.core.editor.FSEventWatchEditorFactory;
import ch.cyberduck.core.i18n.BundleLocale;
import ch.cyberduck.core.local.*;
import ch.cyberduck.core.preferences.ApplicationSupportDirectoryFinder;
import ch.cyberduck.core.preferences.BundleApplicationResourcesFinder;
import ch.cyberduck.core.preferences.UserHomeSupportDirectoryFinder;
import ch.cyberduck.core.serializer.impl.HostPlistReader;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.PlistSerializer;
import ch.cyberduck.core.serializer.impl.PlistWriter;
import ch.cyberduck.core.serializer.impl.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.TransferPlistReader;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.urlhandler.DisabledSchemeHandler;
import ch.cyberduck.ui.resources.NSImageIconCache;

/**
 * @version $Id$
 */
public class TerminalPreferences extends MemoryPreferences {

    @Override
    public void setFactories() {
        super.setFactories();

        defaults.put("factory.locale.class", DisabledLocale.class.getName());
        defaults.put("factory.local.class", Local.class.getName());
        defaults.put("factory.proxy.class", DisabledProxyFinder.class.getName());
        defaults.put("factory.certificatestore.class", TerminalCertificateStore.class.getName());
        defaults.put("factory.logincallback.class", TerminalLoginCallback.class.getName());
        defaults.put("factory.hostkeycallback.class", TerminalHostKeyVerifier.class.getName());
        defaults.put("factory.transfererrorcallback.class", TerminalTransferErrorCallback.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            defaults.put(String.format("factory.transferpromptcallback.%s.class", t.name()), TerminalTransferPrompt.class.getName());
        }
        defaults.put("factory.dateformatter.class", DefaultUserDateFormatter.class.getName());
        defaults.put("factory.rendezvous.class", DisabledRendezvous.class.getName());
        defaults.put("factory.trash.class", DefaultLocalTrashFeature.class.getName());
        defaults.put("factory.quarantine.class", DisabledQuarantineService.class.getName());
        defaults.put("factory.symlink.class", NullLocalSymlinkFeature.class.getName());
        defaults.put("factory.licensefactory.class", DonationKeyFactory.class.getName());
        defaults.put("factory.badgelabeler.class", DisabledApplicationBadgeLabeler.class.getName());
        defaults.put("factory.filedescriptor.class", NullFileDescriptor.class.getName());
        defaults.put("factory.iconservice.class", DisabledIconService.class.getName());
        defaults.put("factory.schemehandler.class", DisabledSchemeHandler.class.getName());
        defaults.put("factory.sleeppreventer.class", DisabledSleepPreventer.class.getName());
        defaults.put("factory.notification.class", TerminalNotification.class.getName());
        defaults.put("factory.applicationfinder.class", NullApplicationFinder.class.getName());
        switch(Factory.Platform.getDefault()) {
            case mac:
                defaults.put("factory.supportdirectoryfinder.class", ApplicationSupportDirectoryFinder.class.getName());
                defaults.put("factory.locale.class", BundleLocale.class.getName());
                defaults.put("factory.editorfactory.class", FSEventWatchEditorFactory.class.getName());
                defaults.put("factory.applicationlauncher.class", WorkspaceApplicationLauncher.class.getName());
                defaults.put("factory.applicationfinder.class", LaunchServicesApplicationFinder.class.getName());
                defaults.put("factory.local.class", FinderLocal.class.getName());
                defaults.put("factory.autorelease.class", AutoreleaseActionOperationBatcher.class.getName());
                defaults.put("factory.passwordstore.class", Keychain.class.getName());
                defaults.put("factory.proxy.class", SystemConfigurationProxy.class.getName());
                defaults.put("factory.sleeppreventer.class", IOKitSleepPreventer.class.getName());
                defaults.put("factory.reachability.class", SystemConfigurationReachability.class.getName());
                defaults.put("factory.quarantine.class", LaunchServicesQuarantineService.class.getName());
                defaults.put("factory.iconservice.class", WorkspaceIconService.class.getName());
                defaults.put("factory.iconcache.class", NSImageIconCache.class.getName());
                defaults.put("factory.filedescriptor.class", LaunchServicesFileDescriptor.class.getName());
                defaults.put("factory.workingdirectory.class", FileManagerWorkingDirectoryFinder.class.getName());
                break;
            case windows:
                break;
            case linux:
                defaults.put("factory.supportdirectoryfinder.class", UserHomeSupportDirectoryFinder.class.getName());
                defaults.put("factory.applicationlauncher.class", ExecApplicationLauncher.class.getName());
                defaults.put("factory.editorfactory.class", DefaultEditorFactory.class.getName());
                break;
        }
    }

    @Override
    protected void setLogging() {
        defaults.put("logging.config", "log4j-cli.xml");
        defaults.put("logging", "fatal");

        super.setLogging();
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        System.setProperty("jna.library.path", this.getProperty("java.library.path"));

        defaults.put("application.version", Version.getSpecification());
        defaults.put("application.revision", Version.getImplementation());

        switch(Factory.Platform.getDefault()) {
            case mac:
                defaults.put("connection.ssl.keystore.type", "KeychainStore");
                defaults.put("connection.ssl.keystore.provider", "Cyberduck");
                final Local resources = new BundleApplicationResourcesFinder().find();
                defaults.put("application.bookmarks.path", resources.getAbsolute() + "/Bookmarks");
                defaults.put("application.profiles.path", resources.getAbsolute() + "/Profiles");
                defaults.put("factory.serializer.class", PlistSerializer.class.getName());
                defaults.put("factory.deserializer.class", PlistDeserializer.class.getName());
                defaults.put("factory.reader.profile.class", ProfilePlistReader.class.getName());
                defaults.put("factory.writer.profile.class", PlistWriter.class.getName());
                defaults.put("factory.reader.transfer.class", TransferPlistReader.class.getName());
                defaults.put("factory.writer.transfer.class", PlistWriter.class.getName());
                defaults.put("factory.reader.host.class", HostPlistReader.class.getName());
                defaults.put("factory.writer.host.class", PlistWriter.class.getName());
                break;
            case windows:
                defaults.put("connection.ssl.keystore.type", "Windows-MY");
                defaults.put("connection.ssl.keystore.provider", "SunMSCAPI");
                defaults.put("application.bookmarks.path", "bookmarks"); // relative to .exe
                defaults.put("application.profiles.path", "profiles"); // relative to .exe
                break;
            case linux:
            default:
                final Local settings = new UserHomeSupportDirectoryFinder().find();
                defaults.put("application.bookmarks.path", settings.getAbsolute());
                defaults.put("application.profiles.path", settings.getAbsolute());
        }
        defaults.put("local.normalize.prefix", String.valueOf(true));
        defaults.put("queue.download.folder", WorkingDirectoryFinderFactory.get().find().getAbsolute());
    }

    @Override
    public String getProperty(final String property) {
        final String system = System.getProperty(property);
        if(null == system) {
            return super.getProperty(property);
        }
        return system;
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
}
