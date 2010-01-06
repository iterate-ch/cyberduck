#  Copyright (c) 2003 David Kocher. All rights reserved.
#  http://cyberduck.ch/
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
#  GNU General Public License for more details.
#
#  Bug fixes, suggestions and comments should be sent to:
#  dkocher@cyberduck.ch

#!/bin/bash

# From Xcode 2.5 Developer Tools installation
nibtool="nibtool"
base_language="en.lproj"
arch="i386"

usage() {
	echo ""
	echo "	  Usage: i18n.sh --extractstrings"
	echo "	  Usage: i18n.sh [-l <language>] --status"
	echo "	  Usage: i18n.sh [-l <language>] --init"
	echo "	  Usage: i18n.sh [-l <language>] [-n <nib>] [--force] --update"
	echo "	  Usage: i18n.sh [-l <language>] --run"
	echo ""
	echo "<language> must be Japanese.lproj, French.lproj, Spanish.lproj, ..."
	echo "<nib> must be Preferences.nib, Main.nib, ..."
	echo ""
	echo "Call with no parameters to update all languages and all nib files"
	echo ""
}

init() {
	mkdir -p $language
	for nibfile in `ls $base_language | grep .nib | grep -v ~.nib | grep -v .bak`; do
	{
		echo "Copying $nibfile"
		nib=`basename $nibfile .nib`
		cp -R $base_language/$nibfile $language/$nibfile
		rm -rf $language/$nibfile/.svn
		$nibtool --localizable-strings $language/$nibfile > $language/$nib.strings
	}
	done
	cp $base_language/Localizable.strings $language/
	cp $base_language/Crash.strings $language/
	cp $base_language/Status.strings $language/
	cp $base_language/Error.strings $language/
	cp $base_language/Credentials.strings $language/
	cp $base_language/Configuration.strings $language/
	cp $base_language/IDisk.strings $language/
	cp $base_language/Growl.strings $language/
	cp $base_language/S3.strings $language/
	cp $base_language/Mosso.strings $language/
	cp $base_language/Keychain.strings $language/
	cp $base_language/Archive.strings $language/
	cp $base_language/License.strings $language/
	cp $base_language/InfoPlist.strings $language/
	cp $base_language/License.txt $language/
}

test() {
	for lproj in `ls . | grep lproj`; do
		language=$lproj;
		run;
	done;
}

open() {
	nib=`basename $nibfile .nib`
	if [ "$language" = "all" ] ; then
	{
		for lproj in `ls . | grep lproj`; do
			if [ $lproj != $base_language ]; then
				echo "*** Opening $lproj/$nib.strings"
				/usr/bin/open $lproj/$nib.strings
			fi;
		done;
	}
	else
	{
		echo "*** Opening $language/$nib.strings"
		/usr/bin/open $language/$nib.strings
	}
	fi;
}

run() {
	echo "Running app using `basename $language .lproj`...";
	arch -arch $arch ./build/Release/Cyberduck.app/Contents/MacOS/Cyberduck -AppleLanguages "(`basename $language .lproj`)"
}

extractstrings() {
	echo "*** Extracting strings from Obj-C source files (genstrings)..."
	genstrings -j -a -q -o $base_language source/ch/cyberduck/ui/cocoa/*.java
	echo "*** Extracting strings from Java source files (genstrings)..."
	genstrings -j -a -q -o $base_language source/ch/cyberduck/core/*.java
	genstrings	  -a -q -o $base_language source/ch/cyberduck/ui/cocoa/*.m
	genstrings -j -a -q -o $base_language source/ch/cyberduck/core/ftp/*.java
	genstrings -j -a -q -o $base_language source/ch/cyberduck/core/ftps/*.java
	genstrings -j -a -q -o $base_language source/ch/cyberduck/core/sftp/*.java
}

status() {
	if [ "$language" = "all" ] ; then
	{
		for lproj in `ls . | grep lproj`; do
			language=$lproj;
			if [ $language != "$base_language" ]; then
				echo "*** Status of $language Localization...";
				/usr/local/bin/polyglot -b en -l `basename $language .lproj` .
			fi;
		done;
	}
	else
	{
		echo "*** Status of $language Localization...";
		/usr/local/bin/polyglot -b en -l `basename $language .lproj` .
	}
	fi;
}

nib() {
	#Changes to the .strings has precedence over the NIBs
	updateNibFromStrings;
	#Update the .strings with new values from NIBs
	udpateStringsFromNib;
}

updateNibFromStrings() {
	rm -rf $language/$nibfile.bak 
	mv $language/$nibfile $language/$nibfile.bak

	if($force == true); then
	{
		# force update
		echo "*** Updating $nib... (force) in $language..."
		$nibtool --write $language/$nibfile --dictionary $language/$nib.strings $base_language/$nibfile
	}
	else
	{
		# incremental update
		echo "*** Updating $nib... (incremental) in $language..."
		$nibtool --write $language/$nibfile \
				--incremental $language/$nibfile.bak \
				--dictionary $language/$nib.strings $base_language/$nibfile
	}
	fi;
	cp -R $language/$nibfile.bak/.svn $language/$nibfile/.svn
	rm -rf $language/$nibfile.bak 
}

udpateStringsFromNib() {
	echo "*** Updating $nib.strings in $language..."
	$nibtool --previous $base_language/$nibfile \
			--incremental $language/$nibfile \
			--localizable-strings $base_language/$nibfile > $language/$nib.strings
}

update() {
	if [ "$language" = "all" ] ; then
	{
		echo "*** Updating all localizations...";
		for lproj in `ls . | grep lproj`; do
			language=$lproj;
			if [ $language != $base_language ]; then
			{
				echo "*** Updating $language Localization...";
				if [ "$nibfile" = "all" ] ; then
					echo "*** Updating all NIBs...";
					for nibfile in `ls $language | grep .nib | grep -v ~.nib | grep -v .bak`; do
						nib=`basename $nibfile .nib`
						$nibtool --localizable-strings $base_language/$nibfile > $base_language/$nib.strings
						nib;
					done;
				fi;
				if [ "$nibfile" != "all" ] ; then
						nib=`basename $nibfile .nib`
						$nibtool --localizable-strings $base_language/$nibfile > $base_language/$nib.strings
						nib;
				fi;
			}
			fi;
		done;
	}
	else
	{
		echo "*** Updating $language Localization...";
		if [ "$nibfile" = "all" ] ; then
			echo "*** Updating all NIBs...";
			for nibfile in `ls $language | grep .nib | grep -v ~.nib | grep -v .bak`; do
				nib=`basename $nibfile .nib`;
				$nibtool --localizable-strings $base_language/$nibfile > $base_language/$nib.strings
				nib;
			done;
		fi;
		if [ "$nibfile" != "all" ] ; then
		{
			nib=`basename $nibfile .nib`;
			$nibtool --localizable-strings $base_language/$nibfile > $base_language/$nib.strings
			nib;
		}
		fi;
	}
	fi;
}

language="all";
nibfile="all";
force=false;

while [ "$1" != "" ] # When there are arguments...
	do case "$1" in 
			-l | --language)
				shift;
				language=$1;
				echo "Using Language:$language";
				shift;
			;;
			-n | --nib) 
				shift;
				nibfile=$1;
				echo "Using Nib:$nibfile";
				shift;
			;;
			-f | --force) 
				force=true;
				shift;
			;;
			-g | --extractstrings)
				extractstrings;
				exit 0;
				echo "*** DONE. ***";
			;;
			-h | --help) 
				usage;
				exit 0;
				echo "*** DONE. ***";
			;; 
			-i | --init)
				echo "Init new localization...";
				init;
				echo "*** DONE. ***";
				exit 0;
			;; 
			-s | --status)
				echo "Status of localization...";
				status;
				echo "*** DONE. ***";
				exit 0;
			;; 
			-u | --update)
				echo "Updating localization...";
				update;
				echo "*** DONE. ***";
				exit 0;
			;;
			-o | --open)
				echo "Opening localization .strings files...";
				open;
				echo "*** DONE. ***";
				exit 0;
			;; 
			-r | --run)
				run;
				echo "*** DONE. ***";
				exit 0;
			;; 
			--arch)
				shift;
				arch=$1;
				echo "Running architecture:$arch";
				shift;
			;;
			--test)
				test;
				echo "*** DONE. ***";
				exit 0;
			;; 
			*)	
				echo "Option [$1] not one of  [--extractstrings, --status, --update, --open, --init]"; # Error (!)
				exit 1
			;; # Abort Script Now
	esac;
done;

usage;
