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

import ch.cyberduck.binding.foundation.CFAllocatorRef;
import ch.cyberduck.binding.foundation.NSData;

import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface SecurityFunctions extends Library {
    SecurityFunctions library = Native.load(
        "Security", SecurityFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    /**
     * The certificate object returned by this function is used as input to other functions in the API.
     *
     * @param allocator The CFAllocator object you wish to use to allocate the certificate object. Pass NULL to use the
     *                  default allocator.
     * @param data      A DER (Distinguished Encoding Rules) representation of an X.509 certificate.
     * @return The newly created certificate object. Call the CFRelease function to release this object when you are
     * finished with it. Returns NULL if the data passed in the data parameter is not a valid DER-encoded X.509
     * certificate.
     */
    SecCertificateRef SecCertificateCreateWithData(CFAllocatorRef allocator, NSData data);
}
