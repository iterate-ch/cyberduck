//
//  AccessControlList.h
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
#import <Security/SecACL.h>
#import <Keychain/TrustedApplication.h>


// We're inherited by the Access class, yet we use it ourselves, so we need to forward declare it.  We still get warnings, unfortunately.  Just ignore these.
@class Access;


/*! @class AccessControlList
    @abstract Defines a set of authorizations for a set of applications
    @discussion An AccessControlList contains a list of authorizations, of various pre-defined types, and a list of TrustedApplication's to which these authorizations apply.  AccessControlList's are usually grouped together, as appropriate, under an Access instance. */

@interface AccessControlList : NSCachedObject {
    SecACLRef ACL;
    int error;
}

/*! @method accessControlListNamed:fromAccess:forApplications:requiringPassphrase:
    @abstract Creates and returns a named AccessControlList as a member of the provided Access instance, for the list of TrustedApplication's provided.
    @discussion It may seem a tad inflexible to require an AccessControlList to be created for an already-existing Access instance.  I totally agree - email the developers at Apple for writing their underlying Security framework in this way.
    @param name The name of the resulting AccessControlList.  This may be changed at a later date, and should not necessarily be used to uniquely identify an instance.
    @param acc The Access to which the resulting AccessControlList will automatically be added.  This cannot be nil.
    @param applications A list of TrustedApplication's to which the resulting AccessControlList applies.  This may be nil.
    @param requiringPassphrase If YES, the current users keychain passphrase must be provided to apply the authorizations in this list.  If NO, the authorizations are always available and in effect.
    @result Returns the newly created AccessControlList instance, or nil if an error occurs. */

+ (AccessControlList*)accessControlListNamed:(NSString*)name fromAccess:(Access*)acc forApplications:(NSArray*)applications requiringPassphrase:(BOOL)reqPass;

/*! @method accessControlListWithACLRef:
    @abstract Creates and returns an AccessControlList derived from the SecACLRef provided
    @discussion The returned instance acts as a wrapper around the SecACLRef.  Any changes to the SecACLRef will reflect in the AccessControlList instance, and vice versa.  The returned instance retains a copy of the SecACLRef for the duration of it's life.

                Note that this method caches each unique object, such that additional calls with the same SecACLRef will return the existing AccessControlList for that particular SecACLRef, not new instances
    @param AC The SecACLRef from which to derive the result
    @result An AccessControlList representing and wrapping around the SecACLRef provided */

+ (AccessControlList*)accessControlListWithACLRef:(SecACLRef)AC;

/*! @method initWithName:fromAccess:forApplications:requiringPassphrase:
    @abstract Initializes an AccessControlList with the provided name, representing the supplied TrustedApplication's (if any), and added automatically to the Access instance provided.
    @discussion It may seem a tad inflexible to always require an AccessControlList to be added to an already-existing Access instance.  I totally agree - email the developers at Apple for writing their underlying Security framework in this way.
    @param name The name to be given to the receiver.  This may be changed at a later date, and should not necessarily be used to uniquely identify an instance.
    @param acc The Access to which the receiver will automatically be added.  This cannot be nil.
    @param applications A list of TrustedApplication's to which the receiver will apply.  This may be nil.
    @param requiringPassphrase If YES, the current users keychain passphrase must be provided to apply the authorizations in this list.  If NO, the authorizations are always available and in effect.
    @result Returns the initialized receiver, or nil if an error occurs. */

- (AccessControlList*)initWithName:(NSString*)name fromAccess:(Access*)acc forApplications:(NSArray*)applications requiringPassphrase:(BOOL)reqPass;

/*! @method initWithACLRef:
    @abstract Initializes the receiver around the SecACLRef provided.
    @discussion This initializer keeps a cache of each unique instance it creates, so that initializing several objects using the same SecAccessRef will return the same unique instance.  Thus, it may not return itself.  If an error occurs, nil is returned.
    @param AC The SecACLRef to wrap around.
    @result If an instance already exists for the provided SecACLRef, the receiver is released and the existing instance returned.  Otherwise, the receiver is initialized appropriate.  Returns nil if an error occurs. */

- (AccessControlList*)initWithACLRef:(SecACLRef)AC;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise an AccessControlList using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (AccessControlList*)init;

/*! @method setApplications:
    @abstract Sets the list of TrustedApplications the receiver governs.  If "applications" is NULL, all applications will be trusted.  If it is an empty array, no applications will be trusted.
    @param applications An NSArray of TrustedApplications. */

- (void)setApplications:(NSArray*)applications;

/*! @method setName:
    @abstract Sets the name of the receiver to the value given.
    @param name The new name to be given to the receiver. */

- (void)setName:(NSString*)name;

/*! @method setRequiresPassphrase:
    @abstract Sets whether or not the receiver requires the user's authorization to be used.
    @discussion If this is set to YES, the user must provided their authorization (by entering their keychain password) in order to the receiver's authorizations to be applied.
    @param reqPass Whether or not the user's authorization is required. */

- (void)setRequiresPassphrase:(BOOL)reqPass;

/*! @method applications
    @abstract Returns the list of TrustedApplication's the receiver governs.
    @result An NSArray containing 1 or more TrustedApplication's.  This may return an empty NSArray or nil if no TrustedApplication's are governed. */

- (NSArray*)applications;

/*! @method name
    @abstract Returns the name of the receiver.
    @discussion An AccessControlList's name is not inherantly a unique identifier of that particular instance.  Be aware of this, and avoid making such dangerous assumptions.
    @result The name of the receiver. */

- (NSString*)name;

/*! @method requiresPassphrase
    @abstract Returns whether or not the current user's permission is required in order for the receiver to apply it's authorizations.
    @discussion If this is YES, the user must provided their permission (and authenticate against their keychain password) before the receiver can apply it's authorizations.
    @result Whether or not the user's permission is currently required. */

- (BOOL)requiresPassphrase;

/*! @method setAuthorizesAction:to
    @abstract Sets whether or not the receiver authorizes a particular action.
    @param action The action type.
    @param to Whether or not the receiver should authorize the action. */

- (void)setAuthorizesAction:(CSSM_ACL_AUTHORIZATION_TAG)action to:(BOOL)value;

/*! @method setAuthorizesEverything
    @abstract Sets whether the receiver authorizes Everything.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.  Note that 'Everything' here is a separate and distinct action in it's own right - it is not an encompassing set of all available actions.
    @param value Whether or not the receiver should authorize Everything. */

- (void)setAuthorizesEverything:(BOOL)value;

/*! @method setAuthorizesLogin
    @abstract Sets whether the receiver authorizes login operations and usage.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize login operations and usage. */

- (void)setAuthorizesLogin:(BOOL)value;

/*! @method setAuthorizesGeneratingKeys
    @abstract Sets whether the receiver authorizes key generation.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize key generation. */

- (void)setAuthorizesGeneratingKeys:(BOOL)value;

/*! @method setAuthorizesDeletion
    @abstract Sets whether the receiver authorizes deletion and removal operations.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize deletion and removal. */

- (void)setAuthorizesDeletion:(BOOL)value;

/*! @method setAuthorizesExportingWrapped
    @abstract Sets whether the receiver authorizes exporting keys wrapped with other keys.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.  Note that this is a distinct authorization to allowing clear (null wrapped) keys.
    @param value Whether or not the receiver should authorize exporting wrapped keys */

- (void)setAuthorizesExportingWrapped:(BOOL)value;

/*! @method setAuthorizesExportingClear
    @abstract Sets whether the receiver authorizes exporting keys in the clear (null wrapped).
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.  Note that this is a separate and distinct authorization to exporting keys wrapped with other keys.
    @param value Whether or not the receiver should authorize exporting keys in the clear. */

- (void)setAuthorizesExportingClear:(BOOL)value;

/*! @method setAuthorizesImportingWrapped
    @abstract Sets whether the receiver authorizes importing keys wrapped with other keys.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.  Note that this authorization does not allow importing clear (null wrapped) keys.
    @param value Whether or not the receiver should authorize importing keys wrapped with other keys. */

- (void)setAuthorizesImportingWrapped:(BOOL)value;

/*! @method setAuthorizesImportingClear
    @abstract Sets whether the receiver authorizes importing keys in the clear (null wrapped).
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.  Note that this is a distinct and separate authorization to importing keys wrapped with other keys.
    @param value Whether or not the receiver should authorize importing keys in the clear. */

- (void)setAuthorizesImportingClear:(BOOL)value;

/*! @method setAuthorizesSigning
    @abstract Sets whether the receiver authorizes signing and verification operations.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize signing and verification operations. */

- (void)setAuthorizesSigning:(BOOL)value;

/*! @method setAuthorizesEncrypting
    @abstract Sets whether the receiver authorizes encryption operations.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize encryption operations. */

- (void)setAuthorizesEncrypting:(BOOL)value;

/*! @method setAuthorizesDecrypting
    @abstract Sets whether the receiver authorizes decryption operations.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize decryption operations. */

- (void)setAuthorizesDecrypting:(BOOL)value;

/*! @method setAuthorizesMACGeneration
    @abstract Sets whether the receiver authorizes MAC generation and verification.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize MAC generation and verification. */

- (void)setAuthorizesMACGeneration:(BOOL)value;

/*! @method setAuthorizesDerivingKeys
    @abstract Sets whether the receiver authorizes key derivation.
    @discussion This is merely a convenience method, which itself calls setAuthorizesAction.
    @param value Whether or not the receiver should authorize key derivation. */

- (void)setAuthorizesDerivingKeys:(BOOL)value;

/*! @method authorizesAction:
    @abstract Returns whether or not the receiver provides authorization for a particular action.
    @param action The action in question.
    @result Whether or not the receiver authorizes the action provided. */

- (BOOL)authorizesAction:(CSSM_ACL_AUTHORIZATION_TAG)action;

/*! @method authorizesEverything
    @abstract Returns whether or not the receiver provides authorization for Everything.
    @discussion This is merely a convenience method, which itself calls authorizesAction.  Note that 'Everything' is a specific and distinct action in itself, not merely a grouping of all available actions.
    @result Whether or not the receiver provides authorization for Everything. */

- (BOOL)authorizesEverything;

/*! @method authorizesLogin
    @abstract Returns whether or not the receiver provides authorization for login operations and usage.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for login operations and usage. */

- (BOOL)authorizesLogin;

/*! @method authorizesGeneratingKeys
    @abstract Returns whether or not the receiver provides authorization for key generation.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for key generation. */

- (BOOL)authorizesGeneratingKeys;

/*! @method authorizesDeletion
    @abstract Returns whether or not the receiver provides authorization for deletion and removal operations.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for deletion and removal operations. */

- (BOOL)authorizesDeletion;

/*! @method authorizesExportingWrapped
    @abstract Returns whether or not the receiver provides authorization for exporting keys wrapped with other keys.
    @discussion This is merely a convenience method, which itself calls authorizesAction.  Note that being able to export keys wrapped with other keys does not imply being able to export clear (null wrapped) keys.
    @result Whether or not the receiver provides authorization for exporting keys wrapped with other keys. */

- (BOOL)authorizesExportingWrapped;

/*! @method authorizesExportingClear
    @abstract Returns whether or not the receiver provides authorization for exporting keys in the clear (null wrapped).
    @discussion This is merely a convenience method, which itself calls authorizesAction.  Note that being able to export keys in the clear (null wrapped) does not imply being able to export keys wrapped with other keys.
    @result Whether or not the receiver provides authorization for exporting keys in the clear. */

- (BOOL)authorizesExportingClear;

/*! @method authorizesImportingWrapped
    @abstract Returns whether or not the receiver provides authorization for importing keys wrapped with other keys.
    @discussion This is merely a convenience method, which itself calls authorizesAction.  Note that being able to import keys wrapped with other keys does not imply being able to import clear (null wrapped) keys.
    @result Whether or not the receiver provides authorization for importing keys wrapped with other keys. */

- (BOOL)authorizesImportingWrapped;

/*! @method authorizesImportingClear
    @abstract Returns whether or not the receiver provides authorization for importing keys in the clear (null wrapped).
    @discussion This is merely a convenience method, which itself calls authorizesAction.  Note that being able to import keys in the clear (null wrapped) does not imply being able to import keys wrapped with other keys.
    @result Whether or not the receiver provides authorization for importing keys in the clear. */

- (BOOL)authorizesImportingClear;

/*! @method authorizesSigning
    @abstract Returns whether or not the receiver provides authorization for signing and verification operations.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for signing and verification operations. */

- (BOOL)authorizesSigning;

/*! @method authorizesEncrypting
    @abstract Returns whether or not the receiver provides authorization for encryption operations.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for encryption operations. */

- (BOOL)authorizesEncrypting;

/*! @method authorizesDecrypting
    @abstract Returns whether or not the receiver provides authorization for decryption operations.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for decryption operations. */

- (BOOL)authorizesDecrypting;

/*! @method authorizesMACGeneration
    @abstract Returns whether or not the receiver provides authorization for MAC generation and verification.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for MAC generation and verification. */

- (BOOL)authorizesMACGeneration;

/*! @method authorizesDerivingKeys
    @abstract Returns whether or not the receiver provides authorization for key derivation.
    @discussion This is merely a convenience method, which itself calls authorizesAction.
    @result Whether or not the receiver provides authorization for key derivation. */

- (BOOL)authorizesDerivingKeys;

/*! @method deleteAccessControlList
    @abstract Removes the receiver from it's owning Access instance
    @discussion I believe this is the behaviour.  However, it may not be correct - the Security framework documentation is extremely sparse. */

- (void)deleteAccessControlList;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method accessRef
    @abstract Returns the SecAccessRef the receiver is based on.
    @discussion If the receiver was created from a SecACLRef, it is this original reference that is returned.  Otherwise, a SecACLRef is created and returned.
    @result The SecACLRef for the receiver.  You should retain this if you wish to use it beyond the lifetime of the receiver. */

- (SecACLRef)ACLRef;

@end
