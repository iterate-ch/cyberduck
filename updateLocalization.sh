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

    echo "Updating $nib in $language.lproj..."

    rm -rf $language.lproj/$nibfile.bak
    mv $language.lproj/$nibfile $language.lproj/$nibfile.bak

    # force update
    # nibtool --write $language.lproj/$nibfile --dictionary $language.lproj/$nib.strings English.lproj/$nibfile

    # incremental update
    nibtool --write $language.lproj/$nibfile \
            --incremental $language.lproj/$nibfile.bak \
            --dictionary $language.lproj/$nib.strings English.lproj/$nibfile

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


if [ $# = 2 ] ; then
    {
        language=$1;
        nibfile=$2;
        echo "*** Updating $language Localization..."
        # First update the nib file with the modifications in the .strings file. 
        # The .strings file is always assumed to be newer - this means that all 
        # modifications should not be made in the .nib file directly but in 
        # the .strings file instead.
        updateNibFromStrings;
        exit 0;
    }
fi;
if [ $# = 1 ] ; then
    {
       nibfile=$1;
        for lproj in `ls . | grep lproj`; do
            language=`basename $lproj .lproj`
            
            if [ $language != "English" ]; then
            {
                echo "*** Updating $language Localization..."
    
#                for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
            
                # Update the nib file with the modifications in the .strings file. 
                # The .strings file is always assumed to be newer - this means that all 
                # modifications should not be made in the .nib file directly but in 
                # the .strings file instead.
                updateNibFromStrings;
 #               done;
            }
            fi;
        done;
        exit 0;
    }
fi;
usage;
exit 1;