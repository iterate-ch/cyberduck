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

$Log: DNSSDException.java,v $
Revision 1.2  2004/04/30 21:48:27  rpantos
Change line endings for CVS.

Revision 1.1  2004/04/30 16:29:35  rpantos
First checked in.

*/

package	com.apple.dnssd;


/**
	Used to report various DNS-SD-related error conditions.
*/

abstract public class	DNSSDException extends Exception
{
    public static final int		NO_ERROR            =  0;
    public static final int		UNKNOWN             = -65537;
    public static final int		NO_SUCH_NAME        = -65538;
    public static final int		NO_MEMORY           = -65539;
    public static final int		BAD_PARAM           = -65540;
    public static final int		BAD_REFERENCE       = -65541;
    public static final int		BAD_STATE           = -65542;
    public static final int		BAD_FLAGS           = -65543;
    public static final int		UNSUPPORTED         = -65544;
    public static final int		NOT_INITIALIZED     = -65545;
    public static final int		ALREADY_REGISTERED  = -65547;
    public static final int		NAME_CONFLICT       = -65548;
    public static final int		INVALID             = -65549;
    public static final int		INCOMPATIBLE        = -65551;
    public static final int		BAD_INTERFACE_INDEX = -65552;

	/** Returns the sub-code that identifies the particular error. */
	abstract public int			getErrorCode();
}

