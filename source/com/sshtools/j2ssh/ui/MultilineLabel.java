/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.ui;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import com.sshtools.j2ssh.util.*;
import com.sshtools.j2ssh.ui.*;

/**
 *  Component to display text. The text string supplied may contain <code>\c</code>
 *  characters to sigal a new line.
 *
 *@author     Brett Smith
 *@created    31 August 2002
 *@version    $Id$
 */
public class MultilineLabel extends JPanel {

    /**
     * Construct a new empty label
     */
    public MultilineLabel() {
        this("");
    }

    /**
     * Construct a new label with some text
     *
     * @param text text
     */
    public MultilineLabel(String text)
    {
        super(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.NONE;
        setText(text);
    }

    /**
     * Set the font
     *
     * @param f font
     */
    public void setFont(Font f) {
        super.setFont(f);
        for(int i = 0; i < getComponentCount() ; i++)
            getComponent(i).setFont(f);
    }

    /**
     * Set the text
     *
     * @param text text
     */
    public void setText(String text) {
        this.text = text;
        removeAll();
        StringTokenizer tok = new StringTokenizer(text,"\n");
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;
        while(tok.hasMoreTokens()) {
            String t = tok.nextToken();
            if(!tok.hasMoreTokens())
                constraints.weighty = 1.0;
            UIUtil.jGridBagAdd(this, new JLabel(t), constraints,
                GridBagConstraints.REMAINDER);
        }
        revalidate();
        repaint();
    }

    /**
     * Return the text
     *
     * @return text
     */
    public String getText() {
        return text;
    }

    //  Private instance variables

    private GridBagConstraints constraints;
    private String text;
}
