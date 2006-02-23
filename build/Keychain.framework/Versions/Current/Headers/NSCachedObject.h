//
//  NSCachedObject.h
//  Keychain
//
//  Created by Wade Tregaskis on Sun Feb 16 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>


/*! @class NSCachedObject
    @abstract An extension of NSObject which allows for caching of subclass instances, such that only one unique instance of the subclass exists for any particular key object.
    @discussion This class provides a generic mechanism for ensuring uniqueness of subclasses which inherit from it.  It maintains a list of cached objects, which can be queried when creating new objects to ensure duplicates don't spring into being.

                Note that this class has been rewritten since it's first conception, and is now both thread-safe and [relatively] efficient, compared with the original method.  Performance is no largely subclass-independent, so one class which has many instances won't effect the perform of other subclasses.  Performance within each subclass, however, is sadly still linear - don't try scaling this up too far.

                Please also note that I'm well aware of the NS prefix being an 'official', Apple-reserved one.  I have two answers to this.  First, I'm too lazy to change all the references to it given I have too much other stuff to do as it is.  Secondly, I believe this class (or at least something with the same functionality) should be part of the Foundation framework, alongside NSObject.  So I'm just using the NS prefix now to make it easier for Apple to copy and paste it in. ;)

                If someone from Apple takes offense to me using the prefix, they can contact me and I'll change it.  Otherwise, things will be left well enough alone. */

@interface NSCachedObject : NSObject {}

/*! @method instanceWithKey:from:simpleKey:
    @abstract Return an existing instance (if any) by matching the provided key with that returned by the provided selector.
    @discussion This method looks for an existing instance [of the same class as the receiver] which returns an identical key from the selector provided.  If the simpleKey parameter is YES, keys are compared purely as memory addresses, despite being id's.  This allows for the use of keys which are simply pointers or integers - both can be cast to id's, which are themselves simply void pointers.

                If the simpleKey parameter is NO, keys are compared using isEqual.  Obviously this only works if the key actually is an object of some kind, inheriting from NSObject, or defining it's own isEqual method.

                If you need to use a more comprehensive comparison between keys, take a look at instanceForSelector:with.
    @param key The key to look for.  If you're looking at creating a new instance, this key would be the same key you plan to give to your new instance.
    @param selector A selector which, when applied to a cached object, returns a key object.  Note that if an object in the cache does not respond to the provided selector it is simply ignored - such a case is not considered an error.
    @param simpleKey If YES, keys are compared based solely on their address.  If NO, isEqual is used.
    @result If an existing instance exists which returns a matching key, it is returned.  Otherwise, or in case of error, nil is returned. */

+ (id)instanceWithKey:(id)key from:(SEL)selector simpleKey:(BOOL)simpleKey;

/*! @method instanceForSelector:with:
    @abstract Returns an existing instance (if any) by querying existing instances with the provided key.
    @discussion This method works in a similar way to instanceWithKey:from:simpleKey:, except that instead of using the provided selector to return an instance's key, it uses the selector to pass the key to an object, which can then return YES in the case of a match, or NO otherwise.  This allows you to define your own methods for comparing keys, rather than using isEqual or address comparison.

                Note that if multiple instances will match a given key, only the first one found will be returned.  At present, instances are checked in chronological order, starting with the oldest.  This is not defined behavior, however, and may change in future.
    @param selector A selector which, when applied to a cached object along with the supplied key, returns a BOOL specifying whether that instance matches for that key.
    @param key A key.  This may be a real object, or an integer/pointer cast to 'id' (i.e. a void pointer).
    @result Returns any existing instance which returns YES from the selector, otherwise - or in case of an error - returns nil. */

+ (id)instanceForSelector:(SEL)selector with:(id)key;

/*! @method init
    @abstract Adds the receiver to the global object cache.
    @discussion When called this method creates the global object cache, if it doesn't already exist, and adds the receiver to it.  Consequently, you should always call this method for each new instance you create (using [super init] in your subclass).  Otherwise the global object cache will be missing entries and may not function as expected.

                Note that this does not check if the receiver is already in the cache.  You shouldn't be calling init on an already initialized instance anyway, and this method can't magically figure out what key the receiver has.  Consequently, you must check for yourself if an instance for a given key already exists, prior to calling this method on your new instance.  If you try checking after calling this method, you will obviously match the instance you just initialized, and the whole system breaks down.
    @result The receiver, or nil if an error occurs. */

- (id)init;

/*! @method dealloc
    @abstract Removes the receiver from the global object cache.
    @discussion This should automatically be called when your object dies, so you wouldn't normally worry about it.  It makes sure the receiver is removed from any object caches, if it is in any.  If it didn't do this, you'd very quickly crash with lots of random memory errors, due the dangling pointers that would be lying around. */

- (void)dealloc;

@end
