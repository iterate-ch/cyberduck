package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.common.IntegerVerifier.java
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
import javax.swing.text.JTextComponent;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

/**
 * A verifier to check for integers in JTextField components
 */
public class IntegerVerifier extends InputVerifier {
    
    private String property = null;

    /**
     * @param p The property to set its value with the text entered into the text component.
     */
    public IntegerVerifier(String p) {
        super();
        this.property = p;
    }

    public IntegerVerifier() {
        super();
    }

    /**
     * Verifies if the text is an integer and then sets the property if not null
     */
    public boolean verify(JComponent input) {
        JTextComponent tf = (JTextComponent) input;
        try {
            int value = Integer.parseInt(tf.getText());
            if(value < 1) {
	            Cyberduck.beep();
                return false;
            }
            if(property != null)
                Preferences.instance().setProperty(property, tf.getText());
            return true;
        }
        catch(NumberFormatException e) {
            Cyberduck.beep();
            return false;
        }
    }
}
