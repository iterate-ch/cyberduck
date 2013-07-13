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

import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class Path extends AbstractPath implements Serializable {

    /**
     * The absolute remote path
     */
    private String path;

    /**
     * Reference to the parent
     */
    protected Path parent;

    /**
     * The local path to be used if file is copied
     */
    private Local local;

    /**
     * An absolute reference here the symbolic link is pointing to
     */
    private Path symlink;

    /**
     * Attributes denoting this path
     */
    private PathAttributes attributes;

    public <T> Path(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        String pathObj = dict.stringForKey("Remote");
        if(pathObj != null) {
            this.setPath(pathObj);
        }
        final String localObj = dict.stringForKey("Local");
        if(localObj != null) {
            this.local = LocalFactory.createLocal(localObj);
        }
        final Object symlinkObj = dict.objectForKey("Symbolic Link");
        if(symlinkObj != null) {
            this.symlink = new Path(symlinkObj);
        }
        final Object attributesObj = dict.objectForKey("Attributes");
        if(attributesObj != null) {
            this.attributes = new PathAttributes(attributesObj);
        }
        else {
            this.attributes = new PathAttributes(Path.FILE_TYPE);
        }
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        return this.getAsDictionary(dict);
    }

    protected <S> S getAsDictionary(Serializer dict) {
        dict.setStringForKey(this.getAbsolute(), "Remote");
        if(local != null) {
            dict.setStringForKey(local.getAbsolute(), "Local");
        }
        if(symlink != null) {
            dict.setObjectForKey(symlink, "Symbolic Link");
        }
        dict.setObjectForKey(attributes, "Attributes");
        return dict.getSerialized();
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param parent the absolute directory
     * @param name   the file relative to param path
     * @param type   File type
     */
    public Path(final Path parent, final String name, final int type) {
        this._setPath(parent, name);
        this.attributes = new PathAttributes(type);
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param absolute The absolute path of the remote file
     * @param type     File type
     */
    public Path(final String absolute, final int type) {
        this.setPath(absolute);
        this.attributes = new PathAttributes(type);
    }

    /**
     * Create a new path where you know the local file already exists
     * and the remote equivalent might be created later.
     * The remote filename will be extracted from the local file.
     *
     * @param parent The absolute path to the parent directory on the remote host
     * @param file   The associated local file
     */
    public Path(final Path parent, final Local file) {
        this._setPath(parent, file.getName());
        this.local = file;
        this.attributes = new PathAttributes(local.attributes().isDirectory() ? DIRECTORY_TYPE : FILE_TYPE);
    }

    private void setPath(final String absolute) {
        if(absolute.equals(String.valueOf(Path.DELIMITER))) {
            this._setPath((Path) null, Path.getName(PathNormalizer.normalize(absolute, true)));
        }
        else {
            final Path parent = new Path(Path.getParent(PathNormalizer.normalize(absolute, true), Path.DELIMITER),
                    Path.DIRECTORY_TYPE);
            if(parent.isRoot()) {
                parent.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
            }
            this._setPath(parent,
                    Path.getName(PathNormalizer.normalize(absolute, true)));
        }
    }

    /**
     * @param parent The parent directory
     * @param name   The filename
     */
    public void setPath(final Path parent, final String name) {
        this._setPath(parent, name);
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
    public String unique() {
        if(StringUtils.isNotBlank(this.attributes().getRegion())) {
            return String.format("%s-%s", super.unique(), this.attributes().getRegion());
        }
        return super.unique();
    }

    /**
     * The path delimiter for remote paths
     */
    public static final char DELIMITER = '/';

    @Override
    public char getPathDelimiter() {
        return String.valueOf(DELIMITER).charAt(0);
    }

    public Path getParent() {
        if(this.isRoot()) {
            return this;
        }
        return parent;
    }

    /**
     * Default implementation returning a reference to self. You can override this
     * if you need a different strategy to compare hashcode and equality for caching
     * in a model.
     *
     * @return Reference to the path to be used in table models an file listing cache.
     * @see ch.cyberduck.core.Cache#lookup(PathReference)
     */
    @Override
    public PathReference getReference() {
        return PathReferenceFactory.createPathReference(this);
    }

    @Override
    public PathAttributes attributes() {
        return attributes;
    }

    public void setAttributes(final PathAttributes attributes) {
        this.attributes = attributes;
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
     * Set the local equivalent of this path
     *
     * @param file Send <code>null</code> to reset the local path to the default value
     */
    public void setLocal(final Local file) {
        this.local = file;
    }

    /**
     * @return The local alias of this path
     */
    public Local getLocal() {
        return local;
    }

    public void setSymlinkTarget(final Path name) {
        this.symlink = name;
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    public Path getSymlinkTarget() {
        final PathAttributes attributes = this.attributes();
        if(attributes.isSymbolicLink()) {
            return symlink;
        }
        return null;
    }

    /**
     * @return The hashcode of #getAbsolute()
     * @see #getAbsolute()
     */
    @Override
    public int hashCode() {
        return this.getReference().hashCode();
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
            return this.getReference().equals(((Path) other).getReference());
        }
        return false;
    }

    /**
     * @return The absolute path name
     */
    @Override
    public String toString() {
        return this.getAbsolute();
    }

    /**
     * @param directory Parent directory
     * @return True if this is a child in the path hierarchy of the argument passed
     */
    public boolean isChild(final Path directory) {
        if(directory.attributes().isFile()) {
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
        if(ObjectUtils.equals(this.getParent(), directory.getParent())) {
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