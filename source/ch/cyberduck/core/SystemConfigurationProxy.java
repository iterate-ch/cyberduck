package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.library.Native;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public final class SystemConfigurationProxy extends AbstractProxy implements Proxy {
    private static Logger log = Logger.getLogger(SystemConfigurationProxy.class);

    public static void register() {
        ProxyFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends ProxyFactory {
        @Override
        protected Proxy create() {
            return new SystemConfigurationProxy();
        }
    }

    static {
        Native.load("Proxy");
    }

    protected SystemConfigurationProxy() {
        //
    }

    /**
     * Use passive connect mode
     *
     * @return True if enabled in the system preferences
     */
    public native boolean usePassiveFTPNative();

    @Override
    public boolean usePassiveFTP() {
        return this.usePassiveFTPNative();
    }

    /**
     * @param hostname Hostname or CIDR notation
     * @return True if host is excluded in native proxy configuration
     */
    private boolean isHostExcluded(String hostname) {
        if(!hostname.contains(".")) {
            // Non fully qualified hostname
            if(this.isSimpleHostnameExcludedNative()) {
                return true;
            }
        }
        for(String exception : this.getProxyExceptionsNative()) {
            if(StringUtils.isBlank(exception)) {
                continue;
            }
            if(this.matches(exception, hostname)) {
                return true;
            }
            try {
                SubnetUtils subnet = new SubnetUtils(exception);
                try {
                    String ip = Inet4Address.getByName(hostname).getHostAddress();
                    if(subnet.getInfo().isInRange(ip)) {
                        return true;
                    }
                }
                catch(UnknownHostException e) {
                    // Should not happen as we resolve addresses before attempting to connect
                    // in ch.cyberduck.core.Resolver
                    log.warn(e.getMessage());
                }
            }
            catch(IllegalArgumentException e) {
                // A hostname pattern but not CIDR. Does not
                // match n.n.n.n/m where n=1-3 decimal digits, m = 1-3 decimal digits in range 1-32
                log.debug("Invalid CIDR notation:" + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Check to see if the hostname is excluded from proxy settings
     *
     * @return Exception patterns
     */
    public native String[] getProxyExceptionsNative();

    public native boolean isSimpleHostnameExcludedNative();

    @Override
    public boolean isSOCKSProxyEnabled(Host host) {
        if(this.isHostExcluded(host.getHostname())) {
            return false;
        }
        return this.isSOCKSProxyEnabledNative();
    }

    @Override
    public String getSOCKSProxyHost(Host host) {
        return this.getSOCKSProxyHostNative();
    }

    @Override
    public int getSOCKSProxyPort(Host host) {
        return this.getSOCKSProxyPortNative();
    }

    @Override
    public boolean isHTTPProxyEnabled(Host host) {
        if(this.isHostExcluded(host.getHostname())) {
            return false;
        }
        return this.isHTTPProxyEnabledNative();
    }

    @Override
    public String getHTTPProxyHost(Host host) {
        return this.getHTTPProxyHostNative();
    }

    @Override
    public int getHTTPProxyPort(Host host) {
        return getHTTPProxyPortNative();
    }

    @Override
    public boolean isHTTPSProxyEnabled(Host host) {
        if(this.isHostExcluded(host.getHostname())) {
            return false;
        }
        return this.isHTTPSProxyEnabledNative();
    }

    @Override
    public String getHTTPSProxyHost(Host host) {
        return this.getHTTPSProxyHostNative();
    }

    @Override
    public int getHTTPSProxyPort(Host host) {
        return this.getHTTPSProxyPortNative();
    }

    /**
     * SOCKS proxy setting enabled
     *
     * @return True if enabled
     */
    public native boolean isSOCKSProxyEnabledNative();

    /**
     * SOCKS proxy setting hostname
     *
     * @return Proxy host
     */
    public native String getSOCKSProxyHostNative();

    /**
     * HTTP proxy setting port
     *
     * @return Proxy port
     */
    public native int getSOCKSProxyPortNative();

    /**
     * HTTP proxy setting enabled
     *
     * @return True if enabled
     */
    public native boolean isHTTPProxyEnabledNative();

    /**
     * HTTP proxy setting hostname
     *
     * @return Proxy host
     */
    public native String getHTTPProxyHostNative();

    /**
     * HTTP proxy setting port
     *
     * @return Proxy port
     */
    public native int getHTTPProxyPortNative();

    /**
     * HTTPS proxy setting enabled
     *
     * @return True if enabled
     */
    public native boolean isHTTPSProxyEnabledNative();

    /**
     * HTTPS proxy setting hostname
     *
     * @return Proxy host
     */
    public native String getHTTPSProxyHostNative();

    /**
     * HTTPS proxy setting port
     *
     * @return Proxy port
     */
    public native int getHTTPSProxyPortNative();
}
