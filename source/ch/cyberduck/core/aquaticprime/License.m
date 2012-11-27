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
#import <JavaNativeFoundation/JNFString.h>

/*
 * Class:     ch_cyberduck_core_aquaticprime_Donation
 * Method:    verify
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_aquaticprime_Donation_verify(JNIEnv *env, jobject this, jstring license)
{
	// Paste obfuscated public key here
    NSString *key = @"0xAF026CFCF552C3D09A051124A596CEF7BBB26B15629504CD163B09675BE507C9C526ED3DBFCB91B78F718E0886A18400B56BC00E9213228CD6D6E9C84D8B6099AA3DE6E6F46F6CC7970982DE93A2A7318351FDFA25AE75B403996E50BB40643384214234E84EDA3E518772A4FF57FE29DD7C77A5EEB14C9023CA18FEC63236EF";
    // Instantiate AquaticPrime
    AquaticPrime *p = [AquaticPrime aquaticPrimeWithKey:key];
	
    return [p verifyLicenseFile:JNFJavaToNSString(env, license)];
}

/*
 * Class:     ch_cyberduck_core_aquaticprime_Donation
 * Method:    getValue
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_aquaticprime_Donation_getValue(JNIEnv *env, jobject this, jstring license, jstring property)
{
	// Paste obfuscated public key here
    NSString *key = @"0xAF026CFCF552C3D09A051124A596CEF7BBB26B15629504CD163B09675BE507C9C526ED3DBFCB91B78F718E0886A18400B56BC00E9213228CD6D6E9C84D8B6099AA3DE6E6F46F6CC7970982DE93A2A7318351FDFA25AE75B403996E50BB40643384214234E84EDA3E518772A4FF57FE29DD7C77A5EEB14C9023CA18FEC63236EF";
    // Instantiate AquaticPrime
    AquaticPrime *p = [AquaticPrime aquaticPrimeWithKey:key];

    return JNFNSToJavaString(env, [[p dictionaryForLicenseFile:JNFJavaToNSString(env, license)] valueForKey:JNFJavaToNSString(env, property)]);
}
