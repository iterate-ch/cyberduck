//
//  Keychain.h
//  Keychain
//
//  Created by Wade Tregaskis on Fri Jan 24 2003.
//  Modified by Wade Tregaskis & Mark Ackerman on Mon Sept 29 2003 [redone all the password-related methods].
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
#import <Keychain/Access.h>
#import <Keychain/Certificate.h>
#import <Keychain/Identity.h>
#import <Keychain/Key.h>


// These four constants are used as NSDictionary keys in Keychain notifications
extern NSString *KeychainKeychainKey; // The keychain relevant to the event
extern NSString *KeychainItemKey; // The keychain item relevant to the event
extern NSString *KeychainVersionKey; // The version of the keychain callback (probably useless, but included for completeness)
extern NSString *KeychainProcessIDKey; // The PID of the process which triggered the event (if applicable)

// These constants are the notifications sent by the Keychain
extern NSString *DefaultKeychainChangedNotification; // Sent on both the old keychain and the new one
extern NSString *KeychainLockedNotification; // May not work in 10.2 (or earlier) properly - I can't remember why, but I do remember having issues with it.  As it stands, it does appear to work perfectly well in 10.3.
extern NSString *KeychainUnlockedNotification; // As above, with the cavaet that an unlock event is often generated prior to accessing the password to an item (in the case where the application accessing it has not previously been granted permanent access to the item), even though the specific item in question is not provided.  Consequently, don't be surprised if you get a lot of these...  if the keychain is locked and an application attempts to access an item, you will get two events, one for the actual unlocking of the keychain and one for the "unlocking" of the item.
extern NSString *KeychainItemAddedNotification; // Sent on the new item *and* the keychain it is added to
extern NSString *KeychainItemRemovedNotification; // Sent on the old item *and* the keychain it is removed from.  Note that the item in question is no longer valid at the time this notification is posted (let alone received), so you cannot expect to use it in any meaningful way.  It is only of use for uniquely identifying the item removed.
extern NSString *KeychainItemUpdatedNotification; // Sent on the item updated
extern NSString *KeychainPasswordChangedNotification; // Sent on the keychain for which the password was changed
extern NSString *KeychainItemAccessedNotification; // Sent on the item accessed
extern NSString *KeychainListChangedNotification; // Sent on all keychains


/*! @class Keychain
    @abstract Represents a collection of keys, certificates, identities, passwords and other such personal items.
    @discussion The keychain is a well known part of MacOS Classic and MacOS X, so I won't rehash the details here.  If you really have no idea what the keychain is, check out Keychain Access in the Utilities folder, and the relevant documentation on <a href="http://developer.apple.com/security/">Apple's Security page</a>. */

@interface Keychain : NSCachedObject {
    SecKeychainRef keychain;
    int error;
}

/*! @method keychainManagerVersion
    @abstract Returns the major version number of the active keychain manager.
    @discussion This may or may not be linked to the version number of a keychain. */

+ (UInt32)keychainManagerVersion;

/*! @method setUserInteractionAllowed:
    @abstract Sets whether or not any user interaction should occur when using keychains.
    @discussion The scope of this is not well defined in the Security framework, but I believe this turns off only optional interaction - whatever that may be.  So users will probably still be handed dialogs requesting access for your app.
    @param allowed Is YES, user interaction will be allowed when necessary. */

+ (void)setUserInteractionAllowed:(BOOL)allowed;

/*! @method userInteractionAllowed
    @abstract Returns whether any user interaction will occur when using keychains.
    @discussion The scope of this is not well defined in the Security framework, but I believe this turns off only optional interaction - whatever that may be.  So users will probably still be handed dialogs requesting access for your app.
    @result Returns YES if user interaction is allowed, NO otherwise and in case of error. */

+ (BOOL)userInteractionAllowed;

/*! @method lockAll
    @abstract Locks all the current user's keychains.
    @discussion You usually won't have any use for this method.  Indeed, if used improperly it will cause severe annoyance for the user.
    @result Returns YES if successful, NO otherwise and in case of error. */

+ (BOOL)lockAll;

/*! @method keychainWithKeychainRef:
    @abstract Creates and returns a Keychain based on a SecKeychainRef.
    @discussion Note that this method caches existing Keychain instances, such that multiple calls to it with the same SecKeychainRef will return the same unique instance.  Also note that the returned Keychain instance is linked with the SecKeychainRef, such that changes to either one will reflect on the other.
    @param keych A SecKeychainRef.
    @result If a Keychain instance for the given SecKeychainRef already exists, the existing instance is returned.  Otherwise, a new one is created and returned.  Returns nil if an error occurs. */

+ (Keychain*)keychainWithKeychainRef:(SecKeychainRef)keych;

/*! @method createNewKeychainAtPath:withPassword:
    @abstract Creates and returns a new Keychain at the path specified, with the password given.
    @discussion This will overwrite any existing file at the location indicated.
    @param path The location at which to create the new keychain file.
    @param password The password for the new keychain.  This may be nil.
    @result The newly created keychain, or nil if an error occurs. */

+ (Keychain*)createNewKeychainAtPath:(NSString*)path withPassword:(NSString*)password;

/*! @method defaultKeychain
    @abstract Returns the default keychain for the current user.
    @discussion This method always returns the same unique instance (unless of course the default keychain is changed).
    @result The current user's keychain, or nil if an error occurs. */

+ (Keychain*)defaultKeychain;

/*! @method defaultKeychainForUser:
    @abstract Opens and returns the default keychain for a given user.
    @discussion This method caches each Keychain instance, such that multiple requests for the same user's default keychain will return the same unique instance.
    @param username The short or full username.  If this is nil, the current user is used.
    @result The given user's default keychain, or nil if an error occurs (e.g. no such user, etc). */

+ (Keychain*)defaultKeychainForUser:(NSString*)username;

/*! @method keychainAtPath:
    @abstract Opens and returns a Keychain instance from a keychain file on disk.
    @discussion This method caches each Keychain instance, such that multiple requests for the same path will return the same unique instance.
    @param path The path of the keychain file to open.  This must already exist, and cannot be nil.
    @result The keychain for the given path, or nil if an error occurs (e.g. the path is missing, or invalid, etc). */

+ (Keychain*)keychainAtPath:(NSString*)path;

/*! @method initWithKeychainRef:
    @abstract Initializes the receiver from a SecKeychainRef.
    @discussion This method caches unique Keychain instances, such that multiple calls to it with the same SecKeychainRef will release the duplicates and return the existing instance.

                Note that the receiver is linked to the SecKeychainRef, such that changes to one will reflect on the other.
    @param keych The SecKeychainRef to initialize the receiver with.  This is retained by the receiver for the duration of the receiver's life.
    @result If a Keychain instance already exists for the given SecKeychainRef, releases the receiver and returns the existing instance.  Otherwise initializes the receiver and returns it.  Returns nil if an error occurs. */

- (Keychain*)initWithKeychainRef:(SecKeychainRef)keych;

/*! @method initNewAtPath:withPassword:
    @abstract Initializes the receiver as a new keychain at the location specified.
    @discussion This creates a new keychain file on disk at the path specified, to represent the receiver.  Any existing file (or keychain) at the given path will be destroyed.
    @param path The path of the file you wish to create for the keychain.
    @param password The password to lock/unlock the keychain.  This may be nil.
    @result If successful, returns the receiver.  Otherwise returns nil. */

- (Keychain*)initNewAtPath:(NSString*)path withPassword:(NSString*)password;

/*! @method initFromDefault;
    @abstract Initializes the receiver from the default keychain for the current user.
    @discussion This method caches existing Keychain instances, so if there is already an instance for the current user's default keychain, it will be returned instead.  Consequently, you should try to use the defaultKeychain class method instead of this method, to save on unnecessary memory allocation and deallocation.
    @result If a Keychain instance already exists for the current user's default keychain, the receiver is released and the existing instance returned.  Otherwise the receiver is initialized from the user's default keychain.  Returns nil if an error occurs, or the user has no default keychain. */

- (Keychain*)initFromDefault;

/*! @method initFromPath:
    @abstract Initializes the receiver from the keychain file provided.
    @discussion This method should return an existing instance, if one exists, for the path specified.  This has not, however, been tested.
    @param path A standard file path to the keychain file to use.
    @result If a Keychain instance already exists for the given path, the receiver will be released and the existing instance returned.  Otherwise the receiver is initialized to become the unique handler of the keychain file specified, and returned.  Returns nil if an error occurs (e.g. the path specified is invalid, the file specified is not a keychain file, etc). */

- (Keychain*)initFromPath:(NSString*)path;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a Keychain using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (Keychain*)init;

/*! @method path
    @abstract Returns the path of the receiver on disk.
    @discussion The returned path points to the actual file in which the receiver's data is sourced (and written to, if applicable).
    @result A standard file path, as suitable for use with most Cocoa methods. */

- (NSString*)path;

/*! @method makeDefault
    @abstract Makes the receiver the default keychain for the current user.
    @discussion You should only call this if you're quite sure the user wants this.  The existing default keychain will not be deleted or anything like that, and may in fact still remain in the current user's list of default keychains, but some applications may not check beyond the default keychain, and so may not be able to access any entries in the old default. */

- (void)makeDefault;

/*! @method isUnlocked
    @abstract Returns whether the receiver is unlocked or not.
    @result Returns YES if the receiver is unlocked, NO otherwise and in case of error. */

- (BOOL)isUnlocked;

/*! @method isReadOnly
    @abstract Returns whether the receiver is read only or not.
    @discussion This property is independent of whether the receiver is unlocked or not - it reflects the keychain file's permissions on disk.  Thus, this may return YES, but if the receiver is locked you still won't be able to read from it.
    @result Returns YES if the receiver is read only (i.e. not writable), NO otherwise and in case of error. */

- (BOOL)isReadOnly;

/*! @method isWritable
    @abstract Returns whether the receiver can be changed and added to.
    @discussion This property is independent of whether the receiver is locked or not.  A locked keychain cannot be read or written at all.  Only if the keychain is both unlocked and writable, as determined by this method, can it actually be written to.
    @result Returns YES if the receiver is writable (but not necessarily unlocked), NO otherwise or in case of error. */

- (BOOL)isWritable;

/*! @method setVersion:
    @abstract Sets the version of the receiver.
    @discussion You should be careful about setting this value, as it may relate intricately to the keychain file format on disk.  See the description for the version method for more details.
    @param version The major version number. */

- (void)setVersion:(UInt32)version;

/*! @method version
    @abstract Returns the version of the receiver.
    @discussion The version relates primarily to the format of the keychain file on disk, rather than how the receiver acts.  At the time of writing, all keychain versions work the same.  But MacOS 10.1 used a different format for storing keychains to 10.2.  Indeed, 10.3 or another future release may change the format again.
    @result The major version number. */

- (UInt32)version;

/*! @method setLockOnSleep:
    @abstract Sets whether or not the receiver will lock itself automatically when the system goes to sleep.
    @discussion Some users like to have their keychain lock automatically when their computer is put to sleep.  If they leave their computer alone overnight, for example, they might not want just anyone to be able to come along while they're sleeping and use their keychain.
    @param lockOnSleep If YES, the receiver will automatically lock whenever the system is put to sleep.  Otherwise it will remain unlocked (if it already is) when the computer is put to and wakes from sleep. */

- (void)setLockOnSleep:(BOOL)lockOnSleep;

/*! @method willLockOnSleep
    @abstract Returns whether or not the receiver will lock itself automatically when the system goes to sleep.
    @result Returns YES if the receiver will lock when the system sleeps, NO otherwise and in case of error. */

- (BOOL)willLockOnSleep;

/*! @method setLockAfterInterval:
    @abstract Sets whether or not the receiver will automatically lock itself after a certain period of inactivity.
    @discussion Some users like to have their keychain lock automatically after a certain period of it being unused, for extra security, as locked keychains cannot be used at all - neither read nor added to.

                Note that you should set this only after setting the interval using setInterval:.  If you set this first, and interval is 0, you will induce a race condition, the outcome of which may be that the receiver becomes locked.
    @param lockAfterInterval YES if the receiver should lock itself automatically after a certain period of inactivity, NO otherwise. */

- (void)setLockAfterInterval:(BOOL)lockAfterInterval;

/*! @method willLockAfterInterval
    @abstract Returns whether or not the receiver will automatically lock itself after a certain period of inactivity.
    @result Returns YES if the receiver is set to lock after some interval (which can be determined using the interval method), NO otherwise and in case of error. */

- (BOOL)willLockAfterInterval;

/*! @method setInterval:
    @abstract Sets the time in seconds after which a keychain automatically locks itself.
    @discussion This is an idle timeout - if the receiver is unused for the specified interval of time, it locks itself.  Note that since keychains are system-wide, other applications may use the receiver without your knowledge.  Also not that this interval does not need to be 'reset' every time you use the receiver - the receiver keeps track of time and usage itself.
    @param interval The number of seconds of idle time after which the receiver should lock itself.  This may be 0, in which case the receiver will lock itself immediately after any operation.  Since this setting is system-wide, you should be wary about setting this value to 0, or any other brief interval. */

- (void)setInterval:(UInt32)interval;

/*! @method interval
    @abstract Returns the time in seconds after which a keychain automatically locks.
    @discussion This is an idle timeout - if the receiver is unused for this interval of time, it locks itself.  Note that since keychains are system-wide, other applications may use the receiver without your knowledge.
    @result The time interval in seconds.  This may be a non-zero number even if the receiver is not set to use it. */

- (UInt32)interval;

/*! @method lock
    @abstract Locks the receiver.
    @discussion You generally should not call this.  Keychains can be set to lock themselves automatically (see setLockAfterInterval and setLockOnSleep) as required.  If you lock a keychain unnecessarily, other applications may just unlock it again straight away, which may require hassling the user.
    @result Returns YES if the receiver locks (or was already locked), NO otherwise and in case of error. */

- (BOOL)lock;

/*! @method unlock
    @abstract Unlocks the receiver using the standard user authentication and permission proceedure.
    @discussion If the receiver requires a password to be unlocked, the user will be prompted for it.
    @result Returns YES if the receiver unlocks (or was already unlocked), NO otherwise and in case of error. */

- (BOOL)unlock;

/*! @method unlockWithPassword:
    @abstract Unlocks the receiver using the provided password.
    @discussion You should try not to use this method, if you can just have the user unlock the keychain directly using the normal proceedure.  There are some uses, however, for unlocking a keychain pragmatically, provided you don't store the password, or at least store it securely.
    @param password The password to unlock the receiver.  This may be nil, if the receiver does not require a password to be unlocked.
    @result Returns YES if the receiver unlocks (or was already unlocked), NO otherwise and in case of error. */

- (BOOL)unlockWithPassword:(NSString*)password;

/*! @method addItem:
    @abstract Adds a KeychainItem to the receiver.
    @discussion This method adds the the KeychainItem given to the receiver.  This does not remove the KeychainItem from any other keychains it may be in.  Indeed, the new entry in the receiver is considered a separate entity.
    @param item The KeychainItem to add.  If this is already in the receiver, this method has no effect. */

- (void)addItem:(KeychainItem*)item;
//- (void)importCertificateBundle:(CertificateBundle*)bundle;

/*! @method addNewItemWithClass:access:
    @abstract Adds a new KeychainItem to the receiver, with given initial parameters.
    @discussion This method adds a new empty KeychainItem given to the receiver. It is primarily designed to customize authorized applications access. The resulting item has then to be filled with KeychainItem modifiers.
    @param itemClass The class of the news item.
    @param initialAccess The customized access to the item. Pass nil for giving access to the current application. */

- (KeychainItem*)addNewItemWithClass:(SecItemClass)itemClass access:(Access*)initialAccess;

/*! @method addCertificate:privateKey:withName:
    @abstract Creates a new identity in the receiver for the given certificate and private key.
    @discussion An identity is represented by a private key and a certificate in which the subject's public key is paired to the given private key.  An identity can be used to represent someone or some entity.  The certificate can be a self-signed certificate, or signed by some other authority.

                Note that if you want immediate access to the new identity, use the identityWithCertificate:privateKey:inKeychain: class constructor for the Identity class.  Note, however, that it uses this method for adding the certificate and private key to the keychain, so there is no performance benefit from using it over this method.
    @param certificate The certificate to be part of the identity.  The subject public key in this certificate should correspond to the private key provided.
    @param privateKey The private key, corresponding to the subject public key in the given certificate.
    @param name The descriptive name that will be given to the certificate and private key in the receiver. */

- (void)addCertificate:(Certificate*)certificate privateKey:(Key*)privateKey withName:(NSString*)name;

/*! @method addKey:withName:permanent:private:publicKeyHash:
    @abstract Adds a key to the receiver.
    @discussion Retrieving keys from a keychain is currently a bit tricky, so this method isn't as useful as it will one day be.  For the moment, the only real use it has is in adding certificate and private key pairs (a.k.a identities), but this can be automated for you using the addCertificate:privateKey:withName: method.
    @param key The key to add.  This may be any sort of key.
    @param name The name to be given to the key (it's user-readable description).
    @param isPermanent If YES, the key will be held in the receiver indefinitely.  If NO, it will be removed at some point in future (e.g. when the keychain is closed).
    @param isPrivate If YES the key can only be extracted by the user providing a passphrase.  It's not documented how you specify this passphrase to start with, or whether it simply means they'll be asked for their keychain passphrase.
    @param publicKeyHash An optional public key hash, if you wish to associate this key with another one. */

- (void)addKey:(Key*)key withName:(NSString*)name isPermanent:(BOOL)isPermanent isPrivate:(BOOL)isPrivate publicKeyHash:(NSData*)publicKeyHash;

/*! @method addCertificate:
    @abstract Adds a certificate to the receiver.
    @discussion This method adds the certificate given to the receiver.  If the certificate is already in the receiver, this method has no effect.
    @param cert The Certificate instance to add. */

- (void)addCertificate:(Certificate*)cert;
//- (void)addKey:(Key*)key; // Can't get all the code for this together

/*! @method addGenericPassword:onService:forAccount:replaceExisting:
    @abstract Adds a password to the receiver for a generic service with the properties given.
    @discussion This method does not require the user's authentication or permission in order to add the password to the receiver.  If an existing item is present with the same parameters, it will be replaced if you pass YES for the replaceExisting parameter, otherwise it will not and the error property set to an appropriate value.
    @param password The password.
    @param service A string describing the service name.  This is not in any standard format.  Examples include a domain name or IP address, a label indicating the password type (e.g. 'AIM' or 'ICQ'), or some other proprietary format.  You should try to use any existing 'standard' names where possible, in order to make the keychain useful.
    @param account The account for the service specified.  This may be nil.
    @param replace If YES, the password for an existing item will be replaced, if such an item already exists.  If NO, any existing item will not be changed. */

- (void)addGenericPassword:(NSString*)password onService:(NSString*)service forAccount:(NSString*)account replaceExisting:(BOOL)replace;

/*! @method addInternetPassword:onServer:forAccount:port:path:inSecurityDomain:protocol:auth:replaceExisting:
    @abstract Adds a password to the receiver for an internet service with the properties given.
    @discussion Most of the parameters are optional, or context-sensitive.  For instance, you needn't specify a domain or protocol if they don't apply to your use.

                This method does not require the user's authentication or permission in order to add the password to the receiver.  If an existing item is present with the same parameters, then it will be replaced if the replaceExisting parameter is YES.  Otherwise, it will not, and an error will occur.
    @param password The password.
    @param server The domain name or IP address of the server for which this password applies.  This parameter may be nil.
    @param account The login, username or account name on the server.  This parameter may be nil.
    @param port The port number, which may implicitly define a service type, for the server.  This may be 0, indicating no port specified.
    @param path The path of a resource on the server, to which this password applies.  This may be nil.
    @param domain The security domain to add this entry in.  This may (and most often will be) nil.
    @param protocol The protocol you are using.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychain.h>SecKeychain.h</a> for predefined types.  This parameter is essentially just a Mac type (i.e. 4 bytes), and can be user-defined.  This parameter is required.
    @param authType The authentication type to be used.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychain.h>SecKeychain.h</a> for predefined types.  You should use kSecAuthenticationTypeDefault if you have no preference or knowledge of the type to be used.  Like the protocol parameter, this a 4-byte code, which may be user defined.
    @param replace If YES then any existing item will have it's password changed, otherwise this method will fail if an item already exists. */

- (void)addInternetPassword:(NSString*)password onServer:(NSString*)server forAccount:(NSString*)account port:(UInt16)port path:(NSString*)path inSecurityDomain:(NSString*)domain protocol:(SecProtocolType)protocol auth:(SecAuthenticationType)authType replaceExisting:(BOOL)replace;

/*! @method items
    @abstract Returns every single item in the keychain, even invisible ones (e.g. keys).
    @discussion Does not work as yet; cannot figure out how to build appropriate Sec* objects from raw data.
    @result Nothing, as yet. */

- (NSArray*)items;

/*! @method passwordForGenericService:forAccount:
    @abstract Searches for and returns the password of a generic entry matching the criteria given.
    @discussion Note that this method will require the user to allow your application access to the password for the returned entry, and thus may take some time to complete while the user responds to the dialog.

                You should generally try to use this method only if you have very specific knowledge about the password you want - i.e. you know exactly what service and account.  If you know only one or other of the parameters, consider performing a proper search instead (using the KeychainSearch class), which will return all matching items, not just one at random.  You can then allow the user to select the right one.  Otherwise, you will be returned a random match, which may or may not be what you really want.
    @param service A string describing the service name.  This is not in any standard format.  Examples include a domain name or IP address, a label indicating the password type (e.g. 'AIM' or 'ICQ'), or some other proprietary format.  You should try to use any existing 'standard' names where possible, in order to make the keychain useful.
    @param account The account for the service specified.  This may be nil.
    @result If a match is found, it is returned.  Otherwise, or in case of error, nil is returned. */

- (NSString*)passwordForGenericService:(NSString*)service forAccount:(NSString*)account;

/*! @method passwordForInternetServer:forAccount:port:path:inSecurityDomain:protocol:auth:
    @abstract Searches for and returns the password for an internet entry matching the criteria given.
    @discussion Most of the parameters are optional, or context-sensitive.  For instance, you needn't specify a domain or protocol if they don't apply to your intended use.  If more than one item in the receiver matches the criteria given, only one will be returned.

                Note that this method will require the user to allow your application access to the password for the returned entry, and thus may take some time to complete while the user responds to the dialog.

                You should generally try to use this method only if you have very specific knowledge about the password you want - i.e. you have definite values for most, if not all, the parameters.  If you know only a few of the parameters, consider performing a proper search instead (using the KeychainSearch class), which will return all matching items, not just one at random.  You can then allow the user to select the right one.  Otherwise, you will be returned a random match, which may or may not be what you really want.
    @param server The domain name or IP address of the server you are accessing.  This parameter may be nil.
    @param account The login, username or account name on the server.  This parameter may be nil.
    @param port The port number, which may implicitly define a service type, for the server.  This may be 0, to accept any port number.
    @param path The path of a resource on the server, for which you are interesting in accessing.  This may be nil.
    @param domain The security domain to look in.  This may (and most often will be) nil.
@param protocol The protocol you are using.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychain.h>SecKeychain.h</a> for predefined types.  This parameter is essentially just a Mac type (i.e. 4 bytes), and can be user-defined.  This parameter is required.
@param authType The authentication type to be used.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychain.h>SecKeychain.h</a> for predefined types.  You should use kSecAuthenticationTypeDefault if you have no preference or knowledge of the type to be used.  Like the protocol parameter, this a 4-byte code, which may be user defined.
    @result If a match is found, it is returned.  Otherwise, or in case of an error, nil is returned. */

- (NSString*)passwordForInternetServer:(NSString*)server forAccount:(NSString*)account port:(UInt16)port path:(NSString*)path inSecurityDomain:(NSString*)domain protocol:(SecProtocolType)protocol auth:(SecAuthenticationType)authType;

/*! @method genericService:forAccount:
    @abstract Searches for and returns a generic KeychainItem matching the criteria given.
    @discussion Note that this method will require the user to allow your application access to the returned entry, and thus may take some time to complete while the user responds to the dialog.  If you wish to find a particular entry or entries without triggering such a dialog, see the KeychainSearch class.
    @param service A string describing the service name.  This is not in any standard format.  Examples include a domain name or IP address, a label indicating the password type (e.g. 'AIM' or 'ICQ'), or some other proprietary format.  You should try to use any existing identifiers where possible, in order to make the keychain useful.
    @param account The account for the service specified.  This may be nil.
    @result If a match is found, it is returned.  Otherwise, or in case of error, nil is returned. */

- (KeychainItem*)genericService:(NSString*)service forAccount:(NSString*)account;

/*! @method internetServer:forAccount:port:path:inSecurityDomain:protocol:auth:
    @abstract Searches for and returns an internet KeychainItem matching the criteria given.
    @discussion Most of the parameters are optional, or context-sensitive.  For instance, you needn't specify a domain or protocol if they don't apply to your intended use.  If more than one item in the receiver matches the criteria given, only one will be returned.

                Note that this method will require the user to allow your application access to the returned entry, and thus may take some time to complete while the user responds to the dialog.  If you wish to find a particular entry or entries without triggering such a dialog, see the KeychainSearch class.
    @param server The domain name or IP address of the server you are accessing.  This parameter may be nil.
    @param account The login, username or account name on the server.  This parameter may be nil.
    @param port The port number, which may implicitly define a service type, for the server.  This may be 0, to accept any port number.
    @param path The path of a resource on the server, for which you are interesting in accessing.  This may be nil.
    @param domain The security domain to look in.  This may (and most often will be) nil.
    @param protocol The protocol you are using.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychain.h>SecKeychain.h</a> for predefined types.  This parameter is essentially just a Mac type (i.e. 4 bytes), and can be user-defined.  This parameter is required.
    @param authType The authentication type to be used.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychain.h>SecKeychain.h</a> for predefined types.  You should use kSecAuthenticationTypeDefault if you have no preference or knowledge of the type to be used.  Like the protocol parameter, this a 4-byte code, which may be user defined.
    @result If a match is found, it is returned.  Otherwise, or in case of an error, nil is returned. */

- (KeychainItem*)internetServer:(NSString*)server forAccount:(NSString*)account port:(UInt16)port path:(NSString*)path inSecurityDomain:(NSString*)domain protocol:(SecProtocolType)protocol auth:(SecAuthenticationType)authType;

/*! @method identitiesForUse:
    @abstract Returns an array of identities in the receiver that are capable of performing the usage given.
    @param use A key usage.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h>cssmtype.h</a> for valid usages.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForUse:(CSSM_KEYUSE)use;

/*! @method identities
    @abstract Returns an array of all the identities in the receiver.
    @discussion This is simply a convenience method, which itself uses identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identities;

/*! @method identitiesForAnyUse
    @abstract Returns an array of identities in the receiver that are capable of being used for anything *and everything*.
    @discussion The 'Any' use attribute is not a set of all the other attributes - it is in fact a distinct attribute on it's own.  Note that this is simply a convenience method, which itself calls identitiesForUse:.

                Note that this is different from the identities method, which returns every identity in the keychain, regardless of what that identity is capable of.  This method only returns identities which are capable of everything, and everything explicitely.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForAnyUse;

/*! @method identitiesForEncryption.
    @abstract Returns an array of identities in the receiver that are capable of encryption.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForEncryption;

/*! @method identitiesForDecryption
    @abstract Returns an array of identities in the receiver that are capable of decryption.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForDecryption;

/*! @method identitiesForSigning
    @abstract Returns an array of identities in the receiver that are capable of signing certificates.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForSigning;

/*! @method identitiesForVerifying
    @abstract Returns an array of identities in the receiver that are capable of verifying signatures.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForVerifying;

/*! @method identitiesForSignRecovery
    @abstract Returns an array of identities in the receiver that are capable of signing recovered items.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForSignRecovery;

/*! @method identitiesForVerifyRecovery
    @abstract Returns an array of identities in the receiver that are capable of verifying recovered items.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForVerifyRecovery;

/*! @method identitiesForWrapping
    @abstract Returns an array of identities in the receiver that are capable of wrapping keys.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForWrapping;

/*! @method identitiesForUnwrapping
    @abstract Returns an array of identities in the receiver that are capable of unwrapping keys.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForUnwrapping;

/*! @method identitiesForDeriving
    @abstract Returns an array of identities in the receiver that are capable of deriving keys.
    @discussion This is simply a convenience method, which itself calls identitiesForUse:.
    @result An array containing zero or more Identity's, or nil if an error occurs. */

- (NSArray*)identitiesForDeriving;

/*! @method createAndAddKeyPairWithAlgorithm:bitSize:publicUse:publicAttributes:privateUse:privateAttributes:access:
    @abstract Creates and returns a public-private key pair, adding them automatically to the receiver.
    @discussion Usually you will want to create public-private key pairs in a keychain, so that they inherit the security and authentication proceedures associated with a keychain.  This method provides a mechanism to do so conveniently.

                None of the parameters are optional.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h>cssmtype.h</a> for a list of possibilities the various parameters.
    @param alg The asymmetric algorithm to generate the key-pair for.  Supported algorithms are listed in <a href="http://developer.apple.com/techpubs/macosx/ReleaseNotes/Security.html">Apple's Security Release Notes</a>.
    @param size The logical size of the key pair in bits.  Refer to <a href="http://developer.apple.com/techpubs/macosx/ReleaseNotes/Security.html">Apple's Security Release Notes</a> for a list of valid sizes for each algorithm.  Note that you should generally default to the highest logical key size, where possible, as larger keys are more secure.
    @param pubUse A mask indicating how the public key of the key-pair may be used.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h>cssmtype.h</a> for valid masks.
    @param pubAttr A mask of attributes to be associated with the public key.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h>cssmtype.h</a> for valid attributes.  Generally, you'll want to specify just CSSM_KEYATTR_EXTRACTABLE.  Note that public keys cannot be sensitive, so do not try to use that attribute.
    @param privUse A mask indicating how the private key of the key-pair may be used.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h>cssmtype.h</a> for valid masks.
    @param privAttr A mask of attributes to be associated with the private key.  See <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h>cssmtype.h</a> for valid attributes.  If you specify CSSM_KEYATTR_EXTRACTABLE, you should generally always also specify CSSM_KEYATTR_SENSITIVE.
    @param acc The Access to be applied to the new key-pair.  This may be nil.
    @result An NSArray containing two Key instances, the public and private keys.  Returns nil if an error occurs. */

- (NSArray*)createAndAddKeyPairWithAlgorithm:(CSSM_ALGORITHMS)alg bitSize:(UInt32)size publicUse:(CSSM_KEYUSE)pubUse publicAttributes:(UInt32)pubAttr privateUse:(CSSM_KEYUSE)privUse privateAttributes:(UInt32)privAttr access:(Access*)acc;

/*! @method setAccess:
    @abstract Sets the Access for the receiver.
    @discussion The Access describes what you can and cannot do with the receiver, and it's contents, in detail.  See the documentation for Access.h and the Access class for more information.

                Note that this method is only available on 10.3 or later.  On earlier systems it will fail silently.
    @param access The new Access to apply to the receiver.  The existing one is taken out of effect and released. */

- (void)setAccess:(Access*)access;

/*! @method access
    @abstract Returns the Access for the receiver.
    @discussion The Access describes what you can and cannot do with the receiver, and it's contents, in detail.  See the documentation for Access.h and the Access class for more information.
    @result An Access instance, or nil if an error occurs. */

- (Access*)access;

/*! @method deleteCompletely
    @abstract Completely destroys a keychain, both in memory and on disk.
    @discussion WARNING - THIS REALLY DOES DELETE THE KEYCHAIN, COMPLETELY AND IRREVERSIBLY, INCLUDING ALL ITS CONTENTS.

                Unless you are dead certain you really want to do this, don't even think about using this method.  If you are vending a Keychain using Distributed Objects, you may wish to enforce a protocol which does not allow this method to be called. */

- (void)deleteCompletely;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method keychainRef
    @abstract Returns a SecKeychainRef representing the receiver.
    @discussion The returned SecKeychainRef is linked to the receiver, such that changes to it will reflect back on the receiver, and vice versa.  You should retain the result if you wish to keep it beyond the receiver's lifetime.
    @result The SecKeychainRef representing the receiver. */

- (SecKeychainRef)keychainRef;

@end


/*! @function defaultSetOfKeychains
    @abstract Returns a list of the default keychains for the current user.
    @discussion These default keychains are the ones that are used when doing a global search.  This may not encompass every keychain belonging to the current user, but may include keychains not belonging to the current user.  As such, it will not always return the same list as keychainsForUser(nil).
    @result An NSArray containing zero or more Keychain instances, or nil if an error occurs. */

NSArray* defaultSetOfKeychains(void);

/*! @function completeSetOfKeychains
    @abstract Returns a list of every keychain that can be found on the local machine.
    @discussion This function does it's best to locate every keychain, for every user and otherwise, on the local machine.  It does this by looking in all the standard locations.  It does not perform a comprehensive search, nor does it maintain any sort of managed list.  It generates the list each time it is called, which may be an expensive operation, so use it sparingly.

                Note that this method is not comprehensive - it will not find keychains on remote volumes, even if the current user's home directory is on a remote volume.  At time of writing there aren't many remote login mechanisms in OS X (10.2.6), so this isn't too much of a problem.  It is rumoured that 10.3 will bring with it many more user management options, and so this function will need to be updated appropriately.
    @result An NSArray containing zero or more Keychain instances, or nil in case of error. */

NSArray* completeSetOfKeychains(void);

/*! @function keychainsForUser
    @abstract Returns a list of keychains belonging to the current user.
    @discussion This method searches for all the keychains belonging to the current user, and returns them as an array of Keychain instances.  If the current user has no keychains, this returned array has no entries.
    @param username The short or full username of the user to query.  If this is nil, the current user is used.
    @result An NSArray of zero or more Keychain instances, or nil in case of error. */

NSArray* keychainsForUser(NSString *username);
