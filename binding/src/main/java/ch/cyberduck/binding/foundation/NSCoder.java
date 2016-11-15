package ch.cyberduck.binding.foundation;

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

import org.rococoa.cocoa.foundation.NSInteger;

import com.sun.jna.Pointer;

public abstract class NSCoder extends NSObject {
    /**
     * Original signature : <code>-(void)encodeValueOfObjCType:(const char*) at:(const void*)</code><br>
     * <i>native declaration : NSCoder.h:12</i>
     */
    public abstract void encodeValueOfObjCType_at(String type, Pointer addr);

    /**
     * Original signature : <code>-(void)encodeDataObject:(NSData*)</code><br>
     * <i>native declaration : NSCoder.h:13</i>
     */
    public abstract void encodeDataObject(NSData data);

    /**
     * Original signature : <code>-(void)decodeValueOfObjCType:(const char*) at:(void*)</code><br>
     * <i>native declaration : NSCoder.h:14</i>
     */
    public abstract void decodeValueOfObjCType_at(String type, Pointer data);

    /**
     * Original signature : <code>-(NSData*)decodeDataObject</code><br>
     * <i>native declaration : NSCoder.h:15</i>
     */
    public abstract NSData decodeDataObject();

    /**
     * Original signature : <code>-(NSInteger)versionForClassName:(NSString*)</code><br>
     * <i>native declaration : NSCoder.h:16</i>
     */
    public abstract NSInteger versionForClassName(NSString className);
    /// <i>native declaration : NSCoder.h</i>
}
