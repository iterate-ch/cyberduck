package ch.cyberduck.connection.sftp;

/*
 *  ch.cyberduck.connection.ftp.Parser.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import java.util.*;
import java.io.IOException;

import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.FileAttributes;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Path;
import ch.cyberduck.connection.Permission;

public class SFTPParser {

    private SFTPParser() {
        super();
    }

    public static List parseList(String parent, List children) throws IOException {
//        Cyberduck.DEBUG("[FTPParser] parseList(" + parent + "," + list + ")");
        List parsedList = new ArrayList();
	SftpFile file;
	Iterator i = children.iterator();
	boolean showHidden = Preferences.instance().getProperty("ftp.showHidden").equals("true");
	while(i.hasNext()) {
	    file = (SftpFile)i.next();
	    FileAttributes attributes = file.getAttributes();
	    Cyberduck.DEBUG("***"+file.getAbsolutePath());
//	    Cyberduck.DEBUG(file.isFile() ? "File" : "Directory");
//	    Cyberduck.DEBUG("   Size:"+attributes.getSize().toString());
	    Cyberduck.DEBUG("   Permissions:"+attributes.getPermissions().toString());
	    Path p = new Path(file.getAbsolutePath());
	    if(file.isDirectory())
		p.setMode("d---------");
		//p = new Path(file.getAbsolutePath()+"/");
	    else
		p.setMode("----------");
//		p = new Path(file.getAbsolutePath());
	    p.setSize(attributes.getSize().intValue());
//	    p.setOwner();
//	    p.setPermission(attributes.getPermissions().intValue());
	    p.setModified(attributes.getModifiedTime().intValue());
	    String filename = p.getName();
	    if(!(filename.equals(".") || filename.equals(".."))) {
		if(!showHidden) {
		    if(filename.charAt(0) == '.') {
			p.setVisible(false);
		    }
		}
		parsedList.add(p);
	    }
	}
        return parsedList;
    }
}
