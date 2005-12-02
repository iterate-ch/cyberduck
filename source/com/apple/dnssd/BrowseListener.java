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

$Log: BrowseListener.java,v $
Revision 1.2  2004/04/30 21:48:27  rpantos
Change line endings for CVS.

Revision 1.1  2004/04/30 16:29:35  rpantos
First checked in.

 */


package	com.apple.dnssd;


/**	A listener that receives results from {@link DNSSD#browse}. */

public interface BrowseListener extends BaseListener
{
	/** Called to report discovered services.<P> 

		@param	browser
					The active browse service.
		<P>
		@param	flags
					Possible values are DNSSD.MORE_COMING.
		<P>
		@param	ifIndex
					The interface on which the service is advertised. This index should be passed 
					to {@link DNSSD#resolve} when resolving the service.
		<P>
		@param	serviceName
					The service name discovered.
		<P>
		@param	regType
					The registration type, as passed in to DNSSD.browse().
		<P>
		@param	domain
					The domain in which the service was discovered.
	*/
	void	serviceFound( DNSSDService browser, int flags, int ifIndex, 
							String serviceName, String regType, String domain);

	/** Called to report services which have been deregistered.<P> 

		@param	browser
					The active browse service.
		<P>
		@param	flags
					Possible values are DNSSD.MORE_COMING.
		<P>
		@param	ifIndex
					The interface on which the service is advertised.
		<P>
		@param	serviceName
					The service name which has deregistered.
		<P>
		@param	regType
					The registration type, as passed in to DNSSD.browse().
		<P>
		@param	domain
					The domain in which the service was discovered.
	*/
	void	serviceLost( DNSSDService browser, int flags, int ifIndex,
							String serviceName, String regType, String domain);
}

