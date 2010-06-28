package ch.cyberduck.core;

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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Encapsulating UNIX file permissions.
 *
 * @version $Id$
 */
public class Permission implements Serializable {
    private static Logger log = Logger.getLogger(Permission.class);

    private static final int EMPTY_MASK = 0;

    /**
     *
     */
    public static final Permission EMPTY = new Permission() {
        @Override
        public boolean isExecutable() {
            return true;
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public String toString() {
            return Locale.localizedString("Unknown");
        }
    };

    public <T> Permission(T dict) {
        this.init(dict);
    }

    public <T> void init(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final String maskObj = dict.stringForKey("Mask");
        if(maskObj != null) {
            this.init(maskObj);
        }
    }

    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setStringForKey(this.getMask(), "Mask");
        return dict.<T>getSerialized();
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
    // --- = 0; {false, false, false}
    // --x = 1; {false, false, true}
    // -w- = 2; {false, true, false}
    // -wx = 3; {false, true, true}
    // r-- = 4; {true, false, false}
    // r-x = 5; {true, false, true}
    // rw- = 6; {true, true, false}
    // rwx = 7; {true, true, true}
    private boolean[] owner = new boolean[3];
    private boolean[] group = new boolean[3];
    private boolean[] other = new boolean[3];

    public Permission() {
        this(Permission.EMPTY_MASK);
    }

    /**
     * Copy
     *
     * @param p
     */
    public Permission(Permission p) {
        this.init(p.getMask());
    }

    /**
     * @param mask the access string to parse the permissions from.
     *             Must be something between --------- and rwxrwxrwx
     */
    public Permission(String mask) {
        this.init(mask);
    }

    /**
     * @param mask
     */
    private void init(String mask) {
        if(mask.length() != 9) {
            log.error("Invalid mask:" + mask);
            throw new NumberFormatException("Must be a nine digit string");
        }
        this.owner = this.getOwnerPermissions(mask);
        this.group = this.getGroupPermissions(mask);
        this.other = this.getOtherPermissions(mask);
    }

    /**
     * @param p A 3*3 boolean array representing read, write and execute permissions
     *          by owner, group and others. (1,1) is the owner's read permission
     */
    public Permission(boolean[][] p) {
        this.init(p);
    }

    /**
     * @param p
     */
    private void init(boolean[][] p) {
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
     * @param octal The permissions as a 3 digit octal number
     */
    public Permission(int octal) {
        String octalString = String.valueOf(octal);
        StringBuilder sb = new StringBuilder();
        int leadingZeros = 3 - octalString.length();
        while(leadingZeros > 0) {
            sb.append('0');
            leadingZeros--;
        }
        sb.append(octalString);
        octalString = sb.toString();

        log.debug("Permission(octalString):" + octalString);

        if(octalString.length() != 3) {
            log.error("Invalid octal value:" + octal);
            throw new NumberFormatException("Must be a three digit number");
        }
        switch(Integer.parseInt(octalString.substring(0, 1))) {
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
        switch(Integer.parseInt(octalString.substring(1, 2))) {
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
        switch(Integer.parseInt(octalString.substring(2, 3))) {
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
    }

    /**
     * @return The unix access permissions
     */
    public String getMask() {
        return this.getRwxString();
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of the file owner.
     */
    public boolean[] getOwnerPermissions() {
        return owner;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of the group
     */
    public boolean[] getGroupPermissions() {
        return group;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of any user
     */
    public boolean[] getOtherPermissions() {
        return other;
    }

    private boolean[] getOwnerPermissions(String s) {
        return new boolean[]{
                s.charAt(0) == 'r',
                s.charAt(1) == 'w',
                s.charAt(2) == 'x' || s.charAt(2) == 's' || s.charAt(2) == 'S' || s.charAt(2) == 't' || s.charAt(2) == 'T' || s.charAt(2) == 'L'};
    }

    private boolean[] getGroupPermissions(String s) {
        return new boolean[]{
                s.charAt(3) == 'r',
                s.charAt(4) == 'w',
                s.charAt(5) == 'x' || s.charAt(5) == 's' || s.charAt(5) == 'S' || s.charAt(5) == 't' || s.charAt(5) == 'T' || s.charAt(5) == 'L'};
    }

    private boolean[] getOtherPermissions(String s) {
        return new boolean[]{
                s.charAt(6) == 'r',
                s.charAt(7) == 'w',
                s.charAt(8) == 'x' || s.charAt(8) == 's' || s.charAt(8) == 'S' || s.charAt(8) == 't' || s.charAt(8) == 'T' || s.charAt(8) == 'L'};
    }

    /**
     * @return i.e. rwxrwxrwx (777)
     */
    @Override
    public String toString() {
        return this.getMask() + " (" + this.getOctalString() + ")";
    }

    /**
     * @return The unix equivalent access string like rwxrwxrwx
     */
    private String getRwxString() {
        String owner = this.getAccessString(this.getOwnerPermissions());
        String group = this.getAccessString(this.getGroupPermissions());
        String other = this.getAccessString(this.getOtherPermissions());
        return owner + group + other;
    }

    /**
     * @return The unix equivalent octal access code like 777
     */
    public String getOctalString() {
        String owner = "" + this.getOctalAccessNumber(this.getOwnerPermissions());
        String group = "" + this.getOctalAccessNumber(this.getGroupPermissions());
        String other = "" + this.getOctalAccessNumber(this.getOtherPermissions());
        return owner + group + other;
    }

    public int getOctalNumber() {
        String owner = "" + this.getOctalAccessNumber(this.getOwnerPermissions());
        String group = "" + this.getOctalAccessNumber(this.getGroupPermissions());
        String other = "" + this.getOctalAccessNumber(this.getOtherPermissions());
        return Integer.parseInt(owner + group + other, 8);
    }

    /**
     * @return 0 = no permissions whatsoever; this person cannot read, write, or execute the file
     *         1 = execute only
     *         2 = write only
     *         3 = write and execute (1+2)
     *         4 = read only
     *         5 = read and execute (4+1)
     *         6 = read and write (4+2)
     *         7 = read and write and execute (4+2+1)
     */
    private int getOctalAccessNumber(boolean[] permissions) {
        if(Arrays.equals(permissions, new boolean[]{false, false, false})) {
            return 0;
        }
        if(Arrays.equals(permissions, new boolean[]{false, false, true})) {
            return 1;
        }
        if(Arrays.equals(permissions, new boolean[]{false, true, false})) {
            return 2;
        }
        if(Arrays.equals(permissions, new boolean[]{false, true, true})) {
            return 3;
        }
        if(Arrays.equals(permissions, new boolean[]{true, false, false})) {
            return 4;
        }
        if(Arrays.equals(permissions, new boolean[]{true, false, true})) {
            return 5;
        }
        if(Arrays.equals(permissions, new boolean[]{true, true, false})) {
            return 6;
        }
        if(Arrays.equals(permissions, new boolean[]{true, true, true})) {
            return 7;
        }
        return -1;
    }

    private String getAccessString(boolean[] permissions) {
        String read = permissions[READ] ? "r" : "-";
        String write = permissions[WRITE] ? "w" : "-";
        String execute = permissions[EXECUTE] ? "x" : "-";
        return read + write + execute;
    }

    public boolean isExecutable() {
        return this.getOwnerPermissions()[Permission.EXECUTE]
                || this.getGroupPermissions()[Permission.EXECUTE]
                || this.getOtherPermissions()[Permission.EXECUTE];
    }

    /**
     * @return true if readable for user, group and world
     */
    public boolean isReadable() {
        return this.getOwnerPermissions()[Permission.READ]
                || this.getGroupPermissions()[Permission.READ]
                || this.getOtherPermissions()[Permission.READ];
    }

    /**
     * @return true if writable for user, group and world
     */
    public boolean isWritable() {
        return this.getOwnerPermissions()[Permission.WRITE]
                || this.getGroupPermissions()[Permission.WRITE]
                || this.getOtherPermissions()[Permission.WRITE];
    }

    @Override
    public int hashCode() {
        return this.getOctalNumber();
    }

    @Override
    public boolean equals(Object o) {
        if(null == o) {
            return false;
        }
        if(o instanceof Permission) {
            Permission other = (Permission) o;
            return this.getOctalNumber() == other.getOctalNumber();
        }
        return false;
    }
}
