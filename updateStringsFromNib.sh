usage ( ) {
	echo "Usage: updateStringsFromNib.sh <language> <name>"
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

mv $language.lproj/$nib.strings $language.lproj/$nib.strings.bak
nibtool --previous English.lproj/$nibfile --incremental $language.lproj/$nibfile --localizable-strings English.lproj/$nibfile > $language.lproj/$nib.strings

exit 0

