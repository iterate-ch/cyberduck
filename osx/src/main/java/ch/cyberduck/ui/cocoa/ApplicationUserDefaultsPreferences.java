package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2015 David Kocher. All rights reserved.
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
 *  feedback@cyberduck.io
 */

import ch.cyberduck.core.bonjour.RendezvousResponder;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.logging.SystemLogAppender;
import ch.cyberduck.core.preferences.ApplicationPreferences;
import ch.cyberduck.core.sparkle.SparklePeriodicUpdateChecker;
import ch.cyberduck.core.sparkle.Updater;
import ch.cyberduck.ui.browser.Column;
import ch.cyberduck.ui.cocoa.controller.CopyPromptController;
import ch.cyberduck.ui.cocoa.controller.DownloadPromptController;
import ch.cyberduck.ui.cocoa.controller.SyncPromptController;
import ch.cyberduck.ui.cocoa.controller.UploadPromptController;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class ApplicationUserDefaultsPreferences extends ApplicationPreferences {

    @Override
    protected void setDefaults() {
        // Parent defaults
        super.setDefaults();

        if(null != Updater.getFeed()) {
            defaults.put("factory.updater.class", SparklePeriodicUpdateChecker.class.getName());
        }
        defaults.put("factory.dateformatter.class", UserDefaultsDateFormatter.class.getName());
        defaults.put("factory.hostkeycallback.class", PromptHostKeyCallback.class.getName());
        defaults.put("factory.logincallback.class", PromptLoginCallback.class.getName());
        defaults.put("factory.passwordcallback.class", PromptPasswordCallback.class.getName());
        defaults.put("factory.alertcallback.class", PromptAlertCallback.class.getName());
        defaults.put("factory.transfererrorcallback.class", PromptTransferErrorCallback.class.getName());
        defaults.put("factory.transferpromptcallback.download.class", DownloadPromptController.class.getName());
        defaults.put("factory.transferpromptcallback.upload.class", UploadPromptController.class.getName());
        defaults.put("factory.transferpromptcallback.copy.class", CopyPromptController.class.getName());
        defaults.put("factory.transferpromptcallback.sync.class", SyncPromptController.class.getName());
        defaults.put("factory.rendezvous.class", RendezvousResponder.class.getName());
        defaults.put("factory.vault.class", CryptoVault.class.getName());
        defaults.put("factory.securerandom.class", FastSecureRandomProvider.class.getName());

        defaults.put(String.format("browser.column.%s", Column.icon.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.icon.name()), String.valueOf(20));
        defaults.put(String.format("browser.column.%s", Column.filename.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.filename.name()), String.valueOf(250));
        defaults.put(String.format("browser.column.%s", Column.kind.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.kind.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.extension.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.extension.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.size.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.size.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.modified.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.modified.name()), String.valueOf(150));
        defaults.put(String.format("browser.column.%s", Column.owner.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.owner.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.group.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.group.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.permission.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.permission.name()), String.valueOf(100));
        defaults.put(String.format("browser.column.%s", Column.region.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.region.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.version.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.version.name()), String.valueOf(80));

        defaults.put("browser.sort.column", Column.filename.name());
    }

    @Override
    protected void post() {
        super.post();
        Logger root = Logger.getRootLogger();
        final SystemLogAppender appender = new SystemLogAppender();
        appender.setLayout(new PatternLayout("[%t] %-5p %c - %m%n"));
        root.addAppender(appender);
    }
}
