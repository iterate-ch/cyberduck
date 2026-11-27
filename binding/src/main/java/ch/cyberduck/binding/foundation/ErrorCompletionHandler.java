package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

/**
 * Wraps an Objective-C completion handler block of type <code>void (^)(NSError *)</code> passed to a delegate
 * callback. Rococoa has no support for blocks, so the block is invoked directly using the stable block ABI where
 * the invoke function pointer is stored after the isa pointer and the flags and reserved integers.
 * <p>
 * The block is copied on creation to allow invoking it after the delegate callback has returned and is released
 * after it has been invoked once.
 */
public final class ErrorCompletionHandler {
    private static final Logger log = LogManager.getLogger(ErrorCompletionHandler.class);

    private static final NativeLibrary system = NativeLibrary.getInstance("System");
    private static final Function copy = system.getFunction("_Block_copy");
    private static final Function release = system.getFunction("_Block_release");

    /**
     * Offset of invoke function pointer in struct Block_layout { void *isa; int flags; int reserved; void (*invoke)(void *, ...); }
     */
    private static final long BLOCK_INVOKE_OFFSET = Native.POINTER_SIZE + 4 + 4;

    /**
     * NSFileWriteUnknownError
     */
    private static final NSInteger NSFileWriteUnknownError = new NSInteger(512);

    private static final _Class NSErrorClass = Rococoa.createClass("NSError", _Class.class);

    public interface _Class extends ObjCClass {
        org.rococoa.cocoa.foundation.NSError errorWithDomain_code_userInfo(String domain, NSInteger code, NSDictionary userInfo);
    }

    private final Pointer block;
    private final AtomicBoolean invoked = new AtomicBoolean();

    /**
     * @param block Completion handler block passed to delegate callback
     */
    public ErrorCompletionHandler(final ID block) {
        this.block = copy.invokePointer(new Object[]{new Pointer(block.longValue())});
    }

    /**
     * Invoke completion handler with no error
     */
    public void success() {
        this.invoke(Pointer.NULL);
    }

    /**
     * Invoke completion handler with error
     *
     * @param description Localized description of failure
     */
    public void failure(final String description) {
        final org.rococoa.cocoa.foundation.NSError error = NSErrorClass.errorWithDomain_code_userInfo("NSCocoaErrorDomain", NSFileWriteUnknownError,
                NSDictionary.dictionaryWithObjectsForKeys(
                        NSArray.arrayWithObject(description),
                        NSArray.arrayWithObject("NSLocalizedDescription")));
        this.invoke(new Pointer(error.id().longValue()));
    }

    private void invoke(final Pointer error) {
        if(!invoked.compareAndSet(false, true)) {
            log.warn("Skip invoking completion handler {} more than once", block);
            return;
        }
        log.debug("Invoke completion handler {} with error {}", block, error);
        try {
            Function.getFunction(block.getPointer(BLOCK_INVOKE_OFFSET)).invokeVoid(new Object[]{block, error});
        }
        finally {
            release.invokeVoid(new Object[]{block});
        }
    }
}
