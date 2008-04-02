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

NSArray* Java_ch_cyberduck_core_Keychain_createCertificatesFromData(JNIEnv *env, jobjectArray jCertificates)
{
	OSStatus err;
    NSMutableArray *result = [NSMutableArray arrayWithCapacity:(*env)->GetArrayLength(env, jCertificates)];
	int i;
    for(i = 0; i < (*env)->GetArrayLength(env, jCertificates); i++) {
        jbyteArray jCertificate = (jbyteArray)(*env)->GetObjectArrayElement(env, jCertificates, i);
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
    	SecCertificateRef certificateRef = NULL;
        err = SecCertificateCreateFromData(cssmData, CSSM_CERT_X_509v3, CSSM_CERT_ENCODING_DER, &certificateRef);
		if(err != noErr) {
			NSLog(@"Error creating certificate from data");
			continue;
		}
        [result addObject:(id)certificateRef];
    }
    return result;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Keychain_isTrusted (JNIEnv *env, jobject this, jstring jHostname, jobjectArray jCertificates)
{
	OSStatus err;
	NSArray *certificates = Java_ch_cyberduck_core_Keychain_createCertificatesFromData(env, jCertificates);
	// Adds a certificates to a keychain.
	int i;
	for(i = 0; i < [certificates count]; i++) {
        SecCertificateRef certificateRef = (SecCertificateRef)[certificates objectAtIndex:i];
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
    }
	// Creates a search object for finding policies.
	SecPolicySearchRef searchRef = NULL;
	err = SecPolicySearchCreate(CSSM_CERT_X_509v3, &CSSMOID_APPLE_TP_SSL, NULL, &searchRef);
	if(err != noErr) {
		return FALSE;
	}
	// Retrieves a policy object for the next policy matching specified search criteria.
	SecPolicyRef policyRef = NULL;
	err = SecPolicySearchCopyNext(searchRef, &policyRef);
	if(err != noErr) {
		if(searchRef) {
			CFRelease(searchRef);
		}
		return FALSE;
	}
	if(searchRef) {
		CFRelease(searchRef);
	}
	NSString *hostname = convertToNSString(env, jHostname);
	CSSM_APPLE_TP_SSL_OPTIONS ssloptions = {
		.Version = CSSM_APPLE_TP_SSL_OPTS_VERSION,
		.ServerNameLen = [hostname length]+1,
		.ServerName = [hostname cString],
		.Flags = 0
	};

	CSSM_DATA customCssmData = {
		.Length = sizeof(ssloptions),
		.Data = (uint8*)&ssloptions
	};
	err = SecPolicySetValue(policyRef, &customCssmData);
	// Creates a trust management object based on certificates and policies.
	SecTrustRef trustRef = NULL;
	err = SecTrustCreateWithCertificates((CFArrayRef)certificates, policyRef, &trustRef);
	if(err != noErr) {
		if(policyRef) {
			CFRelease(policyRef);
		}
		return FALSE;
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
	CFArrayRef certChain;
	CSSM_TP_APPLE_EVIDENCE_INFO *statusChain;
	err = SecTrustGetResult(trustRef, &trustResult, &certChain, &statusChain);
//       CSSM_CERT_STATUS_EXPIRED            = 0x00000001,
//       CSSM_CERT_STATUS_NOT_VALID_YET      = 0x00000002,
//       CSSM_CERT_STATUS_IS_IN_INPUT_CERTS  = 0x00000004,
//       CSSM_CERT_STATUS_IS_IN_ANCHORS      = 0x00000008,
//       CSSM_CERT_STATUS_IS_ROOT            = 0x00000010,
//       CSSM_CERT_STATUS_IS_FROM_NET        = 0x00000020,
	SFCertificateTrustPanel *panel = [[SFCertificateTrustPanel alloc] init];
	if(err == noErr) {
		if ([panel respondsToSelector:@selector(setInformativeText:)]) {
			if((statusChain->StatusBits & CSSM_CERT_STATUS_EXPIRED) == CSSM_CERT_STATUS_EXPIRED) {
				[panel setInformativeText:[NSString stringWithFormat:NSLocalizedStringFromTable(@"The certificate for this server has expired. You might be connecting to a server that is pretending to be \u201c%@\u201d which could put your confidential information at risk. Would you like to connect to the server anyway?", @"Keychain", @""), hostname]];
			}
			else if((statusChain->StatusBits & CSSM_CERT_STATUS_NOT_VALID_YET) == CSSM_CERT_STATUS_NOT_VALID_YET) {
				[panel setInformativeText:[NSString stringWithFormat:NSLocalizedStringFromTable(@"The certificate for this server is not yet valid. You might be connecting to a server that is pretending to be \u201c%@\u201d which could put your confidential information at risk. Would you like to connect to the server anyway?", @"Keychain", @""), hostname]];
			}
			else if((statusChain->StatusBits & CSSM_CERT_STATUS_IS_ROOT) == CSSM_CERT_STATUS_IS_ROOT
			            && (statusChain->StatusBits & CSSM_CERT_STATUS_IS_IN_ANCHORS) != CSSM_CERT_STATUS_IS_IN_ANCHORS) {
				[panel setInformativeText:[NSString stringWithFormat:NSLocalizedStringFromTable(@"The certificate for this server was signed by an unknown certifying authority. You might be connecting to a server that is pretending to be \u201c%@\u201d which could put your confidential information at risk. Would you like to connect to the server anyway?", @"Keychain", @""), hostname]];
			}
			else if((statusChain->StatusBits & CSSM_CERT_STATUS_IS_IN_ANCHORS) != CSSM_CERT_STATUS_IS_IN_ANCHORS) {
				[panel setInformativeText:[NSString stringWithFormat:NSLocalizedStringFromTable(@"The certificate for this server is invalid. You might be connecting to a server that is pretending to be \u201c%@\u201d which could put your confidential information at risk. Would you like to connect to the server anyway?", @"Keychain", @""), hostname]];
			}
		}
	}
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

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Keychain_displayCertificates (JNIEnv *env, jobject this, jobjectArray jCertificates)
{
	NSArray *certificates = Java_ch_cyberduck_core_Keychain_createCertificatesFromData(env, jCertificates);
	SFCertificatePanel *panel = [[SFCertificatePanel alloc] init];
	if([panel respondsToSelector:@selector(setShowsHelp:)]) {
		[panel setShowsHelp:NO];
	}
	int result = [panel runModalForCertificates:certificates showGroup:YES];
	if(result == NSOKButton) {
		return TRUE;
	}
	return FALSE;
}