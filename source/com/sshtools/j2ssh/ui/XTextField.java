/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
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
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;


/**
 * XTextField is a an extension of JTextField that provides cut, copy,
 * paste, delete and select all actions on any typed text. It also selects
 * all text in the field upon focus, windows stylee.
 *
 * @author Brett Smith
 */
public class XTextField extends JTextField implements ClipboardOwner {
    private JPopupMenu popup;
    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;
    private Action deleteAction;
    private Action selectAllAction;

    /**
     * Constructs a new TextField.  A default model is created, the initial
     * string is null, and the number of columns is set to 0.
     */
    public XTextField() {
        this(null, null, 0);
    }

    /**
     * Constructs a new TextField initialized with the specified text.
     * A default model is created and the number of columns is 0.
     *
     * @param text the text to be displayed, or null
     */
    public XTextField(String text) {
        this(null, text, 0);
    }

    /**
     * Constructs a new empty TextField with the specified number of columns.
     * A default model is created and the initial string is set to null.
     *
     * @param columns  the number of columns to use to calculate
     *   the preferred width.  If columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation.
     */
    public XTextField(int columns) {
        this(null, null, columns);
    }

    /**
     * Constructs a new TextField initialized with the specified text
     * and columns.  A default model is created.
     *
     * @param text the text to be displayed, or null
     * @param columns  the number of columns to use to calculate
     *   the preferred width.  If columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation.
     */
    public XTextField(String text, int columns) {
        this(null, text, columns);
    }

    /**
     * Constructs a new JTextField that uses the given text storage
     * model and the given number of columns.  This is the constructor
     * through which the other constructors feed.  If the document is null,
     * a default model is created.
     *
     * @param doc  the text storage to use.  If this is null, a default
     *   will be provided by calling the createDefaultModel method.
     * @param text  the initial string to display, or null
     * @param columns  the number of columns to use to calculate
     *   the preferred width >= 0.  If columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation.
     * @exception IllegalArgumentException if columns < 0
     */
    public XTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        initXtensions();
    }

    /**
     * Invoked when the clipboard is taken over by something else. We don't
     * really care about this - but it may come in useful later
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    private void showPopup(int x, int y) {
        //  Grab the focus, this should deselect any other selected fields.
        requestFocus();

        //  If the popup has never been show before - then build it
        if(popup == null) {
            popup = new JPopupMenu("Clipboard");
            popup.add(cutAction = new CutAction());
            popup.add(copyAction = new CopyAction());
            popup.add(pasteAction = new PasteAction());
            popup.add(deleteAction = new DeleteAction());
            popup.addSeparator();
            popup.add(selectAllAction = new SelectAllAction());
        }

        //  Enabled the actions based on the field contents
        cutAction.setEnabled(isEnabled() && (getSelectedText() != null));
        copyAction.setEnabled(isEnabled() && (getSelectedText() != null));
        deleteAction.setEnabled(isEnabled() && (getSelectedText() != null));
        pasteAction.setEnabled(isEnabled() &&
            Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this)
                   .isDataFlavorSupported(DataFlavor.stringFlavor));
        selectAllAction.setEnabled(isEnabled());

        //  Make the popup visible
        popup.show(this, x, y);
    }

    /**
     * Initialise the extensions to the text field i.e. start listening for
     * focus events and right mouse button clicks
     */
    private void initXtensions() {
        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if(SwingUtilities.isRightMouseButton(evt))
                        showPopup(evt.getX(), evt.getY());
                }
            });
        addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent evt) {
                    XTextField.this.selectAll();
                }

                public void focusLost(FocusEvent evt) {
                    //                if(popup.isVisible())
                    //                    popup.setVisible(false);
                }
            });
    }

    //  Supporting actions
    class CopyAction extends AbstractAction {
        public CopyAction() {
            putValue(Action.NAME, "Copy");
            putValue(Action.SMALL_ICON, new ResourceIcon("/com/sshtools/j2ssh/ui/copy.png"));
            putValue(Action.SHORT_DESCRIPTION, "Copy");
            putValue(Action.LONG_DESCRIPTION, "Copy the selection from the text and place it in the clipboard");
            putValue(Action.MNEMONIC_KEY, new Integer('c'));
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent evt) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(
                    getText()), XTextField.this);
        }
    }

    class CutAction extends AbstractAction {
        public CutAction() {
            putValue(Action.NAME, "Cut");
            putValue(Action.SMALL_ICON, new ResourceIcon("/com/sshtools/j2ssh/ui/cut.png"));
            putValue(Action.SHORT_DESCRIPTION, "Cut selection");
            putValue(Action.LONG_DESCRIPTION, "Cut the selection from the text and place it in the clipboard");
            putValue(Action.MNEMONIC_KEY, new Integer('u'));
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent evt) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(
                    getText()), XTextField.this);
            setText("");
        }
    }

    class PasteAction extends AbstractAction {
        public PasteAction() {
            putValue(Action.NAME, "Paste");
            putValue(Action.SMALL_ICON, new ResourceIcon("/com/sshtools/j2ssh/ui/paste.png"));
            putValue(Action.SHORT_DESCRIPTION, "Paste clipboard content");
            putValue(Action.LONG_DESCRIPTION, "Paste the clipboard contents to the current care position or replace the selection");
            putValue(Action.MNEMONIC_KEY, new Integer('p'));
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent evt) {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
                                    .getContents(this);

            if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    setText(t.getTransferData(DataFlavor.stringFlavor).toString());
                } catch(Exception e) {
                    //  Dont care
                }
            }
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction() {
            putValue(Action.NAME, "Delete");
            putValue(Action.SMALL_ICON, new ResourceIcon("/com/sshtools/j2ssh/ui/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Delete selection");
            putValue(Action.LONG_DESCRIPTION, "Delete the selection from the text");
            putValue(Action.MNEMONIC_KEY, new Integer('d'));
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        }
        public void actionPerformed(ActionEvent evt) {
            setText("");
        }
    }

    class SelectAllAction extends AbstractAction {
        SelectAllAction() {
            putValue(Action.SMALL_ICON, new EmptyIcon(16, 16));
            putValue(Action.NAME, "Select All");
            putValue(Action.SHORT_DESCRIPTION, "Select All");
            putValue(Action.LONG_DESCRIPTION, "Select all items in the context");
            putValue(Action.MNEMONIC_KEY, new Integer('a'));
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent evt) {
            selectAll();
        }
    }
}
