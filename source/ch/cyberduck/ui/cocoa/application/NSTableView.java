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

import ch.cyberduck.ui.cocoa.foundation.*;

import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.NSPoint;

/// <i>native declaration : :69</i>
public interface NSTableView extends NSControl {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSTableView", _Class.class);

    /// <i>native declaration : :60</i>
    public static final int NSTableViewDropOn = 0;
    /// <i>native declaration : :60</i>
    public static final int NSTableViewDropAbove = 1;

    public static final String NSTableViewSelectionDidChangeNotification = "NSTableViewSelectionDidChangeNotification";
    public static final String NSTableViewColumnDidMoveNotification = "NSTableViewColumnDidMoveNotification";
    public static final String NSTableViewColumnDidResizeNotification = "NSTableViewColumnDidResizeNotification";
    public static final String NSTableViewSelectionIsChangingNotification = "NSTableViewSelectionIsChangingNotification";

    public static final int NSTableViewGridNone = 0;
    public static final int NSTableViewSolidVerticalGridLineMask = 1;
    public static final int NSTableViewSolidHorizontalGridLineMask = 2;

    public static final int NSTableViewNoColumnAutoresizing = 0;
    public static final int NSTableViewUniformColumnAutoresizingStyle = 1;
    public static final int NSTableViewSequentialColumnAutoresizingStyle = 2;
    public static final int NSTableViewReverseSequentialColumnAutoresizingStyle = 3;
    public static final int NSTableViewLastColumnOnlyAutoresizingStyle = 4;
    public static final int NSTableViewFirstColumnOnlyAutoresizingStyle = 5;

    public interface _Class extends org.rococoa.NSClass {
        NSTableView alloc();
    }

    public static interface DataSource {
        int numberOfRowsInTableView(NSTableView view);

//        void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value, NSTableColumn tableColumn, int row);

        NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, int row);
    }

    /**
     * Original signature : <code>void addTableColumn(NSTableColumn*)</code><br>
     * <i>native declaration : :98</i>
     */
    void addTableColumn(NSTableColumn column);

    /**
     * Original signature : <code>void setDataSource(id)</code><br>
     * <i>native declaration : :100</i>
     */
    void setDataSource(org.rococoa.ID aSource);

    /**
     * Original signature : <code>id dataSource()</code><br>
     * <i>native declaration : :101</i>
     */
    NSObject dataSource();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :102</i>
     */
    void setDelegate(org.rococoa.ID delegate);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :103</i>
     */
    org.rococoa.ID delegate();

    /**
     * Original signature : <code>void setHeaderView(NSTableHeaderView*)</code><br>
     * <i>native declaration : :104</i>
     */
    void setHeaderView(com.sun.jna.Pointer headerView);

    /**
     * Original signature : <code>NSTableHeaderView* headerView()</code><br>
     * <i>native declaration : :105</i>
     */
    com.sun.jna.Pointer headerView();

    /**
     * Original signature : <code>void setCornerView(NSView*)</code><br>
     * <i>native declaration : :107</i>
     */
    void setCornerView(NSView cornerView);

    /**
     * Original signature : <code>NSView* cornerView()</code><br>
     * <i>native declaration : :108</i>
     */
    com.sun.jna.Pointer cornerView();

    /**
     * Original signature : <code>void setAllowsColumnReordering(BOOL)</code><br>
     * <i>native declaration : :110</i>
     */
    void setAllowsColumnReordering(boolean flag);

    /**
     * Original signature : <code>BOOL allowsColumnReordering()</code><br>
     * <i>native declaration : :111</i>
     */
    boolean allowsColumnReordering();

    /**
     * Controls whether the user can attemp to resize columns by dragging between headers. If flag is YES the user can resize columns; if flag is NO the user can't. The default is YES. Columns can only be resized if a column allows user resizing.  See NSTableColumn setResizingMask: for more details.  You can always change columns programmatically regardless of this setting.<br>
     * Original signature : <code>void setAllowsColumnResizing(BOOL)</code><br>
     * <i>native declaration : :115</i>
     */
    void setAllowsColumnResizing(boolean flag);

    /**
     * Original signature : <code>BOOL allowsColumnResizing()</code><br>
     * <i>native declaration : :116</i>
     */
    boolean allowsColumnResizing();

    /**
     * <i>native declaration : :120</i><br>
     * Conversion Error : /// Original signature : <code>void setColumnAutoresizingStyle(null)</code><br>
     * - (void)setColumnAutoresizingStyle:(null)style; (Argument style cannot be converted)
     */
    void setColumnAutoresizingStyle(int style);

    /**
     * Original signature : <code>columnAutoresizingStyle()</code><br>
     * <i>native declaration : :121</i>
     */
    int columnAutoresizingStyle();

    /**
     * Original signature : <code>void setGridStyleMask(NSUInteger)</code><br>
     * <i>native declaration : :125</i>
     */
    void setGridStyleMask(int gridType);

    /**
     * Original signature : <code>NSUInteger gridStyleMask()</code><br>
     * <i>native declaration : :126</i>
     */
    int gridStyleMask();
    /**
     * <i>native declaration : :128</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :129</i><br>
     * Conversion Error : NSSize
     */
    /**
     * Configures the table to use either the standard alternating row colors, or a solid color for its background.<br>
     * Original signature : <code>void setUsesAlternatingRowBackgroundColors(BOOL)</code><br>
     * <i>native declaration : :133</i>
     */
    void setUsesAlternatingRowBackgroundColors(boolean useAlternatingRowColors);

    /**
     * Original signature : <code>BOOL usesAlternatingRowBackgroundColors()</code><br>
     * <i>native declaration : :134</i>
     */
    boolean usesAlternatingRowBackgroundColors();

    /**
     * The backgroundColor defaults to [NSColor controlBackgroundColor]. On Mac OS 10.5 and higher, the alpha portion of 'color' is properly used when drawing the backgroundColor. To have a transparent tableView, set the backgroundColor to [NSColor clearColor], and set the enclosing NSScrollView to not draw its background with: [[tableView enclosingScrollView] setDrawsBackground:NO]. NSTableView uses NSCompositeSourceOver when drawing the background color.<br>
     * Original signature : <code>void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : :138</i>
     */
    void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : :139</i>
     */
    NSColor backgroundColor();

    /**
     * Original signature : <code>void setGridColor(NSColor*)</code><br>
     * <i>native declaration : :141</i>
     */
    void setGridColor(NSColor color);

    /**
     * Original signature : <code>NSColor* gridColor()</code><br>
     * <i>native declaration : :142</i>
     */
    NSColor gridColor();

    /**
     * Original signature : <code>void setRowHeight(CGFloat)</code><br>
     * <i>native declaration : :144</i>
     */
    void setRowHeight(CGFloat rowHeight);

    /**
     * Original signature : <code>CGFloat rowHeight()</code><br>
     * <i>native declaration : :145</i>
     */
    CGFloat rowHeight();

    /**
     * If the delegate implements -tableView:heightOfRow:, this method immediately re-tiles the table view using row heights it provides.<br>
     * Original signature : <code>void noteHeightOfRowsWithIndexesChanged(NSIndexSet*)</code><br>
     * <i>native declaration : :149</i>
     */
    void noteHeightOfRowsWithIndexesChanged(NSIndexSet indexSet);

    /**
     * Original signature : <code>NSArray* tableColumns()</code><br>
     * <i>native declaration : :151</i>
     */
    NSArray tableColumns();

    /**
     * Original signature : <code>NSInteger numberOfColumns()</code><br>
     * <i>native declaration : :152</i>
     */
    int numberOfColumns();

    /**
     * Original signature : <code>NSInteger numberOfRows()</code><br>
     * <i>native declaration : :153</i>
     */
    int numberOfRows();

    /**
     * Original signature : <code>void removeTableColumn(NSTableColumn*)</code><br>
     * <i>native declaration : :156</i>
     */
    void removeTableColumn(NSTableColumn column);

    /**
     * Original signature : <code>NSInteger columnWithIdentifier(id)</code><br>
     * <i>native declaration : :157</i>
     */
    int columnWithIdentifier(String identifier);

    /**
     * Original signature : <code>NSTableColumn* tableColumnWithIdentifier(id)</code><br>
     * <i>native declaration : :158</i>
     */
    NSTableColumn tableColumnWithIdentifier(String identifier);

    /**
     * Original signature : <code>void tile()</code><br>
     * <i>native declaration : :160</i>
     */
    void tile();

    /**
     * Original signature : <code>void sizeToFit()</code><br>
     * <i>native declaration : :161</i>
     */
    void sizeToFit();

    /**
     * Original signature : <code>void sizeLastColumnToFit()</code><br>
     * <i>native declaration : :162</i>
     */
    void sizeLastColumnToFit();

    /**
     * Original signature : <code>void scrollRowToVisible(NSInteger)</code><br>
     * <i>native declaration : :163</i>
     */
    void scrollRowToVisible(int row);

    /**
     * Original signature : <code>void scrollColumnToVisible(NSInteger)</code><br>
     * <i>native declaration : :164</i>
     */
    void scrollColumnToVisible(int column);

    /**
     * Original signature : <code>void moveColumn(NSInteger, NSInteger)</code><br>
     * <i>native declaration : :165</i>
     */
    void moveColumn_toColumn(int column, int newIndex);

    /**
     * Original signature : <code>void reloadData()</code><br>
     * <i>native declaration : :167</i>
     */
    void reloadData();

    /**
     * Original signature : <code>void noteNumberOfRowsChanged()</code><br>
     * <i>native declaration : :168</i>
     */
    void noteNumberOfRowsChanged();

    /**
     * Original signature : <code>NSInteger editedColumn()</code><br>
     * <i>native declaration : :170</i>
     */
    int editedColumn();

    /**
     * Original signature : <code>NSInteger editedRow()</code><br>
     * <i>native declaration : :171</i>
     */
    int editedRow();

    /**
     * Original signature : <code>NSInteger clickedColumn()</code><br>
     * <i>native declaration : :172</i>
     */
    int clickedColumn();

    /**
     * Original signature : <code>NSInteger clickedRow()</code><br>
     * <i>native declaration : :173</i>
     */
    int clickedRow();

    /**
     * Original signature : <code>void setDoubleAction(SEL)</code><br>
     * <i>native declaration : :175</i>
     */
    void setDoubleAction(org.rococoa.Selector aSelector);

    /**
     * Original signature : <code>SEL doubleAction()</code><br>
     * <i>native declaration : :176</i>
     */
    org.rococoa.Selector doubleAction();

    /**
     * Sorting Support<br>
     * The array of sort descriptors is archived.  Sort descriptors will persist along with other column information if an autosave name is set.<br>
     * Original signature : <code>void setSortDescriptors(NSArray*)</code><br>
     * <i>native declaration : :182</i>
     */
    void setSortDescriptors(NSArray array);

    /**
     * Original signature : <code>NSArray* sortDescriptors()</code><br>
     * <i>native declaration : :183</i>
     */
    NSArray sortDescriptors();

    /**
     * Support for little "indicator" images in table header cells.<br>
     * Original signature : <code>void setIndicatorImage(NSImage*, NSTableColumn*)</code><br>
     * <i>native declaration : :187</i>
     */
    void setIndicatorImage_inTableColumn(NSImage anImage, NSTableColumn tc);

    /**
     * Original signature : <code>NSImage* indicatorImageInTableColumn(NSTableColumn*)</code><br>
     * <i>native declaration : :188</i>
     */
    NSImage indicatorImageInTableColumn(NSTableColumn tc);

    /**
     * Support for highlightable column header, for use with row selection.<br>
     * Original signature : <code>void setHighlightedTableColumn(NSTableColumn*)</code><br>
     * <i>native declaration : :192</i>
     */
    void setHighlightedTableColumn(NSTableColumn tc);

    /**
     * Original signature : <code>NSTableColumn* highlightedTableColumn()</code><br>
     * <i>native declaration : :193</i>
     */
    NSTableColumn highlightedTableColumn();

    /**
     * Original signature : <code>void setVerticalMotionCanBeginDrag(BOOL)</code><br>
     * <i>native declaration : :199</i>
     */
    void setVerticalMotionCanBeginDrag(boolean flag);

    /**
     * Original signature : <code>BOOL verticalMotionCanBeginDrag()</code><br>
     * <i>native declaration : :200</i>
     */
    boolean verticalMotionCanBeginDrag();
    /**
     * <i>native declaration : :206</i><br>
     * Conversion Error : /**<br>
     *  * The return value indicates whether the receiver can attempt to initiate a row drag at 'mouseDownPoint'. Return NO to disallow initiating drags at the given location. <br>
     *  * For applications linked on and after Leopard, NSCell hit testing will determine if a row can be dragged or not. Custom cells should properly implement [NSCell(NSCellHitTest) hitTestForEvent:inRect:ofView]; see NSCell.h for more information. NSTableView will not begin a drag if cell returns NSCellHitTrackableArea.<br>
     *  * Original signature : <code>BOOL canDragRowsWithIndexes(NSIndexSet*, null)</code><br>
     *  * /<br>
     * - (BOOL)canDragRowsWithIndexes:(NSIndexSet*)rowIndexes atPoint:(null)mouseDownPoint; (Argument mouseDownPoint cannot be converted)
     */
    /**
     * <i>native declaration : :212</i><br>
     * Conversion Error : /**<br>
     *  * This method computes and returns an image to use for dragging.  Override this to return a custom image.  'dragRows' represents the rows participating in the drag.  'tableColumns' represent the columns that should be in the output image.  Note that drawing may be clipped to the visible rows, and columns.  'dragEvent' is a reference to the mouse down event that began the drag.  'dragImageOffset' is an in/out parameter.  This method will be called with dragImageOffset set to NSZeroPoint, but it can be modified to re-position the returned image.  A dragImageOffset of NSZeroPoint will cause the image to be centered under the mouse.<br>
     *  * Compatability Note: This method replaces dragImageForRows:event:dragImageOffset:.  If present, this is used instead of the deprecated method.<br>
     *  * Original signature : <code>NSImage* dragImageForRowsWithIndexes(NSIndexSet*, NSArray*, NSEvent*, null)</code><br>
     *  * /<br>
     * - (NSImage*)dragImageForRowsWithIndexes:(NSIndexSet*)dragRows tableColumns:(NSArray*)tableColumns event:(NSEvent*)dragEvent offset:(null)dragImageOffset; (Argument dragImageOffset cannot be converted)
     */
    /**
     * <i>native declaration : :216</i><br>
     * Conversion Error : /**<br>
     *  * Configures the default value returned from -draggingSourceOperationMaskForLocal:.  An isLocal value of YES indicates that 'mask' applies when the destination object is in the same application.  A isLocal value of NO indicates that 'mask' applies when the destination object in an application outside the receiver's application.  NSTableView will archive the values you set for each isLocal setting.<br>
     *  * Original signature : <code>void setDraggingSourceOperationMask(null, BOOL)</code><br>
     *  * /<br>
     * - (void)setDraggingSourceOperationMask:(null)mask forLocal:(BOOL)isLocal; (Argument mask cannot be converted)
     */
    /**
     * To be used from validateDrop: if you wish to "re-target" the proposed drop. To specify a drop on the second row, one would specify row=2, and op=NSTableViewDropOn. To specify a drop below the last row, one would specify row=[tv numberOfRows], and op=NSTableViewDropAbove. To specify a drop on the entire tableview, one would specify row=-1 and op=NSTableViewDropOn.<br>
     * Original signature : <code>void setDropRow(NSInteger, NSTableViewDropOperation)</code><br>
     * <i>native declaration : :220</i>
     */
    void setDropRow_dropOperation(int row, int op);

    /**
     * Selection<br>
     * Original signature : <code>void setAllowsMultipleSelection(BOOL)</code><br>
     * <i>native declaration : :226</i>
     */
    void setAllowsMultipleSelection(boolean flag);

    /**
     * Original signature : <code>BOOL allowsMultipleSelection()</code><br>
     * <i>native declaration : :227</i>
     */
    boolean allowsMultipleSelection();

    /**
     * Original signature : <code>void setAllowsEmptySelection(BOOL)</code><br>
     * <i>native declaration : :228</i>
     */
    void setAllowsEmptySelection(boolean flag);

    /**
     * Original signature : <code>BOOL allowsEmptySelection()</code><br>
     * <i>native declaration : :229</i>
     */
    boolean allowsEmptySelection();

    /**
     * Original signature : <code>void setAllowsColumnSelection(BOOL)</code><br>
     * <i>native declaration : :230</i>
     */
    void setAllowsColumnSelection(boolean flag);

    /**
     * Original signature : <code>BOOL allowsColumnSelection()</code><br>
     * <i>native declaration : :231</i>
     */
    boolean allowsColumnSelection();

    /**
     * Original signature : <code>void selectAll(id)</code><br>
     * <i>native declaration : :232</i>
     */
    void selectAll(NSObject sender);

    /**
     * Original signature : <code>void deselectAll(id)</code><br>
     * <i>native declaration : :233</i>
     */
    void deselectAll(NSObject sender);

    /**
     * Sets the column selection using the indexes.  Selection is set/extended based on the extend flag. <br>
     * Compatability Note: This method replaces selectColumn:byExtendingSelection:<br>
     * If a subclasser implements only the deprecated single-index method (selectColumn:byExtendingSelection:), the single-index method will be invoked for each index.  If a subclasser implements the multi-index method (selectColumnIndexes:byExtendingSelection:), the deprecated single-index version method will not be used.  This allows subclassers already overriding the single-index method to still receive a selection message.  Note: to avoid cycles, subclassers of this method and single-index method should not call each other.<br>
     * Original signature : <code>void selectColumnIndexes(NSIndexSet*, BOOL)</code><br>
     * <i>native declaration : :241</i>
     */
    void selectColumnIndexes_byExtendingSelection(NSIndexSet indexes, boolean extend);

    /**
     * Sets the row selection using 'indexes'. Selection is set/extended based on the extend flag. On 10.5 and greater, selectRowIndexes:byExtendingSelection: will allow you to progrmatically select more than one index, regardless of the allowsMultipleSelection and allowsEmptySelection options set on the table.<br>
     * Compatability Note: This method replaces selectRow:byExtendingSelection:<br>
     * If a subclasser implements only the deprecated single-index method (selectRow:byExtendingSelection:), the single-index method will be invoked for each index.  If a subclasser implements the multi-index method (selectRowIndexes:byExtendingSelection:), the deprecated single-index version method will not be used.  This allows subclassers already overriding the single-index method to still receive a selection message.  Note: to avoid cycles, subclassers of this method and single-index method should not call each other.<br>
     * Original signature : <code>void selectRowIndexes(NSIndexSet*, BOOL)</code><br>
     * <i>native declaration : :248</i>
     */
    void selectRowIndexes_byExtendingSelection(NSIndexSet indexes, boolean extend);

    /**
     * Original signature : <code>NSIndexSet* selectedColumnIndexes()</code><br>
     * <i>native declaration : :250</i>
     */
    NSIndexSet selectedColumnIndexes();

    /**
     * Original signature : <code>NSIndexSet* selectedRowIndexes()</code><br>
     * <i>native declaration : :251</i>
     */
    NSIndexSet selectedRowIndexes();

    /**
     * Original signature : <code>void deselectColumn(NSInteger)</code><br>
     * <i>native declaration : :254</i>
     */
    void deselectColumn(int column);

    /**
     * Original signature : <code>void deselectRow(NSInteger)</code><br>
     * <i>native declaration : :255</i>
     */
    void deselectRow(int row);

    /**
     * Original signature : <code>NSInteger selectedColumn()</code><br>
     * <i>native declaration : :256</i>
     */
    int selectedColumn();

    /**
     * Original signature : <code>NSInteger selectedRow()</code><br>
     * <i>native declaration : :257</i>
     */
    int selectedRow();

    /**
     * Original signature : <code>BOOL isColumnSelected(NSInteger)</code><br>
     * <i>native declaration : :258</i>
     */
    boolean isColumnSelected(int column);

    /**
     * Original signature : <code>BOOL isRowSelected(NSInteger)</code><br>
     * <i>native declaration : :259</i>
     */
    boolean isRowSelected(int row);

    /**
     * Original signature : <code>NSInteger numberOfSelectedColumns()</code><br>
     * <i>native declaration : :260</i>
     */
    int numberOfSelectedColumns();

    /**
     * Original signature : <code>NSInteger numberOfSelectedRows()</code><br>
     * <i>native declaration : :261</i>
     */
    int numberOfSelectedRows();

    /**
     * Original signature : <code>BOOL allowsTypeSelect()</code><br>
     * <i>native declaration : :269</i>
     */
    boolean allowsTypeSelect();

    /**
     * Original signature : <code>void setAllowsTypeSelect(BOOL)</code><br>
     * <i>native declaration : :270</i>
     */
    void setAllowsTypeSelect(boolean value);

    /**
     * Gets and sets the current selection highlight style. Defaults to NSTableViewSelectionHighlightStyleRegular.<br>
     * Original signature : <code>selectionHighlightStyle()</code><br>
     * <i>native declaration : :288</i>
     */
    NSObject selectionHighlightStyle();

    /**
     * <i>native declaration : :289</i><br>
     * Conversion Error : /// Original signature : <code>void setSelectionHighlightStyle(null)</code><br>
     * - (void)setSelectionHighlightStyle:(null)selectionHighlightStyle; (Argument selectionHighlightStyle cannot be converted)
     */
    void setSelectionHighlightStyle(int selectionHighlightStyle);
    /**
     * <i>native declaration : :295</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :297</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :302</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :305</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :307</i><br>
     * Conversion Error : /// Original signature : <code>NSInteger columnAtPoint(null)</code><br>
     * - (NSInteger)columnAtPoint:(null)point; (Argument point cannot be converted)
     */
    int columnAtPoint(NSPoint point);

    /**
     * <i>native declaration : :309</i><br>
     * Conversion Error : /// Original signature : <code>NSInteger rowAtPoint(null)</code><br>
     * - (NSInteger)rowAtPoint:(null)point; (Argument point cannot be converted)
     */
    int rowAtPoint(NSPoint point);
    /**
     * <i>native declaration : :313</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Returns the fully prepared cell that the view will normally use for drawing or any processing. The value for the cell will be correctly set, and the delegate method 'willDisplayCell:' will have be called. You can override this method to do any additional setting up of the cell that is required, or call it to retrieve a cell that will have its contents properly set for the particular column and row.<br>
     * Original signature : <code>NSCell* preparedCellAtColumn(NSInteger, NSInteger)</code><br>
     * <i>native declaration : :319</i>
     */
    com.sun.jna.Pointer preparedCellAtColumn_row(int column, int row);

    /**
     * Text delegate methods<br>
     * Original signature : <code>BOOL textShouldBeginEditing(NSText*)</code><br>
     * <i>native declaration : :326</i>
     */
    boolean textShouldBeginEditing(NSText textObject);

    /**
     * Original signature : <code>BOOL textShouldEndEditing(NSText*)</code><br>
     * <i>native declaration : :327</i>
     */
    boolean textShouldEndEditing(NSText textObject);

    /**
     * Original signature : <code>void textDidBeginEditing(NSNotification*)</code><br>
     * <i>native declaration : :328</i>
     */
    void textDidBeginEditing(NSNotification notification);

    /**
     * Original signature : <code>void textDidEndEditing(NSNotification*)</code><br>
     * <i>native declaration : :329</i>
     */
    void textDidEndEditing(NSNotification notification);

    /**
     * Original signature : <code>void textDidChange(NSNotification*)</code><br>
     * <i>native declaration : :330</i>
     */
    void textDidChange(NSNotification notification);

    /**
     * Persistence methods<br>
     * Original signature : <code>void setAutosaveName(NSString*)</code><br>
     * <i>native declaration : :335</i>
     */
    void setAutosaveName(String name);

    /**
     * Original signature : <code>NSString* autosaveName()</code><br>
     * <i>native declaration : :336</i>
     */
    String autosaveName();

    /**
     * On Mac OS 10.4 and higher, the NSTableColumn width and location is saved. On Mac OS 10.5 and higher, the NSTableColumn 'isHidden' state is also saved. The 'autosaveName' must be set for 'autosaveTableColumns' to take effect.<br>
     * Original signature : <code>void setAutosaveTableColumns(BOOL)</code><br>
     * <i>native declaration : :340</i>
     */
    void setAutosaveTableColumns(boolean save);

    /**
     * Original signature : <code>BOOL autosaveTableColumns()</code><br>
     * <i>native declaration : :341</i>
     */
    boolean autosaveTableColumns();

    /**
     * For subclassers<br>
     * Original signature : <code>void editColumn(NSInteger, NSInteger, NSEvent*, BOOL)</code><br>
     * <i>native declaration : :346</i>
     */
    void editColumn_row_withEvent_select(int column, int row, NSEvent theEvent, boolean select);
    /**
     * <i>native declaration : :347</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :348</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :349</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :351</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Deprecated in Mac OS 10.3.  Calls setGridStyleMask:, setting grid style to either None, or vertical and horizonal solid grid lines as appropriate.<br>
     * Original signature : <code>void setDrawsGrid(BOOL)</code><br>
     * <i>from NSDeprecated native declaration : :506</i>
     */
    void setDrawsGrid(boolean flag);

    /**
     * Deprecated in Mac OS 10.3.  Returns YES if gridStyleMask returns anything other than NSTableViewGridNone.<br>
     * Original signature : <code>BOOL drawsGrid()</code><br>
     * <i>from NSDeprecated native declaration : :508</i>
     */
    boolean drawsGrid();

    /**
     * Deprecated in Mac OS 10.3.  You should use selectColumnIndexes:byExtendingSelection: instead.  See that method for more details.<br>
     * Original signature : <code>void selectColumn(NSInteger, BOOL)</code><br>
     * <i>from NSDeprecated native declaration : :511</i>
     */
    void selectColumn_byExtendingSelection(int column, boolean extend);

    /**
     * Deprecated in Mac OS 10.3.  You should use selectRowIndexes:byExtendingSelection: instead.  See that method for more details.<br>
     * Original signature : <code>void selectRow(NSInteger, BOOL)</code><br>
     * <i>from NSDeprecated native declaration : :513</i>
     */
    void selectRow_byExtendingSelection(int row, boolean extend);

    /**
     * Deprecated in Mac OS 10.3.  You should use selectedColumnIndexes instead.<br>
     * Original signature : <code>NSEnumerator* selectedColumnEnumerator()</code><br>
     * <i>from NSDeprecated native declaration : :515</i>
     */
    NSEnumerator selectedColumnEnumerator();

    /**
     * Deprecated in Mac OS 10.3.  You should use selectedRowIndexes instead.<br>
     * Original signature : <code>NSEnumerator* selectedRowEnumerator()</code><br>
     * <i>from NSDeprecated native declaration : :517</i>
     */
    NSEnumerator selectedRowEnumerator();
    /**
     * <i>from NSDeprecated native declaration : :520</i><br>
     * Conversion Error : /**<br>
     *  * Deprecated in Mac OS 10.4.  You should use / override dragImageForRowsWithIndexes:tableColumns:event:dragImageOffset: instead.<br>
     *  * Original signature : <code>NSImage* dragImageForRows(NSArray*, NSEvent*, null)</code><br>
     *  * /<br>
     * - (NSImage*)dragImageForRows:(NSArray*)dragRows event:(NSEvent*)dragEvent dragImageOffset:(null)dragImageOffset; (Argument dragImageOffset cannot be converted)
     */
    /**
     * Deprecated in Mac OS 10.4.  You should use setColumnAutoresizingStyle: instead.  To preserve compatibility, if flag is YES, This method calls setColumnAutoresizingStyle:NSTableViewUniformColumnAutoresizingStyle.  If flag is NO, this method calls setColumnAutoresizingStyle:NSTableViewLastColumnOnlyAutoresizingStyle.<br>
     * Original signature : <code>void setAutoresizesAllColumnsToFit(BOOL)</code><br>
     * <i>from NSDeprecated native declaration : :523</i>
     */
    void setAutoresizesAllColumnsToFit(boolean flag);

    /**
     * Original signature : <code>BOOL autoresizesAllColumnsToFit()</code><br>
     * <i>from NSDeprecated native declaration : :524</i>
     */
    boolean autoresizesAllColumnsToFit();
    /**
     * <i>from NSDeprecated native declaration : :528</i><br>
     * Conversion Error : NSRect
     */
}
