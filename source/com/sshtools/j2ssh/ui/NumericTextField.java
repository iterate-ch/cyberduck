/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * A text component that allows numeric entry only. The class of object that is
 * passed for minimum and maximum values, is the class that will be returned
 * as a value. For example, if <code>Integer</code> objects are passed, then a
 * <code>getValue</code> will return an <code>Integer</code> object.
 *
 * @author Brett Smith
 * @version $Id$
 *
 * @created 20 December 2002
 */
public class NumericTextField
    extends XTextField {
    private Color positiveBackground;
    private DecimalFormatSymbols symbols;

    //  Private instance variables
    private NumberFormat numberFormat;
    private boolean selectAllOnFocusGain;
    private int wColumnWidth;

    /**
     * Construct a new text field given a range.
     *
     * @param min minimum value that may be entered
     * @param max maximum value that may be entered
     */
    public NumericTextField(Number min, Number max) {
        this(min, max, min);
    }

    /**
     * Construct a new text field given a range.
     *
     * @param min minimum value that may be entered
     * @param max maximum value that may be entered
     * @param initial initial value
     * @param rightJustify justify display to right
     */
    public NumericTextField(Number min, Number max, Number initial,
                            boolean rightJustify) {
        this(min, max, initial, rightJustify, null);
    }

    /**
     * Construct a new text field given a range.
     *
     * @param min minimum value that may be entered
     * @param max maximum value that may be entered
     * @param initial initial value
     */
    public NumericTextField(Number min, Number max, Number initial) {
        this(min, max, initial, true);
    }

    /**
     * Construct a new text field given a range.
     *
     * @param min minimum value that may be entered
     * @param max maximum value that may be entered
     * @param initial initial value
     * @param rightJustify justify display to right
     * @param numberFormat a formatting string
     *
     * @throws IllegalArgumentException if the arguments are not all of the
     *         same class
     */
    public NumericTextField(Number min, Number max, Number initial,
                            boolean rightJustify, NumberFormat numberFormat) {
        super(Math.max(min.toString().length(), max.toString().length()));
        setNumberFormat(numberFormat);

        if (min.getClass().equals(max.getClass())
                && max.getClass().equals(initial.getClass())) {
            setDocument(new ADocument(min, max));
            setValue(initial);
        } else {
            throw new IllegalArgumentException("All arguments must be of the same class");
        }

        setRightJustify(rightJustify);
    }

    /*
     *  Overides <code>JTextFields</codes> calculation of the width of a single
     *  character (M space)
     *
     *  @return column width based on '9'
     *  public int getColumnWidth() {
     *  if (wColumnWidth==0) {
     *  FontMetrics metrics = getFontMetrics(getFont());
     *  wColumnWidth = metrics.charWidth('W');
     *  }
     *  return wColumnWidth;
     *  }
     */

    protected void processFocusEvent(FocusEvent e)
    {
        super.processFocusEvent(e);
        if(!e.isTemporary()) {
            switch(e.getID()) {
                case FocusEvent.FOCUS_LOST:
                    if(getNumberFormat() != null) {
                        String s = getNumberFormat().format(getValue()).toString();
                        if(!getText().equals(s)) {
                            setText(s);
                        }
                    }
                    break;
                case FocusEvent.FOCUS_GAINED:
                    if(isSelectAllOnFocusGain()) {
                        selectAll();
                    }
                    break;
            }
        }
    }

    /**
     * Return if this field should select everything on focus gain
     * @return
     */

    public boolean isSelectAllOnFocusGain() {
        return selectAllOnFocusGain;
    }

    /**
     * Set whether everything should be selected when focus is gained
     *
     * @param selectAllOnFocusGain select all on focus gain
     */
    public void setSelectAllOnFocusGain(boolean selectAllOnFocusGain) {
        this.selectAllOnFocusGain = selectAllOnFocusGain;
    }

    /**
     * Set the maximum value that may be entered.
     *
     * @param max maximum value
     */
    public void setMaximum(Number max) {
        ((ADocument) getDocument()).setMaximum(max);
    }

    /**
     * Get the maximum value that may be entered.
     *
     * @return the maximum value
     */
    public Number getMaximum() {
        return ((ADocument) getDocument()).max;
    }

    /**
     * Set the minimum value that may be entered.
     *
     * @param min minimum value
     */
    public void setMinimum(Number min) {
        ((ADocument) getDocument()).setMinimum(min);
    }

    /**
     * Return the minimum value this text component will allow to be entered.
     *
     * @return minimum value
     */
    public Number getMinimum() {
        return ((ADocument) getDocument()).min;
    }

    /**
     * Set the number formatter.
     *
     * @param numberFormat number formatter
     */
    public void setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;

        if (numberFormat instanceof DecimalFormat) {
            symbols = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
        } else {
            symbols = new DecimalFormatSymbols();
        }
    }

    /**
     * Get the number formatter.
     *
     * @return number formatter
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * Set whether or not the display is right justified.
     *
     * @param rightJustify right justified
     */
    public void setRightJustify(boolean rightJustify) {
        setHorizontalAlignment(rightJustify ? JTextField.RIGHT : JTextField.LEFT);
    }

    /**
     * Return whether or not the display is right justified.
     *
     * @return right justified
     */
    public boolean isRightJustify() {
        return getHorizontalAlignment()==JTextField.RIGHT;
    }

    /**
     * Set the numeric value given a string
     *
     * @param s string
     */
    public void setText(String s) {
        ADocument doc = (ADocument) getDocument();
        Number oldValue = doc.currentVal;

        try {
            doc.currentVal = doc.parse(s);
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }

        if (oldValue!=doc.currentVal) {
            doc.checkingEnabled = false;
            super.setText(s);
            doc.checkingEnabled = true;
        }
    }

    /**
     * Set the current numeric value of this text component. This must be of
     * the same class as what was provided for minimum and maximum values
     *
     * @param i the value
     */
    public void setValue(Number i) {
        setText(i.toString());
    }

    /**
     * Return the current numeric value of this text component. The return
     * value will be of the same class that was provided when the component
     * was constructed.
     *
     * @return minimum value
     */
    public Number getValue() {
        return ((ADocument) getDocument()).getValue();
    }

    //  Supporting classes

    /**
     * Description of the Class
     *
     * @author lee
     *
     * @created 20 December 2002
     */
    class ADocument
        extends PlainDocument {
        Number currentVal;
        Number max;
        Number min;
        boolean checkingEnabled = true;
        boolean rightJustify = true;

        /**
         * Creates a new ADocument object.
         *
         * @param min the minimum value
         * @param max the maximum value
         */
        public ADocument(Number min, Number max) {
            this.min = min;
            this.max = max;

            if (min.getClass().equals(Byte.class)) {
                currentVal = new Byte((byte) 0);
            } else {
                if (min.getClass().equals(Short.class)) {
                    currentVal = new Short((short) 0);
                } else {
                    if (min.getClass().equals(Integer.class)) {
                        currentVal = new Integer(0);
                    } else {
                        if (min.getClass().equals(Long.class)) {
                            currentVal = new Long(0L);
                        } else {
                            if (min.getClass().equals(Float.class)) {
                                currentVal = new Float(0f);
                            } else {
                                currentVal = new Double(0d);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Sets the maximum value
         *
         * @param max the maximum value
         */
        public void setMaximum(Number max) {
            this.max = max;
        }

        /**
         * Sets the minimum value
         *
         * @param min the miniumum value
         */
        public void setMinimum(Number min) {
            this.min = min;
        }

        /**
         * Sets this field to right justify
         *
         * @param rightJustify <tt>true</tt> to right justify <tt>false</tt>
         *        not
         */
        public void setRightJustify(boolean rightJustify) {
            this.rightJustify = rightJustify;
        }

        /**
         * Determines if we are justifying to the right
         *
         * @return <tt>true</tt> to right justify <tt>false</tt> not
         */
        public boolean isRightJustify() {
            return rightJustify;
        }

        /**
         * Gets the current value
         *
         * @return the current value
         */
        public Number getValue() {
            return currentVal;
        }

        /**
         * DOCUMENT ME!
         *
         * @param offs DOCUMENT ME!
         * @param str DOCUMENT ME!
         * @param a DOCUMENT ME!
         *
         * @throws BadLocationException DOCUMENT ME!
         */
        public void insertString(int offs, String str, AttributeSet a)
                          throws BadLocationException {
            if (str==null) {
                return;
            }

            if (!checkingEnabled) {
                super.insertString(offs, str, a);

                return;
            }

            String proposedResult = null;

            if (getLength()==0) {
                proposedResult = str;
            } else {
                StringBuffer currentBuffer =
                    new StringBuffer(getText(0, getLength()));
                currentBuffer.insert(offs, str);
                proposedResult = currentBuffer.toString();
            }

            try {
                currentVal = parse(proposedResult);
                super.insertString(offs, str, a);
            } catch (Exception e) {
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param proposedResult DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws NumberFormatException DOCUMENT ME!
         */
        public Number parse(String proposedResult)
                     throws NumberFormatException {
            Double d = new Double(0d);

            //   See if the proposed result matches the number format (if any)
            if (!proposedResult.equals(String.valueOf(symbols.getMinusSign()))
                    && (proposedResult.length()!=0)) {
                if (getNumberFormat()!=null) {
                    //   Strip out everything from the proposed result other than the
                    //   numbers and and decimal separators
                    StringBuffer sB = new StringBuffer();

                    for (int i = 0;i<proposedResult.length();i++) {
                        char ch = proposedResult.charAt(i);

                        if ((ch==symbols.getDecimalSeparator())
                                || ((ch>='0') && (ch<='9'))) {
                            sB.append(ch);
                        }
                    }

                    String s = sB.toString();

                    //   Find out how many digits there are before the decimal place
                    int i = 0;

                    for (;
                             (i<s.length())
                             && (s.charAt(i)!=symbols.getDecimalSeparator());
                             i++) {
                        ;
                    }

                    int before = i;
                    int after = 0;

                    if (before<s.length()) {
                        after = s.length() - i - 1;
                    }

                    if (before>getNumberFormat().getMaximumIntegerDigits()) {
                        throw new NumberFormatException("More digits BEFORE the decimal separator than allowed:"
                                                        + proposedResult);
                    }

                    if (after>getNumberFormat().getMaximumFractionDigits()) {
                        throw new NumberFormatException("More digits AFTER the decimal separator than allowed:"
                                                        + proposedResult);
                    }

                    //   Now try to parse the field against the number format
                    try {
                        d = new Double(getNumberFormat().parse(proposedResult)
                                           .doubleValue());
                    } catch (ParseException pE) {
                        throw new NumberFormatException("Failed to parse. "
                                                        + proposedResult
                                                        + pE.getMessage());
                    }
                }
                //   Just use the default parse
                else {
                    d = new Double(proposedResult);
                }
            }

            //   Now determine if the number if within range
            if ((d.doubleValue()>=min.doubleValue())
                    && (d.doubleValue()<=max.doubleValue())) {
                //   Now create the real type
                if (min.getClass().equals(Byte.class)) {
                    return new Byte(d.byteValue());
                } else {
                    if (min.getClass().equals(Short.class)) {
                        return new Short(d.shortValue());
                    } else {
                        if (min.getClass().equals(Integer.class)) {
                            return new Integer(d.intValue());
                        } else {
                            if (min.getClass().equals(Long.class)) {
                                return new Long(d.longValue());
                            } else {
                                if (min.getClass().equals(Float.class)) {
                                    return new Float(d.floatValue());
                                } else {
                                    return d;
                                }
                            }
                        }
                    }
                }
            } else {
                throw new NumberFormatException(d
                                                + " Is out of range. Minimum is "
                                                + min.doubleValue()
                                                + ", Maximum is "
                                                + max.doubleValue());
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param offs DOCUMENT ME!
         * @param len DOCUMENT ME!
         *
         * @throws BadLocationException DOCUMENT ME!
         */
        public void remove(int offs, int len)
                    throws BadLocationException {
            if (!checkingEnabled) {
                super.remove(offs, len);

                return;
            }

            String currentText = getText(0, getLength());
            String beforeOffset = currentText.substring(0, offs);
            String afterOffset =
                currentText.substring(len + offs, currentText.length());
            String proposedResult = beforeOffset + afterOffset;

            try {
                currentVal = parse(proposedResult);
                super.remove(offs, len);
            } catch (Exception e) {
            }
        }
    }
}
