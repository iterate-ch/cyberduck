//
//  TrustedApplication.h
//  Keychain
//
//  Created by Wade Tregaskis on Fri Jan 24 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Keychain/NSCachedObject.h>
#import <Foundation/Foundation.h>
#import <Security/Security.h>


/*! @class TrustedApplication
    @abstract Represents an executable file on disk.
    @discussion Although the name contained "Trusted", this does not imply the application is in fact trusted in any way.  A level of trusted may be granted to the application by adding it to appropriate ACLs and so forth.  The TrustedApplication terminology is only used here so as to conform to the Security framework's use of the name.

                Each instance records information about the application at the time it was created.  This information can be used to verify that the application has not changed since the instance was created.  The format of this information is not defined, being Apple-proprietary.  Nor is the exact nature of this temporal "equality" check defined; it may be as simple as a hash of the application file, or something else.  The implementation is in the Security framework and is not defined or controlled by the Keychain framework.  Consult the relevant Apple documentation for any more details.

                To retrieve the information identifying the application at a particular point in time, use the data method.  You can compare data between TrustedApplication instances, and do things such as save it to disk, send it over the network, etc.  To obtain a TrustedApplication instance for a given application, using the stored data, simply create a TrustedApplication instance for the application as normal, then use the setData: method to override it's default data with the existing data.  This may cause the TrustedApplication to no longer be "trusted", if the data you have specified does not match that on disk.

                While you can detect modifications to an application by comparing the data returned by the TrustedApplication instance, you should be aware of the race conditions that can arise.  In particular, verifying the data of the application before manually launching it is <b>not</b> safe.  At time of writing there is no machinery for doing this sort of operation atomically. */

@interface TrustedApplication : NSCachedObject {
    SecTrustedApplicationRef trustedApplication;
    int error;
}

/*! @method trustedApplicationWithPath:
    @abstract Returns a new TrustedApplication instance for the application with the given path.
    @discussion Each instance of a TrustedApplication is unique, even if it represents the same path, as the time of creation is important - it defines what the TrustedApplication instance considers "correct" when verifying the application.  Thus, multiple calls to this method will return different instances.
    @param path The path to the application to be covered by the TrustedApplication instance.
    @result Returns a new TrustedApplication instance for the given path, or nil if an error occurs. */

+ (TrustedApplication*)trustedApplicationWithPath:(NSString*)path;

/*! @method trustedApplicationWithTrustedApplicationRef:
    @abstract Returns a TrustedApplication instance for a given SecTrustedApplicationRef.
    @discussion You can use this method for obtaining a TrustedApplication instance that wraps around an existing SecTrustedApplicationRef.  Multiple calls to this method with the same SecTrustedApplicationRef will return the same instance.
    @param trustedApp The SecTrustedApplicationRef to wrap around.
    @result Returns a TrustedApplication instance for the given SecTrustedApplicationRef. */

+ (TrustedApplication*)trustedApplicationWithTrustedApplicationRef:(SecTrustedApplicationRef)trustedApp;

/*! @method initWithPath:
    @abstract Initialises the receiver for the application at the given path.
    @discussion Each instance of a TrustedApplication, even if based on the same path, is considered unique.  Thus, this method will not return any existing instance that may also use the given path.  Once initialised you can use the setData: and data methods as appropriate, or use the TrustedApplication in an ACL or similar.
    @param path The path of the application to be covered by the receiver.
    @result Returns a TrustedApplication instance (not necessarily the receiver) for the given path. */

- (TrustedApplication*)initWithPath:(NSString*)path;

/*! @method initWithTrustedApplicationRef:
    @abstract Initialises the receiver as a wrapper around an existing SecTrustedApplicationRef.
    @discussion There can only be one TrustedApplication instance per SecTrustedApplicationRef, so this method may release the receiver and return the existing TrustedApplication instance for the given SecTrustedApplicationRef, if such a thing exists.  Otherwise, it returns a newly initialised TrustedApplication instance for the given SecTrustedApplicationRef.
    @param trustedApp The SecTrustedApplicationRef for the receiver to wrap around.
    @result Returns the TrustedApplication instance for the given SecTrustedApplicationRef, which may or may not be the receiver.  Returns nil if an error occurs. */

- (TrustedApplication*)initWithTrustedApplicationRef:(SecTrustedApplicationRef)trustedApp;

/*! @method init
    @abstract Initialises the receiver as a TrustedApplication for the current application.
    @discussion Note that this is the same as calling initWithPath: and passing nil as the path parameter.  As a consequence, it also will always initialise a new instance, even if an existing instance for the same path already exists.
    @result Returns the receiver if successful, otherwise releases the receiver and returns nil. */

- (TrustedApplication*)init;

/*! @method setData:
    @abstract Sets the opaque verification data for the receiver.
    @discussion The verification data is used to determine if the application has been modified since it was first seen.  You will want to use the setData: method if you wish to accept the modifications, or if you are recreating a previous TrustedApplication using data from the network, previously stored, or similar.
    @param data The new verification data to use for the receiver. */

- (void)setData:(NSData*)data;

/*! @method data
    @abstract Returns the opaque verification data for the receiver.
    @discussion This method returns the original verification data for the receiver's application, which may or may not represent the actual data for the application on disk, depending on whether the application has been modified or not since the receiver was initialised.  You can store this data for later comparison or use.
    @result Returns the opaque verification data for the receiver. */

- (NSData*)data;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method trustedApplicationRef
    @abstract Returns the underlying SecTrustedApplicationRef of the receiver.
    @discussion The TrustedApplication is ultimately a wrapper over a SecTrustedApplicationRef instance.  You can use this method to retrieve the receiver's SecTrustedApplicationRef, so that you may use it directly with the Security framework.
    @result Returns the receiver's SecTrustedApplicationRef. */

- (SecTrustedApplicationRef)trustedApplicationRef;

@end
