/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
 */

package ch.cyberduck.cli;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.UnsecureHostPasswordStore;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.editor.DefaultEditorFactory;
import ch.cyberduck.core.i18n.RegexLocale;
import ch.cyberduck.core.local.DefaultSymlinkFeature;
import ch.cyberduck.core.local.DesktopBrowserLauncher;
import ch.cyberduck.core.local.ExecApplicationLauncher;
import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.StaticApplicationResourcesFinder;
import ch.cyberduck.core.preferences.UserHomeSupportDirectoryFinder;
import ch.cyberduck.core.proxy.EnvironmentVariableProxyFinder;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;

public class TerminalPreferences extends MemoryPreferences {
    private static final Logger log = Logger.getLogger(TerminalPreferences.class);

    @Override
    protected void setFactories() {
        super.setFactories();

        this.setDefault("factory.certificatestore.class", TerminalCertificateStore.class.getName());
        this.setDefault("factory.logincallback.class", TerminalLoginCallback.class.getName());
        this.setDefault("factory.passwordcallback.class", TerminalPasswordCallback.class.getName());
        this.setDefault("factory.alertcallback.class", TerminalAlertCallback.class.getName());
        this.setDefault("factory.hostkeycallback.class", TerminalHostKeyVerifier.class.getName());
        this.setDefault("factory.transfererrorcallback.class", TerminalTransferErrorCallback.class.getName());
        this.setDefault("factory.notification.class", TerminalNotification.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            this.setDefault(String.format("factory.transferpromptcallback.%s.class", t.name()), TerminalTransferPrompt.class.getName());
        }
        switch(Factory.Platform.getDefault()) {
            case mac:
                break;
            case windows:
                break;
            case linux:
                if(Desktop.isDesktopSupported()) {
                    this.setDefault("factory.browserlauncher.class", DesktopBrowserLauncher.class.getName());
                }
                else {
                    this.setDefault("factory.browserlauncher.class", TerminalBrowserLauncher.class.getName());
                }
                this.setDefault("factory.supportdirectoryfinder.class", UserHomeSupportDirectoryFinder.class.getName());
                this.setDefault("factory.applicationresourcesfinder.class", StaticApplicationResourcesFinder.class.getName());
                this.setDefault("factory.locale.class", RegexLocale.class.getName());
                this.setDefault("factory.applicationlauncher.class", ExecApplicationLauncher.class.getName());
                this.setDefault("factory.editorfactory.class", DefaultEditorFactory.class.getName());
                this.setDefault("factory.proxy.class", EnvironmentVariableProxyFinder.class.getName());
                this.setDefault("factory.symlink.class", DefaultSymlinkFeature.class.getName());
                this.setDefault("factory.passwordstore.class", UnsecureHostPasswordStore.class.getName());
                break;
        }
        this.setDefault("factory.vault.class", CryptoVault.class.getName());
        this.setDefault("factory.securerandom.class", FastSecureRandomProvider.class.getName());
    }

    @Override
    protected void setLogging() {
        this.setDefault("logging", "fatal");

        super.setLogging();
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setDefault("website.home", "http://duck.sh/");
        this.setDefault("website.help", "http://help.duck.sh/");

        System.setProperty("jna.library.path", this.getProperty("java.library.path"));

        switch(Factory.Platform.getDefault()) {
            case mac: {
                break;
            }
            case windows: {
                break;
            }
            case linux: {
                try {
                    final Process echo = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "echo ~"});
                    this.setDefault("local.user.home", StringUtils.strip(IOUtils.toString(echo.getInputStream(), Charset.defaultCharset())));
                }
                catch(IOException e) {
                    log.warn("Failure determining user home with `echo ~`");
                }
                this.setDefault("ssh.authentication.agent.enable", String.valueOf(false));
                // Lowercase folder names to use when looking for profiles and bookmarks in user support directory
                this.setDefault("bookmarks.folder.name", "bookmarks");
                this.setDefault("profiles.folder.name", "profiles");
                this.setDefault("connection.ssl.securerandom", "NativePRNGNonBlocking");

                break;
            }
        }
        this.setDefault("local.normalize.prefix", String.valueOf(true));
        this.setDefault("connection.login.name", System.getProperty("user.name"));

        // Disable transfer filters
        this.setDefault("queue.download.skip.enable", "false");
        this.setDefault("queue.upload.skip.enable", "false");

        this.setDefault("queue.copy.action", TransferAction.comparison.name());
        this.setDefault("queue.copy.reload.action", TransferAction.comparison.name());
    }

    public TerminalPreferences withDefaults(final CommandLine input) {
        if(input.hasOption(TerminalOptionsBuilder.Params.chmod.name())) {
            final Permission permission = new Permission(input.getOptionValue(TerminalOptionsBuilder.Params.chmod.name()));
            this.setDefault("queue.upload.permissions.change", String.valueOf(true));
            this.setDefault("queue.upload.permissions.default", String.valueOf(true));
            this.setDefault("queue.upload.permissions.file.default", permission.getMode());
        }
        return this;
    }

    @Override
    public String getProperty(final String property) {
        final String env = System.getenv(property);
        if(null == env) {
            final String system = System.getProperty(property);
            if(null == system) {
                return super.getProperty(property);
            }
            return system;
        }
        return env;
    }
}
