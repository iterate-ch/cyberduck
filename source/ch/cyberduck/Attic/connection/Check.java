package ch.cyberduck.connection;

/*
 *  Check.java
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

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

/**
 * Used to validate a requested download (ie initial, resume) action.
 * @version $Id$
 */
public class Check {
    private Bookmark transfer;
    private Status status;
    private JOptionPane dialog;
        
    public Check(Bookmark transfer, Status status) {
        Cyberduck.DEBUG("[Check] new Check()");
        this.transfer = transfer;
        this.status = status;
    }

    /**
        * @return boolean Return false if validation fails for whatever reason
     */
    public boolean validate(Message handler) throws IOException {
        status.setResume(false);
        if(handler.equals(Status.RELOAD)) {
            Cyberduck.DEBUG("[Check] Reload.");
            return this.validateOverwrite();
            /*
            if(! this.validateOverwrite())
                throw new IOException("Bookmark canceled by user.");
            return;
             */
        }
        if(handler.equals(Status.RESUME)) {
            Cyberduck.DEBUG("[Check] Resume.");
            return this.validateResume();
            /*
            if(! this.validateResume())
                throw new IOException("Bookmark canceled by user.");
            return;
             */
        }
        if(handler.equals(Status.INITIAL)) {
            Cyberduck.DEBUG("[Check] Initial.");
            return this.validateInitial();
            /*
            if(! this.validateInitial())
                throw new IOException("Bookmark canceled by user.");
            return;
             */
        }
        else {
            throw new IOException("Unknown handler.");
        }
    }
    
    private boolean validateInitial() {
        if(status.isComplete()) {
            Cyberduck.DEBUG("[Check] Bookmark already complete.");
            String[] options = {"Cancel", "Overwrite", "Similar Name"};
            int value = dialog.showOptionDialog (
                    null, 
                    "The download of '" + transfer.getLocalFilename() + "' has already \n" +
                    "been completed.", 
                    "Restart transfer",
                    dialog.DEFAULT_OPTION,
                    dialog.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            switch(value) {
                    case 0:
                            return false;
                    case 1:
                            this.prepareOverwrite();
                            return true;
                    case 2:
                            this.prepareSimilarName();
                            return true;
                    default:
                            return false;
            }
        }
        /*
        if(transfer.getLocalTempPath() == null || !transfer.getLocalTempPath().exists()) {
            this.prepareInitial();
            return true;
        }
         */
        if(! transfer.getLocalPath().exists() && !transfer.getLocalTempPath().exists()) {
            this.prepareInitial();
            return true;
        }
        else { // transfer.getLocalTempPath().exists()
            /*
            if(! (transfer.getLocalTempPath().length() > 0)) {
                this.prepareOverwrite();
                return true;
            }
             */
            //else {
            if(Preferences.instance().getProperty("duplicate.ask").equals("true")) {
                Object[] values = {"Resume", "Similar Name", "Overwrite", "Cancel"};
                int option = dialog.showOptionDialog(
                                                     null,
                                                     "The file '" + transfer.getLocalFilename() + "' already exists\n" +
                                                     "in your download directory.",
                                                     "File exists",
                                                     dialog.DEFAULT_OPTION,
                                                     dialog.QUESTION_MESSAGE,
                                                     null,
                                                     values,
                                                     values[0]
                                                     );
                switch(option) {
                    case 0:
                        this.prepareResume();
                        return true;
                    case 1:
                        this.prepareSimilarName();
                        return true;
                    case 2:
                        this.prepareOverwrite();
                        return true;
                    case 3:
                        return false;
                    default:
                        return false;
                }
            }
            if(Preferences.instance().getProperty("duplicate.similar").equals("true")) {
                this.prepareSimilarName();
                return true;
            }
            if(Preferences.instance().getProperty("duplicate.resume").equals("true")) {
                this.prepareResume();
                return true;
            }
            if(Preferences.instance().getProperty("duplicate.overwrite").equals("true")) {
                this.prepareOverwrite();
                return true;
            }
            System.err.println("[Check] Fatal error: dupliate property not set");
            //}
        }
        return false;
    }
    
    private boolean validateResume() {
        if(transfer.getLocalTempPath() != null && transfer.getLocalTempPath().exists()) {
            this.prepareResume();
            return true;
        }
        else { //if(!transfer.getLocalTempPath().exists())
            this.prepareInitial();
            return true;
        }
    }
    
    private boolean validateSimilar() {
    	this.prepareSimilarName();
    	return true;
    }
    
    private boolean validateOverwrite() {
    	this.prepareOverwrite();
    	return true;
    }
 
    private void prepareResume() {
        Cyberduck.DEBUG("[Check] prepareResume()");
        status.setResume(true);
        status.setCurrent(new Long(transfer.getLocalTempPath().length()).intValue());
    }
    
    private void prepareSimilarName() {
        Cyberduck.DEBUG("[Check] prepareSimilarName()");
        status.setResume(false);
        status.setCurrent(0);
        
        String fn = null;
        String filename = transfer.getLocalFilename();
        int no = 1;
        int index = filename.lastIndexOf(".");
        do {
            fn = filename.substring(0, index) + "_" + no + filename.substring(index);
            transfer.setLocalPath(new File(transfer.getLocalDirectory(), fn));
            no++;
        }
        while (transfer.getLocalTempPath().exists() || transfer.getLocalPath().exists());
    }
    
    private void prepareOverwrite() {
        Cyberduck.DEBUG("[Check] prepareOverwrite()");
        status.setResume(false);
        status.setCurrent(0);
    }
    
    private void prepareInitial() {
        Cyberduck.DEBUG("[Check] prepareInitial()");
        status.setResume(false);
        status.setCurrent(0);
    }
}
