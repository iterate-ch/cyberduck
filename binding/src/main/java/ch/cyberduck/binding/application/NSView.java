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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSView extends NSResponder {
    private static final NSView._Class CLASS = org.rococoa.Rococoa.createClass("NSView", NSView._Class.class);

    public static final int NSViewNotSizable = 0;
    public static final int NSViewMinXMargin = 1;
    public static final int NSViewWidthSizable = 2;
    public static final int NSViewMaxXMargin = 4;
    public static final int NSViewMinYMargin = 8;
    public static final int NSViewHeightSizable = 16;
    public static final int NSViewMaxYMargin = 32;

    public static final int NSViewLayerContentsRedrawNever = 0;
    public static final int NSViewLayerContentsRedrawOnSetNeedsDisplay = 1;
    public static final int NSViewLayerContentsRedrawDuringViewResize = 2;
    public static final int NSViewLayerContentsRedrawBeforeViewResize = 3;
    public static final int NSViewLayerContentsRedrawCrossfade = 4;

    public enum NSFocusRingType {
        NSFocusRingTypeDefault,
        NSFocusRingTypeNone,
        NSFocusRingTypeExterior
    }

    public static NSView create() {
        return CLASS.alloc().init();
    }

    public static NSView create(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public interface _Class extends ObjCClass {
        NSView alloc();

        /**
         * Original signature : <code>NSView* focusView()</code><br>
         * <i>native declaration : :213</i>
         */
        NSView focusView();

        /**
         * Original signature : <code>NSMenu* defaultMenu()</code><br>
         * <i>native declaration : :311</i>
         */
        NSMenu defaultMenu();

        /**
         * Original signature : <code>defaultFocusRingType()</code><br>
         * <i>from NSKeyboardUI native declaration : :357</i>
         */
        NSObject defaultFocusRingType();
    }

    public abstract NSView init();

    public abstract NSView initWithFrame(NSRect frameRect);

    /**
     * <i>native declaration : :115</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>NSWindow* window()</code><br>
     * <i>native declaration : :117</i>
     */
    public abstract NSWindow window();

    /**
     * Original signature : <code>NSView* superview()</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract NSView superview();

    /**
     * Original signature : <code>NSArray* subviews()</code><br>
     * <i>native declaration : :119</i>
     */
    public abstract NSArray subviews();

    /**
     * Original signature : <code>BOOL isDescendantOf(NSView*)</code><br>
     * <i>native declaration : :120</i>
     */
    public abstract boolean isDescendantOf(NSView aView);

    /**
     * Original signature : <code>NSView* ancestorSharedWithView(NSView*)</code><br>
     * <i>native declaration : :121</i>
     */
    public abstract NSView ancestorSharedWithView(NSView aView);

    /**
     * Original signature : <code>NSView* opaqueAncestor()</code><br>
     * <i>native declaration : :122</i>
     */
    public abstract NSView opaqueAncestor();

    /**
     * Original signature : <code>void setHidden(BOOL)</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract void setHidden(boolean flag);

    /**
     * Original signature : <code>BOOL isHidden()</code><br>
     * <i>native declaration : :125</i>
     */
    public abstract boolean isHidden();

    /**
     * Original signature : <code>BOOL isHiddenOrHasHiddenAncestor()</code><br>
     * <i>native declaration : :126</i>
     */
    public abstract boolean isHiddenOrHasHiddenAncestor();

    /**
     * Original signature : <code>void getRectsBeingDrawn(const NSRect**, NSInteger*)</code><br>
     * <i>native declaration : :128</i>
     */
    public abstract void getRectsBeingDrawn_count(com.sun.jna.ptr.PointerByReference rects, java.nio.IntBuffer count);
    /**
     * <i>native declaration : :129</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>BOOL wantsDefaultClipping()</code><br>
     * <i>native declaration : :130</i>
     */
    public abstract boolean wantsDefaultClipping();

    /**
     * Original signature : <code>void viewDidHide()</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract void viewDidHide();

    /**
     * Original signature : <code>void viewDidUnhide()</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract void viewDidUnhide();

    /**
     * Original signature : <code>void setSubviews(NSArray*)</code><br>
     * <i>native declaration : :136</i>
     */
    public abstract void setSubviews(NSArray newSubviews);

    /**
     * Original signature : <code>void addSubview(NSView*)</code><br>
     * <i>native declaration : :138</i>
     */
    public abstract void addSubview(NSView aView);

    /**
     * <i>native declaration : :139</i><br>
     * Conversion Error : /// Original signature : <code>void addSubview(NSView*, null, NSView*)</code><br>
     * - (void)addSubview:(NSView*)aView positioned:(null)place relativeTo:(NSView*)otherView; (Argument place cannot be converted)
     */
    public abstract void addSubview_positioned_relativeTo(NSView aView, int place, NSView otherView);

    /**
     * Original signature : <code>void viewWillMoveToWindow(NSWindow*)</code><br>
     * <i>native declaration : :141</i>
     */
    public abstract void viewWillMoveToWindow(NSWindow newWindow);

    /**
     * Original signature : <code>void viewDidMoveToWindow()</code><br>
     * <i>native declaration : :142</i>
     */
    public abstract void viewDidMoveToWindow();

    /**
     * Original signature : <code>void viewWillMoveToSuperview(NSView*)</code><br>
     * <i>native declaration : :143</i>
     */
    public abstract void viewWillMoveToSuperview(NSView newSuperview);

    /**
     * Original signature : <code>void viewDidMoveToSuperview()</code><br>
     * <i>native declaration : :144</i>
     */
    public abstract void viewDidMoveToSuperview();

    /**
     * Original signature : <code>void didAddSubview(NSView*)</code><br>
     * <i>native declaration : :145</i>
     */
    public abstract void didAddSubview(NSView subview);

    /**
     * Original signature : <code>void willRemoveSubview(NSView*)</code><br>
     * <i>native declaration : :146</i>
     */
    public abstract void willRemoveSubview(NSView subview);

    /**
     * Original signature : <code>void removeFromSuperview()</code><br>
     * <i>native declaration : :147</i>
     */
    public abstract void removeFromSuperview();

    /**
     * Original signature : <code>void replaceSubview(NSView*, NSView*)</code><br>
     * <i>native declaration : :148</i>
     */
    public abstract void replaceSubview_with(NSView oldView, NSView newView);

    /**
     * Original signature : <code>void removeFromSuperviewWithoutNeedingDisplay()</code><br>
     * <i>native declaration : :149</i>
     */
    public abstract void removeFromSuperviewWithoutNeedingDisplay();

    /**
     * Original signature : <code>void setPostsFrameChangedNotifications(BOOL)</code><br>
     * <i>native declaration : :151</i>
     */
    public abstract void setPostsFrameChangedNotifications(boolean flag);

    /**
     * Original signature : <code>BOOL postsFrameChangedNotifications()</code><br>
     * <i>native declaration : :152</i>
     */
    public abstract boolean postsFrameChangedNotifications();
    /**
     * <i>native declaration : :153</i><br>
     * Conversion Error : /// Original signature : <code>void resizeSubviewsWithOldSize(null)</code><br>
     * - (void)resizeSubviewsWithOldSize:(null)oldSize; (Argument oldSize cannot be converted)
     */
    /**
     * <i>native declaration : :154</i><br>
     * Conversion Error : /// Original signature : <code>void resizeWithOldSuperviewSize(null)</code><br>
     * - (void)resizeWithOldSuperviewSize:(null)oldSize; (Argument oldSize cannot be converted)
     */
    /**
     * Original signature : <code>void setAutoresizesSubviews(BOOL)</code><br>
     * <i>native declaration : :155</i>
     */
    public abstract void setAutoresizesSubviews(boolean flag);

    /**
     * Original signature : <code>BOOL autoresizesSubviews()</code><br>
     * <i>native declaration : :156</i>
     */
    public abstract boolean autoresizesSubviews();

    /**
     * Original signature : <code>void setAutoresizingMask(NSUInteger)</code><br>
     * <i>native declaration : :157</i>
     */
    public abstract void setAutoresizingMask(NSUInteger mask);

    /**
     * Original signature : <code>NSUInteger autoresizingMask()</code><br>
     * <i>native declaration : :158</i>
     */
    public abstract NSUInteger autoresizingMask();

    /**
     * <i>native declaration : :160</i><br>
     * Conversion Error : /// Original signature : <code>void setFrameOrigin(null)</code><br>
     * - (void)setFrameOrigin:(null)newOrigin; (Argument newOrigin cannot be converted)
     */
    public abstract void setFrameOrigin(NSPoint origin);

    /**
     * <i>native declaration : :161</i><br>
     * Conversion Error : /// Original signature : <code>void setFrameSize(null)</code><br>
     * - (void)setFrameSize:(null)newSize; (Argument newSize cannot be converted)
     */
    public abstract void setFrameSize(NSSize size);

    /**
     * <i>native declaration : :162</i><br>
     * Conversion Error : NSRect
     */
    public abstract void setFrame(NSRect frame);

    /**
     * <i>native declaration : :163</i><br>
     * Conversion Error : NSRect
     */
    public abstract NSRect frame();

    /**
     * Original signature : <code>void setFrameRotation(CGFloat)</code><br>
     * <i>native declaration : :164</i>
     */
    public abstract void setFrameRotation(CGFloat angle);

    /**
     * Original signature : <code>CGFloat frameRotation()</code><br>
     * <i>native declaration : :165</i>
     */
    public abstract CGFloat frameRotation();

    /**
     * Original signature : <code>void setFrameCenterRotation(CGFloat)</code><br>
     * <i>native declaration : :167</i>
     */
    public abstract void setFrameCenterRotation(CGFloat angle);

    /**
     * Original signature : <code>CGFloat frameCenterRotation()</code><br>
     * <i>native declaration : :168</i>
     */
    public abstract CGFloat frameCenterRotation();
    /**
     * <i>native declaration : :171</i><br>
     * Conversion Error : /// Original signature : <code>void setBoundsOrigin(null)</code><br>
     * - (void)setBoundsOrigin:(null)newOrigin; (Argument newOrigin cannot be converted)
     */
    /**
     * <i>native declaration : :172</i><br>
     * Conversion Error : /// Original signature : <code>void setBoundsSize(null)</code><br>
     * - (void)setBoundsSize:(null)newSize; (Argument newSize cannot be converted)
     */
    /**
     * Original signature : <code>void setBoundsRotation(CGFloat)</code><br>
     * <i>native declaration : :173</i>
     */
    public abstract void setBoundsRotation(CGFloat angle);

    /**
     * Original signature : <code>CGFloat boundsRotation()</code><br>
     * <i>native declaration : :174</i>
     */
    public abstract CGFloat boundsRotation();
    /**
     * <i>native declaration : :175</i><br>
     * Conversion Error : /// Original signature : <code>void translateOriginToPoint(null)</code><br>
     * - (void)translateOriginToPoint:(null)translation; (Argument translation cannot be converted)
     */
    /**
     * <i>native declaration : :176</i><br>
     * Conversion Error : /// Original signature : <code>void scaleUnitSquareToSize(null)</code><br>
     * - (void)scaleUnitSquareToSize:(null)newUnitSize; (Argument newUnitSize cannot be converted)
     */
    /**
     * Original signature : <code>void rotateByAngle(CGFloat)</code><br>
     * <i>native declaration : :177</i>
     */
    public abstract void rotateByAngle(CGFloat angle);
    /**
     * <i>native declaration : :178</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :179</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>BOOL isFlipped()</code><br>
     * <i>native declaration : :181</i>
     */
    public abstract boolean isFlipped();

    /**
     * Original signature : <code>BOOL isRotatedFromBase()</code><br>
     * <i>native declaration : :182</i>
     */
    public abstract boolean isRotatedFromBase();

    /**
     * Original signature : <code>BOOL isRotatedOrScaledFromBase()</code><br>
     * <i>native declaration : :183</i>
     */
    public abstract boolean isRotatedOrScaledFromBase();

    /**
     * Original signature : <code>BOOL isOpaque()</code><br>
     * <i>native declaration : :184</i>
     */
    public abstract boolean isOpaque();

    /**
     * <i>native declaration : :186</i><br>
     * Conversion Error : /// Original signature : <code>convertPoint(null, NSView*)</code><br>
     * - (null)convertPoint:(null)aPoint fromView:(NSView*)aView; (Argument aPoint cannot be converted)
     */
    public abstract NSPoint convertPoint_fromView(NSPoint aPoint, NSView aView);

    /**
     * <i>native declaration : :187</i><br>
     * Conversion Error : /// Original signature : <code>convertPoint(null, NSView*)</code><br>
     * - (null)convertPoint:(null)aPoint toView:(NSView*)aView; (Argument aPoint cannot be converted)
     */
    public abstract NSPoint convertPoint_toView(NSPoint aPoint, NSView aView);
    /**
     * <i>native declaration : :188</i><br>
     * Conversion Error : /// Original signature : <code>convertSize(null, NSView*)</code><br>
     * - (null)convertSize:(null)aSize fromView:(NSView*)aView; (Argument aSize cannot be converted)
     */
    /**
     * <i>native declaration : :189</i><br>
     * Conversion Error : /// Original signature : <code>convertSize(null, NSView*)</code><br>
     * - (null)convertSize:(null)aSize toView:(NSView*)aView; (Argument aSize cannot be converted)
     */
    /**
     * <i>native declaration : :190</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :191</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :192</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :195</i><br>
     * Conversion Error : /// Original signature : <code>convertPointToBase(null)</code><br>
     * - (null)convertPointToBase:(null)aPoint; (Argument aPoint cannot be converted)
     */
    public abstract NSPoint convertPointToBase(NSPoint aPoint);

    /**
     * <i>native declaration : :196</i><br>
     * Conversion Error : /// Original signature : <code>convertPointFromBase(null)</code><br>
     * - (null)convertPointFromBase:(null)aPoint; (Argument aPoint cannot be converted)
     */
    public abstract NSPoint convertPointFromBase(NSPoint aPoint);

    /**
     * <i>native declaration : :197</i><br>
     * Conversion Error : /// Original signature : <code>convertSizeToBase(null)</code><br>
     * - (null)convertSizeToBase:(null)aSize; (Argument aSize cannot be converted)
     */
    public abstract NSSize convertSizeToBase(NSSize aSize);

    /**
     * <i>native declaration : :198</i><br>
     * Conversion Error : /// Original signature : <code>convertSizeFromBase(null)</code><br>
     * - (null)convertSizeFromBase:(null)aSize; (Argument aSize cannot be converted)
     */
    public abstract NSSize convertSizeFromBase(NSSize aSize);
    /**
     * <i>native declaration : :199</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :200</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>BOOL canDraw()</code><br>
     * <i>native declaration : :203</i>
     */
    public abstract boolean canDraw();

    /**
     * Original signature : <code>void setNeedsDisplay(BOOL)</code><br>
     * <i>native declaration : :204</i>
     */
    public abstract void setNeedsDisplay(boolean flag);
    /**
     * <i>native declaration : :205</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>BOOL needsDisplay()</code><br>
     * <i>native declaration : :206</i>
     */
    public abstract boolean needsDisplay();

    /**
     * Original signature : <code>void lockFocus()</code><br>
     * <i>native declaration : :207</i>
     */
    public abstract void lockFocus();

    /**
     * Original signature : <code>void unlockFocus()</code><br>
     * <i>native declaration : :208</i>
     */
    public abstract void unlockFocus();

    /**
     * Original signature : <code>BOOL lockFocusIfCanDraw()</code><br>
     * <i>native declaration : :209</i>
     */
    public abstract boolean lockFocusIfCanDraw();

    /**
     * Original signature : <code>BOOL lockFocusIfCanDrawInContext(NSGraphicsContext*)</code><br>
     * <i>native declaration : :211</i>
     */
    public abstract boolean lockFocusIfCanDrawInContext(com.sun.jna.Pointer context);
    /**
     * <i>native declaration : :214</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void display()</code><br>
     * <i>native declaration : :216</i>
     */
    public abstract void display();

    /**
     * Original signature : <code>void displayIfNeeded()</code><br>
     * <i>native declaration : :217</i>
     */
    public abstract void displayIfNeeded();

    /**
     * Original signature : <code>void displayIfNeededIgnoringOpacity()</code><br>
     * <i>native declaration : :218</i>
     */
    public abstract void displayIfNeededIgnoringOpacity();
    /**
     * <i>native declaration : :219</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :220</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :221</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :222</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :223</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :225</i><br>
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
     * Original signature : <code>void viewWillDraw()</code><br>
     * <i>native declaration : :231</i>
     */
    public abstract void viewWillDraw();

    /**
     * Original signature : <code>NSInteger gState()</code><br>
     * <i>native declaration : :234</i>
     */
    public abstract int gState();

    /**
     * Original signature : <code>void allocateGState()</code><br>
     * <i>native declaration : :235</i>
     */
    public abstract void allocateGState();

    /**
     * Original signature : <code>void releaseGState()</code><br>
     * <i>native declaration : :236</i>
     */
    public abstract void releaseGState();

    /**
     * Original signature : <code>void setUpGState()</code><br>
     * <i>native declaration : :237</i>
     */
    public abstract void setUpGState();

    /**
     * Original signature : <code>void renewGState()</code><br>
     * <i>native declaration : :238</i>
     */
    public abstract void renewGState();
    /**
     * <i>native declaration : :240</i><br>
     * Conversion Error : /// Original signature : <code>void scrollPoint(null)</code><br>
     * - (void)scrollPoint:(null)aPoint; (Argument aPoint cannot be converted)
     */
    /**
     * <i>native declaration : :241</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>BOOL autoscroll(NSEvent*)</code><br>
     * <i>native declaration : :242</i>
     */
    public abstract boolean autoscroll(NSEvent event);
    /**
     * <i>native declaration : :243</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :244</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :246</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :249</i><br>
     */
    public abstract NSView hitTest(NSPoint point);

    /**
     * <i>native declaration : :250</i><br>
     * Conversion Error : /// Original signature : <code>BOOL mouse(null, NSRect)</code><br>
     * - (BOOL)mouse:(null)aPoint inRect:(NSRect)aRect; (Argument aPoint cannot be converted)
     */
    /**
     * Original signature : <code>id viewWithTag(NSInteger)</code><br>
     * <i>native declaration : :251</i>
     */
    public abstract NSView viewWithTag(int aTag);

    /**
     * Original signature : <code>NSInteger tag()</code><br>
     * <i>native declaration : :252</i>
     */
    public abstract int tag();

    /**
     * Original signature : <code>BOOL acceptsFirstMouse(NSEvent*)</code><br>
     * <i>native declaration : :254</i>
     */
    public abstract boolean acceptsFirstMouse(NSEvent event);

    /**
     * Original signature : <code>BOOL shouldDelayWindowOrderingForEvent(NSEvent*)</code><br>
     * <i>native declaration : :255</i>
     */
    public abstract boolean shouldDelayWindowOrderingForEvent(NSEvent event);

    /**
     * Original signature : <code>BOOL needsPanelToBecomeKey()</code><br>
     * <i>native declaration : :256</i>
     */
    public abstract boolean needsPanelToBecomeKey();

    /**
     * Original signature : <code>BOOL mouseDownCanMoveWindow()</code><br>
     * <i>native declaration : :258</i>
     */
    public abstract boolean mouseDownCanMoveWindow();
    /**
     * <i>native declaration : :261</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :262</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void discardCursorRects()</code><br>
     * <i>native declaration : :263</i>
     */
    public abstract void discardCursorRects();

    /**
     * Original signature : <code>void resetCursorRects()</code><br>
     * <i>native declaration : :264</i>
     */
    public abstract void resetCursorRects();
    /**
     * <i>native declaration : :266</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void removeTrackingRect(NSTrackingRectTag)</code><br>
     * <i>native declaration : :267</i>
     */
    public abstract void removeTrackingRect(int tag);

    /**
     * Original signature : <code>void setWantsLayer(BOOL)</code><br>
     * <i>native declaration : :270</i>
     */
    public abstract void setWantsLayer(boolean flag);

    /**
     * Original signature : <code>BOOL wantsLayer()</code><br>
     * <i>native declaration : :271</i>
     */
    public abstract boolean wantsLayer();

    /**
     * Original signature : <code>void setLayer(CALayer*)</code><br>
     * <i>native declaration : :273</i>
     */
    public abstract void setLayer(com.sun.jna.Pointer newLayer);

    /**
     * Original signature : <code>CALayer* layer()</code><br>
     * <i>native declaration : :274</i>
     */
    public abstract com.sun.jna.Pointer layer();

    public abstract void setLayerContentsRedrawPolicy(int policy);

    public abstract int layerContentsRedrawPolicy();

    /**
     * Original signature : <code>void setAlphaValue(CGFloat)</code><br>
     * <i>native declaration : :276</i>
     */
    public abstract void setAlphaValue(CGFloat viewAlpha);

    /**
     * Original signature : <code>CGFloat alphaValue()</code><br>
     * <i>native declaration : :277</i>
     */
    public abstract CGFloat alphaValue();

    /**
     * Original signature : <code>void setBackgroundFilters(NSArray*)</code><br>
     * <i>native declaration : :279</i>
     */
    public abstract void setBackgroundFilters(NSArray filters);

    /**
     * Original signature : <code>NSArray* backgroundFilters()</code><br>
     * <i>native declaration : :280</i>
     */
    public abstract NSArray backgroundFilters();

    /**
     * Original signature : <code>void setCompositingFilter(CIFilter*)</code><br>
     * <i>native declaration : :282</i>
     */
    public abstract void setCompositingFilter(com.sun.jna.Pointer filter);

    /**
     * Original signature : <code>CIFilter* compositingFilter()</code><br>
     * <i>native declaration : :283</i>
     */
    public abstract com.sun.jna.Pointer compositingFilter();

    /**
     * Original signature : <code>void setContentFilters(NSArray*)</code><br>
     * <i>native declaration : :285</i>
     */
    public abstract void setContentFilters(com.sun.jna.Pointer filters);

    /**
     * Original signature : <code>NSArray* contentFilters()</code><br>
     * <i>native declaration : :286</i>
     */
    public abstract com.sun.jna.Pointer contentFilters();

    /**
     * Original signature : <code>void setShadow(NSShadow*)</code><br>
     * <i>native declaration : :288</i>
     */
    public abstract void setShadow(com.sun.jna.Pointer shadow);

    /**
     * Original signature : <code>NSShadow* shadow()</code><br>
     * <i>native declaration : :289</i>
     */
    public abstract com.sun.jna.Pointer shadow();

    /**
     * The following methods are meant to be invoked, and probably don't need to be overridden<br>
     * Original signature : <code>void addTrackingArea(NSTrackingArea*)</code><br>
     * <i>native declaration : :293</i>
     */
    public abstract void addTrackingArea(com.sun.jna.Pointer trackingArea);

    /**
     * Original signature : <code>void removeTrackingArea(NSTrackingArea*)</code><br>
     * <i>native declaration : :294</i>
     */
    public abstract void removeTrackingArea(com.sun.jna.Pointer trackingArea);

    /**
     * Original signature : <code>NSArray* trackingAreas()</code><br>
     * <i>native declaration : :295</i>
     */
    public abstract NSArray trackingAreas();

    /**
     * updateTrackingAreas should be overridden to remove out of date tracking areas and add recomputed tracking areas, and should call super.<br>
     * Original signature : <code>void updateTrackingAreas()</code><br>
     * <i>native declaration : :299</i>
     */
    public abstract void updateTrackingAreas();

    /**
     * Original signature : <code>BOOL shouldDrawColor()</code><br>
     * <i>native declaration : :303</i>
     */
    public abstract boolean shouldDrawColor();

    /**
     * Original signature : <code>void setPostsBoundsChangedNotifications(BOOL)</code><br>
     * <i>native declaration : :305</i>
     */
    public abstract void setPostsBoundsChangedNotifications(boolean flag);

    /**
     * Original signature : <code>BOOL postsBoundsChangedNotifications()</code><br>
     * <i>native declaration : :306</i>
     */
    public abstract boolean postsBoundsChangedNotifications();

    /**
     * Original signature : <code>NSScrollView* enclosingScrollView()</code><br>
     * <i>native declaration : :308</i>
     */
    public abstract NSView enclosingScrollView();

    /**
     * Original signature : <code>NSMenu* menuForEvent(NSEvent*)</code><br>
     * <i>native declaration : :310</i>
     */
    public abstract NSMenu menuForEvent(NSEvent event);

    /**
     * Original signature : <code>void setToolTip(NSString*)</code><br>
     * <i>native declaration : :313</i>
     */
    public abstract void setToolTip(String string);

    /**
     * Original signature : <code>NSString* toolTip()</code><br>
     * <i>native declaration : :314</i>
     */
    public abstract String toolTip();
    /**
     * <i>native declaration : :315</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void removeToolTip(NSToolTipTag)</code><br>
     * <i>native declaration : :316</i>
     */
    public abstract void removeToolTip(int tag);

    /**
     * Original signature : <code>void removeAllToolTips()</code><br>
     * <i>native declaration : :317</i>
     */
    public abstract void removeAllToolTips();

    /**
     * a view receives viewWillStartLiveResize before the frame is first changed for a live resize<br>
     * Original signature : <code>void viewWillStartLiveResize()</code><br>
     * <i>native declaration : :321</i>
     */
    public abstract void viewWillStartLiveResize();

    /**
     * a view receives viewWillEndLiveResize after the frame is last changed for a live resize<br>
     * Original signature : <code>void viewDidEndLiveResize()</code><br>
     * <i>native declaration : :323</i>
     */
    public abstract void viewDidEndLiveResize();

    /**
     * inLiveResize can be called from drawRect: to decide between cheap and full drawing<br>
     * Original signature : <code>BOOL inLiveResize()</code><br>
     * <i>native declaration : :325</i>
     */
    public abstract boolean inLiveResize();

    /**
     * A view that returns YES for -preservesContentDuringLiveResize is responsible for invalidating its own dirty rects during live resize<br>
     * Original signature : <code>BOOL preservesContentDuringLiveResize()</code><br>
     * <i>native declaration : :328</i>
     */
    public abstract boolean preservesContentDuringLiveResize();
    /**
     * <i>native declaration : :330</i><br>
     * Conversion Error : NSRect
     */

    /**
     * On return from -getRectsExposedDuringLiveResize, exposedRects indicates the parts of the view that are newly exposed (at most 4 rects).  *count indicates how many rects are in the exposedRects list<br>
     * Original signature : <code>void getRectsExposedDuringLiveResize(NSRect[], NSInteger*)</code><br>
     * <i>native declaration : :332</i>
     */
    public abstract void getRectsExposedDuringLiveResize_count(com.sun.jna.Pointer exposedRects, java.nio.IntBuffer count);

    /**
     * Original signature : <code>BOOL performMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :341</i>
     */
    public abstract boolean performMnemonic(com.sun.jna.Pointer theString);

    /**
     * Original signature : <code>void setNextKeyView(NSView*)</code><br>
     * <i>from NSKeyboardUI native declaration : :342</i>
     */
    public abstract void setNextKeyView(NSView next);

    /**
     * Original signature : <code>NSView* nextKeyView()</code><br>
     * <i>from NSKeyboardUI native declaration : :343</i>
     */
    public abstract NSView nextKeyView();

    /**
     * Original signature : <code>NSView* previousKeyView()</code><br>
     * <i>from NSKeyboardUI native declaration : :344</i>
     */
    public abstract NSView previousKeyView();

    /**
     * Original signature : <code>NSView* nextValidKeyView()</code><br>
     * <i>from NSKeyboardUI native declaration : :345</i>
     */
    public abstract NSView nextValidKeyView();

    /**
     * Original signature : <code>NSView* previousValidKeyView()</code><br>
     * <i>from NSKeyboardUI native declaration : :346</i>
     */
    public abstract NSView previousValidKeyView();

    /**
     * Original signature : <code>BOOL canBecomeKeyView()</code><br>
     * <i>from NSKeyboardUI native declaration : :349</i>
     */
    public abstract boolean canBecomeKeyView();
    /**
     * <i>from NSKeyboardUI native declaration : :352</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSKeyboardUI native declaration : :355</i><br>
     * Conversion Error : /// Original signature : <code>void setFocusRingType(null)</code><br>
     */
    public abstract void setFocusRingType(int focusRingType);

    /**
     * Original signature : <code>focusRingType()</code><br>
     * <i>from NSKeyboardUI native declaration : :356</i>
     */
    public abstract int focusRingType();
    /**
     * <i>from NSPrinting native declaration : :364</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSPrinting native declaration : :365</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSPrinting native declaration : :366</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSPrinting native declaration : :367</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Printing action method (Note fax: is obsolete)<br>
     * Original signature : <code>void print(id)</code><br>
     * <i>from NSPrinting native declaration : :370</i>
     */
    public abstract void print(final ID sender);
    /**
     * <i>from NSPrinting native declaration : :373</i><br>
     * Conversion Error : /**<br>
     *  * Pagination<br>
     *  * Original signature : <code>BOOL knowsPageRange(null)</code><br>
     *  * /<br>
     * - (BOOL)knowsPageRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>CGFloat heightAdjustLimit()</code><br>
     * <i>from NSPrinting native declaration : :374</i>
     */
    public abstract CGFloat heightAdjustLimit();

    /**
     * Original signature : <code>CGFloat widthAdjustLimit()</code><br>
     * <i>from NSPrinting native declaration : :375</i>
     */
    public abstract CGFloat widthAdjustLimit();

    /**
     * Original signature : <code>void adjustPageWidthNew(CGFloat*, CGFloat, CGFloat, CGFloat)</code><br>
     * <i>from NSPrinting native declaration : :376</i>
     */
    public abstract void adjustPageWidthNew_left_right_limit(java.nio.FloatBuffer newRight, CGFloat oldLeft, CGFloat oldRight, CGFloat rightLimit);

    /**
     * Original signature : <code>void adjustPageHeightNew(CGFloat*, CGFloat, CGFloat, CGFloat)</code><br>
     * <i>from NSPrinting native declaration : :377</i>
     */
    public abstract void adjustPageHeightNew_top_bottom_limit(java.nio.FloatBuffer newBottom, CGFloat oldTop, CGFloat oldBottom, CGFloat bottomLimit);
    /**
     * <i>from NSPrinting native declaration : :378</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSPrinting native declaration : :379</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSPrinting native declaration : :380</i><br>
     * Conversion Error : /// Original signature : <code>void drawPageBorderWithSize(null)</code><br>
     * - (void)drawPageBorderWithSize:(null)borderSize; (Argument borderSize cannot be converted)
     */
    /**
     * Original signature : <code>NSAttributedString* pageHeader()</code><br>
     * <i>from NSPrinting native declaration : :382</i>
     */
    public abstract NSAttributedString pageHeader();

    /**
     * Original signature : <code>NSAttributedString* pageFooter()</code><br>
     * <i>from NSPrinting native declaration : :383</i>
     */
    public abstract NSAttributedString pageFooter();
    /**
     * <i>from NSPrinting native declaration : :387</i><br>
     * Conversion Error : /**<br>
     *  * This method is obsolete.  It will never be invoked from within AppKit, and NSView's implementation of it does nothing. **<br>
     *  * Original signature : <code>void drawSheetBorderWithSize(null)</code><br>
     *  * /<br>
     * - (void)drawSheetBorderWithSize:(null)borderSize; (Argument borderSize cannot be converted)
     */
    /**
     * Returns print job title. Default implementation first tries its window's NSDocument (displayName), then window's title<br>
     * Original signature : <code>NSString* printJobTitle()</code><br>
     * <i>from NSPrinting native declaration : :391</i>
     */
    public abstract String printJobTitle();

    /**
     * Original signature : <code>void beginDocument()</code><br>
     * <i>from NSPrinting native declaration : :392</i>
     */
    public abstract void beginDocument();

    /**
     * Original signature : <code>void endDocument()</code><br>
     * <i>from NSPrinting native declaration : :393</i>
     */
    public abstract void endDocument();
    /**
     * <i>from NSPrinting native declaration : :395</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void endPage()</code><br>
     * <i>from NSPrinting native declaration : :396</i>
     */
    public abstract void endPage();
    /**
     * <i>from NSDrag native declaration : :401</i><br>
     * Conversion Error : /// Original signature : <code>void dragImage(NSImage*, null, null, NSEvent*, NSPasteboard*, id, BOOL)</code><br>
     * - (void)dragImage:(NSImage*)anImage at:(null)viewLocation offset:(null)initialOffset event:(NSEvent*)event pasteboard:(NSPasteboard*)pboard source:(id)sourceObj slideBack:(BOOL)slideFlag; (Argument viewLocation cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* registeredDraggedTypes()</code><br>
     * <i>from NSDrag native declaration : :404</i>
     */
    public abstract NSArray registeredDraggedTypes();

    /**
     * Original signature : <code>void registerForDraggedTypes(NSArray*)</code><br>
     * <i>from NSDrag native declaration : :406</i>
     */
    public abstract void registerForDraggedTypes(NSArray types);

    /**
     * Original signature : <code>void unregisterDraggedTypes()</code><br>
     * <i>from NSDrag native declaration : :407</i>
     */
    public abstract void unregisterDraggedTypes();

    /**
     * <i>from NSDrag native declaration : :409</i><br>
     * Conversion Error : NSRect
     */
    public abstract boolean dragFile_fromRect_slideBack_event(String filename, NSRect rect, boolean slideBack, NSEvent event);

    /**
     * <i>from NSDrag native declaration : :411</i><br>
     * Conversion Error : NSRect
     */
    public abstract boolean dragPromisedFilesOfTypes_fromRect_source_slideBack_event(
            NSArray typeArray, NSRect rect, org.rococoa.ID sourceObject, boolean slideBack, NSEvent event);

    public boolean dragPromisedFilesOfTypes(
            NSArray typeArray, NSRect rect, org.rococoa.ID sourceObject, boolean slideBack, NSEvent event) {
        return this.dragPromisedFilesOfTypes_fromRect_source_slideBack_event(typeArray, rect, sourceObject, slideBack, event);
    }

    /**
     * Original signature : <code>BOOL enterFullScreenMode(NSScreen*, NSDictionary*)</code><br>
     * <i>from NSFullScreenMode native declaration : :417</i>
     */
    public abstract boolean enterFullScreenMode_withOptions(com.sun.jna.Pointer screen, com.sun.jna.Pointer options);

    /**
     * Original signature : <code>void exitFullScreenModeWithOptions(NSDictionary*)</code><br>
     * <i>from NSFullScreenMode native declaration : :418</i>
     */
    public abstract void exitFullScreenModeWithOptions(NSDictionary options);

    /**
     * Original signature : <code>BOOL isInFullScreenMode()</code><br>
     * <i>from NSFullScreenMode native declaration : :419</i>
     */
    public abstract boolean isInFullScreenMode();
}
