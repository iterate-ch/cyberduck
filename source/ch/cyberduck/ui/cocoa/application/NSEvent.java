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
import ch.cyberduck.ui.cocoa.foundation.NSCopying;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSPoint;

public abstract class NSEvent extends NSObject implements NSCopying {

    /// <i>native declaration : :12</i>
    public static final int NSLeftMouseDown = 1;
    /// <i>native declaration : :13</i>
    public static final int NSLeftMouseUp = 2;
    /// <i>native declaration : :14</i>
    public static final int NSRightMouseDown = 3;
    /// <i>native declaration : :15</i>
    public static final int NSRightMouseUp = 4;
    /// <i>native declaration : :16</i>
    public static final int NSMouseMoved = 5;
    /// <i>native declaration : :17</i>
    public static final int NSLeftMouseDragged = 6;
    /// <i>native declaration : :18</i>
    public static final int NSRightMouseDragged = 7;
    /// <i>native declaration : :19</i>
    public static final int NSMouseEntered = 8;
    /// <i>native declaration : :20</i>
    public static final int NSMouseExited = 9;
    /// <i>native declaration : :21</i>
    public static final int NSKeyDown = 10;
    /// <i>native declaration : :22</i>
    public static final int NSKeyUp = 11;
    /// <i>native declaration : :23</i>
    public static final int NSFlagsChanged = 12;
    /// <i>native declaration : :24</i>
    public static final int NSAppKitDefined = 13;
    /// <i>native declaration : :25</i>
    public static final int NSSystemDefined = 14;
    /// <i>native declaration : :26</i>
    public static final int NSApplicationDefined = 15;
    /// <i>native declaration : :27</i>
    public static final int NSPeriodic = 16;
    /// <i>native declaration : :28</i>
    public static final int NSCursorUpdate = 17;
    /// <i>native declaration : :29</i>
    public static final int NSScrollWheel = 22;
    /// <i>native declaration : :31</i>
    public static final int NSTabletPoint = 23;
    /// <i>native declaration : :32</i>
    public static final int NSTabletProximity = 24;
    /// <i>native declaration : :34</i>
    public static final int NSOtherMouseDown = 25;
    /// <i>native declaration : :35</i>
    public static final int NSOtherMouseUp = 26;
    /// <i>native declaration : :36</i>
    public static final int NSOtherMouseDragged = 27;
    /// <i>native declaration : :41</i>
    public static final int NSLeftMouseDownMask = 1 << NSLeftMouseDown;
    /// <i>native declaration : :42</i>
    public static final int NSLeftMouseUpMask = 1 << NSLeftMouseUp;
    /// <i>native declaration : :43</i>
    public static final int NSRightMouseDownMask = 1 << NSRightMouseDown;
    /// <i>native declaration : :44</i>
    public static final int NSRightMouseUpMask = 1 << NSRightMouseUp;
    /// <i>native declaration : :45</i>
    public static final int NSMouseMovedMask = 1 << NSMouseMoved;
    /// <i>native declaration : :46</i>
    public static final int NSLeftMouseDraggedMask = 1 << NSLeftMouseDragged;
    /// <i>native declaration : :47</i>
    public static final int NSRightMouseDraggedMask = 1 << NSRightMouseDragged;
    /// <i>native declaration : :48</i>
    public static final int NSMouseEnteredMask = 1 << NSMouseEntered;
    /// <i>native declaration : :49</i>
    public static final int NSMouseExitedMask = 1 << NSMouseExited;
    /// <i>native declaration : :50</i>
    public static final int NSKeyDownMask = 1 << NSKeyDown;
    /// <i>native declaration : :51</i>
    public static final int NSKeyUpMask = 1 << NSKeyUp;
    /// <i>native declaration : :52</i>
    public static final int NSFlagsChangedMask = 1 << NSFlagsChanged;
    /// <i>native declaration : :53</i>
    public static final int NSAppKitDefinedMask = 1 << NSAppKitDefined;
    /// <i>native declaration : :54</i>
    public static final int NSSystemDefinedMask = 1 << NSSystemDefined;
    /// <i>native declaration : :55</i>
    public static final int NSApplicationDefinedMask = 1 << NSApplicationDefined;
    /// <i>native declaration : :56</i>
    public static final int NSPeriodicMask = 1 << NSPeriodic;
    /// <i>native declaration : :57</i>
    public static final int NSCursorUpdateMask = 1 << NSCursorUpdate;
    /// <i>native declaration : :58</i>
    public static final int NSScrollWheelMask = 1 << NSScrollWheel;
    /// <i>native declaration : :60</i>
    public static final int NSTabletPointMask = 1 << NSTabletPoint;
    /// <i>native declaration : :61</i>
    public static final int NSTabletProximityMask = 1 << NSTabletProximity;
    /// <i>native declaration : :63</i>
    public static final int NSOtherMouseDownMask = 1 << NSOtherMouseDown;
    /// <i>native declaration : :64</i>
    public static final int NSOtherMouseUpMask = 1 << NSOtherMouseUp;
    /// <i>native declaration : :65</i>
    public static final int NSOtherMouseDraggedMask = 1 << NSOtherMouseDragged;
    /// Failed to infer type of NSUIntegerMax
    /// Failed to infer type of NX_TABLET_POINTER_UNKNOWN
    /// Failed to infer type of NX_TABLET_POINTER_PEN
    /// Failed to infer type of NX_TABLET_POINTER_CURSOR
    /// Failed to infer type of NX_TABLET_POINTER_ERASER
    /// Failed to infer type of NX_TABLET_BUTTON_PENTIPMASK
    /// Failed to infer type of NX_TABLET_BUTTON_PENLOWERSIDEMASK
    /// Failed to infer type of NX_TABLET_BUTTON_PENUPPERSIDEMASK
    /// <i>native declaration : :313</i>
    public static final int NSUpArrowFunctionKey = 63232;
    /// <i>native declaration : :314</i>
    public static final int NSDownArrowFunctionKey = 63233;
    /// <i>native declaration : :315</i>
    public static final int NSLeftArrowFunctionKey = 63234;
    /// <i>native declaration : :316</i>
    public static final int NSRightArrowFunctionKey = 63235;
    /// <i>native declaration : :317</i>
    public static final int NSF1FunctionKey = 63236;
    /// <i>native declaration : :318</i>
    public static final int NSF2FunctionKey = 63237;
    /// <i>native declaration : :319</i>
    public static final int NSF3FunctionKey = 63238;
    /// <i>native declaration : :320</i>
    public static final int NSF4FunctionKey = 63239;
    /// <i>native declaration : :321</i>
    public static final int NSF5FunctionKey = 63240;
    /// <i>native declaration : :322</i>
    public static final int NSF6FunctionKey = 63241;
    /// <i>native declaration : :323</i>
    public static final int NSF7FunctionKey = 63242;
    /// <i>native declaration : :324</i>
    public static final int NSF8FunctionKey = 63243;
    /// <i>native declaration : :325</i>
    public static final int NSF9FunctionKey = 63244;
    /// <i>native declaration : :326</i>
    public static final int NSF10FunctionKey = 63245;
    /// <i>native declaration : :327</i>
    public static final int NSF11FunctionKey = 63246;
    /// <i>native declaration : :328</i>
    public static final int NSF12FunctionKey = 63247;
    /// <i>native declaration : :329</i>
    public static final int NSF13FunctionKey = 63248;
    /// <i>native declaration : :330</i>
    public static final int NSF14FunctionKey = 63249;
    /// <i>native declaration : :331</i>
    public static final int NSF15FunctionKey = 63250;
    /// <i>native declaration : :332</i>
    public static final int NSF16FunctionKey = 63251;
    /// <i>native declaration : :333</i>
    public static final int NSF17FunctionKey = 63252;
    /// <i>native declaration : :334</i>
    public static final int NSF18FunctionKey = 63253;
    /// <i>native declaration : :335</i>
    public static final int NSF19FunctionKey = 63254;
    /// <i>native declaration : :336</i>
    public static final int NSF20FunctionKey = 63255;
    /// <i>native declaration : :337</i>
    public static final int NSF21FunctionKey = 63256;
    /// <i>native declaration : :338</i>
    public static final int NSF22FunctionKey = 63257;
    /// <i>native declaration : :339</i>
    public static final int NSF23FunctionKey = 63258;
    /// <i>native declaration : :340</i>
    public static final int NSF24FunctionKey = 63259;
    /// <i>native declaration : :341</i>
    public static final int NSF25FunctionKey = 63260;
    /// <i>native declaration : :342</i>
    public static final int NSF26FunctionKey = 63261;
    /// <i>native declaration : :343</i>
    public static final int NSF27FunctionKey = 63262;
    /// <i>native declaration : :344</i>
    public static final int NSF28FunctionKey = 63263;
    /// <i>native declaration : :345</i>
    public static final int NSF29FunctionKey = 63264;
    /// <i>native declaration : :346</i>
    public static final int NSF30FunctionKey = 63265;
    /// <i>native declaration : :347</i>
    public static final int NSF31FunctionKey = 63266;
    /// <i>native declaration : :348</i>
    public static final int NSF32FunctionKey = 63267;
    /// <i>native declaration : :349</i>
    public static final int NSF33FunctionKey = 63268;
    /// <i>native declaration : :350</i>
    public static final int NSF34FunctionKey = 63269;
    /// <i>native declaration : :351</i>
    public static final int NSF35FunctionKey = 63270;
    /// <i>native declaration : :352</i>
    public static final int NSInsertFunctionKey = 63271;
    /// <i>native declaration : :353</i>
    public static final int NSDeleteFunctionKey = 63272;
    /// <i>native declaration : :354</i>
    public static final int NSHomeFunctionKey = 63273;
    /// <i>native declaration : :355</i>
    public static final int NSBeginFunctionKey = 63274;
    /// <i>native declaration : :356</i>
    public static final int NSEndFunctionKey = 63275;
    /// <i>native declaration : :357</i>
    public static final int NSPageUpFunctionKey = 63276;
    /// <i>native declaration : :358</i>
    public static final int NSPageDownFunctionKey = 63277;
    /// <i>native declaration : :359</i>
    public static final int NSPrintScreenFunctionKey = 63278;
    /// <i>native declaration : :360</i>
    public static final int NSScrollLockFunctionKey = 63279;
    /// <i>native declaration : :361</i>
    public static final int NSPauseFunctionKey = 63280;
    /// <i>native declaration : :362</i>
    public static final int NSSysReqFunctionKey = 63281;
    /// <i>native declaration : :363</i>
    public static final int NSBreakFunctionKey = 63282;
    /// <i>native declaration : :364</i>
    public static final int NSResetFunctionKey = 63283;
    /// <i>native declaration : :365</i>
    public static final int NSStopFunctionKey = 63284;
    /// <i>native declaration : :366</i>
    public static final int NSMenuFunctionKey = 63285;
    /// <i>native declaration : :367</i>
    public static final int NSUserFunctionKey = 63286;
    /// <i>native declaration : :368</i>
    public static final int NSSystemFunctionKey = 63287;
    /// <i>native declaration : :369</i>
    public static final int NSPrintFunctionKey = 63288;
    /// <i>native declaration : :370</i>
    public static final int NSClearLineFunctionKey = 63289;
    /// <i>native declaration : :371</i>
    public static final int NSClearDisplayFunctionKey = 63290;
    /// <i>native declaration : :372</i>
    public static final int NSInsertLineFunctionKey = 63291;
    /// <i>native declaration : :373</i>
    public static final int NSDeleteLineFunctionKey = 63292;
    /// <i>native declaration : :374</i>
    public static final int NSInsertCharFunctionKey = 63293;
    /// <i>native declaration : :375</i>
    public static final int NSDeleteCharFunctionKey = 63294;
    /// <i>native declaration : :376</i>
    public static final int NSPrevFunctionKey = 63295;
    /// <i>native declaration : :377</i>
    public static final int NSNextFunctionKey = 63296;
    /// <i>native declaration : :378</i>
    public static final int NSSelectFunctionKey = 63297;
    /// <i>native declaration : :379</i>
    public static final int NSExecuteFunctionKey = 63298;
    /// <i>native declaration : :380</i>
    public static final int NSUndoFunctionKey = 63299;
    /// <i>native declaration : :381</i>
    public static final int NSRedoFunctionKey = 63300;
    /// <i>native declaration : :382</i>
    public static final int NSFindFunctionKey = 63301;
    /// <i>native declaration : :383</i>
    public static final int NSHelpFunctionKey = 63302;
    /// <i>native declaration : :384</i>
    public static final int NSModeSwitchFunctionKey = 63303;
    /// <i>native declaration : :389</i>
    public static final int NSWindowExposedEventType = 0;
    /// <i>native declaration : :390</i>
    public static final int NSApplicationActivatedEventType = 1;
    /// <i>native declaration : :391</i>
    public static final int NSApplicationDeactivatedEventType = 2;
    /// <i>native declaration : :392</i>
    public static final int NSWindowMovedEventType = 4;
    /// <i>native declaration : :393</i>
    public static final int NSScreenChangedEventType = 8;
    /// <i>native declaration : :394</i>
    public static final int NSAWTEventType = 16;
    /// <i>native declaration : :398</i>
    public static final int NSPowerOffEventType = 1;


    public static final int NSAlphaShiftKeyMask = 1 << 16;
    public static final int NSShiftKeyMask = 1 << 17;
    public static final int NSControlKeyMask = 1 << 18;
    public static final int NSAlternateKeyMask = 1 << 19;
    public static final int NSCommandKeyMask = 1 << 20;
    public static final int NSNumericPadKeyMask = 1 << 21;
    public static final int NSHelpKeyMask = 1 << 22;
    public static final int NSFunctionKeyMask = 1 << 23;

    public interface _Class extends ObjCClass {
        /**
         * +eventWithEventRef: returns an autoreleased NSEvent corresponding to the EventRef.  The EventRef is retained by the NSEvent and will be released when the NSEvent is freed.  If there is no NSEvent corresponding to the EventRef, +eventWithEventRef will return nil.<br>
         * Original signature : <code>NSEvent* eventWithEventRef(const void*)</code><br>
         * EventRef<br>
         * <i>native declaration : :232</i>
         */
        NSEvent eventWithEventRef(NSEvent eventRef);
        /**
         * <i>native declaration : :240</i><br>
         * Conversion Error : /**<br>
         *  * +eventWithCGEvent: returns an autoreleased NSEvent corresponding to the CGEventRef.  The CGEventRef is retained by the NSEvent and will be released when the NSEvent is freed.  If there is no NSEvent corresponding to the CGEventRef, +eventWithEventRef will return nil.<br>
         *  * Original signature : <code>NSEvent* eventWithCGEvent(null)</code><br>
         *  * /<br>
         * + (NSEvent*)eventWithCGEvent:(null)cgEvent; (Argument cgEvent cannot be converted)
         */
        /**
         * Enable or disable coalescing of mouse movement events, including mouse moved, mouse dragged, and tablet events.  Coalescing is enabled by default.<br>
         * Original signature : <code>void setMouseCoalescingEnabled(BOOL)</code><br>
         * <i>native declaration : :244</i>
         */
        void setMouseCoalescingEnabled(boolean flag);

        /**
         * Original signature : <code>BOOL isMouseCoalescingEnabled()</code><br>
         * <i>native declaration : :245</i>
         */
        boolean isMouseCoalescingEnabled();
        /**
         * <i>native declaration : :296</i><br>
         * Conversion Error : NSTimeInterval
         */
        /**
         * Original signature : <code>void stopPeriodicEvents()</code><br>
         * <i>native declaration : :297</i>
         */
        void stopPeriodicEvents();
        /**
         * <i>native declaration : :300</i><br>
         * Conversion Error : NSPoint
         */
        /**
         * <i>native declaration : :301</i><br>
         * Conversion Error : NSPoint
         */
        /**
         * <i>native declaration : :302</i><br>
         * Conversion Error : NSPoint
         */
        /**
         * <i>native declaration : :303</i><br>
         * Conversion Error : NSPoint
         */
        /**
         * <i>native declaration : :306</i><br>
         * Conversion Error : NSPoint
         */
    }

    /**
     * these messages are valid for all events<br>
     * Original signature : <code>NSEventType type()</code><br>
     * <i>native declaration : :177</i>
     */
    public abstract int type();

    /**
     * Original signature : <code>NSUInteger modifierFlags()</code><br>
     * <i>native declaration : :178</i>
     */
    public abstract int modifierFlags();
    /**
     * <i>native declaration : :179</i><br>
     * Conversion Error : NSTimeInterval
     */
    /**
     * Original signature : <code>NSWindow* window()</code><br>
     * <i>native declaration : :180</i>
     */
    public abstract NSWindow window();

    /**
     * Original signature : <code>NSInteger windowNumber()</code><br>
     * <i>native declaration : :181</i>
     */
    public abstract int windowNumber();

    /**
     * Original signature : <code>NSGraphicsContext* context()</code><br>
     * <i>native declaration : :182</i>
     */
    public abstract com.sun.jna.Pointer context();

    /**
     * these messages are valid for all mouse down/up/drag events<br>
     * Original signature : <code>NSInteger clickCount()</code><br>
     * <i>native declaration : :185</i>
     */
    public abstract int clickCount();

    /**
     * Original signature : <code>NSInteger buttonNumber()</code><br>
     * for NSOtherMouse events, but will return valid constants for NSLeftMouse and NSRightMouse<br>
     * <i>native declaration : :186</i>
     */
    public abstract int buttonNumber();

    /**
     * these messages are valid for all mouse down/up/drag and enter/exit events<br>
     * Original signature : <code>NSInteger eventNumber()</code><br>
     * <i>native declaration : :188</i>
     */
    public abstract int eventNumber();

    /**
     * These messages are also valid for NSTabletPoint events on 10.4 or later<br>
     * Original signature : <code>float pressure()</code><br>
     * <i>native declaration : :192</i>
     */
    public abstract float pressure();

    /**
     * <i>native declaration : :193</i><br>
     * Conversion Error : NSPoint
     */
    public abstract NSPoint locationInWindow();

    /**
     * these messages are valid for scroll wheel events and mouse move/drag events<br>
     * Original signature : <code>CGFloat deltaX()</code><br>
     * <i>native declaration : :196</i>
     */
    public abstract CGFloat deltaX();

    /**
     * Original signature : <code>CGFloat deltaY()</code><br>
     * <i>native declaration : :197</i>
     */
    public abstract CGFloat deltaY();

    /**
     * Original signature : <code>CGFloat deltaZ()</code><br>
     * 0 for most scroll wheel and mouse events<br>
     * <i>native declaration : :198</i>
     */
    public abstract CGFloat deltaZ();

    /**
     * these messages are valid for keyup and keydown events<br>
     * Original signature : <code>NSString* characters()</code><br>
     * <i>native declaration : :201</i>
     */
    public abstract String characters();

    /**
     * Original signature : <code>NSString* charactersIgnoringModifiers()</code><br>
     * <i>native declaration : :202</i>
     */
    public abstract String charactersIgnoringModifiers();

    /**
     * the chars that would have been generated, regardless of modifier keys (except shift)<br>
     * Original signature : <code>BOOL isARepeat()</code><br>
     * <i>native declaration : :204</i>
     */
    public abstract boolean isARepeat();

    /**
     * this message is valid for keyup, keydown and flagschanged events<br>
     * Original signature : <code>unsigned short keyCode()</code><br>
     * device-independent key number<br>
     * <i>native declaration : :206</i>
     */
    public abstract short keyCode();

    /**
     * these messages are valid for enter and exit events<br>
     * Original signature : <code>NSInteger trackingNumber()</code><br>
     * <i>native declaration : :209</i>
     */
    public abstract int trackingNumber();

    /**
     * Original signature : <code>void* userData()</code><br>
     * <i>native declaration : :210</i>
     */
    public abstract NSObject userData();

    /**
     * -trackingArea returns the NSTrackingArea that generated this event.  It is possible for there to be no trackingArea associated with the event in some cases where the event corresponds to a trackingRect installed with -[NSView addTrackingRect:owner:userData:assumeInside:], in which case nil is returned.<br>
     * Original signature : <code>NSTrackingArea* trackingArea()</code><br>
     * <i>native declaration : :213</i>
     */
    public abstract com.sun.jna.Pointer trackingArea();

    /**
     * this message is also valid for mouse events on 10.4 or later<br>
     * Original signature : <code>short subtype()</code><br>
     * <i>native declaration : :218</i>
     */
    public abstract short subtype();

    /**
     * these messages are valid for kit, system, and app-defined events<br>
     * Original signature : <code>NSInteger data1()</code><br>
     * <i>native declaration : :221</i>
     */
    public abstract int data1();

    /**
     * Original signature : <code>NSInteger data2()</code><br>
     * <i>native declaration : :222</i>
     */
    public abstract int data2();

    /**
     * -eventRef returns an EventRef corresponding to the NSEvent.  The EventRef is retained by the NSEvent, so will be valid as long as the NSEvent is valid, and will be released when the NSEvent is freed.  You can use RetainEvent to extend the lifetime of the EventRef, with a corresponding ReleaseEvent when you are done with it.  If there is no EventRef corresponding to the NSEvent, -eventRef will return NULL.<br>
     * Original signature : <code>const void* eventRef()</code><br>
     * <i>native declaration : :229</i>
     */
    public abstract NSEvent eventRef();

    /**
     * -CGEvent returns an autoreleased CGEventRef corresponding to the NSEvent.  If there is no CGEventRef corresponding to the NSEvent, -CGEvent will return NULL.<br>
     * Original signature : <code>CGEvent()</code><br>
     * <i>native declaration : :236</i>
     */
    public abstract com.sun.jna.Pointer CGEvent();

    /**
     * this message is valid for mouse events with subtype NSTabletPointEventSubtype or NSTabletProximityEventSubtype, and for NSTabletPoint and NSTabletProximity events<br>
     * Original signature : <code>NSUInteger deviceID()</code><br>
     * <i>native declaration : :251</i>
     */
    public abstract int deviceID();

    /**
     * absolute x coordinate in tablet space at full tablet resolution<br>
     * Original signature : <code>NSInteger absoluteX()</code><br>
     * <i>native declaration : :255</i>
     */
    public abstract int absoluteX();

    /**
     * absolute y coordinate in tablet space at full tablet resolution<br>
     * Original signature : <code>NSInteger absoluteY()</code><br>
     * <i>native declaration : :257</i>
     */
    public abstract int absoluteY();

    /**
     * absolute z coordinate in tablet space at full tablet resolution<br>
     * Original signature : <code>NSInteger absoluteZ()</code><br>
     * <i>native declaration : :259</i>
     */
    public abstract int absoluteZ();

    /**
     * mask indicating which buttons are pressed.<br>
     * Original signature : <code>NSUInteger buttonMask()</code><br>
     * <i>native declaration : :261</i>
     */
    public abstract int buttonMask();
    /**
     * <i>native declaration : :263</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * device rotation in degrees<br>
     * Original signature : <code>float rotation()</code><br>
     * <i>native declaration : :265</i>
     */
    public abstract float rotation();

    /**
     * tangential pressure on the device; range is -1 to 1<br>
     * Original signature : <code>float tangentialPressure()</code><br>
     * <i>native declaration : :267</i>
     */
    public abstract float tangentialPressure();

    /**
     * NSArray of 3 vendor defined shorts<br>
     * Original signature : <code>vendorDefined()</code><br>
     * <i>native declaration : :269</i>
     */
    public abstract NSArray vendorDefined();

    /**
     * vendor defined, typically USB vendor ID<br>
     * Original signature : <code>NSUInteger vendorID()</code><br>
     * <i>native declaration : :273</i>
     */
    public abstract int vendorID();

    /**
     * vendor defined tablet ID<br>
     * Original signature : <code>NSUInteger tabletID()</code><br>
     * <i>native declaration : :275</i>
     */
    public abstract int tabletID();

    /**
     * index of the device on the tablet.  Usually 0, except for tablets that support multiple concurrent devices<br>
     * Original signature : <code>NSUInteger pointingDeviceID()</code><br>
     * <i>native declaration : :277</i>
     */
    public abstract int pointingDeviceID();

    /**
     * system assigned unique tablet ID<br>
     * Original signature : <code>NSUInteger systemTabletID()</code><br>
     * <i>native declaration : :279</i>
     */
    public abstract int systemTabletID();

    /**
     * vendor defined pointing device type<br>
     * Original signature : <code>NSUInteger vendorPointingDeviceType()</code><br>
     * <i>native declaration : :281</i>
     */
    public abstract int vendorPointingDeviceType();

    /**
     * vendor defined serial number of pointing device<br>
     * Original signature : <code>NSUInteger pointingDeviceSerialNumber()</code><br>
     * <i>native declaration : :283</i>
     */
    public abstract int pointingDeviceSerialNumber();

    /**
     * vendor defined unique ID<br>
     * Original signature : <code>unsigned long long uniqueID()</code><br>
     * <i>native declaration : :285</i>
     */
    public abstract long uniqueID();

    /**
     * mask representing capabilities of device<br>
     * Original signature : <code>NSUInteger capabilityMask()</code><br>
     * <i>native declaration : :287</i>
     */
    public abstract int capabilityMask();

    /**
     * mask representing capabilities of device<br>
     * Original signature : <code>NSPointingDeviceType pointingDeviceType()</code><br>
     * <i>native declaration : :289</i>
     */
    public abstract int pointingDeviceType();

    /**
     * YES - entering; NO - leaving<br>
     * Original signature : <code>BOOL isEnteringProximity()</code><br>
     * <i>native declaration : :291</i>
     */
    public abstract boolean isEnteringProximity();
}
