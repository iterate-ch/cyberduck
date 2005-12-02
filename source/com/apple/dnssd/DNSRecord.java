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

$Log: DNSRecord.java,v $
Revision 1.2  2004/12/11 03:00:59  rpantos
<rdar://problem/3907498> Java DNSRecord API should be cleaned up

Revision 1.1  2004/04/30 16:32:34  rpantos
First checked in.


 */


package	com.apple.dnssd;


/**	
	Reference to a record returned by {@link DNSSDRegistration#addRecord}.<P> 

	Note: client is responsible for serializing access to these objects if 
	they are shared between concurrent threads.
*/

public interface	DNSRecord
{
	/** Update a registered resource record.<P> 
		The record must either be the primary txt record of a service registered via DNSSD.register(), 
		or a record added to a registered service via addRecord().<P>

		@param	flags
					Currently unused, reserved for future use.
		<P>
		@param	rData
					The new rdata to be contained in the updated resource record.
		<P>
		@param	ttl
					The time to live of the updated resource record, in seconds.
	*/
	void			update( int flags, byte[] rData, int ttl)
	throws DNSSDException;

	/** Remove a registered resource record.<P> 
	*/
	void			remove()
	throws DNSSDException;
}

