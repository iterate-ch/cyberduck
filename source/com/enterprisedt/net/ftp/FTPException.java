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
    private int reply = -1;

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
     * @param msg   message that the user will be
     *              able to retrieve
     * @param reply string form of reply code
     */
    public FTPException(String msg, String reply) {

        super(msg);

        // extract reply code if possible
        try {
            this.reply = Integer.parseInt(reply);
        }
        catch (NumberFormatException ex) {
            this.reply = -1;
        }
    }


    /**
     * Get the reply code if it exists
     *
     * @return reply if it exists, -1 otherwise
     */
    public int getReplyCode() {
        return reply;
    }

    /**
     * Determine if a reply code is a positive preliminary response.  All
     * codes beginning with a 1 are positive preliminary responses.
     * Postitive preliminary responses are used to indicate tentative success.
     * No further commands can be issued to the FTP server after a positive
     * preliminary response until a follow up response is received from the
     * server.
     * <p/>
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a postive preliminary response, false
     *         if not.
     */
    public boolean isPositivePreliminary() {
        return (reply >= 100 && reply < 200);
    }

    /**
     * Determine if a reply code is a positive completion response.  All
     * codes beginning with a 2 are positive completion responses.
     * The FTP server will send a positive completion response on the final
     * successful completion of a command.
     * <p/>
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a postive completion response, false
     *         if not.
     */
    public boolean isPositiveCompletion() {
        return (reply >= 200 && reply < 300);
    }

    /**
     * Determine if a reply code is a positive intermediate response.  All
     * codes beginning with a 3 are positive intermediate responses.
     * The FTP server will send a positive intermediate response on the
     * successful completion of one part of a multi-part sequence of
     * commands.  For example, after a successful USER command, a positive
     * intermediate response will be sent to indicate that the server is
     * ready for the PASS command.
     * <p/>
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a postive intermediate response, false
     *         if not.
     */
    public boolean isPositiveIntermediate() {
        return (reply >= 300 && reply < 400);
    }

    /**
     * Determine if a reply code is a negative transient response.  All
     * codes beginning with a 4 are negative transient responses.
     * The FTP server will send a negative transient response on the
     * failure of a command that can be reattempted with success.
     * <p/>
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a negative transient response, false
     *         if not.
     */
    public boolean isNegativeTransient() {
        return (reply >= 400 && reply < 500);
    }

    /**
     * Determine if a reply code is a negative permanent response.  All
     * codes beginning with a 5 are negative permanent responses.
     * The FTP server will send a negative permanent response on the
     * failure of a command that cannot be reattempted with success.
     * <p/>
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a negative permanent response, false
     *         if not.
     */
    public boolean isNegativePermanent() {
        return (reply >= 500 && reply < 600);
    }
}
