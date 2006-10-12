package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSAttributedString;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class CDDownloadQueueValidatorController extends CDValidatorController {
    private static Logger log = Logger.getLogger(CDDownloadQueueValidatorController.class);

    public CDDownloadQueueValidatorController(final Queue queue) {
        super(queue);
        synchronized(CDQueueController.instance()) {
            if(!NSApplication.loadNibNamed("Validator", this)) {
                log.fatal("Couldn't load Validator.nib");
            }
            this.setEnabled(false);
        }
    }

    public List getResult() {
        List result = new ArrayList();
        result.addAll(this.validatedList);
        result.addAll(this.workList);
        return result;
    }

    protected boolean validateDirectory(Path path) {
        return true;
    }

    protected boolean validateFile(Path p, boolean resumeRequested) {
        if(resumeRequested) { // resume existing files independant of settings in preferences
            p.reset();
            p.status.setResume(p.getLocal().exists() && p.getLocal().getSize() > 0);
            return true;
        }
        // When overwriting file anyway we don't have to check if the file already exists
        if(Preferences.instance().getProperty("queue.download.fileExists").equals(OVERWRITE)) {
            log.info("Apply validation rule to overwrite file " + p.getName());
            p.status.setResume(false);
            return true;
        }
        p.reset();
        if(p.getLocal().exists() && p.getLocal().getSize() > 0) {
            if(Preferences.instance().getProperty("queue.download.fileExists").equals(RESUME)) {
                log.debug("Apply validation rule to resume:" + p.getName());
                p.status.setResume(true);
                return true;
            }
            if(Preferences.instance().getProperty("queue.download.fileExists").equals(SIMILAR)) {
                log.debug("Apply validation rule to apply similar name:" + p.getName());
                p.status.setResume(false);
                this.adjustFilename(p);
                log.info("Changed local name to " + p.getName());
                return true;
            }
            if(Preferences.instance().getProperty("queue.download.fileExists").equals(ASK)) {
                log.debug("Apply validation rule to ask:" + p.getName());
                this.prompt(p);
                return false;
            }
            throw new IllegalArgumentException("No rules set to validate transfers");
        }
        else {
            p.status.setResume(false);
            return true;
        }
    }

    protected void prompt(Path p) {
        this.workList.add(p);
        super.prompt(p);
    }

    protected void adjustFilename(Path path) {
        String parent = path.getLocal().getParent();
        String filename = path.getLocal().getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            path.setLocal(new Local(parent, proposal));
            no++;
            if(index != -1 && index != 0) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while(path.getLocal().exists());
    }

    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if(row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            Path p = (Path) this.workList.get(row);
            if(p != null) {
                if(identifier.equals(WARNING_COLUMN)) {
                    if(p.getRemote().attributes.getSize() == 0) {
                        return ALERT_ICON;
                    }
                    if(p.getLocal().attributes.getSize() > p.getRemote().attributes.getSize()) {
                        return ALERT_ICON;
                    }
                }
                if(identifier.equals(SIZE_COLUMN)) {
                    return new NSAttributedString(Status.getSizeAsString(p.attributes.getSize()),
                            CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
                }
            }
            return super.tableViewObjectValueForLocation(view, tableColumn, row);
        }
        return null;
    }
}