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
import ch.cyberduck.ui.browser.BrowserColumn;
import ch.cyberduck.ui.cocoa.callback.PromptAlertCallback;
import ch.cyberduck.ui.cocoa.callback.PromptHostKeyCallback;
import ch.cyberduck.ui.cocoa.callback.PromptLoginCallback;
import ch.cyberduck.ui.cocoa.callback.PromptPasswordCallback;
import ch.cyberduck.ui.cocoa.callback.PromptTransferErrorCallback;
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

        defaults.put(String.format("browser.column.%s", BrowserColumn.icon.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.icon.name()), String.valueOf(20));
        defaults.put(String.format("browser.column.%s", BrowserColumn.filename.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.filename.name()), String.valueOf(250));
        defaults.put(String.format("browser.column.%s", BrowserColumn.kind.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.kind.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", BrowserColumn.extension.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.extension.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", BrowserColumn.size.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.size.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", BrowserColumn.modified.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.modified.name()), String.valueOf(150));
        defaults.put(String.format("browser.column.%s", BrowserColumn.owner.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.owner.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", BrowserColumn.group.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.group.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", BrowserColumn.permission.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.permission.name()), String.valueOf(100));
        defaults.put(String.format("browser.column.%s", BrowserColumn.region.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.region.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", BrowserColumn.version.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", BrowserColumn.version.name()), String.valueOf(80));

        defaults.put("browser.sort.column", BrowserColumn.filename.name());
        defaults.put("website.store", "macappstore://itunes.apple.com/app/id409222199?mt=12");
    }

    @Override
    protected void setFactories() {
        super.setFactories();

        defaults.put("factory.updater.class", SparklePeriodicUpdateChecker.class.getName());
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
    }

    @Override
    protected void post() {
        // Send log output to system.log
        Logger root = Logger.getRootLogger();
        final SystemLogAppender appender = new SystemLogAppender();
        appender.setLayout(new PatternLayout("[%t] %-5p %c - %m%n"));
        root.addAppender(appender);
        // Post configuration
        super.post();
    }
}
