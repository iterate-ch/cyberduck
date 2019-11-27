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

import com.sun.jna.ptr.IntByReference;

public class SecTrustResultType extends IntByReference {

    /**
     * This value indicates that the user explicitly chose to trust a certificate in the chain, usually by clicking a
     * button in a certificate trust panel. Your app should trust the chain. The Keychain Access utility refers to this
     * value as “Always Trust.”
     */
    public static final int kSecTrustResultProceed = 1;

    /**
     * This value indicates that evaluation reached an (implicitly trusted) anchor certificate without any evaluation
     * failures, but never encountered any explicitly stated user-trust preference. This is the most common return
     * value. The Keychain Access utility refers to this value as the “Use System Policy,” which is the default user
     * setting.
     */
    public static final int kSecTrustResultUnspecified = 4;

    /**
     * This value indicates that the user explicitly chose to not trust a certificate in the chain, usually by clicking
     * the appropriate button in a certificate trust panel. Your app should not trust the chain. The Keychain Access
     * utility refers to this value as "Never Trust."
     */
    public static final int kSecTrustResultDeny = 3;

    /**
     * This value indicates that you should not trust the chain as is, but that the chain could be trusted with some
     * minor change to the evaluation context, such as ignoring expired certificates or adding another anchor to the set
     * of trusted anchors.
     */
    public static final int kSecTrustResultRecoverableTrustFailure = 5;

}
