$packageName = 'duck'
$installerType = 'exe'
$silentArgs = '/quiet'
$url = 'https://dist.duck.sh/${FEED}/duck-${VERSION}.${REVISION}.exe'
$checksum = '${CHECKSUM}'
$checksumType = 'sha256'
$validExitCodes = @(0)

Install-ChocolateyPackage -PackageName "$packageName" `
                          -FileType "$installerType" `
                          -SilentArgs "$silentArgs" `
                          -Url "$url" `
                          -ValidExitCodes $validExitCodes `
                          -Checksum "$checksum" `
                          -ChecksumType "$checksumType"
