#!/bin/bash

usage ( ) {
	echo "Usage: updateStringsFromNib.sh <language>"
	echo "       language must be English, French, Spanish, ..."
}

if [ $# -ne 1 ] ; then \
	usage ; \
	exit 1
fi

language=$1

for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
    nib=`basename $nibfile .nib`

    echo "Updating $nib.strings in $language.lproj..."
    
    mv $language.lproj/$nib.strings $language.lproj/$nib.strings.bak
    nibtool --previous English.lproj/$nibfile --incremental $language.lproj/$nibfile --localizable-strings English.lproj/$nibfile > $language.lproj/$nib.strings
done
exit 0

