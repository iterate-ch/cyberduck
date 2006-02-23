//
//  Identity.h
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
#import <Keychain/Keychain.h>
#import <Keychain/Certificate.h>
#import <Keychain/Key.h>

/*! @class Identity
    @abstract Represents an entity via a certificate for that entity, and the corresponding private key of that entity.
    @discussion This isn't currently used for anything.  I suspect there's something coming in a future version of the Security framework (possibly in Panther) which actually makes use of it. */

@interface Identity : NSCachedObject {
    SecIdentityRef identity;
    int error;
}

/*! @method identityWithIdentityRef:
    @abstract Creates and returns an Identity derived from the provided SecIdentityRef.
    @discussion This method caches each unique Identity instance, such that multiple calls to it with the same SecIdentityRef will return the same Identity instance.
    @param ident The SecIdentityRef from which to derive the returned instance.
    @result The existing instance, if one exists, or a newly created one otherwise.  If an error occurs, nil is returned. */

+ (Identity*)identityWithIdentityRef:(SecIdentityRef)ident;

/*! @method identityWithCertificate:privateKey:inKeychain:
    @abstract Adds a new identity (composed of a certificate and associated private key) to a particular keychain, and returns the new Identity resulting from this addition.
    @discussion An Identity is tied to the particular keychain in which it's certificate and private key reside.  Thus, an Identity cannot exist outside a keychain, and the same certificate and private key in two separate keychains will result in two separate Identity instance's, which are not considered in any way the same.  You may wish to check for duplicates manually.

                Note that this method does not verify that the subject's public key and the given private key match.  To do so may require performing some cryptographic operations, which may result in undesirable performance losses.  However, it is considered a bug that it does not verify the pairing, and so the behavior may change in future.

                Also note that there is no equivelant instance initializer for this class constructor.  This is because the Security framework currently requires such a silly way of adding Identities that the process would have to create a new instance anyway.  Thus, there's little point having an instance initializer that always returns a new instance.
    @param certificate The certificate for the identity.  This may be self-signed, or signed by some other person or authority.  The subject's public key should correspond to the private key passed to this method.
    @param privateKey The private key corresponding to the subject's public key in the given certificate.
    @param keychain The keychain in which to create the new Identity.
    @param label A user-readable label given to the Identity, that will be displayed as the name of both the certificate and the identity itself (note: the private key's label is used internally for another purpose, and is not considered user-readable).
    @result If successfully created and added, the new Identity instance is returned.  If an error occurs (e.g. the certificate and/or private key are already in the keychain) this method returns nil. */

+ (Identity*)identityWithCertificate:(Certificate*)certificate privateKey:(Key*)privateKey inKeychain:(Keychain*)keychain label:(NSString*)label;

/*! @method initWithIdentityRef:
    @abstract Initializes the receiver from the SecIdentityRef provided.
    @discussion This method keeps a cache of all unique Identity instances, so calling this a second time with the same SecIdentityRef will return the existing instance.  The receiver retains a copy of the SecIdentityRef for the duration of it's life.
    @param ident The SecIdentityRef to initialize the receiver from.
    @result If an existing instance has the same SecIdentityRef, the receiver is released and the existing instance returned.  Otherwise, the receiver is initialized from the SecIdentityRef provided.  If an error occurs, nil is returned. */

- (Identity*)initWithIdentityRef:(SecIdentityRef)ident;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a Identity using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (Identity*)init;

/*! @method certificate
    @abstract Returns the certificate of the identity.
    @result The certificate for the receiver. */

- (Certificate*)certificate;

/*! @method publicKey
    @abstract Returns the public key of the identity.
    @result The public key for the receiver. */

- (Key*)publicKey;

/*! @method privateKey
    @abstract Returns the private key of the identity.
    @result The private key for the receiver. */

- (Key*)privateKey;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method identityRef
    @abstract Returns a SecIdentityRef representing the receiver.
    @result A SecIdentityRef derived from the receiver, and linked to.  Changes to the returned object will reflect in the receiver, and vice versa. */

- (SecIdentityRef)identityRef;

@end
