#!/bin/bash
        
usage ( ) {
	echo "Usage: udpateLocalization.sh <language>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       as the base language, English is assumed"
}

if [ $# = 1 ] ; then
{
    language=$1;
    echo "Updating $language Localization..."
    # First update the nib file with the modifications in the .strings file. 
    # The .strings file is always assumed to be newer - this means that all 
    # modifications should not be made in the .nib file directly but in 
    # the .strings file instead.
    ./updateNibFromStrings.sh $language
    # Update the localized .strings file with new strings added to to the base (English) version.
    ./updateStringsFromNib.sh $language
}
else 
{
    for lproj in `ls . | grep lproj`; do
        language=`basename $lproj .lproj`
        
        echo "Updating $language Localization..."
    
        # First update the nib file with the modifications in the .strings file. 
        # The .strings file is always assumed to be newer - this means that all 
        # modifications should not be made in the .nib file directly but in 
        # the .strings file instead.
        ./updateNibFromStrings.sh $language
        # Update the localized .strings file with new strings added to to the base (English) version.
        ./updateStringsFromNib.sh $language
    done;
}
fi;
exit 0

