package org.apache.commons.net.ftp.parser;

/*
* Copyright 2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

import java.util.Calendar;

/**
 * Example MVS directory listing
 * <p/>
 * Volume Unit    Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname
 * 0CM142 3390   2001/02/21  3    8  FB     132 27984  PO  DUMP.T12.A9883
 * 0CM161 3390   2001/03/07  1    1  FB      80 27920  PS  DUMP.T12.A9932
 * 0CM666 3390   2001/03/07  1    2  FB      80  4080  PO  QUNA.ISPPLIB
 * 0CM674 3390   2001/03/08  1   15  FB      80 27920  PO  DERKI.MUSX.LOAD
 * 0CM625 3390   2001/03/09  1    3  VB     255 27998  PO  SWANP.PNAL.PANEL
 * 0CM495 3390   2001/03/12  1   12  FB     133 27930  PO  DIK.MAN.C
 */
public class MVSFTPEntryParser extends RegexFTPFileEntryParserImpl {

    private static final String DSORG =
            "(PO|PS|VSAM)";

    private static final String RECFM =
            "(VB|FB|FBS|U|VBS)";

    private static final String REGEX =
            "([A-Z0-9]{6})\\s+"                 // Volume
                    + "([0-9]{4})\\s+"                   // Unit
                    + "([0-9]{4})/([0-9]{2})/([0-9]{2})\\s+"   // year/month/day
                    + "([0-9]{1,3})\\s+"                 // Extends
                    + "([0-9]{1,3})\\s+"                 // Used
                    + RECFM + "\\s+"                       // Record Format
                    + "([0-9]{1,4})\\s+"                 // Logical Record Length
                    + "([0-9]{1,5})\\s+"                 // Block Size
                    + DSORG + "\\s+"                       // Dataset Organisation
                    + "((([A-Z0-9#.]{1,8})[.]?){2,6})+"; // Dataset Name

    public MVSFTPEntryParser() {
        super(REGEX);
    }

    public Path parseFTPEntry(Path parent, String entry) {
        Path f = PathFactory.createPath(parent.getSession());

        if(matches(entry)) {
            //String volume  = group(1);
            //String unit    = group(2);
            String year = group(3);
            String month = group(4);
            String day = group(5);
            //String ext     = group(6);
            //String used    = group(7);
            //String recfm   = group(8);
            //String lrecl   = group(9);
            //String blksize = group(10);
            String dsorg = group(11);
            String dsname = group(12);

            if(dsname == null || dsname.equals("")) {
                return null;
            }

            f.attributes.setSize(0);

            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            try {
                cal.set(Calendar.YEAR, Integer.parseInt(year, 10));
                cal.set(Calendar.MONTH, Integer.parseInt(month, 10) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day, 10));

                f.attributes.setTimestamp(cal.getTime().getTime());
            }
            catch(NumberFormatException e) {
                // do nothing, date will be uninitialized
            }

            f.setPath(dsname);
            f.attributes.setType(((dsorg.equals("PS")) ?
                    Path.FILE_TYPE :
                    Path.DIRECTORY_TYPE));

            return f;
        }
        return null;
    }
}
