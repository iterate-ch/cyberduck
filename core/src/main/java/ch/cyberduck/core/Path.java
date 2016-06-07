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

import ch.cyberduck.core.serializer.Serializer;

import java.util.EnumSet;
import java.util.Objects;

public class Path extends AbstractPath implements Referenceable, Serializable {

    /**
     * The path delimiter for remote paths
     */
    public static final char DELIMITER = '/';
    /**
     * Reference to the parent
     */
    protected Path parent;
    /**
     * The absolute remote path
     */
    private String path;
    /**
     * An absolute reference here the symbolic link is pointing to
     */
    private Path symlink;
    /**
     * The file type
     */
    private EnumSet<Type> type
            = EnumSet.noneOf(Type.class);

    /**
     * Attributes denoting this path
     */
    private PathAttributes attributes;

    /**
     * @param parent the absolute directory
     * @param name   the file relative to param path
     * @param type   File type
     */
    public Path(final Path parent, final String name, final EnumSet<Type> type) {
        this.type = type;
        this.attributes = new PathAttributes();
        this.attributes.setRegion(parent.attributes.getRegion());
        this._setPath(parent, name);
    }

    /**
     * @param absolute The absolute path of the remote file
     * @param type     File type
     */
    public Path(final String absolute, final EnumSet<Type> type) {
        this.type = type;
        this.attributes = new PathAttributes();
        this.setPath(absolute);
    }

    /**
     * @param absolute   The absolute path of the remote file
     * @param attributes File type
     */
    public Path(final String absolute, final EnumSet<Type> type, final PathAttributes attributes) {
        this.type = type;
        this.attributes = attributes;
        this.setPath(absolute);
    }

    /**
     * @param parent     Parent path reference
     * @param name       Filename
     * @param attributes Attributes
     */
    public Path(final Path parent, final String name, final EnumSet<Type> type, final PathAttributes attributes) {
        this.type = type;
        this.attributes = attributes;
        this._setPath(parent, name);
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(String.valueOf(type), "Type");
        dict.setStringForKey(this.getAbsolute(), "Remote");
        if(symlink != null) {
            dict.setObjectForKey(symlink, "Symbolic Link");
        }
        dict.setObjectForKey(attributes, "Attributes");
        return dict.getSerialized();
    }

    private void setPath(final String absolute) {
        if(absolute.equals(String.valueOf(Path.DELIMITER))) {
            this._setPath(null, PathNormalizer.name(absolute));
        }
        else {
            final Path parent = new Path(PathNormalizer.parent(PathNormalizer.normalize(absolute, true), Path.DELIMITER),
                    EnumSet.of(Type.directory));
            parent.attributes().setRegion(attributes.getRegion());
            if(parent.isRoot()) {
                parent.setType(EnumSet.of(Type.volume, Type.directory));
            }
            this._setPath(parent, PathNormalizer.name(absolute));
        }
    }

    private void _setPath(final Path parent, final String name) {
        this.parent = parent;
        if(null == parent) {
            this.path = name;
        }
        else {
            if(parent.isRoot()) {
                this.path = parent.getAbsolute() + name;
            }
            else {
                if(name.startsWith(String.valueOf(DELIMITER))) {
                    this.path = parent.getAbsolute() + name;
                }
                else {
                    this.path = parent.getAbsolute() + Path.DELIMITER + name;
                }
            }
        }
    }

    @Override
    public EnumSet<Type> getType() {
        return type;
    }

    public void setType(final EnumSet<Type> type) {
        this.type = type;
    }

    public boolean isVolume() {
        return type.contains(Type.volume);
    }

    public boolean isDirectory() {
        return type.contains(Type.directory);
    }

    public boolean isPlaceholder() {
        return type.contains(Type.placeholder);
    }

    public boolean isFile() {
        return type.contains(Type.file);
    }

    public boolean isSymbolicLink() {
        return type.contains(Type.symboliclink);
    }

    @Override
    public char getDelimiter() {
        return String.valueOf(DELIMITER).charAt(0);
    }

    public Path getParent() {
        if(this.isRoot()) {
            return this;
        }
        return parent;
    }

    public PathAttributes attributes() {
        return attributes;
    }

    /**
     * @return the path relative to its parent directory
     */
    @Override
    public String getName() {
        if(this.isRoot()) {
            return String.valueOf(DELIMITER);
        }
        final String abs = this.getAbsolute();
        int index = abs.lastIndexOf(DELIMITER);
        return abs.substring(index + 1);
    }

    /**
     * @return the absolute path name, e.g. /home/user/filename
     */
    @Override
    public String getAbsolute() {
        return path;
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see #isSymbolicLink
     */
    public Path getSymlinkTarget() {
        return symlink;
    }

    public void setSymlinkTarget(final Path target) {
        this.symlink = target;
    }

    /**
     * @return The hashcode of #getAbsolute()
     * @see #getAbsolute()
     */
    @Override
    public int hashCode() {
        return new DefaultPathReference(this).hashCode();
    }

    /**
     * @param other Path to compare with
     * @return true if the other path has the same absolute path name
     */
    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Path) {
            return new DefaultPathReference(this).equals(new DefaultPathReference((Path) other));
        }
        return false;
    }

    /**
     * @return The absolute path name
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Path{");
        sb.append("path='").append(path).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    /**
     * @param directory Parent directory
     * @return True if this is a child in the path hierarchy of the argument passed
     */
    public boolean isChild(final Path directory) {
        if(directory.isFile()) {
            // If a file we don't have any children at all
            return false;
        }
        if(this.isRoot()) {
            // Root cannot be a child of any other path
            return false;
        }
        if(directory.isRoot()) {
            // Any other path is a child
            return true;
        }
        if(Objects.equals(this.getParent(), directory.getParent())) {
            // Cannot be a child if the same parent
            return false;
        }
        for(Path parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.equals(directory)) {
                return true;
            }
        }
        return false;
    }
}