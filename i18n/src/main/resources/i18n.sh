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

workdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
nibtool="ibtool"
convertstrings="ruby convertstrings.rb"
base_language="en.lproj"
arch="x86_64"
tx="/usr/local/bin/tx"
extension=".xib"

usage() {
	echo ""
	echo "	  Usage: i18n.sh [-l <language>] --status"
	echo "	  Usage: i18n.sh [-l <language>] --init"
	echo "	  Usage: i18n.sh [-l <language>] [-n <$extension>] [--force] --update"
	echo "	  Usage: i18n.sh [-l <language>] --run"
	echo ""
	echo "<language> must be Japanese.lproj, French.lproj, Spanish.lproj, ..."
	echo "<$extension> must be Preferences.$extension, Main.$extension, ..."
	echo ""
	echo "Call with no parameters to update all languages and all $extension files"
	echo ""
}

init() {
	mkdir -p $language
	for n in `ls $base_language | grep .$extension | grep -v ~.$extension`; do
	{
		echo "Copying $n"
		cp -R $base_language/$n $language/$n
	}
	done
	for stringsfile in `ls $base_language | grep .strings | grep -v .strings.1`; do
	{
		echo "Copying $stringsfile"
		cp -R $base_language/$stringsfile $language/$stringsfile
	}
	done
	cp $base_language/License.txt $language/
}

test() {
	for lproj in `ls . | grep lproj`; do
		language=$lproj;
		run;
	done;
}

run() {
	echo "Running app using `basename $language .lproj`...";
    basedir="$( cd "$workdir/../../../.." && pwd )"
	arch -arch $arch $basedir/osx/target/Cyberduck.app/Contents/MacOS/Cyberduck -AppleLanguages "(`basename $language .lproj`)"
}

nib() {
	#Changes to the .strings has precedence over the NIBs
	import_strings;
	#Update the .strings with new values from NIBs
	export_strings;
}

import_strings() {
	if($force == true); then
	{
		# force update
		echo "*** Updating $nib... (force) in $language..."
		$nibtool --reference-external-strings-file \
		        --write $language/$nibfile \
		        --import-strings-file $language/$nib.strings \
		        $base_language/$nibfile
	}
	else
	{
		# incremental update
		echo "*** Updating $nib... (incremental) in $language..."
		$nibtool --write $language/$nibfile \
		        --incremental-file $language/$nibfile \
		        --previous-file $base_language/$nibfile \
				--import-strings-file $language/$nib.strings \
				--localize-incremental $base_language/$nibfile
	}
	fi;
}

export_strings() {
	echo "*** Updating $nib.strings in $language..."
	$nibtool --export-strings-file $language/$nib.strings $language/$nibfile
}

export_strings_legacy() {
	for lproj in `ls . | grep lproj`; do
		language=$lproj;
		echo "*** Updating $language Localization...";
		for nibfile in `ls $language | grep $extension`; do
			nib=`basename $nibfile $extension`
			echo "Update $language/$nib.strings.1 from $base_language/$nib.strings"
			$convertstrings $base_language/$nib.strings $language/$nib.strings > $language/$nib.strings.1
		done;
	done;
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
					for nibfile in `ls $language | grep $extension`; do
						nib=`basename $nibfile $extension`
						$nibtool --export-strings-file $base_language/$nib.strings $base_language/$nibfile
						nib;
					done;
				fi;
				if [ "$nibfile" != "all" ] ; then
						nib=`basename $nibfile $extension`
						$nibtool --export-strings-file $base_language/$nib.strings $base_language/$nibfile
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
			for nibfile in `ls $language | grep $extension`; do
				nib=`basename $nibfile $extension`;
                $nibtool --export-strings-file $base_language/$nib.strings $base_language/$nibfile
				nib;
			done;
		fi;
		if [ "$nibfile" != "all" ] ; then
		{
			nib=`basename $nibfile $extension`;
            $nibtool --export-strings-file $base_language/$nib.strings $base_language/$nibfile
			nib;
		}
		fi;
	}
	fi;
}

tx_push() {
    echo "*** Updating all localizations...";
    if [ "$stringsfile" = "all" ] ; then
        echo "*** Updating all .strings...";
        for stringsfile in `ls en.lproj | grep .strings | grep -v .strings.1`; do
            strings=`basename $stringsfile .strings`
            echo "*** Updating $strings.strings...";
            $tx --traceback set --auto-local -r cyberduck.$strings '<lang>'.lproj/$strings.strings --source-language en --type=STRINGS --execute
            $tx --traceback push --source --translations --resource=cyberduck.$strings --force --no-interactive --skip
        done;
    fi;
    if [ "$stringsfile" != "all" ] ; then
        strings=`basename $stringsfile .strings`
        echo "*** Updating $strings.strings...";
        $tx --traceback set --auto-local -r cyberduck.$strings '<lang>'.lproj/$strings.strings --source-language en --type=STRINGS --execute
        $tx --traceback push --source --translations --resource=cyberduck.$strings --force --no-interactive --skip
    fi;
}

tx_pull() {
	if [ "$language" = "all" ] ; then
	{
		echo "*** Updating all localizations...";
		for lproj in `ls . | grep lproj`; do
			language=$lproj;
            lang=`basename $language .lproj`
            echo "*** Updating $language Localization...";
            if [ "$stringsfile" = "all" ] ; then
                echo "*** Updating all .strings...";
                for stringsfile in `ls en.lproj | grep .strings | grep -v .strings.1`; do
                    strings=`basename $stringsfile .strings`
                    lang=`basename $language .lproj`
                    echo "*** Updating $strings.strings...";
                    $tx --traceback pull -l $lang --resource=cyberduck.$strings --force
                done;
            fi;
            if [ "$stringsfile" != "all" ] ; then
                strings=`basename $stringsfile .strings`
                lang=`basename $language .lproj`
                echo "*** Updating $strings.strings...";
                $tx --traceback pull -l $lang --resource=cyberduck.$strings --force
            fi;
		done;
	}
	else
	{
		echo "*** Updating $language Localization...";
        if [ "$stringsfile" = "all" ] ; then
            echo "*** Updating all .strings...";
            for stringsfile in `ls en.lproj | grep .strings | grep -v .strings.1`; do
                strings=`basename $stringsfile .strings`
                lang=`basename $language .lproj`
                echo "*** Updating $strings.strings...";
                $tx --traceback pull --source -l $lang --resource=cyberduck.$strings --force
            done;
        fi;
        if [ "$stringsfile" != "all" ] ; then
            strings=`basename $stringsfile .strings`
            lang=`basename $language .lproj`
            echo "*** Updating $strings.strings...";
            $tx --traceback pull --source -l $lang --resource=cyberduck.$strings --force
        fi;
	}
	fi;
}

language="all";
nibfile="all";
stringsfile="all";
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
			--strings)
				shift;
				stringsfile=$1;
				echo "Using strings:$stringsfile";
				shift;
			;;
			--convertstrings)
				export_strings_legacy;
				exit 0;
				echo "*** DONE. ***";
			;;
			-f | --force)
				force=true;
				shift;
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
			-u | --update)
				echo "Updating localization...";
				update;
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
			--tx-push)
				echo "Updating .tx...";
				tx_push;
				echo "*** DONE. ***";
				exit 0;
			;;
			--tx-pull)
				echo "Updating .tx...";
				tx_pull;
				echo "*** DONE. ***";
				exit 0;
			;;
			--test)
				test;
				echo "*** DONE. ***";
				exit 0;
			;;
			*)
				echo "Option [$1] not one of  [--update, --init, --tx-push, --tx-pull]"; # Error (!)
				exit 1
			;; # Abort Script Now
	esac;
done;

usage;
