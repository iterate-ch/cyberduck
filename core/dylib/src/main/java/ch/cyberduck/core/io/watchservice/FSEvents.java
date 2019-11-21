package ch.cyberduck.core.io.watchservice;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.CFAllocatorRef;
import ch.cyberduck.binding.foundation.CFArrayRef;
import ch.cyberduck.binding.foundation.CFIndex;
import ch.cyberduck.binding.foundation.CFRunLoopRef;
import ch.cyberduck.binding.foundation.CFStringRef;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface FSEvents extends Library {
    FSEvents library = Native.load("Carbon", FSEvents.class);

    CFArrayRef CFArrayCreate(
        CFAllocatorRef allocator, // always set to Pointer.NULL
        Pointer[] values,
        CFIndex numValues,
        Void callBacks // always set to Pointer.NULL
    );

    CFStringRef CFStringCreateWithCharacters(
        Void alloc, //  always pass NULL
        char[] chars,
        CFIndex numChars
    );

    FSEventStreamRef FSEventStreamCreate(
        Pointer v, // always use Pointer.NULL
        FSEventStreamCallback callback,
        Pointer context,  // always use Pointer.NULL
        CFArrayRef pathsToWatch,
        long sinceWhen, // use -1 for events since now
        double latency, // in seconds
        int flags // 0 is good for now

    );

    boolean FSEventStreamStart(FSEventStreamRef streamRef);

    void FSEventStreamStop(FSEventStreamRef streamRef);

    void FSEventStreamInvalidate(FSEventStreamRef streamRef);

    void FSEventStreamRelease(FSEventStreamRef streamRef);

    void FSEventStreamScheduleWithRunLoop(FSEventStreamRef streamRef, CFRunLoopRef runLoop, CFStringRef runLoopMode);

    void FSEventStreamUnscheduleFromRunLoop(FSEventStreamRef streamRef, CFRunLoopRef runLoop, CFStringRef runLoopMode);

    CFRunLoopRef CFRunLoopGetCurrent();

    void CFRunLoopRun();

    void CFRunLoopStop(CFRunLoopRef rl);

    interface FSEventStreamCallback extends Callback {
        void invoke(FSEventStreamRef streamRef, Pointer clientCallBackInfo, NativeLong numEvents, Pointer eventPaths, Pointer eventFlags, Pointer eventIds);
    }
}
