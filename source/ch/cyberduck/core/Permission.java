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

import java.util.Arrays;

/**
 * Encapsulating unix file permissions.
 * @version $Id$
 */
public class Permission implements java.io.Serializable {

    public static final int OWNER = 0;
    public static final int GROUP = 1;
    public static final int OTHER = 2;

    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final int EXECUTE = 2;
    
    // {read, write, execute}
    private boolean[] owner = new boolean[3];
    private boolean[] group = new boolean[3];
    private boolean[] other = new boolean[3];

    /**
     * @param s the access string to parse the permissions from.
     * Must be someting like -rwxrwxrwx
     */
    public Permission(String s) {
        this.owner = this.getOwnerPermissions(s);
        this.group = this.getGroupPermissions(s);
        this.other = this.getOtherPermissions(s);
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
        boolean[] b = {s.charAt(1) == 'r', s.charAt(2) == 'w', s.charAt(3) == 'x'};
        return b;
    }
    
    private boolean[] getGroupPermissions(String s) {
        boolean[] b = {s.charAt(4) == 'r', s.charAt(5) == 'w', s.charAt(6) == 'x'};
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
    	return this.getString()+" ("+this.getCode()+")";
    }

    /**
     * @return The unix equivalent access string like rwxrwxrwx
     */
    public String getString() {
        String owner = this.getAccessString(this.getOwnerPermissions());
        String group = this.getAccessString(this.getGroupPermissions());
        String other = this.getAccessString(this.getOtherPermissions());
        return owner+group+other;
    }

    /**
     * @return The unix equivalent access code like 777
     */
    public String getCode() {
        String owner = ""+ this.getAccessNumber(this.getOwnerPermissions());
        String group = ""+ this.getAccessNumber(this.getGroupPermissions());
        String other = ""+ this.getAccessNumber(this.getOtherPermissions());
        return owner+group+other;
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

    private int getAccessNumber(boolean[] permissions) {
        if(Arrays.equals(permissions, new boolean[]{false, false, false}))
            return 0;
        if(Arrays.equals(permissions, new boolean[]{false, false, true}))
            return 1;
        if(Arrays.equals(permissions, new boolean[]{false, true, false}))
            return 2;
        if(Arrays.equals(permissions, new boolean[]{false, true, true}))
            return 3;
        if(Arrays.equals(permissions, new boolean[]{true, false, false}))
            return 4;
        if(Arrays.equals(permissions, new boolean[]{true, false, true}))
            return 5;
        if(Arrays.equals(permissions, new boolean[]{true, true, false}))
            return 6;
        if(Arrays.equals(permissions, new boolean[]{true, true, true}))
            return 7;
        return -1;
    }

    private String getAccessString(boolean[] permissions) {
        String read = permissions[READ] ? "r" : "-";
        String write = permissions[WRITE] ? "w" : "-";
        String execute = permissions[EXECUTE] ? "x" : "-";
        return read+write+execute;
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
