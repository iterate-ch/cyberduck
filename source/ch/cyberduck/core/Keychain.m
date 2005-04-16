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
    unichars = (*env)->GetStringChars(env, javaString, nil);
    if ((*env)->ExceptionOccurred(env)) {
        return @"";
    }
    converted = [NSString stringWithCharacters:unichars length:(*env)->GetStringLength(env, javaString)]; // auto-released
    (*env)->ReleaseStringChars(env, javaString, unichars);
    return converted;
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Keychain_getInternetPasswordFromKeychain(JNIEnv *env, jobject this, jstring jProtocol, jstring jService,jint jPort,jstring jAccount) {

    SecProtocolType protocol;
    const char *protocolString = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE);
	memcpy(&protocol, protocolString, 4);
	
	NSString *password = [[Keychain defaultKeychain] passwordForInternetServer:convertToNSString(env, jService) forAccount:convertToNSString(env, jAccount) port:(UInt16)jPort path:@"" inSecurityDomain:@"" protocol:protocol auth:kSecAuthenticationTypeDefault];

	return (*env)->NewStringUTF(env, [password UTF8String]);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Keychain_getPasswordFromKeychain(JNIEnv *env, jobject this, jstring jService, jstring jAccount) 
{
	NSString *password = [[Keychain defaultKeychain] passwordForGenericService:convertToNSString(env, jService) forAccount:convertToNSString(env, jAccount)]; 

	return (*env)->NewStringUTF(env, [password UTF8String]);
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addInternetPasswordToKeychain(JNIEnv *env, jobject this, jstring jProtocol,jstring jService,jint jPort,jstring jUsername, jstring jPassword
																				  ) 
{
    SecProtocolType protocol;
    const char *protocolString = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE);
	memcpy(&protocol, protocolString, 4);

	[[Keychain defaultKeychain] addInternetPassword:convertToNSString(env, jPassword) onServer:convertToNSString(env, jService) forAccount:convertToNSString(env, jUsername) port:(UInt16)jPort path:@"" inSecurityDomain:@"" protocol:protocol auth:kSecAuthenticationTypeDefault replaceExisting:YES];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addPasswordToKeychain(JNIEnv *env, jobject this, jstring jService, jstring jUsername, jstring jPass) 
{
	[[Keychain defaultKeychain] addGenericPassword:convertToNSString(env, jPass) onService:convertToNSString(env, jService) forAccount:convertToNSString(env, jUsername) replaceExisting:YES]; 
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Keychain_addCertificateToKeychain(JNIEnv *env, jobject this, jbyteArray jCertificate) 
{
	
	NSData *certData = [NSData dataWithBytes:jCertificate length:sizeof(jCertificate)];
	Certificate *certificate = [Certificate certificateWithData:certData type:CSSM_CERT_X_509v3 encoding:CSSM_CERT_ENCODING_BER];
	
	[[Keychain defaultKeychain] addCertificate:certificate];
}
