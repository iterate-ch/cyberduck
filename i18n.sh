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
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  Bug fixes, suggestions and comments should be sent to:
#  dkocher@cyberduck.ch

#!/bin/bash
        
usage() {
	echo ""
	echo "    Usage: i18n.sh --extractstrings"
	echo "    Usage: i18n.sh [-l <language>] --status"
	echo "    Usage: i18n.sh [-l <language>] --init"
	echo "    Usage: i18n.sh [-l <language>] [-n <nib>] [--force] --update"
	echo ""
	echo "<language> must be Japanese.lproj, French.lproj, Spanish.lproj, ..."
	echo "<nib> must be Preferences.nib, Main.nib, ..."
	echo ""
	echo "Call with no parameters to update all languages and all nib files"
	echo ""
}

init() {
	mkdir -p $language
	for nibfile in `ls English.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
		echo "Copying $nibfile"
		nib=`basename $nibfile .nib`
		cp -R English.lproj/$nibfile $language/$nibfile
		rm -rf $language/$nibfile/CVS
		nibtool --localizable-strings $language/$nibfile > $language/$nib.strings
	done
	cp English.lproj/Localizable.strings $language/
	cp English.lproj/InfoPlist.strings $language/
	cp English.lproj/License.txt $language/
}

open() {
	nib=`basename $nibfile .nib`
	if [ "$language" = "all" ] ; then
	{
		for lproj in `ls . | grep lproj`; do
			if [ $lproj != "English.lproj" ]; then
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

extractstrings() {
    echo "*** Extracting strings from Obj-C source files (genstrings)..."
    genstrings -j -a -q -o English.lproj source/ch/cyberduck/ui/cocoa/*.java
    echo "*** Extracting strings from Java source files (genstrings)..."
    genstrings -j -a -q -o English.lproj source/ch/cyberduck/core/*.java
    genstrings    -a -q -o English.lproj source/ch/cyberduck/ui/cocoa/*.m
    genstrings -j -a -q -o English.lproj source/ch/cyberduck/core/ftp/*.java
    genstrings -j -a -q -o English.lproj source/ch/cyberduck/core/ftps/*.java
    genstrings -j -a -q -o English.lproj source/ch/cyberduck/core/sftp/*.java
}

status() {
	if [ "$language" = "all" ] ; then
	{
		for lproj in `ls . | grep lproj`; do
			language=$lproj;
			if [ $language != "English.lproj" ]; then
				echo "*** Status of $language Localization...";
				/usr/local/bin/polyglot -l `basename $language .lproj` .
			fi;
		done;
	}
	else
	{
		echo "*** Status of $language Localization...";
		/usr/local/bin/polyglot -l `basename $language .lproj` .
	}
	fi;
}

nib() {
    updateNibFromStrings;
    udpateStringsFromNib;
}

updateNibFromStrings() {
	rm -rf $language/$nibfile.bak 
    mv $language/$nibfile $language/$nibfile.bak

    if($force == true); then
	{
		# force update
		echo "*** Updating $nib... (force) in $language..."
		nibtool --write $language/$nibfile --dictionary $language/$nib.strings English.lproj/$nibfile
	}
    else
	{
		# incremental update
		echo "*** Updating $nib... (incremental) in $language..."
		nibtool --write $language/$nibfile \
				--incremental $language/$nibfile.bak \
				--dictionary $language/$nib.strings English.lproj/$nibfile
	}
    fi;
    cp -R $language/$nibfile.bak/CVS $language/$nibfile/CVS
}

udpateStringsFromNib() {
    echo "*** Updating $nib.strings in $language..."
        
	rm $language/$nib.strings.bak 
    mv $language/$nib.strings $language/$nib.strings.bak
    nibtool --previous English.lproj/$nibfile \
            --incremental $language/$nibfile \
            --localizable-strings English.lproj/$nibfile > $language/$nib.strings
}

update() {
	if [ "$language" = "all" ] ; then
		{
			echo "*** Updating all localizations...";
			for lproj in `ls . | grep lproj`; do
				language=$lproj;
				if [ $language != "English.lproj" ]; then
				{
					echo "*** Updating $language Localization...";
					if [ "$nibfile" = "all" ] ; then
						echo "*** Updating all NIBs...";
						for nibfile in `ls $language | grep .nib | grep -v ~.nib | grep -v .bak`; do
							nib=`basename $nibfile .nib`
							nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
							nib;
						done;
					fi;
					if [ "$nibfile" != "all" ] ; then
							nib=`basename $nibfile .nib`
							nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
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
					nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
					nib;
				done;
			fi;
			if [ "$nibfile" != "all" ] ; then
			{
				nib=`basename $nibfile .nib`;
				nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
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
			*)  
				echo "Option [$1] not one of  [--extractstrings, --status, --update, --open, --init]"; # Error (!)
				exit 1
			;; # Abort Script Now
	esac;
done;
usage;
