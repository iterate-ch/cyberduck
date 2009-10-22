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

import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSCopying;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSCell extends NSObject implements NSCopying {

    /// <i>native declaration : :11</i>
    public static final int NSAnyType = 0;
    /// <i>native declaration : :12</i>
    public static final int NSIntType = 1;
    /// <i>native declaration : :13</i>
    public static final int NSPositiveIntType = 2;
    /// <i>native declaration : :14</i>
    public static final int NSFloatType = 3;
    /// <i>native declaration : :15</i>
    public static final int NSPositiveFloatType = 4;
    /// <i>native declaration : :16</i>
    public static final int NSDoubleType = 6;
    /// <i>native declaration : :17</i>
    public static final int NSPositiveDoubleType = 7;
    /// <i>native declaration : :21</i>
    public static final int NSNullCellType = 0;
    /// <i>native declaration : :22</i>
    public static final int NSTextCellType = 1;
    /// <i>native declaration : :23</i>
    public static final int NSImageCellType = 2;
    /// <i>native declaration : :28</i>
    public static final int NSCellDisabled = 0;
    /// <i>native declaration : :29</i>
    public static final int NSCellState = 1;
    /// <i>native declaration : :30</i>
    public static final int NSPushInCell = 2;
    /// <i>native declaration : :31</i>
    public static final int NSCellEditable = 3;
    /// <i>native declaration : :32</i>
    public static final int NSChangeGrayCell = 4;
    /// <i>native declaration : :33</i>
    public static final int NSCellHighlighted = 5;
    /// <i>native declaration : :34</i>
    public static final int NSCellLightsByContents = 6;
    /// <i>native declaration : :35</i>
    public static final int NSCellLightsByGray = 7;
    /// <i>native declaration : :36</i>
    public static final int NSChangeBackgroundCell = 8;
    /// <i>native declaration : :37</i>
    public static final int NSCellLightsByBackground = 9;
    /// <i>native declaration : :38</i>
    public static final int NSCellIsBordered = 10;
    /// <i>native declaration : :39</i>
    public static final int NSCellHasOverlappingImage = 11;
    /// <i>native declaration : :40</i>
    public static final int NSCellHasImageHorizontal = 12;
    /// <i>native declaration : :41</i>
    public static final int NSCellHasImageOnLeftOrBottom = 13;
    /// <i>native declaration : :42</i>
    public static final int NSCellChangesContents = 14;
    /// <i>native declaration : :43</i>
    public static final int NSCellIsInsetButton = 15;
    /// <i>native declaration : :44</i>
    public static final int NSCellAllowsMixedState = 16;
    /// <i>native declaration : :49</i>
    public static final int NSNoImage = 0;
    /// <i>native declaration : :50</i>
    public static final int NSImageOnly = 1;
    /// <i>native declaration : :51</i>
    public static final int NSImageLeft = 2;
    /// <i>native declaration : :52</i>
    public static final int NSImageRight = 3;
    /// <i>native declaration : :53</i>
    public static final int NSImageBelow = 4;
    /// <i>native declaration : :54</i>
    public static final int NSImageAbove = 5;
    /// <i>native declaration : :55</i>
    public static final int NSImageOverlaps = 6;
    /**
     * Deprecated. Use NSScaleProportionallyDown<br>
     * <i>native declaration : :60</i>
     */
    public static final int NSScaleProportionally = 0;
    /**
     * Deprecated. Use NSScaleAxesIndependently<br>
     * <i>native declaration : :61</i>
     */
    public static final int NSScaleToFit = 1;
    /**
     * Deprecated. Use NSImageScaleNone<br>
     * <i>native declaration : :62</i>
     */
    public static final int NSScaleNone = 2;
    /**
     * Scale image down if it is too large for destination. Preserve aspect ratio.<br>
     * <i>native declaration : :67</i>
     */
    public static final int NSImageScaleProportionallyDown = 0;
    /**
     * Scale each dimension to exactly fit destination. Do not preserve aspect ratio.<br>
     * <i>native declaration : :68</i>
     */
    public static final int NSImageScaleAxesIndependently = 1;
    /**
     * Do not scale.<br>
     * <i>native declaration : :69</i>
     */
    public static final int NSImageScaleNone = 2;
    /**
     * Scale image to maximum possible dimensions while (1) staying within destination area (2) preserving aspect ratio<br>
     * <i>native declaration : :70</i>
     */
    public static final int NSImageScaleProportionallyUpOrDown = 3;
    /// <i>native declaration : :76</i>
    public static final int NSMixedState = -1;
    /// <i>native declaration : :77</i>
    public static final int NSOffState = 0;
    /// <i>native declaration : :78</i>
    public static final int NSOnState = 1;
    /// <i>native declaration : :85</i>
    public static final int NSNoCellMask = 0;
    /// <i>native declaration : :86</i>
    public static final int NSContentsCellMask = 1;
    /// <i>native declaration : :87</i>
    public static final int NSPushInCellMask = 2;
    /// <i>native declaration : :88</i>
    public static final int NSChangeGrayCellMask = 4;
    /// <i>native declaration : :89</i>
    public static final int NSChangeBackgroundCellMask = 8;
    /**
     * system 'default'<br>
     * <i>native declaration : :93</i>
     */
    public static final int NSDefaultControlTint = 0;
    /// <i>native declaration : :95</i>
    public static final int NSBlueControlTint = 1;
    /// <i>native declaration : :96</i>
    public static final int NSGraphiteControlTint = 6;
    /// <i>native declaration : :98</i>
    public static final int NSClearControlTint = 7;
    /// <i>native declaration : :103</i>
    public static final int NSRegularControlSize = 0;
    /// <i>native declaration : :104</i>
    public static final int NSSmallControlSize = 1;
    /// <i>native declaration : :106</i>
    public static final int NSMiniControlSize = 2;
    /**
     * An empty area, or did not hit in the cell<br>
     * <i>native declaration : :355</i>
     */
    public static final int NSCellHitNone = 0;
    /**
     * A content area in the cell<br>
     * <i>native declaration : :357</i>
     */
    public static final int NSCellHitContentArea = 1 << 0;
    /**
     * An editable text area of the cell<br>
     * <i>native declaration : :359</i>
     */
    public static final int NSCellHitEditableTextArea = 1 << 1;
    /**
     * A trackable area in the cell<br>
     * <i>native declaration : :361</i>
     */
    public static final int NSCellHitTrackableArea = 1 << 2;
    /**
     * The background is a light color. Dark content contrasts well with this background.<br>
     * <i>native declaration : :396</i>
     */
    public static final int NSBackgroundStyleLight = 0;
    /**
     * The background is a dark color. Light content contrasts well with this background.<br>
     * <i>native declaration : :397</i>
     */
    public static final int NSBackgroundStyleDark = 1;
    /**
     * The background is intended to appear higher than the content drawn on it. Content might need to be inset.<br>
     * <i>native declaration : :398</i>
     */
    public static final int NSBackgroundStyleRaised = 2;
    /**
     * The background is intended to appear lower than the content drawn on it. Content might need to be embossed.<br>
     * <i>native declaration : :399</i>
     */
    public static final int NSBackgroundStyleLowered = 3;

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>BOOL prefersTrackingUntilMouseUp()</code><br>
         * <i>native declaration : :175</i>
         */
        boolean prefersTrackingUntilMouseUp();

        /**
         * Original signature : <code>NSMenu* defaultMenu()</code><br>
         * <i>native declaration : :280</i>
         */
        NSMenu defaultMenu();

        /**
         * Original signature : <code>defaultFocusRingType()</code><br>
         * <i>from NSKeyboardUI native declaration : :323</i>
         */
        com.sun.jna.Pointer defaultFocusRingType();

        NSCell alloc();
    }

    /**
     * Original signature : <code>id initTextCell(NSString*)</code><br>
     * <i>native declaration : :178</i>
     */
    public abstract NSCell initTextCell(NSString aString);

    /**
     * Original signature : <code>id initImageCell(NSImage*)</code><br>
     * <i>native declaration : :179</i>
     */
    public abstract NSCell initImageCell(NSImage image);

    /**
     * Original signature : <code>NSView* controlView()</code><br>
     * <i>native declaration : :181</i>
     */
    public abstract NSView controlView();

    /**
     * Original signature : <code>void setControlView(NSView*)</code><br>
     * <i>native declaration : :183</i>
     */
    public abstract void setControlView(NSView view);

    /**
     * Original signature : <code>NSCellType type()</code><br>
     * <i>native declaration : :185</i>
     */
    public abstract int type();

    /**
     * Original signature : <code>void setType(NSCellType)</code><br>
     * <i>native declaration : :186</i>
     */
    public abstract void setType(int aType);

    /**
     * Original signature : <code>NSInteger state()</code><br>
     * <i>native declaration : :187</i>
     */
    public abstract int state();

    /**
     * Original signature : <code>void setState(NSInteger)</code><br>
     * <i>native declaration : :188</i>
     */
    public abstract void setState(int value);

    /**
     * Original signature : <code>action()</code><br>
     * <i>native declaration : :191</i>
     */
    public abstract Selector action();

    /**
     * <i>native declaration : :192</i><br>
     * Conversion Error : /// Original signature : <code>void setAction(null)</code><br>
     * - (void)setAction:(null)aSelector; (Argument aSelector cannot be converted)
     */
    public abstract void setAction(Selector action);

    /**
     * Original signature : <code>NSInteger tag()</code><br>
     * <i>native declaration : :193</i>
     */
    public abstract int tag();

    /**
     * Original signature : <code>void setTag(NSInteger)</code><br>
     * <i>native declaration : :194</i>
     */
    public abstract void setTag(int anInt);

    /**
     * Original signature : <code>NSString* title()</code><br>
     * <i>native declaration : :195</i>
     */
    public abstract String title();

    /**
     * Original signature : <code>void setTitle(NSString*)</code><br>
     * <i>native declaration : :196</i>
     */
    public abstract void setTitle(String aString);

    /**
     * Original signature : <code>BOOL isOpaque()</code><br>
     * <i>native declaration : :197</i>
     */
    public abstract boolean isOpaque();

    /**
     * Original signature : <code>BOOL isEnabled()</code><br>
     * <i>native declaration : :198</i>
     */
    public abstract boolean isEnabled();

    /**
     * Original signature : <code>void setEnabled(BOOL)</code><br>
     * <i>native declaration : :199</i>
     */
    public abstract void setEnabled(boolean flag);

    /**
     * Original signature : <code>NSInteger sendActionOn(NSInteger)</code><br>
     * <i>native declaration : :200</i>
     */
    public abstract int sendActionOn(int mask);

    /**
     * Original signature : <code>BOOL isContinuous()</code><br>
     * <i>native declaration : :201</i>
     */
    public abstract boolean isContinuous();

    /**
     * Original signature : <code>void setContinuous(BOOL)</code><br>
     * <i>native declaration : :202</i>
     */
    public abstract void setContinuous(boolean flag);

    /**
     * Original signature : <code>BOOL isEditable()</code><br>
     * <i>native declaration : :203</i>
     */
    public abstract boolean isEditable();

    /**
     * Original signature : <code>void setEditable(BOOL)</code><br>
     * <i>native declaration : :204</i>
     */
    public abstract void setEditable(boolean flag);

    /**
     * Original signature : <code>BOOL isSelectable()</code><br>
     * <i>native declaration : :205</i>
     */
    public abstract boolean isSelectable();

    /**
     * Original signature : <code>void setSelectable(BOOL)</code><br>
     * <i>native declaration : :206</i>
     */
    public abstract void setSelectable(boolean flag);

    /**
     * Original signature : <code>BOOL isBordered()</code><br>
     * <i>native declaration : :207</i>
     */
    public abstract boolean isBordered();

    /**
     * Original signature : <code>void setBordered(BOOL)</code><br>
     * <i>native declaration : :208</i>
     */
    public abstract void setBordered(boolean flag);

    /**
     * Original signature : <code>BOOL isBezeled()</code><br>
     * <i>native declaration : :209</i>
     */
    public abstract boolean isBezeled();

    /**
     * Original signature : <code>void setBezeled(BOOL)</code><br>
     * <i>native declaration : :210</i>
     */
    public abstract void setBezeled(boolean flag);

    /**
     * Original signature : <code>BOOL isScrollable()</code><br>
     * <i>native declaration : :211</i>
     */
    public abstract boolean isScrollable();

    /**
     * Original signature : <code>void setScrollable(BOOL)</code><br>
     * If YES, sets wraps to NO<br>
     * <i>native declaration : :212</i>
     */
    public abstract void setScrollable(boolean flag);

    /**
     * Original signature : <code>BOOL isHighlighted()</code><br>
     * <i>native declaration : :213</i>
     */
    public abstract boolean isHighlighted();

    /**
     * Original signature : <code>void setHighlighted(BOOL)</code><br>
     * <i>native declaration : :214</i>
     */
    public abstract void setHighlighted(boolean flag);

    /**
     * Original signature : <code>alignment()</code><br>
     * <i>native declaration : :215</i>
     */
    public abstract com.sun.jna.Pointer alignment();

    /**
     * <i>native declaration : :216</i><br>
     * Conversion Error : /// Original signature : <code>void setAlignment(null)</code><br>
     * - (void)setAlignment:(null)mode; (Argument mode cannot be converted)
     */
    public abstract void setAlignment(int mode);

    /**
     * Original signature : <code>BOOL wraps()</code><br>
     * <i>native declaration : :217</i>
     */
    public abstract boolean wraps();

    /**
     * Original signature : <code>void setWraps(BOOL)</code><br>
     * If YES, sets scrollable to NO<br>
     * <i>native declaration : :218</i>
     */
    public abstract void setWraps(boolean flag);

    /**
     * Original signature : <code>NSFont* font()</code><br>
     * <i>native declaration : :219</i>
     */
    public abstract NSFont font();

    /**
     * Original signature : <code>void setFont(NSFont*)</code><br>
     * <i>native declaration : :220</i>
     */
    public abstract void setFont(NSFont fontObj);

    /**
     * Original signature : <code>NSInteger entryType()</code><br>
     * <i>native declaration : :221</i>
     */
    public abstract int entryType();

    /**
     * Original signature : <code>void setEntryType(NSInteger)</code><br>
     * <i>native declaration : :222</i>
     */
    public abstract void setEntryType(int aType);

    /**
     * Original signature : <code>BOOL isEntryAcceptable(NSString*)</code><br>
     * <i>native declaration : :223</i>
     */
    public abstract boolean isEntryAcceptable(String aString);

    /**
     * Original signature : <code>void setFloatingPointFormat(BOOL, NSUInteger, NSUInteger)</code><br>
     * <i>native declaration : :224</i>
     */
    public abstract void setFloatingPointFormat_left_right(boolean autoRange, int leftDigits, int rightDigits);

    /**
     * Original signature : <code>NSString* keyEquivalent()</code><br>
     * <i>native declaration : :225</i>
     */
    public abstract String keyEquivalent();

    /**
     * Original signature : <code>void setFormatter(NSFormatter*)</code><br>
     * <i>native declaration : :226</i>
     */
    public abstract void setFormatter(com.sun.jna.Pointer newFormatter);

    /**
     * Original signature : <code>id formatter()</code><br>
     * <i>native declaration : :227</i>
     */
    public abstract org.rococoa.ID formatter();

    /**
     * Original signature : <code>id objectValue()</code><br>
     * <i>native declaration : :228</i>
     */
    public abstract NSObject objectValue();

    /**
     * <i>native declaration : :229</i><br>
     * Conversion Error : id<NSCopying>
     */
    public abstract void setObjectValue(NSObject value);

    /**
     * Original signature : <code>BOOL hasValidObjectValue()</code><br>
     * <i>native declaration : :230</i>
     */
    public abstract boolean hasValidObjectValue();

    /**
     * Original signature : <code>NSString* stringValue()</code><br>
     * <i>native declaration : :231</i>
     */
    public abstract String stringValue();

    /**
     * Original signature : <code>void setStringValue(NSString*)</code><br>
     * <i>native declaration : :232</i>
     */
    public abstract void setStringValue(String aString);

    /**
     * Original signature : <code>compare(id)</code><br>
     * <i>native declaration : :233</i>
     */
    public abstract com.sun.jna.Pointer compare(NSObject otherCell);

    /**
     * Original signature : <code>int intValue()</code><br>
     * <i>native declaration : :234</i>
     */
    public abstract int intValue();

    /**
     * Original signature : <code>void setIntValue(int)</code><br>
     * <i>native declaration : :235</i>
     */
    public abstract void setIntValue(int anInt);

    /**
     * Original signature : <code>float floatValue()</code><br>
     * <i>native declaration : :236</i>
     */
    public abstract float floatValue();

    /**
     * Original signature : <code>void setFloatValue(float)</code><br>
     * <i>native declaration : :237</i>
     */
    public abstract void setFloatValue(float aFloat);

    /**
     * Original signature : <code>double doubleValue()</code><br>
     * <i>native declaration : :238</i>
     */
    public abstract double doubleValue();

    /**
     * Original signature : <code>void setDoubleValue(double)</code><br>
     * <i>native declaration : :239</i>
     */
    public abstract void setDoubleValue(double aDouble);

    /**
     * Original signature : <code>void takeIntValueFrom(id)</code><br>
     * <i>native declaration : :240</i>
     */
    public abstract void takeIntValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeFloatValueFrom(id)</code><br>
     * <i>native declaration : :241</i>
     */
    public abstract void takeFloatValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeDoubleValueFrom(id)</code><br>
     * <i>native declaration : :242</i>
     */
    public abstract void takeDoubleValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeStringValueFrom(id)</code><br>
     * <i>native declaration : :243</i>
     */
    public abstract void takeStringValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeObjectValueFrom(id)</code><br>
     * <i>native declaration : :244</i>
     */
    public abstract void takeObjectValueFrom(final ID sender);

    /**
     * Original signature : <code>NSImage* image()</code><br>
     * <i>native declaration : :245</i>
     */
    public abstract NSImage image();

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :246</i>
     */
    public abstract void setImage(NSImage image);

    /**
     * Original signature : <code>void setControlTint(NSControlTint)</code><br>
     * <i>native declaration : :247</i>
     */
    public abstract void setControlTint(NSUInteger controlTint);

    /**
     * Original signature : <code>NSControlTint controlTint()</code><br>
     * <i>native declaration : :248</i>
     */
    public abstract NSUInteger controlTint();

    /**
     * Original signature : <code>void setControlSize(NSControlSize)</code><br>
     * <i>native declaration : :249</i>
     */
    public abstract void setControlSize(int size);

    /**
     * Original signature : <code>NSControlSize controlSize()</code><br>
     * <i>native declaration : :250</i>
     */
    public abstract int controlSize();

    /**
     * Original signature : <code>id representedObject()</code><br>
     * <i>native declaration : :251</i>
     */
    public abstract org.rococoa.ID representedObject();

    /**
     * Original signature : <code>void setRepresentedObject(id)</code><br>
     * <i>native declaration : :252</i>
     */
    public abstract void setRepresentedObject(org.rococoa.ID anObject);

    /**
     * Original signature : <code>NSInteger cellAttribute(NSCellAttribute)</code><br>
     * <i>native declaration : :253</i>
     */
    public abstract int cellAttribute(int aParameter);

    /**
     * Original signature : <code>void setCellAttribute(NSCellAttribute, NSInteger)</code><br>
     * <i>native declaration : :254</i>
     */
    public abstract void setCellAttribute_to(int aParameter, int value);
    /**
     * <i>native declaration : :255</i><br>
     * Conversion Error : /// Original signature : <code>imageRectForBounds(null)</code><br>
     * - (null)imageRectForBounds:(null)theRect; (Argument theRect cannot be converted)
     */
    /**
     * <i>native declaration : :256</i><br>
     * Conversion Error : /// Original signature : <code>titleRectForBounds(null)</code><br>
     * - (null)titleRectForBounds:(null)theRect; (Argument theRect cannot be converted)
     */
    /**
     * <i>native declaration : :257</i><br>
     * Conversion Error : /// Original signature : <code>drawingRectForBounds(null)</code><br>
     * - (null)drawingRectForBounds:(null)theRect; (Argument theRect cannot be converted)
     */
    /**
     * Original signature : <code>cellSize()</code><br>
     * <i>native declaration : :258</i>
     */
    public abstract NSSize cellSize();
    /**
     * <i>native declaration : :259</i><br>
     * Conversion Error : /// Original signature : <code>cellSizeForBounds(null)</code><br>
     * - (null)cellSizeForBounds:(null)aRect; (Argument aRect cannot be converted)
     */
    /**
     * <i>native declaration : :260</i><br>
     * Conversion Error : /// Original signature : <code>NSColor* highlightColorWithFrame(null, NSView*)</code><br>
     * - (NSColor*)highlightColorWithFrame:(null)cellFrame inView:(NSView*)controlView; (Argument cellFrame cannot be converted)
     */
    /**
     * <i>native declaration : :261</i><br>
     * Conversion Error : /// Original signature : <code>void calcDrawInfo(null)</code><br>
     * - (void)calcDrawInfo:(null)aRect; (Argument aRect cannot be converted)
     */
    /**
     * Original signature : <code>NSText* setUpFieldEditorAttributes(NSText*)</code><br>
     * <i>native declaration : :262</i>
     */
    public abstract NSText setUpFieldEditorAttributes(com.sun.jna.Pointer textObj);
    /**
     * <i>native declaration : :263</i><br>
     * Conversion Error : /// Original signature : <code>void drawInteriorWithFrame(null, NSView*)</code><br>
     * - (void)drawInteriorWithFrame:(null)cellFrame inView:(NSView*)controlView; (Argument cellFrame cannot be converted)
     */
    /**
     * <i>native declaration : :264</i><br>
     * Conversion Error : /// Original signature : <code>void drawWithFrame(null, NSView*)</code><br>
     * - (void)drawWithFrame:(null)cellFrame inView:(NSView*)controlView; (Argument cellFrame cannot be converted)
     */
    /**
     * <i>native declaration : :265</i><br>
     * Conversion Error : /// Original signature : <code>void highlight(BOOL, null, NSView*)</code><br>
     * - (void)highlight:(BOOL)flag withFrame:(null)cellFrame inView:(NSView*)controlView; (Argument cellFrame cannot be converted)
     */
    /**
     * Original signature : <code>NSInteger mouseDownFlags()</code><br>
     * <i>native declaration : :266</i>
     */
    public abstract int mouseDownFlags();

    /**
     * Original signature : <code>void getPeriodicDelay(float*, float*)</code><br>
     * <i>native declaration : :267</i>
     */
    public abstract void getPeriodicDelay_interval(java.nio.FloatBuffer delay, java.nio.FloatBuffer interval);
    /**
     * <i>native declaration : :268</i><br>
     * Conversion Error : /// Original signature : <code>BOOL startTrackingAt(null, NSView*)</code><br>
     * - (BOOL)startTrackingAt:(null)startPoint inView:(NSView*)controlView; (Argument startPoint cannot be converted)
     */
    /**
     * <i>native declaration : :269</i><br>
     * Conversion Error : /// Original signature : <code>BOOL continueTracking(null, null, NSView*)</code><br>
     * - (BOOL)continueTracking:(null)lastPoint at:(null)currentPoint inView:(NSView*)controlView; (Argument lastPoint cannot be converted)
     */
    /**
     * <i>native declaration : :270</i><br>
     * Conversion Error : /// Original signature : <code>void stopTracking(null, null, NSView*, BOOL)</code><br>
     * - (void)stopTracking:(null)lastPoint at:(null)stopPoint inView:(NSView*)controlView mouseIsUp:(BOOL)flag; (Argument lastPoint cannot be converted)
     */
    /**
     * <i>native declaration : :271</i><br>
     * Conversion Error : /// Original signature : <code>BOOL trackMouse(NSEvent*, null, NSView*, BOOL)</code><br>
     * - (BOOL)trackMouse:(NSEvent*)theEvent inRect:(null)cellFrame ofView:(NSView*)controlView untilMouseUp:(BOOL)flag; (Argument cellFrame cannot be converted)
     */
    /**
     * <i>native declaration : :272</i><br>
     * Conversion Error : /// Original signature : <code>void editWithFrame(null, NSView*, NSText*, id, NSEvent*)</code><br>
     * - (void)editWithFrame:(null)aRect inView:(NSView*)controlView editor:(NSText*)textObj delegate:(id)anObject event:(NSEvent*)theEvent; (Argument aRect cannot be converted)
     */
    /**
     * <i>native declaration : :273</i><br>
     * Conversion Error : /// Original signature : <code>void selectWithFrame(null, NSView*, NSText*, id, NSInteger, NSInteger)</code><br>
     * - (void)selectWithFrame:(null)aRect inView:(NSView*)controlView editor:(NSText*)textObj delegate:(id)anObject start:(NSInteger)selStart length:(NSInteger)selLength; (Argument aRect cannot be converted)
     */
    /**
     * Original signature : <code>void endEditing(NSText*)</code><br>
     * <i>native declaration : :274</i>
     */
    public abstract void endEditing(NSText textObj);
    /**
     * <i>native declaration : :275</i><br>
     * Conversion Error : /// Original signature : <code>void resetCursorRect(null, NSView*)</code><br>
     * - (void)resetCursorRect:(null)cellFrame inView:(NSView*)controlView; (Argument cellFrame cannot be converted)
     */
    /**
     * Original signature : <code>void setMenu(NSMenu*)</code><br>
     * <i>native declaration : :277</i>
     */
    public abstract void setMenu(NSMenu aMenu);

    /**
     * Original signature : <code>NSMenu* menu()</code><br>
     * <i>native declaration : :278</i>
     */
    public abstract NSMenu menu();
    /**
     * <i>native declaration : :279</i><br>
     * Conversion Error : /// Original signature : <code>NSMenu* menuForEvent(NSEvent*, null, NSView*)</code><br>
     * - (NSMenu*)menuForEvent:(NSEvent*)event inRect:(null)cellFrame ofView:(NSView*)view; (Argument cellFrame cannot be converted)
     */
    /**
     * Original signature : <code>void setSendsActionOnEndEditing(BOOL)</code><br>
     * <i>native declaration : :282</i>
     */
    public abstract void setSendsActionOnEndEditing(boolean flag);

    /**
     * Original signature : <code>BOOL sendsActionOnEndEditing()</code><br>
     * <i>native declaration : :283</i>
     */
    public abstract boolean sendsActionOnEndEditing();

    /**
     * Original signature : <code>baseWritingDirection()</code><br>
     * <i>native declaration : :286</i>
     */
    public abstract int baseWritingDirection();
    /**
     * <i>native declaration : :287</i><br>
     * Conversion Error : /// Original signature : <code>void setBaseWritingDirection(null)</code><br>
     * - (void)setBaseWritingDirection:(null)writingDirection; (Argument writingDirection cannot be converted)
     */
    /**
     * <i>native declaration : :289</i><br>
     * Conversion Error : /// Original signature : <code>void setLineBreakMode(null)</code><br>
     * - (void)setLineBreakMode:(null)mode; (Argument mode cannot be converted)
     */
    /**
     * Original signature : <code>lineBreakMode()</code><br>
     * <i>native declaration : :290</i>
     */
    public abstract com.sun.jna.Pointer lineBreakMode();

    /**
     * Original signature : <code>void setAllowsUndo(BOOL)</code><br>
     * <i>native declaration : :292</i>
     */
    public abstract void setAllowsUndo(boolean allowsUndo);

    /**
     * Original signature : <code>BOOL allowsUndo()</code><br>
     * <i>native declaration : :293</i>
     */
    public abstract boolean allowsUndo();

    /**
     * Original signature : <code>NSInteger integerValue()</code><br>
     * <i>native declaration : :297</i>
     */
    public abstract NSInteger integerValue();

    /**
     * Original signature : <code>void setIntegerValue(NSInteger)</code><br>
     * <i>native declaration : :298</i>
     */
    public abstract void setIntegerValue(NSInteger anInteger);

    /**
     * Original signature : <code>void takeIntegerValueFrom(id)</code><br>
     * <i>native declaration : :299</i>
     */
    public abstract void takeIntegerValueFrom(final ID sender);

    /**
     * Truncates and adds the ellipsis character to the last visible line if the text doesn't fit into the cell bounds. The setting is ignored if -lineBreakMode is neither NSLineBreakByWordWrapping nor NSLineBreakByCharWrapping.<br>
     * Original signature : <code>BOOL truncatesLastVisibleLine()</code><br>
     * <i>native declaration : :304</i>
     */
    public abstract boolean truncatesLastVisibleLine();

    /**
     * Original signature : <code>void setTruncatesLastVisibleLine(BOOL)</code><br>
     * <i>native declaration : :305</i>
     */
    public abstract void setTruncatesLastVisibleLine(boolean flag);

    /**
     * Original signature : <code>void setRefusesFirstResponder(BOOL)</code><br>
     * <i>from NSKeyboardUI native declaration : :309</i>
     */
    public abstract void setRefusesFirstResponder(boolean flag);

    /**
     * Original signature : <code>BOOL refusesFirstResponder()</code><br>
     * <i>from NSKeyboardUI native declaration : :310</i>
     */
    public abstract boolean refusesFirstResponder();

    /**
     * Original signature : <code>BOOL acceptsFirstResponder()</code><br>
     * <i>from NSKeyboardUI native declaration : :311</i>
     */
    public abstract boolean acceptsFirstResponder();

    /**
     * Original signature : <code>void setShowsFirstResponder(BOOL)</code><br>
     * <i>from NSKeyboardUI native declaration : :312</i>
     */
    public abstract void setShowsFirstResponder(boolean showFR);

    /**
     * Original signature : <code>BOOL showsFirstResponder()</code><br>
     * <i>from NSKeyboardUI native declaration : :313</i>
     */
    public abstract boolean showsFirstResponder();

    /**
     * Original signature : <code>void setMnemonicLocation(NSUInteger)</code><br>
     * <i>from NSKeyboardUI native declaration : :314</i>
     */
    public abstract void setMnemonicLocation(int location);

    /**
     * Original signature : <code>NSUInteger mnemonicLocation()</code><br>
     * <i>from NSKeyboardUI native declaration : :315</i>
     */
    public abstract int mnemonicLocation();

    /**
     * Original signature : <code>NSString* mnemonic()</code><br>
     * <i>from NSKeyboardUI native declaration : :316</i>
     */
    public abstract String mnemonic();

    /**
     * Original signature : <code>void setTitleWithMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :317</i>
     */
    public abstract void setTitleWithMnemonic(String stringWithAmpersand);

    /**
     * Original signature : <code>void performClick(id)</code><br>
     * <i>from NSKeyboardUI native declaration : :318</i>
     */
    public abstract void performClick(final ID sender);
    /**
     * <i>from NSKeyboardUI native declaration : :321</i><br>
     * Conversion Error : /// Original signature : <code>void setFocusRingType(null)</code><br>
     * - (void)setFocusRingType:(null)focusRingType; (Argument focusRingType cannot be converted)
     */
    /**
     * Original signature : <code>focusRingType()</code><br>
     * <i>from NSKeyboardUI native declaration : :322</i>
     */
    public abstract com.sun.jna.Pointer focusRingType();

    /**
     * Original signature : <code>BOOL wantsNotificationForMarkedText()</code><br>
     * If the receiver returns YES, the field editor initiated by it posts text change notifications (i.e. NSTextDidChangeNotification) while editing marked text; otherwise, they are delayed until the marked text confirmation. The NSCell's implementation returns NO.<br>
     * <i>from NSKeyboardUI native declaration : :326</i>
     */
    public abstract boolean wantsNotificationForMarkedText();

    /**
     * Original signature : <code>NSAttributedString* attributedStringValue()</code><br>
     * <i>from NSCellAttributedStringMethods native declaration : :331</i>
     */
    public abstract NSAttributedString attributedStringValue();

    /**
     * Original signature : <code>void setAttributedStringValue(NSAttributedString*)</code><br>
     * <i>from NSCellAttributedStringMethods native declaration : :332</i>
     */
    public abstract void setAttributedStringValue(NSAttributedString obj);

    /**
     * These methods determine whether the user can modify text attributes and import graphics in a rich cell.  Note that whatever these flags are, cells can still contain attributed text if programmatically set.<br>
     * Original signature : <code>BOOL allowsEditingTextAttributes()</code><br>
     * <i>from NSCellAttributedStringMethods native declaration : :334</i>
     */
    public abstract boolean allowsEditingTextAttributes();

    /**
     * Original signature : <code>void setAllowsEditingTextAttributes(BOOL)</code><br>
     * If NO, also clears setImportsGraphics:<br>
     * <i>from NSCellAttributedStringMethods native declaration : :335</i>
     */
    public abstract void setAllowsEditingTextAttributes(boolean flag);

    /**
     * Original signature : <code>BOOL importsGraphics()</code><br>
     * <i>from NSCellAttributedStringMethods native declaration : :336</i>
     */
    public abstract boolean importsGraphics();

    /**
     * Original signature : <code>void setImportsGraphics(BOOL)</code><br>
     * If YES, also sets setAllowsEditingTextAttributes:<br>
     * <i>from NSCellAttributedStringMethods native declaration : :337</i>
     */
    public abstract void setImportsGraphics(boolean flag);

    /**
     * Original signature : <code>void setAllowsMixedState(BOOL)</code><br>
     * allow button to have mixed state value<br>
     * <i>from NSCellMixedState native declaration : :341</i>
     */
    public abstract void setAllowsMixedState(boolean flag);

    /**
     * Original signature : <code>BOOL allowsMixedState()</code><br>
     * <i>from NSCellMixedState native declaration : :342</i>
     */
    public abstract boolean allowsMixedState();

    /**
     * Original signature : <code>NSInteger nextState()</code><br>
     * get next state state in cycle<br>
     * <i>from NSCellMixedState native declaration : :343</i>
     */
    public abstract NSInteger nextState();

    /**
     * Original signature : <code>void setNextState()</code><br>
     * toggle/cycle through states<br>
     * <i>from NSCellMixedState native declaration : :344</i>
     */
    public abstract void setNextState();
    /**
     * <i>from NSCellHitTest native declaration : :382</i><br>
     * Conversion Error : /**<br>
     *  * Return hit testing information for the cell. Use a bit-wise mask to look for a specific value when calling the method. Generally, this should be overridden by custom NSCell subclasses to return the correct result. Currently, it is called by some multi-cell views, such as NSTableView.<br>
     *  * By default, NSCell will look at the cell type and do the following:<br>
     *  * NSImageCellType: <br>
     *  * If the image exists, and the event point is in the image return NSCellHitContentArea, else NSCellHitNone.<br>
     *  * NSTextCellType (also applies to NSTextFieldCell): <br>
     *  * If there is text:<br>
     *  * If the event point hits in the text, return NSCellHitContentArea. Additionally, if the cell is enabled return NSCellHitContentArea | NSCellHitEditableTextArea.<br>
     *  * If there is not text:<br>
     *  * Returns NSCellHitNone.<br>
     *  * NSNullCellType (this is the default that applies to non text or image cells who don't override hitTestForEvent:):<br>
     *  * Return NSCellHitContentArea by default.<br>
     *  * If the cell not disabled, and it would track, return NSCellHitContentArea | NSCellHitTrackableArea.<br>
     *  * Original signature : <code>NSUInteger hitTestForEvent(NSEvent*, null, NSView*)</code><br>
     *  * /<br>
     * - (NSUInteger)hitTestForEvent:(NSEvent*)event inRect:(null)cellFrame ofView:(NSView*)controlView; (Argument cellFrame cannot be converted)
     */
    /**
     * <i>from NSCellExpansion native declaration : :388</i><br>
     * Conversion Error : /**<br>
     *  * Allows the cell to return an expansion cell frame if cellFrame is too small for the entire contents in the view. When the mouse is hovered over the cell in certain controls, the full cell contents will be shown in a special floating tool tip view. If the frame is not too small, return an empty rect, and no expansion tool tip view will be shown. By default, NSCell returns NSZeroRect, while some subclasses (such as NSTextFieldCell) will return the proper frame when required.<br>
     *  * Original signature : <code>expansionFrameWithFrame(null, NSView*)</code><br>
     *  * /<br>
     * - (null)expansionFrameWithFrame:(null)cellFrame inView:(NSView*)view; (Argument cellFrame cannot be converted)
     */
    /**
     * <i>from NSCellExpansion native declaration : :392</i><br>
     * Conversion Error : /**<br>
     *  * Allows the cell to perform custom expansion tool tip drawing. Note that the view may be different from the original view that the cell appeared in. By default, NSCell simply calls drawWithFrame:inView:.<br>
     *  * Original signature : <code>void drawWithExpansionFrame(null, NSView*)</code><br>
     *  * /<br>
     * - (void)drawWithExpansionFrame:(null)cellFrame inView:(NSView*)view; (Argument cellFrame cannot be converted)
     */
    /**
     * Describes the surface the cell is drawn onto in -[NSCell drawWithFrame:inView:]. A control typically sets this before it asks the cell to draw. A cell may draw differently based on background characteristics. For example, a tableview drawing a cell in a selected row might call [cell setBackgroundStyle:NSBackgroundStyleDark]. A text cell might decide to render its text white as a result. A rating-style level indicator might draw its stars white instead of gray.<br>
     * Original signature : <code>NSBackgroundStyle backgroundStyle()</code><br>
     * <i>from NSCellBackgroundStyle native declaration : :407</i>
     */
    public abstract NSUInteger backgroundStyle();

    /**
     * Original signature : <code>void setBackgroundStyle(NSBackgroundStyle)</code><br>
     * <i>from NSCellBackgroundStyle native declaration : :408</i>
     */
    public abstract void setBackgroundStyle(NSUInteger style);

    /**
     * Describes the surface drawn onto in -[NSCell drawInteriorWithFrame:inView:]. This is often the same as the backgroundStyle, but a button that draws a bezel would have a different interiorBackgroundStyle.  <br>
     * This is both an override point and a useful method to call. A button that draws a custom bezel would override this to describe that surface. A cell that has custom interior drawing might query this method to help pick an image that looks good on the cell. Calling this method gives you some independence from changes in framework art style.<br>
     * Original signature : <code>NSBackgroundStyle interiorBackgroundStyle()</code><br>
     * <i>from NSCellBackgroundStyle native declaration : :415</i>
     */
    public abstract NSUInteger interiorBackgroundStyle();
}
