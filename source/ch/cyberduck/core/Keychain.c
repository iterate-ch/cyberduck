/*
 *  Copyright (c) 2003 Regents of The University of Michigan.
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

extern int errno;

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Login_getPasswordFromKeychain(JNIEnv *env, 
																					 jobject this, 
																					 jstring service, 
																					 jstring account) {
	char		*password;
	OSStatus		error;
	
    const char *serviceChar = (*env)->GetStringUTFChars(env, service, JNI_FALSE);
    const char *accountChar = (*env)->GetStringUTFChars(env, account, JNI_FALSE);
	
	if (( password = getpwdfromkeychain( serviceChar,
										 accountChar, 
										 &error )) == NULL ) {
		if ( error == errSecItemNotFound ) {
			syslog( LOG_INFO, "Keychain item not found" );
		} else {
			syslog( LOG_INFO, "Attempting to retrieve password from keychain return error %d", error );
		}
		return (*env)->NewStringUTF(env, password);
	}
	
	(*env)->ReleaseStringUTFChars(env, service, serviceChar);
	(*env)->ReleaseStringUTFChars(env, service, accountChar);
	
	return (*env)->NewStringUTF(env, password);
//	free ( password );
}



char *getpwdfromkeychain(const char *service, 
						 const char *account, 
						 OSStatus *error ) {
    OSStatus 			err;
    SecKeychainRef		skcref;
    UInt32 			len;
    char			*password;
	
    err = SecKeychainCopyDefault( &skcref );
    
    if ( err ) {
        syslog( LOG_ERR, "SecKeychainCopyDefault failed" );
        return( NULL );
    }
    
	//    if (( password = ( char * )malloc( _PASSWORD_LEN + 1 )) == NULL ) {
	//      syslog( LOG_ERR, "malloc: %s", strerror( errno ));
	//        return( NULL );
	//    }
    
    err = SecKeychainFindGenericPassword( skcref,
										  strlen( service ), service,
										  strlen( account ), account, &len, ( void ** )&password, NULL );
	
    *error = err;
    switch ( err ) {
		case 0:
			break;
		case errSecItemNotFound:
			syslog( LOG_INFO, "Keychain item not found" );
			free( password );
			return( NULL );
		case errSecAuthFailed:
			syslog( LOG_ERR, "Authorization failed." );
			free( password );
			return( NULL );
		case errSecNoDefaultKeychain:
        syslog( LOG_INFO, "No default Keychain!" );
			free( password );
			return( NULL );
		case errSecBufferTooSmall:
			/* if the buffer's too small, make it really large and try again */
			syslog( LOG_INFO, "buffer too small, realloc'ing" );
			if (( password = ( char * )realloc( password, 4096 )) == NULL ) {
				syslog( LOG_ERR, "realloc: %s", strerror( errno ));
				free( password );
				return( NULL );
			}
				err = SecKeychainFindGenericPassword( skcref,
													  strlen( service ), service,
													  strlen( account ), account, &len, ( void ** )&password, NULL );
			if ( ! err ) break;
				free( password );
			return( NULL );
		default:
			syslog( LOG_ERR, "unknown error" );
			free( password );
			return( NULL );
    }
    
    password[ len ] = '\0';
    
    /* returns malloc'd bytes which must be free'd */
    return( password );
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Login_addPasswordToKeychain(JNIEnv *env, 
												   jobject this, 
												   jstring service, 
												   jstring account, 
												   jstring password) {
    const char *serviceChar = (*env)->GetStringUTFChars(env, service, JNI_FALSE);
    const char *accountChar = (*env)->GetStringUTFChars(env, account, JNI_FALSE);
    const char *passwordChar = (*env)->GetStringUTFChars(env, password, JNI_FALSE);
	
	addpwdtokeychain(serviceChar, 
					 accountChar, 
					 passwordChar);
	
	(*env)->ReleaseStringUTFChars(env, service, serviceChar);
	(*env)->ReleaseStringUTFChars(env, service, accountChar);
	(*env)->ReleaseStringUTFChars(env, service, passwordChar);
}

void addpwdtokeychain(const char *service, 
					  const char *account, 
					  const char *password) {
    OSStatus		err;
    SecKeychainRef	skcref;
	
    err = SecKeychainCopyDefault( &skcref );
    
    if ( err ) {
        syslog( LOG_ERR, "SecKeychainCopyDefault failed. Make sure you have a keychain available." );
        return;
    }
    
    err = SecKeychainAddGenericPassword( skcref,
										 strlen( service ), service,
										 strlen( account ), account,
										 strlen( password ),
										 ( const void * )password, NULL );
    
    switch ( err ) {
		case 0:
			break;
		case errSecDuplicateItem:
			syslog( LOG_INFO, "Keychain item already exists." );
			break;
		case errSecAuthFailed:
			syslog( LOG_ERR, "Authorization failed." );
			break;
		default:
			syslog( LOG_ERR, "Unknown error when adding password to the Keychain" );
			break;
    }
}