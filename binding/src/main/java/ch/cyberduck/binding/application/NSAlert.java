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
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.StringAppender;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSError;

/// <i>native declaration : :20</i>
public abstract class NSAlert extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSAlert", _Class.class);

    public static final int NSAlertFirstButtonReturn = 1000;
    public static final int NSAlertSecondButtonReturn = 1001;
    public static final int NSAlertThirdButtonReturn = 1002;

    /// <i>native declaration : :54</i>
    public static final int NSAlertDefaultReturn = 1;
    /// <i>native declaration : :55</i>
    public static final int NSAlertAlternateReturn = 0;
    /// <i>native declaration : :56</i>
    public static final int NSAlertOtherReturn = -1;
    /// <i>native declaration : :57</i>
    public static final int NSAlertErrorReturn = -2;

    /// <i>native declaration : line 12</i>
    public static final int NSWarningAlertStyle = 0;
    /// <i>native declaration : line 13</i>
    public static final int NSInformationalAlertStyle = 1;
    /// <i>native declaration : line 14</i>
    public static final int NSCriticalAlertStyle = 2;

    public static NSAlert alert() {
        return CLASS.alloc().init();
    }

    public static NSAlert alertWithError(NSError error) {
        return CLASS.alertWithError(error);
    }

    public static NSAlert alert(
            String title, String message, String defaultButton, String alternateButton, String otherButton) {
        NSAlert alert = NSAlert.alert();
        alert.setMessageText(title);
        alert.setInformativeText(new StringAppender().append(message).toString());
        if(StringUtils.isNotBlank(defaultButton)) {
            // OK
            alert.addButtonWithTitle(defaultButton);
        }
        if(StringUtils.isNotBlank(otherButton)) {
            // Cancel
            alert.addButtonWithTitle(otherButton);
        }
        if(StringUtils.isNotBlank(alternateButton)) {
            alert.addButtonWithTitle(alternateButton);
        }
        return alert;
    }

    public interface _Class extends ObjCClass {
        NSAlert alloc();

        /**
         * Given an NSError, create an NSAlert that can be used to present the error to the user. The error's localized description, recovery suggestion, and recovery options will be used to set the alert's message text, informative text, and button titles, respectively.<br>
         * Original signature : <code>NSAlert* alertWithError(NSError*)</code><br>
         * <i>native declaration : :60</i>
         */
        NSAlert alertWithError(NSError error);

        /**
         * the following class method is for use by apps migrating from the C-based API.  Note that this returns an NSAlert that is equivalent to the one created in NSRunAlertPanel, so the layout, button return values, and key equivalents are the same as for the C-based API.<br>
         * Original signature : <code>NSAlert* alertWithMessageText(NSString*, NSString*, NSString*, NSString*, NSString*, null)</code><br>
         * <i>native declaration : :65</i>
         */
        NSAlert alertWithMessageText_defaultButton_alternateButton_otherButton_informativeTextWithFormat(
                String message, String defaultButton, String alternateButton, String otherButton, String format);
    }

    public abstract NSAlert init();

    /**
     * Original signature : <code>void setMessageText(NSString*)</code><br>
     * <i>native declaration : :67</i>
     */
    public abstract void setMessageText(String messageText);

    /**
     * Original signature : <code>void setInformativeText(NSString*)</code><br>
     * <i>native declaration : :68</i>
     */
    public abstract void setInformativeText(String informativeText);

    /**
     * Original signature : <code>NSString* messageText()</code><br>
     * <i>native declaration : :70</i>
     */
    public abstract String messageText();

    /**
     * Original signature : <code>NSString* informativeText()</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract String informativeText();

    /**
     * customize the icon.  By default uses the image named NSApplicationIcon<br>
     * Original signature : <code>void setIcon(NSImage*)</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract void setIcon(NSImage icon);

    /**
     * Original signature : <code>NSImage* icon()</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract NSImage icon();

    /**
     * buttons are added from right to left (for left to right languages)<br>
     * Original signature : <code>NSButton* addButtonWithTitle(NSString*)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract NSButton addButtonWithTitle(String title);

    /**
     * get the buttons, where the rightmost button is at index 0<br>
     * Original signature : <code>NSArray* buttons()</code><br>
     * <i>native declaration : :81</i>
     */
    public abstract NSArray buttons();

    /**
     * -setShowsHelp:YES adds a help button to the alert panel. When the help button is pressed, the delegate is first consulted.  If the delegate does not implement alertShowHelp: or returns NO, then -[NSHelpManager openHelpAnchor:inBook:] is called with a nil book and the anchor specified by -setHelpAnchor:, if any.  An exception will be raised if the delegate returns NO and there is no help anchor set.<br>
     * Original signature : <code>void setShowsHelp(BOOL)</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract void setShowsHelp(boolean showsHelp);

    /**
     * Original signature : <code>BOOL showsHelp()</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract boolean showsHelp();

    /**
     * Original signature : <code>void setHelpAnchor(NSString*)</code><br>
     * <i>native declaration : :102</i>
     */
    public abstract void setHelpAnchor(String anchor);

    /**
     * Original signature : <code>NSString* helpAnchor()</code><br>
     * <i>native declaration : :103</i>
     */
    public abstract String helpAnchor();

    /**
     * Original signature : <code>void setAlertStyle(NSAlertStyle)</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract void setAlertStyle(int style);

    /**
     * Original signature : <code>NSAlertStyle alertStyle()</code><br>
     * <i>native declaration : :106</i>
     */
    public abstract int alertStyle();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract void setDelegate(org.rococoa.ID delegate);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :109</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * -setShowsSuppressionButton: indicates whether or not the alert should contain a suppression checkbox.  The default is NO.  This checkbox is typically used to give the user an option to not show this alert again.  If shown, the suppression button will have a default localized title similar to @"Do not show this message again".  You can customize this title using [[alert suppressionButton] setTitle:].  When the alert is dismissed, you can get the state of the suppression button, using [[alert suppressionButton] state] and store the result in user defaults, for example.  This setting can then be checked before showing the alert again.  By default, the suppression button is positioned below the informative text, and above the accessory view (if any) and the alert buttons, and left-aligned with the informative text.  However do not count on the placement of this button, since it might be moved if the alert panel user interface is changed in the future. If you need a checkbox for purposes other than suppression text, it is recommended you create your own using an accessory view.<br>
     * Original signature : <code>void setShowsSuppressionButton(BOOL)</code><br>
     * <i>native declaration : :114</i>
     */
    public abstract void setShowsSuppressionButton(boolean flag);

    /**
     * Original signature : <code>BOOL showsSuppressionButton()</code><br>
     * <i>native declaration : :115</i>
     */
    public abstract boolean showsSuppressionButton();

    /**
     * -suppressionButton returns a suppression button which may be customized, including the title and the initial state.  You can also use this method to get the state of the button after the alert is dismissed, which may be stored in user defaults and checked before showing the alert again.  In order to show the suppression button in the alert panel, you must call -setShowsSuppressionButton:YES.<br>
     * Original signature : <code>NSButton* suppressionButton()</code><br>
     * <i>native declaration : :119</i>
     */
    public abstract NSButton suppressionButton();

    /**
     * -setAccessoryView: sets the accessory view displayed in the alert panel.  By default, the accessory view is positioned below the informative text and the suppression button (if any) and above the alert buttons, left-aligned with the informative text.  If you want to customize the location of the accessory view, you must first call -layout.  See the discussion of -layout for more information.<br>
     * Original signature : <code>void setAccessoryView(NSView*)</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract void setAccessoryView(NSView view);

    /**
     * Original signature : <code>NSView* accessoryView()</code><br>
     * <i>native declaration : :125</i>
     */
    public abstract NSView accessoryView();

    /**
     * -layout can be used to indicate that the alert panel should do immediate layout, overriding the default behavior of laying out lazily just before showing panel.  You should only call this method if you want to do your own custom layout after it returns.  You should call this method only after you have finished with NSAlert customization, including setting message and informative text, and adding buttons and an accessory view if needed.  You can make layout changes after this method returns, in particular to adjust the frame of an accessory view.  Note that the standard layout of the alert may change in the future, so layout customization should be done with caution.<br>
     * Original signature : <code>void layout()</code><br>
     * <i>native declaration : :129</i>
     */
    public abstract void layout();

    /**
     * Run the alert as an application-modal panel and return the result<br>
     * Original signature : <code>NSInteger runModal()</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract int runModal();

    /**
     * Original signature : <code>void beginSheetModalForWindow(NSWindow*, id, SEL, void*)</code><br>
     * <i>native declaration : :139</i>
     */
    public abstract void beginSheetModalForWindow_modalDelegate_didEndSelector_contextInfo(NSWindow window, org.rococoa.ID delegate, org.rococoa.Selector didEndSelector, ID contextInfo);

    public void beginSheet(NSWindow window, org.rococoa.ID delegate, org.rococoa.Selector didEndSelector, ID contextInfo) {
        this.beginSheetModalForWindow_modalDelegate_didEndSelector_contextInfo(window, delegate, didEndSelector, contextInfo);
    }

    /**
     * return the application-modal panel or the document-modal sheet corresponding to this alert<br>
     * Original signature : <code>id window()</code><br>
     * <i>native declaration : :142</i>
     */
    public abstract NSWindow window();
}
