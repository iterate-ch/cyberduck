package ch.cyberduck.core.keychain;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.application.NSPanel;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSArray;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;
import org.rococoa.Selector;

import com.sun.jna.ptr.PointerByReference;

public abstract class SFCertificatePanel extends NSPanel {

    /**
     * Use this method if your application displays a single certificate panel or sheet at a time. If your application
     * can display multiple certificate panels or sheets at once, you must allocate separate object instances (using the
     * alloc class method inherited from NSObject) and initialize them (using the init instance method, also inherited
     * from NSObject) instead of using this class method.
     *
     * @return Returns a fully initialized, singleton certificate panel object.
     */
    public static SFCertificatePanel sharedCertificatePanel() {
        return Rococoa.createClass("SFCertificatePanel", SFCertificatePanel._Class.class).sharedCertificatePanel();
    }


    public interface _Class extends ObjCClass {
        SFCertificatePanel sharedCertificatePanel();
    }

    /**
     * The behavior of this method is somewhat different in macOS 10.4 and later versus OS X v10.3. In OS X v10.3, the
     * sheet displays whatever certificates you pass in the certificates parameter (provided the showGroup parameter is
     * set to YES). Starting with OS X v10.4, the sheet displays the leaf certificate (that is, the first certificate in
     * the array you pass) plus any other certificates in the certificate chain that the Security Server can find. If
     * you include all of the certificates in the chain in the certificates parameter, you can ensure that the same
     * certificates are displayed whatever the version of the operating system, and may decrease the time required to
     * find and display the certificates in macOS 10.4 and later.
     * <p>
     * The parameters for the delegate method are:
     * <p>
     * sheet The window to which the sheet was attached.
     * <p>
     * returnCode The result code indicating which button the user clicked: either NSFileHandlingPanelOKButton or
     * NSFileHandlingPanelCancelButton.
     * <p>
     * contextInfo Client-defined contextual data that is passed in the contextInfo parameter of the
     * beginSheetForDirectory:... method.
     * <p>
     * The delegate method may dismiss the keychain settings sheet itself; if it does not, the sheet is dismissed on
     * return from the beginSheetForDirectory:... method.
     *
     * @param docWindow      The parent window to which the sheet is attached.
     * @param delegate       The delegate object in which the method specified in the didEndSelector parameter is
     *                       implemented.
     * @param didEndSelector A selector for a delegate method called when the sheet has been dismissed. Implementation
     *                       of this delegate method is optional.
     * @param contextInfo    A pointer to data that is passed to the delegate method. You can use this data pointer for
     *                       any purpose you wish.
     * @param certificates   The certificates to display. Pass an NSArray containing one or more objects of type
     *                       SecCertificateRef in this parameter. The first certificate in the array must be the leaf
     *                       certificate. The other certificates (if any) can be included in any order.
     * @param showGroup      Specifies whether additional certificates (other than the leaf certificate) are displayed.
     */
    public abstract void beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_certificates_showGroup(
        NSWindow docWindow, ID delegate, Selector didEndSelector, PointerByReference contextInfo, NSArray certificates, boolean showGroup);

    /**
     * Customizes the title of the alternate button.
     *
     * @param title The new title for the alternate button. If this method is not called, or if title is set to nil, the
     *              button is not shown.
     */
    public abstract void setAlternateButtonTitle(String title);

    /**
     * Customizes the title of the default button.
     *
     * @param title The new title for the default button. The default title for this button is “OK”.
     */
    public abstract void setDefaultButtonTitle(String title);

    /**
     * Displays a Help button in the sheet or panel. When a user clicks the help button, the certificate panel first
     * checks the delegate for a certificatePanelShowHelp: method. If the delegate does not implement such a method, or
     * the delegate method returns NO, then the NSHelpManager method openHelpAnchor:inBook: is called with a nil book
     * and the anchor specified by the setHelpAnchor: method. An exception is raised if the delegate returns NO and
     * there is no help anchor set.
     *
     * @param showsHelp Set to YES to display the help button. The help button is hidden by default.
     */
    public abstract void setShowsHelp(final boolean showsHelp);
}
