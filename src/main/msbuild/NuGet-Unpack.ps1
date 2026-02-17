[CmdletBinding()]
param (
    # Source path, where all .nupkgs are stored.
    [Parameter()]
    [string]
    $PackagesListPath
)

if (-not (Test-Path -Path $PackagesListPath -PathType Leaf)) {
    return;
}

$NugetParent = [System.IO.Path]::GetDirectoryName($PackagesListPath)
$PackagesPath = "$NugetParent\packages"

foreach ($line in Get-Content $PackagesListPath) {
    $line = $line.Trim()
    if ($line -notmatch "\s*(?<group>[^:]+):(?<artifact>[^:]+):(?<type>[^:]+):(?<version>[^:]+):(?<scope>[^:]+):(?<path>.*)$") {
        continue
    }

    $artifact = $Matches["artifact"]
    $version = $Matches["version"]

    $NupkgFile = $Matches["path"]
    $PackageName = [System.IO.Path]::GetFileNameWithoutExtension((tar.exe tf "$NupkgFile" '*.nuspec'))
    $TargetDirectory = "$PackagesPath\$PackageName\$version"
    New-Item -Force -Type Directory $TargetDirectory
    Get-ChildItem $TargetDirectory | Remove-Item -Recurse -Force
    tar.exe xf $NupkgFile -C $TargetDirectory
    Set-Content "$TargetDirectory\.nupkg.metadata" '{}'
}
