package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.*;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.ui.cocoa.CDQueueController;

/**
 * @version $Id$
 */
public class Queue extends Observable implements Observer {
    private static Logger log = Logger.getLogger(Queue.class);

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    /**
     * What kind of this, either KIND_DOWNLOAD or KIND_UPLOAD
     */
    private int kind;

    private List roots = new ArrayList();

    private String status = "";
	
	private Worker worker;

    /**
     * Creating an empty queue containing no items. Items have to be added later
     * using the <code>addRoot</code> method.
     * 
     * @param kind Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
     */
    public Queue(int kind) {
        this.kind = kind;
		this.worker = new Worker(this, null);
    }

    public Queue(NSDictionary dict) {
        Object kindObj = dict.objectForKey("Kind");
        if (kindObj != null) {
            this.kind = Integer.parseInt((String)kindObj);
        }
        Object hostObj = dict.objectForKey("Host");
        if (hostObj != null) {
            Host host = new Host((NSDictionary)hostObj);
            Session s = SessionFactory.createSession(host);
            Object rootsObj = dict.objectForKey("Roots");
            if (rootsObj != null) {
                NSArray r = (NSArray)rootsObj;
                for (int i = 0; i < r.count(); i++) {
                    this.addRoot(PathFactory.createPath(s, (NSDictionary)r.objectAtIndex(i)));
                }
            }
			/*
            Object itemsObj = dict.objectForKey("Items");
            if (itemsObj != null) {
                NSArray items = (NSArray)itemsObj;
                if (null != items) {
                    for (int i = 0; i < items.count(); i++) {
                        this.jobs.add(PathFactory.createPath(s, (NSDictionary)items.objectAtIndex(i)));
                    }
                }
            }
			 */
			this.worker = new Worker(this, null);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.kind + "", "Kind");
        dict.setObjectForKey(this.getRoot().getHost().getAsDictionary(), "Host");
        NSMutableArray r = new NSMutableArray();
        for (Iterator iter = this.roots.iterator(); iter.hasNext();) {
            r.addObject(((Path)iter.next()).getAsDictionary());
        }
        dict.setObjectForKey(r, "Roots");
		/*
        NSMutableArray items = new NSMutableArray();
        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            items.addObject(((Path)iter.next()).getAsDictionary());
        }
        dict.setObjectForKey(items, "Items");
		 */
        return dict;
    }

    /**
     * Add an item to the queue
     *
     * @param item The path to be added in the queue
     */
    public void addRoot(Path item) {
        if (log.isDebugEnabled()) {
            log.debug("add:" + item);
        }
        this.roots.add(item);
    }

	public Path getRoot() {
        return (Path)roots.get(0);
    }
		
    public List getRoots() {
        return this.roots;
    }

    /**
     * @return Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
     */
    public int kind() {
        return this.kind;
    }

	/**
	 * Notify all observers
     *
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Object arg) {
        this.setChanged();
        this.notifyObservers(arg);
    }
		
    public void update(Observable o, Object arg) {
        if (arg instanceof Message) {
            Message msg = (Message)arg;
            if (msg.getTitle().equals(Message.PROGRESS)) {
                this.status = (String)msg.getContent();
			}
		}
		this.callObservers(arg);
    }
	
    /**
     * Process the queue. All files will be downloaded or uploaded rerspectively.
     *
     * @param validator A callback class where the user can decide what to do if
     *                  the file already exists at the download or upload location respectively
     */
    public synchronized void start(Validator validator) {
        log.debug("start");
		this.worker = new Worker(this, validator);
		this.worker.start();
	}
	
	private class Worker extends Thread {		
		Queue queue;
		private Validator validator;
		List jobs;
		boolean running;
		boolean canceled;
        long size;
		private Timer progress;
		
		public Worker(Queue queue, Validator validator) {
			this.queue = queue;
			this.validator = validator;
			this.init();
		}
				
		public long size() {
			return this.size;
		}
		
		public long current() {
			long value = 0;
			for (Iterator iter = jobs.iterator(); iter.hasNext();) {
				value += ((Path)iter.next()).status.getCurrent();
			}
			return value;
		}
		
		public int numberOfEntries() {
			return this.jobs.size();
		}
		
		public void cancel() {
			for (Iterator iter = jobs.iterator(); iter.hasNext();) {
				((Path)iter.next()).status.setCanceled(true);
			}
			this.canceled = true;
		}
		
		public boolean isStopped() {
			return !this.running;
		}
		
		private void init() {
			this.jobs = new ArrayList();
			this.progress = new Timer(500,
										   new ActionListener() {
											   int i = 0;
											   long current;
											   long last;
											   long[] speeds = new long[8];
											   
											   public void actionPerformed(ActionEvent e) {
												   long diff = 0;
												   current = current(); // Bytes
												   if (current <= 0) {
													   setSpeed(0);
												   }
												   else {
													   speeds[i] = (current - last) * 2; // Bytes per second
													   i++;
													   last = current;
													   if (i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
														   i = 0;
													   }
													   for (int k = 0; k < speeds.length; k++) {
														   diff = diff + speeds[k]; // summe der differenzen zwischen einer halben sekunde
													   }
													   setSpeed((diff / speeds.length)); //Bytes per second
												   }
												   
											   }
										   }
										   );
		}
		
		public void run() {
//@todo			int mypool = NSAutoreleasePool.push();
			this.running = true;
			this.queue.callObservers(new Message(Message.QUEUE_START));
			
			this.queue.getRoot().getSession().addObserver(this.queue);
			this.queue.getRoot().getSession().cache().clear();
			for (Iterator i = roots.iterator(); i.hasNext() && !canceled; ) {
				Path r = (Path)i.next();
				log.debug("Iterating over childs of " + r);
				Iterator childs = r.getChilds(this.queue.kind()).iterator();
				while (childs.hasNext() && !canceled) {
					if(canceled) break;
					Path child = (Path)childs.next();
					log.debug("Adding " + child.getName() + " as child to queue.");
					this.jobs.add(child);
				}
			}
			
			for (Iterator iter = jobs.iterator(); iter.hasNext() && !canceled; ) {
				Path item = (Path)iter.next();
				log.debug("Validating " + item.toString());
				if (!this.validator.validate(item)) {
					iter.remove();
				}
				item.status.reset();
				this.size += item.status.getSize();
			}
			
			this.progress.start();
			for (Iterator iter = jobs.iterator(); iter.hasNext() && !canceled; ) {
				Path job = (Path)iter.next();
				job.status.addObserver(queue);				
				switch (kind) {
					case KIND_DOWNLOAD:
						job.download();
						break;
					case KIND_UPLOAD:
						job.upload();
						break;
				}
				job.status.deleteObserver(queue);
			}
			this.progress.stop();
			
			this.queue.getRoot().getSession().close();
			this.queue.getRoot().getSession().deleteObserver(this.queue);
			
			this.running = false;
			this.queue.callObservers(new Message(Message.QUEUE_STOP));
			
//			NSAutoreleasePool.pop(mypool);
		}
	}
		
		/**
     * Stops the currently running thread processing the queue.
     *
     * @pre The thread must be running
     */
    public void cancel() {
		this.worker.cancel();
    }

    /**
     * @return True if this queue's thread is running
     */
    public boolean isRunning() {
        return !this.worker.isStopped();
    }

    /**
     * @return True if the processing of the queue has been stopped,
     * either becasuse the transfers have all been completed or
     * been cancled by the user.
     */
	/*
    private boolean isCanceled() {
        return this.canceled;
    }
	 */

    public int numberOfRoots() {
        return this.roots.size();
    }

    /**
     * @return Number of jobs in the this.
     */
    public int numberOfJobs() {
		return this.worker.numberOfEntries();
		// return this.this.jobs.size();
    }

    /**
     * @return rue if all items in the this queue have been processed sucessfully.
     */
    public boolean isComplete() {
        return this.isInitalized() && (this.getSize() == this.getCurrent());
    }
	
	public boolean isInitalized() {
		return !(this.getSize() == 0 && this.getCurrent() == 0);
	}
	
    public String getStatusText() {
		if(this.isInitalized()) {
			if(this.isRunning()) {
				return this.getCurrentAsString()
				+" of "+this.getSizeAsString()
				+" at "+this.getSpeedAsString()+"  "
				+this.status;
			}
			return this.getCurrentAsString()
			+" of "+this.getSizeAsString()+"  "
			+this.status;
		}
		return "(Unknown size)  "+this.status;
    }
	
    /**
     * @return The cummulative file size of all files remaining in the this
     */
    public long getSize() {
		return this.worker.size();
    }

    public String getSizeAsString() {
        return Status.getSizeAsString(this.getSize());
    }

    /**
     * @return The number of bytes already processed of all elements in the whole this.
     */
    public long getCurrent() {
		return this.worker.current();
    }

    public String getCurrentAsString() {
        return Status.getSizeAsString(this.getCurrent());
    }

    /**
     * @return double current bytes/second
     */
    public String getSpeedAsString() {
        if (this.isRunning()) {
            if (this.getSpeed() > -1) {
                return Status.getSizeAsString(this.getSpeed()) + "/sec";
            }
        }
        return "";
    }

	private long speed;
	
    /**
     * @return The bytes being processed per second
     */
    public long getSpeed() {
        return this.speed;
    }

    private void setSpeed(long s) {
        this.speed = s;
    }
}