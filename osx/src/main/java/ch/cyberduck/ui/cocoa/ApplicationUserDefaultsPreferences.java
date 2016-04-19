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

import ch.cyberduck.core.logging.SystemLogAppender;
import ch.cyberduck.core.sparkle.SparklePeriodicUpdateChecker;
import ch.cyberduck.core.sparkle.Updater;
import ch.cyberduck.core.threading.AlertTransferErrorCallback;
import ch.cyberduck.core.updater.DisabledPeriodicUpdater;
import ch.cyberduck.preferences.ApplicationPreferences;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class ApplicationUserDefaultsPreferences extends ApplicationPreferences {

    @Override
    protected void setDefaults() {
        super.setDefaults();

        if(null == Updater.getFeed()) {
            defaults.put("factory.updater.class", DisabledPeriodicUpdater.class.getName());
        }
        else {
            defaults.put("factory.updater.class", SparklePeriodicUpdateChecker.class.getName());
        }
        defaults.put("factory.dateformatter.class", UserDefaultsDateFormatter.class.getName());
        defaults.put("factory.hostkeycallback.class", AlertHostKeyController.class.getName());
        defaults.put("factory.logincallback.class", PromptLoginController.class.getName());
        defaults.put("factory.transfererrorcallback.class", AlertTransferErrorCallback.class.getName());
        defaults.put("factory.transferpromptcallback.download.class", DownloadPromptController.class.getName());
        defaults.put("factory.transferpromptcallback.upload.class", UploadPromptController.class.getName());
        defaults.put("factory.transferpromptcallback.copy.class", CopyPromptController.class.getName());
        defaults.put("factory.transferpromptcallback.sync.class", SyncPromptController.class.getName());
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
