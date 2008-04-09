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
import com.apple.cocoa.foundation.NSNotificationCenter;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSNotification;

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

            public void awakeFromNib() {
                this.update();
            }

            private NSTextField titleField; // IBOutlet

            public void setTitleField(NSTextField titleField) {
                this.titleField = titleField;
                this.updateField(this.titleField, reason);
            }

            private NSTextField userField; // IBOutlet

            public void setUserField(NSTextField userField) {
                this.userField = userField;
                this.updateField(this.userField, credentials.getUsername());
                if(protocol.equals(Protocol.S3)) {
                    ((NSTextFieldCell) this.userField.cell()).setPlaceholderString(
                            NSBundle.localizedString("Access Key ID", "S3")
                    );
                }
                NSNotificationCenter.defaultCenter().addObserver(this,
                        new NSSelector("userFieldTextDidChange", new Class[]{NSNotification.class}),
                        NSControl.ControlTextDidChangeNotification,
                        this.userField);
            }

            public void userFieldTextDidChange(NSNotification notification) {
                credentials.setUsername(userField.stringValue());
                this.update();
            }

            private NSTextField textField; // IBOutlet

            public void setTextField(NSTextField textField) {
                this.textField = textField;
                this.updateField(this.textField, message);
            }

            private NSSecureTextField passField; // IBOutlet

            public void setPassField(NSSecureTextField passField) {
                this.passField = passField;
                this.updateField(this.passField, credentials.getPassword());
                if(protocol.equals(Protocol.S3)) {
                    ((NSTextFieldCell) this.passField.cell()).setPlaceholderString(
                            NSBundle.localizedString("Secret Access Key", "S3")
                    );
                }
                NSNotificationCenter.defaultCenter().addObserver(this,
                        new NSSelector("passFieldTextDidChange", new Class[]{NSNotification.class}),
                        NSControl.ControlTextDidChangeNotification,
                        this.passField);
            }

            public void passFieldTextDidChange(NSNotification notification) {
                credentials.setPassword(passField.stringValue());
            }

            private NSButton keychainCheckbox;

            public void setKeychainCheckbox(NSButton keychainCheckbox) {
                this.keychainCheckbox = keychainCheckbox;
                this.keychainCheckbox.setState(Preferences.instance().getBoolean("connection.login.useKeychain")
                        && Preferences.instance().getBoolean("connection.login.addKeychain") ? NSCell.OnState : NSCell.OffState);
                this.keychainCheckbox.setTarget(this);
                this.keychainCheckbox.setAction(new NSSelector("keychainCheckboxClicked", new Class[]{NSButton.class}));
            }

            public void keychainCheckboxClicked(final NSButton sender) {
                credentials.setUseKeychain(sender.state() == NSCell.OnState);
            }

            private NSButton anonymousCheckbox;

            public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
                this.anonymousCheckbox = anonymousCheckbox;
                this.anonymousCheckbox.setTarget(this);
                this.anonymousCheckbox.setAction(new NSSelector("anonymousCheckboxClicked", new Class[]{NSButton.class}));
            }

            public void anonymousCheckboxClicked(final NSButton sender) {
                if(sender.state() == NSCell.OnState) {
                    credentials.setUsername(Preferences.instance().getProperty("connection.login.anon.name"));
                    credentials.setPassword(Preferences.instance().getProperty("connection.login.anon.pass"));
                }
                if(sender.state() == NSCell.OffState) {
                    credentials.setUsername(Preferences.instance().getProperty("connection.login.name"));
                    credentials.setPassword(null);
                }
                this.updateField(this.userField, credentials.getUsername());
                this.updateField(this.passField, credentials.getPassword());
                this.update();
            }

            private void update() {
                this.userField.setEnabled(!credentials.isAnonymousLogin()
                        && !credentials.usesPublicKeyAuthentication()
                );
                this.passField.setEnabled(!credentials.isAnonymousLogin());
                this.keychainCheckbox.setEnabled(!credentials.isAnonymousLogin());
                this.anonymousCheckbox.setState(credentials.isAnonymousLogin() ? NSCell.OnState : NSCell.OffState);
            }

            protected boolean validateInput() {
                return StringUtils.hasLength(credentials.getUsername())
                        && StringUtils.hasLength(credentials.getPassword());
            }

            public void callback(final int returncode) {
                if (returncode == CDSheetCallback.DEFAULT_OPTION) {
                    this.window().endEditingForObject(null);
                    credentials.setUsername((String) userField.objectValue());
                    credentials.setPassword((String) passField.objectValue());
                }
            }
        };
        c.beginSheet();

        if (c.returnCode() == CDSheetCallback.CANCEL_OPTION) {
            throw new LoginCanceledException();
        }
    }

    protected String getBundleName() {
        return null;
    }
}
