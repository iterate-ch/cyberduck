//
//  ChainedOutputStream.h
//  Keychain
//
//  Created by Wade Tregaskis on 29/6/2005.
//
//  Copyright (c) 2005, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>


/*! @header ChainedOutputStream
    @abstract Defines the ChainedOutputStream class and key-value codings.
    @discussion See the documentation for each individual element of this header for more information. */


/*! @class ChainedOutputStream
    @abstract An NSOutputStream subclass which can be chained to another NSOutputStream.
    @discussion You cannot cleanly subclass NSOutputStream.  This class re-implements it in such a way that you can, and more so - such that you can "chain" multiple ChainedOutputStream's together, such that the output of one is passed automatically to another. */

@interface ChainedOutputStream : NSOutputStream {
    NSOutputStream *stream; /* The next stream in the chain, to which we pass on any data we receive (may be nil). */
    BOOL outputIsExplicit; /* If YES then output really is an NSOutputStream according to our user(s), so we cannot bind ourselves to it transparently. */
    int err; /* Records the last result from any CSSM function. */
}

/* Inherited from NSStream. */

/*! @method open
    @abstract Opens the ChainedOutputStream and makes it ready to receive data.
    @discussion A stream <b>must</b> be opened (via this method) before it can be used.

                Note that the result of trying to re-open a stream after previously closing it is undefined, and dependant not only on this implementation but also Apple's underlying NSOutputStream (and NSStream).  Best not to try it. */

- (void)open;

/*! @method close
    @abstract Closes the ChainedOutputStream.
    @discussion Once you are finished with the stream you should close it, to allow it to immediately clean up any internal storage or other items.  Once closed the stream should not be reused (the results of trying to do so are undefined). */

- (void)close;

/*! @method delegate
    @abstract Returns the receiver's delegate.
    @discussion The delegate is called whenever any asynchronous events occur on the stream, such as an error.  It may be nil, in which case no external notifications will be provided.

                Note that the receiver never retains the delegate.
    @result Returns the receiver's delegate, which may be nil. */

- (id)delegate;

/*! @method setDelegate:
    @abstract Sets the receiver's delegate.
    @discussion The delegate is called whenever any asynchronous events occur on the stream, such as an error.  It may be nil, in which case no external notifications will be provided.

                Note that the receiver never retains the delegate - it is the caller's responsibility to ensure the given delegate exists for the entire life of the receiver.
    @param newDelegate The new delegate to set.  May be nil. */

- (void)setDelegate:(id)newDelegate;

/*! @method scheduleInRunLoop:forMode:
    @abstract Schedules the receiver in a particular runloop.
    @discussion From Apple's NSStream documentation: "Schedules the receiver on aRunLoop using the specified mode. Unless the client is polling the stream, it is responsible for ensuring that the stream is scheduled on at least one run loop and that at least one of the run loops on which the stream is scheduled is being run."

                While you should not rely on the behaviour, at time of writing ChainedOutputStream does not utilise runloops for any of it's particular operations.  However, it's superclasses (NSOutputStream & NSStream) may do so - see the relevant documentation for more details.
    @param aRunLoop The runloop to schedule the receiver in.  Should not be nil.
    @param mode The mode to schedule the receiver with [on the given runloop].  Should not be nil. */

- (void)scheduleInRunLoop:(NSRunLoop*)aRunLoop forMode:(NSString*)mode;

/*! @method removeFromRunLoop:forMode:
    @abstract Removes the receiver from a particular runloop.
    @discussion See Apple's documentation for NSOutputStream & NSStream for further details on this method.
    @param aRunLoop The runloop to remove the receiver from.  Should not be nil.
    @param mode The mode to remove the receiver from [on the given runloop].  Should not be nil. */

- (void)removeFromRunLoop:(NSRunLoop*)aRunLoop forMode:(NSString*)mode;

/*! @method propertyForKey:
    @abstract Returns the value of the receiver's named property.
    @discussion See Apple's documentation of key-value coding for more information.
    @param key The key who's value you are interested in.
    @result The resulting value, which may be nil (notably in the case in which the given key is unrecognised). */

- (id)propertyForKey:(NSString*)key;

/*! @method setProperty:forKey:
    @abstract Attempts to set a given value for a given key of the receiver.
    @discussion See Apple's documentation on key-value coding for more information.
    @param property The value to set.  May or may not (key-dependent) be nil, or otherwise have to subscribe to certain conventions.  Should be of an appropriate class for the given key.
    @param key Key indicating which of the receiver's properties to set.
    @result Returns YES if the given property was modified with the given value, NO otherwise (including the cases where the given property does not exist). */

- (BOOL)setProperty:(id)property forKey:(NSString*)key;

/*! @method streamError
    @abstract Returns an NSError representing the current error state of the stream.
    @discussion Note that this will probably always return an NSError instance, even if there is no error.  You should use the streamStatus method to determine if there actually is an error state, at which point you can call this method for more information on what the error is.

                Refer to the documentation for NSOutputStream & NSStream for more information.
    @result Returns an NSError instance containing information about the receiver's current error state. */

- (NSError*)streamError;

/*! @method streamStatus
    @abstract Returns the status of the receiver - e.g. opening, open, closing, errored, etc.
    @discussion You can use the status result to determine what state the stream is - e.g. to determine if it is open and ready for more data, or if an error has occured.

                Refer to the documentation for NSOutputStream & NSStream for more information.
    @result Returns the stream's current state. */

- (NSStreamStatus)streamStatus;

/* Inherited from NSOutputStream. */

/*! @method outputStreamToMemory
    @abstract Returns a ChainedOutputStream that writes into it's own internal memory buffer.
    @discussion Works the same as NSOutputStream's version.
    @result Returns a new ChainedOutputStream that writes into it's own internal memory buffer, or nil if an error occurs. */

+ (id)outputStreamToMemory;

/*! @method outputStreamToBuffer:capacity:
    @abstract Returns a ChainedOutputStream that writes into a user-provided buffer.
    @discussion Works the same as NSOutputStream's version
    @param buffer The user-provided buffer to write data into.  Should not be NULL.
    @param capacity The size of 'buffer'.  At most this many bytes will be written into the buffer.
    @result Returns a new ChainedOutputStream that writes into the user-provided buffer, or nil if an error occurs. */

+ (id)outputStreamToBuffer:(uint8_t*)buffer capacity:(unsigned int)capacity;

/*! @method outputStreamToFileAtPath:append:
    @abstract Returns a ChainedOutputStream that writes to a named file.
    @discussion Works the same as NSOutputStream's version.
    @param path A path indicating where to write to.  Should not be nil.
    @param shouldAppend If YES any existing file will be appended to.  If NO, any existing file will be removed.  If no file already exists, has no effect [obviously].
    @result Returns a new ChainedOutputStream that writes to the named, or nil if an error occurs. */

+ (id)outputStreamToFileAtPath:(NSString*)path append:(BOOL)shouldAppend;

/*! @method outputStreamToOutputStream:
    @abstract Returns a ChainedOutputStream that writes to another NSOutputStream.
    @discussion This method may be used to chain multiple ChainedOutputStream's together, so that the results from one go directly to another without any additional glue code.  Note that you may connect this class to a non-ChainedOutputStream NSOutputStream, but of course you may not be able to extend the chain any further, since the chaining behaviour is provided by ChainedOutputStream, not NSOutputStream.

                When this is used, any data provided to the ChainedOutputStream via the write:maxLength: method will be passed on to the stream (if any) provided in this class constructor.  If the stream indicates it is full, the ChainedOutputStream before it similarly will indicate it is full, and so on.  All intuitive behaviour.

                Note that the argument can be nil, in which the ChainedOutputStream acts as a destructive sink for any data given to it - kinda pointless as is, but useful with the additional behaviour of certain subclasses (e.g. DigestOutputStream).
    @param otherStream The other NSOutputStream to send data to when it arrives at the ChainedOutputStream.  May be nil.
    @result Returns a new ChainedOutputStream that writes to the given NSOutputStream (if any), or nil if an error occurs. */

+ (id)outputStreamToOutputStream:(NSOutputStream*)otherStream;

/*! @method initToMemory
    @abstract Initialises the receiver to write received data to an internal buffer.
    @discussion Works the same as NSOutputStream's version.
    @result Returns a ChainedOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToMemory;

/*! @method initToBuffer:capacity:
    @abstract Initialises the receiver to write received data to a user-provided buffer.
    @discussion Works the same as NSOutputStream's version.
    @result Returns a ChainedOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToBuffer:(uint8_t*)buffer capacity:(unsigned int)capacity;

/*! @method initToFileAtPath:append:
    @abstract Initialises the receiver to write received data to a file.
    @discussion Works the same as NSOutputStream's version.
    @result Returns a ChainedOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToFileAtPath:(NSString*)path append:(BOOL)shouldAppend;

/*! @method initToOutputStream:
    @abstract Initialises the receiver to write received data to another NSOutputStream.
    @discussion This initialiser may be used to chain multiple ChainedOutputStream's together, so that the results from one go directly to another without any additional glue code.  Note that you may connect the receiver to a non-ChainedOutputStream NSOutputStream, but of course you may not be able to extend the chain any further, since the chaining behaviour is provided by ChainedOutputStream, not NSOutputStream.

                When this is used, any data provided to the receiver via the write:maxLength: method will be passed on to the stream (if any) provided.  If the stream indicates it is full, the receiver will indicate it is full, as will the previous ChainedOutputStream, and so on.  All intuitive behaviour.

                Note that the argument can be nil, in which the receiver acts as a destructive sink for any data given to it - kinda pointless as is, but useful with the additional behaviour of certain subclasses (e.g. DigestOutputStream).
    @param otherStream The other NSOutputStream to send data to when it arrives at the ChainedOutputStream.  May be nil.
    @result Returns a ChainedOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToOutputStream:(NSOutputStream*)otherStream;

/*! @method write:maxLength:
    @abstract Writes data to the stream.
    @discussion This is the method used to input data to the stream.  The data may be written into memory, a file, or another NSOutputStream, as determined by how the receiver was initialised.
    @param buffer A buffer containing the data to write.  Should not be NULL.
    @param len The maximum length of valid data in 'buffer'.  At most this many bytes will be written.
    @result Returns the number of bytes written, which will always be less than or equal to 'len'.  If the stream is full it will return 0.  If an error occurs, -1 will be returned, and you will be able to obtain more information using the receivers streamError method. */

- (int)write:(const uint8_t*)buffer maxLength:(unsigned int)len;

/*! @method hasSpaceAvailable
    @abstract Returns whether or not the receiver has room for additional data.
    @discussion Works the same as NSOutputStream's version.  Note that if the output of the receiver is another NSOutputStream, it will be consulted to determine if it is full (using hasSpaceAvailable again), in which case YES will be automatically returned by the receiver as well (to prevent awkward situations where the start of a chain is happy to accept any data at any time, but some element later in the chain is unable to do likewise).

                Note that if the next NSOutputStream is nil (i.e. the receiver is a destructive sink) this method will always return YES.
    @result Returns YES if the receiver (and all successive NSOutputStreams, if any) has room for additional data, NO otherwise. */

- (BOOL)hasSpaceAvailable;

/* Our additions. */

/*! @method destination
    @abstract Returns the next NSOutputStream in the chain, if any, for the receiver.
    @discussion Returns the 'otherStream' previously provided to the outputStreamToOutputStream: class constructor or initToOutputStream initialiser.  If the receiver was not created using these methods the result is undefined.
    @result See discussion. */

- (NSOutputStream*)destination;

@end
