/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

#import "Proxy.h"

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

jstring convertToJString(JNIEnv *env, NSString *nsString) 
{
	if(nsString == nil) {
		return NULL;
	}
	const char *unichars = [nsString UTF8String];
	
	return (*env)->NewStringUTF(env, unichars);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_usePassiveFTP(JNIEnv *env, jobject this)
{
	return [Proxy usePassiveFTP];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isHostExcluded(JNIEnv *env, jobject this, jstring hostname)
{
	return [Proxy isHostExcluded:convertToNSString(env, hostname)];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isSOCKSProxyEnabled(JNIEnv *env, jobject this)
{
	return [Proxy isSOCKSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyPort(JNIEnv *env,  jobject this)
{
	return [[Proxy getSOCKSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyHost(JNIEnv *env, jobject this)
{
	return convertToJString(env, [Proxy getSOCKSProxyHost]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isHTTPProxyEnabled(JNIEnv *env, jobject this)
{
	return [Proxy isHTTPProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_Proxy_getHTTPProxyPort(JNIEnv *env, jobject this)
{
	return [[Proxy getHTTPProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getHTTPProxyHost(JNIEnv *env, jobject this)
{
	return convertToJString(env, [Proxy getHTTPProxyHost]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isHTTPSProxyEnabled(JNIEnv *env, jobject this)
{
	return [Proxy isHTTPSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_Proxy_getHTTPSProxyPort(JNIEnv *env, jobject this)
{
	return [[Proxy getHTTPSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getHTTPSProxyHost(JNIEnv *env, jobject this)
{
	return convertToJString(env, [Proxy getHTTPSProxyHost]);
}

@implementation Proxy

+ (BOOL)usePassiveFTP
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesFTPPassive] boolValue];
}

+ (BOOL)isHostExcluded:(NSString *)hostname {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
    NSEnumerator *exceptions = [[proxies objectForKey:(NSString *)kSCPropNetProxiesExceptionsList] objectEnumerator];
    NSString *domain;
    while(domain = [exceptions nextObject]) {
        if([domain rangeOfString: hostname].location != NSNotFound) {
            return YES;
        }
    }
    return NO;
}

+ (BOOL)isSOCKSProxyEnabled
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSEnable] boolValue];
}

+ (NSString *)getSOCKSProxyHost
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSProxy];
}

+ (NSNumber *)getSOCKSProxyPort
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [proxies objectForKey:(NSNumber *)kSCPropNetProxiesSOCKSPort];
}

+ (BOOL)isHTTPProxyEnabled
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPEnable] boolValue];
}

+ (NSString *)getHTTPProxyHost
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPProxy];
}

+ (NSNumber *)getHTTPProxyPort
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPPort];
}

+ (BOOL)isHTTPSProxyEnabled
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSEnable] boolValue];
}

+ (NSString *)getHTTPSProxyHost
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSProxy];
}

+ (NSNumber *)getHTTPSProxyPort
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	[pool release];
	return [proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPSPort];
}

@end