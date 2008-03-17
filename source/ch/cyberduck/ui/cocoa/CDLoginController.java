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

import ch.cyberduck.core.*;
import ch.cyberduck.core.LoginController;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDLoginController extends AbstractLoginController implements LoginController {
    private static Logger log = Logger.getLogger(CDLoginController.class);

    CDWindowController parent;

    public CDLoginController(final CDWindowController parent) {
        this.parent = parent;
    }

    public void prompt(final Protocol protocol, final Credentials credentials, final String reason, final String message)
            throws LoginCanceledException {

        CDSheetController c = new CDSheetController(parent) {
            protected String getBundleName() {
                return "Login";
            }

            private NSTextField titleField; // IBOutlet

            public void setTitleField(NSTextField titleField) {
                this.titleField = titleField;
                this.titleField.setStringValue(reason);
            }

            private NSTextField userField; // IBOutlet

            public void setUserField(NSTextField userField) {
                this.userField = userField;
                this.userField.setEnabled(!credentials.usesPublicKeyAuthentication());
                if(StringUtils.hasText(credentials.getUsername())) {
                    this.userField.setStringValue(credentials.getUsername());
                }
                if(protocol.equals(Protocol.S3)) {
                    ((NSTextFieldCell) this.userField.cell()).setPlaceholderString(
                            NSBundle.localizedString("Access Key ID", "S3")
                    );
                }
            }

            private NSTextField textField; // IBOutlet

            public void setTextField(NSTextField textField) {
                this.textField = textField;
                this.textField.setStringValue(message);
            }

            private NSSecureTextField passField; // IBOutlet

            public void setPassField(NSSecureTextField passField) {
                this.passField = passField;
                if(StringUtils.hasText(credentials.getPassword())) {
                    this.passField.setStringValue(credentials.getPassword());
                }
                if(protocol.equals(Protocol.S3)) {
                    ((NSTextFieldCell) this.passField.cell()).setPlaceholderString(
                            NSBundle.localizedString("Secret Access Key", "S3")
                    );
                }
            }

            private NSButton keychainCheckbox;

            public void setKeychainCheckbox(NSButton keychainCheckbox) {
                this.keychainCheckbox = keychainCheckbox;
                if(Preferences.instance().getBoolean("connection.login.useKeychain")
                        && Preferences.instance().getBoolean("connection.login.addKeychain")) {
                    this.keychainCheckbox.setState(NSCell.OnState);
                }
                else {
                    this.keychainCheckbox.setState(NSCell.OffState);
                }
            }

            public void callback(final int returncode) {
                if (returncode == DEFAULT_OPTION) {
                    log.info("Update login credentials...");
                    this.window().endEditingForObject(null);
                    credentials.setUsername((String) userField.objectValue());
                    credentials.setPassword((String) passField.objectValue());
                    credentials.setUseKeychain(keychainCheckbox.state() == NSCell.OnState);
                }
                if (returncode == CANCEL_OPTION) {
                    log.info("Cancel login...");
                    credentials.setUsername(null);
                    credentials.setPassword(null);
                }
            }
        };
        c.beginSheet();

        if(null == credentials.getUsername() && null == credentials.getPassword()) {
            throw new LoginCanceledException();
        }
    }

    protected String getBundleName() {
        return null;
    }
}
