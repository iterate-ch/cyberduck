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
	echo "Usage: udpateLocalization.sh -l <language> -n <nib>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       nib must be Preferences.nib, Main.nib, ..."
	echo "Call with no parameters to update all languages and all nib files"
}

updateNibFromStrings() {
    nib=`basename $nibfile .nib`

    rm -rf $language.lproj/$nibfile.bak
    mv $language.lproj/$nibfile $language.lproj/$nibfile.bak

    if($force == true); then
        {
            # force update
            echo "Updating $nib... (force)"
            nibtool --write $language.lproj/$nibfile --dictionary $language.lproj/$nib.strings English.lproj/$nibfile
        }
    else
        {
            # incremental update
            echo "Updating $nib... (incremental)"
            nibtool --write $language.lproj/$nibfile \
                    --incremental $language.lproj/$nibfile.bak \
                    --dictionary $language.lproj/$nib.strings English.lproj/$nibfile
        
        }
    fi;
    cp -R $language.lproj/$nibfile.bak/CVS $language.lproj/$nibfile/CVS
}

#udpateStringsFromNib() {
#    for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
#        nib=`basename $nibfile .nib`
#    
#        echo "Updating $nib.strings in $language.lproj..."
#        
#        mv $language.lproj/$nib.strings $language.lproj/$nib.strings.bak
#        nibtool --previous English.lproj/$nibfile \
#                --incremental $language.lproj/$nibfile \
#                --localizable-strings English.lproj/$nibfile > $language.lproj/$nib.strings
#    done;
#}

language="all";
nibfile="all";
force=false;

while [ "$1" != "" ] # When there are arguments...
do
    case "$1" in 
        -h | --help) 
            usage;
            exit 0;
        ;; 
        -l | --language) 
            shift;
            language=$1
        ;;
        -n | --nib) 
            shift;
            nibfile=$1
        ;;
        -force) 
            shift;
            force=true
        ;;
        *)  echo "Option [$1] not one of  [--langauge, --nib]";       # Error (!)
            exit 1
        ;; # Abort Script Now
    esac;
    shift;
done;

echo "Language:$language";
echo "Nib:$nibfile";

if [ "$language" == "all" ] ; then
    {
        echo "*** Updating all localizations...";
        for lproj in `ls . | grep lproj`; do
            language=`basename $lproj .lproj`
            
            if [ $language != "English" ]; then
            {
                echo "*** Updating $language Localization...";
                if [ nibfile == "all" ] ; then
                    echo "*** Updating all NIBs...";
                    for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
                        updateNibFromStrings;
                    done;
                fi;
                if [ nibfile != "all" ] ; then
                        updateNibFromStrings;
                fi;
            }
            fi;
        done;
    }
    echo "*** Done.";
    exit 0;
fi;


if [ "$language" != "all" ] ; then
    {
        echo "*** Updating $language Localization...";
        if [ nibfile == "all" ] ; then
            echo "*** Updating all NIBs...";
            for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
                updateNibFromStrings;
            done;
        fi;
        if [ nibfile != "all" ] ; then
        {
            updateNibFromStrings;
        }
        fi;
    }
    echo "*** Done.";
    exit 0;
fi;

exit 1;




