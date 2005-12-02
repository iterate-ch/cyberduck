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

$Log: ResolveListener.java,v $
Revision 1.2  2004/04/30 21:48:27  rpantos
Change line endings for CVS.

Revision 1.1  2004/04/30 16:29:35  rpantos
First checked in.

*/


package	com.apple.dnssd;


/**	A listener that receives results from {@link DNSSD#resolve}. */

public interface ResolveListener extends BaseListener
{
	/** Called when a service has been resolved.<P> 

		@param	resolver
					The active resolver object.
		<P>
		@param	flags
					Currently unused, reserved for future use.
		<P>
		@param	fullName
					The full service domain name, in the form &lt;servicename&gt;.&lt;protocol&gt;.&lt;domain&gt;.
					(Any literal dots (".") are escaped with a backslash ("\."), and literal
					backslashes are escaped with a second backslash ("\\"), e.g. a web server
					named "Dr. Pepper" would have the fullname  "Dr\.\032Pepper._http._tcp.local.").
					This is the appropriate format to pass to standard system DNS APIs such as 
					res_query(), or to the special-purpose functions included in this API that
					take fullname parameters.
		<P>
		@param	hostName
					The target hostname of the machine providing the service.  This name can 
					be passed to functions like queryRecord() to look up the host's IP address.
		<P>
		@param	port
					The port number on which connections are accepted for this service.
		<P>
		@param	txtRecord
					The service's primary txt record.
	*/
	void	serviceResolved( DNSSDService resolver, int flags, int ifIndex, String fullName, 
								String hostName, int port, TXTRecord txtRecord);
}

