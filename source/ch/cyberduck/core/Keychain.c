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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Login_getPasswordFromKeychain(JNIEnv *env, 
																			   jobject this, 
																			   jstring jService, 
																			   jstring jAccount) {
    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jAccount, JNI_FALSE);
    char *pass = (char *)calloc(256, 1);;
	UInt32 passLength = 256;

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
			//Free the data allocated by SecKeychainFindGenericPassword:
			SecKeychainItemFreeContent (
										NULL,           //No attribute data to release
										pass    //Release data buffer allocated by SecKeychainFindGenericPassword
										);
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


/*
char *getPwdFromKeychain(const char *service, 
						 const char *account,
						 SecKeychainItemRef itemRef) {
    OSStatus status;
    UInt32 passLength = 0;
    char *pass;
	
	status = SecKeychainFindGenericPassword(NULL,
											strlen(service), 
											service,
											strlen(account), 
											account, 
											&passLength,
											(void **)&pass, 
											&itemRef);
		
	switch (status) {
		case noErr:
			//Free the data allocated by SecKeychainFindGenericPassword:
			SecKeychainItemFreeContent (
										NULL,           //No attribute data to release
										pass    //Release data buffer allocated by SecKeychainFindGenericPassword
										);
			return(pass);
			break;
		case errSecItemNotFound:
			syslog(LOG_INFO, "Keychain item not found");
			//free(pass);
			return(NULL);
		case errSecAuthFailed:
			syslog(LOG_ERR, "Authorization failed.");
			//free(pass);
			return(NULL);
		case errSecNoDefaultKeychain:
			syslog(LOG_INFO, "No default Keychain!");
			//free(pass);
			return(NULL);
		default:
			syslog(LOG_ERR, "Unknown error");
			//free(pass);
			return(NULL);
    }
    
//    pass[ passLength ] = '\0';
//    return(pass);
}
*/

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Login_addPasswordToKeychain(JNIEnv *env, 
												   jobject this, 
												   jstring jService, 
												   jstring jAccount, 
												   jstring jPass) {

    const char *service = (*env)->GetStringUTFChars(env, jService, JNI_FALSE);
    const char *account = (*env)->GetStringUTFChars(env, jAccount, JNI_FALSE);
    const char *pass = (*env)->GetStringUTFChars(env, jPass, JNI_FALSE);
	UInt32 passLength = 0;

	SecKeychainItemRef itemRef = nil;
				
	OSStatus status = SecKeychainFindGenericPassword(NULL,//default keychain
														 strlen(service), 
														 service,
														 strlen(account), 
														 account, 
														 &passLength,
														 (void **)&pass, 
														 &itemRef);
				
	switch (status) {
		case noErr:
			syslog(LOG_INFO, "Deleting item from keychain");
			SecKeychainItemDelete(itemRef);
			// Set up attribute vector (each attribute consists of {tag, length, pointer}):
			//	SecKeychainAttribute attrs[] = {
			//	{ kSecAccountItemAttr, strlen(account), (char *)account },
			//	{ kSecServiceItemAttr, strlen(service), (char *)service }
			//	};
			//	const SecKeychainAttributeList attributes = { sizeof(attrs) / sizeof(attrs[0]), attrs };
			//  status = SecKeychainItemModifyAttributesAndData (itemRef, // the item reference
			//													NULL,//&attributes, // no change to attributes
			//													strlen(pass), // length of pass
			//													( const void * )pass); // pointer to pass data
			// nobreak;
		default:
			syslog(LOG_INFO, "Adding password to Keychain");
			status = SecKeychainAddGenericPassword(NULL,//default keychain
													  strlen(service), 
													  service,
													  strlen(account), 
													  account,
													  strlen(pass),
													  (const void *)pass, 
													  NULL);//&itemRef); //item reference
			break;
	}

	if (itemRef) { CFRelease(itemRef); }

	switch (status) {
		case noErr:
			syslog(LOG_INFO, "Keychain item sucessfully updated");
			break;
		case errSecItemNotFound:
			syslog(LOG_INFO, "Keychain item not found");
			break;
		case errSecAuthFailed:
			syslog(LOG_ERR, "Authorization failed.");
			break;
		case errSecNoDefaultKeychain:
			syslog(LOG_INFO, "No default Keychain");
			break;
		default:
			syslog(LOG_ERR, "Unknown error");
			break;
    }
	
	(*env)->ReleaseStringUTFChars(env, jService, service);
	(*env)->ReleaseStringUTFChars(env, jAccount, account);
	(*env)->ReleaseStringUTFChars(env, jPassword, pass);
}

/*
void addPwdToKeychain(const char *service, 
					  const char *account, 
					  const char *pass) {
    OSStatus status;
    
    status = SecKeychainAddGenericPassword(NULL,//default keychain //skcref,
										strlen(service), service,
										strlen(account), account,
										strlen(pass),
										(const void *)pass, 
										NULL //item reference
										);
    
    switch (status) {
		case 0:
			break;
		case errSecDuplicateItem:
			syslog(LOG_INFO, "Keychain item already exists.");
			break;
		case errSecAuthFailed:
			syslog(LOG_ERR, "Authorization failed.");
			break;
		default:
			syslog(LOG_ERR, "Unknown error when adding pass to the Keychain");
			break;
    }
}
*/

/*
JNIEXPORT void JNICALL Java_ch_cyberduck_core_Login_changePasswordInKeychain(JNIEnv *env, 
																			 jobject this, 
																			 jstring service, 
																			 jstring account, 
																			 jstring pass) {
	SecKeychainItemRef itemRef = nil;

    const char *serviceChar = (*env)->GetStringUTFChars(env, service, JNI_FALSE);
    const char *accountChar = (*env)->GetStringUTFChars(env, account, JNI_FALSE);
    const char *passwordChar = (*env)->GetStringUTFChars(env, password, JNI_FALSE);

	getPwdFromKeychain(serviceChar, 
						accountChar, 
						&itemRef);
	
	changePwdInKeychain(serviceChar, 
						accountChar, 
						passwordChar,
						&itemRef);
	
	if (itemRef) { CFRelease(itemRef); }

	(*env)->ReleaseStringUTFChars(env, service, serviceChar);
	(*env)->ReleaseStringUTFChars(env, account, accountChar);
	(*env)->ReleaseStringUTFChars(env, password, passwordChar);
}
*/
/*
void changePwdInKeychain (const char *service, 
						  const char *account, 
						  const char *password,
						  SecKeychainItemRef *itemRef) {
    OSStatus status;
	
    // Set up attribute vector (each attribute consists of {tag, length, pointer}):
	
    SecKeychainAttribute attrs[] = {
		{ kSecAccountItemAttr, strlen(account), (char *)account },
		{ kSecServiceItemAttr, strlen(service), (char *)service }
	};
    const SecKeychainAttributeList attributes = { sizeof(attrs) / sizeof(attrs[0]), attrs };
	status = SecKeychainItemModifyAttributesAndData (
													 &itemRef,        // the item reference
													 &attributes,    // no change to attributes
													 strlen(password),  // length of password
													 password); // pointer to password data
	switch (status) {
		case noErr:
			syslog(LOG_INFO, "Keychain item sucessfully updated");
			break;
		case errSecItemNotFound:
			syslog(LOG_INFO, "Keychain item not found");
			break;
		case errSecAuthFailed:
			syslog(LOG_ERR, "Authorization failed.");
			break;
		case errSecNoDefaultKeychain:
			syslog(LOG_INFO, "No default Keychain!");
			break;
		default:
			syslog(LOG_ERR, "Unknown error");
			break;
    }
}
*/