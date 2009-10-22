package ch.cyberduck.ui.cocoa.foundation;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSNumber;

/// <i>native declaration : :15</i>
public abstract class NSURL extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSURL", _Class.class);

    public static NSURL URLWithString(String URLString) {
        return CLASS.URLWithString(URLString);
    }

    public static NSURL fileURLWithPath(String URLString) {
        return CLASS.fileURLWithPath(URLString);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>fileURLWithPath(NSString*, BOOL)</code><br>
         * <i>native declaration : :28</i>
         */
        NSURL fileURLWithPath_isDirectory(String path, boolean isDir);

        /**
         * Original signature : <code>fileURLWithPath(String*)</code><br>
         * Better to use fileURLWithPath:isDirectory: if you know if the path is a file vs directory, as it saves an i/o.<br>
         * <i>native declaration : :29</i>
         */
        NSURL fileURLWithPath(String path);

        /**
         * Original signature : <code>URLWithString(String*)</code><br>
         * <i>native declaration : :34</i>
         */
        NSURL URLWithString(String URLString);

        /**
         * Original signature : <code>URLWithString(String*, NSURL*)</code><br>
         * <i>native declaration : :35</i>
         */
        NSURL URLWithString_relativeToURL(String URLString, NSURL baseURL);
    }

    /**
     * Convenience initializers<br>
     * Original signature : <code>initWithScheme(String*, String*, String*)</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract NSURL initWithScheme_host_path(String scheme, String host, String path);

    /**
     * Original signature : <code>initFileURLWithPath(String*, BOOL)</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract NSURL initFileURLWithPath_isDirectory(String path, boolean isDir);

    /**
     * Original signature : <code>initFileURLWithPath(String*)</code><br>
     * Better to use initFileURLWithPath:isDirectory: if you know if the path is a file vs directory, as it saves an i/o.<br>
     * <i>native declaration : :26</i>
     */
    public abstract NSURL initFileURLWithPath(String path);

    /**
     * These methods expect their string arguments to contain any percent escape codes that are necessary<br>
     * Original signature : <code>initWithString(String*)</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract NSURL initWithString(String URLString);

    /**
     * Original signature : <code>initWithString(String*, NSURL*)</code><br>
     * It is an error for URLString to be nil<br>
     * <i>native declaration : :33</i>
     */
    public abstract NSURL initWithString_relativeToURL(String URLString, NSURL baseURL);

    /**
     * Original signature : <code>String* absoluteString()</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract String absoluteString();

    /**
     * Original signature : <code>String* relativeString()</code><br>
     * The relative portion of a URL.  If baseURL is nil, or if the receiver is itself absolute, this is the same as absoluteString<br>
     * <i>native declaration : :38</i>
     */
    public abstract String relativeString();

    /**
     * Original signature : <code>NSURL* baseURL()</code><br>
     * may be nil.<br>
     * <i>native declaration : :39</i>
     */
    public abstract NSURL baseURL();

    /**
     * Original signature : <code>NSURL* absoluteURL()</code><br>
     * if the receiver is itself absolute, this will return self.<br>
     * <i>native declaration : :40</i>
     */
    public abstract NSURL absoluteURL();

    /**
     * Any URL is composed of these two basic pieces.  The full URL would be the concatenation of [myURL scheme], ':', [myURL resourceSpecifier]<br>
     * Original signature : <code>String* scheme()</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract String scheme();

    /**
     * Original signature : <code>String* resourceSpecifier()</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract String resourceSpecifier();

    /**
     * If the URL conforms to rfc 1808 (the most common form of URL), the following accessors will return the various components; otherwise they return nil.  The litmus test for conformance is as recommended in RFC 1808 - whether the first two characters of resourceSpecifier is @"//".  In all cases, they return the component's value after resolving the receiver against its base URL.<br>
     * Original signature : <code>String* host()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract String host();

    /**
     * Original signature : <code>NSNumber* port()</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract NSNumber port();

    /**
     * Original signature : <code>String* user()</code><br>
     * <i>native declaration : :49</i>
     */
    public abstract String user();

    /**
     * Original signature : <code>String* password()</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract String password();

    /**
     * Original signature : <code>String* path()</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract String path();

    /**
     * Original signature : <code>String* fragment()</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract String fragment();

    /**
     * Original signature : <code>String* parameterString()</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract String parameterString();

    /**
     * Original signature : <code>String* query()</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract String query();

    /**
     * Original signature : <code>String* relativePath()</code><br>
     * The same as path if baseURL is nil<br>
     * <i>native declaration : :55</i>
     */
    public abstract String relativePath();

    /**
     * Original signature : <code>BOOL isFileURL()</code><br>
     * Whether the scheme is file:; if [myURL isFileURL] is YES, then [myURL path] is suitable for input into NSFileManager or NSPathUtilities.<br>
     * <i>native declaration : :57</i>
     */
    public abstract boolean isFileURL();

    /**
     * Original signature : <code>NSURL* standardizedURL()</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract NSURL standardizedURL();

    /**
     * Original signature : <code>NSData* resourceDataUsingCache(BOOL)</code><br>
     * Blocks to load the data if necessary.  If shouldUseCache is YES, then if an equivalent URL has already been loaded and cached, its resource data will be returned immediately.  If shouldUseCache is NO, a new load will be started<br>
     * <i>from NSURLLoading native declaration : :84</i>
     */
    public abstract NSData resourceDataUsingCache(boolean shouldUseCache);
    /**
     * <i>from NSURLLoading native declaration : :85</i><br>
     * Conversion Error : /// Original signature : <code>void loadResourceDataNotifyingClient(null, BOOL)</code><br>
     * - (void)loadResourceDataNotifyingClient:(null)client usingCache:(BOOL)shouldUseCache; // Starts an asynchronous load of the data, registering delegate to receive notification.  Only one such background load can proceed at a time.<br>
     *  (Argument client cannot be converted)
     */
    /**
     * Original signature : <code>propertyForKey(String*)</code><br>
     * <i>from NSURLLoading native declaration : :86</i>
     */
    public abstract NSObject propertyForKey(String propertyKey);

    /**
     * These attempt to write the given arguments for the resource specified by the URL; they return success or failure<br>
     * Original signature : <code>BOOL setResourceData(NSData*)</code><br>
     * <i>from NSURLLoading native declaration : :89</i>
     */
    public abstract boolean setResourceData(NSData data);
}
