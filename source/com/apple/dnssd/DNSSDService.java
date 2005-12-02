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

$Log: DNSSDService.java,v $
Revision 1.2  2004/04/30 21:48:27  rpantos
Change line endings for CVS.

Revision 1.1  2004/04/30 16:32:34  rpantos
First checked in.

 */


package	com.apple.dnssd;

/**	A tracking object for a service created by {@link DNSSD}. */

public interface	DNSSDService
{
	/**
	Halt the active operation and free resources associated with the DNSSDService.<P>

	Any services or records registered with this DNSSDService will be deregistered. Any
	Browse, Resolve, or Query operations associated with this reference will be terminated.<P>

	Note: if the service was initialized with DNSSD.register(), and an extra resource record was
	added to the service via {@link DNSSDRegistration#addRecord}, the DNSRecord so created 
	is invalidated when this method is called - the DNSRecord may not be used afterward.
	*/
	void		stop();
} 

