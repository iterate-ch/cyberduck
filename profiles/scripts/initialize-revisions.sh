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
if [ ! $TMPDIR ]; then
    TMPDIR=$RUNNER_TEMP
fi
echo "Finding profiles in $DIRECTORY"

for path in $DIRECTORY/*.cyberduckprofile; do
  filename=$(basename "$path")
  echo "Read versions for $filename"
  # Read every version
  git log --reverse --format=%H "$filename" |
    while read hash; do
      git show "$hash:profiles/$filename" > "$TMPDIR/$filename"
      echo "Sync $filename@$hash"
      env "s3.metadata.default=Content-Type=application/xml" duck -qy --username $AWS_ACCESS_KEY_ID --password $AWS_SECRET_ACCESS_KEY --existing overwrite --upload "$TARGET/$filename" "$TMPDIR/$filename"
      rm "$TMPDIR/$filename"
    done
done

duck -qy --username $AWS_ACCESS_KEY_ID --password $AWS_SECRET_ACCESS_KEY --purge "$TARGET/"
