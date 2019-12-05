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

#import "KeychainPasswordStore.h"
#import <Security/Security.h>
#import <SecurityInterface/SFCertificatePanel.h>
#import <SecurityInterface/SFCertificateTrustPanel.h>
#import <SecurityInterface/SFChooseIdentityPanel.h>
#import <JavaNativeFoundation/JNFString.h>

#import "EMKeychainProxy.h"
#import "EMKeychainItem.h"

SecProtocolType convertToSecProtocolType(JNIEnv *env, jstring jProtocol)
{
    if([JNFJavaToNSString(env, jProtocol) isEqualTo: @"ftp"]) {
        return kSecProtocolTypeFTP;
    }
    if([JNFJavaToNSString(env, jProtocol) isEqualTo: @"ftps"]) {
        return kSecProtocolTypeFTPS;
    }
    if([JNFJavaToNSString(env, jProtocol) isEqualTo: @"sftp"]) {
        return kSecProtocolTypeSSH;
    }
    if([JNFJavaToNSString(env, jProtocol) isEqualTo: @"http"]) {
        return kSecProtocolTypeHTTP;
    }
    if([JNFJavaToNSString(env, jProtocol) isEqualTo: @"https"]) {
        return kSecProtocolTypeHTTPS;
    }
    return kSecProtocolTypeAny;
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_KeychainPasswordStore_getInternetPasswordFromKeychain(JNIEnv *env, jobject this, jstring jProtocol, 
																						  jint port, jstring jService,jstring jUsername) {
	
	EMInternetKeychainItem *keychainItem = [[EMKeychainProxy sharedProxy] internetKeychainItemForServer:JNFJavaToNSString(env, jService)
																						   withUsername:JNFJavaToNSString(env, jUsername)
																								   path:nil 
																								   port:port 
																							   protocol:convertToSecProtocolType(env, jProtocol)];
    return (*env)->NewStringUTF(env, [[keychainItem password] UTF8String]);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_KeychainPasswordStore_getPasswordFromKeychain(JNIEnv *env, jobject this, jstring jService, jstring jUsername) {
	EMGenericKeychainItem *keychainItem = [[EMKeychainProxy sharedProxy] genericKeychainItemForService:JNFJavaToNSString(env, jService)
																						  withUsername:JNFJavaToNSString(env, jUsername)];
    return (*env)->NewStringUTF(env, [[keychainItem password] UTF8String]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_KeychainPasswordStore_addInternetPasswordToKeychain(JNIEnv *env, jobject this, jstring jProtocol, jint port,
																					 jstring jService, jstring jUsername, jstring jPassword) {
	if(nil == [[EMKeychainProxy sharedProxy] addInternetKeychainItemForServer:JNFJavaToNSString(env, jService)
													   withUsername:JNFJavaToNSString(env, jUsername)
														   password:JNFJavaToNSString(env, jPassword) 
															   path:nil 
															   port:port
														   protocol:convertToSecProtocolType(env, jProtocol)]) {
       return NO;
   }
   return YES;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_KeychainPasswordStore_addPasswordToKeychain(JNIEnv *env, jobject this, jstring jService, jstring jUsername, jstring jPass)  {
	if(nil == [[EMKeychainProxy sharedProxy] addGenericKeychainItemForService:JNFJavaToNSString(env, jService)
													   withUsername:JNFJavaToNSString(env, jUsername)
														   password:JNFJavaToNSString(env, jPass)]) {
       return NO;
   }
   return YES;
}
