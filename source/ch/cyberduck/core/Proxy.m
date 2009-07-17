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
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesFTPPassive] boolValue];
}

+ (BOOL)isHostExcluded:(NSString *)hostname {
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
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
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getSOCKSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	NSString *hostname = [proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSProxy];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return hostname;
}

+ (NSNumber *)getSOCKSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	NSNumber *port = [proxies objectForKey:(NSNumber *)kSCPropNetProxiesSOCKSPort];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return port;
}

+ (BOOL)isHTTPProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getHTTPProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	NSString *hostname = [proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPProxy];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return hostname;
}

+ (NSNumber *)getHTTPProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	NSNumber *port = [proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPPort];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return port;
}

+ (BOOL)isHTTPSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getHTTPSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	NSString *hostname = [proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSProxy];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return hostname;
}

+ (NSNumber *)getHTTPSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	NSNumber *port = [proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPSPort];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return port;
}

@end