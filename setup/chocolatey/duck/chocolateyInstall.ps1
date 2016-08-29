$packageName = 'duck'
$installerType = 'exe'
$url = 'https://dist.duck.sh/${FEED}/duck-${VERSION}.${REVISION}.exe'
$silentArgs = '/quiet'
$validExitCodes = @(0)
$checksum = '${CHECKSUM}'
$checksumType = 'sha256'

Install-ChocolateyPackage "$packageName" "$installerType" "$silentArgs" "$url" -validExitCodes $validExitCodes -Checksum "$checksum" -ChecksumType "$checksumType"
