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

import ch.cyberduck.binding.application.NSWindow;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;


public abstract class SFCertificateTrustPanel extends SFCertificatePanel {
    public interface SecurityInterfaceFunctions extends Library {
    }

    static {
        Native.load("SecurityInterface", SecurityInterfaceFunctions.class);
    }

    public static SFCertificateTrustPanel sharedCertificateTrustPanel() {
        return Rococoa.createClass("SFCertificateTrustPanel", SFCertificateTrustPanel._Class.class).sharedCertificateTrustPanel();
    }

    public interface _Class extends ObjCClass {
        SFCertificateTrustPanel sharedCertificateTrustPanel();
    }

    /**
     * Displays a modal sheet that shows the results of a certificate trust evaluation and that allows the user to edit
     * trust settings.
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
     * The delegate method may dismiss the keychain settings sheet itself; if it does not, the sheet is dismissed on
     * return from the beginSheetForWindow:... method.
     *
     * @param docWindow      The parent window to which the sheet is attached.
     * @param delegate       The delegate object in which the method specified in the didEndSelector parameter is
     *                       implemented.
     * @param didEndSelector A method selector for a delegate method called when the sheet has been dismissed.
     *                       Implementation of this delegate method is optional.
     * @param contextInfo    A pointer to data that is passed to the delegate method. You can use this data pointer for
     *                       any purpose you wish.
     * @param trust          A trust management object. Use the SecTrustCreateWithCertificates function (in
     *                       Security/SecTrust.h) to create the trust management object.
     * @param message        A message string to display in the sheet.
     */
    public abstract void beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_trust_message(
        NSWindow docWindow, ID delegate, Selector didEndSelector, PointerByReference contextInfo, SecTrustRef trust, String message);

    /**
     * Displays a modal panel that shows the results of a certificate trust evaluation and that allows the user to edit
     * trust settings.
     *
     * @param trust   A trust management object. Use the SecTrustCreateWithCertificates function (in
     *                Security/SecTrust.h) to create the trust management object.
     * @param message A message string to display in the panel.
     * @return This method returns NSOKButton if the default button is clicked, or NSCancelButton if the alternate
     * button is clicked.
     */
    public abstract NSInteger runModalForTrust_message(SecTrustRef trust, String message);

    /**
     * Sets the (optional) informative text displayed in the panel.
     *
     * @param informativeText By default, informative text describing the current certificate’s trust status is
     *                        displayed. Call this method only if your application needs to customize the displayed
     *                        informative text.
     */
    public abstract void setInformativeText(String informativeText);

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

}
