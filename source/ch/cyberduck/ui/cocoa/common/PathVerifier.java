package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.common.PathVerifier.java
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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import java.io.File;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

public class PathVerifier extends InputVerifier {

    private JLabel error;
    private String property;

    /**
     * Verifies the component (<code>instanceof javax.swing.text.JTextComponent</code>)
     * to accept only paths of the local filesystem.
     * @param error The error label to display if verify() returns false.
     * @param property The property to set.
     */
    public PathVerifier(JLabel error, String property) {
        super();
        this.error = error;
        this.property = property;
    }

    /**
     * Verifies the component (<code>instanceof javax.swing.text.JTextComponent</code>)
     * to accept only paths of the local filesystem.
     * @param error The error label to display if verify() returns false.
     */
    public PathVerifier(JLabel error) {
        super();
        this.error = error;
    }

    public boolean verify(JComponent input) {
        Cyberduck.DEBUG("[PathVerifier] verifiy()");
        JTextComponent tf = (JTextComponent) input;
        File path = new File(tf.getText());
        if(path != null) {
            if(path.exists()) {
                Cyberduck.DEBUG("[PathVerifier] return true");
                error.setVisible(false);
                if(property != null) {
                    if(tf.getText().charAt(tf.getText().length() - 1) == '/')
                        Preferences.instance().setProperty(property, tf.getText());
                    else
                        Preferences.instance().setProperty(property, tf.getText() + "/");
                }
                return true;
            }
            else {
                Cyberduck.DEBUG("[PathVerifier] return false");
                error.setVisible(true);
                Cyberduck.beep();
                return false;
            }
        }
        return true;
    }
}
