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
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.preferences.ApplicationPreferences;
import ch.cyberduck.core.sparkle.SparklePeriodicUpdateChecker;
import ch.cyberduck.core.threading.DispatchThreadPool;
import ch.cyberduck.ui.browser.BrowserColumn;
import ch.cyberduck.ui.cocoa.callback.PromptAlertCallback;
import ch.cyberduck.ui.cocoa.callback.PromptCertificateIdentityCallback;
import ch.cyberduck.ui.cocoa.callback.PromptCertificateTrustCallback;
import ch.cyberduck.ui.cocoa.callback.PromptHostKeyCallback;
import ch.cyberduck.ui.cocoa.callback.PromptLoginCallback;
import ch.cyberduck.ui.cocoa.callback.PromptPasswordCallback;
import ch.cyberduck.ui.cocoa.callback.PromptTransferErrorCallback;
import ch.cyberduck.ui.cocoa.controller.CopyPromptController;
import ch.cyberduck.ui.cocoa.controller.DownloadPromptController;
import ch.cyberduck.ui.cocoa.controller.SyncPromptController;
import ch.cyberduck.ui.cocoa.controller.UploadPromptController;

public class ApplicationUserDefaultsPreferences extends ApplicationPreferences {

    @Override
    protected void setDefaults() {
        // Parent defaults
        super.setDefaults();

        this.setDefault(String.format("browser.column.%s", BrowserColumn.icon.name()), String.valueOf(true));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.icon.name()), String.valueOf(20));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.filename.name()), String.valueOf(true));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.filename.name()), String.valueOf(250));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.kind.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.kind.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.extension.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.extension.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.size.name()), String.valueOf(true));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.size.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.modified.name()), String.valueOf(true));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.modified.name()), String.valueOf(150));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.owner.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.owner.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.group.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.group.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.permission.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.permission.name()), String.valueOf(100));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.region.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.region.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.version.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.version.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.storageclass.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.storageclass.name()), String.valueOf(80));
        this.setDefault(String.format("browser.column.%s", BrowserColumn.checksum.name()), String.valueOf(false));
        this.setDefault(String.format("browser.column.%s.width", BrowserColumn.checksum.name()), String.valueOf(80));

        this.setDefault("browser.sort.column", BrowserColumn.filename.name());
        this.setDefault("website.store", "macappstore://itunes.apple.com/app/id409222199?mt=12");

        if(new FinderLocal("~/Downloads").exists()) {
            // For 10.5+ this usually exists and should be preferrred
            this.setDefault("queue.download.folder", "~/Downloads");
        }
        else {
            this.setDefault("queue.download.folder", "~/Desktop");
        }
        this.setDefault("editor.bundleIdentifier", "com.apple.TextEdit");
    }

    @Override
    protected void setFactories() {
        super.setFactories();

        this.setDefault("factory.threadpool.class", DispatchThreadPool.class.getName());
        this.setDefault("factory.updater.class", SparklePeriodicUpdateChecker.class.getName());
        this.setDefault("factory.dateformatter.class", UserDefaultsDateFormatter.class.getName());
        this.setDefault("factory.hostkeycallback.class", PromptHostKeyCallback.class.getName());
        this.setDefault("factory.logincallback.class", PromptLoginCallback.class.getName());
        this.setDefault("factory.passwordcallback.class", PromptPasswordCallback.class.getName());
        this.setDefault("factory.certificatetrustcallback.class", PromptCertificateTrustCallback.class.getName());
        this.setDefault("factory.certificateidentitycallback.class", PromptCertificateIdentityCallback.class.getName());
        this.setDefault("factory.alertcallback.class", PromptAlertCallback.class.getName());
        this.setDefault("factory.transfererrorcallback.class", PromptTransferErrorCallback.class.getName());
        this.setDefault("factory.transferpromptcallback.download.class", DownloadPromptController.class.getName());
        this.setDefault("factory.transferpromptcallback.upload.class", UploadPromptController.class.getName());
        this.setDefault("factory.transferpromptcallback.copy.class", CopyPromptController.class.getName());
        this.setDefault("factory.transferpromptcallback.sync.class", SyncPromptController.class.getName());
        this.setDefault("factory.rendezvous.class", RendezvousResponder.class.getName());
        this.setDefault("factory.vault.class", CryptoVault.class.getName());
        this.setDefault("factory.securerandom.class", FastSecureRandomProvider.class.getName());
    }
}
