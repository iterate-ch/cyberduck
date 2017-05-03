package ch.cyberduck.binding.foundation;

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
import org.rococoa.cocoa.foundation.NSData;
import org.rococoa.cocoa.foundation.NSInteger;

/// <i>native declaration : :14</i>
public abstract class NSUserDefaults extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSUserDefaults", _Class.class);

    public static NSUserDefaults standardUserDefaults() {
        return CLASS.standardUserDefaults();
    }

    public static NSUserDefaults sharedUserDefaults(String group) {
        return CLASS.alloc().initWithSuiteName(group);
    }

    public static void resetStandardUserDefaults() {
        CLASS.resetStandardUserDefaults();
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSUserDefaults* standardUserDefaults()</code><br>
         * <i>native declaration : :20</i>
         */
        NSUserDefaults standardUserDefaults();

        /**
         * Original signature : <code>void resetStandardUserDefaults()</code><br>
         * <i>native declaration : :21</i>
         */
        void resetStandardUserDefaults();

        NSUserDefaults alloc();
    }

    /**
     * Original signature : <code>id init()</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract NSUserDefaults init();

    /**
     * Original signature : <code>id initWithUser(NSString*)</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract NSUserDefaults initWithUser(String username);

    /**
     * Returns an NSUserDefaults object initialized with the defaults for the specified app group.
     */
    public abstract NSUserDefaults initWithSuiteName(String group);

    /**
     * Original signature : <code>id objectForKey(NSString*)</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract NSObject objectForKey(String defaultName);

    public void setObjectForKey(NSObject value, String defaultName) {
        this.setObject_forKey(value, defaultName);
    }

    /**
     * Original signature : <code>void setObject(id, NSString*)</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract void setObject_forKey(NSObject value, String defaultName);

    /**
     * Original signature : <code>void removeObjectForKey(NSString*)</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract void removeObjectForKey(String defaultName);

    /**
     * Original signature : <code>NSString* stringForKey(NSString*)</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract String stringForKey(String defaultName);

    /**
     * Original signature : <code>NSArray* arrayForKey(NSString*)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract NSArray arrayForKey(String defaultName);

    /**
     * Original signature : <code>NSDictionary* dictionaryForKey(NSString*)</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract NSDictionary dictionaryForKey(String defaultName);

    /**
     * Original signature : <code>NSData* dataForKey(NSString*)</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract NSData dataForKey(String defaultName);

    /**
     * Original signature : <code>NSArray* stringArrayForKey(NSString*)</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract NSArray stringArrayForKey(String defaultName);

    /**
     * Original signature : <code>NSInteger integerForKey(NSString*)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract NSInteger integerForKey(String defaultName);

    /**
     * Original signature : <code>float floatForKey(NSString*)</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract float floatForKey(String defaultName);

    /**
     * Original signature : <code>double doubleForKey(NSString*)</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract double doubleForKey(String defaultName);

    /**
     * Original signature : <code>BOOL boolForKey(NSString*)</code><br>
     * <i>native declaration : :38</i>
     */
    public abstract boolean boolForKey(String defaultName);

    /**
     * Original signature : <code>void setInteger(NSInteger, NSString*)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void setInteger_forKey(NSInteger value, String defaultName);

    /**
     * Original signature : <code>void setFloat(float, NSString*)</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract void setFloat_forKey(float value, String defaultName);

    /**
     * Original signature : <code>void setDouble(double, NSString*)</code><br>
     * <i>native declaration : :42</i>
     */
    public abstract void setDouble_forKey(double value, String defaultName);

    /**
     * Original signature : <code>void setBool(BOOL, NSString*)</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract void setBool_forKey(boolean value, String defaultName);

    /**
     * Original signature : <code>void registerDefaults(NSDictionary*)</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract void registerDefaults(NSDictionary registrationDictionary);

    /**
     * Original signature : <code>void addSuiteNamed(NSString*)</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract void addSuiteNamed(String suiteName);

    /**
     * Original signature : <code>void removeSuiteNamed(NSString*)</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract void removeSuiteNamed(String suiteName);

    /**
     * Original signature : <code>NSDictionary* dictionaryRepresentation()</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract NSDictionary dictionaryRepresentation();

    /**
     * Original signature : <code>NSArray* volatileDomainNames()</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract NSArray volatileDomainNames();

    /**
     * Original signature : <code>NSDictionary* volatileDomainForName(NSString*)</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract NSDictionary volatileDomainForName(String domainName);

    /**
     * Original signature : <code>void setVolatileDomain(NSDictionary*, NSString*)</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract void setVolatileDomain_forName(NSDictionary domain, String domainName);

    /**
     * Original signature : <code>void removeVolatileDomainForName(NSString*)</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract void removeVolatileDomainForName(String domainName);

    /**
     * Original signature : <code>NSArray* persistentDomainNames()</code><br>
     * <i>native declaration : :57</i>
     */
    public abstract NSArray persistentDomainNames();

    /**
     * Original signature : <code>NSDictionary* persistentDomainForName(NSString*)</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract NSDictionary persistentDomainForName(String domainName);

    /**
     * Original signature : <code>void setPersistentDomain(NSDictionary*, NSString*)</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract void setPersistentDomain_forName(NSDictionary domain, String domainName);

    /**
     * Original signature : <code>void removePersistentDomainForName(NSString*)</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract void removePersistentDomainForName(String domainName);

    /**
     * Original signature : <code>BOOL synchronize()</code><br>
     * <i>native declaration : :62</i>
     */
    public abstract boolean synchronize();

    /**
     * Original signature : <code>BOOL objectIsForcedForKey(NSString*)</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract boolean objectIsForcedForKey(String key);

    /**
     * Original signature : <code>BOOL objectIsForcedForKey(NSString*, NSString*)</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract boolean objectIsForcedForKey_inDomain(String key, String domain);
}
