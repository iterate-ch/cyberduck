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

#import "SystemConfigurationProxy.h"

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

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_usePassiveFTPNative(JNIEnv *env, jobject this)
{
	return [Proxy usePassiveFTP];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isHostExcludedNative(JNIEnv *env, jobject this, jstring hostname)
{
	return [Proxy isHostExcluded:convertToNSString(env, hostname)];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isSOCKSProxyEnabledNative(JNIEnv *env, jobject this)
{
	return [Proxy isSOCKSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getSOCKSProxyPortNative(JNIEnv *env,  jobject this)
{
	return [[Proxy getSOCKSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getSOCKSProxyHostNative(JNIEnv *env, jobject this)
{
	return convertToJString(env, [Proxy getSOCKSProxyHost]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isHTTPProxyEnabledNative(JNIEnv *env, jobject this)
{
	return [Proxy isHTTPProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPProxyPortNative(JNIEnv *env, jobject this)
{
	return [[Proxy getHTTPProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPProxyHostNative(JNIEnv *env, jobject this)
{
	return convertToJString(env, [Proxy getHTTPProxyHost]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isHTTPSProxyEnabledNative(JNIEnv *env, jobject this)
{
	return [Proxy isHTTPSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPSProxyPortNative(JNIEnv *env, jobject this)
{
	return [[Proxy getHTTPSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPSProxyHostNative(JNIEnv *env, jobject this)
{
	return convertToJString(env, [Proxy getHTTPSProxyHost]);
}

@implementation Proxy

+ (BOOL)usePassiveFTP
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesFTPPassive] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
    return enabled;
}

+ (BOOL)isHostExcluded:(NSString *)hostname {
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
    NSEnumerator *exceptions = [[proxies objectForKey:(NSString *)kSCPropNetProxiesExceptionsList] objectEnumerator];
    NSString *domain;
    BOOL excluded = NO;
    while((domain = [exceptions nextObject])) {
        if([domain rangeOfString: hostname].location != NSNotFound) {
            excluded = YES;
            break;
        }
    }
	if (proxies != NULL) {
        CFRelease(proxies);
    }
    return excluded;
}

+ (BOOL)isSOCKSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getSOCKSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSString *hostname = [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSProxy] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [hostname autorelease];
}

+ (NSNumber *)getSOCKSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSNumber *port = [[proxies objectForKey:(NSNumber *)kSCPropNetProxiesSOCKSPort] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [port autorelease];
}

+ (BOOL)isHTTPProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getHTTPProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSString *hostname = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPProxy] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [hostname autorelease];
}

+ (NSNumber *)getHTTPProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSNumber *port = [[proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPPort] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [port autorelease];
}

+ (BOOL)isHTTPSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getHTTPSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSString *hostname = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSProxy] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [hostname autorelease];
}

+ (NSNumber *)getHTTPSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSNumber *port = [[proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPSPort] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [port autorelease];
}

@end