#!/usr/bin/env bash
#
# Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
# https://cyberduck.io/
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#

TARGET='s3:/profiles.cyberduck.io'
DIRECTORY=$1
if [ ! $DIRECTORY ]; then
  DIRECTORY=$(dirname "$(cd -P -- "$(dirname -- "$0")" && pwd -P)")
fi
echo "Finding profiles in $DIRECTORY"

# Upload changed profiles by comparing checksum
env "s3.metadata.default=Content-Type=application/xml" duck -y --username $AWS_ACCESS_KEY_ID --password $AWS_SECRET_ACCESS_KEY --existing compare --upload "$TARGET/" $DIRECTORY/*.cyberduckprofile

# Delete profiles no longer maintained
duck -y --username $AWS_ACCESS_KEY_ID --password $AWS_SECRET_ACCESS_KEY --list $TARGET/ |
  while read name; do
    [ -z "$name" ] && continue
    
    if [ ! -f "$DIRECTORY/$name" ]; then
      echo "Deleting $name"
      duck -y --username $AWS_ACCESS_KEY_ID --password $AWS_SECRET_ACCESS_KEY --delete "$TARGET/$name"
    fi
  done
