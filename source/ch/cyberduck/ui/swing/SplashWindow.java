package ch.cyberduck.ui.swing;

/*
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

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ch.cyberduck.ui.swing.common.GUIFactory;

/**
 * Startup screen and about window
 * @version $Id$
 */
public class SplashWindow extends JWindow {

    /**
     * Build a splash screen with progress bar at the bottom
     * @param progressModel The model to build the progress bar with
     */
    public SplashWindow(BoundedRangeModel progressModel) {
        JLabel l = new JLabel(GUIFactory.getIcon(GUIFactory.getResource(this.getClass(), Cyberduck.SPLASH)));
        this.getContentPane().add(l, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar(progressModel);
        JPanel panel = new JPanel();
        panel.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout());
        panel.add( BorderLayout.CENTER, progressBar );
        this.getContentPane().add(panel,  BorderLayout.SOUTH);
        
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = this.getPreferredSize();
        this.setLocation(screenSize.width/2 - (labelSize.width/2),
                         screenSize.height/2 - (labelSize.height/2));
    }

    public synchronized void enableDismissEvents() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                setVisible(false);
                dispose();
            }
        }
                            );
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setVisible(false);
                dispose();
            }
        }
                              );
    }

    /**
     * Build a splash window without a progress bar (about window)
     */
    public SplashWindow() {
        super();
        JLabel l = new JLabel(GUIFactory.getIcon(GUIFactory.getResource(this.getClass(), Cyberduck.SPLASH)));
        this.getContentPane().add(l, BorderLayout.CENTER);
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        this.setLocation(screenSize.width/2 - (labelSize.width/2),
        screenSize.height/2 - (labelSize.height/2));
        this.addMouseListener(
            new MouseAdapter() {
                public void mousePressed(MouseEvent e)
                {
                    dispose();
                }
            }
        );
    }
}
