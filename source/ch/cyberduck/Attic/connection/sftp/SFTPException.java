

package ch.cyberduck.connection.sftp;

import ch.cyberduck.connection.SessionException;

/**
 *  FTP specific exceptions
 *
 *  @author     Bruce Blackshaw
 *  @version    $Revision$
 * @version $Id$
 *
 */
public class SFTPException extends SessionException {

    /**
     *   Constructor. Delegates to super.
     *
     *   @param   msg   Message that the user will be
     *                  able to retrieve
     */
    public SFTPException(String msg) {
        super(msg);
    }

    /**
     *  Constructor. Permits setting of reply code
     *
     *   @param   msg        message that the user will be
     *                       able to retrieve
     *   @param   replyCode  string form of reply code
     */
    public SFTPException(String msg, String replyCode) {
        super(msg, replyCode);
    }
}
