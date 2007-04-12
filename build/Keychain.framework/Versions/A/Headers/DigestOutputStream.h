//
//  DigestOutputStream.h
//  Keychain
//
//  Created by Wade Tregaskis on 23/5/2005.
//
//  Copyright (c) 2005, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/Security.h>

#import <Keychain/ChainedOutputStream.h>
#import <Keychain/CSSMModule.h>


/*! @header DigestOutputStream
    @abstract Defines the DigestOutputStream class and key-value codings.
    @discussion See the documentation for each individual element of this header for more information. */


/*! @constant DigestOutputStreamAlgorithm
    @abstract Key-value coding key for retrieving/setting the DigestOutputStream's algorithm.
    @discussion When used with DigestOutputStream's propertyForKey: and setProperty:forKey: methods, manipulates an NSNumber containing the CDSA algorithm number used by the DigestOutputStream instance.

                Note: The algorithm can only be modified when the DigestOutputStream is closed - i.e. prior to calling open on it. */

extern NSString *DigestOutputStreamAlgorithm;

/*! @constant DigestOutputStreamCurrentDigestValue
    @abstract Key-value coding key for retrieving the DigestOutputStream's current digest value.
    @discussion Returns the same NSData instance available via the currentDigestValue method.  This value is not settable. */

extern NSString *DigestOutputStreamCurrentDigestValue;


/*! @class DigestOutputStream
    @abstract An NSOutputStream subclass which calculates the digest of data passing through it.
    @discussion When you are digesting large amounts of data, or data that is only available at a slow pace (e.g. over a network connection), it is a much better idea to use this stream to calculate the digest, than trying to load all the data into a single NSData and using the digestUsingAlgorithm: category method.  You can also tie the DigestOutputStream to another NSOutputStream, allowing you to chain together a number of operations - e.g. you might tie a DigestOutputStream to an EncryptOutputStream, which then goes to an NSOutputStream representing a file or socket.

                Note that this subclass fully supports all NSOutputStream functionality intrinsically - i.e. outputting to memory, a user-allocated buffer or a file. */

@interface DigestOutputStream : ChainedOutputStream {
    CSSMModule *_CSPModule;
    
    CSSM_CC_HANDLE ccHandle; /* Our digest session handle. */
    CSSM_ALGORITHMS algorithm; /* Our digest algorithm. */
}

/* Inherited from NSStream. */

/*! @method open
    @abstract Opens the DigestOutputStream and makes it ready to receive data.
    @discussion A stream <b>must</b> be opened (via this method) before it can be used.  In the case of DigestOutputStream, make sure you set the desired digest algorithm prior to calling open - you cannot change it once the stream is opened.

                Note that the result of trying to re-open a stream after previously closing it is undefined, and dependant not only on this implementation but also Apple's underlying NSOutputStream (and NSStream).  Best not to try it. */

- (void)open;

/*! @method close
    @abstract Closes the DigestOutputStream.
    @discussion Once you are finished with the stream you should close it, to allow it to immediately clean up any internal storage or other items.  Once closed the stream should not be reused (the results of trying to do so are undefined). */

- (void)close;

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

/* Inherited from NSOutputStream. */

/*! @method initToMemory
    @abstract Initialises the receiver to write received data to an internal buffer.
    @discussion Works the same as NSOutputStream's version, with the exception that it initialises the additional cryptographic functionality of the receiver.

                Note that the default digest algorithm set by this method is not defined and should not be relied upon.  Always use setAlgorithm: on the receiver before opening it.
    @result Returns a DigestOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToMemory;

/*! @method initToBuffer:capacity:
    @abstract Initialises the receiver to write received data to a user-provided buffer.
    @discussion Works the same as NSOutputStream's version, with the exception that it initialises the additional cryptographic functionality of the receiver.

                Note that the default digest algorithm set by this method is not defined and should not be relied upon.  Always use setAlgorithm: on the receiver before opening it.
    @result Returns a DigestOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToBuffer:(uint8_t*)buffer capacity:(unsigned int)capacity;

/*! @method initToFileAtPath:append:
    @abstract Initialises the receiver to write received data to a file.
    @discussion Works the same as NSOutputStream's version, with the exception that it initialises the additional cryptographic functionality of the receiver.

                Note that the default digest algorithm set by this method is not defined and should not be relied upon.  Always use setAlgorithm: on the receiver before opening it.
    @result Returns a DigestOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToFileAtPath:(NSString*)path append:(BOOL)shouldAppend;

/*! @method initToOutputStream:
    @abstract Initialises the receiver to write received data to another NSOutputStream.
    @discussion This initialiser may be used to chain multiple NSOutputStream's together, so that the results from one go directly to another without any additional glue code.

                When this is used, any data provided to the receiver via the write:maxLength: method will be passed on to the stream (if any) provided.  If the stream indicates it is full, the receiver similarly will indicate it is full, and so on.  All intuitive behaviour.

                Note that the argument can be nil, in which the receiver acts as a destructive sink for any data given to it - that is, the data is used to calculate the digest value, but is not then saved anywhere.  Very useful in a general sense for computing digestes.
    @param otherStream The other NSOutputStream to send data to when it arrives at the DigestOutputStream.  May be nil.
    @result Returns a DigestOutputStream (possibly the receiver) suitably initialised, or nil if an error occurs. */

- (id)initToOutputStream:(NSOutputStream*)otherStream;

/*! @method write:maxLength:
    @abstract Writes data to the stream.
    @discussion This is the method used to input data to the stream.  The data may be written into memory, a file, or another NSOutputStream, as determined by how the receiver was initialised.  Any data which is successfully written is used to update the digest value.  Any data which cannot be written is not put through the digest calculation.

                Note that if the receiver is set to output to another NSOutputStream, but the stream was previously specified as nil, then the given data will always be added to the digest calculation, in it's entirity.
    @param buffer A buffer containing the data to write.  Should not be NULL.
    @param len The maximum length of valid data in 'buffer'.  At most this many bytes will be written.
    @result Returns the number of bytes written, which will always be less than or equal to 'len'.  If the stream is full it will return 0.  If an error occurs, -1 will be returned, and you will be able to obtain more information using the receivers streamError method. */

- (int)write:(const uint8_t*)buffer maxLength:(unsigned int)len;

/*! @method hasSpaceAvailable
    @abstract Returns whether or not the receiver has room for additional data.
    @discussion Works the same as NSOutputStream's version - the addition of digesting functionality has no influence on data storage.  Note that if the output of the receiver is another NSOutputStream, it will be consulted to determine if it is full (using hasSpaceAvailable again), in which case YES will be automatically returned by the receiver as well (to prevent awkward situations where the start of a chain is happy to accept any data at any time, but some element later in the chain is unable to do likewise).

                Note that if the next NSOutputStream is nil (i.e. the receiver is a destructive sink) this method will always return YES.
    @result Returns YES if the receiver (and all successive NSOutputStreams, if any) has room for 1 or more bytes of additional data, NO otherwise. */

- (BOOL)hasSpaceAvailable;

/* Our additions. */

/*! @method currentDigestValue
    @abstract Returns the current digest value of the receiver.
    @discussion The "current digest value" is the digest as computed over all the data seen by the stream so far.  This method can be called at any time, provided the stream is open - once closed the value is lost.
    @result Returns the current digest value, or nil if an error occurs (e.g. the receiver is not open & valid). */

- (NSData*)currentDigestValue;

/*! @method algorithm
    @abstract Returns the digest algorithm used by the receiver.
    @discussion The default value of the algorithm is undefined - always use setAlgorithm: prior to opening the DigestOutputStream; the default value is not defined and may change in future versions.
    @result Returns the digest algorithm used by the receiver. */

- (CSSM_ALGORITHMS)algorithm;

/*! @method setAlgorithm:
    @abstract Sets the digest algorithm to be used by the receiver.
    @discussion The default value of the algorithm is undefined - always use this method prior to opening the receiver; the default value is not defined and may change in future versions.

                Note that you cannot change the algorithm on an open stream - make sure to do so before calling 'open'.
    @param newAlgorithm The new digest algorithm to use.
    @result Returns YES if the new algorithm was accepted successfully, NO otherwise (typically when the receiver is already open or otherwise invalid). */

- (BOOL)setAlgorithm:(CSSM_ALGORITHMS)newAlgorithm;

/*! @method module
    @abstract Returns the CSP module used by the receiver.
    @discussion By default the DigestOutputStream uses whatever the default CSP is at initialisation time.  This can be overriden using setModule:.  In any case, the module the receiver is using can be retrieved using this method.

                Note that until the stream is actually opened the module may be changed, using setModule:.  Once the stream is open the module cannot be changed.
    @param module The CSP module to be used by the receiver for cryptographic operations. */

- (CSSMModule*)module;

/*! @method setModule:
    @abstract Sets the CSP module to be used by the receiver.
    @discussion By default the DigestOutputStream uses whatever the default CSP is at initialisation time.  You can override this using this method.

                Note that you cannot change the module on an open stream - make sure to do so before calling 'open'.
    @param CSPModule The CSP module to use for cryptographic operations.  May be nil, in which case the current default is used.
    @result Returns YES if successful, NO otherwise (typically when the receiver is already open or otherwise invalid). */

- (BOOL)setModule:(CSSMModule*)CSPModule;

@end
