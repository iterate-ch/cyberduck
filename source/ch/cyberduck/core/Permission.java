package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * Encapsulating unix file permissions.
 * @version $Id$
 */
public class Permission {
	private static Logger log = Logger.getLogger(Permission.class);

	private static final String DEFAULT_MASK = "-rw-r--r--"; //defaulting to a file
	private String mask;

	public Permission(NSDictionary dict) {
		log.debug("Attributes");
		this.mask = (String)dict.objectForKey("Mask");
	}
	
	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.mask, "Mask");
		return dict;
	}
	
	/**
	 * Index of OWNER bit
	 */
	public static final int OWNER = 0;
	/**
	 * Index of GROUP bit
	 */
	public static final int GROUP = 1;
	/**
	 * Index of OTHER bit
	 */
	public static final int OTHER = 2;

	/**
	 * Index of READ bit
	 */
	public static final int READ = 0;
	/**
	 * Index of WRITE bit
	 */
	public static final int WRITE = 1;
	/**
	 * Index of EXECUTE bit
	 */
	public static final int EXECUTE = 2;

	// {read, write, execute}
	private boolean[] owner = new boolean[3];
	private boolean[] group = new boolean[3];
	private boolean[] other = new boolean[3];
	
	public Permission() {
		this(DEFAULT_MASK);
	}

	/**
	 * @param s the access string to parse the permissions from.
	 * Must be someting like -rwxrwxrwx
	 */
	public Permission(String mask) {
		this.mask = mask;
		this.owner = this.getOwnerPermissions(mask);
		this.group = this.getGroupPermissions(mask);
		this.other = this.getOtherPermissions(mask);
//		log.debug("Permission:"+this.toString());
	}

	/**
	 * @param p A 3*3 boolean array representing read, write and execute permissions
	 * by owner, group and others. (1,1) is the owner's read permission
	 */
	public Permission(boolean[][] p) {
		this.owner[READ] = p[OWNER][READ];
		this.owner[WRITE] = p[OWNER][WRITE];
		this.owner[EXECUTE] = p[OWNER][EXECUTE];

		this.group[READ] = p[GROUP][READ];
		this.group[WRITE] = p[GROUP][WRITE];
		this.group[EXECUTE] = p[GROUP][EXECUTE];

		this.other[READ] = p[OTHER][READ];
		this.other[WRITE] = p[OTHER][WRITE];
		this.other[EXECUTE] = p[OTHER][EXECUTE];
//		log.debug("Permission:"+this.toString());
		this.mask = "-"+getString();
	}


	public Permission(int decimal) {
//		log.debug("Permission(decimal):"+decimal);
		String octal = Integer.toOctalString(decimal);
//		log.debug("Permission(octal):"+octal);
		if (octal.length() != 3)
			throw new IllegalArgumentException("Permission must be a three digit number");
		switch (Integer.parseInt(octal.substring(0, 1))) {
			case (0):
				this.owner = new boolean[]{false, false, false};
				break;
			case (1):
				this.owner = new boolean[]{false, false, true};
				break;
			case (2):
				this.owner = new boolean[]{false, true, false};
				break;
			case (3):
				this.owner = new boolean[]{false, true, true};
				break;
			case (4):
				this.owner = new boolean[]{true, false, false};
				break;
			case (5):
				this.owner = new boolean[]{true, false, true};
				break;
			case (6):
				this.owner = new boolean[]{true, true, false};
				break;
			case (7):
				this.owner = new boolean[]{true, true, true};
				break;
		}
		switch (Integer.parseInt(octal.substring(1, 2))) {
			case (0):
				this.group = new boolean[]{false, false, false};
				break;
			case (1):
				this.group = new boolean[]{false, false, true};
				break;
			case (2):
				this.group = new boolean[]{false, true, false};
				break;
			case (3):
				this.group = new boolean[]{false, true, true};
				break;
			case (4):
				this.group = new boolean[]{true, false, false};
				break;
			case (5):
				this.group = new boolean[]{true, false, true};
				break;
			case (6):
				this.group = new boolean[]{true, true, false};
				break;
			case (7):
				this.group = new boolean[]{true, true, true};
				break;
		}
		switch (Integer.parseInt(octal.substring(2, 3))) {
			case (0):
				this.other = new boolean[]{false, false, false};
				break;
			case (1):
				this.other = new boolean[]{false, false, true};
				break;
			case (2):
				this.other = new boolean[]{false, true, false};
				break;
			case (3):
				this.other = new boolean[]{false, true, true};
				break;
			case (4):
				this.other = new boolean[]{true, false, false};
				break;
			case (5):
				this.other = new boolean[]{true, false, true};
				break;
			case (6):
				this.other = new boolean[]{true, true, false};
				break;
			case (7):
				this.other = new boolean[]{true, true, true};
				break;
		}
		this.mask = "-"+getString();
//		log.debug("Permission:"+this.toString());
	}

	/**
		* @param access unix access permitions, i.e. -rwxrwxrwx
	 */
	
	public void setMask(String mask) {
		this.mask = mask;
	}
	
	/**
		* @return The unix access permissions including the the first bit
	 */
	public String getMask() {
		return this.mask;
	}
		
	/**
	 * @return a thee-dimensional boolean array representing read, write
	 * and execute permissions (in that order) of the file owner.
	 */
	public boolean[] getOwnerPermissions() {
		return owner;
	}

	/**
	 * @return a thee-dimensional boolean array representing read, write
	 * and execute permissions (in that order) of the group
	 */
	public boolean[] getGroupPermissions() {
		return group;
	}

	/**
	 * @return a thee-dimensional boolean array representing read, write
	 * and execute permissions (in that order) of any user
	 */
	public boolean[] getOtherPermissions() {
		return other;
	}

	private boolean[] getOwnerPermissions(String s) {
		boolean[] b = {s.charAt(1) == 'r', s.charAt(2) == 'w', s.charAt(3) == 'x' || s.charAt(9) == 's'};
		return b;
	}

	private boolean[] getGroupPermissions(String s) {
		boolean[] b = {s.charAt(4) == 'r', s.charAt(5) == 'w', s.charAt(6) == 'x' || s.charAt(9) == 's'};
		return b;
	}

	private boolean[] getOtherPermissions(String s) {
		boolean[] b = {s.charAt(7) == 'r', s.charAt(8) == 'w', s.charAt(9) == 'x' || s.charAt(9) == 't' || s.charAt(9) == 'T'};
		return b;
	}

	/**
	 * @return i.e. rwxrwxrwx (777)
	 */
	public String toString() {
		return this.getString() + " (" + this.getOctalCode() + ")";
	}

	/**
	 * @return The unix equivalent access string like rwxrwxrwx
	 */
	public String getString() {
		String owner = this.getAccessString(this.getOwnerPermissions());
		String group = this.getAccessString(this.getGroupPermissions());
		String other = this.getAccessString(this.getOtherPermissions());
		return owner + group + other;
	}

	/**
	 * @return The unix equivalent octal access code like 777
	 */
	public int getOctalCode() {
		String owner = "" + this.getOctalAccessNumber(this.getOwnerPermissions());
		String group = "" + this.getOctalAccessNumber(this.getGroupPermissions());
		String other = "" + this.getOctalAccessNumber(this.getOtherPermissions());
		return Integer.parseInt(owner + group + other);
	}

	public int getDecimalCode() {
		String owner = "" + this.getOctalAccessNumber(this.getOwnerPermissions());
		String group = "" + this.getOctalAccessNumber(this.getGroupPermissions());
		String other = "" + this.getOctalAccessNumber(this.getOtherPermissions());
		return Integer.parseInt(owner + group + other, 8);
	}

	/*
	*	0 = no permissions whatsoever; this person cannot read, write, or execute the file
	 *	1 = execute only
	 *	2 = write only
	 *	3 = write and execute (1+2)
	 *	4 = read only
	 *	5 = read and execute (4+1)
	 *	6 = read and write (4+2)
	 *	7 = read and write and execute (4+2+1)
	 */

	//-rwxrwxrwx

	private int getOctalAccessNumber(boolean[] permissions) {
		if (Arrays.equals(permissions, new boolean[]{false, false, false}))
			return 0;
		if (Arrays.equals(permissions, new boolean[]{false, false, true}))
			return 1;
		if (Arrays.equals(permissions, new boolean[]{false, true, false}))
			return 2;
		if (Arrays.equals(permissions, new boolean[]{false, true, true}))
			return 3;
		if (Arrays.equals(permissions, new boolean[]{true, false, false}))
			return 4;
		if (Arrays.equals(permissions, new boolean[]{true, false, true}))
			return 5;
		if (Arrays.equals(permissions, new boolean[]{true, true, false}))
			return 6;
		if (Arrays.equals(permissions, new boolean[]{true, true, true}))
			return 7;
		return -1;
	}

	private String getAccessString(boolean[] permissions) {
		String read = permissions[READ] ? "r" : "-";
		String write = permissions[WRITE] ? "w" : "-";
		String execute = permissions[EXECUTE] ? "x" : "-";
		return read + write + execute;
	}

	/*
		public static final int --- = 0; {false, false, false}
		public static final int --x = 1; {false, false, true}
		public static final int -w- = 2; {false, true, false}
		public static final int -wx = 3; {false, true, true}
		public static final int r-- = 4; {true, false, false}
		public static final int r-x = 5; {true, false, true}
		public static final int rw- = 6; {true, true, false}
		public static final int rwx = 7; {true, true, true}
		*/
}
