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
import org.rococoa.cocoa.foundation.NSInteger;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;

public abstract class SFChooseIdentityPanel extends NSPanel {
    public interface SecurityInterfaceFunctions extends Library {
    }

    static {
        Native.load("SecurityInterface", SFChooseIdentityPanel.SecurityInterfaceFunctions.class);
    }

    public static SFChooseIdentityPanel sharedChooseIdentityPanel() {
        return Rococoa.createClass("SFChooseIdentityPanel", SFChooseIdentityPanel._Class.class).sharedChooseIdentityPanel();
    }

    public interface _Class extends ObjCClass {
        SFChooseIdentityPanel sharedChooseIdentityPanel();
    }

    /**
     * Sets an optional domain in which the identity is to be used.
     * <p>
     * Call this method to associate a domain with the chosen identity. If the user chooses an identity and a domain is
     * set, an identity preference item is created in the default keychain. Subsequent calls to SecIdentitySearchCreate
     * and SecIdentitySearchCopyNext return the preferred identity for this domain first.
     *
     * @param domainString A string containing a hostname, RFC 822 name (email address), URL, or similar identifier.
     */
    public abstract void setDomain(String domainString);

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
     * Specifies one or more policies that apply to the displayed certificates.Specifies one or more policies that apply
     * to the displayed certificates.Specifies one or more policies that apply to the displayed certificates.Specifies
     * one or more policies that apply to the displayed certificates.Specifies one or more policies that apply to the
     * displayed certificates.
     *
     * @param policies The policies to use when evaluating the certificates’ status. You can pass either a SecPolicyRef
     *                 object or an NSArray (containing one or more SecPolicyRef instances) in this parameter. If
     *                 policies is set to nil, the Apple X.509 Basic Policy is used.
     */
    public abstract void setPolicies(SecPolicyRef policies);

    /**
     * Sets the optional informative text displayed in the panel.
     *
     * @param text A string containing a hostname, RFC 822 name (email address), URL, or similar identifier.
     */
    public abstract void setInformativeText(String text);

    /**
     * Displays a list of identities in a modal sheet from which the user can select an identity.
     * <p>
     * The parameters for the delegate method are:
     * <p>
     * sheet The window to which the sheet was attached.
     * <p>
     * returnCode The result code indicating which button the user clicked: either NSFileHandlingPanelOKButton or
     * NSFileHandlingPanelCancelButton.
     * <p>
     * contextInfo Client-defined contextual data that is passed in the contextInfo parameter of the
     * beginSheetForWindow:... method.
     * <p>
     * The sheet is dismissed on return from the beginSheetForWindow:... method.
     *
     * @param docWindow      The parent window to which the sheet is attached.
     * @param delegate       The delegate object in which the method specified in the didEndSelector parameter is
     *                       implemented.
     * @param didEndSelector A method selector for a delegate method called when the sheet has been dismissed.
     *                       Implementation of this delegate method is optional.
     * @param contextInfo    A pointer to data that is passed to the delegate method. You can use this data pointer for
     *                       any purpose you wish.
     * @param identities     An array of identity objects (objects of type SecIdentityRef). Use the
     *                       SecIdentitySearchCopyNext function (in Security/SecIdentitySearch.h) to find identity
     *                       objects.
     * @param message        A message string to display in the sheet.
     */
    public abstract void beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_identities_message(
        NSWindow docWindow, ID delegate, Selector didEndSelector, PointerByReference contextInfo, NSArray identities, String message);

    /**
     * Displays a list of identities in a modal panel.
     *
     * @param identities An array of identity objects (objects of type SecIdentityRef. Use the SecIdentitySearchCopyNext
     *                   function (in Security/SecIdentitySearch.h) to find identity objects.
     * @param message    A message string to display in the panel.
     * @return This method returns NSOKButton if the default button is clicked, or NSCancelButton if the alternate
     * button is clicked.
     * <p>
     * Use the identity method to obtain the identity chosen by the user.
     */
    public abstract NSInteger runModalForIdentities_message(NSArray identities, String message);

    /**
     * @return Returns the identity that the user chose in the panel or sheet.
     */
    public abstract SecIdentityRef identity();

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
