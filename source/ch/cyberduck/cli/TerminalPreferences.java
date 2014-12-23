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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.MemoryPreferences;
import ch.cyberduck.core.SystemConfigurationProxy;
import ch.cyberduck.core.SystemConfigurationReachability;
import ch.cyberduck.core.editor.DefaultEditorFactory;
import ch.cyberduck.core.editor.FSEventWatchEditorFactory;
import ch.cyberduck.core.i18n.BundleLocale;
import ch.cyberduck.core.local.ExecApplicationLauncher;
import ch.cyberduck.core.local.FileManagerWorkingDirectoryFinder;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.LaunchServicesQuarantineService;
import ch.cyberduck.core.local.WorkingDirectoryFinderFactory;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.local.WorkspaceIconService;
import ch.cyberduck.core.preferences.ApplicationSupportDirectoryFinder;
import ch.cyberduck.core.preferences.BundleApplicationResourcesFinder;
import ch.cyberduck.core.preferences.UserHomeSupportDirectoryFinder;
import ch.cyberduck.core.resources.NSImageIconCache;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.core.transfer.Transfer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class TerminalPreferences extends MemoryPreferences {
    private static final Logger log = Logger.getLogger(TerminalPreferences.class);

    @Override
    protected void setFactories() {
        super.setFactories();

        defaults.put("factory.certificatestore.class", TerminalCertificateStore.class.getName());
        defaults.put("factory.logincallback.class", TerminalLoginCallback.class.getName());
        defaults.put("factory.hostkeycallback.class", TerminalHostKeyVerifier.class.getName());
        defaults.put("factory.transfererrorcallback.class", TerminalTransferErrorCallback.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            defaults.put(String.format("factory.transferpromptcallback.%s.class", t.name()), TerminalTransferPrompt.class.getName());
        }
        defaults.put("factory.notification.class", TerminalNotification.class.getName());

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
                // Disable
                defaults.put("ssh.authentication.agent.enable", String.valueOf(false));
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
            case mac: {
                defaults.put("connection.ssl.keystore.type", "KeychainStore");
                defaults.put("connection.ssl.keystore.provider", "Cyberduck");

                final Local resources = new BundleApplicationResourcesFinder().find();
                defaults.put("application.bookmarks.path", String.format("%s/Bookmarks", resources.getAbsolute()));
                defaults.put("application.profiles.path", String.format("%s/Profiles", resources.getAbsolute()));
                break;
            }
            case windows: {
                defaults.put("connection.ssl.keystore.type", "Windows-MY");
                defaults.put("connection.ssl.keystore.provider", "SunMSCAPI");

                defaults.put("application.bookmarks.path", "bookmarks"); // relative to .exe
                defaults.put("application.profiles.path", "profiles"); // relative to .exe
                break;
            }
            case linux: {
                final Local resources = LocalFactory.get("/opt/duck/app");
                defaults.put("application.bookmarks.path", String.format("%s/bookmarks", resources.getAbsolute()));
                defaults.put("application.profiles.path", String.format("%s/profiles", resources.getAbsolute()));
                try {
                    final Process echo = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "echo ~"});
                    defaults.put("local.user.home", StringUtils.strip(IOUtils.toString(echo.getInputStream())));
                }
                catch(IOException e) {
                    log.warn("Failure determining user home with `echo ~`");
                }
            }
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
