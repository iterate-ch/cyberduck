#!/bin/bash

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

workdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
nibtool="ibtool"
convertstrings="ruby convertstrings.rb"
base_language="en.lproj"
arch="$(/usr/bin/arch)"
tx="$workdir/tx --root-config $workdir/.transifexrc"
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
	for n in `ls $base_language | grep $extension | grep -v ~$extension`; do
	{
		echo "Copying $n"
		cp -R $base_language/$n "$language"/$n
	}
	done
	for stringsfile in `ls $base_language | grep .strings | grep -v .strings.1`; do
	{
		echo "Copying $stringsfile"
		cp -R $base_language/"$stringsfile" "$language"/"$stringsfile"
	}
	done
	cp $base_language/License.txt $language/
}

test() {
	for lproj in $(ls . | grep lproj); do
		language=$lproj;
		run;
	done;
}

run() {
	echo "Running app using `basename $language .lproj` on $arch...";
    basedir="$( cd "$workdir/../../../.." && pwd )"
	CA_DEBUG_TRANSACTIONS=1 arch -arch $arch $basedir/osx/target/Cyberduck.app/Contents/MacOS/Cyberduck -AppleLanguages "(`basename $language .lproj`)"
}

nib() {
    if [ $language != $base_language ]; then
    {
        #Changes to the .strings has precedence over the NIBs
        import_strings;
	}
	fi
	#Update the .strings with new values from NIBs
	export_strings;
}

import_strings() {
	if [ true == "$force" ] ; then
	{
		# force update
		echo "*** Updating $nib... (force) in $language..."
		$nibtool    --reference-external-strings-file \
                --write $language/$nib$extension \
                --import-strings-file $language/$nib.strings \
                $base_language/$nib$extension
	}
  else
	{
		# incremental update
		echo "*** Updating $nib... (incremental) in $language..."
	  # Checkout previous version from base language
    git show $(git log -2 --format="%H" $base_language/$nib$extension | tail -n 1):./$base_language/$nib$extension > $base_language/$nib$extension.prev.xib
		$nibtool    --write $language/$nib$extension \
                --incremental-file $language/$nib$extension \
                --previous-file $base_language/$nib$extension.prev.xib \
                --import-strings-file $language/$nib.strings \
                --localize-incremental \
                $base_language/$nib$extension
    rm $base_language/$nib$extension.prev.xib
	}
  fi;
}

export_strings() {
	echo "*** Updating $nib.strings in $language..."
	$nibtool --export-strings-file $language/$nib.strings $language/$nib$extension
}

export_strings_legacy() {
	if [ "$language" = "all" ] ; then
		echo "*** Updating all localizations...";
    for lproj in $(ls . | grep lproj); do
      language=$lproj;
      echo "*** Updating $language Localization...";
        if [ "$stringsfile" = "all" ] ; then
          for n in $(ls $language | grep $extension); do
            nib=`basename $n $extension`
            echo "Update $language/$nib.strings.1 from $base_language/$nib.strings"
            $convertstrings $base_language/$nib.strings $language/$nib.strings > $language/$nib.strings.1
          done;
        else
          nib=`basename $stringsfile ".strings"`
          echo "Update $language/$nib.strings.1 from $base_language/$nib.strings"
          $convertstrings $base_language/$nib.strings $language/$nib.strings > $language/$nib.strings.1
        fi;
    done;
	else
		echo "*** Updating $language Localization...";
      if [ "$stringsfile" = "all" ] ; then
        for n in `ls $language | grep $extension`; do
          nib=`basename $n $extension`
          echo "Update $language/$nib.strings.1 from $base_language/$nib.strings"
          $convertstrings $base_language/$nib.strings $language/$nib.strings > $language/$nib.strings.1
        done;
      else
        nib=`basename $stringsfile ".strings"`
        echo "Update $language/$nib.strings.1 from $base_language/$nib.strings"
        $convertstrings $base_language/$nib.strings $language/$nib.strings > $language/$nib.strings.1
      fi;
	fi;
}

update() {
	if [ "$language" = "all" ] ; then
	{
		echo "*** Updating all localizations...";
		for lproj in `ls . | grep lproj`; do
			language=$lproj;
            echo "*** Updating $language Localization...";
            if [ "$nibfile" = "all" ] ; then
                echo "*** Updating all NIBs...";
                for n in `ls $language | grep $extension`; do
                    nib=`basename $n $extension`
                    nib;
                done;
            fi;
            if [ "$nibfile" != "all" ] ; then
                    nib=`basename $nibfile $extension`
                    nib;
            fi;
		done;
	}
	else
	{
		echo "*** Updating $language Localization...";
		if [ "$nibfile" = "all" ] ; then
			echo "*** Updating all NIBs...";
			for n in `ls $language | grep $extension`; do
				nib=`basename $n $extension`;
				nib;
			done;
		fi;
		if [ "$nibfile" != "all" ] ; then
		{
			nib=`basename $nibfile $extension`;
			nib;
		}
		fi;
	}
	fi;
}

tx_push() {
	if [ "$language" = "all" ] ; then
	{
        echo "*** Updating all localizations...";
        if [ "$stringsfile" = "all" ] ; then
            echo "*** Updating all .strings...";
            for s in `ls en.lproj | grep .strings | grep -v .strings.1`; do
                strings=`basename $s .strings`
                echo "*** Updating $strings.strings...";
                $tx push --force --translation cyberduck.$strings
            done;
        fi;
        if [ "$stringsfile" != "all" ] ; then
            strings=`basename $stringsfile .strings`
            echo "*** Updating $strings.strings...";
            $tx push --force --translation cyberduck.$strings
        fi;
	}
	else
	{
		    echo "*** Updating $language Localization...";
        if [ "$stringsfile" = "all" ] ; then
            echo "*** Updating all .strings...";
            for s in `ls en.lproj | grep .strings | grep -v .strings.1`; do
                strings=`basename $s .strings`
                lang=`basename $language .lproj`
                echo "*** Updating $strings.strings...";
                $tx push --force --translation -l $lang cyberduck.$strings
            done;
        fi;
        if [ "$stringsfile" != "all" ] ; then
            strings=`basename $stringsfile .strings`
            lang=`basename $language .lproj`
            echo "*** Updating $strings.strings...";
            if [ "$lang" = "en" ] ; then
              $tx push --source cyberduck.$strings
            else
              $tx push --force --translation -l $lang cyberduck.$strings
            fi;
        fi;
	}
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
          for s in `ls en.lproj | grep .strings | grep -v .strings.1`; do
              strings=`basename $s .strings`
              lang=`basename $language .lproj`
              echo "*** Updating $strings.strings...";
              $tx pull --force -l $lang cyberduck.$strings
          done;
      fi;
      if [ "$stringsfile" != "all" ] ; then
          strings=`basename "$stringsfile" .strings`
          lang=`basename "$language" .lproj`
          echo "*** Updating $strings.strings...";
          $tx pull --force -l $lang cyberduck.$strings
      fi;
		done;
	}
	else
	{
		echo "*** Updating $language Localization...";
        if [ "$stringsfile" = "all" ] ; then
            echo "*** Updating all .strings...";
            for s in `ls en.lproj | grep .strings | grep -v .strings.1`; do
                strings=`basename "$s" .strings`
                lang=`basename "$language" .lproj`
                echo "*** Updating $strings.strings...";
                $tx pull --force -l $lang cyberduck.$strings
            done;
        fi;
        if [ "$stringsfile" != "all" ] ; then
            strings=`basename "$stringsfile" .strings`
            lang=`basename "$language" .lproj`
            echo "*** Updating $strings.strings...";
            $tx pull --force -l $lang cyberduck.$strings
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
