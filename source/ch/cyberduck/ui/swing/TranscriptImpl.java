package ch.cyberduck.ui.swing;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
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

import javax.swing.JTextArea;
import ch.cyberduck.core.Transcripter;
import ch.cyberduck.ui.swing.common.GUIFactory;

/**
* Singleton text area to append logging messages.
 * @version $Id$
 */
public class TranscriptImpl implements Transcripter { //TranscriptImplSwing

    private JTextArea view;
    
    public TranscriptImpl() {
	super();
	this.view = new JTextArea();
	view.setEditable(true);
	view.setFont(GUIFactory.FONT_MONOSPACED_SMALL);
    }

    public void transcript(String text) {
        view.append(text);
        view.setSelectionStart(view.getText().length());
    }
    
    public Object getView() {
	return this.view;
    }
}