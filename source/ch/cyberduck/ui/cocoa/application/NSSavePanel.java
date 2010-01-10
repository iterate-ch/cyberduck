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
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;

/// <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:30</i>
public abstract class NSSavePanel extends NSPanel {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSSavePanel", _Class.class);

    public static NSSavePanel savePanel() {
        return CLASS.savePanel();
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSSavePanel* savePanel()</code><br>
         * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:67</i>
         */
        NSSavePanel savePanel();
    }

    /**
     * Original signature : <code>NSURL* URL()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:69</i>
     */
    public abstract NSURL URL();

    /**
     * Original signature : <code>NSString* filename()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:71</i>
     */
    public abstract String filename();

    /**
     * Original signature : <code>NSString* directory()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:73</i>
     */
    public abstract String directory();

    /**
     * Original signature : <code>void setDirectory(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:74</i>
     */
    public abstract void setDirectory(String path);

    /**
     * A file specified in the save panel is saved with the designated filename and this file type as an extension. This method is equivalent to calling allowedFileTypes and returning the first element of the list of allowed types, or nil if there are none.  It is preferred to use 'allowedFileTypes' over this method.<br>
     * Original signature : <code>NSString* requiredFileType()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:78</i>
     */
    public abstract String requiredFileType();

    /**
     * Original signature : <code>void setRequiredFileType(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:79</i>
     */
    public abstract void setRequiredFileType(String type);

    /**
     * An array NSStrings specifying the file types the user can save the file as. The fil type can be a common file extension, or a UTI. A nil value indicates that any file type can be used. If the array is not nil and the array contains no items, an exception will be raised. If the user specifies a type not in the array, and 'allowsOtherFileTypes' is YES, they will be presented with another dialog when prompted to save. The default value is 'nil'.<br>
     * Original signature : <code>NSArray* allowedFileTypes()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:84</i>
     */
    public abstract NSArray allowedFileTypes();

    /**
     * Original signature : <code>void setAllowedFileTypes(NSArray*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:85</i>
     */
    public abstract void setAllowedFileTypes(NSArray types);

    /**
     * Returns a BOOL value that indicates whether the receiver allows the user to save files with an extension that‚Äôs not in the list of 'allowedFileTypes'.<br>
     * Original signature : <code>BOOL allowsOtherFileTypes()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:89</i>
     */
    public abstract boolean allowsOtherFileTypes();

    /**
     * Original signature : <code>void setAllowsOtherFileTypes(BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:90</i>
     */
    public abstract void setAllowsOtherFileTypes(boolean flag);

    /**
     * Original signature : <code>NSView* accessoryView()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:93</i>
     */
    public abstract NSView accessoryView();

    /**
     * Original signature : <code>void setAccessoryView(NSView*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:94</i>
     */
    public abstract void setAccessoryView(NSView view);

    /**
     * Original signature : <code>BOOL isExpanded()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:101</i>
     */
    public abstract boolean isExpanded();

    /**
     * Original signature : <code>BOOL canCreateDirectories()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:104</i>
     */
    public abstract boolean canCreateDirectories();

    /**
     * Original signature : <code>void setCanCreateDirectories(BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:105</i>
     */
    public abstract void setCanCreateDirectories(boolean flag);

    /**
     * Original signature : <code>BOOL canSelectHiddenExtension()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:109</i>
     */
    public abstract boolean canSelectHiddenExtension();

    /**
     * Original signature : <code>void setCanSelectHiddenExtension(BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:111</i>
     */
    public abstract void setCanSelectHiddenExtension(boolean flag);

    /**
     * Original signature : <code>BOOL isExtensionHidden()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:112</i>
     */
    public abstract boolean isExtensionHidden();

    /**
     * Original signature : <code>void setExtensionHidden(BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:113</i>
     */
    public abstract void setExtensionHidden(boolean flag);

    /**
     * Original signature : <code>BOOL treatsFilePackagesAsDirectories()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:115</i>
     */
    public abstract boolean treatsFilePackagesAsDirectories();

    /**
     * Original signature : <code>void setTreatsFilePackagesAsDirectories(BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:116</i>
     */
    public abstract void setTreatsFilePackagesAsDirectories(boolean flag);

    /**
     * Original signature : <code>NSString* prompt()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:118</i>
     */
    public abstract String prompt();

    /**
     * Original signature : <code>void setPrompt(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:119</i>
     */
    public abstract void setPrompt(String prompt);

    /**
     * Original signature : <code>NSString* nameFieldLabel()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:125</i>
     */
    public abstract String nameFieldLabel();

    /**
     * Original signature : <code>void setNameFieldLabel(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:126</i>
     */
    public abstract void setNameFieldLabel(String label);

    /**
     * Original signature : <code>NSString* message()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:130</i>
     */
    public abstract String message();

    /**
     * Original signature : <code>void setMessage(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:131</i>
     */
    public abstract void setMessage(String message);

    /**
     * Original signature : <code>void validateVisibleColumns()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:134</i>
     */
    public abstract void validateVisibleColumns();

    /**
     * A method that was deprecated in Mac OS 10.3.  -[NSSavePanel selectText:] does nothing.<br>
     * Original signature : <code>void selectText(id)</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:138</i>
     */
    public abstract void selectText(final ID sender);

    /**
     * Original signature : <code>void ok(id)</code><br>
     * <i>from NSSavePanelRuntime native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:145</i>
     */
    public abstract void ok(final ID sender);

    /**
     * Original signature : <code>void cancel(id)</code><br>
     * <i>from NSSavePanelRuntime native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:146</i>
     */
    public abstract void cancel(final ID sender);

    /**
     * <i>from NSSavePanelRuntime native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:152</i><br>
     * Conversion Error : /**<br>
     * * didEndSelector should have the signature:<br>
     * * - (void)savePanelDidEnd:(NSSavePanel *)sheet returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo;<br>
     * * The value passed as returnCode will be either NSCancelButton or NSOKButton.<br>
     * * Original signature : <code>void beginSheetForDirectory(NSString*, NSString*, NSWindow*, id, null, void*)</code><br>
     * * /<br>
     * - (void)beginSheetForDirectory:(NSString*)path file:(NSString*)name modalForWindow:(NSWindow*)docWindow modalDelegate:(id)delegate didEndSelector:(null)didEndSelector contextInfo:(void*)contextInfo; (Argument didEndSelector cannot be converted)
     */
    public abstract void beginSheetForDirectory_file_modalForWindow_modalDelegate_didEndSelector_contextInfo(String path, String name, NSWindow docWindow, ID delegate, org.rococoa.Selector didEndSelector, ID contextInfo);

    public void beginSheetForDirectory(String path, String name, NSWindow docWindow, ID delegate, org.rococoa.Selector didEndSelector, ID contextInfo) {
        this.beginSheetForDirectory_file_modalForWindow_modalDelegate_didEndSelector_contextInfo(path, name, docWindow, delegate, didEndSelector, contextInfo);
    }

    /**
     * Original signature : <code>NSInteger runModalForDirectory(NSString*, NSString*)</code><br>
     * <i>from NSSavePanelRuntime native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:154</i>
     */
    public abstract NSInteger runModalForDirectory_file(String path, String name);

    /**
     * Original signature : <code>NSInteger runModal()</code><br>
     * <i>from NSSavePanelRuntime native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSSavePanel.h:155</i>
     */
    public abstract NSInteger runModal();
}
