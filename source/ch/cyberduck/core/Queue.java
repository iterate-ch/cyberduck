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

import javax.swing.Timer;
import java.util.*;

import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class Queue extends Observable {
	protected static Logger log = Logger.getLogger(Queue.class);

	protected Worker worker;
	protected long size;
	protected long current;
	private List roots;

	/**
	 * Creating an empty queue containing no items. Items have to be added later
	 * using the <code>addRoot</code> method.
	 */
	public Queue() {
		this.worker = new Worker(this, ValidatorFactory.createValidator(this.getClass()));
		this.roots = new ArrayList();
	}

	public Queue(Path root) {
		this.worker = new Worker(this, ValidatorFactory.createValidator(this.getClass()));
		this.roots = new ArrayList();
		this.addRoot(root);
	}

	public static final int KIND_DOWNLOAD = 0;
	public static final int KIND_UPLOAD = 1;
	public static final int KIND_SYNC = 2;

	public static Queue createQueue(NSDictionary dict) {
		Queue q = null;
		Object kindObj = dict.objectForKey("Kind");
		if(kindObj != null) {
			int kind = Integer.parseInt((String)kindObj);
			switch(kind) {
				case Queue.KIND_DOWNLOAD:
					q = new DownloadQueue();
					break;
				case Queue.KIND_UPLOAD:
					q = new UploadQueue();
					break;
				case Queue.KIND_SYNC:
					q = new SyncQueue();
					break;
				default:
					throw new IllegalArgumentException("Unknown queue");
			}
		}
		Object hostObj = dict.objectForKey("Host");
		if(hostObj != null) {
			Host host = new Host((NSDictionary)hostObj);
			Session s = SessionFactory.createSession(host);
			Object rootsObj = dict.objectForKey("Roots");
			if(rootsObj != null) {
				NSArray r = (NSArray)rootsObj;
				for(int i = 0; i < r.count(); i++) {
					q.addRoot(PathFactory.createPath(s, (NSDictionary)r.objectAtIndex(i)));
				}
			}
		}
		Object sizeObj = dict.objectForKey("Size");
		if(sizeObj != null) {
			q.size = Long.parseLong((String)sizeObj);
		}
		Object currentObj = dict.objectForKey("Current");
		if(currentObj != null) {
			q.current = Long.parseLong((String)currentObj);
		}
		return q;
	}

	public NSMutableDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.getRoot().getHost().getAsDictionary(), "Host");
		NSMutableArray r = new NSMutableArray();
		for(Iterator iter = this.roots.iterator(); iter.hasNext();) {
			r.addObject(((Path)iter.next()).getAsDictionary());
		}
		dict.setObjectForKey(r, "Roots");
		dict.setObjectForKey(""+this.getSize(), "Size");
		dict.setObjectForKey(""+this.getCurrent(), "Current");
		return dict;
	}

	/**
	 * Add an item to the queue
	 *
	 * @param item The path to be added in the queue
	 */
	public void addRoot(Path item) {
		this.roots.add(item);
	}
	
	public Path getRoot() {
		return (Path)roots.get(0);
	}

	public String getName() {
		String name = "";
		for(Iterator iter = this.roots.iterator(); iter.hasNext();) {
			name = name+((Path)iter.next()).getName()+" ";
		}
		return name;
	}

	public List getRoots() {
		return this.roots;
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
	
	public List getChilds() {
		List childs = new ArrayList();
		for(Iterator rootIter = this.getRoots().iterator(); rootIter.hasNext();) {
			this.getChilds(childs, (Path)rootIter.next());
		}
		return childs;
	}

	protected abstract List getChilds(List childs, Path root);

	protected abstract void process(Path p);

	private boolean resumeRequested;
	
	public boolean isResumeRequested() {
		return this.resumeRequested;
	}
	
	public List getJobs() {
		return this.worker.getJobs();
	}
	
	/**
	 * Process the queue. All files will be downloaded/uploaded/synced rerspectively.
	 */
	public void start(boolean resumeRequested) {
		this.resumeRequested = resumeRequested;
		this.worker = new Worker(this, ValidatorFactory.createValidator(this.getClass()));
		this.worker.start();
	}

	private class Worker extends Thread {
		private Queue queue;
		private List jobs;
		private Validator validator;
		private Timer progress;
		private boolean running;
		private boolean canceled;

		protected Worker(Queue queue, Validator validator) {
			this.queue = queue;
			this.validator = validator;
		}
		
		public List getJobs() {
			return this.jobs;
		}
				
		private void init() {
			this.running = true;
			this.progress = new Timer(500,
									  new java.awt.event.ActionListener() {
										  int i = 0;
										  long current;
										  long last;
										  long[] speeds = new long[8];
										  
										  public void actionPerformed(java.awt.event.ActionEvent e) {
											  long diff = 0;
											  current = getCurrent(); // Bytes
											  if(current <= 0) {
												  setSpeed(0);
											  }
											  else {
												  speeds[i] = (current-last)*2; // Bytes per second
												  i++;
												  last = current;
												  if(i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
													  i = 0;
												  }
												  for(int k = 0; k < speeds.length; k++) {
													  diff = diff+speeds[k]; // summe der differenzen zwischen einer halben sekunde
												  }
												  setSpeed((diff/speeds.length)); //Bytes per second
											  }
											  
										  }
									  });
			this.progress.start();
			//@todo this.queue.getRoot().getSession().cache().clear(); //@todo in path.upload() instead
			this.queue.callObservers(new Message(Message.QUEUE_START));
		}
		
		public void run() {
			this.init();
			this.validator.validate(this.queue);
			if(!this.validator.isCanceled()) {
				if(this.validator.getResult().size() > 0) {
					this.jobs = this.validator.getResult();
					this.queue.reset();
					for(Iterator iter = this.jobs.iterator(); iter.hasNext() && !this.isCanceled(); ) {
						((Path)iter.next()).status.reset();
					}
					for(Iterator iter = jobs.iterator(); iter.hasNext() && !this.isCanceled(); ) {
						this.queue.process((Path)iter.next());
					}
				}
			}
			else {
				this.cancel();
			}
			this.finish();
		}
				
		private void finish() {
			this.running = false;
			this.progress.stop();
			this.queue.getRoot().getSession().close();
			this.queue.callObservers(new Message(Message.QUEUE_STOP));
			this.jobs = null;
		}
		
		protected void cancel() {
			if(this.isInitialized()) {
				for(Iterator iter = this.jobs.iterator(); iter.hasNext();) {
					((Path)iter.next()).status.setCanceled(true);
				}
			}
			this.canceled = true;
		}

		protected boolean isCanceled() {
			return this.canceled;
		}

		protected boolean isRunning() {
			return this.running;
		}
		
		protected boolean isInitialized() {
			return this.getJobs() != null;
		}
	}

	protected abstract void reset();
	
	public void cancel() {
		this.worker.cancel();
	}

	public boolean isCanceled() {
		return this.worker.isCanceled();
	}
	
	public boolean isInitialized() {
		return this.getSize() != 0;
//		return this.worker.isInitialized();
	}
	
	/**
	 * @return True if this queue's thread is running
	 */
	public boolean isRunning() {
		return this.worker.isRunning();
	}

	public int numberOfRoots() {
		return this.roots.size();
	}

	public boolean isComplete() {
		return !(this.getSize() == 0 && this.getCurrent() == 0) && (this.getSize() == this.getCurrent());
	}

	public abstract long getSize();

	public String getSizeAsString() {
		return Status.getSizeAsString(this.getSize());
	}

	public long getCurrent() {
		if(/*this.worker.isRunning() && */this.worker.isInitialized()) {
			long size = 0;
			for(Iterator iter = this.worker.getJobs().iterator(); iter.hasNext();) {
				size += ((Path)iter.next()).status.getCurrent();
			}
			this.current = size;
		}
		return this.current; //cached value
	}

	public String getCurrentAsString() {
		return Status.getSizeAsString(this.getCurrent());
	}

	/**
	 * @return double current bytes/second
	 */
	public String getSpeedAsString() {
		if(this.worker.isRunning() && this.worker.isInitialized()) {
			if(this.getSpeed() > -1) {
				return Status.getSizeAsString(this.getSpeed())+"/sec";
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