package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.common.URLVerifier.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.text.JTextComponent;
import java.net.MalformedURLException;
import java.net.URL;

import ch.cyberduck.Cyberduck;

public class URLVerifier extends javax.swing.InputVerifier {

    private JLabel errorLabel;
    private JTextComponent nameField;
    private JPasswordField passwdField;
    
    public URLVerifier(JLabel errorLabel, JTextComponent nameField, JPasswordField passwdField) {
        super();
        this.errorLabel = errorLabel;
        this.nameField = nameField;
        this.passwdField = passwdField;
    }

    public boolean verify(JComponent input) {
        Cyberduck.DEBUG("[URLVerifier] verifiy()");
        JTextComponent tf = (JTextComponent)input;
        String text = tf.getText();
        URL url;
        try {
            url = new URL(text);
            nameField.setVisible((url.getProtocol()).equals("ftp"));
            passwdField.setVisible((url.getProtocol()).equals("ftp"));
            errorLabel.setVisible(false);
            Cyberduck.DEBUG("[URLVerifier] return true");
            return true;
        }
        catch(MalformedURLException e) {
            errorLabel.setVisible(true);
            Cyberduck.beep();
            Cyberduck.DEBUG("[URLVerifier] return false");
            return false;
        }
    }
}
