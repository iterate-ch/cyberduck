package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.common.DummyVerifier.java
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
import javax.swing.JTextField;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

public class DummyVerifier extends InputVerifier {
    
    private String property = null;

    /**
     * Constructs a new Verifier with the associated Preferences property
     * which will be set with the value of the component (ie text of textfield) after
     * successful verification.
     */
    public DummyVerifier(String p) {
        super();
        this.property = p;
    }

    /**
     * Constructs a verifier only checking that the textfield
     * isn't empty. No property is associated.
     */
    public DummyVerifier() {
        super();
    }

    /**
     * @param input the textfield to verifiy
     */
    public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        if(tf.getText().equals("")) {
            tf.requestFocus();
            Cyberduck.beep();
            return false;
        }
        else {
            if(property != null) {
                Preferences.instance().setProperty(property, tf.getText());
            }
            return true;
        }
     }
}
