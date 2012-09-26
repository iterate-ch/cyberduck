/*
 * Copyright (c) 2002-2012 David Kocher. All rights reserved.
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
 */

#import <Cocoa/Cocoa.h>
#include <dlfcn.h>
#include <jni.h>

#define JAVA_LAUNCH_ERROR "Runtime Launcher Error"

#define JVM_PROPERTIES_KEY "Runtime"
#define JVM_LIB_KEY "Library"
#define JVM_DEFAULT_LIB "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/lib/jli/libjli.dylib"
#define JVM_MAIN_CLASS_NAME_KEY "MainClass"
#define JVM_WORKING_DIRECTORY_KEY "WorkingDirectory"
#define JVM_STARTONMAINTHREAD_KEY "StartOnMainThread"
#define JVM_OPTIONS_KEY "VMOptions"
#define JVM_ARGUMENTS_KEY "VMArguments"

#define UNSPECIFIED_ERROR "An unknown error occurred."

#define APP_ROOT_PREFIX "$APP_PACKAGE"

typedef int (JNICALL *JLI_Launch_t)(int argc, char ** argv, /* main argc, argc */
                                    int jargc, const char** jargv, /* java args */
                                    int appclassc, const char** appclassv,/* app classpath */
                                    const char* fullversion, /* full version defined */
                                    const char* dotversion, /* dot version defined */
                                    const char* pname, /* program name */
                                    const char* lname, /* launcher name */
                                    jboolean javaargs, /* JAVA_ARGS */
                                    jboolean cpwildcard, /* classpath wildcard*/
                                    jboolean javaw, /* windows-only javaw */
                                    jint ergo); /* ergonomics class policy */

int launch(char *);

int main(int argc, char *argv[]) {
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    @try {
        launch(argv[0]);
        return 0;
    } @catch (NSException *exception) {
        NSAlert *alert = [[NSAlert alloc] init];
        [alert setAlertStyle:NSCriticalAlertStyle];
        [alert setMessageText:[exception reason]];
        [alert setInformativeText:@"The application failed to launch with a unrecoverable exception."];
        [alert runModal];
        [alert release];
        return 1;
    } @finally {
        [pool drain];
    }
}

int launch(char *commandName) {
    // Get the main bundle
    NSBundle *mainBundle = [NSBundle mainBundle];

    // Get the main bundle dictionary
    NSDictionary *infoDictionary = [mainBundle infoDictionary];
    NSDictionary *javaDict = [infoDictionary objectForKey:@JVM_PROPERTIES_KEY];
    // Get the main class name
    NSString *mainClassName = [javaDict objectForKey:@JVM_MAIN_CLASS_NAME_KEY];
    if (mainClassName == nil) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
            reason:NSLocalizedString(@"Main class name required in Info.plist", @UNSPECIFIED_ERROR)
            userInfo:nil] raise];
    }

    NSString *javaPath = [[javaDict objectForKey:@JVM_WORKING_DIRECTORY_KEY] stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]];
    if (javaPath == nil) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
            reason:NSLocalizedString(@"Working directory path required in Info.plist", @UNSPECIFIED_ERROR)
            userInfo:nil] raise];
    }
    NSMutableString *classPath = [NSMutableString stringWithFormat:@"-Djava.class.path=%@", javaPath];
    // Set the class path
    NSFileManager *defaultFileManager = [NSFileManager defaultManager];
    for (NSString *file in [defaultFileManager contentsOfDirectoryAtPath:javaPath error:nil]) {
        if ([file hasSuffix:@".jar"]) {
            [classPath appendFormat:@":%@/%@", javaPath, file];
        }
    }

    NSArray *options;
    // Get the VM options
    if ([javaDict objectForKey:@JVM_OPTIONS_KEY] == nil) {
        options = [NSArray array];
    }
    else {
        if([[javaDict objectForKey:@JVM_OPTIONS_KEY] isKindOfClass:[NSString class]]) {
            options = [[javaDict objectForKey:@JVM_OPTIONS_KEY] componentsSeparatedByString:@" "];
        }
        else {
            options = [javaDict objectForKey:@JVM_OPTIONS_KEY];
        }
    }
    if ([javaDict objectForKey:@JVM_STARTONMAINTHREAD_KEY] != nil) {
        if ([[javaDict objectForKey:@JVM_STARTONMAINTHREAD_KEY] boolValue]) {
            options = [options arrayByAddingObject:@"-XstartOnFirstThread"];
        }
    }

    // Get the application arguments
    NSArray *arguments = [javaDict objectForKey:@JVM_ARGUMENTS_KEY];
    if (arguments == nil) {
        arguments = [NSArray array];
    }

    // Initialize the arguments to JLI_Launch()
    int argc = 2 + [options count] + 1 + [arguments count];
    char *argv[argc];

    int i = 0;
    argv[i++] = commandName;
    argv[i++] = strdup([classPath UTF8String]);

    for (NSString *option in options) {
        option = [option stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]];
        argv[i++] = strdup([option UTF8String]);
    }

    argv[i++] = strdup([mainClassName UTF8String]);

    for (NSString *argument in arguments) {
        argument = [argument stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]];
        argv[i++] = strdup([argument UTF8String]);
    }

    // Locate the JLI_Launch() function
    NSString *libjliPath = [[javaDict objectForKey:@JVM_LIB_KEY] stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]];
    if (libjliPath == nil) {
        libjliPath = @JVM_DEFAULT_LIB;
    }
    void *libJLI = dlopen([libjliPath fileSystemRepresentation], RTLD_LAZY);

    JLI_Launch_t jli_LaunchFxnPtr = NULL;
    if(libJLI != NULL) {
        jli_LaunchFxnPtr = dlsym(libJLI, "JLI_Launch");
    }
    if(jli_LaunchFxnPtr == NULL) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
                                 reason:NSLocalizedString(@"Error loading Runtime.", @UNSPECIFIED_ERROR)
                               userInfo:nil] raise];
    }
    // Invoke JLI_Launch()
    return jli_LaunchFxnPtr(argc, argv,
                            0, NULL, 0, NULL, "", "",
                            [[infoDictionary objectForKey:@"CFBundleName"] UTF8String], "Launcher",
                            FALSE, FALSE, FALSE, 0);
}