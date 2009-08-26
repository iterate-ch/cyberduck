/**
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003 Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 */

package com.enterprisedt.net.ftp;

/**
 * Enumerates the transfer types possible. We
 * support only the two common types, ASCII and
 * Image (often called binary).
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPTransferType {

	/**
	 * Represents ASCII transfer type
	 */
	public static FTPTransferType ASCII = new FTPTransferType() {
        @Override
        public String toString() {
            return "ascii";
        }
    };

	/**
	 * Represents Image (or binary) transfer type
	 */
	public static FTPTransferType BINARY = new FTPTransferType() {
        @Override
        public String toString() {
            return "binary";
        }
    };

    public static FTPTransferType AUTO = new FTPTransferType() {
        @Override
        public String toString() {
            return "auto";
        }
    };

    /**
	 * The char sent to the server to set ASCII
	 */
	static String ASCII_CHAR = "A";

	/**
	 * The char sent to the server to set BINARY
	 */
	static String BINARY_CHAR = "I";

	/**
	 * Private so no-one else can instantiate this class
	 */
	private FTPTransferType() {
		//
	}
}
