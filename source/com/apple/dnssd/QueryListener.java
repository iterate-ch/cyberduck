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

$Log: QueryListener.java,v $
Revision 1.2  2004/04/30 21:48:27  rpantos
Change line endings for CVS.

Revision 1.1  2004/04/30 16:29:35  rpantos
First checked in.

 */


package	com.apple.dnssd;


/**	A listener that receives results from {@link DNSSD#queryRecord}. */

public interface QueryListener extends BaseListener
{
	/** Called when a record query has been completed.<P> 

		@param	query
					The active query object.
		<P>
		@param	flags
					Possible values are DNSSD.MORE_COMING.
		<P>
		@param	ifIndex
					The interface on which the query was resolved. (The index for a given 
					interface is determined via the if_nametoindex() family of calls.) 
		<P>
		@param	fullName
					The resource record's full domain name.
		<P>
		@param	rrtype
					The resource record's type (e.g. PTR, SRV, etc) as defined by RFC 1035 and its updates.
		<P>
		@param	rrclass
					The class of the resource record, as defined by RFC 1035 and its updates.
		<P>
		@param	rdata
					The raw rdata of the resource record.
		<P>
		@param	ttl
					The resource record's time to live, in seconds.
	*/
	void	queryAnswered( DNSSDService query, int flags, int ifIndex, String fullName, 
								int rrtype, int rrclass, byte[] rdata, int ttl);
}

