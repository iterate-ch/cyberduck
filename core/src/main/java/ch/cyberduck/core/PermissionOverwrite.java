package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.apache.commons.lang3.CharUtils;

public class PermissionOverwrite {
    public final Action user, group, other;

    public PermissionOverwrite() {
        this.user = new Action(null, null, null);
        this.group = new Action(null, null, null);
        this.other = new Action(null, null, null);
    }

    public PermissionOverwrite(Action user, Action group, Action other) {
        this.user = user;
        this.group = group;
        this.other = other;
    }

    public String getMode() {
        StringBuilder builder = new StringBuilder(3);

        builder.append(user.mode());
        builder.append(group.mode());
        builder.append(other.mode());

        return builder.toString();
    }

    public Permission resolve(final Permission original) {
        return new Permission(
                user.resolve(original.getUser()),
                group.resolve(original.getGroup()),
                other.resolve(original.getOther()),
                original.isSticky(), original.isSetuid(), original.isSetgid());
    }

    @Override
    public String toString() {
        final StringBuilder symbolic = new StringBuilder();

        symbolic.append(user);
        symbolic.append(group);
        symbolic.append(other);

        return symbolic.toString();
    }

    public static final class Action {
        private final char MULTIPLE_VALUES = '\u2022';

        public Boolean read;
        public Boolean write;
        public Boolean execute;

        public Action(Boolean read, Boolean write, Boolean execute) {
            this.read = read;
            this.write = write;
            this.execute = execute;
        }

        private static Permission.Action solve(Permission.Action base, Permission.Action permission, boolean value) {
            return value ? base.or(permission) : base.and(permission.not());
        }

        public char mode() {
            final char intermediate = MULTIPLE_VALUES;
            int value = 0;

            if(this.read == null) {
                return intermediate;
            }
            value += this.read ? 4 : 0;

            if(this.write == null) {
                return intermediate;
            }
            value += this.write ? 2 : 0;

            if(this.execute == null) {
                return intermediate;
            }
            value += this.execute ? 1 : 0;
            return Character.forDigit(value, 8);
        }

        public void parse(char c) {
            if(CharUtils.isAsciiNumeric(c)) {
                int intValue = CharUtils.toIntValue(c);
                this.read = (intValue & 4) > 0;
                this.write = (intValue & 2) > 0;
                this.execute = (intValue & 1) > 0;
            }
            else {
                if(c == MULTIPLE_VALUES) {
                    this.read = this.write = this.execute = null;
                }
            }
        }

        public Permission.Action resolve(Permission.Action original) {
            Permission.Action result = Permission.Action.none;

            result = solve(result, Permission.Action.read, read == null ? original.implies(Permission.Action.read) : read);
            result = solve(result, Permission.Action.write, write == null ? original.implies(Permission.Action.write) : write);
            result = solve(result, Permission.Action.execute, execute == null ? original.implies(Permission.Action.execute) : execute);

            return result;
        }

        @Override
        public String toString() {
            final StringBuilder symbolic = new StringBuilder();

            symbolic.append(read != null ? read ? 'r' : '-' : MULTIPLE_VALUES);
            symbolic.append(write != null ? write ? 'w' : '-' : MULTIPLE_VALUES);
            symbolic.append(execute != null ? execute ? 'x' : '-' : MULTIPLE_VALUES);

            return symbolic.toString();
        }

    }
}
