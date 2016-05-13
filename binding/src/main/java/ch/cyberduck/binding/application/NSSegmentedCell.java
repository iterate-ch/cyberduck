package ch.cyberduck.binding.application;

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

import org.rococoa.cocoa.CGFloat;

/// <i>native declaration : :20</i>
public abstract class NSSegmentedCell extends NSActionCell {

    /**
     * only one button can be selected<br>
     * <i>native declaration : :12</i>
     */
    public static final int NSSegmentSwitchTrackingSelectOne = 0;
    /**
     * any button can be selected<br>
     * <i>native declaration : :13</i>
     */
    public static final int NSSegmentSwitchTrackingSelectAny = 1;
    /**
     * only selected while tracking<br>
     * <i>native declaration : :14</i>
     */
    public static final int NSSegmentSwitchTrackingMomentary = 2;

    /**
     * Number of segments<br>
     * Original signature : <code>void setSegmentCount(NSInteger)</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract void setSegmentCount(int count);

    /**
     * Original signature : <code>NSInteger segmentCount()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract int segmentCount();

    /**
     * Which button is active. May turn off other segments depending on mode.<br>
     * Original signature : <code>void setSelectedSegment(NSInteger)</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract void setSelectedSegment(int selectedSegment);

    /**
     * Original signature : <code>NSInteger selectedSegment()</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract int selectedSegment();

    /**
     * Original signature : <code>BOOL selectSegmentWithTag(NSInteger)</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract boolean selectSegmentWithTag(int tag);

    /**
     * For keyboard UI. Wraps.<br>
     * Original signature : <code>void makeNextSegmentKey()</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract void makeNextSegmentKey();

    /**
     * Original signature : <code>void makePreviousSegmentKey()</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract void makePreviousSegmentKey();

    /**
     * Original signature : <code>void setTrackingMode(NSSegmentSwitchTracking)</code><br>
     * <i>native declaration : :63</i>
     */
    public abstract void setTrackingMode(int trackingMode);

    /**
     * Original signature : <code>NSSegmentSwitchTracking trackingMode()</code><br>
     * <i>native declaration : :64</i>
     */
    public abstract int trackingMode();

    /**
     * Width of 0 means autosize to fit<br>
     * Original signature : <code>void setWidth(CGFloat, NSInteger)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract void setWidth_forSegment(CGFloat width, int segment);

    /**
     * Original signature : <code>CGFloat widthForSegment(NSInteger)</code><br>
     * <i>native declaration : :72</i>
     */
    public abstract CGFloat widthForSegment(int segment);

    /**
     * Original signature : <code>void setImage(NSImage*, NSInteger)</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract void setImage_forSegment(NSImage image, int segment);

    /**
     * Original signature : <code>NSImage* imageForSegment(NSInteger)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract NSImage imageForSegment(int segment);
    /**
     * <i>native declaration : :79</i><br>
     * Conversion Error : /// Original signature : <code>void setImageScaling(null, NSInteger)</code><br>
     * - (void)setImageScaling:(null)scaling forSegment:(NSInteger)segment; (Argument scaling cannot be converted)
     */
    /**
     * Original signature : <code>imageScalingForSegment(NSInteger)</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract com.sun.jna.Pointer imageScalingForSegment(int segment);

    /**
     * Original signature : <code>void setLabel(NSString*, NSInteger)</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract void setLabel_forSegment(String label, int segment);

    /**
     * Original signature : <code>NSString* labelForSegment(NSInteger)</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract String labelForSegment(int segment);

    /**
     * Original signature : <code>void setSelected(BOOL, NSInteger)</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract void setSelected_forSegment(boolean selected, int segment);

    /**
     * Original signature : <code>BOOL isSelectedForSegment(NSInteger)</code><br>
     * <i>native declaration : :88</i>
     */
    public abstract boolean isSelectedForSegment(int segment);

    /**
     * Original signature : <code>void setEnabled(BOOL, NSInteger)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract void setEnabled_forSegment(boolean enabled, int segment);

    /**
     * Original signature : <code>BOOL isEnabledForSegment(NSInteger)</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract boolean isEnabledForSegment(int segment);

    /**
     * Original signature : <code>void setMenu(NSMenu*, NSInteger)</code><br>
     * <i>native declaration : :93</i>
     */
    public abstract void setMenu_forSegment(NSMenu menu, int segment);

    /**
     * Original signature : <code>NSMenu* menuForSegment(NSInteger)</code><br>
     * <i>native declaration : :94</i>
     */
    public abstract NSMenu menuForSegment(int segment);

    /**
     * Original signature : <code>void setToolTip(NSString*, NSInteger)</code><br>
     * <i>native declaration : :96</i>
     */
    public abstract void setToolTip_forSegment(String toolTip, int segment);

    /**
     * Original signature : <code>NSString* toolTipForSegment(NSInteger)</code><br>
     * <i>native declaration : :97</i>
     */
    public abstract String toolTipForSegment(int segment);

    /**
     * Original signature : <code>void setTag(NSInteger, NSInteger)</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract void setTag_forSegment(int tag, int segment);

    /**
     * Original signature : <code>NSInteger tagForSegment(NSInteger)</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract int tagForSegment(int segment);
    /**
     * <i>native declaration : :104</i><br>
     * Conversion Error : /**<br>
     *  * see NSSegmentedControl.h for segment style names and values<br>
     *  * Original signature : <code>void setSegmentStyle(null)</code><br>
     *  * /<br>
     * - (void)setSegmentStyle:(null)segmentStyle; (Argument segmentStyle cannot be converted)
     */
    /**
     * Original signature : <code>segmentStyle()</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract int segmentStyle();
    /**
     * <i>native declaration : :110</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Describes the surface drawn onto in -[NSCell drawSegment:inFrame:withView:]. That method draws a segment interior, not the segment bezel.  This is both an override point and a useful method to call. A segmented cell that draws a custom bezel would override this to describe that surface. A cell that has custom segment drawing might query this method to help pick an image that looks good on the cell. Calling this method gives you some independence from changes in framework art style.<br>
     * Original signature : <code>interiorBackgroundStyleForSegment(NSInteger)</code><br>
     * <i>from NSSegmentBackgroundStyle native declaration : :119</i>
     */
    public abstract int interiorBackgroundStyleForSegment(int segment);
}
