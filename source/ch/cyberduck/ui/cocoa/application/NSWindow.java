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

import org.rococoa.cocoa.NSPoint;
import org.rococoa.cocoa.NSRect;
import org.rococoa.cocoa.NSSize;

/// <i>native declaration : :119</i>
public interface NSWindow extends NSObject {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSWindow", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        /**
         * <i>native declaration : :217</i><br>
         * Conversion Error : NSRect
         */
        /**
         * <i>native declaration : :218</i><br>
         * Conversion Error : NSRect
         */
        /**
         * Original signature : <code>CGFloat minFrameWidthWithTitle(NSString*, NSUInteger)</code><br>
         * <i>native declaration : :219</i>
         */
        float minFrameWidthWithTitle_styleMask(String aTitle, int aStyle);

        /**
         * Original signature : <code>defaultDepthLimit()</code><br>
         * <i>native declaration : :220</i>
         */
        NSObject defaultDepthLimit();

        /**
         * Original signature : <code>void removeFrameUsingName(NSString*)</code><br>
         * <i>native declaration : :473</i>
         */
        void removeFrameUsingName(String name);

        /**
         * Original signature : <code>void menuChanged(NSMenu*)</code><br>
         * <i>native declaration : :504</i>
         */
        void menuChanged(NSMenu menu);

        /**
         * Original signature : <code>NSButton* standardWindowButton(NSWindowButton, NSUInteger)</code><br>
         * <i>native declaration : :513</i>
         */
        NSButton standardWindowButton_forStyleMask(int b, int styleMask);

        NSWindow alloc();
    }
    /**
     * <i>native declaration : :223</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :224</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :227</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :228</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>NSString* title()</code><br>
     * <i>native declaration : :230</i>
     */
    String title();

    /**
     * Original signature : <code>void setTitle(NSString*)</code><br>
     * <i>native declaration : :231</i>
     */
    void setTitle(String aString);

    /**
     * setRepresentedURL:<br>
     * If url is not nil and its path is not empty, the window will show a document icon in the titlebar.  <br>
     * If the url represents a filename or other resource with a known icon, that icon will be used as the document icon.  Otherwise the default document icon will be used.  The icon can be customized using [[NSWindow standardWindowButton:NSWindowDocumentIconButton] setImage:customImage].  If url is not nil and its path is not empty, the window will have a pop-up menu which can be shown via command-click on the area containing the document icon and title.  By default, this menu will display the path components of the url.  The presence and contents of this menu can be controlled by the delegate method window:shouldPopUpDocumentPathMenu:If the url is nil or has an empty path, the window will not show a document icon and will not have a pop-up menu available via command-click.<br>
     * Original signature : <code>void setRepresentedURL(NSURL*)</code><br>
     * <i>native declaration : :237</i>
     */
    void setRepresentedURL(NSURL url);

    /**
     * Original signature : <code>NSURL* representedURL()</code><br>
     * <i>native declaration : :238</i>
     */
    NSURL representedURL();

    /**
     * Original signature : <code>NSString* representedFilename()</code><br>
     * <i>native declaration : :240</i>
     */
    String representedFilename();

    /**
     * Original signature : <code>void setRepresentedFilename(NSString*)</code><br>
     * <i>native declaration : :241</i>
     */
    void setRepresentedFilename(String aString);

    /**
     * Original signature : <code>void setTitleWithRepresentedFilename(NSString*)</code><br>
     * <i>native declaration : :242</i>
     */
    void setTitleWithRepresentedFilename(String filename);

    /**
     * Original signature : <code>void setExcludedFromWindowsMenu(BOOL)</code><br>
     * <i>native declaration : :243</i>
     */
    void setExcludedFromWindowsMenu(boolean flag);

    /**
     * Original signature : <code>BOOL isExcludedFromWindowsMenu()</code><br>
     * <i>native declaration : :244</i>
     */
    boolean isExcludedFromWindowsMenu();

    /**
     * Original signature : <code>void setContentView(NSView*)</code><br>
     * <i>native declaration : :245</i>
     */
    void setContentView(NSView aView);

    /**
     * Original signature : <code>id contentView()</code><br>
     * <i>native declaration : :246</i>
     */
    NSObject contentView();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :247</i>
     */
    void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :248</i>
     */
    org.rococoa.ID delegate();

    /**
     * Original signature : <code>NSInteger windowNumber()</code><br>
     * <i>native declaration : :249</i>
     */
    int windowNumber();

    /**
     * Original signature : <code>NSUInteger styleMask()</code><br>
     * <i>native declaration : :250</i>
     */
    int styleMask();

    /**
     * Original signature : <code>NSText* fieldEditor(BOOL, id)</code><br>
     * <i>native declaration : :251</i>
     */
    com.sun.jna.Pointer fieldEditor_forObject(boolean createFlag, NSObject anObject);

    /**
     * Original signature : <code>void endEditingFor(id)</code><br>
     * <i>native declaration : :252</i>
     */
    void endEditingFor(NSObject anObject);
    /**
     * <i>native declaration : :254</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :255</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :256</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :257</i><br>
     * Conversion Error : /// Original signature : <code>void setFrameOrigin(null)</code><br>
     * - (void)setFrameOrigin:(null)aPoint; (Argument aPoint cannot be converted)
     */
    void setFrameOrigin(NSPoint aPoint);

    /**
     * <i>native declaration : :258</i><br>
     * Conversion Error : /// Original signature : <code>void setFrameTopLeftPoint(null)</code><br>
     * - (void)setFrameTopLeftPoint:(null)aPoint; (Argument aPoint cannot be converted)
     */
    void setFrameTopLeftPoint(NSPoint aPoint);

    /**
     * <i>native declaration : :259</i><br>
     * Conversion Error : /// Original signature : <code>cascadeTopLeftFromPoint(null)</code><br>
     * - (null)cascadeTopLeftFromPoint:(null)topLeftPoint; (Argument topLeftPoint cannot be converted)
     */
    NSPoint cascadeTopLeftFromPoint(NSPoint topLeftPoint);

    /**
     * <i>native declaration : :260</i><br>
     * Conversion Error : NSRect
     */
    NSRect frame();
    /**
     * <i>native declaration : :265</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :268</i><br>
     * Conversion Error : NSRect
     */
    /**
     * show/hide resize corner (does not affect whether window is resizable)<br>
     * Original signature : <code>void setShowsResizeIndicator(BOOL)</code><br>
     * <i>native declaration : :271</i>
     */
    void setShowsResizeIndicator(boolean show);

    /**
     * Original signature : <code>BOOL showsResizeIndicator()</code><br>
     * <i>native declaration : :272</i>
     */
    boolean showsResizeIndicator();
    /**
     * <i>native declaration : :274</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :275</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :276</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :277</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :280</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :281</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :282</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :283</i><br>
     * Conversion Error : NSSize
     */
    /**
     * Original signature : <code>void useOptimizedDrawing(BOOL)</code><br>
     * <i>native declaration : :286</i>
     */
    void useOptimizedDrawing(boolean flag);

    /**
     * Original signature : <code>void disableFlushWindow()</code><br>
     * <i>native declaration : :287</i>
     */
    void disableFlushWindow();

    /**
     * Original signature : <code>void enableFlushWindow()</code><br>
     * <i>native declaration : :288</i>
     */
    void enableFlushWindow();

    /**
     * Original signature : <code>BOOL isFlushWindowDisabled()</code><br>
     * <i>native declaration : :289</i>
     */
    boolean isFlushWindowDisabled();

    /**
     * Original signature : <code>void flushWindow()</code><br>
     * <i>native declaration : :290</i>
     */
    void flushWindow();

    /**
     * Original signature : <code>void flushWindowIfNeeded()</code><br>
     * <i>native declaration : :291</i>
     */
    void flushWindowIfNeeded();

    /**
     * Original signature : <code>void setViewsNeedDisplay(BOOL)</code><br>
     * <i>native declaration : :292</i>
     */
    void setViewsNeedDisplay(boolean flag);

    /**
     * Original signature : <code>BOOL viewsNeedDisplay()</code><br>
     * <i>native declaration : :293</i>
     */
    boolean viewsNeedDisplay();

    /**
     * Original signature : <code>void displayIfNeeded()</code><br>
     * <i>native declaration : :294</i>
     */
    void displayIfNeeded();

    /**
     * Original signature : <code>void display()</code><br>
     * <i>native declaration : :295</i>
     */
    void display();

    /**
     * Original signature : <code>void setAutodisplay(BOOL)</code><br>
     * <i>native declaration : :296</i>
     */
    void setAutodisplay(boolean flag);

    /**
     * Original signature : <code>BOOL isAutodisplay()</code><br>
     * <i>native declaration : :297</i>
     */
    boolean isAutodisplay();

    /**
     * Original signature : <code>BOOL preservesContentDuringLiveResize()</code><br>
     * <i>native declaration : :300</i>
     */
    boolean preservesContentDuringLiveResize();

    /**
     * Original signature : <code>void setPreservesContentDuringLiveResize(BOOL)</code><br>
     * <i>native declaration : :301</i>
     */
    void setPreservesContentDuringLiveResize(boolean flag);

    /**
     * Original signature : <code>void update()</code><br>
     * <i>native declaration : :304</i>
     */
    void update();

    /**
     * Original signature : <code>BOOL makeFirstResponder(NSResponder*)</code><br>
     * <i>native declaration : :305</i>
     */
    boolean makeFirstResponder(NSResponder aResponder);

    /**
     * Original signature : <code>NSResponder* firstResponder()</code><br>
     * <i>native declaration : :306</i>
     */
    NSResponder firstResponder();

    /**
     * Original signature : <code>NSInteger resizeFlags()</code><br>
     * <i>native declaration : :307</i>
     */
    int resizeFlags();

    /**
     * Original signature : <code>void keyDown(NSEvent*)</code><br>
     * <i>native declaration : :308</i>
     */
    void keyDown(NSEvent event);

    /**
     * Original signature : <code>void close()</code><br>
     * <i>native declaration : :309</i>
     */
    void close();

    /**
     * Original signature : <code>void setReleasedWhenClosed(BOOL)</code><br>
     * <i>native declaration : :310</i>
     */
    void setReleasedWhenClosed(boolean flag);

    /**
     * Original signature : <code>BOOL isReleasedWhenClosed()</code><br>
     * <i>native declaration : :311</i>
     */
    boolean isReleasedWhenClosed();

    /**
     * Original signature : <code>void miniaturize(id)</code><br>
     * <i>native declaration : :312</i>
     */
    void miniaturize(NSObject sender);

    /**
     * Original signature : <code>void deminiaturize(id)</code><br>
     * <i>native declaration : :313</i>
     */
    void deminiaturize(NSObject sender);

    /**
     * Original signature : <code>BOOL isZoomed()</code><br>
     * <i>native declaration : :314</i>
     */
    boolean isZoomed();

    /**
     * Original signature : <code>void zoom(id)</code><br>
     * <i>native declaration : :315</i>
     */
    void zoom(NSObject sender);

    /**
     * Original signature : <code>BOOL isMiniaturized()</code><br>
     * <i>native declaration : :316</i>
     */
    boolean isMiniaturized();
    /**
     * <i>native declaration : :317</i><br>
     * Conversion Error : /// Original signature : <code>BOOL tryToPerform(null, id)</code><br>
     * - (BOOL)tryToPerform:(null)anAction with:(id)anObject; (Argument anAction cannot be converted)
     */
    /**
     * Original signature : <code>id validRequestorForSendType(NSString*, NSString*)</code><br>
     * <i>native declaration : :318</i>
     */
    NSObject validRequestorForSendType_returnType(com.sun.jna.Pointer sendType, com.sun.jna.Pointer returnType);

    /**
     * Original signature : <code>void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : :319</i>
     */
    void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : :320</i>
     */
    NSColor backgroundColor();
    /**
     * <i>native declaration : :323</i><br>
     * Conversion Error : /// Original signature : <code>void setContentBorderThickness(CGFloat, null)</code><br>
     * - (void)setContentBorderThickness:(CGFloat)thickness forEdge:(null)edge; (Argument edge cannot be converted)
     */
    /**
     * <i>native declaration : :324</i><br>
     * Conversion Error : /// Original signature : <code>CGFloat contentBorderThicknessForEdge(null)</code><br>
     * - (CGFloat)contentBorderThicknessForEdge:(null)edge; (Argument edge cannot be converted)
     */
    /**
     * <i>native declaration : :326</i><br>
     * Conversion Error : /// Original signature : <code>void setAutorecalculatesContentBorderThickness(BOOL, null)</code><br>
     * - (void)setAutorecalculatesContentBorderThickness:(BOOL)flag forEdge:(null)edge; (Argument edge cannot be converted)
     */
    /**
     * <i>native declaration : :327</i><br>
     * Conversion Error : /// Original signature : <code>BOOL autorecalculatesContentBorderThicknessForEdge(null)</code><br>
     * - (BOOL)autorecalculatesContentBorderThicknessForEdge:(null)edge; (Argument edge cannot be converted)
     */
    /**
     * Original signature : <code>void setMovableByWindowBackground(BOOL)</code><br>
     * <i>native declaration : :331</i>
     */
    void setMovableByWindowBackground(boolean flag);

    /**
     * Original signature : <code>BOOL isMovableByWindowBackground()</code><br>
     * <i>native declaration : :332</i>
     */
    boolean isMovableByWindowBackground();

    /**
     * Original signature : <code>void setHidesOnDeactivate(BOOL)</code><br>
     * <i>native declaration : :335</i>
     */
    void setHidesOnDeactivate(boolean flag);

    /**
     * Original signature : <code>BOOL hidesOnDeactivate()</code><br>
     * <i>native declaration : :336</i>
     */
    boolean hidesOnDeactivate();

    /**
     * indicate whether a window can be hidden during -[NSApplication hide:].  Default is YES<br>
     * Original signature : <code>void setCanHide(BOOL)</code><br>
     * <i>native declaration : :339</i>
     */
    void setCanHide(boolean flag);

    /**
     * Original signature : <code>BOOL canHide()</code><br>
     * <i>native declaration : :340</i>
     */
    boolean canHide();

    /**
     * Original signature : <code>void center()</code><br>
     * <i>native declaration : :342</i>
     */
    void center();

    /**
     * Original signature : <code>void makeKeyAndOrderFront(id)</code><br>
     * <i>native declaration : :343</i>
     */
    void makeKeyAndOrderFront(NSObject sender);

    /**
     * Original signature : <code>void orderFront(id)</code><br>
     * <i>native declaration : :344</i>
     */
    void orderFront(NSObject sender);

    /**
     * Original signature : <code>void orderBack(id)</code><br>
     * <i>native declaration : :345</i>
     */
    void orderBack(NSObject sender);

    /**
     * Original signature : <code>void orderOut(id)</code><br>
     * <i>native declaration : :346</i>
     */
    void orderOut(NSObject sender);
    /**
     * <i>native declaration : :347</i><br>
     * Conversion Error : /// Original signature : <code>void orderWindow(null, NSInteger)</code><br>
     * - (void)orderWindow:(null)place relativeTo:(NSInteger)otherWin; (Argument place cannot be converted)
     */
    /**
     * Original signature : <code>void orderFrontRegardless()</code><br>
     * <i>native declaration : :348</i>
     */
    void orderFrontRegardless();

    /**
     * Original signature : <code>void setMiniwindowImage(NSImage*)</code><br>
     * <i>native declaration : :350</i>
     */
    void setMiniwindowImage(NSImage image);

    /**
     * Original signature : <code>void setMiniwindowTitle(NSString*)</code><br>
     * <i>native declaration : :351</i>
     */
    void setMiniwindowTitle(String title);

    /**
     * Original signature : <code>NSImage* miniwindowImage()</code><br>
     * <i>native declaration : :352</i>
     */
    NSImage miniwindowImage();

    /**
     * Original signature : <code>NSString* miniwindowTitle()</code><br>
     * <i>native declaration : :353</i>
     */
    String miniwindowTitle();

    /**
     * Original signature : <code>NSDockTile* dockTile()</code><br>
     * <i>native declaration : :356</i>
     */
    com.sun.jna.Pointer dockTile();

    /**
     * Original signature : <code>void setDocumentEdited(BOOL)</code><br>
     * <i>native declaration : :359</i>
     */
    void setDocumentEdited(boolean flag);

    /**
     * Original signature : <code>BOOL isDocumentEdited()</code><br>
     * <i>native declaration : :360</i>
     */
    boolean isDocumentEdited();

    /**
     * Original signature : <code>BOOL isVisible()</code><br>
     * <i>native declaration : :361</i>
     */
    boolean isVisible();

    /**
     * Original signature : <code>BOOL isKeyWindow()</code><br>
     * <i>native declaration : :362</i>
     */
    boolean isKeyWindow();

    /**
     * Original signature : <code>BOOL isMainWindow()</code><br>
     * <i>native declaration : :363</i>
     */
    boolean isMainWindow();

    /**
     * Original signature : <code>BOOL canBecomeKeyWindow()</code><br>
     * <i>native declaration : :364</i>
     */
    boolean canBecomeKeyWindow();

    /**
     * Original signature : <code>BOOL canBecomeMainWindow()</code><br>
     * <i>native declaration : :365</i>
     */
    boolean canBecomeMainWindow();

    /**
     * Original signature : <code>void makeKeyWindow()</code><br>
     * <i>native declaration : :366</i>
     */
    void makeKeyWindow();

    /**
     * Original signature : <code>void makeMainWindow()</code><br>
     * <i>native declaration : :367</i>
     */
    void makeMainWindow();

    /**
     * Original signature : <code>void becomeKeyWindow()</code><br>
     * <i>native declaration : :368</i>
     */
    void becomeKeyWindow();

    /**
     * Original signature : <code>void resignKeyWindow()</code><br>
     * <i>native declaration : :369</i>
     */
    void resignKeyWindow();

    /**
     * Original signature : <code>void becomeMainWindow()</code><br>
     * <i>native declaration : :370</i>
     */
    void becomeMainWindow();

    /**
     * Original signature : <code>void resignMainWindow()</code><br>
     * <i>native declaration : :371</i>
     */
    void resignMainWindow();

    /**
     * Original signature : <code>BOOL worksWhenModal()</code><br>
     * <i>native declaration : :373</i>
     */
    boolean worksWhenModal();
    /**
     * <i>native declaration : :374</i><br>
     * Conversion Error : /// Original signature : <code>convertBaseToScreen(null)</code><br>
     * - (null)convertBaseToScreen:(null)aPoint; (Argument aPoint cannot be converted)
     */
    /**
     * <i>native declaration : :375</i><br>
     * Conversion Error : /// Original signature : <code>convertScreenToBase(null)</code><br>
     * - (null)convertScreenToBase:(null)aPoint; (Argument aPoint cannot be converted)
     */
    /**
     * Original signature : <code>void performClose(id)</code><br>
     * <i>native declaration : :376</i>
     */
    void performClose(NSObject sender);

    /**
     * Original signature : <code>void performMiniaturize(id)</code><br>
     * <i>native declaration : :377</i>
     */
    void performMiniaturize(NSObject sender);

    /**
     * Original signature : <code>void performZoom(id)</code><br>
     * <i>native declaration : :378</i>
     */
    void performZoom(NSObject sender);

    /**
     * Original signature : <code>NSInteger gState()</code><br>
     * <i>native declaration : :379</i>
     */
    int gState();

    /**
     * Original signature : <code>void setOneShot(BOOL)</code><br>
     * <i>native declaration : :380</i>
     */
    void setOneShot(boolean flag);

    /**
     * Original signature : <code>BOOL isOneShot()</code><br>
     * <i>native declaration : :381</i>
     */
    boolean isOneShot();
    /**
     * <i>native declaration : :382</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :383</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void print(id)</code><br>
     * <i>native declaration : :384</i>
     */
    void print(NSObject sender);

    /**
     * Original signature : <code>void disableCursorRects()</code><br>
     * <i>native declaration : :386</i>
     */
    void disableCursorRects();

    /**
     * Original signature : <code>void enableCursorRects()</code><br>
     * <i>native declaration : :387</i>
     */
    void enableCursorRects();

    /**
     * Original signature : <code>void discardCursorRects()</code><br>
     * <i>native declaration : :388</i>
     */
    void discardCursorRects();

    /**
     * Original signature : <code>BOOL areCursorRectsEnabled()</code><br>
     * <i>native declaration : :389</i>
     */
    boolean areCursorRectsEnabled();

    /**
     * Original signature : <code>void invalidateCursorRectsForView(NSView*)</code><br>
     * <i>native declaration : :390</i>
     */
    void invalidateCursorRectsForView(com.sun.jna.Pointer aView);

    /**
     * Original signature : <code>void resetCursorRects()</code><br>
     * <i>native declaration : :391</i>
     */
    void resetCursorRects();

    /**
     * Original signature : <code>void setAllowsToolTipsWhenApplicationIsInactive(BOOL)</code><br>
     * <i>native declaration : :394</i>
     */
    void setAllowsToolTipsWhenApplicationIsInactive(boolean allowWhenInactive);

    /**
     * Original signature : <code>BOOL allowsToolTipsWhenApplicationIsInactive()</code><br>
     * <i>native declaration : :397</i>
     */
    boolean allowsToolTipsWhenApplicationIsInactive();
    /**
     * <i>native declaration : :401</i><br>
     * Conversion Error : /// Original signature : <code>void setBackingType(null)</code><br>
     * - (void)setBackingType:(null)bufferingType; (Argument bufferingType cannot be converted)
     */
    /**
     * Original signature : <code>backingType()</code><br>
     * <i>native declaration : :402</i>
     */
    NSObject backingType();

    /**
     * Original signature : <code>void setLevel(NSInteger)</code><br>
     * <i>native declaration : :403</i>
     */
    void setLevel(int newLevel);

    /**
     * Original signature : <code>NSInteger level()</code><br>
     * <i>native declaration : :404</i>
     */
    int level();
    /**
     * <i>native declaration : :405</i><br>
     * Conversion Error : /// Original signature : <code>void setDepthLimit(null)</code><br>
     * - (void)setDepthLimit:(null)limit; (Argument limit cannot be converted)
     */
    /**
     * Original signature : <code>depthLimit()</code><br>
     * <i>native declaration : :406</i>
     */
    NSObject depthLimit();

    /**
     * Original signature : <code>void setDynamicDepthLimit(BOOL)</code><br>
     * <i>native declaration : :407</i>
     */
    void setDynamicDepthLimit(boolean flag);

    /**
     * Original signature : <code>BOOL hasDynamicDepthLimit()</code><br>
     * <i>native declaration : :408</i>
     */
    boolean hasDynamicDepthLimit();

    /**
     * Original signature : <code>NSScreen* screen()</code><br>
     * <i>native declaration : :409</i>
     */
    com.sun.jna.Pointer screen();

    /**
     * Original signature : <code>NSScreen* deepestScreen()</code><br>
     * <i>native declaration : :410</i>
     */
    com.sun.jna.Pointer deepestScreen();

    /**
     * Original signature : <code>BOOL canStoreColor()</code><br>
     * <i>native declaration : :411</i>
     */
    boolean canStoreColor();

    /**
     * Original signature : <code>void setHasShadow(BOOL)</code><br>
     * <i>native declaration : :412</i>
     */
    void setHasShadow(boolean hasShadow);

    /**
     * Original signature : <code>BOOL hasShadow()</code><br>
     * <i>native declaration : :413</i>
     */
    boolean hasShadow();

    /**
     * Original signature : <code>void invalidateShadow()</code><br>
     * <i>native declaration : :415</i>
     */
    void invalidateShadow();

    /**
     * Original signature : <code>void setAlphaValue(CGFloat)</code><br>
     * <i>native declaration : :417</i>
     */
    void setAlphaValue(float windowAlpha);

    /**
     * Original signature : <code>CGFloat alphaValue()</code><br>
     * <i>native declaration : :418</i>
     */
    float alphaValue();

    /**
     * Original signature : <code>void setOpaque(BOOL)</code><br>
     * <i>native declaration : :419</i>
     */
    void setOpaque(boolean isOpaque);

    /**
     * Original signature : <code>BOOL isOpaque()</code><br>
     * <i>native declaration : :420</i>
     */
    boolean isOpaque();

    /**
     * -setSharingType: specifies whether the window content can be read and/or written from another process.  The default sharing type is NSWindowSharingReadOnly, which means other processes can read the window content (eg. for window capture) but cannot modify it.  If you set your window sharing type to NSWindowSharingNone, so that the content cannot be captured, your window will also not be able to participate in a number of system services, so this setting should be used with caution.  If you set your window sharing type to NSWindowSharingReadWrite, other processes can both read and modify the window content.<br>
     * Original signature : <code>void setSharingType(NSWindowSharingType)</code><br>
     * <i>native declaration : :426</i>
     */
    void setSharingType(int type);

    /**
     * Original signature : <code>NSWindowSharingType sharingType()</code><br>
     * <i>native declaration : :427</i>
     */
    int sharingType();

    /**
     * -setPreferredBackingLocation: sets the preferred location for the window backing store.  In general, you should not use this API unless indicated by performance measurement.<br>
     * Original signature : <code>void setPreferredBackingLocation(NSWindowBackingLocation)</code><br>
     * <i>native declaration : :431</i>
     */
    void setPreferredBackingLocation(int backingLocation);

    /**
     * -preferredBackingLocation gets the preferred location for the window backing store.  This may be different from the actual location.<br>
     * Original signature : <code>NSWindowBackingLocation preferredBackingLocation()</code><br>
     * <i>native declaration : :434</i>
     */
    int preferredBackingLocation();

    /**
     * -backingLocation gets the current location of the window backing store.<br>
     * Original signature : <code>NSWindowBackingLocation backingLocation()</code><br>
     * <i>native declaration : :437</i>
     */
    int backingLocation();

    /**
     * Original signature : <code>BOOL displaysWhenScreenProfileChanges()</code><br>
     * <i>native declaration : :442</i>
     */
    boolean displaysWhenScreenProfileChanges();

    /**
     * Original signature : <code>void setDisplaysWhenScreenProfileChanges(BOOL)</code><br>
     * <i>native declaration : :443</i>
     */
    void setDisplaysWhenScreenProfileChanges(boolean flag);

    /**
     * Original signature : <code>void disableScreenUpdatesUntilFlush()</code><br>
     * <i>native declaration : :445</i>
     */
    void disableScreenUpdatesUntilFlush();

    /**
     * This API controls whether the receiver is permitted onscreen before the user has logged in.  This property is off by default.  Alert panels and windows presented by input managers are examples of windows which should have this property set.<br>
     * Original signature : <code>BOOL canBecomeVisibleWithoutLogin()</code><br>
     * <i>native declaration : :451</i>
     */
    boolean canBecomeVisibleWithoutLogin();

    /**
     * Original signature : <code>void setCanBecomeVisibleWithoutLogin(BOOL)</code><br>
     * <i>native declaration : :452</i>
     */
    void setCanBecomeVisibleWithoutLogin(boolean flag);

    /**
     * Original signature : <code>void setCollectionBehavior(NSWindowCollectionBehavior)</code><br>
     * <i>native declaration : :455</i>
     */
    void setCollectionBehavior(int behavior);

    /**
     * Original signature : <code>NSWindowCollectionBehavior collectionBehavior()</code><br>
     * <i>native declaration : :456</i>
     */
    int collectionBehavior();

    /**
     * -setCanBeVisibleOnAllSpaces: controls whether a window can be visible on all spaces (YES) or is associated with one space at a time (NO).  The default setting is NO.<br>
     * Original signature : <code>BOOL canBeVisibleOnAllSpaces()</code><br>
     * <i>native declaration : :462</i>
     */
    boolean canBeVisibleOnAllSpaces();

    /**
     * Original signature : <code>void setCanBeVisibleOnAllSpaces(BOOL)</code><br>
     * <i>native declaration : :463</i>
     */
    void setCanBeVisibleOnAllSpaces(boolean flag);

    /**
     * Original signature : <code>NSString* stringWithSavedFrame()</code><br>
     * <i>native declaration : :465</i>
     */
    String stringWithSavedFrame();

    /**
     * Original signature : <code>void setFrameFromString(NSString*)</code><br>
     * <i>native declaration : :466</i>
     */
    void setFrameFromString(String string);

    /**
     * Original signature : <code>void saveFrameUsingName(NSString*)</code><br>
     * <i>native declaration : :467</i>
     */
    void saveFrameUsingName(String name);

    /**
     * Set force=YES to use setFrameUsingName on a non-resizable window<br>
     * Original signature : <code>BOOL setFrameUsingName(NSString*, BOOL)</code><br>
     * <i>native declaration : :469</i>
     */
    boolean setFrameUsingName_force(String name, boolean force);

    /**
     * Original signature : <code>BOOL setFrameUsingName(NSString*)</code><br>
     * <i>native declaration : :470</i>
     */
    boolean setFrameUsingName(String name);

    /**
     * Original signature : <code>BOOL setFrameAutosaveName(NSString*)</code><br>
     * <i>native declaration : :471</i>
     */
    boolean setFrameAutosaveName(String name);

    /**
     * Original signature : <code>NSString* frameAutosaveName()</code><br>
     * <i>native declaration : :472</i>
     */
    String frameAutosaveName();
    /**
     * <i>native declaration : :476</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void restoreCachedImage()</code><br>
     * <i>native declaration : :477</i>
     */
    void restoreCachedImage();

    /**
     * Original signature : <code>void discardCachedImage()</code><br>
     * <i>native declaration : :478</i>
     */
    void discardCachedImage();
    /**
     * <i>native declaration : :480</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :481</i><br>
     * Conversion Error : NSSize
     */
    NSSize maxSize();

    /**
     * <i>native declaration : :482</i><br>
     * Conversion Error : NSSize
     */
    void setMinSize(NSSize size);

    /**
     * <i>native declaration : :483</i><br>
     * Conversion Error : NSSize
     */
    void setMaxSize(NSSize size);
    /**
     * <i>native declaration : :485</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :486</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :487</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :488</i><br>
     * Conversion Error : NSSize
     */
    /**
     * Original signature : <code>NSEvent* nextEventMatchingMask(NSUInteger)</code><br>
     * <i>native declaration : :490</i>
     */
    NSEvent nextEventMatchingMask(int mask);

    /**
     * Original signature : <code>NSEvent* nextEventMatchingMask(NSUInteger, NSDate*, NSString*, BOOL)</code><br>
     * <i>native declaration : :491</i>
     */
    NSEvent nextEventMatchingMask_untilDate_inMode_dequeue(int mask, NSDate expiration, String mode, boolean deqFlag);

    /**
     * Original signature : <code>void discardEventsMatchingMask(NSUInteger, NSEvent*)</code><br>
     * <i>native declaration : :492</i>
     */
    void discardEventsMatchingMask_beforeEvent(int mask, com.sun.jna.Pointer lastEvent);

    /**
     * Original signature : <code>void postEvent(NSEvent*, BOOL)</code><br>
     * <i>native declaration : :493</i>
     */
    void postEvent_atStart(NSEvent event, boolean flag);

    /**
     * Original signature : <code>NSEvent* currentEvent()</code><br>
     * <i>native declaration : :494</i>
     */
    NSEvent currentEvent();

    /**
     * Original signature : <code>void setAcceptsMouseMovedEvents(BOOL)</code><br>
     * <i>native declaration : :495</i>
     */
    void setAcceptsMouseMovedEvents(boolean flag);

    /**
     * Original signature : <code>BOOL acceptsMouseMovedEvents()</code><br>
     * <i>native declaration : :496</i>
     */
    boolean acceptsMouseMovedEvents();

    /**
     * Original signature : <code>void setIgnoresMouseEvents(BOOL)</code><br>
     * <i>native declaration : :498</i>
     */
    void setIgnoresMouseEvents(boolean flag);

    /**
     * Original signature : <code>BOOL ignoresMouseEvents()</code><br>
     * <i>native declaration : :499</i>
     */
    boolean ignoresMouseEvents();

    /**
     * Original signature : <code>NSDictionary* deviceDescription()</code><br>
     * <i>native declaration : :501</i>
     */
    NSDictionary deviceDescription();

    /**
     * Original signature : <code>void sendEvent(NSEvent*)</code><br>
     * <i>native declaration : :502</i>
     */
    void sendEvent(NSEvent theEvent);

    /**
     * Original signature : <code>mouseLocationOutsideOfEventStream()</code><br>
     * <i>native declaration : :503</i>
     */
    NSObject mouseLocationOutsideOfEventStream();

    /**
     * Original signature : <code>id windowController()</code><br>
     * <i>native declaration : :506</i>
     */
    NSObject windowController();

    /**
     * Original signature : <code>void setWindowController(NSWindowController*)</code><br>
     * <i>native declaration : :507</i>
     */
    void setWindowController(NSObject windowController);

    /**
     * Original signature : <code>BOOL isSheet()</code><br>
     * <i>native declaration : :509</i>
     */
    boolean isSheet();

    /**
     * Original signature : <code>NSWindow* attachedSheet()</code><br>
     * <i>native declaration : :510</i>
     */
    NSWindow attachedSheet();

    /**
     * Original signature : <code>NSButton* standardWindowButton(NSWindowButton)</code><br>
     * <i>native declaration : :514</i>
     */
    NSButton standardWindowButton(int b);
    /**
     * <i>native declaration : :518</i><br>
     * Conversion Error : /// Original signature : <code>void addChildWindow(NSWindow*, null)</code><br>
     * - (void)addChildWindow:(NSWindow*)childWin ordered:(null)place; (Argument place cannot be converted)
     */
    /**
     * Original signature : <code>void removeChildWindow(NSWindow*)</code><br>
     * <i>native declaration : :519</i>
     */
    void removeChildWindow(NSWindow childWin);

    /**
     * Original signature : <code>NSArray* childWindows()</code><br>
     * <i>native declaration : :520</i>
     */
    NSArray childWindows();

    /**
     * Original signature : <code>NSWindow* parentWindow()</code><br>
     * <i>native declaration : :522</i>
     */
    NSWindow parentWindow();

    /**
     * Original signature : <code>void setParentWindow(NSWindow*)</code><br>
     * <i>native declaration : :523</i>
     */
    void setParentWindow(NSWindow window);

    /**
     * Returns NSGraphicsContext used to render the receiver's content on the screen for the calling thread.<br>
     * Original signature : <code>NSGraphicsContext* graphicsContext()</code><br>
     * <i>native declaration : :529</i>
     */
    com.sun.jna.Pointer graphicsContext();

    /**
     * Returns scale factor applied to view coordinate system to get to base coordinate system of window<br>
     * Original signature : <code>CGFloat userSpaceScaleFactor()</code><br>
     * <i>native declaration : :533</i>
     */
    float userSpaceScaleFactor();

    /**
     * Original signature : <code>void setInitialFirstResponder(NSView*)</code><br>
     * <i>from NSKeyboardUI native declaration : :539</i>
     */
    void setInitialFirstResponder(NSView view);

    /**
     * Original signature : <code>NSView* initialFirstResponder()</code><br>
     * <i>from NSKeyboardUI native declaration : :540</i>
     */
    NSView initialFirstResponder();

    /**
     * Original signature : <code>void selectNextKeyView(id)</code><br>
     * <i>from NSKeyboardUI native declaration : :541</i>
     */
    void selectNextKeyView(NSObject sender);

    /**
     * Original signature : <code>void selectPreviousKeyView(id)</code><br>
     * <i>from NSKeyboardUI native declaration : :542</i>
     */
    void selectPreviousKeyView(NSObject sender);

    /**
     * Original signature : <code>void selectKeyViewFollowingView(NSView*)</code><br>
     * <i>from NSKeyboardUI native declaration : :543</i>
     */
    void selectKeyViewFollowingView(NSView aView);

    /**
     * Original signature : <code>void selectKeyViewPrecedingView(NSView*)</code><br>
     * <i>from NSKeyboardUI native declaration : :544</i>
     */
    void selectKeyViewPrecedingView(NSView aView);

    /**
     * Original signature : <code>NSSelectionDirection keyViewSelectionDirection()</code><br>
     * <i>from NSKeyboardUI native declaration : :545</i>
     */
    int keyViewSelectionDirection();

    /**
     * Original signature : <code>void setDefaultButtonCell(NSButtonCell*)</code><br>
     * <i>from NSKeyboardUI native declaration : :546</i>
     */
    void setDefaultButtonCell(NSButtonCell defButt);

    /**
     * Original signature : <code>NSButtonCell* defaultButtonCell()</code><br>
     * <i>from NSKeyboardUI native declaration : :547</i>
     */
    NSButtonCell defaultButtonCell();

    /**
     * Original signature : <code>void disableKeyEquivalentForDefaultButtonCell()</code><br>
     * <i>from NSKeyboardUI native declaration : :548</i>
     */
    void disableKeyEquivalentForDefaultButtonCell();

    /**
     * Original signature : <code>void enableKeyEquivalentForDefaultButtonCell()</code><br>
     * <i>from NSKeyboardUI native declaration : :549</i>
     */
    void enableKeyEquivalentForDefaultButtonCell();

    /**
     * Original signature : <code>void setAutorecalculatesKeyViewLoop(BOOL)</code><br>
     * <i>from NSKeyboardUI native declaration : :551</i>
     */
    void setAutorecalculatesKeyViewLoop(boolean flag);

    /**
     * Original signature : <code>BOOL autorecalculatesKeyViewLoop()</code><br>
     * <i>from NSKeyboardUI native declaration : :552</i>
     */
    boolean autorecalculatesKeyViewLoop();

    /**
     * Original signature : <code>void recalculateKeyViewLoop()</code><br>
     * <i>from NSKeyboardUI native declaration : :553</i>
     */
    void recalculateKeyViewLoop();

    /**
     * Original signature : <code>void setToolbar(NSToolbar*)</code><br>
     * <i>from NSToolbarSupport native declaration : :558</i>
     */
    void setToolbar(NSToolbar toolbar);

    /**
     * Original signature : <code>NSToolbar* toolbar()</code><br>
     * <i>from NSToolbarSupport native declaration : :559</i>
     */
    NSToolbar toolbar();

    /**
     * Original signature : <code>void toggleToolbarShown(id)</code><br>
     * <i>from NSToolbarSupport native declaration : :560</i>
     */
    void toggleToolbarShown(NSObject sender);

    /**
     * Original signature : <code>void runToolbarCustomizationPalette(id)</code><br>
     * <i>from NSToolbarSupport native declaration : :561</i>
     */
    void runToolbarCustomizationPalette(NSObject sender);

    /**
     * Original signature : <code>void setShowsToolbarButton(BOOL)</code><br>
     * <i>from NSToolbarSupport native declaration : :563</i>
     */
    void setShowsToolbarButton(boolean show);

    /**
     * Original signature : <code>BOOL showsToolbarButton()</code><br>
     * <i>from NSToolbarSupport native declaration : :564</i>
     */
    boolean showsToolbarButton();
    /**
     * <i>from NSDrag native declaration : :569</i><br>
     * Conversion Error : /// Original signature : <code>void dragImage(NSImage*, null, NSSize, NSEvent*, NSPasteboard*, id, BOOL)</code><br>
     * - (void)dragImage:(NSImage*)anImage at:(null)baseLocation offset:(NSSize)initialOffset event:(NSEvent*)event pasteboard:(NSPasteboard*)pboard source:(id)sourceObj slideBack:(BOOL)slideFlag; (Argument baseLocation cannot be converted)
     */
    /**
     * Original signature : <code>void registerForDraggedTypes(NSArray*)</code><br>
     * <i>from NSDrag native declaration : :571</i>
     */
    void registerForDraggedTypes(NSArray newTypes);

    /**
     * Original signature : <code>void unregisterDraggedTypes()</code><br>
     * <i>from NSDrag native declaration : :572</i>
     */
    void unregisterDraggedTypes();

    public static final String WindowDidBecomeKeyNotification = "NSWindowDidBecomeKeyNotification";
    public static final String WindowDidBecomeMainNotification = "NSWindowDidBecomeMainNotification";
    public static final String WindowDidChangeScreenNotification = "NSWindowDidChangeScreenNotification";
    public static final String WindowDidChangeScreenProfileNotification = "NSWindowDidChangeScreenProfileNotification";
    public static final String WindowDidDeminiaturizeNotification = "NSWindowDidDeminiaturizeNotification";
    public static final String WindowDidEndSheetNotification = "NSWindowDidEndSheetNotification";
    public static final String WindowDidExposeNotification = "NSWindowDidExposeNotification";
    public static final String WindowDidMiniaturizeNotification = "NSWindowDidMiniaturizeNotification";
    public static final String WindowDidMoveNotification = "NSWindowDidMoveNotification";
    public static final String WindowDidResignKeyNotification = "NSWindowDidResignKeyNotification";
    public static final String WindowDidResignMainNotification = "NSWindowDidResignMainNotification";
    public static final String WindowDidResizeNotification = "NSWindowDidResizeNotification";
    public static final String WindowDidUpdateNotification = "NSWindowDidUpdateNotification";
    public static final String WindowWillBeginSheetNotification = "NSWindowWillBeginSheetNotification";
    public static final String WindowWillCloseNotification = "NSWindowWillCloseNotification";
    public static final String WindowWillMiniaturizeNotification = "NSWindowWillMiniaturizeNotification";
    public static final String WindowWillMoveNotification = "NSWindowWillMoveNotification";
    public static final int Retained = 0;
    public static final int NonRetained = 1;
    public static final int Buffered = 2;
    public static final int BorderlessWindowMask = 0;
    public static final int TitledWindowMask = 1;
    public static final int ClosableWindowMask = 2;
    public static final int MiniaturizableWindowMask = 4;
    public static final int ResizableWindowMask = 8;
    public static final int TexturedBackgroundWindowMask = 256;
    public static final int UnscaledWindowMask = 2048;
    public static final int UnifiedTitleAndToolbarWindowMask = 4096;
    public static final int NormalWindowLevel = 0;
    public static final int FloatingWindowLevel = 3;
    public static final int SubmenuWindowLevel = 3;
    public static final int TornOffMenuWindowLevel = 3;
    public static final int MainMenuWindowLevel = 20;
    public static final int StatusWindowLevel = 21;
    public static final int ModalPanelWindowLevel = 100;
    public static final int PopUpMenuWindowLevel = 101;
    public static final int ScreenSaverWindowLevel = 1000;
    public static final int Above = 1;
    public static final int Below = -1;
    public static final int Out = 0;
    public static final int DirectSelection = 0;
    public static final int SelectingNext = 1;
    public static final int SelectingPrevious = 2;
    public static final int CloseButton = 0;
    public static final int MiniaturizeButton = 1;
    public static final int ZoomButton = 2;
    public static final int ToolbarButton = 3;
    public static final int DocumentIconButton = 4;
}
