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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Lee David Painter
 * @version $Id$
 */
public class ExtensionClassLoader extends ClassLoader {
    private static Log log = LogFactory.getLog(ExtensionClassLoader.class);
    Vector classpath = new Vector();
    private Hashtable cache = new Hashtable();
    private HashMap packages = new HashMap();

    public ExtensionClassLoader() {
    }

    public ExtensionClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void add(String file) {
        add(new File(file));
    }

    public void add(File[] files) {
        for (int i = 0; i < files.length; i++) {
            add(files[i]);
        }
    }

    public void add(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("Classpath " +
                    file.getAbsolutePath() + " doesn't exist!");
        }
        else if (!file.canRead()) {
            throw new IllegalArgumentException("Don't have read access for file " + file.getAbsolutePath());
        }

        // Check that it is a directory or jar file
        if (!(file.isDirectory() || isJarArchive(file))) {
            throw new IllegalArgumentException(file.getAbsolutePath() +
                    " is not a directory or jar file" +
                    " or if it's a jar file then it is corrupted.");
        }

        log.info("Adding " + file.getAbsolutePath() +
                " to the extension classpath");
        this.classpath.add(file);
    }

    public boolean isJarArchive(File file) {
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

    public URL getResource(String name, File location) {
        if (isJarArchive(location)) {
            return findResourceInZipfile(location, name);
        }
        else {
            return findResourceInDirectory(location, name);
        }
    }

    protected URL findResource(String name) {
        // The class object that will be returned.
        URL url = null;

        // Try to load it from each classpath
        Iterator it = classpath.iterator();

        while (it.hasNext()) {
            byte[] classData;
            File file = (File) it.next();

            if (file.isDirectory()) {
                url = findResourceInDirectory(file, name);
            }
            else {
                url = findResourceInZipfile(file, name);
            }

            if (url != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found resource " + url.toExternalForm());
                }

                return url;
            }

            if (log.isDebugEnabled()) {
                log.debug("Could not find resource " + name);
            }
        }

        return null;
    }

    protected Enumeration findResources(String name) {
        HashSet resources = new HashSet();
        URL url = null;

        // Try to load it from each classpath
        Iterator it = classpath.iterator();

        while (it.hasNext()) {
            byte[] classData;
            File file = (File) it.next();

            if (file.isDirectory()) {
                url = findResourceInDirectory(file, name);
            }
            else {
                url = findResourceInZipfile(file, name);
            }

            if (url != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found resource " + url.toExternalForm());
                }

                resources.add(url);
            }
        }

        return new ResourceEnumeration(resources);
    }

    public byte[] loadClassData(String name) throws ClassNotFoundException {
        // Try to load it from each classpath
        Iterator it = classpath.iterator();

        // Cache entry.
        ClassCacheEntry classCache = new ClassCacheEntry();

        while (it.hasNext()) {
            byte[] classData;
            File file = (File) it.next();

            try {
                if (file.isDirectory()) {
                    classData = loadClassFromDirectory(file, name, null);
                }
                else {
                    classData = loadClassFromZipfile(file, name, null);
                }
            }
            catch (IOException ioe) {
                // Error while reading in data, consider it as not found
                classData = null;
            }

            if (classData != null) {
                return classData;
            }
        }

        // If not found in any classpath
        throw new ClassNotFoundException(name);
    }

    public Class findClass(String name) throws ClassNotFoundException {
        // The class object that will be returned.
        Class c = null;

        // Use the cached value, if this class is already loaded into
        // this classloader.
        ClassCacheEntry entry = (ClassCacheEntry) cache.get(name);

        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Loaded " + name + " from cache");
            }

            // Class found in our cache
            c = entry.loadedClass;
            resolveClass(c);

            return c;
        }

        // Try to load it from each classpath
        Iterator it = classpath.iterator();

        // Cache entry.
        ClassCacheEntry classCache = new ClassCacheEntry();

        while (it.hasNext()) {
            byte[] classData;
            File file = (File) it.next();

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
                // Does the package exist?
                String packageName = "";

                if (name.lastIndexOf(".") > 0) {
                    packageName = name.substring(0, name.lastIndexOf("."));
                }

                if (!packageName.equals("") &&
                        !packages.containsKey(packageName)) {
                    packages.put(packageName,
                            definePackage(packageName, "", "", "", "", "", "", null));

                    // Define the class
                }

                c = defineClass(name, classData, 0, classData.length);

                // Cache the result;
                classCache.loadedClass = c;

                // Origin is set by the specific loader
                classCache.lastModified = classCache.origin.lastModified();
                cache.put(name, classCache);
                resolveClass(c);

                if (log.isDebugEnabled()) {
                    log.debug("Loaded " + name +
                            " adding to cache and returning");
                }

                return c;
            }
        }

        // If not found in any classpath
        throw new ClassNotFoundException(name);
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
            if (cache != null) {
                cache.origin = classFile;
            }

            InputStream in = new FileInputStream(classFile);

            try {
                return loadBytesFromStream(in, (int) classFile.length());
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
                if (cache != null) {
                    cache.origin = file;
                }

                return loadBytesFromStream(zipfile.getInputStream(entry),
                        (int) entry.getSize());
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

    private URL findResourceInDirectory(File dir, String name) {
        // Name of resources are always separated by /
        String fileName = name.replace('/', File.separatorChar);
        File resFile = new File(dir, fileName);

        if (resFile.exists()) {
            try {
                return resFile.toURL();
            }
            catch (MalformedURLException ex) {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private URL findResourceInZipfile(File file, String name) {
        try {
            ZipFile zipfile = new ZipFile(file);
            ZipEntry entry = zipfile.getEntry(name);

            if (entry != null) {
                return new URL("jar:" + file.toURL() + "!" +
                        (name.startsWith("/") ? "" : "/") + name);
            }
            else {
                return null;
            }
        }
        catch (IOException e) {
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

    private class ResourceEnumeration implements Enumeration {
        Set resources;
        Iterator it;

        ResourceEnumeration(Set resources) {
            this.resources = resources;
            it = resources.iterator();
        }

        public boolean hasMoreElements() {
            return it.hasNext();
        }

        public Object nextElement() {
            return it.next();
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
