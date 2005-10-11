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

jstring convertToJString(JNIEnv *env, NSString *nsString) 
{
	if(nsString == nil) {
		return NULL;
	}
	const char *unichars = [nsString UTF8String];
	
	return (*env)->NewStringUTF(env, unichars);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isSOCKSProxyEnabled(JNIEnv *env, 
																			jobject this)
{
	return [Proxy isSOCKSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyPort(JNIEnv *env, 
																	  jobject this)
{
	return [[Proxy getSOCKSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyHost(JNIEnv *env, 
																		 jobject this)
{
	return convertToJString(env, [Proxy getSOCKSProxyHost]);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyUser(JNIEnv *env, 
																		 jobject this) {
	return convertToJString(env, [Proxy getSOCKSProxyUser]);
}

@implementation Proxy

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

+ (NSString *)getSOCKSProxyUser
{
	return nil;
}

@end
