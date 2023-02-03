package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.UnsecureHostPasswordStore;
import ch.cyberduck.core.editor.DefaultEditorFactory;
import ch.cyberduck.core.i18n.RegexLocale;
import ch.cyberduck.core.local.DefaultSymlinkFeature;
import ch.cyberduck.core.local.DesktopBrowserLauncher;
import ch.cyberduck.core.local.ExecApplicationLauncher;
import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.UserHomeSupportDirectoryFinder;
import ch.cyberduck.core.proxy.EnvironmentVariableProxyFinder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;

public class LinuxTerminalPreferences extends TerminalPreferences {
    private static final Logger log = LogManager.getLogger(LinuxTerminalPreferences.class);

    public LinuxTerminalPreferences() {
        super(new MemoryPreferences());
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

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
        this.setDefault("connection.ssl.securerandom.algorithm", "NativePRNGNonBlocking");
    }

    @Override
    protected void setFactories() {
        super.setFactories();

        if(Desktop.isDesktopSupported()) {
            this.setDefault("factory.browserlauncher.class", DesktopBrowserLauncher.class.getName());
        }
        else {
            this.setDefault("factory.browserlauncher.class", TerminalBrowserLauncher.class.getName());
        }
        this.setDefault("factory.supportdirectoryfinder.class", UserHomeSupportDirectoryFinder.class.getName());
        this.setDefault("factory.localsupportdirectoryfinder.class", UserHomeSupportDirectoryFinder.class.getName());
        this.setDefault("factory.applicationresourcesfinder.class", ClasspathResourcesFinder.class.getName());
        this.setDefault("factory.locale.class", RegexLocale.class.getName());
        this.setDefault("factory.applicationlauncher.class", ExecApplicationLauncher.class.getName());
        this.setDefault("factory.editorfactory.class", DefaultEditorFactory.class.getName());
        this.setDefault("factory.proxy.class", EnvironmentVariableProxyFinder.class.getName());
        this.setDefault("factory.symlink.class", DefaultSymlinkFeature.class.getName());
        this.setDefault("factory.passwordstore.class", UnsecureHostPasswordStore.class.getName());
    }
}
