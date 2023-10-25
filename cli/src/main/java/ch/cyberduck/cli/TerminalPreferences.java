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

import ch.cyberduck.core.DisabledConnectionTimeout;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TerminalPreferences extends Preferences {
    private static final Logger log = LogManager.getLogger(TerminalPreferences.class);

    private final Preferences proxy;

    public TerminalPreferences(final Preferences persistence) {
        this.proxy = persistence;
    }

    @Override
    protected void setFactories() {
        super.setFactories();

        this.setDefault("factory.local.class", Local.class.getName());
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
        this.setDefault("factory.vault.class", CryptoVault.class.getName());
        this.setDefault("factory.securerandom.class", FastSecureRandomProvider.class.getName());
        this.setDefault("factory.connectiontimeout.class", DisabledConnectionTimeout.class.getName());
    }

    @Override
    protected void configureAppenders(final String level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final Appender appender = new TerminalAppender(PatternLayout.newBuilder().withConfiguration(config).withPattern("[%t] %-5p %c - %m%n").withCharset(StandardCharsets.UTF_8).build());
        appender.start();
        config.addAppender(appender);
        config.getRootLogger().addAppender(appender, null, null);
        ctx.updateLoggers();
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setDefault("logging", "fatal");

        this.setDefault("website.home", "https://duck.sh/");
        this.setDefault("website.help", "https://help.duck.sh/");

        System.setProperty("jna.library.path", this.getProperty("java.library.path"));

        this.setDefault("local.normalize.prefix", String.valueOf(true));
        this.setDefault("connection.login.name", System.getProperty("user.name"));

        // Disable transfer filters
        this.setDefault("queue.download.skip.enable", "false");
        this.setDefault("queue.upload.skip.enable", "false");

        this.setDefault("queue.copy.action", TransferAction.comparison.name());
        this.setDefault("queue.copy.reload.action", TransferAction.comparison.name());

        this.setDefault("keychain.secure", String.valueOf(false));
    }

    public TerminalPreferences withDefaults(final CommandLine input) {
        if(input.hasOption(TerminalOptionsBuilder.Params.chmod.name())) {
            final Permission permission = new Permission(input.getOptionValue(TerminalOptionsBuilder.Params.chmod.name()));
            this.setDefault("queue.upload.permissions.change", String.valueOf(true));
            this.setDefault("queue.upload.permissions.default", String.valueOf(true));
            this.setDefault("queue.upload.permissions.file.default", permission.getMode());
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.debug.name())) {
            this.setLogging("debug");
        }
        return this;
    }

    @Override
    public void setDefault(final String property, final String value) {
        proxy.setDefault(property, value);
    }

    @Override
    public String getProperty(final String property) {
        final String env = System.getenv(property);
        if(null == env) {
            final String system = System.getProperty(property);
            if(null == system) {
                return proxy.getProperty(property);
            }
            return system;
        }
        return env;
    }

    @Override
    public void setProperty(final String property, final String v) {
        proxy.setProperty(property, v);
    }

    @Override
    public void deleteProperty(final String property) {
        proxy.deleteProperty(property);
    }

    @Override
    public String getDefault(final String property) {
        return proxy.getDefault(property);
    }

    @Override
    public void save() {
        proxy.save();
    }

    @Override
    public void load() {
        proxy.load();
    }

    @Override
    public List<String> applicationLocales() {
        return proxy.applicationLocales();
    }

    @Override
    public List<String> systemLocales() {
        return proxy.systemLocales();
    }
}
