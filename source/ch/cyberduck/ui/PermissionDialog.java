package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.PermissionPanel.java
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

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Path;
import ch.cyberduck.connection.Permission;
import ch.cyberduck.connection.TransferAction;
import ch.cyberduck.ui.common.GUIFactory;
import ch.cyberduck.ui.layout.ParagraphLayout;

/**
* A dialog that lets change the unix file permissions of a remote file over ftp
 * @see ch.cyberduck.connection.Permission
 * @version $Id$
 */
public class PermissionDialog extends JDialog implements ItemListener {

    private JCheckBox[] ownerButton = new JCheckBox[3];
    private JCheckBox[] groupButton = new JCheckBox[3];
    private JCheckBox[] otherButton = new JCheckBox[3];

    private JPanel accessPanel;
    
    private Bookmark bookmark;
    private Path file;

    /**
     * @param b The bookmark being edited
     * @param p The path to edit the permissions
     */
    public PermissionDialog(Bookmark b, Path p) {
        Cyberduck.DEBUG("[PermissionDialog]");
        this.bookmark = b;
        this.file = p;
        this.init();
    }

    private void init() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        this.setTitle(file.getName());
        this.setResizable(false);

        this.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    dispose();
                else if (e.isMetaDown() && (e.getKeyCode() == KeyEvent.VK_W))
                    dispose();
                else if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_W)) {
                    dispose();
                }
            }
        }
                       );
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        }
                          );
        
        JPanel infoPanel = GUIFactory.panelBuilder(new ParagraphLayout());
        accessPanel = GUIFactory.panelBuilder(new ParagraphLayout());
        JPanel buttonPanel = GUIFactory.panelBuilder(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel icon = new JLabel();
        if(file.isDirectory()) {
            icon.setIcon(ch.cyberduck.ui.common.GUIFactory.FOLDER_ICON);
        }
        if(file.isFile()) {
            icon.setIcon(ch.cyberduck.ui.common.GUIFactory.FILE_ICON);
        }
       	infoPanel.add(icon, ParagraphLayout.NEW_PARAGRAPH);
       	infoPanel.add(GUIFactory.labelBuilder(file.getPath(), GUIFactory.FONT_SMALL));
       	infoPanel.add(GUIFactory.labelBuilder("Size", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	infoPanel.add(GUIFactory.labelBuilder(file.getSize(), GUIFactory.FONT_SMALL));
       	infoPanel.add(GUIFactory.labelBuilder("Kind", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	infoPanel.add(GUIFactory.labelBuilder(file.isFile() ? "File" : "Folder", GUIFactory.FONT_SMALL));
       	infoPanel.add(GUIFactory.labelBuilder("Owner", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	infoPanel.add(GUIFactory.labelBuilder(file.getOwner(), GUIFactory.FONT_SMALL));
       	infoPanel.add(GUIFactory.labelBuilder("Group", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	infoPanel.add(GUIFactory.labelBuilder(file.getGroup(), GUIFactory.FONT_SMALL));
       	infoPanel.add(GUIFactory.labelBuilder("Modified", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	infoPanel.add(GUIFactory.labelBuilder(file.getModified(), GUIFactory.FONT_SMALL));
       	
       	Permission permission = file.getPermission();
       	boolean[] ownerPerm = permission.getOwnerPermissions();
       	boolean[] groupPerm = permission.getGroupPermissions();
       	boolean[] otherPerm = permission.getOtherPermissions();
       	
       	accessPanel.add(GUIFactory.labelBuilder("Owner", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	accessPanel.add(ownerButton[Permission.READ] = GUIFactory.checkboxBuilder("Read", GUIFactory.FONT_SMALL, ownerPerm[Permission.READ], this));
       	accessPanel.add(ownerButton[Permission.WRITE] = GUIFactory.checkboxBuilder("Write", GUIFactory.FONT_SMALL, ownerPerm[Permission.WRITE], this));
       	accessPanel.add(ownerButton[Permission.EXECUTE] = GUIFactory.checkboxBuilder("Execute", GUIFactory.FONT_SMALL, ownerPerm[Permission.EXECUTE], this));

       	accessPanel.add(GUIFactory.labelBuilder("Group", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	accessPanel.add(groupButton[Permission.READ] = GUIFactory.checkboxBuilder("Read", GUIFactory.FONT_SMALL, groupPerm[Permission.READ], this));
       	accessPanel.add(groupButton[Permission.WRITE] = GUIFactory.checkboxBuilder("Write", GUIFactory.FONT_SMALL, groupPerm[Permission.WRITE], this));
       	accessPanel.add(groupButton[Permission.EXECUTE] = GUIFactory.checkboxBuilder("Execute", GUIFactory.FONT_SMALL, groupPerm[Permission.EXECUTE], this));

       	accessPanel.add(GUIFactory.labelBuilder("Others", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
       	accessPanel.add(otherButton[Permission.READ] = GUIFactory.checkboxBuilder("Read", GUIFactory.FONT_SMALL, otherPerm[Permission.READ], this));
       	accessPanel.add(otherButton[Permission.WRITE] = GUIFactory.checkboxBuilder("Write", GUIFactory.FONT_SMALL, otherPerm[Permission.WRITE], this));
       	accessPanel.add(otherButton[Permission.EXECUTE] = GUIFactory.checkboxBuilder("Execute", GUIFactory.FONT_SMALL, otherPerm[Permission.EXECUTE], this));

        accessPanel.setBorder(BorderFactory.createTitledBorder("Permissions | "+file.getPermission().toString()));
        buttonPanel.add(GUIFactory.buttonBuilder("Cancel", GUIFactory.FONT_SMALL,
                                                 new AbstractAction() {
                                                     public void actionPerformed(ActionEvent e) {
                                                         dispose();
                                                     }
                                                 }));
        buttonPanel.add(GUIFactory.buttonBuilder("Save", GUIFactory.FONT_SMALL, new SaveAction()));

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(accessPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel);
        this.pack();
    }

    public void itemStateChanged(ItemEvent e) {
        Cyberduck.DEBUG(e.paramString());
        boolean[][] p = new boolean[3][3];
        for(int k = Permission.READ; k <= Permission.EXECUTE; k++) {
            p[Permission.OWNER][k] = ownerButton[k].isSelected();
            p[Permission.GROUP][k] = groupButton[k].isSelected();
            p[Permission.OTHER][k] = otherButton[k].isSelected();
        }
        accessPanel.setBorder(BorderFactory.createTitledBorder("Permissions | "+new Permission(p).toString()));
    }

    // ************************************************************************************************************

    private class SaveAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            Cyberduck.DEBUG(e.paramString());
            boolean[][] p = new boolean[3][3];
            for(int k = Permission.READ; k <= Permission.EXECUTE; k++) {
                p[Permission.OWNER][k] = ownerButton[k].isSelected();
                p[Permission.GROUP][k] = groupButton[k].isSelected();
                p[Permission.OTHER][k] = otherButton[k].isSelected();
            }
            bookmark.transfer(new TransferAction(TransferAction.SITE, "CHMOD "+new Permission(p).getCode()+" "+file.getPath()));
            dispose();
        }
    }
}
