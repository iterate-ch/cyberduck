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

#include <sys/types.h>
#include <unistd.h>
#include <CoreFoundation/CoreFoundation.h>
#include "utils.h"

/* 
   Parses Info.plist properties of main bundle for the VM options, properties,
   main class, and main class args and returns them in the VMLaunchOptions
   structure.
*/
VMLaunchOptions *NewVMLaunchOptions(int argc, const char **currentArg) 
{
    // Allocated the structure that will be used to return the launch options */
    VMLaunchOptions *vmLaunchOptions = malloc(sizeof(VMLaunchOptions));
    	
	CFBundleRef mainBundleRef = CFBundleGetMainBundle();
	CFURLRef appPackageRelativeRef = CFBundleCopyBundleURL(mainBundleRef);
	CFURLRef appPackageRef = CFURLCopyAbsoluteURL(appPackageRelativeRef);
	CFRelease(appPackageRelativeRef);
	CFStringRef appPackageStringRef = CFURLCopyFileSystemPath(appPackageRef, kCFURLPOSIXPathStyle);
	CFRelease(appPackageRef);
	
	// Get an instance of the non-localized keys.
    CFDictionaryRef bundleInfoDict = CFBundleGetInfoDictionary(mainBundleRef);	
	if(NULL == bundleInfoDict) {
		fprintf(stderr, "[Launcher Error] No info dictionary.\n");
		exit(-1);
	}
	// If we succeeded, look for our property.
	CFDictionaryRef javaDict = CFDictionaryGetValue(bundleInfoDict, CFSTR("Java"));
	if(NULL == javaDict) {
		fprintf(stderr, "[Launcher Error] No Java key in dictionary.\n");
		exit(-1);
	}
	
	// Parse main class
	CFStringRef plistMainClassStringRef = CFDictionaryGetValue(javaDict, CFSTR("MainClass") );
	if(NULL == plistMainClassStringRef) {
		fprintf(stderr, "[Launcher Error] No main class specified.\n");
		exit(-1);
	}
	CFMutableStringRef mainClassStringRef = CFStringCreateMutableCopy(kCFAllocatorDefault, 0, plistMainClassStringRef);
	CFStringFindAndReplace(mainClassStringRef, CFSTR("."), CFSTR("/"), CFRangeMake(0, CFStringGetLength(mainClassStringRef)), kCFCompareCaseInsensitive);
	
	CFIndex mainClassStringRefSize = CFStringGetMaximumSizeForEncoding(CFStringGetLength(mainClassStringRef), kCFStringEncodingUTF8);
	vmLaunchOptions->mainClass = malloc(mainClassStringRefSize+1);
	if(!CFStringGetCString(mainClassStringRef, vmLaunchOptions->mainClass, mainClassStringRefSize, kCFStringEncodingUTF8)) {
		fprintf(stderr, "[Launcher Error] Error setting main class option.\n");
		exit(-1);
	}
	CFRelease(mainClassStringRef);
	
	// Parse classpath
	CFStringRef plistClasspathStringRef = CFDictionaryGetValue(javaDict, CFSTR("ClassPath") );
	if(NULL == plistClasspathStringRef) {
		fprintf(stderr, "[Launcher Error] No classpath specified.\n");
		exit(-1);
	}
	CFArrayRef jarsArrayRef = CFStringCreateArrayBySeparatingStrings(kCFAllocatorDefault, plistClasspathStringRef, CFSTR(":"));
	CFMutableStringRef classpathStringRef = CFStringCreateMutable(kCFAllocatorDefault, 0);
	CFStringAppend(classpathStringRef, CFSTR("-Djava.class.path="));
	
	for(int index = 0; index < CFArrayGetCount(jarsArrayRef); index++) {
		CFStringRef jarStringRef = (CFStringRef)CFArrayGetValueAtIndex(jarsArrayRef, index);
		// Make absolute path to bundle resource
		CFURLRef urlRef = CFBundleCopyResourceURL(mainBundleRef, jarStringRef, NULL, CFSTR("Java"));
		if(NULL == urlRef) {
			fprintf(stdout, "Resource not found for: %s\n", CFStringGetCStringPtr(jarStringRef, CFStringGetFastestEncoding(jarStringRef)));
			continue;
		}
		CFStringRef urlPathRef = CFURLCopyFileSystemPath(urlRef, kCFURLPOSIXPathStyle);
		CFRelease(urlRef);
		CFStringAppend(classpathStringRef, urlPathRef);
		CFRelease(urlPathRef);
		if(index < CFArrayGetCount(jarsArrayRef)-1) {
			CFStringAppend(classpathStringRef, CFSTR(":"));
		}
	}
	CFRelease(jarsArrayRef);
	
	// vmOptionsCFArrayRef will temporarly hold a list of VM options and properties to be passed in when creating the JVM
    CFMutableArrayRef vmArgumentsArrayRef = CFArrayCreateMutable(kCFAllocatorDefault, 0, &kCFTypeArrayCallBacks);
	CFArrayAppendValue(vmArgumentsArrayRef, classpathStringRef);
	CFRelease(classpathStringRef);
		
	// Set working directory
	CFStringRef plistWorkingDirStringRef = CFDictionaryGetValue(javaDict, CFSTR("WorkingDirectory") );
	if(NULL == plistWorkingDirStringRef) {
		fprintf(stderr, "[Launcher Error] No working directory specified.\n");
		exit(-1);
	}
	CFMutableStringRef workingDirRef = CFStringCreateMutable(kCFAllocatorDefault, 0);
	CFStringAppend(workingDirRef, CFSTR("-Duser.dir="));
	CFStringAppend(workingDirRef, plistWorkingDirStringRef);
	CFStringFindAndReplace(workingDirRef, CFSTR("$APP_PACKAGE"), appPackageStringRef, CFRangeMake(0, CFStringGetLength(workingDirRef)), kCFCompareCaseInsensitive);
	CFArrayAppendValue(vmArgumentsArrayRef, workingDirRef);
	CFRelease(workingDirRef);

	// Set additional VM options
	CFStringRef plistVmOptionsStringRef = CFDictionaryGetValue(javaDict, CFSTR("VMOptions") );
	if(NULL == plistVmOptionsStringRef) {
		fprintf(stderr, "[Launcher Error] No VM options specified.\n");
		exit(-1);
	}
	CFArrayRef vmOptionsArrayRef = CFStringCreateArrayBySeparatingStrings(kCFAllocatorDefault, plistVmOptionsStringRef, CFSTR(" "));
	for(int index = 0;index < CFArrayGetCount(vmOptionsArrayRef); index++) {
		CFStringRef optionStringRef = (CFStringRef)CFArrayGetValueAtIndex(vmOptionsArrayRef,index);
		CFMutableStringRef optionStringReplacedRef = CFStringCreateMutable(kCFAllocatorDefault, 0);
		CFStringAppend(optionStringReplacedRef, optionStringRef);
		CFStringFindAndReplace(optionStringReplacedRef, CFSTR("$APP_PACKAGE"), appPackageStringRef, CFRangeMake(0, CFStringGetLength(optionStringReplacedRef)), kCFCompareCaseInsensitive);
		CFArrayAppendValue(vmArgumentsArrayRef, optionStringReplacedRef);
		CFRelease(optionStringReplacedRef);
	}
	CFRelease(vmOptionsArrayRef);
	// Now we know how many JVM options there are and they are all in a CFArray of CFStrings.
    vmLaunchOptions->nOptions = CFArrayGetCount(vmArgumentsArrayRef);
    // We only need to do this if there are options
    if( vmLaunchOptions->nOptions > 0) {
        // Allocate some memory for the array of JavaVMOptions
        JavaVMOption *options = malloc(vmLaunchOptions->nOptions*sizeof(JavaVMOption));
        // Iterate over each option adding it to the JavaVMOptions array
        for(int index = 0;index < vmLaunchOptions->nOptions; index++) {
            // Allocate enough memory for each optionString char* to hold the max possible lengh a UTF8 encoded copy of the string would require
            CFStringRef optionStringRef = (CFStringRef)CFArrayGetValueAtIndex(vmArgumentsArrayRef, index);
            CFIndex optionStringSize = CFStringGetMaximumSizeForEncoding(CFStringGetLength(optionStringRef), kCFStringEncodingUTF8);
            options[index].extraInfo = NULL;
            options[index].optionString = malloc(optionStringSize+1);
            // Now copy the option into the the optionString char* buffer in a UTF8 encoding
            if(!CFStringGetCString(optionStringRef, options[index].optionString, optionStringSize, kCFStringEncodingUTF8)) {
                fprintf(stderr, "[Launcher Error] Error parsing JVM options.\n");
                exit(-1);
            }
			//fprintf(stdout, "Adding option:%s\n", CFStringGetCStringPtr(optionStringRef, CFStringGetFastestEncoding(optionStringRef)));
        }
        vmLaunchOptions->options = options;		
    }
    else {
        vmLaunchOptions->options = NULL;
	}
	CFRelease(appPackageStringRef);
    return vmLaunchOptions;
}

/* Release the Memory used by the VMLaunchOptions */
void FreeVMLaunchOptions(VMLaunchOptions *vmOptionsPtr) {
    int index = 0;
    if(vmOptionsPtr != NULL) { 
        JavaVMOption *options = vmOptionsPtr->options;
        /* Itterate through the JVM options, freeing the optionStrings and extraInfo. */
        if(options != NULL) {
            for(index = 0; index < vmOptionsPtr->nOptions; index++) {
                if(options[index].optionString != NULL)
                    free(options[index].optionString);

                if(options[index].extraInfo != NULL)
                    free(options[index].extraInfo);
            }
            free(vmOptionsPtr->options);
        }
        free(vmOptionsPtr);
    }
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