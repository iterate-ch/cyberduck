package ch.cyberduck.core.socket;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;

public class IOKitHardwareAddress implements HardwareAddress {
    private static final Logger log = LogManager.getLogger(IOKitHardwareAddress.class);

    // #define kIOMACAddress            "IOMACAddress"
    private static final CoreFoundation.CFStringRef kIOMACAddress = CoreFoundation.CFStringRef.createCFString("IOMACAddress");
    // #define kIOServicePlane          "IOService"
    private final String kIOServicePlane = "IOService";

    private final int kIORegistryIterateRecursively = 0x00000001;
    private final int kIORegistryIterateParents = 0x00000002;

    @Override
    public byte[] getAddress() throws BackgroundException {
        final IOKit.IOService en0 = IOKitUtil.getMatchingService(IOKitUtil.getBSDNameMatchingDict("en0"));
        if(null == en0) {
            // Interface is not found when link is down #fail
            log.warn("No network interface en0");
            throw new UnsupportedException("No network interface en0");
        }
        final CoreFoundation.CFTypeRef property = IOKit.INSTANCE.IORegistryEntrySearchCFProperty(en0, kIOServicePlane, kIOMACAddress,
                CoreFoundation.INSTANCE.CFAllocatorGetDefault(), kIORegistryIterateRecursively | kIORegistryIterateParents);
        if(null == property) {
            log.error("Cannot determine MAC address");
            throw new UnsupportedException("No hardware address for network interface en0");
        }
        final CoreFoundation.CFDataRef dataRef = new CoreFoundation.CFDataRef(property.getPointer());
        return dataRef.getBytePtr().getByteArray(0, dataRef.getLength());
    }
}
