/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.util;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class DynamicClassLoader extends ClassLoader {
    private static Log log = LogFactory.getLog(DynamicClassLoader.class);
    private static int generationCounter = 0;
    private Hashtable cache;
    private List classpath = new Vector();
    private int generation;
    private ClassLoader parent;

    /**
     * Creates a new DynamicClassLoader object.
     *
     * @param parent
     * @param classpath
     * @throws IllegalArgumentException
     */
    public DynamicClassLoader(ClassLoader parent, List classpath)
            throws IllegalArgumentException {
        this.parent = parent;

        // Create the cache to hold the loaded classes
        cache = new Hashtable();

        Iterator it = classpath.iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            File f = null;

            if (obj instanceof String) {
                f = new File((String)obj);
            }
            else if (obj instanceof File) {
                f = (File)obj;
            }
            else {
                throw new IllegalArgumentException("Entries in classpath must be either a String or File object");
            }

            if (!f.exists()) {
                throw new IllegalArgumentException("Classpath " +
                        f.getAbsolutePath() + " doesn't exist!");
            }
            else if (!f.canRead()) {
                throw new IllegalArgumentException("Don't have read access for file " + f.getAbsolutePath());
            }

            // Check that it is a directory or jar file
            if (!(f.isDirectory() || isJarArchive(f))) {
                throw new IllegalArgumentException(f.getAbsolutePath() +
                        " is not a directory or jar file" +
                        " or if it's a jar file then it is corrupted.");
            }

            this.classpath.add(f);
        }

        // Increment and store generation counter
        this.generation = generationCounter++;
    }

    /**
     * @param name
     * @return
     */
    public URL getResource(String name) {
        URL u = getSystemResource(name);

        if (u != null) {
            return u;
        }

        // Load for it only in directories since no URL can point into
        // a zip file.
        Iterator it = classpath.iterator();

        while (it.hasNext()) {
            File file = (File)it.next();

            if (file.isDirectory()) {
                String fileName = name.replace('/', File.separatorChar);
                File resFile = new File(file, fileName);

                if (resFile.exists()) {
                    // Build a file:// URL form the file name
                    try {
                        return new URL("file://" + resFile.getAbsolutePath());
                    }
                    catch (java.net.MalformedURLException badurl) {
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
     * @param name
     * @return
     */
    public InputStream getResourceAsStream(String name) {
        // Try to load it from the system class
        InputStream s = getSystemResourceAsStream(name);

        if (s == null) {
            // Try to find it from every classpath
            Iterator it = classpath.iterator();

            while (it.hasNext()) {
                File file = (File)it.next();

                if (file.isDirectory()) {
                    s = loadResourceFromDirectory(file, name);
                }
                else {
                    s = loadResourceFromZipfile(file, name);
                }

                if (s != null) {
                    break;
                }
            }
        }

        return s;
    }

    /**
     * @return
     */
    public DynamicClassLoader reinstantiate() {
        return new DynamicClassLoader(parent, classpath);
    }

    /**
     * @param classname
     * @return
     */
    public synchronized boolean shouldReload(String classname) {
        ClassCacheEntry entry = (ClassCacheEntry)cache.get(classname);

        if (entry == null) {
            // class wasn't even loaded
            return false;
        }
        else if (entry.isSystemClass()) {
            // System classes cannot be reloaded
            return false;
        }
        else {
            boolean reload = (entry.origin.lastModified() != entry.lastModified);

            return reload;
        }
    }

    /**
     * @return
     */
    public synchronized boolean shouldReload() {
        // Check whether any class has changed
        Enumeration e = cache.elements();

        while (e.hasMoreElements()) {
            ClassCacheEntry entry = (ClassCacheEntry)e.nextElement();

            if (entry.isSystemClass()) {
                continue;
            }

            long msOrigin = entry.origin.lastModified();

            if (msOrigin == 0) {
                // class no longer exists
                return true;
            }

            if (msOrigin != entry.lastModified) {
                // class is modified
                return true;
            }
        }

        // No changes, no need to reload
        return false;
    }

    /**
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // The class object that will be returned.
        Class c = null;

        // Use the cached value, if this class is already loaded into
        // this classloader.
        ClassCacheEntry entry = (ClassCacheEntry)cache.get(name);

        if (entry != null) {
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

            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }

                return c;
            }
        }
        catch (Exception e) {
            c = null;
        }

        // Try to load it from each classpath
        Iterator it = classpath.iterator();

        // Cache entry.
        ClassCacheEntry classCache = new ClassCacheEntry();

        while (it.hasNext()) {
            byte[] classData;
            File file = (File)it.next();

            try {
                if (file.isDirectory()) {
                    classData = loadClassFromDirectory(file, name, classCache);
                }
                else {
                    classData = loadClassFromZipfile(file, name, classCache);
                }
            }
            catch (IOException ioe) {
                // Error while reading in data, consider it as not found
                classData = null;
            }

            if (classData != null) {
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

    private boolean isJarArchive(File file) {
        boolean isArchive = true;
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(file);
        }
        catch (ZipException zipCurrupted) {
            isArchive = false;
        }
        catch (IOException anyIOError) {
            isArchive = false;
        }
        finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                }
                catch (IOException ignored) {
                }
            }
        }

        return isArchive;
    }

    private byte[] loadBytesFromStream(InputStream in, int length)
            throws IOException {
        byte[] buf = new byte[length];
        int nRead;
        int count = 0;

        while ((length > 0) && ((nRead = in.read(buf, count, length)) != -1)) {
            count += nRead;
            length -= nRead;
        }

        return buf;
    }

    private byte[] loadClassFromDirectory(File dir, String name,
                                          ClassCacheEntry cache) throws IOException {
        // Translate class name to file name
        String classFileName = name.replace('.', File.separatorChar) +
                ".class";

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
                return loadBytesFromStream(in, (int)classFile.length());
            }
            finally {
                in.close();
            }
        }
        else {
            // Not found
            return null;
        }
    }

    private byte[] loadClassFromZipfile(File file, String name,
                                        ClassCacheEntry cache) throws IOException {
        // Translate class name to file name
        String classFileName = name.replace('.', '/') + ".class";
        ZipFile zipfile = new ZipFile(file);

        try {
            ZipEntry entry = zipfile.getEntry(classFileName);

            if (entry != null) {
                cache.origin = file;

                return loadBytesFromStream(zipfile.getInputStream(entry),
                        (int)entry.getSize());
            }
            else {
                // Not found
                return null;
            }
        }
        finally {
            zipfile.close();
        }
    }

    private InputStream loadResourceFromDirectory(File dir, String name) {
        // Name of resources are always separated by /
        String fileName = name.replace('/', File.separatorChar);
        File resFile = new File(dir, fileName);

        if (resFile.exists()) {
            try {
                return new FileInputStream(resFile);
            }
            catch (FileNotFoundException shouldnothappen) {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private InputStream loadResourceFromZipfile(File file, String name) {
        try {
            ZipFile zipfile = new ZipFile(file);
            ZipEntry entry = zipfile.getEntry(name);

            if (entry != null) {
                return zipfile.getInputStream(entry);
            }
            else {
                return null;
            }
        }
        catch (IOException e) {
            return null;
        }
    }

    private Class loadSystemClass(String name, boolean resolve)
            throws NoClassDefFoundError, ClassNotFoundException {
        //        Class c = findSystemClass(name);
        Class c = parent.loadClass(name);

        if (resolve) {
            resolveClass(c);
        }

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

    private boolean securityAllowsClass(String className) {
        try {
            SecurityManager security = System.getSecurityManager();

            if (security == null) {
                // if there's no security manager then all classes
                // are allowed to be loaded
                return true;
            }

            int lastDot = className.lastIndexOf('.');

            // Check if we are allowed to load the class' package
            security.checkPackageDefinition((lastDot > -1)
                    ? className.substring(0, lastDot) : "");

            // Throws if not allowed
            return true;
        }
        catch (SecurityException e) {
            return false;
        }
    }

    private static class ClassCacheEntry {
        Class loadedClass;
        File origin;
        long lastModified;

        public boolean isSystemClass() {
            return origin == null;
        }
    }
}
