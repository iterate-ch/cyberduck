/**
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003  Enterprise Distributed Technologies Ltd
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
 *        Revision 1.7  2004/11/02 12:26:27  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.3  2004/07/23 08:27:19  bruceb
 *        made public cvsId
 *
 *        Revision 1.2  2002/11/19 22:01:25  bruceb
 *        changes for 1.2
 *
 *        Revision 1.1  2001/10/09 20:53:46  bruceb
 *        Active mode changes
 *
 *        Revision 1.1  2001/10/05 14:42:04  bruceb
 *        moved from old project
 *
 *
 */

package com.enterprisedt.net.ftp;

/**
 *  Enumerates the connect modes that are possible,
 *  active & PASV
 *
 *  @author     Bruce Blackshaw
 *  @version    $Revision$
 *
 */
 public class FTPConnectMode {

     /**
      *  Revision control id
      */
     public static String cvsId = "@(#)$Id$";

     /**
      *   Represents active connect mode
      */
     public static FTPConnectMode ACTIVE = new FTPConnectMode();

     /**
      *   Represents PASV connect mode
      */
     public static FTPConnectMode PASV = new FTPConnectMode();

     /**
      *  Private so no-one else can instantiate this class
      */
     private FTPConnectMode() {
     }
 }
