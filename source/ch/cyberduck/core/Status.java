package ch.cyberduck.core;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
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

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Timer;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;
/**
  * The Status class is the model of a download's status. The view of this is represented by
  * the <code>StatusPanel</code> notified by the methods available from the <code>Observable</code> class.
  * To get notifed of my status register via <code>registerObserver()</code> of <code>BookmarkPanel</code>.
  * @version $Id$
  */
public class Status extends Observable implements Serializable {

    private static Logger log = Logger.getLogger(Status.class);

/*
    public static final Icon GREEN_ICON = Status.getIcon(Status.getResource("blipGreen.gif"));
    public static final Icon BLUE_ICON = Status.getIcon(Status.getResource("blipBlue.gif"));
    public static final Icon RED_ICON = Status.getIcon(Status.getResource("blipRed.gif"));
    public static final Icon GRAY_ICON = Status.getIcon(Status.getResource("blipGray.gif"));


    public static URL getResource(String name) {
        URL url= Status.class.getResource(name);
        if (url == null) {
            log.warn("Failed to load resource '" + name + "'.");
        }
        return url;
    }
    
    public static Icon getIcon(URL url) {
        Icon image = null;
        try {
            image = new ImageIcon(url);
        }
        catch(Exception e) {
            log.warn("Failed to load image.");
        }
        return image;
    }
    */


    /**
      * Progress trackers.
      */
    private transient Timer chronoTimer, currentSpeedTimer, overallSpeedTimer;//, timeLeftTimer;

    private boolean canceled;
    /**
     * Indicates that the last action has been completed.
     */
    private boolean complete = false;
    /**
     * The last actioin has been stopped, but must not be completed.
     */
    private transient boolean stopped = true;
    /**
     * Download is resumable
     */
    private transient boolean resume = false;    

    /**
     * I am selected in the table view
     */
    private boolean selected;

    public void setSelected(boolean s) {
        this.selected = s;
    }
    public boolean isSelected() {
        return this.selected;
    }

    // Messages sent to observers as argument to update only specific components
    // different type of status messages (eg for jlabels in status panel)
    // milestones of the transfer
//    public static final Message ACTIVE = new Message ("ACTIVE");
//    public static final Message STOP = new Message ("STOP");
//    public static final Message COMPLETE = new Message ("COMPLETE");
    // panel messages - check if the appropriate panel is displayed.    
//    public static final String PROGRESSPANEL = new String("PROGRESSPANEL");
////    public static final String EDITPANEL = new String("EDITPANEL");
//    public static final String LOGINPANEL = new String("LOGINPANEL");            
//    public static final String LISTPANEL = new String("LISTPANEL");
    /** progress noted */
//    public static final Message CURRENT = new Message("CURRENT");
    /**
     * Indiciating an initial transfer
     */
//    public static final Message INITIAL = new Message("INITIAL");
    /**
     * Indiciating a resumable transfer
     */
//    public static final Message RESUME = new Message("RESUME");
    /**
     * Indicating that the we want to reload the data
     */
//    public static final Message RELOAD = new Message("RELOAD");
    /**
        * An error message
     */
//    public static final Message ERROR = new Message("ERROR");
    /**
        * Time counter message
     */
  //  public static final Message TIME = new Message("TIME");
    /**
        * Something like 'Connecting...'
     */
   // public static final Message PROGRESS = new Message("PROGRESS");
    /**
        * A message about the transfered bytes
     */
   // public static final Message DATA = new Message("DATA");
    /**
        * Write this message to the log
     */
//    public static final Message LOG = new Message("LOG");
    /**
        * Write this mesage to the transcript
     */
    
    private int current = 0;
    /*
     * current speed (bytes/second)
     */
    private transient double speed = 0;
    /*
     * overall speed (bytes/second)
     */
    private transient double overall = 0;
    /*
     * the size of the file
     */
    private int length = -1;
    /*
     * timee left in seconds ((length-current)/speed)
     */
            
    private Calendar calendar = Calendar.getInstance();
    private DateFormat df = DateFormat.getTimeInstance();
    
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;

    //@todo
//    private final static transient AudioClip startSound = Applet.newAudioClip(Cyberduck.getResource("start.au"));
//    private final static transient AudioClip stopSound = Applet.newAudioClip(Cyberduck.getResource("stop.au"));
//    private final static transient AudioClip completeSound = Applet.newAudioClip(Cyberduck.getResource("complete.au"));

    /**
     * The wrapper for any status informations of a transfer like it's length and transferred
     * bytes.
     */
    public Status () {
        log.debug("new Status()");

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        /*
        timeLeftTimer = new Timer(1000, 
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setTimeLeft((int)((getLength() - getCurrent())/getSpeed()));
                }
            }
        );
         */
    }

    /**
        * Notify all observers
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Object arg) {
//        log.debug("[Bookmark] Notifying " + this.countObservers() + " observers.");
//        long start = System.currentTimeMillis();
        this.setChanged();
//@todo        if(this.isSelected())
	this.notifyObservers(arg);
    }


    // @todo replace this with HashMap messageMap
//    private String errormessage = "";
    private String progressmessage = "Idle";
//    private String transcriptmessage = "";
    private String timemessage = "00:00";
    
    // @todo replace this with messageMap.put(type, msg);
    public void setMessage(String message, String title) {
//	log.debug("setMessage("+message+","+title);
	Message msg = new Message(title, message);
	if(title.equals(Message.PROGRESS)) {
	    this.progressmessage = message;
	    msg = new Message(Message.PROGRESS, timemessage+" "+message);
	}
	if(title.equals(Message.TIME)) {
	    this.timemessage = message;
	    msg = new Message(Message.PROGRESS, message+" "+progressmessage);
	}
	
        this.callObservers(msg);
    }

    // @todo replace this with messageMap.get(type);
    /**
     * @return The status string
     * @param type The type of message to return
     * @see ch.cyberduck.core.Message
     */
    public String getMessage(Message type) {
//        log.debug("getMessage(" + type + ")");
	/*
        if(type.equals(Message.TIME)) {
            return timemessage;
        }
        if(type.equals(Message.DATA)) {
            if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
                return this.parseDouble(this.getCurrent()/1024) + " of " + this.parseDouble(this.getLength()/1024) + " kBytes.";
            }
            else {
                if(this.getOverall() <= 0) {
                    return this.parseDouble(this.getCurrent()/1024) + " of "
                    + this.parseDouble(this.getLength()/1024) + " kBytes. Current: " +
                    + this.parseDouble(this.getSpeed()/1024) + "kB/s. ";// + this.getTimeLeftMessage();
                }
                else {
                    return this.parseDouble(this.getCurrent()/1024) + " of "
                    + this.parseDouble(this.getLength()/1024) + " kBytes. Current: "
                    + this.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
                    + this.parseDouble(this.getOverall()/1024) + " kB/s. ";// + this.getTimeLeftMessage();
                }
            }
        }
        if(type.equals(Message.ERROR)) {
            return errormessage;
        }
        if(type.equals(Message.PROGRESS)) {
            return progressmessage;
        }
        if(type.equals(Message.TRANSCRIPT)) {
            return transcriptmessage;
        }
	 */
        throw new IllegalArgumentException("Can't find message of type " + type.toString());
    }
    
    private String parseTime(int t) {
        if(t > 9) {
            return String.valueOf(t);
        }
        else {
            return "0" + t;
        }
    }
    
    private double parseDouble(double d) {
        //log.debug("parseDouble(" + d + ")");
        String s = Double.toString(d);
        if(s.indexOf(".") != -1) {
            int l = s.substring(s.indexOf(".")).length();
            if(l > 3) {
                return Double.parseDouble(s.substring(0, s.indexOf('.') + 3));
            }
            else {
                return Double.parseDouble(s.substring(0, s.indexOf('.') + l));
            }
        }
        else {
            return d;
        }
    }

/*
    public javax.swing.Icon getIcon() {
        if(this.isComplete()) {
            return GREEN_ICON;
        }
        if(!this.isStopped()) {
            return BLUE_ICON;
        }
        if(this.getLength() > this.getCurrent()) {
            return RED_ICON;
        }
        return GRAY_ICON;
    }
    */

    // ZUSTAENDE
    private void setComplete(boolean b) {
        this.complete = b;
    }
    public boolean isComplete() {
        return this.complete;
    }    
    
    private void setStopped(boolean b) {
        this.stopped = b;
    }
    public boolean isStopped() {
        return this.stopped;
    } 

    public void setCanceled(boolean b) {
        canceled = b;
    }

    public boolean isCancled() {
        return canceled;
    }
    
    /**
     * Notify that the connection has been opened.
     */
    public void fireActiveEvent() {
        log.debug("fireActiveEvent()");
        this.reset();
        this.setCanceled(false);
        this.setComplete(false);
        this.setStopped(false);
        this.overallSpeedTimer.start();
        this.currentSpeedTimer.start();
        this.chronoTimer.start();
        if(!ignoreEvents) {
            if(Preferences.instance().getProperty("status.sound.start").equals("true")) {
//@todo                startSound.play();
            }
            this.callObservers(new Message(Message.ACTIVE, null));
        }
    }    

    /**
     * Notify that the connection has been closed.
     */
    public void fireStopEvent() {
        log.debug("fireStopEvent()");
        if(this.currentSpeedTimer != null)
            this.currentSpeedTimer.stop();
        if(this.overallSpeedTimer != null)
            this.overallSpeedTimer.stop();
        if(this.chronoTimer != null)
            this.chronoTimer.stop();
        if(!ignoreEvents) {
            if(Preferences.instance().getProperty("status.sound.stop").equals("true") && !this.isComplete()) {
//@todo                stopSound.play();
            }
            this.setStopped(true);
            this.setResume(false);
            this.callObservers(new Message(Message.STOP, null));
        }
    }

    /**
     * Notify that the transfer has been completed.
     */
    public void fireCompleteEvent() {
        log.debug("fireCompleteEvent()");
        this.currentSpeedTimer.stop();
        this.overallSpeedTimer.stop();
        this.chronoTimer.stop();
        if(!ignoreEvents) {
            if(Preferences.instance().getProperty("status.sound.complete").equals("true")) {
//@todo                completeSound.play();
            }
            this.setStopped(true);
            this.setResume(false);
            this.setComplete(true);
            this.callObservers(new Message(Message.COMPLETE, null));
        }
    }

    private transient boolean ignoreEvents = false;
    
    public void ignoreEvents(boolean ignore) {
        this.ignoreEvents = ignore;
    }
            
    public void setResume(boolean value) {
        this.resume = value;
    }
    public boolean isResume() {
        return this.resume;
    }

    public int getCurrent() {
        return current;
    }
    /**
     * @param c The currently transfered bytes
     */
    public void setCurrent(int c) {
//        log.debug("setCurrent(" + c + ")");
        this.current = c;

	Message msg = null;
	if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
	    msg = new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of " + this.parseDouble(this.getLength()/1024) + " kBytes.");
	}
	else {
	    if(this.getOverall() <= 0) {
		msg = new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of "
                    + this.parseDouble(this.getLength()/1024) + " kBytes. Current: " +
                    + this.parseDouble(this.getSpeed()/1024) + "kB/s. ");// + this.getTimeLeftMessage();
	    }
	    else {
		msg = new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of "
                    + this.parseDouble(this.getLength()/1024) + " kBytes. Current: "
                    + this.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
                    + this.parseDouble(this.getOverall()/1024) + " kB/s. ");// + this.getTimeLeftMessage();
	    }
	}


	this.callObservers(msg);
    }

    /**
        * @return double current bytes/second
     */
    private double getSpeed() {
        return this.speed;
    }
    private void setSpeed(double s) {
        this.speed = s;
    }

    /**
     * @return double bytes per seconds transfered since the connection has been opened
     */
    private double getOverall() {
        return this.overall;
    }
    private void setOverall(double s) {
	this.overall = s;
    }

    /*
    private void setTimeLeft(int seconds) {
        this.left = seconds;
    }
    private int getTimeLeft() {
        return this.left;
    }
     */
    /*
    private String getTimeLeftMessage() {
        int s = this.getTimeLeft();
        String message = "";
        /*@todo: implementation of better 'time left' management.
        if(s != -1) {
            if(s >= 60) {
                message = (int)s/60 + " minutes remaining.";
            }
            else {
                message = s + " seconds remaining.";
            }
        }
        return message;
    }
     */

    /**
     * @ param length the size of file in bytes.
     */
    public void setLength(int length) {
        log.debug("setLength("+length+")");
        this.length = length;
    }
    
    /**
     * @ return length the size of file in bytes.
     */
    public int getLength() {
        return length;
    }

    public BoundedRangeModel getProgressModel() {
        DefaultBoundedRangeModel m = null;
        try {
            if(this.getLength() < 0) {
                m = new DefaultBoundedRangeModel(0, 0, 0, 100);
            }
            m = new DefaultBoundedRangeModel(this.getCurrent(), 0, 0, this.getLength());
        }
        catch(IllegalArgumentException e) {
            m = new DefaultBoundedRangeModel(0, 0, 0, 100);
        }
        return m;
    }

    /*
    public String getPanelProperty() {
        if(this.panelProperty == null)
            this.panelProperty = this.getDefaultPanelProperty();
        return this.panelProperty;
    }

    public String getLastPanelProperty() {
        if(this.lastPanelProperty == null || this.panelProperty.equals(this.lastPanelProperty))
            return this.getDefaultPanelProperty();
        return lastPanelProperty;
    }

    public String getDefaultPanelProperty() {
        return PROGRESSPANEL;
    }
*/

    // initial panel property
    /*
    private String panelProperty;
    private String lastPanelProperty;
    
    public void setPanelProperty(String newPanelProperty) {
        log.debug("setPanelProperty("+newPanelProperty+")");
        if(lastPanelProperty != newPanelProperty) {
            this.lastPanelProperty = this.panelProperty;
        }
        if(panelProperty != newPanelProperty) {
            this.panelProperty = newPanelProperty;
            this.callObservers(newPanelProperty);
        }
    }
     */

    /**
     * reset messages and timers
     */
    private void reset() {
        log.debug("reset()");
        this.speed = 0;
        this.overall = 0;
//	this.errormessage = "";
	//this.infomessage = "";
        this.timemessage = "00:00";

        if(overallSpeedTimer == null) {
        overallSpeedTimer = new Timer(4000,
            new ActionListener() {
                Vector overall = new Vector();
                double current;
                double last;
                public void actionPerformed(ActionEvent e) {
                    //                    log.debug("overallSpeedTimer:actionPerformed()");
                    current = getCurrent();
                    if(current <= 0) {
                        setOverall(0);
                    }
                    else {
                        overall.add(new Double((current - last)/4)); // bytes transferred for the last 4 seconds
                        Iterator iterator = overall.iterator();
                        double sum = 0;
                        while(iterator.hasNext()) {
                            Double s = (Double)iterator.next();
                            sum = sum + s.doubleValue();
                        }
                        setOverall((sum/overall.size()));
                        last = current;
                        //                        log.debug("overallSpeed " + sum/overall.size()/1024 + " KBytes/sec");
                    }
                }
            }
            );
        }

        if(currentSpeedTimer == null) {
        currentSpeedTimer = new Timer(500,
            new ActionListener() {
                int i = 0;
                int current;
                int last;
                int[] speeds = new int[8];
                public void actionPerformed(ActionEvent e) {
                    //                    log.debug("currentSpeedTimer:actionPerformed()");
                    int diff = 0;
                    current = getCurrent();
                    if(current <= 0) {
                        setSpeed(0);
                    }
                    else {
                        speeds[i] = (current - last)*(2); i++; last = current;
                        if(i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
                            i = 0;
                        }

                        for (int k = 0; k < speeds.length; k++) {
                            diff = diff + speeds[k]; // summe der differenzen zwischen einer halben sekunde
                        }

                        //                        log.debug("currentSpeed " + diff/speeds.length/1024 + " KBytes/sec");
                        setSpeed((diff/speeds.length));
                    }
                }
            }
            );
        }

        if(chronoTimer == null) {
        chronoTimer = new Timer(1000,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    //                    log.debug("chronoTimer:actionPerformed()");
                    seconds++;
                    // calendar.set(year, mont, date, hour, minute, second)
                    // >= one hour
                    if(seconds >= 3600) {
                        hours = (int)(seconds/60/60);
                        minutes = (int)((seconds - hours*60*60)/60);
                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), hours, minutes, seconds - minutes*60);
                    }
                    else {
                        // >= one minute
                        if(seconds >= 60) {
                            minutes = (int)(seconds/60);
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), minutes, seconds - minutes*60);
                        }
                        // only seconds
                        else {
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), seconds);
                        }
                    }

                    // der variable timemessage den neuen wert zuweisen.
                    if(calendar.get(Calendar.HOUR) > 0) {
                        setMessage(parseTime(calendar.get(Calendar.HOUR)) + ":" + parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND)), Message.TIME);
                    }
                    else {
                        setMessage(parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND)), Message.TIME);
                    }
                }
            }
            );
        }
    }

    public String toString() {
        return "Status:" + "Stopped=" + isStopped() + ", Complete=" + isComplete() + ", Resume=" + isResume() + ", Current=" + getCurrent() + ", Speed=" + getSpeed() + ", Overall=" + getOverall();
    }
} 
