/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
#include <pthread.h>
#include <CoreFoundation/CoreFoundation.h>
#include "utils.h"

/*Starts a JVM using the options,classpath,main class, and args stored in a VMLaunchOptions structure */
static void* startup(VMLaunchOptions *launchOptions) {    
    int result = 0;
    JNIEnv* env;
    JavaVM* theVM;

    /* default vm args */
    JavaVMInitArgs	vm_args;
    vm_args.version	= JNI_VERSION_1_6;
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
     * Start VM. The current thread becomes the main thread of the VM.
     */
    startup(launchOptions);

    return 0;
}