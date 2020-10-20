package ch.cyberduck.core.keychain;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

public class SecTrustOptionFlags extends IntByReference {

    /**
     * Allow expired certificates (except for the root certificate).
     */
    public static final int kSecTrustOptionAllowExpired = 0x00000001;

    /**
     * Allow CA certificates as leaf certificates.
     */
    public static final int kSecTrustOptionLeafIsCA = 0x00000002;

    /**
     * Allow network downloads of CA certificates.
     */
    public static final int kSecTrustOptionFetchIssuerFromNet = 0x00000004;

    /**
     * Allow expired root certificates.
     */
    public static final int kSecTrustOptionAllowExpiredRoot = 0x00000008;

    /**
     * Require a positive revocation check for each certificate.
     */
    public static final int kSecTrustOptionRequireRevPerCert = 0x00000010;

    /**
     * Use TrustSettings instead of anchors.
     */
    public static final int kSecTrustOptionUseTrustSettings = 0x00000020;

    /**
     * Treat properly self-signed certificates as anchors implicitly.
     */
    public static final int kSecTrustOptionImplicitAnchors = 0x00000040;

}
