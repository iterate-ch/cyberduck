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

import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Acl extends HashMap<Acl.User, Set<Acl.Role>> implements Serializable {
    private static final Logger log = LogManager.getLogger(Acl.class);

    public static final Acl EMPTY = new Acl();
    /**
     * A pre-canned REST ACL to set an object's permissions to Private (only owner can read/write)
     */
    public static final Acl CANNED_PRIVATE = new Acl("private");
    /**
     * A pre-canned REST ACL to set an object's permissions to Public Read (anyone can read, only owner can write)
     */
    public static final Acl CANNED_PUBLIC_READ = new Acl("public-read");
    /**
     * A pre-canned REST ACL to set an object's permissions to Public Read and Write (anyone can read/write)
     */
    public static final Acl CANNED_PUBLIC_READ_WRITE = new Acl("public-read-write");
    /**
     * A pre-canned REST ACL to set an object's permissions to Authenticated Read (authenticated Amazon users can read,
     * only owner can write)
     */
    public static final Acl CANNED_AUTHENTICATED_READ = new Acl("authenticated-read");
    public static final Acl CANNED_BUCKET_OWNER_FULLCONTROL = new Acl("bucket-owner-full-control");
    public static final Acl CANNED_BUCKET_OWNER_READ = new Acl("bucket-owner-read");

    /**
     * @param identifier Canned ACL identifier string
     * @return Static ACL that can be set as request parameter
     */
    public static Acl toAcl(final String identifier) {
        if(CANNED_PRIVATE.getCannedString().equals(identifier)) {
            return Acl.CANNED_PRIVATE;
        }
        if(CANNED_PUBLIC_READ.getCannedString().equals(identifier)) {
            return Acl.CANNED_PUBLIC_READ;
        }
        if(CANNED_PUBLIC_READ_WRITE.getCannedString().equals(identifier)) {
            return Acl.CANNED_PUBLIC_READ_WRITE;
        }
        if(CANNED_AUTHENTICATED_READ.getCannedString().equals(identifier)) {
            return Acl.CANNED_AUTHENTICATED_READ;
        }
        if(CANNED_BUCKET_OWNER_FULLCONTROL.getCannedString().equals(identifier)) {
            return Acl.CANNED_BUCKET_OWNER_FULLCONTROL;
        }
        if(CANNED_BUCKET_OWNER_READ.getCannedString().equals(identifier)) {
            return Acl.CANNED_BUCKET_OWNER_READ;
        }
        log.warn(String.format("Unknown canned ACL identifier %s", identifier));
        return Acl.EMPTY;
    }

    /**
     * Canned ACL identifier
     */
    private final String canned;
    /**
     * Read only when false
     */
    private boolean editable = true;

    public Acl(final String canned) {
        this.canned = canned;
    }

    public Acl(Acl.User user, Acl.Role... permissions) {
        this.addAll(user, permissions);
        this.canned = StringUtils.EMPTY;
    }

    public Acl(Acl.UserAndRole... set) {
        this.addAll(set);
        this.canned = StringUtils.EMPTY;
    }

    public Acl(final Acl other) {
        this.putAll(other);
        this.canned = other.canned;
        this.editable = other.editable;
    }

    public boolean isCanned() {
        return CANNED_PRIVATE.equals(this)
                || CANNED_PUBLIC_READ_WRITE.equals(this)
                || CANNED_PUBLIC_READ.equals(this)
                || CANNED_AUTHENTICATED_READ.equals(this)
                || CANNED_BUCKET_OWNER_FULLCONTROL.equals(this)
                || CANNED_BUCKET_OWNER_READ.equals(this);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    public String getCannedString() {
        return canned;
    }

    /**
     * @param user        Grantee
     * @param permissions Permissions
     */
    public void addAll(final Acl.User user, final Acl.Role... permissions) {
        if(this.containsKey(user)) {
            this.get(user).addAll(Arrays.asList(permissions));
        }
        else {
            this.put(user, new HashSet<>(Arrays.asList(permissions)));
        }
    }

    public void addAll(final Acl.UserAndRole... set) {
        for(Acl.UserAndRole userAndRole : set) {
            this.addAll(userAndRole.getUser(), userAndRole.getRole());
        }
    }

    /**
     * @return List of users and roles
     */
    public List<UserAndRole> asList() {
        List<UserAndRole> grants = new ArrayList<>();
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

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        for(Entry<User, Set<Role>> entry : this.entrySet()) {
            final List<Role> roles = new ArrayList<>(entry.getValue());
            dict.setListForKey(roles, entry.getKey().getIdentifier());
        }
        return dict.getSerialized();
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

        public boolean isValid() {
            return user.isValid() && role.isValid();
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof UserAndRole)) {
                return false;
            }
            final UserAndRole that = (UserAndRole) o;
            return Objects.equals(user, that.user) &&
                Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, role);
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
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            final User user = (User) o;
            return Objects.equals(identifier, user.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public int compareTo(User o) {
            return identifier.compareTo(o.identifier);
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

    public static class Owner extends CanonicalUser {
        public Owner(final String identifier) {
            super(identifier);
        }

        public Owner(final String identifier, final String displayName) {
            super(identifier, displayName, false);
        }

        @Override
        public String getPlaceholder() {
            return LocaleFactory.localizedString("Owner");
        }

        @Override
        public String getDisplayName() {
            return LocaleFactory.localizedString("Owner");
        }
    }

    /**
     * The permission.
     */
    public static class Role implements Comparable<Role>, Serializable {

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
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Role)) {
                return false;
            }
            final Role role = (Role) o;
            return Objects.equals(name, role.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public int compareTo(Role o) {
            return name.compareTo(o.name);
        }

        public boolean isModified() {
            return modified;
        }

        @Override
        public <T> T serialize(final Serializer<T> dict) {
            dict.setStringForKey(name, "Name");
            return dict.getSerialized();
        }
    }

    public static class RoleDictionary<T> {

        private final DeserializerFactory<T> deserializer;

        public RoleDictionary() {
            this.deserializer = new DeserializerFactory<>();
        }

        public RoleDictionary(final DeserializerFactory<T> deserializer) {
            this.deserializer = deserializer;
        }

        public Acl.Role deserialize(T serialized) {
            final Deserializer dict = deserializer.create(serialized);
            return new Role(dict.stringForKey("Name"));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Acl)) {
            return false;
        }
        if(!super.equals(o)) {
            return false;
        }
        final Acl acl = (Acl) o;
        return Objects.equals(canned, acl.canned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), canned);
    }
}
