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
 *
 *  Change Log:
 *
 *        $Log$
 *        Revision 1.6  2004/02/21 21:39:30  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.5  2004/02/21 12:12:27  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.4  2003/12/28 00:42:51  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.3  2003/12/15 23:14:04  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.1  2002/11/19 22:01:25  bruceb
 *        changes for 1.2
 */

package com.enterprisedt.net.ftp;

/**
 *  Encapsulates the FTP server reply
 *
 *  @author      Bruce Blackshaw
 *  @version     $Revision$
 */
public class FTPReply {

	/**
	 *  Reply code
	 */
	private String replyCode;

	/**
	 *  Reply text
	 */
	private String replyText;

    /***
		* Determine if a reply code is a positive preliminary response.  All
		* codes beginning with a 1 are positive preliminary responses.
		* Postitive preliminary responses are used to indicate tentative success.
		* No further commands can be issued to the FTP server after a positive
		* preliminary response until a follow up response is received from the
		* server.
		* <p>
		* @param reply  The reply code to test.
		* @return True if a reply code is a postive preliminary response, false
		*         if not.
		***/
    public static boolean isPositivePreliminary(int reply)
    {
        return (reply >= 100 && reply < 200);
    }
	
    /***
		* Determine if a reply code is a positive completion response.  All
		* codes beginning with a 2 are positive completion responses.
		* The FTP server will send a positive completion response on the final
		* successful completion of a command.
		* <p>
		* @param reply  The reply code to test.
		* @return True if a reply code is a postive completion response, false
		*         if not.
		***/
    public static boolean isPositiveCompletion(int reply)
    {
        return (reply >= 200 && reply < 300);
    }
	
    /***
		* Determine if a reply code is a positive intermediate response.  All
		* codes beginning with a 3 are positive intermediate responses.
		* The FTP server will send a positive intermediate response on the
		* successful completion of one part of a multi-part sequence of
		* commands.  For example, after a successful USER command, a positive
		* intermediate response will be sent to indicate that the server is
		* ready for the PASS command.
		* <p>
		* @param reply  The reply code to test.
		* @return True if a reply code is a postive intermediate response, false
		*         if not.
		***/
    public static boolean isPositiveIntermediate(int reply)
    {
        return (reply >= 300 && reply < 400);
    }
	
    /***
		* Determine if a reply code is a negative transient response.  All
		* codes beginning with a 4 are negative transient responses.
		* The FTP server will send a negative transient response on the
		* failure of a command that can be reattempted with success.
		* <p>
		* @param reply  The reply code to test.
		* @return True if a reply code is a negative transient response, false
		*         if not.
		***/
    public static boolean isNegativeTransient(int reply)
    {
        return (reply >= 400 && reply < 500);
    }
	
    /***
		* Determine if a reply code is a negative permanent response.  All
		* codes beginning with a 5 are negative permanent responses.
		* The FTP server will send a negative permanent response on the
		* failure of a command that cannot be reattempted with success.
		* <p>
		* @param reply  The reply code to test.
		* @return True if a reply code is a negative permanent response, false
		*         if not.
		***/
    public static boolean isNegativePermanent(int reply)
    {
        return (reply >= 500 && reply < 600);
    }
	
	
	/**
	 *  Constructor. Only to be constructed
	 *  by this package, hence package access
	 *
	 *  @param  replyCode  the server's reply code
	 *  @param  replyText  the server's reply text
	 */
	FTPReply(String replyCode, String replyText) {
		this.replyCode = replyCode;
		this.replyText = replyText;
	}

	/**
	 *  Getter for reply code
	 *
	 *  @return server's reply code
	 */
	public String getReplyCode() {
		return replyCode;
	}

	/**
	 *  Getter for reply text
	 *
	 *  @return server's reply text
	 */
	public String getReplyText() {
		return replyText;
	}
}

/*
 public static final int CODE_110 = 110;
 public static final int CODE_120 = 120;
 public static final int CODE_125 = 125;
 public static final int CODE_150 = 150;
 public static final int CODE_200 = 200;
 public static final int CODE_202 = 202;
 public static final int CODE_211 = 211;
 public static final int CODE_212 = 212;
 public static final int CODE_213 = 213;
 public static final int CODE_214 = 214;
 public static final int CODE_215 = 215;
 public static final int CODE_220 = 220;
 public static final int CODE_221 = 221;
 public static final int CODE_225 = 225;
 public static final int CODE_226 = 226;
 public static final int CODE_227 = 227;
 public static final int CODE_230 = 230;
 public static final int CODE_250 = 250;
 public static final int CODE_257 = 257;
 public static final int CODE_331 = 331;
 public static final int CODE_332 = 332;
 public static final int CODE_350 = 350;
 public static final int CODE_421 = 421;
 public static final int CODE_425 = 425;
 public static final int CODE_426 = 426;
 public static final int CODE_450 = 450;
 public static final int CODE_451 = 451;
 public static final int CODE_452 = 452;
 public static final int CODE_500 = 500;
 public static final int CODE_501 = 501;
 public static final int CODE_502 = 502;
 public static final int CODE_503 = 503;
 public static final int CODE_504 = 504;
 public static final int CODE_530 = 530;
 public static final int CODE_532 = 532;
 public static final int CODE_550 = 550;
 public static final int CODE_551 = 551;
 public static final int CODE_552 = 552;
 public static final int CODE_553 = 553;
 
 public static final int RESTART_MARKER = CODE_110;
 public static final int SERVICE_NOT_READY = CODE_120;
 public static final int DATA_CONNECTION_ALREADY_OPEN = CODE_125;
 public static final int FILE_STATUS_OK = CODE_150;
 public static final int COMMAND_OK = CODE_200;
 public static final int COMMAND_IS_SUPERFLUOUS = CODE_202;
 public static final int SYSTEM_STATUS = CODE_211;
 public static final int DIRECTORY_STATUS = CODE_212;
 public static final int FILE_STATUS = CODE_213;
 public static final int HELP_MESSAGE = CODE_214;
 public static final int NAME_SYSTEM_TYPE = CODE_215;
 public static final int SERVICE_READY = CODE_220;
 public static final int SERVICE_CLOSING_CONTROL_CONNECTION = CODE_221;
 public static final int DATA_CONNECTION_OPEN = CODE_225;
 public static final int CLOSING_DATA_CONNECTION = CODE_226;
 public static final int ENTERING_PASSIVE_MODE = CODE_227;
 public static final int USER_LOGGED_IN = CODE_230;
 public static final int FILE_ACTION_OK = CODE_250;
 public static final int PATHNAME_CREATED = CODE_257;
 public static final int NEED_PASSWORD = CODE_331;
 public static final int NEED_ACCOUNT = CODE_332;
 public static final int FILE_ACTION_PENDING = CODE_350;
 public static final int SERVICE_NOT_AVAILABLE = CODE_421;
 public static final int CANNOT_OPEN_DATA_CONNECTION = CODE_425;
 public static final int TRANSFER_ABORTED = CODE_426;
 public static final int FILE_ACTION_NOT_TAKEN = CODE_450;
 public static final int ACTION_ABORTED = CODE_451;
 public static final int INSUFFICIENT_STORAGE = CODE_452;
 public static final int UNRECOGNIZED_COMMAND = CODE_500;
 public static final int SYNTAX_ERROR_IN_ARGUMENTS = CODE_501;
 public static final int COMMAND_NOT_IMPLEMENTED = CODE_502;
 public static final int BAD_COMMAND_SEQUENCE = CODE_503;
 public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = CODE_504;
 public static final int NOT_LOGGED_IN = CODE_530;
 public static final int NEED_ACCOUNT_FOR_STORING_FILES = CODE_532;
 public static final int FILE_UNAVAILABLE = CODE_550;
 public static final int PAGE_TYPE_UNKNOWN = CODE_551;
 public static final int STORAGE_ALLOCATION_EXCEEDED = CODE_552;
 public static final int FILE_NAME_NOT_ALLOWED = CODE_553;
*/
