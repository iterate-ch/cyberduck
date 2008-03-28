/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#import "Keychain.h"

// Simple utility to convert java strings to NSStrings
NSString *convertToNSString(JNIEnv *env, jstring javaString)
{
    NSString *converted = nil;
    const jchar *unichars = NULL;
    if (javaString == NULL) {
        return nil; 
    }                   
    unichars = (*env)->GetStringChars(env, javaString, NULL);
    if ((*env)->ExceptionOccurred(env)) {
        return @"";
    }
    converted = [NSString stringWithCharacters:unichars length:(*env)->GetStringLength(env, javaString)]; // auto-released
    (*env)->ReleaseStringChars(env, javaString, unichars);
    return converted;
}

SecProtocolType convertToSecProtocolType(JNIEnv *env, jstring jProtocol)
{
    if([convertToNSString(env, jProtocol) isEqualTo: @"ftp"]) {
        return kSecProtocolTypeFTP;
    }
    if([convertToNSString(env, jProtocol) isEqualTo: @"ftps"]) {
        return kSecProtocolTypeFTPS;
    }
    if([convertToNSString(env, jProtocol) isEqualTo: @"sftp"]) {
        return kSecProtocolTypeSSH;
    }
    if([convertToNSString(env, jProtocol) isEqualTo: @"http"]) {
        return kSecProtocolTypeHTTP;
    }
    if([convertToNSString(env, jProtocol) isEqualTo: @"https"]) {
        return kSecProtocolTypeHTTPS;
    }
    return kSecProtocolTypeAny;
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Keychain_getInternetPasswordFromKeychain(JNIEnv *env, jobject this, jstring jProtocol, jstring jService,jstring jAccount)
{
    NSString *password = [[Keychain defaultKeychain] passwordForInternetServer:convertToNSString(env, jService)
                                                                    forAccount:convertToNSString(env, jAccount)
                                                                          port:0
                                                                          path:@""
                                                              inSecurityDomain:@""
                                                                      protocol:convertToSecProtocolType(env, jProtocol)
                                                                          auth:kSecAuthenticationTypeDefault];

    return (*env)->NewStringUTF(env, [password UTF8String]);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Keychain_getPasswordFromKeychain(JNIEnv *env, jobject this, jstring jService, jstring jAccount)
{
    NSString *password = [[Keychain defaultKeychain] passwordForGenericService:convertToNSString(env, jService)
                                                                    forAccount:convertToNSString(env, jAccount)];

    return (*env)->NewStringUTF(env, [password UTF8String]);
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addInternetPasswordToKeychain(JNIEnv *env, jobject this, jstring jProtocol, jint port, jstring jService, jstring jUsername, jstring jPassword
                                                                                  ) 
{
    [[Keychain defaultKeychain] addInternetPassword:convertToNSString(env, jPassword)
                                           onServer:convertToNSString(env, jService)
                                         forAccount:convertToNSString(env, jUsername)
                                               port:port
                                               path:@""
                                   inSecurityDomain:@""
                                           protocol:convertToSecProtocolType(env, jProtocol)
                                               auth:kSecAuthenticationTypeDefault
                                    replaceExisting:YES];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addPasswordToKeychain(JNIEnv *env, jobject this, jstring jService, jstring jUsername, jstring jPass) 
{
    [[Keychain defaultKeychain] addGenericPassword:convertToNSString(env, jPass) onService:convertToNSString(env, jService) forAccount:convertToNSString(env, jUsername) replaceExisting:YES];
}

OSStatus Java_ch_cyberduck_core_Keychain_createCertificateFromData(JNIEnv *env, jbyteArray jCertificate, SecCertificateRef *certificateRef) 
{
	jbyte *certByte = (*env)->GetByteArrayElements(env, jCertificate, NULL);
    // Creates a certificate object based on the specified data, type, and encoding.
    NSData *certData = [NSData dataWithBytes:certByte length:(*env)->GetArrayLength(env, jCertificate)];
	(*env)->ReleaseByteArrayElements(env, jCertificate, certByte, 0);
	CSSM_DATA *cssmData = NULL;
    if (certData) {
        cssmData = (CSSM_DATA*)malloc(sizeof(CSSM_DATA));
        cssmData->Length = [certData length];
        cssmData->Data = (uint8*)malloc(cssmData->Length);
        [certData getBytes:(char*)(cssmData->Data)];
    }
    return SecCertificateCreateFromData(cssmData, CSSM_CERT_X_509v3, CSSM_CERT_ENCODING_DER, certificateRef);
} 

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Keychain_isTrusted (JNIEnv *env, jobject this, jbyteArray jCertificate)
{
	OSStatus err;

	SecCertificateRef certificateRef = NULL;
	err = Java_ch_cyberduck_core_Keychain_createCertificateFromData(env, jCertificate, &certificateRef);
	if(err != noErr) {
		return FALSE;
	}
	// Adds a certificate to a keychain.
	err = SecCertificateAddToKeychain(certificateRef, NULL);
	switch(err) 
	{
		case errSecDuplicateItem:
			// The function returns errSecDuplicateItem and does not add another copy to the keychain.
			// The function looks at the certificate data, not at the certificate object, to
			// determine whether the certificate is a duplicate. It considers two certificates to be
			// duplicates if they have the same primary key attributes.
			break;
		default:
			NSLog(@"Error adding certificate to Keychain");
			break;
	}
	// Creates a search object for finding policies.
	SecPolicySearchRef searchRef = NULL;
	err = SecPolicySearchCreate(CSSM_CERT_X_509v3, &CSSMOID_APPLE_TP_SSL, NULL, &searchRef);
	if(err != noErr) {
		if(certificateRef) {
			CFRelease(certificateRef);
		}
		return FALSE;
	}
	// Retrieves a policy object for the next policy matching specified search criteria.
	SecPolicyRef policyRef = NULL;
	err = SecPolicySearchCopyNext(searchRef, &policyRef);
	if(err != noErr) {
		if(certificateRef) {
			CFRelease(certificateRef);
		}
		if(searchRef) {
			CFRelease(searchRef);
		}
		return FALSE;
	}
	if(searchRef) {
		CFRelease(searchRef);
	}
	// Creates a trust management object based on certificates and policies.
	SecTrustRef trustRef = NULL;
	SecCertificateRef evalCertArray[1] = {certificateRef};
    CFArrayRef certificateArrayRef = CFArrayCreate ((CFAllocatorRef) NULL, (void *)evalCertArray, 1, &kCFTypeArrayCallBacks);
	err = SecTrustCreateWithCertificates(certificateArrayRef, policyRef, &trustRef);
	if(err != noErr) {
		if(policyRef) {
			CFRelease(policyRef);
		}
		if(certificateRef) {
			CFRelease(certificateRef);
		}
		return FALSE;
	}
	if(certificateRef) {
		CFRelease(certificateRef);
	}
	SecTrustResultType trustResult;
	err = SecTrustEvaluate(trustRef, &trustResult);
	if(err != noErr) {
		if(policyRef) {
			CFRelease(policyRef);
		}
		if(trustRef) {
			CFRelease(trustRef);
		}
		return FALSE;
	}
	// kSecTrustResultProceed -> trust ok, go right ahead
	// kSecTrustResultConfirm -> trust ok, but user asked (earlier) that you check with him before proceeding
	// kSecTrustResultDeny -> trust ok, but user previously said not to trust it anyway
	// kSecTrustResultUnspecified -> trust ok, user has no particular opinion about this
	// kSecTrustResultRecoverableTrustFailure -> trust broken, perhaps argue with the user
	// kSecTrustResultFatalTrustFailure -> trust broken, user can't fix it
	// kSecTrustResultOtherError -> something failed weirdly, abort operation
	// kSecTrustResultInvalid -> logic error; fix your program (SecTrust was used incorrectly)
	switch(trustResult) {
		case kSecTrustResultProceed:
			// Accepted by user keychain setting
			if(policyRef) {
				CFRelease(policyRef);
			}
			if(trustRef) {
				CFRelease(trustRef);
			}
			return TRUE;
		default:
			break;
	}
	SFCertificateTrustPanel *panel = [[SFCertificateTrustPanel alloc] init];
	if([panel respondsToSelector:@selector(setAlternateButtonTitle:)]) {
		[panel setAlternateButtonTitle:NSLocalizedString(@"Disconnect", @"")];
	}
	if([panel respondsToSelector:@selector(setPolicies:)]) {
		[panel setPolicies:(id)policyRef];
	}
	if([panel respondsToSelector:@selector(setShowsHelp:)]) {
		[panel setShowsHelp:YES];
	}
	// Displays a modal panel that shows the results of a certificate trust evaluation and
	// that allows the user to edit trust settings.
	int result = [panel runModalForTrust:trustRef message:nil];
	[panel release];
	if(policyRef) {
		CFRelease(policyRef);
	}
	if(trustRef) {
		CFRelease(trustRef);
	}
	if(NSOKButton == result) {
		return TRUE;
	}
    return FALSE;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Keychain_displayCertificate (JNIEnv *env, jobject this, jbyteArray jCertificate)
{
	OSStatus err;

	SecCertificateRef certificateRef = NULL;
	err = Java_ch_cyberduck_core_Keychain_createCertificateFromData(env, jCertificate, &certificateRef);
	if(err != noErr) {
		return FALSE;
	}
	SFCertificatePanel *panel = [[SFCertificatePanel alloc] init];
	if([panel respondsToSelector:@selector(setShowsHelp:)]) {
		[panel setShowsHelp:NO];
	}
	int result = [panel runModalForCertificates:[NSArray arrayWithObject:(id)certificateRef] showGroup:YES];
	if(certificateRef) {
		CFRelease(certificateRef);
	}
	if(result == NSOKButton) {
		return TRUE;
	}
	return FALSE;
}