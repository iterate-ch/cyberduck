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
																					   jstring jService,
																					   jshort jPort,
																					   jstring jAccount) {
    SecProtocolType protocol;
    const char *protocolString = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE);
	memcpy(&protocol, protocolString, 4);
    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jAccount, JNI_FALSE);
	UInt16 port = (UInt16)jPort;
    char *pass;
    UInt32 passLength;
	
    OSStatus status = SecKeychainFindInternetPassword(NULL, 
											strlen(service), 
											service, 
											0, 
											NULL, 
											strlen(account), 
											account, 
											0, 
											NULL, 
											port, 
											protocol, 
											kSecAuthenticationTypeDefault, 
											&passLength, 
											(void**)&pass, 
											NULL);
    
	(*env)->ReleaseStringUTFChars(env, jService, service);
	(*env)->ReleaseStringUTFChars(env, jAccount, account);
	(*env)->ReleaseStringUTFChars(env, jProtocol, protocolString);
	
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


JNIEXPORT void JNICALL Java_ch_cyberduck_core_Login_addInternetPasswordToKeychain(JNIEnv *env, 
																				  jobject this, 
																				  jstring jProtocol,
																				  jstring jService,
																				  jshort jPort,
																				  jstring jUsername, 
																				  jstring jPassword
																				  ) 
{
    SecProtocolType protocol;
    const char *protocolString = (*env)->GetStringUTFChars(env, jProtocol, JNI_FALSE);
	memcpy(&protocol, protocolString, 4);
    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *user = (*env)->GetStringUTFChars(env, jUsername, JNI_FALSE);
    const char *pass = (*env)->GetStringUTFChars(env, jPassword, JNI_FALSE);
	UInt16 port = (UInt16)jPort;
			
	syslog(LOG_INFO, "Adding password to Keychain");
	OSStatus status = SecKeychainAddInternetPassword (
													  NULL, // default keychain
													  strlen(service), // server name length
													  service, // server name
													  0,//strlen(domain), // security domain length
													  NULL,//domain, // security domain
													  strlen(user), // account name length
													  user, // account name
													  0, // path length
													  NULL, // path
													  port, // port
													  protocol,//kSecProtocolTypeFTP, // protocol
													  kSecAuthenticationTypeDefault, // authentication type
													  strlen(pass), // password length
													  pass, // password
													  NULL // item ref
													  );
	// if we have a duplicate item error...
	if(status == errSecDuplicateItem) {
		syslog(LOG_INFO, "Duplicate keychain item");
		UInt32 existingPasswordLength;
		char * existingPassword;
		SecKeychainItemRef existingItem;
		
		// ...get the existing password and a reference to the existing keychain item, then...
		status = SecKeychainFindInternetPassword(NULL, 
												 strlen(service), //hostname length
												 service, //hostname
												 0, //security domain length
												 NULL, //security domain
												 strlen(user), //username length
												 user, //username
												 0, //path length
												 NULL, //path
												 port, // port
												 protocol, //protocol (4 chars)
												 kSecAuthenticationTypeDefault, 
												 &existingPasswordLength, 
												 (void **)&existingPassword,
												 &existingItem);
		
		syslog(LOG_INFO, "Updating keychain item");
		status = SecKeychainItemModifyContent (existingItem,
											   NULL,
											   strlen(pass),
											   (const void*)pass
											   );
		// ...free the memory allocated in call to SecKeychainFindGenericPassword() above
		SecKeychainItemFreeContent(NULL, existingPassword);
		CFRelease(existingItem);
	}	

	(*env)->ReleaseStringUTFChars(env, jProtocol, protocolString);
	(*env)->ReleaseStringUTFChars(env, jService, service);
	(*env)->ReleaseStringUTFChars(env, jUsername, user);
	(*env)->ReleaseStringUTFChars(env, jPassword, pass);
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Login_addPasswordToKeychain(JNIEnv *env, 
												   jobject this, 
												   jstring jService, 
												   jstring jUsername, 
												   jstring jPass) {

    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jUsername, JNI_FALSE);
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
	(*env)->ReleaseStringUTFChars(env, jUsername, account);
	(*env)->ReleaseStringUTFChars(env, jPass, pass);
}