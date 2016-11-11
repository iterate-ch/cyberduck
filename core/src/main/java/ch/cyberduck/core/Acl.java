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

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public final class Acl extends HashMap<Acl.User, Set<Acl.Role>> {
    private static final long serialVersionUID = 372192161904802600L;

    public static final Acl EMPTY = new Acl();

    private CanonicalUser owner;

    public Acl() {
        super();
    }

    public Acl(Acl.User user, Acl.Role... permissions) {
        this.addAll(user, permissions);
    }

    public Acl(Acl.UserAndRole... set) {
        this.addAll(set);
    }

    public CanonicalUser getOwner() {
        return owner;
    }

    public void setOwner(CanonicalUser owner) {
        this.owner = owner;
    }

    /**
     * @param user        Grantee
     * @param permissions Permissions
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
     * @return List of users and roles
     */
    public List<UserAndRole> asList() {
        List<UserAndRole> grants = new ArrayList<UserAndRole>();
        for(Map.Entry<User, Set<Role>> user : this.entrySet()) {
            for(Acl.Role role : user.getValue()) {
                grants.add(new UserAndRole(user.getKey(), role));
            }
        }
        Collections.sort(grants);
        return grants;
    }

    /**
     * @return True if a user of role has been edited
     */
    public boolean isModified() {
        for(UserAndRole ua : this.asList()) {
            if(ua.getUser().isModified()) {
                return true;
            }
            if(ua.getRole().isModified()) {
                return true;
            }
        }
        return false;
    }

    public static class UserAndRole implements Comparable<UserAndRole> {
        private final Acl.User user;
        private final Acl.Role role;

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
                return user.equals(((UserAndRole) obj).user)
                        && role.equals(((UserAndRole) obj).role);
            }
            return false;
        }

        public boolean isValid() {
            return user.isValid() && role.isValid();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public String toString() {
            return String.format("%s:%s", user.toString(), role.toString());
        }

        @Override
        public int compareTo(UserAndRole o) {
            return this.toString().compareTo(o.toString());
        }
    }

    public static abstract class User implements Comparable<User> {

        private String identifier;
        private final boolean editable;
        private boolean modified;

        public User(String identifier) {
            this(identifier, true);
        }

        public User(String identifier, boolean editable) {
            this.identifier = identifier;
            this.editable = editable;
        }

        /**
         * @return Placeholder string as help text.
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
            return LocaleFactory.localizedString(this.getIdentifier(), "S3");
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
            this.modified = true;
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

        @Override
        public int compareTo(User o) {
            return this.getIdentifier().compareTo(o.getIdentifier());
        }

        public boolean isModified() {
            return modified;
        }
    }

    /**
     * A canonical grantee.
     */
    public static class CanonicalUser extends User {

        private String displayName;

        public CanonicalUser() {
            this(StringUtils.EMPTY, null);
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
            return LocaleFactory.localizedString("Canonical User ID", "S3");
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
                return String.format("%s (%s)", displayName, this.getIdentifier());
            }
            return super.toString();
        }
    }

    /**
     * An email grantee
     */
    public static class EmailUser extends CanonicalUser {
        public EmailUser() {
            super(StringUtils.EMPTY, true);
        }

        public EmailUser(String identifier) {
            super(identifier, true);
        }

        public EmailUser(String identifier, boolean editable) {
            super(identifier, editable);
        }

        public EmailUser(String identifier, String displayName, boolean editable) {
            super(identifier, displayName, editable);
        }

        @Override
        public String getPlaceholder() {
            return LocaleFactory.localizedString("Email Address", "S3");
        }
    }

    public static class GroupUser extends User {

        public static final String EVERYONE = "AllUsers";
        public static final String AUTHENTICATED = "AllAuthenticatedUsers";

        public GroupUser(String identifier) {
            this(identifier, false);
        }

        public GroupUser(String identifier, boolean editable) {
            super(identifier, editable);
        }

        @Override
        public String getPlaceholder() {
            return LocaleFactory.localizedString(this.getIdentifier(), "S3");
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
            return LocaleFactory.localizedString("Email Address", "S3");
        }
    }

    public static class DomainUser extends User {
        public DomainUser(String identifier) {
            super(identifier, true);
        }

        @Override
        public String getPlaceholder() {
            return LocaleFactory.localizedString("Domain Name", "S3");
        }
    }

    /**
     * The permission.
     */
    public static class Role implements Comparable<Role> {

        public static final String FULL = "FULL_CONTROL";
        public static final String READ = "READ";
        public static final String WRITE = "WRITE";

        private String name;
        private final boolean editable;
        private boolean modified;

        public Role(String name) {
            this(name, true);
        }

        public Role(String name, boolean editable) {
            this.name = name;
            this.editable = editable;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return LocaleFactory.localizedString(this.getName(), "S3");
        }

        public void setName(String name) {
            this.name = name;
            this.modified = true;
        }

        public boolean isValid() {
            return StringUtils.isNotBlank(this.getName());
        }

        public boolean isEditable() {
            return editable;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Role) {
                return StringUtils.equals(name, ((Role)obj).name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName());
        }

        @Override
        public int compareTo(Role o) {
            return this.getName().compareTo(o.getName());
        }

        public boolean isModified() {
            return modified;
        }
    }
}