package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.common.GUIFactory.java
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
import javax.swing.border.Border;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ItemListener;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

/**
 * A factory to produce common Swing GUI elements.
 */
public class GUIFactory {
    public static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public static final Font FONT_MONOSPACED_SMALL = new Font("Monospaced"/*"Lucida Grande Regular"*/, Font.PLAIN, Integer.parseInt(Preferences.instance().getProperty("font.small")));
    public static final Font FONT_SMALL = new Font("SansSerif"/*"Lucida Grande Regular"*/, Font.PLAIN, Integer.parseInt(Preferences.instance().getProperty("font.small")));
    public static final Font FONT_SMALL_BOLD = new Font("SansSerif"/*"Lucida Grande Bold"*/, Font.BOLD, Integer.parseInt(Preferences.instance().getProperty("font.small")));
    public static final Font FONT_NORMAL = new Font("SansSerif"/*"Lucida Grande Regular"*/, Font.PLAIN, Integer.parseInt(Preferences.instance().getProperty("font.normal")));
    public static final Font FONT_NORMAL_BOLD = new Font("SansSerif"/*"Lucida Grande Bold"*/, Font.BOLD, Integer.parseInt(Preferences.instance().getProperty("font.normal")));

    public static final Icon FOLDER_ICON = UIManager.getIcon("FileView.directoryIcon");
    public static final Icon FILE_ICON = UIManager.getIcon("FileView.fileIcon");
    public static final Icon HARDDRIVE_ICON = UIManager.getIcon("FileView.hardDriveIcon");

    public static final Icon UP_FOLDER_ICON = UIManager.getIcon("FileChooser.upFolderIcon");
    public static final Icon NEW_FOLDER_ICON = UIManager.getIcon("FileChooser.newFolderIcon");
    public static final Icon COMPUTER_ICON = UIManager.getIcon("FileView.computerIcon");

    public static final Icon UNKNOWN_ICON =  Cyberduck.getIcon(Cyberduck.getResource("unknown.gif"));
    
    public static final JLabel FOLDER_LABEL = new JLabel(FOLDER_ICON);
    public static final JLabel FILE_LABEL = new JLabel(FILE_ICON);
    public static final JLabel UNKNOWN_LABEL = new JLabel(UNKNOWN_ICON);

    public static final Icon GREEN_ICON = Cyberduck.getIcon(Cyberduck.getResource("blipGreen.gif"));
    public static final Icon BLUE_ICON = Cyberduck.getIcon(Cyberduck.getResource("blipBlue.gif"));
    public static final Icon RED_ICON = Cyberduck.getIcon(Cyberduck.getResource("blipRed.gif"));
    public static final Icon GRAY_ICON = Cyberduck.getIcon(Cyberduck.getResource("blipGray.gif"));

    public static final JLabel GREEN_LABEL = new JLabel(GREEN_ICON);
    public static final JLabel BLUE_LABEL = new JLabel(BLUE_ICON);
    public static final JLabel RED_LABEL = new JLabel(RED_ICON);
    public static final JLabel GRAY_LABEL = new JLabel(GRAY_ICON);
    /*
    public static final int TOP = 12;
    public static final int LEFT = 5;
    public static final int BOTTOM = 12;
    public static final int RIGHT = 5;
    */

    //LABELS
    public static JLabel labelBuilder(Icon icon, String name, Font font) {
        JLabel label = labelBuilder(name, font);
        label.setIcon(icon);
        return label;
    }

    public static JLabel labelBuilder(Icon icon) {
        JLabel label = new JLabel();
        label.setIcon(icon);
        return label;
    }
    
    public static JLabel labelBuilder(String name, Font font) {
        JLabel label = new JLabel(name);
        label.setFont(font);
        return label;
    }

    public static JLabel labelBuilder(Font font) {
        JLabel label = new JLabel();
        label.setFont(font);
        return label;
    }

    //BUTTONS
    public static JButton buttonBuilder(javax.swing.Action action) {
        JButton button = new JButton();
        button.setAction(action);
        return button;
    }

    public static JButton buttonBuilder(Font font, javax.swing.Action action) {
        JButton button = buttonBuilder(action);
        button.setFont(font);
        return button;
    }
    
    public static JButton buttonBuilder(String name, Font font, javax.swing.Action action) {
        JButton button = buttonBuilder(font, action);
        button.setText(name);
        return button;
    }
    
    public static JButton buttonBuilder(String name, Font font, java.awt.event.ActionListener listener) {
        JButton button = buttonBuilder(name, font);
        button.addActionListener(listener);
        return button;
    }

    public static JButton buttonBuilder(String name, Font font) {
        JButton button = new JButton(name);
        button.setFont(font);
        return button;
    }

    public static JButton buttonBuilder(Font font) {
        JButton button = new JButton();
        button.setFont(font);
        return button;
    }
    
    //COMBOBOX
    public static JComboBox comboBuilder(String[] items, int selected, Font font, ItemListener listener) {
        JComboBox combo = new JComboBox();
        for (int i = 0; i < items.length; i++) {
            combo.addItem(items[i]);
        }
        combo.addItemListener(listener);
        combo.setSelectedIndex(selected);
        combo.setFont(font);
        return combo;
    }

    public static JComboBox comboBuilder(String[] items, String selected, Font font) {
        JComboBox combo = new JComboBox();
        int index = 0;
        for (int i = 0; i < items.length; i++) {
            combo.addItem(items[i]);
            if(items[i].equals(selected)) {
                index = i;
            }
        }
        combo.setSelectedIndex(index);
        combo.setFont(font);
        return combo;
    }
    
    public static JComboBox comboBuilder(String[] items, String selected, Font font, ItemListener listener) {
        JComboBox combo = comboBuilder(items, selected, font);
        combo.addItemListener(listener);
        return combo;
    }
    
    // PANELS
    public static JPanel panelBuilder(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        return panel;
    }
    
    public static JPanel panelBuilder(LayoutManager l) {
        JPanel panel = new JPanel();
        panel.setLayout(l);
        return panel;
    }
    
    public static JPanel panelBuilder(Border border) {
        JPanel panel = panelBuilder(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(border);
        return panel;
    }

    
    //RADIO BUTTONS
    public static JRadioButton radiobuttonBuilder(ButtonGroup group, Font font, Action action) {
        JRadioButton b = new JRadioButton();
        group.add(b);
        b.setFont(font);
        b.setAction(action);
        return b;
    }

    public static JRadioButton radiobuttonBuilder(ButtonGroup group, String name, Font font) {
        return radiobuttonBuilder(group, name, font, false);
    }

    public static JRadioButton radiobuttonBuilder(ButtonGroup group, String name, Font font, boolean selected) {
        JRadioButton b = new JRadioButton(name, selected);
        b.setFont(font);
        group.add(b);
        return b;
    }

    public static JRadioButton radiobuttonBuilder(ButtonGroup group, String name, Font font, boolean selected, ItemListener listener) {
        JRadioButton b = radiobuttonBuilder(group, name, font, selected);
        b.addItemListener(listener);
        return b;
    }

    public static JRadioButton radiobuttonBuilder(ButtonGroup group, String name, Font font, String selected, ItemListener listener) {
        return radiobuttonBuilder(group, name, font, selected.equals("true"), listener);
    }

    //CHECKBOXES

    public static JCheckBox checkboxBuilder(Font font, Action action) {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setFont(font);
        checkbox.setAction(action);
        return checkbox;
    }

    public static JCheckBox checkboxBuilder(String name, Font font, String selected, Action action) {
        JCheckBox checkbox = checkboxBuilder(name, font, selected.equals("true"));
        checkbox.setAction(action);
        return checkbox;
    }
    
    public static JCheckBox checkboxBuilder(String name, Font font, String selected, ItemListener listener) {
        return checkboxBuilder(name, font, selected.equals("true"), listener);
    }

    public static JCheckBox checkboxBuilder(String name, Font font, boolean selected, ItemListener listener) {
        JCheckBox checkbox = checkboxBuilder(name, font, selected);
        checkbox.addItemListener(listener);
        return checkbox;
    }
    
    public static JCheckBox checkboxBuilder(String name, Font font) {
        return checkboxBuilder(name, font, false);
    }

    public static JCheckBox checkboxBuilder(String name, Font font, boolean selected) {
        JCheckBox checkbox = new JCheckBox(name, selected);
        checkbox.setFont(font);
        return checkbox;
    }
    

    //PASSWORD FIELDS
    public static JPasswordField passwordFieldBuilder(String property, Font font, InputVerifier verifier) {
        JPasswordField field = passwordFieldBuilder(font);
        field.setText(Preferences.instance().getProperty(property));
        field.setInputVerifier(verifier);
        return field;
    }

    public static JPasswordField passwordFieldBuilder(Font font, InputVerifier verifier) {
        JPasswordField field = passwordFieldBuilder(font);
        field.setInputVerifier(verifier);
        return field;
    }

    public static JPasswordField passwordFieldBuilder(Font font, int columns, InputVerifier verifier) {
        JPasswordField field = passwordFieldBuilder(font, verifier);
        field.setColumns(columns);
        return field;
    }
    
    public static JPasswordField passwordFieldBuilder(Font font) {
        JPasswordField field = new JPasswordField();
        field.setEchoChar('¥');
        field.setFont(font);
        return field;
    }

    public static JPasswordField passwordFieldBuilder(Font font, int columns) {
        JPasswordField field = passwordFieldBuilder(font);
        field.setColumns(columns);
        return field;
    }
    
    //TEXTFIELDS
    public static JTextField textFieldBuilder(String text, Font font, InputVerifier verifier) {
        JTextField field = textFieldBuilder(text, font);
        field.setInputVerifier(verifier);
        return field;
    }

    public static JTextField textFieldBuilder(String text, Font font, int columns, InputVerifier verifier) {
        JTextField field = textFieldBuilder(text, font, columns);
        field.setInputVerifier(verifier);
        return field;
    }
    
    public static JTextField textFieldBuilder(String text, Font font) {
        JTextField field = textFieldBuilder(font);
        field.setText(text);
        return field;
    }

    public static JTextField textFieldBuilder(String text, Font font, int columns) {
        JTextField field = textFieldBuilder(font, columns);
        field.setText(text);
        return field;
    }
    
    public static JTextField textFieldBuilder(Font font, InputVerifier verifier) {
        JTextField field = textFieldBuilder(font);
        field.setInputVerifier(verifier);
        return field;
    }

    public static JTextField textFieldBuilder(Font font, int columns, InputVerifier verifier) {
        JTextField field = textFieldBuilder(font, columns);
        field.setInputVerifier(verifier);
        return field;
    }
    
    public static JTextField textFieldBuilder(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        return field;
    }
    public static JTextField textFieldBuilder(Font font, int columns) {
        JTextField field = new JTextField();
        field.setFont(font);
        field.setColumns(columns);
        return field;
    }
    
    private GUIFactory() {

    }
}
