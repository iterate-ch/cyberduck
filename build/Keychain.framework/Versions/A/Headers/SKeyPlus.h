//
//  SKeyPlus.h
//  Keychain
//
//  Created by Wade Tregaskis on 26/01/05.
//
//  Copyright (c) 2005, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Cocoa/Cocoa.h>

#import <Security/cssmtype.h>
#import <Security/cssmapple.h>

#import <Keychain/CSSMModule.h>


/*! @header SKeyPlus
    @abstract Contains a generator and verifier class pair for applying the S/Key authentication algorithm.
    @discussion The S/Key algorithm is an ingenious way of performing reliable, secure authentication with two interesting properties:

                a) The passwords are one-time-only and effectively random.  Thus, assuming no man-in-the-middle attacks, the algorithm cannot be subverted (except by brute force) using traditional means - such as espionage, dictionary or similar password breaking attachs, etc.
                b) The verifier does not ever actually know what the current valid password(s) are.

                This makes it an excellent suit for many applications of both interactive and autonomous authentication.  The S/Key approach is used by numerous hardware authentication tokens.  It is unfortunately relatively rarely used by standard users, for tasks such as ssh/rlogin, HTTPS and others.  Hopefully, with these easy to use classes, that situation will change.

                The principle behind the S/Key algorithm is that one-way algorithms (hashes) are very, very hard to reverse.  The algorithm works by taking a base password, and applying the one-way hash to it a certain number of times.  The result is then used as a password.  The very first such password is actually the seed, from which you can create the verifier.  The verifier stores this seed, and uses it to determine if a given password is valid.  It does this by applying the one-way hash to the supposed password, and seeing if the result equals the seed.  If it does, it considers the password valid.  It replaces it's seed with the password, and thus awaits the next password in the sequence.

                So, the generator can find the next password by simply decrementing (by one) the number of hash iterations, and then performing those hashes starting from the original base password.  It can repeat this process until it's number of iterations reaches 1.  It does not permit less than one (i.e. zero) iterations - this would provide the original base password as one of the passwords, which is forbidden.  The reasoning for this is that the base password may be anything - perhaps something provided by a user.  [Ideally this is not the case, but we assume it may be]  Thus, it may not be cryptographically strong.  It may be susceptible to dictionary attacks, or guesses.  Thus, we do not ever want it to be a valid password itself.  Also, we do not want to divulge it - by confirming it as a valid password - to an attacker.  Doing so may allow the attacker to break other authentication schemes the user uses, if they use the same password in multiple places.

                The idea behind the SKeyPlusGenerator and SKeyPlusVerifier is that you can create them once, then store them - using NSCoding - throughout their life.  Once they expire, they can be permanently destroyed.  You should not try to reconstruct either one part way through a sequence.  Doing so implies that you are retaining sensitive data outside the generator and verifier, which introduces the possibility of that data being compromised.  If you lose either or both the objects, the correct procedure is to create a new pair. */


/*! @class SKeyPlusGeneratorr
    @abstract Represents a S/Key generator, which produces valid passwords in sequence.
    @discussion Refer to the header documentation for details of the S/Key algorithm, or one of the numerous excellent references available online. */

@interface SKeyPlusGenerator : NSObject <NSCoding> {
    CSSMModule *_CSPModule;
    CSSM_ALGORITHMS _algorithm;
    CSSM_DATA _base;
    unsigned int _usesRemaining,
                 _maximumUses;
    
    CSSM_DATA _current;
}

/*! @method generatorWithPassword:algorithm:maximumUses:
    @abstract Generates a new S/Key with a given base password and number of uses.
    @discussion The returned S/Key will be able to generate passwords from the sequence based on 'password', with up to 'uses' unique passwords generated.

                Note that unlike some other Keychain framework classes, this method will always return a new SKeyPlusGenerator instance, even if another one already exists with the same password & uses count.  There are many situations where you may want two independant copies, since using them changes them.

                Also note that you do not have to call nextPassword before using the returned SKeyPlusGenerator - it is automatically initialised to the first password in sequence.
    @param password The base password to use.  This will be copied and stored internally, and will never be revealed again.  Make sure you keep your own copy if you don't want to lose it.
    @param algorithm The algorithm to use.  Traditional S/Key uses SHA1, which can be specified as CSSM_ALGID_SHA1.  SHA1 is no longer considered a strong hash - it is recommended you move to SHA512 (CSSM_ALGID_SHA512).
    @param maximumUses The maximum number of uses permitted of the S/Key.  This must be the same for all users of the S/Key (e.g. client & server), otherwise they will not be able to match up to each other, even with the same base password.  You will not be able to retrieve this value from the returned instance, so make sure you keep a copy of it if necessary.
    @param CSPModule The CSP module to be used to perform the hashing.
    @result Returns a new SKeyPlusGenerator instance if successful, nil if an error occurs. */

+ (SKeyPlusGenerator*)generatorWithPassword:(NSData*)password algorithm:(CSSM_ALGORITHMS)algorithm maximumUses:(unsigned int)maximumUses module:(CSSMModule*)CSPModule;

/*! @method initWithPassword:algorithm:maximumUses:
    @abstract Initialises a new S/Key with a given base password and number of uses.
    @discussion The returned S/Key will be able to generate passwords from the sequence based on 'password', with up to 'uses' unique passwords generated.  You cannot call this method on an already initialised SKeyPlusGenerator instance - it will return nil, and leave the original unmodified.

                Note that unlike some other Keychain framework classes, this method will always return either nil or the receiver; it will not try to return any existing instance which may have the same initialisation parameters.  SKeyPlusGenerator's are always considered unequal to each other (this includes when used with methods such as isEqual:).

                Also note that you do not have to call nextPassword before using the returned SKeyPlusGenerator - it is automatically initialised to the first password in sequence.
    @param password The base password to use.  This will be copied and stored internally, and will never be revealed again.  Make sure you keep your own copy if you don't want to lose it.
    @param algorithm The algorithm to use.  Traditional S/Key uses SHA1, which can be specified as CSSM_ALGID_SHA1.  SHA1 is no longer considered a strong hash - it is recommended you move to SHA512 (CSSM_ALGID_SHA512).
    @param maximumUses The maximum number of uses permitted of the S/Key.  This must be the same for all users of the S/Key (e.g. client & server), otherwise they will not be able to match up to each other, even with the same base password.  You will not be able to retrieve this value from the returned instance (only the <i>current</i> number of uses remaining), so make sure you keep a copy of it if necessary.
    @param CSPModule The CSP module to be used to perform the hashing.
    @result Returns the receiver if successful, nil if an error occurs. */

- (SKeyPlusGenerator*)initWithPassword:(NSData*)password algorithm:(CSSM_ALGORITHMS)algorithm maximumUses:(unsigned int)maximumUses module:(CSSMModule*)CSPModule;

/*! @method usesRemaining
    @abstract Returns the number of uses remaining of the SKeyPlusGenerator.
    @discussion This number will always decrement over time, although is not guaranteed to change between calls (this method itself does not change the receiver).

                A return value of 0 indicates the receiver has expired and can no longer generate passwords.  The 'currentPassword' and 'nextPassword' methods will logically return nil, in this case.

                Note that this number of uses includes the current password - i.e. a return value of 1 means the next call to nextPassword will <b>not</b> generate a new password, but rather nil as the receiver will have expired.
    @result Returns the number of uses remaining. */

- (unsigned int)usesRemaining;

/*! @method seed
    @abstract The seed "password" from which to create a verifier.
    @discussion This is not a "password" per se - it will never be accepted as valid by a verifier.  It is used to seed a verifier.  It is the base password hashed maximumUses + 1 times.  You should only ever create a verifier with this seed - don't do naughty things with the normal passwords.
    @result Returns the seed from which to create a verifier.  This will not change for the life of the receiver. */

- (NSData*)seed;

/*! @method currentPassword
    @abstract Returns the current password for the current number of uses remaining.
    @discussion See the description on how S/Key systems work for more details.

                This method will not change the receiver - that is, it will not decrement the number of uses count.  You may call it any number of times and will receive the same password (provided of course you don't call nextPassword inbetween).
    @result Returns the current password, or nil if the receiver has expired. */

- (NSData*)currentPassword;

/*! @method nextPassword
    @abstract Generates and returns the next password in the receiver's sequence.
    @discussion This method will change the receiver, and should only be called at appropriate times.  It first decrements the number of uses remaining, and then generates the password for the new number of uses.  It then returns the new password.

                If the number of uses remaining drops to 0, the receiver expires and nil will be returned.  If the number of uses remaining is already 0 when this method is invoked, nil will be returned immediately.

                Note that you can retrieve the 'current' password - that is, the password returned by the most recent call to this method, using the currentPassword method.

                Also note that you do *not* have to call this method after initialisation - the class is automatically initialised to the first password in the sequence.
    @result Returns the next password in the sequence, or nil if the receiver has expired. */

- (NSData*)nextPassword;

/*! @method algorithm
    @abstract Returns the algorithm used by the S/Key generator.
    @discussion The algorithm can only be set at creation time - it makes no sense to try and change it afterwards.
    @result Returns the algorithm used by the receiver. */

- (CSSM_ALGORITHMS)algorithm;

@end


/*! @class SKeyPlusVerifier
    @abstract Reprersents a record of an S/Key password at some point in it's life.
    @discussion To verify the password, you don't actually need the original password - just the last one used.  From that you can determine whether the next password is valid or not simply by performing the iterative hash difference, which is the fundamental operation on which S/Key operates.

                In simplest terms, this means your password verifier does not actually know the current password!  It can only know whether any proposed password is valid or not.  This relies on the hash function used not being reversible, which is true to a high enough probability for modern hashes as to be considered secure.

                The SKeyPlusVerifier supports NSCoding (with or without keyed archiving) which allows you to save the "password" permanently. */

@interface SKeyPlusVerifier : NSObject <NSCoding> {
    CSSMModule *_CSPModule;
    
    unsigned int _usesRemaining;
    CSSM_DATA _lastPassword;
    CSSM_ALGORITHMS _algorithm;
    
    unsigned int _maximumNumberOfSkips;
}

/*! @method verifierWithSeed:algorithm:maximumUses:
    @abstract Creates a new autoreleased SKeyPlusVerifier for the given password, algorithm and maximum number of uses.
    @discussion Simply allocs a new SKeyPlusVerifier, passing the parameters to initWithSeed:algorithm:maximumUses:.  The returned object is autoreleased - you should retain it if you intend to keep it.

                Note that successful creation of an SKeyPlusVerifier does not guarantee successful operation of the verifier.  The implementation reserves all rights regarding lazy verification of parameters.
    @param seed The seed "password" to initialise the verifier with.  This is <i>not</i> a valid password in itself.
    @param algorithm The hash algorithm desired.  SHA1 is the algorithm used by traditional S/Key, but SHA1 is no longer considered secure.  SHA512 is recommended.  Note that the size of the passwords is dependent on the hash algorithm - 8 bytes with SHA1, 64 bytes with SHA512, etc.
    @param maximumUses The maximum number of passwords that can be used, in sequence, from the given seed.  This is determined by the generator.  It is <b>very important</b> that you don't allow more uses than the generator expects to provide - the system is designed so that in correct use the last password is the one before the original password used to create the generator.  To protect the privacy of that original password, which should never be revealed by the S/Key generator or verifier, ensure you adhere to this and related usage guidelines.

                       Note that it does not hurt to permit fewer uses in your verifier than your generator is prepared to provided.  There may be a performance penalty for doing so, but for most if not all S/Key applications, this penalty is insignificant.
    @param CSPModule The CSP module to be used to perform the hashing.
    @result Returns a new SKeyPlusVerifier instance.  The returned instance is autoreleased - you must retain it if you intend to keep it. */

+ (SKeyPlusVerifier*)verifierWithSeed:(NSData*)seed algorithm:(CSSM_ALGORITHMS)algorithm maximumUses:(unsigned int)maximumUses module:(CSSMModule*)CSPModule;

/*! @method initWithSeed:algorithm:maximumUses:
    @abstract Initialises the receiver for the given password, algorithm and maximum number of uses.
    @discussion Initialises the receiver with the given parameters.  Note that certain policy-related settings are not part of the initialiser - in particular the maximum number of skips.  Policy decisions may be made before, during or after initialisation, and may change during the lifetime of a given SKeyPlusVerifier, and thus can be modified at any time.  Attributes core to the S/Key operation - the seed, algorithm, maximum uses and uses remaining - are all immutable after initialisation.  This is to prevent programming errors (accidental or malicious).

                Note that successful initialisation does not guarantee successful operation of the verifier.  The implementation reserves all rights regarding lazy verification of parameters.
    @param seed The seed "password" to initialise the receiver with.  This is <i>not</i> a valid password in itself.
    @param algorithm The hash algorithm desired.  SHA1 is the algorithm used by traditional S/Key, but SHA1 is no longer considered secure.  SHA512 is recommended.  Note that the size of the passwords is dependent on the hash algorithm - 8 bytes with SHA1, 64 bytes with SHA512, etc.
    @param maximumUses The maximum number of passwords that can be used, in sequence, from the given seed.  This is determined by the generator.  It is <b>very important</b> that you don't allow more uses than the generator expects to provide - the system is designed so that in correct use the last password is the one before the original password used to create the generator.  To protect the privacy of that original password, which should never be revealed by the S/Key generator or verifier, ensure you adhere to this and related usage guidelines.

                       Note that it does not hurt to permit fewer uses in your verifier than your generator is prepared to provided.  There may be a performance penalty for doing so, but for most if not all S/Key applications, this penalty is insignificant.
    @param CSPModule The CSP module to be used to perform the hashing.
    @result Returns the receiver if initialisation is successful, nil otherwise (in which case the receiver is released). */

- (SKeyPlusVerifier*)initWithSeed:(NSData*)seed algorithm:(CSSM_ALGORITHMS)algorithm maximumUses:(unsigned int)maximumUses module:(CSSMModule*)CSPModule;

/*! @method setMaximumNumberOfSkips:
    @abstract Sets the maximum number of passwords that can be skipped in the sequence.
    @discussion It is often useful to permit the user a certain number of skips in the password sequence.  That is, if you permit for example one skip, then both the next expected password <i>and</i> the password after that are considered valid at the current time.  By default no skips are permitted, as the safest default, but typical values range from three to five.

                You should not make use of this feature needlessly.  It is typically only required if your generator makes the current password available only for a limited time - 60 seconds is typical with hardware-based tokens.  After that time, there is no way for the user to obtain that password again - they must move on to the next one.  To account for inevitable user or system error, it is wise to allow a few skips.  Otherwise, you'll find your generator is out of sequence with your verifier, and a new generator/verifier pair may have to be made.  Your users will not be impressed if this is a frequent event.

                But having said that, every additional skip decreases the security of the system, by enlarging the pool of valid passwords at any particular time.  If possible, design your system so that you do not need to use many - if any - skips, and keep the verifier policy closely tied to this.

                On a final note, remember that just because your user has missed a password or passwords, does not mean the verifier will not still accept them.  Failed verification attempts <b>do not</b> effect which passwords are presently valid.  Ensure you have appropriate policies and mechanisms in place to prevent effective brute force attacks against the verifier.  Putting a limit on the maximum number of attempts per time period is a very effective method - one per second is usually more than enough.
    @param maximumNumberOfSkips The maximum number of skips to permit.  Any value is acceptable - the verifier won't second guess you, even if you do provide some stupid value. */

- (void)setMaximumNumberOfSkips:(unsigned int)maximumNumberOfSkips;

/*! @method maximumNumberOfSkips
    @abstract Returns the maximum number of skips the verifier permits.
    @discussion See the documentation for setMaximumNumberOfSkips: for information about this parameter.
    @result Returns the maximum number of skips.  The default value is 0. */

- (unsigned int)maximumNumberOfSkips;

/*! @method verify:andUpdate:
    @abstract Returns whether or not a given password is valid, and optionally updates the receiver if it is.
    @discussion Simply tests if the given password is valid.  If it is, the "update" parameter is consulted - if YES, the receiver is updated.  Otherwise it's state does not change.  To be updated means to take the current, valid password as accepted and thus no longer valid - the verifier will then be looking for the next password(s) in the sequence.  If the maximum number of uses is reached, the receiver becomes permanently disabled - the result of this method will then always be NO, regardless of the parameters.

                There is no way to "reset" or otherwise re-use a verifier which has reached it's maximum number of uses.  At that point, the verifier is useless and should be destroyed.  You can check the number of uses remaining using the usesRemaining method.
    @param password The password to verify.  Should not be nil.
    @param update If YES, update the receiver appropriately if the given password is valid.  Otherwise, do not modify the receiver.
    @result Returns YES if the given password is valid, NO otherwise. */

- (BOOL)verify:(NSData*)password andUpdate:(BOOL)update;

/*! @method usesRemaining
    @abstract Returns the number of uses remaining in the receiver's password sequence.
    @discussion See the header documentation for explanation of how the algorithm works, and why there is a hard limit on the number of times a particular verifier can be used.

                Note that this is the most optimistic estimate of the number of uses remaining.  Every password that is skipped (if skipping is permitted) counts as used.  Thus, the minimum number of uses remaining is equal to this value divided by the maximum number of skips + 1 (rounded down).
    @result Returns the maximum number of uses remaining.  If 0, the receiver has expired and should be destroyed. */

- (unsigned int)usesRemaining;

@end
