package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.Login;
import ch.cyberduck.ui.LoginController;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSNotificationCenter;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDLoginController implements LoginController
{
    private static Logger log = Logger.getLogger(CDLoginController.class);

    CDWindowController parent;

    public CDLoginController(CDWindowController parent) {
        this.parent = parent;
    }

    public void promptUser(final Login login, final String message) {
        CDSheetController c = new CDSheetController(parent) {
            private NSTextField userField; // IBOutlet

            public void setUserField(NSTextField userField) {
                this.userField = userField;
                this.userField.setStringValue(login.getUsername());
            }

            private NSTextField textField; // IBOutlet

            public void setTextField(NSTextField textField) {
                this.textField = textField;
                this.textField.setStringValue(message);
            }

            private NSSecureTextField passField; // IBOutlet

            public void setPassField(NSSecureTextField passField) {
                this.passField = passField;
                this.passField.setStringValue("");
            }

            private NSButton keychainCheckbox;

            public void setKeychainCheckbox(NSButton keychainCheckbox) {
                this.keychainCheckbox = keychainCheckbox;
                this.keychainCheckbox.setState(NSCell.OffState);
            }

            public void callback(int returncode) {
                if (returncode == DEFAULT_OPTION) {
                    log.info("Update login credentials...");
                    login.setTryAgain(true);
                    login.setUsername((String) userField.objectValue());
                    login.setPassword((String) passField.objectValue());
                    login.setUseKeychain(keychainCheckbox.state() == NSCell.OnState);
                }
                if (returncode == OTHER_OPTION) {
                    log.info("Cancel login...");
                    login.setTryAgain(false);
                }
            }
        };
        if (!NSApplication.loadNibNamed("Login", c)) {
            log.fatal("Couldn't load Login.nib");
        }
        c.beginSheet(true);
    }
}
