/*
 *  Copyright (c) 2003 Regents of The University of Michigan.
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

#include <Keychain.h>

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Login_getInternetPasswordFromKeychain(JNIEnv *env, 
																					   jobject this, 
																					   jstring jProtocol,
																					   jstring jServer, 
																					   jstring jAccount) {
    const char *protocol = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE); //todo
    const char *server = (*env)->GetStringUTFChars(env, jServer, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jAccount, JNI_FALSE);
    char *pass;
    UInt32 passLength;
	
    OSStatus status = SecKeychainFindInternetPassword(NULL, 
											strlen(server), 
											server, 
											0, 
											NULL, 
											strlen(account), 
											account, 
											0, 
											NULL, 
											0, 
											kSecProtocolTypeFTP, 
											kSecAuthenticationTypeDefault, 
											&passLength, 
											(void**)&pass, 
											NULL);
    
	(*env)->ReleaseStringUTFChars(env, jServer, server);
	(*env)->ReleaseStringUTFChars(env, jAccount, account);
	(*env)->ReleaseStringUTFChars(env, jProtocol, protocol);
	
	switch (status) {
		case noErr:
			// ...free the memory allocated in call to SecKeychainFindGenericPassword() above
			SecKeychainItemFreeContent (
										NULL, //No attribute data to release
										pass //Release data buffer allocated by SecKeychainFindGenericPassword
										);
			pass[passLength] = '\0';
			return (*env)->NewStringUTF(env, pass);
			break;
		case errSecItemNotFound:
			syslog(LOG_INFO, "Keychain item not found");
			return(NULL);
		case errSecAuthFailed:
			syslog(LOG_ERR, "Authorization failed.");
			return(NULL);
		case errSecNoDefaultKeychain:
		syslog(LOG_INFO, "No default Keychain!");
			return(NULL);
		default:
			syslog(LOG_ERR, "Unknown error");
			return(NULL);
    }
}


JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Login_getPasswordFromKeychain(JNIEnv *env, 
																			   jobject this, 
																			   jstring jService, 
																			   jstring jAccount) {
    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jAccount, JNI_FALSE);
    char *pass;
    UInt32 passLength;

	OSStatus status = SecKeychainFindGenericPassword(NULL,
											strlen(service), 
											service,
											strlen(account), 
											account, 
											&passLength,
											(void **)&pass, 
											NULL);
	
	(*env)->ReleaseStringUTFChars(env, jService, service);
	(*env)->ReleaseStringUTFChars(env, jAccount, account);

	switch (status) {
		case noErr:
			// ...free the memory allocated in call to SecKeychainFindGenericPassword() above
			SecKeychainItemFreeContent (
										NULL, //No attribute data to release
										pass //Release data buffer allocated by SecKeychainFindGenericPassword
										);
			pass[passLength] = '\0';
			return (*env)->NewStringUTF(env, pass);
		case errSecItemNotFound:
			syslog(LOG_INFO, "Keychain item not found");
			return(NULL);
		case errSecAuthFailed:
			syslog(LOG_ERR, "Authorization failed.");
			return(NULL);
		case errSecNoDefaultKeychain:
			syslog(LOG_INFO, "No default Keychain!");
			return(NULL);
		default:
			syslog(LOG_ERR, "Unknown error");
			return(NULL);
    }
}


JNIEXPORT void JNICALL Java_ch_cyberduck_core_Login_addPasswordToKeychain(JNIEnv *env, 
												   jobject this, 
												   jstring jService, 
												   jstring jAccount, 
												   jstring jPass) {

    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jAccount, JNI_FALSE);
    const char *pass = (*env)->GetStringUTFChars(env, jPass, JNI_FALSE);

	// http://sourceforge.net/projects/keychain/
	// SecKeychainAddGenericPassword() will enter new item into keychain, if item with attributes service and account don't already 
	// exist in keychain;  returns errSecDuplicateItem if the item already exists;  
	// uses strlen() and UTF8String in place of cStringLength and cString;  
	// passes NULL for &itemRef since SecKeychainItemRef isn't needed, and SecKeychainItemRef won't be returned 
	// in &itemRef if errSecDuplicateItem is returned (at least that's been my experience;  couldn't find this behavio(u)r documented)
		
	syslog(LOG_INFO, "Adding password to Keychain");
	OSStatus status = SecKeychainAddGenericPassword (NULL,
													 strlen(service), 
													 service,
													 strlen(account), 
													 account, 
													 strlen(pass),
													 (const void*)pass,
													 NULL
													 );
	
		// if we have a duplicate item error...
	if(status == errSecDuplicateItem) {
		syslog(LOG_INFO, "Duplicate keychain item");
		UInt32 existingPasswordLength;
		char * existingPassword;
		SecKeychainItemRef existingItem;
		
		// ...get the existing password and a reference to the existing keychain item, then...
		status = SecKeychainFindGenericPassword (NULL,
												 strlen(service), 
												 service,
												 strlen(account), 
												 account, 
												 &existingPasswordLength,
												 (void **)&existingPassword,
												 &existingItem
												 );
		
		syslog(LOG_INFO, "Updating keychain item");
		// ...modify the password for the existing keychain item;  (I'll admit to being mystified as to how this function works;  
		// how does it know that it's the password data that's being modified??;  anyway, it seems to work); and finally...
		// Answer: the data of a keychain item is what is being modified.  In the case of internet or generic passwords, 
		// the data is the password.  For a certificate, for example, the data is the certificate itself.
		status = SecKeychainItemModifyContent (existingItem,
											   NULL,
											   strlen(pass),
											   (const void*)pass
											   );
		// ...free the memory allocated in call to SecKeychainFindGenericPassword() above
		SecKeychainItemFreeContent(NULL, existingPassword);
		CFRelease(existingItem);
	}

	(*env)->ReleaseStringUTFChars(env, jService, service);
	(*env)->ReleaseStringUTFChars(env, jAccount, account);
	(*env)->ReleaseStringUTFChars(env, jPass, pass);
}