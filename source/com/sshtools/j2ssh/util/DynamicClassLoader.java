/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


/**
 * This class implements a dynamic class loader that enables the Sshtools J2SSH
 * library to load classes dynamically at runtime from extension jars.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 *
 * @created 20 December 2002
 */
public class DynamicClassLoader
    extends ClassLoader {
    private static Logger log = Logger.getLogger(DynamicClassLoader.class);

    /**
     * Generation counter, incremented for each classloader as they are
     * created.
     */
    private static int generationCounter = 0;

    /**
     * Cache of the loaded classes. This contains ClassCacheEntry keyed by
     * class names.
     */
    private Hashtable cache;

    /**
     * The classpath which this classloader searches for class definitions.
     * Each element of the vector should be either a directory, a .zip file,
     * or a .jar file.
     *
     * <p>
     * It may be empty when only system classes are controlled.
     * </p>
     */
    private List classpath = new Vector();

    /**
     * Generation number of the classloader, used to distinguish between
     * different instances.
     */
    private int generation;

    /**
     * Creates a new class loader that will load classes from specified
     * classpath.
     *
     * @param classpath An set of File classes indicating directories and/or
     *        jar files.
     *
     * @exception IllegalArgumentException Description of the Exception
     */
    public DynamicClassLoader(List classpath)
                       throws IllegalArgumentException {
        // Create the cache to hold the loaded classes
        cache = new Hashtable();

        Iterator it = classpath.iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            File f = null;

            if (obj instanceof String) {
                f = new File((String) obj);
            } else if (obj instanceof File) {
                f = (File) obj;
            } else {
                throw new IllegalArgumentException("Entries in classpath must be either a String or File object");
            }

            if (!f.exists()) {
                throw new IllegalArgumentException("Classpath "
                                                   + f.getAbsolutePath()
                                                   + " doesn't exist!");
            } else if (!f.canRead()) {
                throw new IllegalArgumentException("Don't have read access for file "
                                                   + f.getAbsolutePath());
            }

            // Check that it is a directory or jar file
            if (!(f.isDirectory() || isJarArchive(f))) {
                throw new IllegalArgumentException(f.getAbsolutePath()
                                                   + " is not a directory or jar file"
                                                   + " or if it's a jar file then it is corrupted.");
            }

            this.classpath.add(f);
        }

        // Increment and store generation counter
        this.generation = generationCounter++;
    }

    /**
     * Find a resource with a given name. The return is a URL to the resource.
     * Doing a getContent() on the URL may return an Image, an AudioClip,or an
     * InputStream.
     *
     * <p>
     * This classloader looks for the resource only in the directory classpath
     * for this resource.
     * </p>
     *
     * @param name the name of the resource, to be used as is.
     *
     * @return an URL on the resource, or null if not found.
     */
    public URL getResource(String name) {
        URL u = getSystemResource(name);

        if (u!=null) {
            return u;
        }

        // Load for it only in directories since no URL can point into
        // a zip file.
        Iterator it = classpath.iterator();

        while (it.hasNext()) {
            File file = (File) it.next();

            if (file.isDirectory()) {
                String fileName = name.replace('/', File.separatorChar);
                File resFile = new File(file, fileName);

                if (resFile.exists()) {
                    // Build a file:// URL form the file name
                    try {
                        return new URL("file://" + resFile.getAbsolutePath());
                    } catch (java.net.MalformedURLException badurl) {
                        badurl.printStackTrace();

                        return null;
                    }
                }
            }
        }

        // Not found
        return null;
    }

    /**
     * Get an InputStream on a given resource. Will return null if no resource
     * with this name is found.
     *
     * <p></p>
     *
     * @param name the name of the resource, to be used as is.
     *
     * @return an InputStream on the resource, or null if not found.
     *
     * @see java.lang.Class#getResourceAsStream(String)
     */
    public InputStream getResourceAsStream(String name) {
        // Try to load it from the system class
        InputStream s = getSystemResourceAsStream(name);

        if (s==null) {
            // Try to find it from every classpath
            Iterator it = classpath.iterator();

            while (it.hasNext()) {
                File file = (File) it.next();

                if (file.isDirectory()) {
                    s = loadResourceFromDirectory(file, name);
                } else {
                    s = loadResourceFromZipfile(file, name);
                }

                if (s!=null) {
                    break;
                }
            }
        }

        return s;
    }

    /**
     * Re-instantiate this class loader.
     *
     * <p>
     * This method creates a new instance of the class loader that will load
     * classes from the same path as this one.
     * </p>
     *
     * @return A new instance of DynamicClassLoader with the current classpath
     */
    public DynamicClassLoader reinstantiate() {
        return new DynamicClassLoader(classpath);
    }

    /**
     * Check to see if a given class should be reloaded because of a
     * modification to the original class.
     *
     * @param classname The name of the class to check for modification.
     *
     * @return <tt>true</tt> if the class should be reloaded otherwise
     *         <tt>false</tt>
     */
    public synchronized boolean shouldReload(String classname) {
        ClassCacheEntry entry = (ClassCacheEntry) cache.get(classname);

        if (entry==null) {
            // class wasn't even loaded
            return false;
        } else if (entry.isSystemClass()) {
            // System classes cannot be reloaded
            return false;
        } else {
            boolean reload = (entry.origin.lastModified()!=entry.lastModified);

            return reload;
        }
    }

    /**
     * Check whether the classloader should be reinstantiated.
     *
     * <P>
     * The classloader must be replaced if there is any class whose origin file
     * has changed since it was last loaded.
     * </p>
     *
     * @return <tt>true</tt> if the class should be reloaded otherwise
     *         <tt>false</tt>
     */
    public synchronized boolean shouldReload() {
        // Check whether any class has changed
        Enumeration e = cache.elements();

        while (e.hasMoreElements()) {
            ClassCacheEntry entry = (ClassCacheEntry) e.nextElement();

            if (entry.isSystemClass()) {
                continue;
            }

            long msOrigin = entry.origin.lastModified();

            if (msOrigin==0) {
                // class no longer exists
                return true;
            }

            if (msOrigin!=entry.lastModified) {
                // class is modified
                return true;
            }
        }

        // No changes, no need to reload
        return false;
    }

    /**
     * Resolves the specified name to a Class. The method loadClass() is called
     * by the virtual machine. As an abstract method, loadClass() must be
     * defined in a subclass of ClassLoader.
     *
     * @param name the name of the desired Class.
     * @param resolve true if the Class needs to be resolved; false if the
     *        virtual machine just wants to determine whether the class exists
     *        or not
     *
     * @return the resulting Class.
     *
     * @exception ClassNotFoundException if the class loader cannot find a the
     *            requested class.
     */
    protected synchronized Class loadClass(String name, boolean resolve)
                                    throws ClassNotFoundException {
        // The class object that will be returned.
        Class c = null;

        // Use the cached value, if this class is already loaded into
        // this classloader.
        ClassCacheEntry entry = (ClassCacheEntry) cache.get(name);

        if (entry!=null) {
            // Class found in our cache
            c = entry.loadedClass;

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }

        if (!securityAllowsClass(name)) {
            return loadSystemClass(name, resolve);
        }

        // Attempt to load the class from the system
        try {
            c = loadSystemClass(name, resolve);

            if (c!=null) {
                if (resolve) {
                    resolveClass(c);
                }

                return c;
            }
        } catch (Exception e) {
            c = null;
        }

        // Try to load it from each classpath
        Iterator it = classpath.iterator();

        // Cache entry.
        ClassCacheEntry classCache = new ClassCacheEntry();

        while (it.hasNext()) {
            byte classData[];

            File file = (File) it.next();

            try {
                if (file.isDirectory()) {
                    classData = loadClassFromDirectory(file, name, classCache);
                } else {
                    classData = loadClassFromZipfile(file, name, classCache);
                }
            } catch (IOException ioe) {
                // Error while reading in data, consider it as not found
                classData = null;
            }

            if (classData!=null) {
                // Define the class
                c = defineClass(name, classData, 0, classData.length);

                // Cache the result;
                classCache.loadedClass = c;

                // Origin is set by the specific loader
                classCache.lastModified = classCache.origin.lastModified();
                cache.put(name, classCache);

                // Resolve it if necessary
                if (resolve) {
                    resolveClass(c);
                }

                return c;
            }
        }

        // If not found in any classpath
        throw new ClassNotFoundException(name);
    }

    /**
     * Test if a file is a jar archive.
     *
     * @param file the file to be tested.
     *
     * @return true if the file is a jar archive, false otherwise.
     */
    private boolean isJarArchive(File file) {
        boolean isArchive = true;
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(file);
        } catch (ZipException zipCurrupted) {
            isArchive = false;
        } catch (IOException anyIOError) {
            isArchive = false;
        } finally {
            if (zipFile!=null) {
                try {
                    zipFile.close();
                } catch (IOException ignored) {
                }
            }
        }

        return isArchive;
    }

    /**
     * Loads all the bytes of an InputStream.
     *
     * @param in the InputStream to load from
     * @param length the length to read
     *
     * @return the bytes read
     *
     * @throws IOException if an IO error occurs
     */
    private byte[] loadBytesFromStream(InputStream in, int length)
                                throws IOException {
        byte buf[] = new byte[length];
        int nRead;
        int count = 0;

        while ((length>0) && ((nRead = in.read(buf, count, length))!=-1)) {
            count += nRead;
            length -= nRead;
        }

        return buf;
    }

    /**
     * Tries to load the class from a directory.
     *
     * @param dir The directory that contains classes.
     * @param name The classname
     * @param cache The cache entry to set the file if successful.
     *
     * @return the class data
     *
     * @throws IOException if an IO error occurs
     */
    private byte[] loadClassFromDirectory(File dir, String name,
                                          ClassCacheEntry cache)
                                   throws IOException {
        // Translate class name to file name
        String classFileName = name.replace('.', File.separatorChar) + ".class";

        // Check for garbage input at beginning of file name
        // i.e. ../ or similar
        if (!Character.isJavaIdentifierStart(classFileName.charAt(0))) {
            // Find real beginning of class name
            int start = 1;

            while (!Character.isJavaIdentifierStart(classFileName.charAt(start++))) {
                ;
            }

            classFileName = classFileName.substring(start);
        }

        File classFile = new File(dir, classFileName);

        if (classFile.exists()) {
            cache.origin = classFile;

            InputStream in = new FileInputStream(classFile);

            try {
                return loadBytesFromStream(in, (int) classFile.length());
            } finally {
                in.close();
            }
        } else {
            // Not found
            return null;
        }
    }

    /**
     * Tries to load the class from a zip file.
     *
     * @param file The zipfile that contains classes.
     * @param name The classname
     * @param cache The cache entry to set the file if successful.
     *
     * @return the class data
     *
     * @throws IOException if an IO error occurs
     */
    private byte[] loadClassFromZipfile(File file, String name,
                                        ClassCacheEntry cache)
                                 throws IOException {
        // Translate class name to file name
        String classFileName = name.replace('.', '/') + ".class";

        ZipFile zipfile = new ZipFile(file);

        try {
            ZipEntry entry = zipfile.getEntry(classFileName);

            if (entry!=null) {
                cache.origin = file;

                return loadBytesFromStream(zipfile.getInputStream(entry),
                                           (int) entry.getSize());
            } else {
                // Not found
                return null;
            }
        } finally {
            zipfile.close();
        }
    }

    /**
     * Loads resource from a directory.
     *
     * @param dir the directory to load from
     * @param name the name of the resource
     *
     * @return an InputStream to the resource
     */
    private InputStream loadResourceFromDirectory(File dir, String name) {
        // Name of resources are always separated by /
        String fileName = name.replace('/', File.separatorChar);
        File resFile = new File(dir, fileName);

        if (resFile.exists()) {
            try {
                return new FileInputStream(resFile);
            } catch (FileNotFoundException shouldnothappen) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Loads resource from a zip file
     *
     * @param file the zip file name
     * @param name the name of the resource
     *
     * @return an InputStream to the resource
     */
    private InputStream loadResourceFromZipfile(File file, String name) {
        try {
            ZipFile zipfile = new ZipFile(file);
            ZipEntry entry = zipfile.getEntry(name);

            if (entry!=null) {
                return zipfile.getInputStream(entry);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Load a class using the system classloader.
     *
     * @param name the name of the class
     * @param resolve <tt>true</tt> if the class should resolved otherwise
     *        <tt>false</tt>
     *
     * @return the Class loaded
     *
     * @exception NoClassDefFoundError if the class loader cannot find a
     *            definition for the class.
     * @exception ClassNotFoundException if the class loader cannot find a the
     *            requested class.
     */
    private Class loadSystemClass(String name, boolean resolve)
                           throws NoClassDefFoundError, ClassNotFoundException {
        Class c = findSystemClass(name);

        // Throws if not found.
        // Add cache entry
        ClassCacheEntry cacheEntry = new ClassCacheEntry();
        cacheEntry.origin = null;
        cacheEntry.loadedClass = c;
        cacheEntry.lastModified = Long.MAX_VALUE;
        cache.put(name, cacheEntry);

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }

    /**
     * Checks whether a classloader is allowed to define a given class, within
     * the security manager restrictions.
     *
     * @param className the class name
     *
     * @return <tt>true</tt> if the classloader can define a given class
     *         otherwise <tt>false</tt>
     */
    private boolean securityAllowsClass(String className) {
        try {
            SecurityManager security = System.getSecurityManager();

            if (security==null) {
                // if there's no security manager then all classes
                // are allowed to be loaded
                return true;
            }

            int lastDot = className.lastIndexOf('.');

            // Check if we are allowed to load the class' package
            security.checkPackageDefinition((lastDot>-1)
                                            ? className.substring(0, lastDot) : "");

            // Throws if not allowed
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Private class used to maintain information about the classes that we
     * loaded.
     *
     * @author lee
     *
     * @created 20 December 2002
     */
    private static class ClassCacheEntry {
        /** The actual loaded class */
        Class loadedClass;

        /**
         * The file from which this class was loaded; or null if it was loaded
         * from the system.
         */
        File origin;

        /**
         * The time at which the class was loaded from the origin file, in ms
         * since the epoch.
         */
        long lastModified;

        /**
         * Check whether this class was loaded from the system.
         *
         * @return <tt>true</tt> if the class is a system class otherwise
         *         <tt>false</tt>
         */
        public boolean isSystemClass() {
            return origin==null;
        }
    }
}
