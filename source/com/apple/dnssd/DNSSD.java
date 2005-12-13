/*
 * Copyright (c) 2004 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@

    Change History (most recent first):

$Log: DNSSD.java,v $
Revision 1.4  2004/12/11 03:00:59  rpantos
<rdar://problem/3907498> Java DNSRecord API should be cleaned up

Revision 1.3  2004/11/12 03:23:08  rpantos
rdar://problem/3809541 implement getIfIndexForName, getNameForIfIndex.

Revision 1.2  2004/05/20 17:43:18  cheshire
Fix invalid UTF-8 characters in file

Revision 1.1  2004/04/30 16:32:34  rpantos
First checked in.


	This file declares and implements DNSSD, the central Java factory class
	for doing DNS Service Discovery. It includes the mostly-abstract public
	interface, as well as the Apple* implementation subclasses.

	To do:
	- implement network interface mappings
	- RegisterRecord
 */


package com.apple.dnssd;


/**
 * DNSSD provides access to DNS Service Discovery features of ZeroConf networking.<P>
 * <p/>
 * It is a factory class that is used to invoke registration and discovery-related
 * operations. Most operations are non-blocking; clients are called back through an interface
 * with the result of an operation. Callbacks are made from a separate worker thread.<P>
 * <p/>
 * For example, in this program<P>
 * <PRE><CODE>
 * class   MyClient implements BrowseListener {
 * void    lookForWebServers() {
 * myBrowser = DNSSD.browse("_http_.tcp", this);
 * }
 * <p/>
 * public void serviceFound(DNSSDService browser, int flags, int ifIndex,
 * String serviceName, String regType, String domain) {}
 * ...
 * }</CODE></PRE>
 * <CODE>MyClient.serviceFound()</CODE> would be called for every HTTP server discovered in the
 * default browse domain(s).
 */

abstract public class DNSSD {
    /**
     * Flag indicates to a {@link BrowseListener} that another result is
     * queued.  Applications should not update their UI to display browse
     * results if the MORE_COMING flag is set; they will be called at least once
     * more with the flag clear.
     */
    public static final int MORE_COMING = (1 << 0);

    /**
     * If flag is set in a {@link DomainListener} callback, indicates that the result is the default domain.
     */
    public static final int DEFAULT = (1 << 2);

    /**
     * If flag is set, a name conflict will trigger an exception when registering non-shared records.<P>
     * A name must be explicitly specified when registering a service if this bit is set
     * (i.e. the default name may not not be used).
     */
    public static final int NO_AUTO_RENAME = (1 << 3);

    /**
     * If flag is set, allow multiple records with this name on the network (e.g. PTR records)
     * when registering individual records on a {@link DNSSDRegistration}.
     */
    public static final int SHARED = (1 << 4);

    /**
     * If flag is set, records with this name must be unique on the network (e.g. SRV records).
     */
    public static final int UNIQUE = (1 << 5);

    /**
     * Set flag when calling enumerateDomains() to restrict results to domains recommended for browsing.
     */
    public static final int BROWSE_DOMAINS = (1 << 6);
    /**
     * Set flag when calling enumerateDomains() to restrict results to domains recommended for registration.
     */
    public static final int REGISTRATION_DOMAINS = (1 << 7);

    /**
     * Maximum length, in bytes, of a domain name represented as an escaped C-String.
     */
    public static final int MAX_DOMAIN_NAME = 1005;

    /**
     * Pass for ifIndex to specify all available interfaces.
     */
    public static final int ALL_INTERFACES = 0;

    /**
     * Pass for ifIndex to specify the localhost interface.
     */
    public static final int LOCALHOST_ONLY = -1;

    /**
     * Browse for instances of a service.<P>
     * <p/>
     * Note: browsing consumes network bandwidth. Call {@link DNSSDService#stop} when you have finished browsing.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    flags Currently ignored, reserved for future use.
     * <p/>
     * @param    ifIndex If non-zero, specifies the interface on which to browse for services
     * (the index for a given interface is determined via the if_nametoindex()
     * family of calls.)  Most applications will pass 0 to browse on all available
     * interfaces.  Pass -1 to only browse for services provided on the local host.
     * <p/>
     * @param    regType The registration type being browsed for followed by the protocol, separated by a
     * dot (e.g. "_ftp._tcp"). The transport protocol must be "_tcp" or "_udp".
     * <p/>
     * @param    domain If non-null, specifies the domain on which to browse for services.
     * Most applications will not specify a domain, instead browsing on the
     * default domain(s).
     * <p/>
     * @param    listener This object will get called when instances of the service are discovered (or disappear).
     * <p/>
     * @return A {@link DNSSDService} that represents the active browse operation.
     * @see RuntimePermission
     */
    public static DNSSDService browse(int flags, int ifIndex, String regType, String domain, BrowseListener listener)
            throws DNSSDException {
        return getInstance()._makeBrowser(flags, ifIndex, regType, domain, listener);
    }

    /**
     * Browse for instances of a service. Use default flags, ifIndex and domain.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    regType The registration type being browsed for followed by the protocol, separated by a
     * dot (e.g. "_ftp._tcp"). The transport protocol must be "_tcp" or "_udp".
     * <p/>
     * @param    listener This object will get called when instances of the service are discovered (or disappear).
     * <p/>
     * @return A {@link DNSSDService} that represents the active browse operation.
     * @see RuntimePermission
     */
    public static DNSSDService browse(String regType, BrowseListener listener)
            throws DNSSDException {
        return browse(0, 0, regType, "", listener);
    }

    /**
     * Resolve a service name discovered via browse() to a target host name, port number, and txt record.<P>
     * <p/>
     * Note: Applications should NOT use resolve() solely for txt record monitoring - use
     * queryRecord() instead, as it is more efficient for this task.<P>
     * <p/>
     * Note: When the desired results have been returned, the client MUST terminate the resolve by
     * calling {@link DNSSDService#stop}.<P>
     * <p/>
     * Note: resolve() behaves correctly for typical services that have a single SRV record and
     * a single TXT record (the TXT record may be empty.)  To resolve non-standard services with
     * multiple SRV or TXT records, use queryRecord().<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    flags Currently ignored, reserved for future use.
     * <p/>
     * @param    ifIndex The interface on which to resolve the service.  The client should
     * pass the interface on which the serviceName was discovered (i.e.
     * the ifIndex passed to the serviceFound() callback)
     * or 0 to resolve the named service on all available interfaces.
     * <p/>
     * @param    serviceName The servicename to be resolved.
     * <p/>
     * @param    regType The registration type being resolved followed by the protocol, separated by a
     * dot (e.g. "_ftp._tcp").  The transport protocol must be "_tcp" or "_udp".
     * <p/>
     * @param    domain The domain on which the service is registered, i.e. the domain passed
     * to the serviceFound() callback.
     * <p/>
     * @param    listener This object will get called when the service is resolved.
     * <p/>
     * @return A {@link DNSSDService} that represents the active resolve operation.
     * @see RuntimePermission
     */
    public static DNSSDService resolve(int flags, int ifIndex, String serviceName, String regType,
                                       String domain, ResolveListener listener)
            throws DNSSDException {
        return getInstance()._resolve(flags, ifIndex, serviceName, regType, domain, listener);
    }

    /**
     * Register a service, to be discovered via browse() and resolve() calls.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    flags Possible values are: NO_AUTO_RENAME.
     * <p/>
     * @param    ifIndex If non-zero, specifies the interface on which to register the service
     * (the index for a given interface is determined via the if_nametoindex()
     * family of calls.)  Most applications will pass 0 to register on all
     * available interfaces.  Pass -1 to register a service only on the local
     * machine (service will not be visible to remote hosts).
     * <p/>
     * @param    serviceName If non-null, specifies the service name to be registered.
     * Applications need not specify a name, in which case the
     * computer name is used (this name is communicated to the client via
     * the serviceRegistered() callback).
     * <p/>
     * @param    regType The registration type being registered followed by the protocol, separated by a
     * dot (e.g. "_ftp._tcp").  The transport protocol must be "_tcp" or "_udp".
     * <p/>
     * @param    domain If non-null, specifies the domain on which to advertise the service.
     * Most applications will not specify a domain, instead automatically
     * registering in the default domain(s).
     * <p/>
     * @param    host If non-null, specifies the SRV target host name.  Most applications
     * will not specify a host, instead automatically using the machine's
     * default host name(s).  Note that specifying a non-null host does NOT
     * create an address record for that host - the application is responsible
     * for ensuring that the appropriate address record exists, or creating it
     * via {@link DNSSDRegistration#addRecord}.
     * <p/>
     * @param    port The port on which the service accepts connections.  Pass 0 for a
     * "placeholder" service (i.e. a service that will not be discovered by
     * browsing, but will cause a name conflict if another client tries to
     * register that same name.)  Most clients will not use placeholder services.
     * <p/>
     * @param    txtRecord The txt record rdata.  May be null.  Note that a non-null txtRecord
     * MUST be a properly formatted DNS TXT record, i.e. &lt;length byte&gt; &lt;data&gt;
     * &lt;length byte&gt; &lt;data&gt; ...
     * <p/>
     * @param    listener This object will get called when the service is registered.
     * <p/>
     * @return A {@link DNSSDRegistration} that controls the active registration.
     * @see RuntimePermission
     */
    public static DNSSDRegistration register(int flags, int ifIndex, String serviceName, String regType,
                                             String domain, String host, int port, TXTRecord txtRecord, RegisterListener listener)
            throws DNSSDException {
        return getInstance()._register(flags, ifIndex, serviceName, regType, domain, host, port, txtRecord, listener);
    }

    /**
     * Register a service, to be discovered via browse() and resolve() calls. Use default flags, ifIndex, domain, host and txtRecord.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    serviceName If non-null, specifies the service name to be registered.
     * Applications need not specify a name, in which case the
     * computer name is used (this name is communicated to the client via
     * the serviceRegistered() callback).
     * <p/>
     * @param    regType The registration type being registered followed by the protocol, separated by a
     * dot (e.g. "_ftp._tcp").  The transport protocol must be "_tcp" or "_udp".
     * <p/>
     * @param    port The port on which the service accepts connections.  Pass 0 for a
     * "placeholder" service (i.e. a service that will not be discovered by
     * browsing, but will cause a name conflict if another client tries to
     * register that same name.)  Most clients will not use placeholder services.
     * <p/>
     * @param    listener This object will get called when the service is registered.
     * <p/>
     * @return A {@link DNSSDRegistration} that controls the active registration.
     * @see RuntimePermission
     */
    public static DNSSDRegistration register(String serviceName, String regType, int port, RegisterListener listener)
            throws DNSSDException {
        return register(0, 0, serviceName, regType, null, null, port, null, listener);
    }

    /**
     * Query for an arbitrary DNS record.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    flags Possible values are: MORE_COMING.
     * <p/>
     * @param    ifIndex If non-zero, specifies the interface on which to issue the query
     * (the index for a given interface is determined via the if_nametoindex()
     * family of calls.)  Passing 0 causes the name to be queried for on all
     * interfaces.  Passing -1 causes the name to be queried for only on the
     * local host.
     * <p/>
     * @param    serviceName The full domain name of the resource record to be queried for.
     * <p/>
     * @param    rrtype The numerical type of the resource record to be queried for (e.g. PTR, SRV, etc)
     * as defined in nameser.h.
     * <p/>
     * @param    rrclass The class of the resource record, as defined in nameser.h
     * (usually 1 for the Internet class).
     * <p/>
     * @param    listener This object will get called when the query completes.
     * <p/>
     * @return A {@link DNSSDService} that controls the active query.
     * @see RuntimePermission
     */
    public static DNSSDService queryRecord(int flags, int ifIndex, String serviceName, int rrtype,
                                           int rrclass, QueryListener listener)
            throws DNSSDException {
        return getInstance()._queryRecord(flags, ifIndex, serviceName, rrtype, rrclass, listener);
    }

    /**
     * Asynchronously enumerate domains available for browsing and registration.<P>
     * <p/>
     * Currently, the only domain returned is "local.", but other domains will be returned in future.<P>
     * <p/>
     * The enumeration MUST be cancelled by calling {@link DNSSDService#stop} when no more domains
     * are to be found.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    flags Possible values are: BROWSE_DOMAINS, REGISTRATION_DOMAINS.
     * <p/>
     * @param    ifIndex If non-zero, specifies the interface on which to look for domains.
     * (the index for a given interface is determined via the if_nametoindex()
     * family of calls.)  Most applications will pass 0 to enumerate domains on
     * all interfaces.
     * <p/>
     * @param    listener This object will get called when domains are found.
     * <p/>
     * @return A {@link DNSSDService} that controls the active enumeration.
     * @see RuntimePermission
     */
    public static DNSSDService enumerateDomains(int flags, int ifIndex, DomainListener listener)
            throws DNSSDException {
        return getInstance()._enumerateDomains(flags, ifIndex, listener);
    }

    /**
     * Concatenate a three-part domain name (as provided to the listeners) into a
     * properly-escaped full domain name. Note that strings passed to listeners are
     * ALREADY ESCAPED where necessary.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    serviceName The service name - any dots or slashes must NOT be escaped.
     * May be null (to construct a PTR record name, e.g. "_ftp._tcp.apple.com").
     * <p/>
     * @param    regType The registration type followed by the protocol, separated by a dot (e.g. "_ftp._tcp").
     * <p/>
     * @param    domain The domain name, e.g. "apple.com".  Any literal dots or backslashes must be escaped.
     * <p/>
     * @return The full domain name.
     * @see RuntimePermission
     */
    public static String constructFullName(String serviceName, String regType, String domain)
            throws DNSSDException {
        return getInstance()._constructFullName(serviceName, regType, domain);
    }

    /**
     * Instruct the daemon to verify the validity of a resource record that appears to
     * be out of date. (e.g. because tcp connection to a service's target failed.) <P>
     * <p/>
     * Causes the record to be flushed from the daemon's cache (as well as all other
     * daemons' caches on the network) if the record is determined to be invalid.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    flags Currently unused, reserved for future use.
     * <p/>
     * @param    ifIndex If non-zero, specifies the interface on which to reconfirm the record
     * (the index for a given interface is determined via the if_nametoindex()
     * family of calls.)  Passing 0 causes the name to be reconfirmed on all
     * interfaces.  Passing -1 causes the name to be reconfirmed only on the
     * local host.
     * <p/>
     * @param    fullName The resource record's full domain name.
     * <p/>
     * @param    rrtype The resource record's type (e.g. PTR, SRV, etc) as defined in nameser.h.
     * <p/>
     * @param    rrclass The class of the resource record, as defined in nameser.h (usually 1).
     * <p/>
     * @param    rdata The raw rdata of the resource record.
     * @see RuntimePermission
     */
    public static void reconfirmRecord(int flags, int ifIndex, String fullName, int rrtype,
                                       int rrclass, byte[] rdata) {
        getInstance()._reconfirmRecord(flags, ifIndex, fullName, rrtype, rrclass, rdata);
    }

    /**
     * Return the canonical name of a particular interface index.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    ifIndex A valid interface index. Must not be ALL_INTERFACES.
     * <p/>
     * @return The name of the interface, which should match java.net.NetworkInterface.getName().
     * @see RuntimePermission
     */
    public static String getNameForIfIndex(int ifIndex) {
        return getInstance()._getNameForIfIndex(ifIndex);
    }

    /**
     * Return the index of a named interface.<P>
     *
     * @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     * @param    ifName A valid interface name. An example is java.net.NetworkInterface.getName().
     * <p/>
     * @return The interface index.
     * @see RuntimePermission
     */
    public static int getIfIndexForName(String ifName) {
        return getInstance()._getIfIndexForName(ifName);
    }

    protected DNSSD() {
    }    // prevent direct instantiation

    /**
     * Return the single instance of DNSSD.
     */
    static protected final DNSSD getInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("getDNSSDInstance"));
        return fInstance;
    }

    abstract protected DNSSDService _makeBrowser(int flags, int ifIndex, String regType, String domain, BrowseListener listener)
            throws DNSSDException;

    abstract protected DNSSDService _resolve(int flags, int ifIndex, String serviceName, String regType,
                                             String domain, ResolveListener listener)
            throws DNSSDException;

    abstract protected DNSSDRegistration _register(int flags, int ifIndex, String serviceName, String regType,
                                                   String domain, String host, int port, TXTRecord txtRecord, RegisterListener listener)
            throws DNSSDException;

    abstract protected DNSSDService _queryRecord(int flags, int ifIndex, String serviceName, int rrtype,
                                                 int rrclass, QueryListener listener)
            throws DNSSDException;

    abstract protected DNSSDService _enumerateDomains(int flags, int ifIndex, DomainListener listener)
            throws DNSSDException;

    abstract protected String _constructFullName(String serviceName, String regType, String domain)
            throws DNSSDException;

    abstract protected void _reconfirmRecord(int flags, int ifIndex, String fullName, int rrtype,
                                             int rrclass, byte[] rdata);

    abstract protected String _getNameForIfIndex(int ifIndex);

    abstract protected int _getIfIndexForName(String ifName);

    protected static DNSSD fInstance;

    static {
        try {
            String name = System.getProperty("com.apple.dnssd.DNSSD");
            if (name == null)
                name = "com.apple.dnssd.AppleDNSSD";    // Fall back to Apple-provided class.
            fInstance = (DNSSD) Class.forName(name).newInstance();
        }
        catch (Exception e) {
            throw new InternalError("cannot instantiate DNSSD" + e);
        }
    }
}

// Concrete implementation of DNSSDException

class AppleDNSSDException extends DNSSDException {
    public AppleDNSSDException(int errorCode) {
        fErrorCode = errorCode;
    }

    public int getErrorCode() {
        return fErrorCode;
    }

    public String getMessage() {
        final String kMessages[] = {        // should probably be put into a resource or something
                "UNKNOWN",
                "NO_SUCH_NAME",
                "NO_MEMORY",
                "BAD_PARAM",
                "BAD_REFERENCE",
                "BAD_STATE",
                "BAD_FLAGS",
                "UNSUPPORTED",
                "NOT_INITIALIZED",
                "",        // there is NO number 6
                "ALREADY_REGISTERED",
                "NAME_CONFLICT",
                "INVALID",
                "",        // another MIA
                "INCOMPATIBLE",
                "BAD_INTERFACE_INDEX"
        };

        if (fErrorCode <= UNKNOWN && fErrorCode > (UNKNOWN - kMessages.length)) {
            return "DNS-SD Error " + String.valueOf(fErrorCode) + ": " + kMessages[UNKNOWN - fErrorCode];
        }
        else
            return super.getMessage() + "(" + String.valueOf(fErrorCode) + ")";
    }

    protected int fErrorCode;
}

// The concrete, default implementation.

class AppleDNSSD extends DNSSD {
    static {
        int libInitResult = InitLibrary(1);

        if (libInitResult != DNSSDException.NO_ERROR)
            throw new InternalError("cannot instantiate DNSSD: " + new AppleDNSSDException(libInitResult).getMessage());
    }

    static public boolean hasAutoCallbacks;    // Set by InitLibrary() to value of AUTO_CALLBACKS

    protected DNSSDService _makeBrowser(int flags, int ifIndex, String regType, String domain, BrowseListener client)
            throws DNSSDException {
        return new AppleBrowser(flags, ifIndex, regType, domain, client);
    }

    protected DNSSDService _resolve(int flags, int ifIndex, String serviceName, String regType,
                                    String domain, ResolveListener client)
            throws DNSSDException {
        return new AppleResolver(flags, ifIndex, serviceName, regType, domain, client);
    }

    protected DNSSDRegistration _register(int flags, int ifIndex, String serviceName, String regType,
                                          String domain, String host, int port, TXTRecord txtRecord, RegisterListener client)
            throws DNSSDException {
        return new AppleRegistration(flags, ifIndex, serviceName, regType, domain, host, port,
                (txtRecord != null) ? txtRecord.getRawBytes() : null, client);
    }

    protected DNSSDService _queryRecord(int flags, int ifIndex, String serviceName, int rrtype,
                                        int rrclass, QueryListener client)
            throws DNSSDException {
        return new AppleQuery(flags, ifIndex, serviceName, rrtype, rrclass, client);
    }

    protected DNSSDService _enumerateDomains(int flags, int ifIndex, DomainListener listener)
            throws DNSSDException {
        return new AppleDomainEnum(flags, ifIndex, listener);
    }

    protected String _constructFullName(String serviceName, String regType, String domain)
            throws DNSSDException {
        String[]    responseHolder = new String[1];    // lame maneuver to get around Java's lack of reference parameters

        int rc = ConstructName(serviceName, regType, domain, responseHolder);
        if (rc != 0)
            throw new AppleDNSSDException(rc);

        return responseHolder[0];
    }

    protected void _reconfirmRecord(int flags, int ifIndex, String fullName, int rrtype,
                                    int rrclass, byte[] rdata) {
        ReconfirmRecord(flags, ifIndex, fullName, rrtype, rrclass, rdata);
    }

    protected String _getNameForIfIndex(int ifIndex) {
        return GetNameForIfIndex(ifIndex);
    }

    protected int _getIfIndexForName(String ifName) {
        return GetIfIndexForName(ifName);
    }


    protected native int ConstructName(String serviceName, String regType, String domain, String[] pOut);

    protected native void ReconfirmRecord(int flags, int ifIndex, String fullName, int rrtype,
                                          int rrclass, byte[] rdata);

    protected native String GetNameForIfIndex(int ifIndex);

    protected native int GetIfIndexForName(String ifName);

    protected static native int InitLibrary(int callerVersion);
}

class AppleService implements DNSSDService, Runnable {
    public AppleService() {
        fNativeContext = 0;
    }

    public void stop() {
        this.HaltOperation();
    }

    public void finalize() throws Throwable {
        try {
            this.stop();
        }
        finally {
            super.finalize();
        }
    }

    /* The run() method is used internally to schedule an update from another thread */
    public void run() {
        this.ProcessResults();
    }

    /* Block for timeout ms (or forever if -1). Returns 1 if data present, 0 if timed out, -1 if not browsing. */
    protected native int BlockForData(int msTimeout);

    /* Call ProcessResults when data appears on socket descriptor. */
    protected native void ProcessResults();

    protected native void HaltOperation();

    protected void ThrowOnErr(int rc) throws DNSSDException {
        if (rc != 0)
            throw new AppleDNSSDException(rc);
    }

    protected int    /* warning */    fNativeContext;        // Private storage for native side
}

// A ServiceThread calls AppleService.BlockForData() and schedules its client
// when data appears.

class ServiceThread extends Thread {
    public ServiceThread(AppleService owner) {
        fOwner = owner;
    }

    public void run() {
        int result;

        while (true) {
            result = fOwner.BlockForData(-1);
            if (result == 1) {
                fOwner.run();
            }
            else
                break;    // terminate thread
        }
    }

    protected AppleService fOwner;
}


class AppleBrowser extends AppleService {
    public AppleBrowser(int flags, int ifIndex, String regType, String domain, BrowseListener client)
            throws DNSSDException {
        fClient = client;
        this.ThrowOnErr(this.CreateBrowser(flags, ifIndex, regType, domain));
        if (!AppleDNSSD.hasAutoCallbacks)
            new ServiceThread(this).start();
    }

    // Sets fNativeContext. Returns non-zero on error.
    protected native int CreateBrowser(int flags, int ifIndex, String regType, String domain);

    protected BrowseListener fClient;
}

class AppleResolver extends AppleService {
    public AppleResolver(int flags, int ifIndex, String serviceName, String regType,
                         String domain, ResolveListener client)
            throws DNSSDException {
        fClient = client;
        this.ThrowOnErr(this.CreateResolver(flags, ifIndex, serviceName, regType, domain));
        if (!AppleDNSSD.hasAutoCallbacks)
            new ServiceThread(this).start();
    }

    // Sets fNativeContext. Returns non-zero on error.
    protected native int CreateResolver(int flags, int ifIndex, String serviceName, String regType,
                                        String domain);

    protected ResolveListener fClient;
}

// An AppleDNSRecord is a simple wrapper around a dns_sd DNSRecord.

class AppleDNSRecord implements DNSRecord {
    public AppleDNSRecord(AppleService owner) {
        fOwner = owner;
        fRecord = 0;         // record always starts out empty
    }

    public void update(int flags, byte[] rData, int ttl)
            throws DNSSDException {
        this.ThrowOnErr(this.Update(flags, rData, ttl));
    }

    public void remove()
            throws DNSSDException {
        this.ThrowOnErr(this.Remove());
    }

    protected int fRecord;        // Really a DNSRecord; sizeof(int) == sizeof(void*) ?
    protected AppleService fOwner;

    protected void ThrowOnErr(int rc) throws DNSSDException {
        if (rc != 0)
            throw new AppleDNSSDException(rc);
    }

    protected native int Update(int flags, byte[] rData, int ttl);

    protected native int Remove();
}

class AppleRegistration extends AppleService implements DNSSDRegistration {
    public AppleRegistration(int flags, int ifIndex, String serviceName, String regType, String domain,
                             String host, int port, byte[] txtRecord, RegisterListener client)
            throws DNSSDException {
        fClient = client;
        this.ThrowOnErr(this.BeginRegister(ifIndex, flags, serviceName, regType, domain, host, port, txtRecord));
        if (!AppleDNSSD.hasAutoCallbacks)
            new ServiceThread(this).start();
    }

    public DNSRecord addRecord(int flags, int rrType, byte[] rData, int ttl)
            throws DNSSDException {
        AppleDNSRecord newRecord = new AppleDNSRecord(this);

        this.ThrowOnErr(this.AddRecord(flags, rrType, rData, ttl, newRecord));

        return newRecord;
    }

    public DNSRecord getTXTRecord()
            throws DNSSDException {
        return new AppleDNSRecord(this);    // A record with ref 0 is understood to be primary TXT record
    }

    // Sets fNativeContext. Returns non-zero on error.
    protected native int BeginRegister(int ifIndex, int flags, String serviceName, String regType,
                                       String domain, String host, int port, byte[] txtRecord);

    // Sets fNativeContext. Returns non-zero on error.
    protected native int AddRecord(int flags, int rrType, byte[] rData, int ttl, AppleDNSRecord destObj);

    protected RegisterListener fClient;
}

class AppleQuery extends AppleService {
    public AppleQuery(int flags, int ifIndex, String serviceName, int rrtype,
                      int rrclass, QueryListener client)
            throws DNSSDException {
        fClient = client;
        this.ThrowOnErr(this.CreateQuery(flags, ifIndex, serviceName, rrtype, rrclass));
        if (!AppleDNSSD.hasAutoCallbacks)
            new ServiceThread(this).start();
    }

    // Sets fNativeContext. Returns non-zero on error.
    protected native int CreateQuery(int flags, int ifIndex, String serviceName, int rrtype, int rrclass);

    protected QueryListener fClient;
}

class AppleDomainEnum extends AppleService {
    public AppleDomainEnum(int flags, int ifIndex, DomainListener listener)
            throws DNSSDException {
        fClient = listener;
        this.ThrowOnErr(this.BeginEnum(flags, ifIndex));
        if (!AppleDNSSD.hasAutoCallbacks)
            new ServiceThread(this).start();
    }

    // Sets fNativeContext. Returns non-zero on error.
    protected native int BeginEnum(int flags, int ifIndex);

	protected DomainListener	fClient;
}


