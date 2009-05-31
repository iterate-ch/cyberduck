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

/// <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSEnumerator.h:27</i>
public interface NSFastEnumeration extends NSObject {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("NSFastEnumeration", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSFastEnumeration alloc();
    }

    /**
     * Original signature : <code>NSUInteger countByEnumeratingWithState(NSFastEnumerationState*, id*, NSUInteger)</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSEnumerator.h:29</i>
     */
    int NSFastEnumeration_countByEnumeratingWithState_objects_count(NSFastEnumerationState state, NSObject stackbuf, int len);

    public static class NSFastEnumerationState extends com.sun.jna.Structure {
        /// Allocate a new NSFastEnumerationState struct on the heap
        public NSFastEnumerationState() {
        }

        /// Cast data at given memory location (pointer + offset) as an existing NSFastEnumerationState struct
        public NSFastEnumerationState(com.sun.jna.Pointer pointer, int offset) {
            super();
            useMemory(pointer, offset);
            read();
        }

        /// Create an instance that shares its memory with another NSFastEnumerationState instance
        public NSFastEnumerationState(NSFastEnumerationState struct) {
            this(struct.getPointer(), 0);
        }

        public static class ByReference extends NSFastEnumerationState implements com.sun.jna.Structure.ByReference {
            /// Allocate a new NSFastEnumerationState.ByRef struct on the heap
            public ByReference() {
            }

            /// Create an instance that shares its memory with another NSFastEnumerationState instance
            public ByReference(NSFastEnumerationState struct) {
                super(struct.getPointer(), 0);
            }
        }

        public static class ByValue extends NSFastEnumerationState implements com.sun.jna.Structure.ByValue {
            /// Allocate a new NSFastEnumerationState.ByVal struct on the heap
            public ByValue() {
            }

            /// Create an instance that shares its memory with another NSFastEnumerationState instance
            public ByValue(NSFastEnumerationState struct) {
                super(struct.getPointer(), 0);
            }
        }

        public com.sun.jna.NativeLong state;
        public NSObject itemsPtr;
        public com.sun.jna.ptr.NativeLongByReference mutationsPtr;
        public com.sun.jna.NativeLong[] extra = new com.sun.jna.NativeLong[(5)];
    }
}
