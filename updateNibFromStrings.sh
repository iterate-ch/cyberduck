usage ( ) {
	echo "Usage: udpateNibFromStrings.sh <language> <name>"
	echo "       language must be English, French, Spanish, ..."
	echo "       name must be the filename of the nib"
}

if [ $# -ne 2 ] ; then \
	usage ; \
	exit 1
fi

language=$1
nibfile=$2
nib=`basename $nibfile .nib`

rm -rf $language.lproj/$nibfile.bak
mv $language.lproj/$nibfile $language.lproj/$nibfile.bak
nibtool --write $language.lproj/$nibfile --dictionary $language.lproj/$nib.strings English.lproj/$nibfile

exit 0

