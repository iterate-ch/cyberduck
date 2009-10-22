package ch.cyberduck.ui.cocoa.application;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import org.rococoa.ID;

/// <i>native declaration : :42</i>
public abstract class NSProgressIndicator extends NSView {

    /// <i>native declaration : :22</i>
    public static final int NSProgressIndicatorPreferredThickness = 14;
    /// <i>native declaration : :23</i>
    public static final int NSProgressIndicatorPreferredSmallThickness = 10;
    /// <i>native declaration : :24</i>
    public static final int NSProgressIndicatorPreferredLargeThickness = 18;
    /// <i>native declaration : :25</i>
    public static final int NSProgressIndicatorPreferredAquaThickness = 12;
    /// <i>native declaration : :32</i>
    public static final int NSProgressIndicatorBarStyle = 0;
    /// <i>native declaration : :33</i>
    public static final int NSProgressIndicatorSpinningStyle = 1;

    /**
     * Original signature : <code>BOOL isIndeterminate()</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract boolean isIndeterminate();

    /**
     * Original signature : <code>void setIndeterminate(BOOL)</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract void setIndeterminate(boolean flag);

    /**
     * Original signature : <code>BOOL isBezeled()</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract boolean isBezeled();

    /**
     * Original signature : <code>void setBezeled(BOOL)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract void setBezeled(boolean flag);

    /**
     * Original signature : <code>controlTint()</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract int controlTint();
    /**
     * <i>native declaration : :93</i><br>
     * Conversion Error : /// Original signature : <code>void setControlTint(null)</code><br>
     * - (void)setControlTint:(null)tint; (Argument tint cannot be converted)
     */
    /**
     * Original signature : <code>controlSize()</code><br>
     * <i>native declaration : :95</i>
     */
    public abstract int controlSize();

    /**
     * <i>native declaration : :96</i><br>
     * Conversion Error : /// Original signature : <code>void setControlSize(null)</code><br>
     * - (void)setControlSize:(null)size; (Argument size cannot be converted)
     */
    public abstract void setControlSize(int size);

    /**
     * Original signature : <code>double doubleValue()</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract double doubleValue();

    /**
     * Original signature : <code>void setDoubleValue(double)</code><br>
     * <i>native declaration : :101</i>
     */
    public abstract void setDoubleValue(double doubleValue);

    /**
     * Original signature : <code>void incrementBy(double)</code><br>
     * equivalent to [self setDoubleValue:[self doubleValue] + delta]<br>
     * <i>native declaration : :103</i>
     */
    public abstract void incrementBy(double delta);

    /**
     * Original signature : <code>double minValue()</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract double minValue();

    /**
     * Original signature : <code>double maxValue()</code><br>
     * <i>native declaration : :106</i>
     */
    public abstract double maxValue();

    /**
     * Original signature : <code>void setMinValue(double)</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract void setMinValue(double newMinimum);

    /**
     * Original signature : <code>void setMaxValue(double)</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract void setMaxValue(double newMaximum);
    /**
     * <i>native declaration : :112</i><br>
     * Conversion Error : NSTimeInterval
     */
    /**
     * <i>native declaration : :113</i><br>
     * Conversion Error : NSTimeInterval
     */
    /**
     * Original signature : <code>BOOL usesThreadedAnimation()</code><br>
     * returns YES if the PI uses a thread instead of a timer (default in NO)<br>
     * <i>native declaration : :115</i>
     */
    public abstract boolean usesThreadedAnimation();

    /**
     * Original signature : <code>void setUsesThreadedAnimation(BOOL)</code><br>
     * <i>native declaration : :116</i>
     */
    public abstract void setUsesThreadedAnimation(boolean threadedAnimation);

    /**
     * Original signature : <code>void startAnimation(id)</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract void startAnimation(ID sender);

    /**
     * Original signature : <code>void stopAnimation(id)</code><br>
     * <i>native declaration : :119</i>
     */
    public abstract void stopAnimation(ID sender);

    /**
     * Original signature : <code>void animate(id)</code><br>
     * manual animation<br>
     * <i>native declaration : :121</i>
     */
    public abstract void animate(final ID sender);

    /**
     * Original signature : <code>void setStyle(NSProgressIndicatorStyle)</code><br>
     * <i>native declaration : :125</i>
     */
    public abstract void setStyle(int style);

    /**
     * Original signature : <code>NSProgressIndicatorStyle style()</code><br>
     * <i>native declaration : :126</i>
     */
    public abstract int style();

    /**
     * For the bar style, the height will be set to the recommended height.<br>
     * Original signature : <code>void sizeToFit()</code><br>
     * <i>native declaration : :130</i>
     */
    public abstract void sizeToFit();

    /**
     * Original signature : <code>BOOL isDisplayedWhenStopped()</code><br>
     * <i>native declaration : :132</i>
     */
    public abstract boolean isDisplayedWhenStopped();

    /**
     * Original signature : <code>void setDisplayedWhenStopped(BOOL)</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract void setDisplayedWhenStopped(boolean isDisplayed);
}
