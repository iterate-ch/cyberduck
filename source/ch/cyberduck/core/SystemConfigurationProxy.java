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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public final class SystemConfigurationProxy extends AbstractProxy implements Proxy {
    private static final Logger log = Logger.getLogger(SystemConfigurationProxy.class);

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

    private HostUrlProvider provider
            = new HostUrlProvider();

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
    private boolean isHostExcluded(final String hostname) {
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
    public boolean isSOCKSProxyEnabled(final Host host) {
        if(this.isHostExcluded(host.getHostname())) {
            return false;
        }
        return this.isSOCKSProxyEnabledNative(provider.get(host));
    }

    @Override
    public String getSOCKSProxyHost(final Host host) {
        return this.getSOCKSProxyHostNative(provider.get(host));
    }

    @Override
    public int getSOCKSProxyPort(final Host host) {
        return this.getSOCKSProxyPortNative(provider.get(host));
    }

    @Override
    public boolean isHTTPProxyEnabled(final Host host) {
        if(this.isHostExcluded(host.getHostname())) {
            return false;
        }
        return this.isHTTPProxyEnabledNative(provider.get(host));
    }

    @Override
    public String getHTTPProxyHost(final Host host) {
        return this.getHTTPProxyHostNative(provider.get(host));
    }

    @Override
    public int getHTTPProxyPort(final Host host) {
        return getHTTPProxyPortNative(provider.get(host));
    }

    @Override
    public boolean isHTTPSProxyEnabled(final Host host) {
        if(this.isHostExcluded(host.getHostname())) {
            return false;
        }
        return this.isHTTPSProxyEnabledNative(provider.get(host));
    }

    @Override
    public String getHTTPSProxyHost(final Host host) {
        return this.getHTTPSProxyHostNative(provider.get(host));
    }

    @Override
    public int getHTTPSProxyPort(final Host host) {
        return this.getHTTPSProxyPortNative(provider.get(host));
    }

    /**
     * SOCKS proxy setting enabled
     *
     * @param url The URL the application intends to access
     * @return True if enabled
     */
    public native boolean isSOCKSProxyEnabledNative(String url);

    /**
     * SOCKS proxy setting hostname
     *
     * @param url The URL the application intends to access
     * @return Proxy host
     */
    public native String getSOCKSProxyHostNative(String url);

    /**
     * HTTP proxy setting port
     *
     * @param url The URL the application intends to access
     * @return Proxy port
     */
    public native int getSOCKSProxyPortNative(String url);

    /**
     * HTTP proxy setting enabled
     *
     * @param url The URL the application intends to access
     * @return True if enabled
     */
    public native boolean isHTTPProxyEnabledNative(String url);

    /**
     * HTTP proxy setting hostname
     *
     * @param url The URL the application intends to access
     * @return Proxy host
     */
    public native String getHTTPProxyHostNative(String url);

    /**
     * HTTP proxy setting port
     *
     * @param url The URL the application intends to access
     * @return Proxy port
     */
    public native int getHTTPProxyPortNative(String url);

    /**
     * HTTPS proxy setting enabled
     *
     * @param url The URL the application intends to access
     * @return True if enabled
     */
    public native boolean isHTTPSProxyEnabledNative(String url);

    /**
     * HTTPS proxy setting hostname
     *
     * @param url The URL the application intends to access
     * @return Proxy host
     */
    public native String getHTTPSProxyHostNative(String url);

    /**
     * HTTPS proxy setting port
     *
     * @param url The URL the application intends to access
     * @return Proxy port
     */
    public native int getHTTPSProxyPortNative(String url);
}
