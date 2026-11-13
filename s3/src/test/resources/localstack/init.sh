#!/bin/sh
# Create buckets expected by tests. Executed by LocalStack when ready.
set -e
create() {
    if [ "$2" = "us-east-1" ]; then
        awslocal s3api create-bucket --bucket "$1" --region us-east-1 $3
    else
        awslocal s3api create-bucket --bucket "$1" --region "$2" --create-bucket-configuration "LocationConstraint=$2" $3
    fi
}
create test-eu-central-1-cyberduck eu-central-1
create versioning-test-eu-central-1-cyberduck eu-central-1
awslocal s3api put-bucket-versioning --bucket versioning-test-eu-central-1-cyberduck --versioning-configuration Status=Enabled
create test-eu-central-1-objectlock eu-central-1 --object-lock-enabled-for-bucket
create test-us-east-1-cyberduck us-east-1
create sse-test-us-east-1-cyberduck us-east-1
create test-eu-west-1-cyberduck eu-west-1
create test-ap-southeast-2-cyberduck ap-southeast-2
create test-acl-us-east-1-cyberduck us-east-1
create test-lifecycle-us-east-1-cyberduck us-east-1
create test-eu-central-1-cyberduck-unsupportedprefix eu-central-1
create test-eu-central-1-cyberduck-ownerenforced eu-central-1 "--object-ownership BucketOwnerEnforced"
create test-eu-central-1-acl-disabled eu-central-1 "--object-ownership BucketOwnerEnforced"
echo "localstack init done"
