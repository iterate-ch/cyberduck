//
//  Access.h
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
#import <Keychain/AccessControlList.h>


/*! @class Access
    @abstract Groups a number of AccessControlList instances under a common name and usage.
    @discussion An Access instance groups together a number of AccessControlList instances, which can then be applied together to control access to resources.  Generally, it represents the collective access controls to an object.  The Access itself has no knowledge of what object(s) it applies to; it is merely the mechanism, not policy.
*/

@interface Access : NSCachedObject {
    SecAccessRef access;
    int error;
}

// Q) Why isn't there a mechanism to retrieve the name after creation?
// A) That would be too easy.

/*! @method accessWithName:
    @abstract Creates and returns an empty Access with the name provided.
    @param name The name of the new Access instance.
    @result Returns the new instance, or nil if an error occurs. */

+ (Access*)accessWithName:(NSString*)name;

/*! @method accessWithName:applications:
    @abstract Creates and returns an empty Access with the name & applications provided.
    @param name The name of the new Access instance.
    @param apps The applications for the new Access instance.
    @result Returns the new instance, or nil if an error occurs. */

+ (Access*)accessWithName:(NSString*)name applications:(NSArray*)apps;

/*! @method accessWithAccessRef:
    @abstract Creates and returns an Access instance derived from a SecAccessRef.
    @discussion Creates and returns an Access instance derived [and linked to] the SecAccessRef provided.  Changes to the SecAccessRef will reflect in the returned instance, and vice versa.  Note that instances created from SecAccessRef's are cached, meaning successive calls to this method with the same SecAccessRef will return the same unique instance.
    @param acc The SecAccessRef from which to derive the Access instance.
    @result Returns the existing instance for this SecAccessRef, if one exists, or creates and returns a new instance if not.  Returns nil if an error occurs. */

+ (Access*)accessWithAccessRef:(SecAccessRef)acc;

/*! @method initWithName:applications:
    @abstract Initialises an Access instance with the name & applications provided.
    @discussion Unlike the initWithAccessRef method, this does not cache each copy created.  This means you can have multiple instances with the same name, and you can't get a reference to any unique instance by name using this method, as you may be tempted to try doing.  If an error occurs, nil is returned.
    @param name The name to be given to the Access instance.
    @param apps The list of zero or more SecTrustedApplicationRef or TrustedApplication's to add to the new Access.  If this is nil the current application is added by default.
    @result Returns the receiver if successful, otherwise the receiver is released and nil is returned. */

- (Access*)initWithName:(NSString*)name applications:(NSArray*)apps;

/*! @method initWithAccessRef:
    @abstract Initialises an Access instance around the SecAccessRef provided
    @discussion This initializer keeps a cache of each unique instance it creates, so that initializing several objects using the same SecAccessRef will return the same unique instance.  Thus, it may not return itself.  If an error occurs, nil is returned.
    @param acc The SecAccessRef with which to initialize the Access instance.  The receiver retains a copy of this reference.
    @result If an Access instance already exists for this SecAccessRef, then it is returned instead of the receiver.  If no existing instance exists, the receiver is initialized with the SecAccessRef and returned.  If an error occurs, the receiver is released and nil is returned. */

- (Access*)initWithAccessRef:(SecAccessRef)acc;

/*! @method init
    @abstract Initialises an empty Access instance for the current application.
    @discussion Returns an empty Access instance with only the current application in it's trusted list.  The new instance is by default named "Unnamed".
    @result Returns the receiver is successful, otherwise the receiver is released and nil is return. */

- (Access*)init;

/*! @method accessControlLists
    @abstract Returns the AccessControlList's which are a part of the receiver.
    @result An NSArray containing all the AccessControlList's which are part of the receiver. */

- (NSArray*)accessControlLists;

/*! @method accessControlListsForAction:
    @abstract Returns all AccessControlList's which authorize the action specified.
    @param action The action type.
    @result Returns an NSArray containing one or more AccessControlList's.  May return nil or an empty NSArray if no matches are found. */

- (NSArray*)accessControlListsForAction:(CSSM_ACL_AUTHORIZATION_TAG)action;

/*! @method accessControlListsForEverything
    @abstract Returns all AccessControlList's which authorize Everything.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.  This is not the same as the accessControlLists method.  There exists a specific 'Everything' action, which is separate from and not a set of all other actions.  So an AccessControlList may authorize any action, but that does not mean to say it authorizes 'Everything'.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForEverything;

/*! @method accessControlListsForLogin
    @abstract Returns all AccessControlList's which authorize a login operation or usage.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForLogin;

/*! @method accessControlListsForGeneratingKeys
    @abstract Returns all AccessControlList's which authorize key generation.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForGeneratingKeys;

/*! @method accessControlListsForDeletion
    @abstract Returns all AccessControlList's which authorize deletion and removal operations.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForDeletion;

/*! @method accessControlListsForExportingWrapped
    @abstract Returns all AccessControlList's which authorize exporting keys in wrapped form.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.  Note that this authorization covers wrapping a key with another key.  Exporting in the clear, also known as null wrapping, requires a separate authorization
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForExportingWrapped;

/*! @method accessControlListsForExportingClear
    @abstract Returns all AccessControlList's which authorize exporting keys in clear form (null wrapped).
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.  Note that this authorization does not cover or grant the ability to wrap keys with other keys.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForExportingClear;

/*! @method accessControlListsForImportingWrapped
    @abstract Returns all AccessControlList's which authorize importing wrapped keys.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.  Note that this authorization covers importing keys wrapped with other keys.  Importing clear (null wrapped) keys requires a separate authorization
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForImportingWrapped;

/*! @method accessControlListsForImportingClear
    @abstract Returns all AccessControlList's which authorize importing clear keys (null wrapped).
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.  Note that this authorization does not cover or grant the ability to import keys wrapped with other keys
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForImportingClear;

/*! @method accessControlListsForSigning
    @abstract Returns all AccessControlList's which authorize signing and verifying operations.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForSigning;

/*! @method accessControlListsForEncrypting
    @abstract Returns all AccessControlList's which authorize encryption operations.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForEncrypting;

/*! @method accessControlListsForDecrypting
    @abstract Returns all AccessControlList's which authorize decryption operations.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForDecrypting;

/*! @method accessControlListsForMACGeneration
    @abstract Returns all AccessControlList's which authorize MAC generation and verification.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForMACGeneration;

/*! @method accessControlListsForDerivingKeys
    @abstract Returns all AccessControlList's which authorize key derivation.
    @discussion This is merely a convenience method which itself calls accessControlListsForAction.
    @result As for accessControlListsForAction. */

- (NSArray*)accessControlListsForDerivingKeys;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method accessRef
    @abstract Returns the SecAccessRef the receiver is based on.
    @discussion If the receiver was created from a SecAccessRef, it is this original reference that is returned.  Otherwise, a SecAccessRef is created and returned.
    @result The SecAccessRef for the receiver.  You should retain this if you wish to use it beyond the lifetime of the receiver. */

- (SecAccessRef)accessRef;

@end
