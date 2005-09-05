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
#import <Keychain/Keychain.h>
#import <Keychain/KeychainSearch.h>

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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Keychain_getInternetPasswordFromKeychain(JNIEnv *env, jobject this, jstring jProtocol, jstring jService,jstring jAccount) {

    SecProtocolType protocol;
    const char *protocolString = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE);
	memcpy(&protocol, protocolString, 4);
	
	NSString *password = [[Keychain defaultKeychain] passwordForInternetServer:convertToNSString(env, jService) forAccount:convertToNSString(env, jAccount) port:0 path:@"" inSecurityDomain:@"" protocol:protocol auth:kSecAuthenticationTypeDefault];

	return (*env)->NewStringUTF(env, [password UTF8String]);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Keychain_getPasswordFromKeychain(JNIEnv *env, jobject this, jstring jService, jstring jAccount) 
{
	NSString *password = [[Keychain defaultKeychain] passwordForGenericService:convertToNSString(env, jService) forAccount:convertToNSString(env, jAccount)]; 

	return (*env)->NewStringUTF(env, [password UTF8String]);
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addInternetPasswordToKeychain(JNIEnv *env, jobject this, jstring jProtocol,jstring jService,jstring jUsername, jstring jPassword
																				  ) 
{
    SecProtocolType protocol;
    const char *protocolString = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE);
	memcpy(&protocol, protocolString, 4);

	[[Keychain defaultKeychain] addInternetPassword:convertToNSString(env, jPassword) onServer:convertToNSString(env, jService) forAccount:convertToNSString(env, jUsername) port:0 path:@"" inSecurityDomain:@"" protocol:protocol auth:kSecAuthenticationTypeDefault replaceExisting:YES];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addPasswordToKeychain(JNIEnv *env, jobject this, jstring jService, jstring jUsername, jstring jPass) 
{
	[[Keychain defaultKeychain] addGenericPassword:convertToNSString(env, jPass) onService:convertToNSString(env, jService) forAccount:convertToNSString(env, jUsername) replaceExisting:YES]; 
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addCertificateToKeychain(JNIEnv *env, jobject this, jbyteArray jCertificate) 
{
	jbyte *certByte = (*env)->GetByteArrayElements(env, jCertificate, NULL);
	
	NSData *certData = [NSData dataWithBytes:certByte length:(*env)->GetArrayLength(env, jCertificate)];
	Certificate *certificate = [Certificate certificateWithData:certData type:CSSM_CERT_X_509v3 encoding:CSSM_CERT_ENCODING_DER];
	
	(*env)->ReleaseByteArrayElements(env, jCertificate, certByte, 0);
	[[Keychain defaultKeychain] addCertificate:certificate];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Keychain_hasCertificate (JNIEnv * env, jobject this, jbyteArray jCertificate)
{
	jbyte *certByte = (*env)->GetByteArrayElements(env, jCertificate, NULL);
	
	NSData *certData = [NSData dataWithBytes:certByte length:(*env)->GetArrayLength(env, jCertificate)];
	Certificate *certificate = [Certificate certificateWithData:certData type:CSSM_CERT_X_509v3 encoding:CSSM_CERT_ENCODING_DER];
	
	(*env)->ReleaseByteArrayElements(env, jCertificate, certByte, 0);
	
	KeychainSearch *search = [KeychainSearch keychainSearchWithKeychains:[NSArray arrayWithObject:[Keychain defaultKeychain]]];
	NSArray *certificates = [search certificateSearchResults];
    if (certificates) {
		NSObject *item = nil;
        NSEnumerator *enumerator = [certificates objectEnumerator];
        while (item = [enumerator nextObject]) {
			if([item isKindOfClass:[KeychainItem class]]) {
				KeychainItem *result = (KeychainItem*)item;
				if([result isCertificate]) {
					if([[result certificate] isEqualToCertificate:certificate]) {
						return TRUE;
					}
				}
			}
		}
	}
	return FALSE;
}
