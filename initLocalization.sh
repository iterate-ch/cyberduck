usage ( ) {
	echo "Usage: initLocalization.sh <language>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       as the base language, English is assumed"
}

if [ $# -ne 1 ] ; then \
	usage ; \
	exit 1
fi

language=$1;

mkdir -p $language.lproj

for nibfile in `ls English.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
    echo Copying $nibfile
    cp -R English.lproj/$nibfile $language.lproj/$nibfile
done

exit 0

