package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.serializer.Serializer;

import java.util.Objects;

public interface Permission extends Serializable {
    Permission EMPTY = new Permission() {
        @Override
        public boolean isExecutable() {
            return true;
        }

        @Override
        public Action getUser() {
            return Action.none;
        }

        @Override
        public Action getGroup() {
            return Action.none;
        }

        @Override
        public Action getOther() {
            return Action.none;
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
        public String getMode() {
            return "--";
        }

        @Override
        public String getSymbol() {
            return "--";
        }

        @Override
        public String toString() {
            return "--";
        }

        @Override
        public boolean equals(final Object obj) {
            if(null == obj) {
                return false;
            }
            if(obj instanceof Permission) {
                return ((Permission) obj).getUser().equals(Action.none)
                        && ((Permission) obj).getGroup().equals(Action.none)
                        && ((Permission) obj).getOther().equals(Action.none);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Action.none, Action.none, Action.none);
        }
    };

    /**
     * Determines if the current permission setting allows reading.
     *
     * @return true if the permission is set to allow reading; false otherwise.
     */
    default boolean isReadable() {
        return this.getUser().implies(Action.read);
    }

    /**
     * Determines if the current permission setting allows writing.
     *
     * @return true if the permission is set to allow writing; false otherwise.
     */
    default boolean isWritable() {
        return this.getUser().implies(Action.write);
    }

    /**
     * Determines if the current permission setting allows execution.
     *
     * @return true if the permission is set to allow execution; false otherwise.
     */
    default boolean isExecutable() {
        return this.getUser().implies(Action.execute);
    }

    /**
     * Retrieves the action associated with the user permissions.
     *
     * @return The {@link Action} representing the user's permission level.
     */
    Action getUser();

    /**
     * Retrieves the action associated with the group permissions.
     *
     * @return The {@link Action} representing the group's permission level.
     */
    Action getGroup();

    /**
     * Retrieves the action associated with the other permissions.
     *
     * @return The {@link Action} representing the other's permission level.
     */
    Action getOther();

    /**
     * Constructs and retrieves the octal representation of the permission levels
     * for the user, group, and others based on their ordinal values.
     *
     * @return A string representing the octal permission mode. This value is
     * constructed by combining the permission levels of the user, group, and others
     * into a single integer, then converting it to its octal representation.
     */
    default String getMode() {
        return Integer.toString(this.getUser().ordinal() << 6 |
                this.getGroup().ordinal() << 3 |
                this.getOther().ordinal(), 8);
    }

    default boolean isSetuid() {
        return false;
    }

    default boolean isSetgid() {
        return false;
    }

    default boolean isSticky() {
        return false;
    }

    /**
     * Constructs and retrieves a symbolic representation of the user's permissions
     * based on readable, writable, and executable states.
     *
     * @return A string representing the symbolic notation of the user's permissions.
     * The string consists of three characters - 'r' for readable, 'w' for writable,
     * and 'x' for executable, or '-' for the absence of these permissions.
     */
    default String getSymbol() {
        return String.format("%s%s%s",
                this.getUser().symbolic, this.getGroup().symbolic, this.getOther().symbolic);
    }

    @Override
    default <T> T serialize(final Serializer<T> dict) {
        dict.setStringForKey(this.getSymbol(), "Mask");
        return dict.getSerialized();
    }

    /**
     * Retrieves a descriptive representation of the permission.
     * The description combines the symbolic representation of the user's permissions
     * with the octal permission mode.
     *
     * @return A string representing the permission description in the format "symbolic (octal)".
     */
    default String getDescription() {
        return String.format("%s (%s)", this.getSymbol(), this.getMode());
    }

    /**
     * POSIX style
     */
    enum Action {
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

        Action(final String symbol) {
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
