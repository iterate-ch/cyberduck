//
//  Policy.h
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

@interface Policy : NSCachedObject {
    SecPolicyRef policy;
    int error;
}

/*! @method policyWithPolicyRef:
    @abstract Returns a Policy instance for a given SecPolicyRef.
    @discussion There can only ever be one instance of a Policy for each unique SecPolicyRef.  Thus, this method may return an existing instance.  If no suitable instance already exists, it will be created and returned.
    @param poli The SecPolicyRef to wrap a Policy instance around.
    @result Returns the unique Policy instance representing the given SecPolicyRef, or nil if an error occurs. */

+ (Policy*)policyWithPolicyRef:(SecPolicyRef)poli;

/*! @method initWithPolicyRef:
    @abstract Initialises the receiver around a given SecPolicyRef.
    @discussion Since there can only ever be one Policy instance for each unique SecPolicyRef, this method is not guaranteed to return the receiver; if there already exists a Policy instance for the given SecPolicyRef, the receiver is released and the existing instance returned instead.
    @param poli The SecPolicyRef to wrap the receiver around.
    @result Returns the unique Policy instance representing the given SecPolicyRef, which may be the receiver or an existing Policy instance for the given SecPolicyRef, if available. */

- (Policy*)initWithPolicyRef:(SecPolicyRef)poli;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a Policy using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (Policy*)init;

/*! @method type
    @abstract Returns the type of policy the receiver is.
    @discussion Refer to the documentation for the allPolicies method for a list of currently known policy types.
    @result Returns the OID (as an NSData instance - see UtilitySupport.h for conversion functions between NSData & CSSM_OID (CSSM_DATA)) identifying the type of policy the receiver is (or NULL if an error occurs). */

- (NSData*)type;

/*! @method data
    @abstract Returns the data contained in the receiver, as an opaque blob.
    @discussion The format of the receiver's data depends on it's policy type.  At time of writing there is no facility for interpretting this automatically.
    @result The opaque data of the receiver, or nil if an error occurs. */

- (NSData*)data;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method policyRef
    @abstract Returns the underlying SecPolicyRef that Policy instance represents.
    @discussion While you can manipulate the returned reference to some degree, be wary of how it may effect the Policy instance.  Do not manually release the returned reference.  Conversely, do not use the returned reference beyond the lifetime of the owning Policy instance unless you CFRetain it.
    @result Returns the SecPolicyRef underlying the receiver. */

- (SecPolicyRef)policyRef;
   
@end

/*! @function allPolicies
    @abstract Finds all policies of a particular type that apply to a particular types of certificates.
    @discussion You can use this function to find Policy instances (at time of writing there is no way to define your own policies).
    @param certificateType The type of certificate the policies should apply for.
    @param policyType The type of policies to find.  At time of writing recognised values include:

                        CSSMOID_APPLE_X509_BASIC
                        CSSMOID_APPLE_TP_SSL
                        CSSMOID_APPLE_TP_LOCAL_CERT_GEN
                        CSSMOID_APPLE_TP_CSR_GEN
                        CSSMOID_APPLE_TP_REVOCATION_CRL
                        CSSMOID_APPLE_TP_REVOCATION_OCSP
                        CSSMOID_APPLE_TP_SMIME
                        CSSMOID_APPLE_TP_EAP
                        CSSMOID_APPLE_TP_CODE_SIGN
                        CSSMOID_APPLE_TP_IP_SEC
                        CSSMOID_APPLE_TP_ICHAT
    @result Returns an array containing zero or more Policy's, or nil if an error occurs. */

NSArray *allPolicies(CSSM_CERT_TYPE certificateType, const CSSM_OID *policyType);
