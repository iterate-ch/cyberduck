/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any latefile://localhost/Users/dkocher/Library/Java/JavaVirtualMachines/openjdk-1.7-i586/Contents/Home/jre/lib/i386/server/libjvm.dylibr version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 *
 * Derived work based on sample code by Apple Computer Inc.
 */

#include <sys/stat.h>
#include <sys/resource.h>
#include <pthread.h>
#include <CoreFoundation/CoreFoundation.h>
#include "utils.h"

/*Starts a JVM using the options,classpath,main class, and args stored in a VMLauchOptions structure */ 
static void* startup(VMLaunchOptions *launchOptions) {    
    int result = 0;
    JNIEnv* env;
    JavaVM* theVM;

    /* default vm args */
    JavaVMInitArgs	vm_args;
    vm_args.version	= JNI_VERSION_1_4;
    vm_args.options	= launchOptions->options;
    vm_args.nOptions = launchOptions->nOptions;
    vm_args.ignoreUnrecognized	= JNI_TRUE;

    /* start a VM session */    
    result = JNI_CreateJavaVM(&theVM, (void**)&env, &vm_args);

    if ( result != 0 ) {
        fprintf(stderr, "[Launcher Error] Error starting up VM.\n");
        exit(result);
        return NULL;
    }
    
    /* Find the main class */
	jclass mainClass = (*env)->FindClass(env, launchOptions->mainClass);
    if ( mainClass == NULL ) {
        (*env)->ExceptionDescribe(env);
        result = -1;
        goto leave;
    }

    /* Get the application's main method */
    jmethodID mainID = (*env)->GetStaticMethodID(env, mainClass, "main",
                                                 "([Ljava/lang/String;)V");
    if (mainID == NULL) {
        if ((*env)->ExceptionOccurred(env)) {
            (*env)->ExceptionDescribe(env);
        } else {
            fprintf(stderr, "[Launcher Error] No main method found in specified class.\n");
        }
        result = -1;
        goto leave;
    }

    // Create an empty array of java.lang.Strings to pass in as arguments to the main method
    jobjectArray mainArgs = (*env)->NewObjectArray(env, 0,
                        (*env)->FindClass(env, "java/lang/String"), NULL);
    if (mainArgs == 0) {
        result = -1;
        goto leave;
    }

    /* Invoke main method passing in the argument object. */
    (*env)->CallStaticVoidMethod(env, mainClass, mainID, mainArgs);
    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
        result = -1;
        goto leave;
    }
        
leave:
    FreeVMLaunchOptions(launchOptions);
    (*theVM)->DestroyJavaVM(theVM);
    exit(result);
    return NULL;
}

/* call back for dummy source used to make sure the CFRunLoop doesn't exit right away */
/* This callback is called when the source has fired. */
void sourceCallBack (  void *info  ) {}

/*  The following code will spin a new thread off to start the JVM
    while using the primordial thread to run the main runloop.
*/
int main(int argc, const char **argv)
{
    /* Parse the args */
    VMLaunchOptions *launchOptions = NewVMLaunchOptions(argc, argv);

	/*
     * Start VM.  The current thread becomes the main thread of the VM.
     */
	startup(launchOptions);

    return 0;
}

/*
 Copyright: 	© Copyright 2003 Apple Computer, Inc. All rights reserved.
 
 Disclaimer:	IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc.
 ("Apple") in consideration of your agreement to the following terms, and your
 use, installation, modification or redistribution of this Apple software
 constitutes acceptance of these terms.  If you do not agree with these terms,
 please do not use, install, modify or redistribute this Apple software.
 
 In consideration of your agreement to abide by the following terms, and subject
 to these terms, Apple grants you a personal, non-exclusive license, under Apple’s
 copyrights in this original Apple software (the "Apple Software"), to use,
 reproduce, modify and redistribute the Apple Software, with or without
 modifications, in source and/or binary forms; provided that if you redistribute
 the Apple Software in its entirety and without modifications, you must retain
 this notice and the following text and disclaimers in all such redistributions of
 the Apple Software.  Neither the name, trademarks, service marks or logos of
 Apple Computer, Inc. may be used to endorse or promote products derived from the
 Apple Software without specific prior written permission from Apple.  Except as
 expressly stated in this notice, no other rights or licenses, express or implied,
 are granted by Apple herein, including but not limited to any patent rights that
 may be infringed by your derivative works or by other works in which the Apple
 Software may be incorporated.
 
 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO
 WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED
 WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN
 COMBINATION WITH YOUR PRODUCTS.
 
 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION
 OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT
 (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */