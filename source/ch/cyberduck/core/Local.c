/*
 *  Copyright (c) 2004 Werner Randelshofer
 *  Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Werner Randelshofer. ("Confidential Information").  You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into
 *  with Werner Randelshofer.
 *
 *  Changes by David Kocher <dkocher@cyberduck.ch>
 * 
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#include <stdio.h>
#include <jni.h>
#include "Local.h"
#include <Carbon/Carbon.h>
#include <CoreServices/CoreServices.h>

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Local_isAlias(JNIEnv *env, jclass instance, jstring pathJ) 
{

    // Assert arguments
    if (pathJ == NULL) return false;

    // Convert Java String to C char array
    const char *pathC;
    pathC = (*env)->GetStringUTFChars(env, pathJ, 0);

    // Do the API calls
    FSRef fileRef;
    OSErr err;
    Boolean isAlias, isFolder;
    err = FSPathMakeRef(pathC, &fileRef, NULL);
    if (err == 0) {
        err = FSIsAliasFile(&fileRef, &isAlias, &isFolder);
    }

    // Release the C char array
    (*env)->ReleaseStringUTFChars(env, pathJ, pathC);

    // Return the result
    return (err == 0) & isAlias;
}


JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Local_resolveAlias(JNIEnv *env, jclass instance, jstring aliasPathJ)
{
	
    // Assert arguments
    if (aliasPathJ == NULL) return false;

    // Convert Java filename to C filename
    const char *aliasPathC;
    aliasPathC = (*env)->GetStringUTFChars(env, aliasPathJ, 0);
    
    // Do the API calls
    FSRef fileRef;
    OSErr err;
    OSStatus status;
    Boolean wasAliased, targetIsFolder;
    UInt8 resolvedPathC[2048];

    err = FSPathMakeRef(aliasPathC, &fileRef, NULL);
    if (err == 0) {
        err = FSResolveAliasFile(&fileRef, true, &targetIsFolder, &wasAliased);
    }
    if (err == 0) {
        if (wasAliased) {
            status = FSRefMakePath(&fileRef, resolvedPathC, 2048);
            if (status != 0) err = 1;
        }
    }

    // Release the C filename
    (*env)->ReleaseStringUTFChars(env, aliasPathJ, aliasPathC);


    // Return the result
    return (err == 0 && wasAliased) ? (*env)->NewStringUTF(env, resolvedPathC) : aliasPathJ;
}