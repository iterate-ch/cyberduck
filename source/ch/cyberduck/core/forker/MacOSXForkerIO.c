/*
** MacOSXForkerIO.c -- JNI code
**
** Copyright 2002, 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
**
** The code in this implementation requires Mac OS 10.1 or higher.
**
** 05May2002 GLG  FIX: resolveRefX returns contents of FSRef
** 06May2002 GLG  revise some to use jstring args
** 07May2002 GLG  revise more to use jstring args
** 28May2002 GLG  rename to _resolve(); add _moveRef()
** 28May2002 GLG  add _openIterator(), _bulkInfo(), _closeIterator()
** 05Dec2002 GLG  change package and class names
** 05Dec2002 GLG  add _newAlias() and _freeHand()
** 07Dec2002 GLG  add _createAliasFile()
** 08Dec2002 GLG  add iconHints int-array to _createAliasFile()
** 09Dec2002 GLG  refactor how IconFamily retrieved in _createAliasFile()
** 11Dec2002 GLG  add _createSymlink()
** 11Dec2002 GLG  cover change to package name
** 16Dec2002 GLG  switch ApplicationServices framework to Carbon, for 10.1 happiness
** 02Jan2003 GLG  add _resolveAliasHand()
** 06Jan2003 GLG  add _changed()
** 07Jan2003 GLG  add a jint arg to _changed()
** 09Jan2003 GLG  add _TinFSRefItem_nativeInit()
** 09Jan2003 GLG  add Watcher stuff, still just stubs
** 10Jan2003 GLG  add real Watcher code: requires 10.1 or later
** 13Jan2003 GLG  remove test Watcher code; FNSubscriptions still don't work on 10.2 or 10.1
** 20Jan2003 GLG  add _TinAlias_getHandleSize() and _TinAlias_getHandleData()
** 24Jan2003 GLG  cut _resolveAliasHand()
*/

#include "TinAlias.h"
#include "TinFSFork.h"
#include "TinFSRefItem.h"
#include "TinWatcher.h"

//#include <ApplicationServices/ApplicationServices.h>  // GetIconRef() et al. aren't here in 10.1
#include <Carbon/Carbon.h>
#include <CoreServices/CoreServices.h>
#include <unistd.h>


// Used in _createAliasFile().
// May safely be null, but normally won't be null after _TinFSRefItem_nativeInit().
// We never release this IconRef, so it persists for the life of the process.
// I hope Icon Services is smart enough that it doesn't persist beyond that.
static IconRef genericFolderIcon = NULL;

/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    nativeInit
 * Signature: ()I
 */

/**
	 This idempotent method performs all the native-side once-only initialization.
	 It's synchronized on the class-lock so it can't be called re-entrantly from other threads.
	 That shouldn't happen, but one never knows.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_nativeInit (
	JNIEnv * env,  jclass theClass )
{
	OSErr result = 0;

	if ( genericFolderIcon == NULL )
	{
		// Don't act on errors, since genericFolderIcon will simply remain NULL.
		result = GetIconRef( kOnSystemDisk, kSystemIconsCreator, kGenericFolderIcon, &genericFolderIcon );
	}

	return ( result );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    volumeRefX
 * Signature: (SI[C[B)S
 */

/**
	 will call FSGetVolumeInfo.
	 Returns OSError.
*/

JNIEXPORT jshort JNICALL
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_volumeRefX (
	JNIEnv * env,  jclass ignored,
	jshort volume,  jint index,
	jcharArray nameChars,  jbyteArray fsRef )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	HFSUniStr255 name;
	FSRef ref;
	OSErr err;

	err = FSGetVolumeInfo( volume, index, NULL, kFSVolInfoNone, NULL, &name, &ref );
	if ( err == 0 )
	{
		(*env)->SetCharArrayRegion( env, nameChars, 0, 256, (jchar *) &name );
		(*env)->SetByteArrayRegion( env, fsRef, 0, sizeof(FSRef), (jbyte *) &ref );
	}
	return ( err );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    findFolderX
 * Signature: (SIZ[S)S
 */

/**
	 xx.
	 Returns OSError.
*/

JNIEXPORT jshort JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_findFolderX (
	JNIEnv * env,  jclass ignored,
	jshort volume,  jint type,
	jboolean createIt,  jshortArray refNumOut )
{
	jshort refNum;
	SInt32 dirID;
	OSErr err;

	err = FindFolder( volume, type, createIt, &refNum, &dirID );
	if ( err == 0 )
	{
		(*env)->SetShortArrayRegion( env, refNumOut, 0, 1, &refNum );
	}

	return ( err );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    makeRef
 * Signature: ([BLjava/lang/String;[B)I
 */

/**
	 Make the resultFSRef refer to the given file or directory,
	 calling FSMakeFSRefUnicode.
	 Return an OSErr value as the result.
	 None of the items may be null.
	<p>
	 If the targeted item doesn't exist, an error-code is returned.
	 Unlike with an FSSpec, an FSRef can't refer to a non-existent item.
	 The rest of the code in FSRefItem is responsible for handling non-existent targets,
	 so they can be encapsulated with behavior similar to a non-existent FSSpec.
	 will call .
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_makeRef (
	JNIEnv * env,  jobject ignored,
	jbyteArray inRef,  jstring name,
	jbyteArray outRef )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	// Java Strings are unalterable, so point at its chars directly.
	FSRef refParent;
	jsize count;
	const jchar * namePtr;
	FSRef refResult;
	OSErr err;

	// Copy from the Java objects into my local storage.
	namePtr = (*env)->GetStringChars( env, name, NULL );
	if ( namePtr == NULL )
		return ( -1 );

	count = (*env)->GetStringLength( env, name );
	(*env)->GetByteArrayRegion( env, inRef, 0, sizeof(FSRef), (jbyte *) &refParent );
	
	// Make the call.  On success, copy my refResult into Java's outRef.
	err = FSMakeFSRefUnicode( &refParent, count, namePtr, kTextEncodingUnknown, &refResult );
	if ( err == 0 )
		(*env)->SetByteArrayRegion( env, outRef, 0, sizeof(FSRef), (jbyte *) &refResult );

	// Always release the String chars.
	(*env)->ReleaseStringChars( env, name, namePtr );

	return ( err );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    getRefInfo
 * Signature: ([BI[B[C[B)I
 */

/**
	 Get the FSCatalogInfo for theFSRef.
	 Return an OSErr value as the result.
	 The nameBuf and/or parentFSRef may be null.
*/

JNIEXPORT jint JNICALL
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_getRefInfo (
	JNIEnv * env,  jobject ignored,
	jbyteArray inRef,
	jint infoBits,  jbyteArray catInfo,
	jcharArray nameChars,  jbyteArray outRef )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	// Either nameChars or outRef may be null, signifed in C as a NULL arg.
	FSRef itemRef;
	FSCatalogInfo info;
	HFSUniStr255 outName;
	FSRef parentRef;
	OSErr err;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, inRef, 0, sizeof(FSRef), (jbyte *) &itemRef );
	
	// Make the call.  On success, copy results back into Java's objects.
	// Always pass NULL as the FSSpec ptr, since we never want one.
	// Always give non-NULL outName and parentRef, even if we won't copy them back.
	err = FSGetCatalogInfo( &itemRef, infoBits, &info, &outName, NULL, &parentRef );
	if ( err == 0 )
	{
		// The FSCatalogInfo is always returned.
		(*env)->SetByteArrayRegion( env, catInfo, 0, sizeof(FSCatalogInfo), (jbyte *) &info );

		if ( nameChars != NULL )
			(*env)->SetCharArrayRegion( env, nameChars, 0, 256, (jchar *) &outName );

		if ( outRef != NULL )
			(*env)->SetByteArrayRegion( env, outRef, 0, sizeof(FSRef), (jbyte *) &parentRef );
	}
	return ( (jint) err );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    briefRefInfoX
 * Signature: ([BI[B)S
 */

/**
	 will call FSGetVolumeInfo.
	 Returns OSError.
*/
/*
JNIEXPORT jshort JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_briefRefInfoX (
	JNIEnv * env,  jclass ignored,
	jbyteArray inRef,
	jint infoBits,  jbyteArray infoStruct )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	FSRef itemRef;
	FSCatalogInfo info;
	OSErr err;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, inRef, 0, sizeof(FSRef), (jbyte *) &itemRef );
	
	// Make the call.  On success, copy results back into Java's objects.
	// Pass NULL as the name ptr, FSSpec ptr, and FSRef pt, since we don't want any.
	err = FSGetCatalogInfo( &itemRef, infoBits, &info, NULL, NULL, NULL );
	if ( err == 0 )
	{
		(*env)->SetByteArrayRegion( env, infoStruct, 0, sizeof(FSCatalogInfo), (jbyte *) &info );
	}
	return ( err );
}
*/


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    openRef
 * Signature: ([B[CB[S)I
 */

/**
	 Open the item's named fork, calling FSOpenFork.
	 Return an OSError value as the result.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_openRef (
	JNIEnv * env,  jobject ignored,
	jbyteArray itemRef,
	jcharArray forkName,
	jbyte perm,  jshortArray refNumOut )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	// Use an HFSUniStr255 struct because it holds an entire correctly-sized array to hold the chars.
	jsize count;
	HFSUniStr255 name;
	FSRef ref;
	SInt16 refNum;
	OSErr err;

	// Copy from the Java arrays into my local storage.
	count = (*env)->GetArrayLength( env, forkName );
	(*env)->GetCharArrayRegion( env, forkName, 0, count, name.unicode );
	(*env)->GetByteArrayRegion( env, itemRef, 0, sizeof(FSRef), (jbyte *) &ref );
	
	// Make the open call.  On success, put refNum into Java's refNumOut.
	err = FSOpenFork( &ref, count, name.unicode, perm, &refNum );
	if ( err == 0 )
		(*env)->SetShortArrayRegion( env, refNumOut, 0, 1, &refNum );

	return ( err );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    resolve
 * Signature: ([BZ[B[B)I
 */

/**
	 This is only to be called AFTER thread-safety is confirmed.
	 This explicit thread-safety is because the FSRef-based calls being used are
	 not listed as MP-task safe, nor pthread-safe, nor Cocoa-safe, nor anything-safe.
*/

JNIEXPORT jint JNICALL Java_glguerin_io_imp_mac_macosx_TinFSRefItem_resolve (
	JNIEnv * env,  jclass ignored,
	jbyteArray itemRef,
	jboolean resolveChains,
	jbyteArray targetIsFolder,  jbyteArray wasAliased )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	FSRef ref;
	Boolean isAliased, isFolder;
	OSErr err;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, itemRef, 0, sizeof(FSRef), (jbyte *) &ref );
	
	// Make the call.  On overall success, put local flags into byte-arrays.
	// N.B. flag-ptr args to FSIsAliasFile() are reversed from FSResolveAliasFileWithMountFlags() args.
	err = FSIsAliasFile( &ref, &isAliased, &isFolder );
	if ( err == 0  &&  isAliased )
		err = FSResolveAliasFileWithMountFlags( &ref, resolveChains, &isFolder, &isAliased, kResolveAliasFileNoUI );

	// ## This is the FSResolveAliasFile() call, which may be susceptible to threading issues if a UI appears.
	//	## err = FSResolveAliasFile( &ref, resolveChains, &isFolder, &isAliased );

	if ( err == 0 )
	{
		// The FSRef must be returned, as well as the two flags.
		(*env)->SetByteArrayRegion( env, itemRef, 0, sizeof(FSRef), (jbyte *) &ref );
		(*env)->SetByteArrayRegion( env, targetIsFolder, 0, 1, (jbyte *) &isFolder );
		(*env)->SetByteArrayRegion( env, wasAliased, 0, 1, (jbyte *) &isAliased );
	}

	return ( err );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    createRef
 * Signature: ([BLjava/lang/String;Z[B)I
 */

/**
	 Create the file or directory referenced by the FSRef and other args,
	 calling FSCreateDirectoryUnicode() or FSCreateFileUnicode().
	 Return an OSErr value as the result.
	 None of the items may be null.
*/

JNIEXPORT jint JNICALL
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_createRef (
	JNIEnv * env,  jobject ignored,
	jbyteArray inRef,
	jstring name,
	jboolean isDir,  jbyteArray outRef )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	// Java Strings are unalterable, so point at its chars directly.
	FSRef refParent;
	jsize count;
	const jchar * namePtr;
	FSRef refResult;
	OSErr err;
	FSCatalogInfoBitmap noInfo = 0;

	// Copy from the Java objects into my local storage.
	namePtr = (*env)->GetStringChars( env, name, NULL );
	if ( namePtr == NULL )
		return ( -1 );

	count = (*env)->GetStringLength( env, name );
	(*env)->GetByteArrayRegion( env, inRef, 0, sizeof(FSRef), (jbyte *) &refParent );

	// Make the call..
	if ( isDir )
		err = FSCreateDirectoryUnicode( &refParent, count, namePtr, noInfo, NULL, &refResult, NULL, NULL );
	else
		err = FSCreateFileUnicode( &refParent, count, namePtr, noInfo, NULL, &refResult, NULL );

	// On success, put refResult into Java's outRef
	if ( err == 0 )
		(*env)->SetByteArrayRegion( env, outRef, 0, sizeof(FSRef), (jbyte *) &refResult );

	// Always release the String chars.
	(*env)->ReleaseStringChars( env, name, namePtr );


	return ( err );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    setRefInfo
 * Signature: ([BI[B)I
 */

/**
	 Set the FSCatalogInfo for theFSRef, calling FSSetCatalogInfo.
	 Return an OSErr value as the result.
	 Returns OSError.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_setRefInfo (
	JNIEnv * env,  jobject ignored,
	jbyteArray inRef,
	jint infoBits,  jbyteArray infoBytes )
{
	// Use my own local structs, so failures don't whack any Java objects.
	FSRef itemRef;
	FSCatalogInfo info;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, inRef, 0, sizeof(FSRef), (jbyte *) &itemRef );
	(*env)->GetByteArrayRegion( env, infoBytes, 0, sizeof(FSCatalogInfo), (jbyte *) &info );
	
	// Make the call and return the OSErr code.
	return ( FSSetCatalogInfo( &itemRef, infoBits, &info ) );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    deleteRef
 * Signature: ([B)I
 */

/**
	 Delete the file or directory referenced by the FSRef,
	 without resolving any aliases.
	 Return an OSError value as the result.
	 Will call FSDeleteObject.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_deleteRef (
	JNIEnv * env,  jobject ignored,
	jbyteArray itemRef )
{
	// Use my own local structs, so failures don't whack any Java objects.
	FSRef ref;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, itemRef, 0, sizeof(FSRef), (jbyte *) &ref );
	
	// Make the call and return the OSErr code.
	return ( FSDeleteObject( &ref ) );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    renameRef
 * Signature: ([BLjava/lang/String;[B)I
 */

/**
	 Rename the file or directory referenced by the FSRef,
	 without resolving any aliases,
	 calling FSRenameUnicode.
	 Return an OSErr value as the result.
	 None of the items may be null.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_renameRef (
	JNIEnv * env,  jobject ignored,
	jbyteArray inRef,  jstring name,
	jbyteArray outRef )
{
	// Use my own local structs, so failures are detected before any Java objects are altered.
	// Java Strings are unalterable, so point at its chars directly.
	FSRef refItem;
	jsize count;
	const jchar * namePtr;
	FSRef refResult;
	OSErr err;

	// Copy from the Java objects into my local storage.
	namePtr = (*env)->GetStringChars( env, name, NULL );
	if ( namePtr == NULL )
		return ( -1 );

	count = (*env)->GetStringLength( env, name );
	(*env)->GetByteArrayRegion( env, inRef, 0, sizeof(FSRef), (jbyte *) &refItem );
	
	// Make the call.  On success, copy my refResult into Java's outRef.
	err = FSRenameUnicode( &refItem, count, namePtr, kTextEncodingUnknown, &refResult );
	if ( err == 0  &&  outRef != NULL )
		(*env)->SetByteArrayRegion( env, outRef, 0, sizeof(FSRef), (jbyte *) &refResult );

	// Always release the String chars.
	(*env)->ReleaseStringChars( env, name, namePtr );

	return ( err );
}




/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    moveRef
 * Signature: ([B[B)I
 */

/**
	 Move the file or directory referenced by the FSRef,
	 without resolving any aliases.
	 Return an OSError value as the result.
	 The destination must reference an existing directory.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_moveRef (
	JNIEnv * env,  jobject ignored,
	jbyteArray fromRef, jbyteArray toRef )
{
	// Use my own local structs, so failures don't whack any Java objects.
	FSRef refOrig;
	FSRef refDest;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, fromRef, 0, sizeof(FSRef), (jbyte *) &refOrig );
	(*env)->GetByteArrayRegion( env, toRef, 0, sizeof(FSRef), (jbyte *) &refDest );

	return ( FSMoveObject( &refOrig, &refDest, NULL ) );
}





/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    openIterator
 * Signature: ([BI[I)S
 */

/**
	 Calls FSOpenIterator()
*/

JNIEXPORT jint JNICALL
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_openIterator (
	JNIEnv * env,  jclass ignored,
	jbyteArray dirRef, jint iterFlags, jintArray iterRef )
{
	// Use my own local structs, so failures don't whack any Java objects.
	FSRef container;
	FSIterator iterator;
	OSErr err;

	// Copy from the Java arrays into my local storage.
	(*env)->GetByteArrayRegion( env, dirRef, 0, sizeof(FSRef), (jbyte *) &container );

	err = FSOpenIterator( &container, (FSIteratorFlags) iterFlags, &iterator );
	if ( err == 0  &&  iterRef != NULL )
		(*env)->SetIntArrayRegion( env, iterRef, 0, 1, (jint *) &iterator );

	return ( err );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    getCatInfo
 * Signature: (II[I[BI[B[C)S
 */

/**
	 Calls FSGetCatalogInfoBulk()
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_bulkInfo (
	JNIEnv * env,  jclass ignored,
	jint iterator,  jint whichInfo, 
	jbyteArray catInfo, jcharArray nameChars )
{
	ItemCount gotCount;
	FSCatalogInfo gotInfo;
	HFSUniStr255 gotName;
	OSErr err;

	err = FSGetCatalogInfoBulk( (FSIterator) iterator, 1, &gotCount, NULL, 
			whichInfo, &gotInfo, NULL, NULL, &gotName );

	if ( err == 0 )
	{
		if ( catInfo != NULL )
			(*env)->SetByteArrayRegion( env, catInfo, 0, sizeof(gotInfo), (jbyte *) &gotInfo );

		if ( nameChars != NULL )
			(*env)->SetCharArrayRegion( env, nameChars, 0, 256, (jchar *) &gotName );
	}

	return ( err );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    closeIterator
 * Signature: (I)S
 */

/**
	 Calls FSCloseIterator()
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_closeIterator (
	JNIEnv * env,  jclass ignored,
	jint iterator )
{
	return ( FSCloseIterator( (FSIterator) iterator ) );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    newAlias
 * Signature: ([B[I)I
 */

/**
	 This method is synchronized under the class-lock, to prevent re-entrant calls from
	 any other thread in the process from calling the thread-unsafe Alias Mgr functions.
	##  newAlias( byte[] fsRef, int[] aliasRef );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_newAlias (
	JNIEnv * env,  jclass ignored,
	jbyteArray targetRef, jintArray aliasRef )
{
	// Use my own local structs, so failures don't whack any Java objects.
	FSRef target;
	AliasHandle alias;
	OSErr result;

	// Copy from the Java array into my local storage.
	(*env)->GetByteArrayRegion( env, targetRef, 0, sizeof(target), (jbyte *) &target );

	result = FSNewAlias( NULL, &target, &alias );
	if ( result == 0 )
		(*env)->SetIntArrayRegion( env, aliasRef, 0, 1, (jint *) &alias );

	return ( result );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    createAliasFile
 * Signature: (I[BLjava/lang/String;[B)I
 */

/**
	 This method is synchronized under the class-lock, to prevent re-entrant calls from
	 any other thread in the process from calling the thread-unsafe Resource Mgr functions.
	##  createAliasFile( int aliasHandle, byte[] parentFSRef, String name, byte[] resultFSRef, int[] iconInfo );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_createAliasFile (
	JNIEnv * env,  jclass ignored,
	jint aliasHand, jbyteArray parentRefBytes, jstring name, jbyteArray resultRefBytes,
	jintArray iconInfo )
{
	// iconInfo[0]: return custom-icon Finder-flags bit-mask or 0
	// iconInfo[1]: creator-hint
	// iconInfo[2]: type-hint
	// Use my own local structs, so failures are detected before any Java objects are altered.
	// Java Strings are unalterable, so point at its chars directly.
	const jchar * namePtr;
	jsize count;
	FSRef parentRef;
	FSRef resultRef;
	OSErr err;
	FSCatalogInfoBitmap noInfo = 0;
	jint iconHints[ 3 ];
	IconRef iconRef = NULL;
	IconFamilyHandle iconFamily = NULL;

	// Get references to Java String object.
	count = (*env)->GetStringLength( env, name );
	namePtr = (*env)->GetStringChars( env, name, NULL );
	if ( namePtr == NULL )
		return ( -1 );

	// Fill in the local parentRef and iconHints[].
	(*env)->GetByteArrayRegion( env, parentRefBytes, 0, sizeof(parentRef), (jbyte *) &parentRef );
	(*env)->GetIntArrayRegion( env, iconInfo, 0, 3, iconHints );

	// Hinted icon-retrieval is done first, so we have an 'icns' (IconFamilyHandle) ready for the resFile.
	// If no hints, or hints fail to retrieve an icon, then try using the AliasHandle to get an icon.
	// If that fails, then alias-file will have no 'icns'.
	if ( iconHints[1] != 0 )
	{
		err = GetIconRef( kOnSystemDisk, (OSType) iconHints[1], (OSType) iconHints[2], &iconRef );
		if ( err != 0 )
			iconRef = NULL;
	}

	// If iconRef is still null, try using AliasHandle to obtain an FSRef of original,
	// then call Icon Services based on that.  The AliasHandle should not change, since that would indicate
	// a TinAlias that had not been update()'d prior to calling this function.
	// At this time, ignore whether the aliasHand changed.
	// ## FIXME: if wasChanged is T, should abandon this because TinAlias won't track the change.
	// This code requires GetIconRefFromFileInfo().
	// ## FIXME: GetIconRefFromFileInfo() is only on 10.1 or later; need a plan for 10.0. ##
	if ( iconRef == NULL )
	{
		Boolean wasChanged;

		// Use resultRef temporarily.  It will be overwritten later, so it doesn't matter if it's used here. 
		err = FSResolveAliasWithMountFlags( NULL, (AliasHandle) aliasHand,
					&resultRef, &wasChanged, kResolveAliasFileNoUI );
		if ( err == 0 )
		{
			SInt16 labelDontCare;
			err = GetIconRefFromFileInfo( &resultRef, 0, NULL, noInfo, NULL, 
						kIconServicesNormalUsageFlag, &iconRef, &labelDontCare );
			if ( err != 0 )
				iconRef = NULL;
		}
	}

	// If iconRef is non-null, get an IconFamilyHandle from it and release the IconRef.
	// To prevent adding custom-icons for generic folders, also compare iconRef with genericFolderIcon.
	// If iconRef matches genericFolderIcon, then iconFamily will remain null.
	if ( iconRef != NULL )
	{
		// Comparison to genericFolderIcon is OK even if genericFolderIcon is null.
		if ( iconRef != genericFolderIcon )
		{
			// Don't care about error from IconRefToIconFamily(), as iconFamily will be NULL if call fails.
			IconRefToIconFamily( iconRef, kSelectorAllAvailableData, &iconFamily );

			// If iconFamily is non-null, then set iconInfo[0] to kHasCustomIcon, writing it back into Java array.
			if ( iconFamily != NULL )
			{
				iconHints[0] = kHasCustomIcon;
				(*env)->SetIntArrayRegion( env, iconInfo, 0, 1, iconHints );
			}
		}

		ReleaseIconRef( iconRef );
		iconRef = NULL;
	}

	// This do...while(false) structure is a C idiom that allows me to use 'break' to go to
	// the unlabeled point just outside the loop.  C has no exceptions, hence this kluge.
	// Normally I'd define a pair of macros, but for one-time use, why bother.
	do {
		short resFile;
		Handle alias;

		// Create the file and put a resource-map in its res-fork.
		FSCreateResFile( &parentRef, count, namePtr, noInfo, NULL, &resultRef, NULL );
		err = ResError();
		if ( err != 0 )
			break;

		// Before anything else, copy resultRef into Java byte-array.
		(*env)->SetByteArrayRegion( env, resultRefBytes, 0, sizeof(resultRef), (jbyte *) &resultRef );

		// After successful creation, proceed to open the resFile and put resources into it.
		// Ask for exclusive R/W access to newly created res-file.
		resFile = FSOpenResFile( &resultRef, fsRdWrPerm );
		if ( resFile < 0 )
		{  err = ResError();  break;  }

		// Make duplicate handle of alias, which will go away when resFile is closed.
		alias = (Handle) aliasHand;
		err = HandToHand( &alias );
		if ( err == 0 )
		{
			AddResource( alias, rAliasType, 0, NULL );

			if ( iconFamily != NULL )
				AddResource( (Handle) iconFamily, 'icns', -16496, NULL );  // -16496 = magic resID of alias-custom-icon

			// Could update the resFile here, but CloseResFile() is adequate for now.
		}

		// Closing the resFile releases the handles that were added as resources.
		CloseResFile( resFile );
		err = ResError();

		// Assume iconFamily's handle went away when resFile closed, so NULL it out.
		iconFamily = NULL;

	} while ( false );

	// Getting here, err will hold zero on success, or non-zero error-code.
	// The resource-file will already be written and closed as necessary.
	// The contents of resultRef will already be copied back into resultRefBytes, too.

	// If iconFamily wasn't saved to resFile, discard it now.
	if ( iconFamily != NULL )
		DisposeHandle( (Handle) iconFamily );

	// Always release the String chars.
	(*env)->ReleaseStringChars( env, name, namePtr );

	return ( err );
}




/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    createSymlink
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */

/**
	 This method is synchronized under the class-lock, to block re-entrant calls from
	 any other thread in the process.
	##  createSymlink( String originalName, String symlinkName );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_createSymlink (
	JNIEnv * env,  jclass ignored,
	jstring originalName, jstring symlinkName )
{
	const jbyte * utfOriginal;
	const jbyte * utfSymlink;
	jboolean isCopy;
	jint result = -1;

	// Get original's full pathname as UTF8 nul-terminated bytes.
	utfOriginal = (*env)->GetStringUTFChars( env, originalName, &isCopy );
	if ( utfOriginal == NULL )
		return ( -1 );  // OutOfMemoryError will be thrown

	// Get symlink's full pathname as UTF8 nul-terminated bytes.
	utfSymlink = (*env)->GetStringUTFChars( env, symlinkName, &isCopy );
	if ( utfSymlink != NULL )
	{
		result = symlink( utfOriginal, utfSymlink );
		if ( result != 0 )
			result = errno;

		// Release utfSymlink's bytes.
		(*env)->ReleaseStringUTFChars( env, symlinkName, utfSymlink );
	}

	// Release utfOriginal's bytes.
	(*env)->ReleaseStringUTFChars( env, originalName, utfOriginal );

	return ( result );
}




/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSRefItem
 * Method:    changed
 * Signature: (Z[B)I
 */

/**
	 This method is synchronized under the class-lock, to prevent re-entrant calls from
	 any other thread in the process from calling the thread-safety-unknown FNNotify() functions.
	##  changed( int msgValue, boolean specifically, byte[] theFSRef );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSRefItem_changed (
	JNIEnv * env,  jclass ignored,
	jint msgValue, jboolean specifically, jbyteArray theFSRef )
{
	// FNNotify() and FNNotifyAll() are present in 10.0 or later.
	FSRef targetRef;
	OSErr err;

	if ( specifically )
	{
		(*env)->GetByteArrayRegion( env, theFSRef, 0, sizeof(targetRef), (jbyte *) &targetRef );
		err = FNNotify( &targetRef, msgValue, kNilOptions );
	}
	else
	{
		err = FNNotifyAll( msgValue, kNilOptions );
	}

	return ( err );
}



/*  ############### */



/*
 * Class:     glguerin_io_imp_mac_macosx_TinAlias
 * Method:    getHandleSize
 * Signature: (I)I
 */

/**
	 @return  size, always less than 2 GB; or negative error-code
	## int getHandleSize( int anyHand );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinAlias_getHandleSize (
	JNIEnv * env,  jclass ignored,
	jint anyHand )
{
	if ( anyHand != 0 )
		return ( GetHandleSize( (Handle) anyHand ) );

	return ( -1 );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinAlias
 * Method:    getHandleData
 * Signature: (I[BII)I
 */

/**
	 @return  result-code
	## int getHandleData( int anyHand, byte[] bytes, int offset, int count );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinAlias_getHandleData (
	JNIEnv * env,  jclass ignored,
	jint anyHand, jbyteArray bytes, jint offset, jint count )
{
	Handle hand;
	SInt8 state;

	if ( anyHand == 0  ||  bytes == NULL  ||  offset < 0  ||  count < 0 )
		return ( -1 );

	// WARNING: Does not check that offset & count lie within array's bounds.
	// WARNING: Does not check that handle-size is below count.

	hand = (Handle) anyHand;
	if ( *hand == NULL )
		return ( -1 );

	// Locking may or may not be necessary, but I feel safer with it locked.
	state = HGetState( hand );
	HLock( hand );
	(*env)->SetByteArrayRegion( env, bytes, offset, count, (jbyte *) *hand );
	HSetState( hand, state );

	return ( 0 );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinAlias
 * Method:    freeHand
 * Signature: (I)I
 */

/**
	 @return  result-code
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinAlias_freeHand (
	JNIEnv * env,  jclass ignored,
	jint aliasHand )
{
	if ( aliasHand != 0 )
		DisposeHandle( (Handle) aliasHand );

	return ( 0 );
}



/*  ############### */

/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkClose
 * Signature: (S)I
 */

/**
	Close the given refNum.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkClose (
	JNIEnv * env,  jobject ignored,
	jshort refNum )
{
	return ( FSCloseFork( refNum ) );
}

/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkLength
 * Signature: (S[J)I
 */

/**
	 Return the length of the given refNum's fork.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkLength (
	JNIEnv * env,  jobject ignored,
	jshort refNum,  jlongArray lenOut )
{
	UInt64 forkSize;
	int err;

	err = FSGetForkSize( refNum, &forkSize );
	if ( err == 0 )
	{
		(*env)->SetLongArrayRegion( env, lenOut, 0, 1, (jlong *) &forkSize );
	}

	return ( err );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkAt
 * Signature: (S[J)I
 */

/**
	 Return the current R/W position of the given refNum's fork.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkAt (
	JNIEnv * env,  jobject ignored,
	jshort refNum,  jlongArray posnOut )
{
	UInt64 posn;
	int err;

	err = FSGetForkPosition( refNum, &posn );
	if ( err == 0 )
	{
		(*env)->SetLongArrayRegion( env, posnOut, 0, 1, (jlong *) &posn );
	}

	return ( err );
}

/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkSeek
 * Signature: (SJ)I
 */

/**
	 Seek to the given position in the given refNum's fork.
	 The position is always relative to the beginning of the file.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkSeek (
	JNIEnv * env,  jobject ignored,
	jshort refNum,  jlong posn )
{
	return ( FSSetForkPosition( refNum, fsFromStart, posn ) );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkRead
 * Signature: (S[BI[I)I
 */

/**
	 Read bytes from the current position in the given refNum's fork,
	 for a byte-count given by requestCount, placing the bytes in the buffer
	 beginning at offset 0.
	 Return the actual byte-count read in actualCount[ 0 ].
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkRead (
	JNIEnv * env,  jobject ignored,
	jshort refNum,
	jbyteArray jBytes,  jint wanted,
	jintArray actualCount )
{
	jbyte * buf;
	SInt64 offset = 0;
	ByteCount got;
	OSErr err;

	// It's silly that the GetByteArrayElements() might make a copy, but I know of no
	// other way to get the bytes back into the jBytes array, short of making another buffer.
	buf = (*env)->GetByteArrayElements( env, jBytes, NULL );
	if ( buf == NULL )
		return ( -1 );

//	offset = 0;
	err = FSReadFork( refNum, fsAtMark, offset, wanted, buf, &got );

	// Always have to release array elements, so do it with same mode = 0 no matter what.
	(*env)->ReleaseByteArrayElements( env, jBytes, buf, 0 );

	// Also have to return actual count, which caller will qualify against returned OSErr.
	(*env)->SetIntArrayRegion( env, actualCount, 0, 1, (jint *) &got );

	return ( err );
}



/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkSetLength
 * Signature: (SJ)I
 */

/**
	 Set the length of the given refNum's fork.
	 When extended, the new bytes in the fork may contain arbitrary
	 and possibly sensitive data from reused disk blocks.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkSetLength (
	JNIEnv * env,  jobject ignored,
	jshort refNum,  jlong len )
{
	return ( FSSetForkSize( refNum, fsFromStart, len ) );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    flushForkX
 * Signature: (S)S
 */

/**
	xx
*/

JNIEXPORT jshort JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_flushForkX (
	JNIEnv * env,  jclass ignored,
	jshort refNum )
{
	return ( FSFlushFork( refNum ) );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinFSFork
 * Method:    forkWrite
 * Signature: (S[BI[I)I
 */

/**
	 Write bytes to the current position in the given refNum's fork,
	 for a byte-count given by requestCount, taking the bytes from the buffer
	 beginning at offset 0.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinFSFork_forkWrite (
	JNIEnv * env,  jobject ignored,
	jshort refNum,
	jbyteArray jBytes,  jint count,
	jintArray actualCount )
{
	jbyte * bufPtr;
	SInt64 offset = 0;
	ByteCount wrote;
	OSErr err;

	// Even if GetByteArrayElements() might make a copy, we'll release it using JNI_ABORT,
	// to avoid write-back.
	bufPtr = (*env)->GetByteArrayElements( env, jBytes, NULL );
	if ( bufPtr == NULL )
		return ( -1 );

	err = FSWriteFork( refNum, fsAtMark, offset, count, bufPtr, &wrote );

	// Always have to release array elements, so do it with same mode no matter what.
	(*env)->ReleaseByteArrayElements( env, jBytes, bufPtr, JNI_ABORT );

	// Also have to return actual count, which caller will qualify against returned OSErr.
	(*env)->SetIntArrayRegion( env, actualCount, 0, 1, (jint *) &wrote );

	return ( err );
}


// ## FIXME: Prior to 10.1, there's no FNSubscription support at all.
// ## To port to 10.0, all this code must be removed.

// If null, then _TinWatcher_nativeInit() will perform once-only initialization.
static JavaVM *theJVM = NULL;

static jmethodID callbackMethod;

static FNSubscriptionUPP callbackUPP;


// The refCon is a global-ref of the TinWatcher whose callback() is invoked.
void
mySubscriptionCallback(
	FNMessage message,  OptionBits flags,  void * refCon,
	FNSubscriptionRef subscription )
{
//	fprintf( stderr, "mySubscriptionCallback(): msg: %d \n", (int) message );

	// Must have reasonable values for static variables, and for refCon.
	// This is slower, but if you're desperate for blinding speed, why use Java?
	if ( theJVM != NULL  &&  callbackMethod != NULL  &&  refCon != NULL )
	{
		JNIEnv * env;
		jint result;

		result = (*theJVM)->AttachCurrentThread( theJVM,  (void **) &env, NULL );
		if ( result == 0 )
		{
			jobject tinWatcher = (jobject) refCon;
			(*env)->CallVoidMethod( env, tinWatcher, callbackMethod, (jint) message );

//			fprintf( stderr, "returned from callback()\n" );
		}

		// N.B. Do not detach thread from JVM. 
		// If we're called from the same native thread in the future, it will already be attached.
		// If we're called from a different native thread, then it will be attached anew.
		// Besides, we don't know that the current native thread wasn't already attached,
		// so detaching it could well be The Wrong Thing.
		// The main reason for detaching would be to destroy the JVM,
		// which is unlikely to occur in an FNSubscription callback.
	}
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinWatcher
 * Method:    nativeInit
 * Signature: ()I
 */

/**
	 This idempotent method performs all the native-side once-only initialization.
	 It's synchronized on the class-lock so it can't be called re-entrantly from other threads.
	 That shouldn't happen, but one never knows.
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinWatcher_nativeInit (
	JNIEnv * env,  jclass watcherClass )
{
	jint result = 0;

	if ( theJVM == NULL )
	{
		// This should never fail.
		result = (*env)->GetJavaVM( env, &theJVM );

		// The callback method in TinWatcher is: void callback(int)
		callbackMethod = (*env)->GetMethodID( env, watcherClass, "callback", "(I)V" );

		// All subscriptions use the same callback function and UPP.
		// This UPP is never disposed, and exists as long as the process runs.
		// I could dispose of it in JNI_OnUnload(), but I see little point.
		callbackUPP = NewFNSubscriptionUPP( mySubscriptionCallback );

//		fprintf( stderr, "nativeInit(): theJVM: %lx, callbackMethod: %lx, callbackUPP: %lx \n",
//				(long int)theJVM, (long int)callbackMethod, (long int)callbackUPP );

	}

	return ( result );
}



// The "magic token" is a type-punned pointer to a magic_struct
// in a calloc()'ed block of memory.
struct magic_struct
{
	// The global-ref of the TinWatcher that's getting callbacks.
	// Also passed as refCon to subscription's callback function.
	jobject watcher;

	// The FNSubscriptionRef to be freed by _killToken().
	FNSubscriptionRef subscription;	
};

typedef  struct magic_struct  magic;


// Destroys all non-null contents of the magic block, then frees the block itself.
// For convenience, always returns 0, which is what _TinWatcher_makeToken()
// returns on all failures.
jint
freeMagic( magic *magicBlock, JNIEnv *env )
{
	if ( magicBlock != NULL )
	{
		// Stop subscription before deleting global-ref.
		// That way, the subscription callbacks will cease before the global-ref does.
		if ( magicBlock->subscription != NULL )
			FNUnsubscribe( magicBlock->subscription );

		if ( magicBlock->watcher != NULL )
			(*env)->DeleteGlobalRef( env, magicBlock->watcher );

		free( magicBlock );
	}

	return ( 0 );
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinWatcher
 * Method:    makeToken
 * Signature: (Ljava/lang/String;Z)I
 */

/**
	 Create a magic token for this TinWatcher.
	  int makeToken( String targetPath, boolean ignoreBroadcasts );
*/

JNIEXPORT jint JNICALL 
Java_glguerin_io_imp_mac_macosx_TinWatcher_makeToken (
	JNIEnv * env,  jobject tinWatcher,
	jstring pathname, jboolean ignoreBroadcasts )
{
	OptionBits flags;
	const jbyte * utfPathname;
	jboolean isCopy;
	FSRef dirRef;
	magic * magicPtr;
	jobject globalRef;
	Boolean isDir;
	OSStatus result;

	flags = kNilOptions;
	if ( ignoreBroadcasts )
		flags |= kFNNoImplicitAllSubscription;

	// Turn the pathname into an FSRef, on the hypothesis that FNSubscribe() works
	// better than FNSubscribeByPathname().  Neither one works, however.  <sigh>

	// Get target's full pathname as UTF8 nul-terminated bytes.
	utfPathname = (*env)->GetStringUTFChars( env, pathname, &isCopy );
	if ( utfPathname == NULL )
		return ( 0 );  // OutOfMemoryError will be thrown

//	fprintf( stderr, "makeToken(): flags: %X, path: %s \n", (unsigned int)flags, utfPathname );

	// Set up an FSRef for given path.  Regardless of result, always free the UTF bytes.
	// Only then do we check result of FSPathMakeRef(), which must refer to a directory.
	result = FSPathMakeRef( (UInt8 *) utfPathname, &dirRef, &isDir );
	(*env)->ReleaseStringUTFChars( env, pathname, utfPathname );
	if ( result != 0  ||  ! isDir )
		return ( 0 );

	// Allocate one piece of magic.
	magicPtr = (magic *) calloc( 1, sizeof( magic ) );
	if ( magicPtr == NULL )
		return ( 0 );

	// Transform local-ref into a global-ref, usable in other threads and contexts.
	magicPtr->watcher = globalRef = (*env)->NewGlobalRef( env, tinWatcher );
	if ( globalRef == NULL )
		return ( freeMagic( magicPtr, env ) );

	// Subscribe to the directory, using dirRef.
	result = FNSubscribe( &dirRef, callbackUPP,  (void *) globalRef,  flags,  &(magicPtr->subscription) );
	if ( result != 0 )
		return ( freeMagic( magicPtr, env ) );

//	fprintf( stderr, "makeToken(): ref: %lx, subscrip: %lx \n",
//			(long int)magicPtr->watcher, (long int)magicPtr->subscription );

	// On success, return magicPtr masquerading as a jint, i.e. a magic token.
	return ( (jint) magicPtr );	
}


/*
 * Class:     glguerin_io_imp_mac_macosx_TinWatcher
 * Method:    killToken
 * Signature: (I)V
 */

/**
	 Destroy the magic token held by this TinWatcher.
*/

JNIEXPORT void JNICALL 
Java_glguerin_io_imp_mac_macosx_TinWatcher_killToken (
	JNIEnv * env,  jobject tinWatcher,
	jint token )
{
	// Everything is done by freeMagic().
	freeMagic( (magic *) token, env );
}
