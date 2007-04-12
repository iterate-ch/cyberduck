//
//  MultiThreading.h
//  Keychain
//
//  Created by Wade Tregaskis on Mon May 26 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>

/*! @header MultiThreading
    @abstract Defines classes & other functionality related to thread safety in the framework.
    @discussion While the Keychain framework doesn't explicitly try to be thread safe in all possible uses, it does offer some limited thread safety for those who need it.  This header contains classes and other items which can be used to control some aspects of thread safety. */


/*! @class KeychainThreadController
    @abstract A generic singleton class for managing threading issues in the Keychain framework.
    @discussion This class provides methods to activate and deactivate thread safety in the Keychain framework.  Thread safety is not provided automatically - you must use this class to activate it.  Once activated, this class will take appropriate steps to ensure it is enabled where necessary. */

@interface KeychainThreadController : NSObject {}

/*! @method defaultController
    @abstract Returns the default KeychainThreadController instance.
    @discussion The KeychainThreadController class is designed as a singleton - that is, there is only ever one instance.  You should never alloc/init one manually - always use this class method to obtain the single default controller.
    @result Returns the default KeychainThreadController, or nil if an error occurs. */

+ (KeychainThreadController*)defaultController;

/*! @method activateThreadSafety
    @abstract Activates thread safety within the Keychain framework.
    @discussion Note that the entire Keychain framework is not thread safe.  Accessing any instance of any class from two threads simultaneously is just asking for trouble.  What thread safety is provided, however, is with regards to NSCachedObject and it's manipulation of internal storage.  Without thread safety, to threads requesting the same instance from any classes initialiser may end up with two different instances, rather than sharing the same one.  This has greater concerns, too - only one instance will ultimately be remembered in the cache, so at least one other instance will become orphaned, and may consequently leak.

                This method provides thread safety after it is called.  It is safe and perfectly efficient to call this method long before your application actually becomes multi-threaded - if it does at all.  If the application is not currently multithreaded the receiver will not immediately enable thread-safety mechanisms, but rather will register for the NSWillBecomeMultiThreadedNotification notification.  When that notification is received (if ever), then steps are taken as appropriate. */

- (void)activateThreadSafety;

/*! @method deactivateThreadSafety
    @abstract Deactivates thread safety within the Keychain framework.
    @discussion (see the discussion for activateThreadSafety for details as to what "thread safety" actually means in this context)

                You generally shouldn't call this method, unless you are absolutely sure you no longer require thread safety. */

- (void)deactivateThreadSafety;

@end