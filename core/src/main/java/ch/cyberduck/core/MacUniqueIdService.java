package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MacUniqueIdService implements UniqueIdService {
    private static final Logger log = LogManager.getLogger(MacUniqueIdService.class.getName());

    @Override
    public String getUUID() throws LocalAccessDeniedException {
        try {
            final NetworkInterface in = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if(null == in) {
                return this.enumerate();
            }
            final byte[] address = in.getHardwareAddress();
            if(null == address) {
                return this.enumerate();
            }
            return this.toHex(address);
        }
        catch(UnknownHostException | SocketException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    protected String enumerate() throws SocketException, LocalAccessDeniedException {
        log.warn("Missing loopback network interface");
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()) {
            final NetworkInterface n = interfaces.nextElement();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Found network interface %s", n));
            }
            if(n.getHardwareAddress() != null) {
                return this.toHex(n.getHardwareAddress());
            }
        }
        throw new LocalAccessDeniedException("Missing default network interface");
    }

    private String toHex(final byte[] hardwareAddress) throws SocketException {
        return Hex.encodeHexString(hardwareAddress);
    }
}
