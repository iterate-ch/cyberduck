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

import java.io.IOException;

/**
 * FTP specific exceptions
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPException extends IOException {

	/**
	 * Integer reply code
	 */
	private int replyCode = -1;

	/**
	 * Constructor. Delegates to super.
	 *
	 * @param msg Message that the user will be
	 *            able to retrieve
	 */
	public FTPException(String msg) {
		super(msg);
	}

	/**
	 * Constructor. Permits setting of reply code
	 *
	 * @param msg       message that the user will be
	 *                  able to retrieve
	 * @param replyCode string form of reply code
	 */
	public FTPException(String msg, String replyCode) {

		super(msg);

		// extract reply code if possible
		try {
			this.replyCode = Integer.parseInt(replyCode);
		}
		catch(NumberFormatException ex) {
			this.replyCode = -1;
		}
	}

	/**
	 * Constructor. Permits setting of reply code
	 *
	 * @param reply reply object
	 */
	public FTPException(FTPReply reply) {

		super(reply.getReplyText());

		// extract reply code if possible
		try {
			this.replyCode = Integer.parseInt(reply.getReplyCode());
		}
		catch(NumberFormatException ex) {
			this.replyCode = -1;
		}
	}

	/**
	 * Get the reply code if it exists
	 *
	 * @return reply if it exists, -1 otherwise
	 */
	public int getReplyCode() {
		return replyCode;
	}
}
