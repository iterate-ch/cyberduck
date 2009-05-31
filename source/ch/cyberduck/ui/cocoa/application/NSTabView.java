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

import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

/// <i>native declaration : :23</i>
public interface NSTabView extends NSObject {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSTabView", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSTabView alloc();
    }

    /**
     * Original signature : <code>void selectTabViewItem(NSTabViewItem*)</code><br>
     * <i>native declaration : :74</i>
     */
    void selectTabViewItem(NSTabViewItem tabViewItem);

    /**
     * Original signature : <code>void selectTabViewItemAtIndex(NSInteger)</code><br>
     * May raise an NSRangeException<br>
     * <i>native declaration : :75</i>
     */
    void selectTabViewItemAtIndex(int index);

    /**
     * Original signature : <code>void selectTabViewItemWithIdentifier(id)</code><br>
     * May raise an NSRangeException if identifier not found<br>
     * <i>native declaration : :76</i>
     */
    void selectTabViewItemWithIdentifier(NSObject identifier);

    /**
     * Original signature : <code>void takeSelectedTabViewItemFromSender(id)</code><br>
     * May raise an NSRangeException<br>
     * <i>native declaration : :77</i>
     */
    void takeSelectedTabViewItemFromSender(NSObject sender);

    /**
     * Original signature : <code>void selectFirstTabViewItem(id)</code><br>
     * <i>native declaration : :81</i>
     */
    void selectFirstTabViewItem(NSTabViewItem sender);

    /**
     * Original signature : <code>void selectLastTabViewItem(id)</code><br>
     * <i>native declaration : :82</i>
     */
    void selectLastTabViewItem(NSTabViewItem sender);

    /**
     * Original signature : <code>void selectNextTabViewItem(id)</code><br>
     * <i>native declaration : :83</i>
     */
    void selectNextTabViewItem(NSTabViewItem sender);

    /**
     * Original signature : <code>void selectPreviousTabViewItem(id)</code><br>
     * <i>native declaration : :84</i>
     */
    void selectPreviousTabViewItem(NSTabViewItem sender);

    /**
     * Original signature : <code>NSTabViewItem* selectedTabViewItem()</code><br>
     * return nil if none are selected<br>
     * <i>native declaration : :88</i>
     */
    NSTabViewItem selectedTabViewItem();

    /**
     * Original signature : <code>NSFont* font()</code><br>
     * returns font used for all tab labels.<br>
     * <i>native declaration : :89</i>
     */
    NSFont font();

    /**
     * Original signature : <code>NSTabViewType tabViewType()</code><br>
     * <i>native declaration : :90</i>
     */
    int tabViewType();

    /**
     * Original signature : <code>NSArray* tabViewItems()</code><br>
     * <i>native declaration : :91</i>
     */
    NSArray tabViewItems();

    /**
     * Original signature : <code>BOOL allowsTruncatedLabels()</code><br>
     * <i>native declaration : :92</i>
     */
    boolean allowsTruncatedLabels();

    /**
     * Original signature : <code>minimumSize()</code><br>
     * returns the minimum size of the tab view<br>
     * <i>native declaration : :93</i>
     */
    NSObject minimumSize();

    /**
     * Original signature : <code>BOOL drawsBackground()</code><br>
     * only relevant for borderless tab view type<br>
     * <i>native declaration : :94</i>
     */
    boolean drawsBackground();

    /**
     * Original signature : <code>controlTint()</code><br>
     * <i>native declaration : :95</i>
     */
    NSObject controlTint();

    /**
     * Original signature : <code>controlSize()</code><br>
     * <i>native declaration : :96</i>
     */
    NSObject controlSize();

    /**
     * Original signature : <code>void setFont(NSFont*)</code><br>
     * <i>native declaration : :100</i>
     */
    void setFont(NSFont font);

    /**
     * Original signature : <code>void setTabViewType(NSTabViewType)</code><br>
     * <i>native declaration : :101</i>
     */
    void setTabViewType(int tabViewType);

    /**
     * Original signature : <code>void setAllowsTruncatedLabels(BOOL)</code><br>
     * <i>native declaration : :102</i>
     */
    void setAllowsTruncatedLabels(boolean allowTruncatedLabels);

    /**
     * Original signature : <code>void setDrawsBackground(BOOL)</code><br>
     * only relevant for borderless tab view type<br>
     * <i>native declaration : :103</i>
     */
    void setDrawsBackground(boolean flag);
    /**
     * <i>native declaration : :104</i><br>
     * Conversion Error : /// Original signature : <code>void setControlTint(null)</code><br>
     * - (void)setControlTint:(null)controlTint; (Argument controlTint cannot be converted)
     */
    /**
     * <i>native declaration : :105</i><br>
     * Conversion Error : /// Original signature : <code>void setControlSize(null)</code><br>
     * - (void)setControlSize:(null)controlSize; (Argument controlSize cannot be converted)
     */
    /**
     * Original signature : <code>void addTabViewItem(NSTabViewItem*)</code><br>
     * Add tab at the end.<br>
     * <i>native declaration : :109</i>
     */
    void addTabViewItem(NSTabViewItem tabViewItem);

    /**
     * Original signature : <code>void insertTabViewItem(NSTabViewItem*, NSInteger)</code><br>
     * May raise an NSRangeException<br>
     * <i>native declaration : :110</i>
     */
    void insertTabViewItem_atIndex(NSTabViewItem tabViewItem, int index);

    /**
     * Original signature : <code>void removeTabViewItem(NSTabViewItem*)</code><br>
     * tabViewItem must be an existing tabViewItem<br>
     * <i>native declaration : :111</i>
     */
    void removeTabViewItem(NSTabViewItem tabViewItem);

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :115</i>
     */
    void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :116</i>
     */
    NSObject delegate();
    /**
     * <i>native declaration : :120</i><br>
     * Conversion Error : /// Original signature : <code>NSTabViewItem* tabViewItemAtPoint(null)</code><br>
     * - (NSTabViewItem*)tabViewItemAtPoint:(null)point; // point in local coordinates. returns nil if none.<br>
     *  (Argument point cannot be converted)
     */
    /**
     * Original signature : <code>contentRect()</code><br>
     * Return the rect available for a "page".<br>
     * <i>native declaration : :124</i>
     */
    NSObject contentRect();

    /**
     * Original signature : <code>NSInteger numberOfTabViewItems()</code><br>
     * <i>native declaration : :128</i>
     */
    int numberOfTabViewItems();

    /**
     * Original signature : <code>NSInteger indexOfTabViewItem(NSTabViewItem*)</code><br>
     * NSNotFound if not found<br>
     * <i>native declaration : :129</i>
     */
    int indexOfTabViewItem(NSTabViewItem tabViewItem);

    /**
     * Original signature : <code>NSTabViewItem* tabViewItemAtIndex(NSInteger)</code><br>
     * May raise an NSRangeException<br>
     * <i>native declaration : :130</i>
     */
    NSTabViewItem tabViewItemAtIndex(int index);

    /**
     * Original signature : <code>NSInteger indexOfTabViewItemWithIdentifier(id)</code><br>
     * NSNotFound if not found<br>
     * <i>native declaration : :131</i>
     */
    int indexOfTabViewItemWithIdentifier(NSObject identifier);
}
