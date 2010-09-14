package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @version $Id$
 */
public class Acl extends HashMap<Acl.User, Set<Acl.Role>> {

    public static final Acl EMPTY = new Acl();

    public Acl() {
        super();
    }

    public Acl(Acl.User user, Acl.Role... permissions) {
        this.addAll(user, permissions);
    }

    public Acl(Acl.UserAndRole... set) {
        this.addAll(set);
    }

    /**
     * @param user
     * @param permissions
     */
    public void addAll(Acl.User user, Acl.Role... permissions) {
        if(this.containsKey(user)) {
            this.get(user).addAll(Arrays.asList(permissions));
        }
        else {
            this.put(user, new HashSet<Acl.Role>(Arrays.asList(permissions)));
        }
    }

    public void addAll(Acl.UserAndRole... set) {
        for(Acl.UserAndRole userAndRole : set) {
            this.addAll(userAndRole.getUser(), userAndRole.getRole());
        }
    }

    /**
     * @return
     */
    public List<UserAndRole> asList() {
        List<UserAndRole> grants = new ArrayList<UserAndRole>();
        for(Acl.User user : this.keySet()) {
            for(Acl.Role role : this.get(user)) {
                grants.add(new UserAndRole(user, role));
            }
        }
        Collections.sort(grants);
        return grants;
    }

    public static class UserAndRole implements Comparable<UserAndRole> {
        private Acl.User user;
        private Acl.Role role;

        public UserAndRole(User user, Role role) {
            this.user = user;
            this.role = role;
        }

        public User getUser() {
            return user;
        }

        public Role getRole() {
            return role;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof UserAndRole) {
                return this.getUser().equals(((UserAndRole) obj).getUser())
                        && this.getRole().equals(((UserAndRole) obj).getRole());
            }
            return false;
        }

        public boolean isValid() {
            return this.getUser().isValid() && this.getRole().isValid();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public String toString() {
            return this.getUser().toString() + ":" + this.getRole().toString();
        }

        public int compareTo(UserAndRole o) {
            return this.toString().compareTo(o.toString());
        }
    }

    public static abstract class User implements Comparable<User> {

        private String identifier;

        private boolean editable;

        public User(String identifier) {
            this(identifier, true);
        }

        public User(String identifier, boolean editable) {
            this.identifier = identifier;
            this.editable = editable;
        }

        /**
         * Placeholder string as help text.
         *
         * @return
         */
        public abstract String getPlaceholder();

        @Override
        public String toString() {
            return identifier;
        }

        /**
         * @return True if identifier is user editable.
         */
        public boolean isEditable() {
            return editable;
        }

        public String getDisplayName() {
            return Locale.localizedString(this.getIdentifier(), "S3");
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public boolean isValid() {
            return StringUtils.isNotBlank(this.getIdentifier());
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof User) {
                return identifier.equals(((User) obj).getIdentifier());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.getIdentifier().hashCode();
        }

        public int compareTo(User o) {
            return this.getIdentifier().compareTo(o.getIdentifier());
        }
    }

    /**
     * A canonical grantee.
     */
    public static class CanonicalUser extends User {

        private String displayName;

        public CanonicalUser() {
            this("", null);
        }

        public CanonicalUser(String identifier) {
            this(identifier, null);
        }

        public CanonicalUser(String identifier, boolean editable) {
            this(identifier, null, editable);
        }

        public CanonicalUser(String identifier, String displayName) {
            this(identifier, displayName, true);
        }

        public CanonicalUser(String identifier, String displayName, boolean editable) {
            super(identifier, editable);
            this.displayName = displayName;
        }


        @Override
        public String getPlaceholder() {
            return Locale.localizedString("Canonical User ID", "S3");
        }

        @Override
        public String getDisplayName() {
            if(StringUtils.isEmpty(displayName)) {
                return super.getDisplayName();
            }
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            if(StringUtils.isNotBlank(displayName)) {
                return displayName + " (" + this.getIdentifier() + ")";
            }
            return super.toString();
        }
    }

    /**
     * An email grantee
     */
    public static class EmailUser extends User {
        public EmailUser() {
            super("", true);
        }

        public EmailUser(String identifier) {
            super(identifier, true);
        }

        public EmailUser(String identifier, boolean editable) {
            super(identifier, editable);
        }

        @Override
        public String getPlaceholder() {
            return Locale.localizedString("Email Address", "S3");
        }
    }

    public static class GroupUser extends User {
        public GroupUser(String identifier) {
            this(identifier, false);
        }

        public GroupUser(String identifier, boolean editable) {
            super(identifier, editable);
        }

        @Override
        public String getPlaceholder() {
            return Locale.localizedString(this.getIdentifier(), "S3");
        }
    }

    public static class EmailGroupUser extends User {
        public EmailGroupUser(String identifier) {
            this(identifier, false);
        }

        public EmailGroupUser(String identifier, boolean editable) {
            super(identifier, editable);
        }

        @Override
        public String getPlaceholder() {
            return Locale.localizedString("Email Address", "S3");
        }
    }

    public static class DomainUser extends User {
        public DomainUser(String identifier) {
            super(identifier, true);
        }

        @Override
        public String getPlaceholder() {
            return Locale.localizedString("Domain Name", "S3");
        }
    }

    /**
     * The permission.
     */
    public static class Role implements Comparable<Role> {
        private String name;

        public Role(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return Locale.localizedString(this.getName(), "S3");
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isValid() {
            return StringUtils.isNotBlank(this.getName());
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Role) {
                return name.equals(((Role) obj).getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.getName().hashCode();
        }

        public int compareTo(Role o) {
            return this.getName().compareTo(o.getName());
        }
    }
}