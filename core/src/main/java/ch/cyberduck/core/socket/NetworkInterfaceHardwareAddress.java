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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkInterfaceHardwareAddress implements HardwareAddress {
    private static final Logger log = LogManager.getLogger(NetworkInterfaceHardwareAddress.class);

    @Override
    public byte[] getAddress() throws BackgroundException {
        try {
            final NetworkInterface en0 = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if(null == en0) {
                // Interface is not found when link is down #fail
                log.warn("No network interface en0");
                throw new UnsupportedException("No network interface en0");
            }
            final byte[] address = en0.getHardwareAddress();
            if(null == address) {
                log.error("Cannot determine MAC address");
                throw new UnsupportedException("No hardware address for network interface en0");
            }
            return address;
        }
        catch(SocketException | UnknownHostException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
