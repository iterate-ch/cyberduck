/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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
	NSNumber *port = [Proxy getSOCKSProxyPort];
	return [port intValue];
//	return convertToJString(env, port);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyHost(JNIEnv *env, 
																		 jobject this)
{
	NSString * host = [Proxy getSOCKSProxyHost];
	return convertToJString(env, host);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isSOCKSAuthenticationEnabled(JNIEnv *env, 
																					 jobject this) {
	return [Proxy isSOCKSAuthenticationEnabled];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyUser(JNIEnv *env, 
																		 jobject this) {
	NSString *user = [Proxy getSOCKSProxyUser];
	return convertToJString(env, user);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyPassword(JNIEnv *env, 
																			 jobject this) {
	NSString *pass = [Proxy getSOCKSProxyPassword];
	return convertToJString(env, pass);
}

@implementation Proxy

+ (BOOL)usePassiveFTP
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	//	[proxies writeToFile:@"proxyDict.dict" atomically:false];
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesFTPPassive] boolValue];
}

+ (BOOL)isSOCKSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	//	[proxies writeToFile:@"proxyDict.dict" atomically:false];
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSEnable] boolValue];
}

+ (NSString *)getSOCKSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	//	[proxies writeToFile:@"proxyDict.dict" atomically:false];
	return [proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSProxy];
}

+ (NSNumber *)getSOCKSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	//	[proxies writeToFile:@"proxyDict.dict" atomically:false];
	return [proxies objectForKey:(NSNumber *)kSCPropNetProxiesSOCKSPort];
}

+ (BOOL)isSOCKSAuthenticationEnabled
{
	return false; //@todo
}

+ (NSString *)getSOCKSProxyUser
{
	return nil; //@todo
}

+ (NSString *)getSOCKSProxyPassword
{
	return nil; //@todo
}

@end
