/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

#import "License.h"
#import "AquaticPrime.h"

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

/*
 * Class:     ch_cyberduck_core_aquaticprime_License
 * Method:    verify
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_aquaticprime_License_verify(JNIEnv *env, jobject this, jstring license)
{
	// Paste obfuscated public key here
    NSString *key = [NSString stringWithString:@"0xAF026CFCF552C3D09A051124A596CEF7BBB26B15629504CD163B09675BE507C9C526ED3DBFCB91B78F718E0886A18400B56BC00E9213228CD6D6E9C84D8B6099AA3DE6E6F46F6CC7970982DE93A2A7318351FDFA25AE75B403996E50BB40643384214234E84EDA3E518772A4FF57FE29DD7C77A5EEB14C9023CA18FEC63236EF"];
    // Instantiate AquaticPrime
    AquaticPrime *validator = [AquaticPrime aquaticPrimeWithKey:key];
	
    return [validator verifyLicenseFile:convertToNSString(env, license)];
}
