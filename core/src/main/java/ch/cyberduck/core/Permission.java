package ch.cyberduck.core;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Encapsulating UNIX file permissions.

 */
public class Permission implements Serializable {
    public static final Permission EMPTY = new Permission(Action.none, Action.none, Action.none) {
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
        public String toString() {
            return LocaleFactory.localizedString("--");
        }

        @Override
        public String getMode() {
            return LocaleFactory.localizedString("--");
        }

        @Override
        public String getSymbol() {
            return LocaleFactory.localizedString("--");
        }
    };
    private static final Logger log = Logger.getLogger(Permission.class);
    private Action user;
    private Action group;
    private Action other;
    /**
     * set user ID upon execution
     */
    private boolean setuid;
    /**
     * set group ID upon execution
     */
    private boolean setgid;
    private boolean sticky;

    public Permission() {
        this.set(Action.none, Action.none, Action.none, false, false, false);
    }

    public Permission(final String mode) {
        try {
            this.fromInteger(Integer.parseInt(mode, 8));
        }
        catch(NumberFormatException e) {
            this.fromSymbol(mode);
        }
    }

    /**
     * Construct by the given {@link Action}.
     *
     * @param u user action
     * @param g group action
     * @param o other action
     */
    public Permission(final Action u, final Action g, final Action o) {
        this.set(u, g, o, false, false, false);
    }

    public Permission(final Action u, final Action g, final Action o,
                      final boolean stickybit, final boolean setuid, final boolean setgid) {
        this.set(u, g, o, stickybit, setuid, setgid);
    }

    /**
     * Modes may be absolute or symbolic.  An absolute mode is an octal number constructed from the sum of one or more of the following values:
     * <p/>
     * 4000    (the set-user-ID-on-execution bit) Executable files with this bit set will run with effective uid set to the uid of the file owner.
     * Directories with the set-user-id bit set will force all files and sub-directories created in them to be owned by the directory owner
     * and not by the uid of the creating process, if the underlying file system supports this feature: see chmod(2) and the suiddir option to
     * mount(8).
     * 2000    (the set-group-ID-on-execution bit) Executable files with this bit set will run with effective gid set to the gid of the file owner.
     * 1000    (the sticky bit) See chmod(2) and sticky(8).
     * 0400    Allow read by owner.
     * 0200    Allow write by owner.
     * 0100    For files, allow execution by owner.  For directories, allow the owner to search in the directory.
     * 0040    Allow read by group members.
     * 0020    Allow write by group members.
     * 0010    For files, allow execution by group members.  For directories, allow group members to search in the directory.
     * 0004    Allow read by others.
     * 0002    Allow write by others.
     * 0001    For files, allow execution by others.  For directories allow others to search in the directory.
     *
     * @param mode Mode
     */
    public Permission(final int mode) {
        try {
            this.fromInteger(Integer.valueOf(Integer.toString(mode), 8));
        }
        catch(NumberFormatException e) {
            log.warn(String.format("Failure parsing %s", mode));
            this.set(Permission.EMPTY);
        }
    }

    /**
     * Copy constructor
     *
     * @param other other permission
     */
    public Permission(final Permission other) {
        this.set(other.user, other.group, other.other,
                other.sticky, other.setuid, other.setgid);
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(this.getSymbol(), "Mask");
        return dict.getSerialized();
    }

    private void set(final Permission other) {
        this.set(other.user, other.group, other.other,
                other.sticky, other.setuid, other.setgid);
    }

    private void set(final Action u, final Action g, final Action o,
                     final boolean s, final boolean setuid, final boolean setgid) {
        this.user = u;
        this.group = g;
        this.other = o;
        this.sticky = s;
        this.setuid = setuid;
        this.setgid = setgid;
    }

    private void fromInteger(int n) {
        Action[] v = Action.values();
        set(
                v[(n >>> 6) & 7],
                v[(n >>> 3) & 7],
                v[n & 7],
                ((n >>> 9) & 1) == 1,
                ((n >>> 9) & 4) == 4,
                ((n >>> 9) & 2) == 2
        );
    }

    /**
     * Encode the object to a integer.
     */
    private int toInteger() {
        return (sticky ? 1 << 9 : 0) | (setuid ? 4 << 9 : 0) | (setgid ? 2 << 9 : 0) |
                user.ordinal() << 6 |
                group.ordinal() << 3 |
                other.ordinal();
    }

    /**
     * @param symbol The perm symbols represent the portions of the mode bits as follows:
     *               <p/>
     *               r       The read bits.
     *               s       The set-user-ID-on-execution and set-group-ID-on-execution bits.
     *               t       The sticky bit.
     *               w       The write bits.
     *               x       The execute/search bits.
     *               X       The execute/search bits if the file is a directory or any of the execute/search bits are set in the original (unmodified) mode.  Operations with the perm symbol ``X'' are only
     *               meaningful in conjunction with the op symbol ``+'', and are ignored in all other cases.
     *               u       The user permission bits in the original mode of the file.
     *               g       The group permission bits in the original mode of the file.
     *               o       The other permission bits in the original mode of the file.
     */
    private void fromSymbol(final String symbol) {
        try {
            int n = 0;
            for(int i = 0; i < symbol.length(); i++) {
                n = n << 1;
                char c = symbol.charAt(i);
                n += (c == '-' || c == 'T' || c == 'S') ? 0 : 1;
            }
            // Add sticky bit value if set. The sticky bit is represented by the letter t in the final
            // character-place. If the sticky-bit is set on a file or directory without the execution bit set for the others category
            // (non-user-owner and non-group-owner), it is indicated with a capital T
            if(symbol.charAt(8) == 't' || symbol.charAt(8) == 'T') {
                // sticky bit octal integer
                n += 01000;
            }
            if(symbol.charAt(5) == 's' || symbol.charAt(5) == 'S') {
                //setgid octal integer
                n += 02000;
            }
            if(symbol.charAt(2) == 's' || symbol.charAt(2) == 'S') {
                //setuid octal integer
                n += 04000;
            }
            this.fromInteger(n);
        }
        catch(StringIndexOutOfBoundsException e) {
            log.warn(String.format("Failure parsing %s", symbol));
            this.set(Permission.EMPTY);
        }
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of the file owner.
     */
    public Action getUser() {
        return user;
    }

    public void setUser(final Action user) {
        this.user = user;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     * and execute permissions (in that order) of the group
     */
    public Action getGroup() {
        return group;
    }

    public void setGroup(final Action group) {
        this.group = group;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of any user
     */
    public Action getOther() {
        return other;
    }

    public void setOther(final Action other) {
        this.other = other;
    }

    public boolean isSetuid() {
        return setuid;
    }

    public void setSetuid(final boolean setuid) {
        this.setuid = setuid;
    }

    public boolean isSetgid() {
        return setgid;
    }

    public void setSetgid(final boolean setgid) {
        this.setgid = setgid;
    }

    public boolean isSticky() {
        return sticky;
    }

    public void setSticky(final boolean sticky) {
        this.sticky = sticky;
    }

    public String getSymbol() {
        final StringBuilder symbolic = new StringBuilder();
        symbolic.append(setuid ? user.implies(Action.execute) ?
                StringUtils.substring(user.symbolic, 0, 2) + "s" : StringUtils.substring(user.symbolic, 0, 2) + "S" :
                user.symbolic);
        symbolic.append(setgid ? group.implies(Action.execute) ?
                StringUtils.substring(group.symbolic, 0, 2) + "s" : StringUtils.substring(group.symbolic, 0, 2) + "S" :
                group.symbolic);
        symbolic.append(sticky ? other.implies(Action.execute) ?
                StringUtils.substring(other.symbolic, 0, 2) + "t" : StringUtils.substring(other.symbolic, 0, 2) + "T" :
                other.symbolic);
        return symbolic.toString();
    }

    /**
     * @return The unix equivalent octal access code like 0777
     */
    public String getMode() {
        return Integer.toString(toInteger(), 8);
    }

    /**
     * @return i.e. rwxrwxrwx (777)
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", this.getSymbol(), this.getMode());
    }

    public boolean isExecutable() {
        return this.getUser().implies(Action.execute)
                || this.getGroup().implies(Action.execute)
                || this.getOther().implies(Action.execute);
    }

    /**
     * @return true if readable for user, group and world
     */
    public boolean isReadable() {
        return this.getUser().implies(Action.read)
                || this.getGroup().implies(Action.read)
                || this.getOther().implies(Action.read);
    }

    /**
     * @return true if writable for user, group and world
     */
    public boolean isWritable() {
        return this.getUser().implies(Action.write)
                || this.getGroup().implies(Action.write)
                || this.getOther().implies(Action.write);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Permission)) {
            return false;
        }
        final Permission that = (Permission) o;
        if(setgid != that.setgid) {
            return false;
        }
        if(setuid != that.setuid) {
            return false;
        }
        if(sticky != that.sticky) {
            return false;
        }
        if(group != that.group) {
            return false;
        }
        if(other != that.other) {
            return false;
        }
        if(user != that.user) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return toInteger();
    }

    /**
     * POSIX style
     */
    public enum Action {
        none("---"),
        execute("--x"),
        write("-w-"),
        write_execute("-wx"),
        read("r--"),
        read_execute("r-x"),
        read_write("rw-"),
        all("rwx");

        /**
         * Retain reference to value array.
         */
        private final static Action[] values = values();

        /**
         * Symbolic representation
         */
        public final String symbolic;

        private Action(final String symbol) {
            symbolic = symbol;
        }

        /**
         * Return true if this action implies that action.
         *
         * @param that action
         */
        public boolean implies(final Action that) {
            if(that != null) {
                return (ordinal() & that.ordinal()) == that.ordinal();
            }
            return false;
        }

        /**
         * AND operation.
         */
        public Action and(final Action that) {
            return values[ordinal() & that.ordinal()];
        }

        /**
         * OR operation.
         */
        public Action or(final Action that) {
            return values[ordinal() | that.ordinal()];
        }

        /**
         * NOT operation.
         */
        public Action not() {
            return values[7 - ordinal()];
        }
    }
}
