usage ( ) {
	echo "Usage: udpateNibFromStrings.sh <language>"
	echo "       language must be English, French, Spanish, ..."
}

if [ $# -ne 1 ] ; then \
	usage ; \
	exit 1
fi

language=$1
for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
    nib=`basename $nibfile .nib`
    rm -rf $language.lproj/$nibfile.bak
    mv $language.lproj/$nibfile $language.lproj/$nibfile.bak
    nibtool --write $language.lproj/$nibfile --dictionary $language.lproj/$nib.strings English.lproj/$nibfile

done
exit 0

