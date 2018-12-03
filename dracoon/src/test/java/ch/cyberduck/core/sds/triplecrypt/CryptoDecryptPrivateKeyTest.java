package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoConstants;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.dracoon.sdk.crypto.model.UserPrivateKey;
import com.dracoon.sdk.crypto.model.UserPublicKey;

public class CryptoDecryptPrivateKeyTest {

    @Test
    public void testDecryptPrivateKey() throws Exception {

        final String pk = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n" +
            "MIIFKzBVBgkqhkiG9w0BBQ0wSDAnBgkqhkiG9w0BBQwwGgQUmkLyZ7aqpBxIt1p3\n" +
            "fh1NHXNjkYwCAicQMB0GCWCGSAFlAwQBKgQQwt50hDFaJeroFEE4+hNqdASCBNDM\n" +
            "41b+QZKvW5uzmhkRKU5tyhvholv/0jvUAQGBKGPS6sueWahUch6pWrZNCUfo/bIf\n" +
            "V3sjKNysJpNtWjIbCzhpehOkT4s0bItJR9YV4+VU3VEmaWCDSDV44KLq8Zwphdhd\n" +
            "a3l1bb0xWUnOc/jX4GbY2hvlxgpgEduzd+xjku+bs3Tlca4vN4s1bYu7yL6e5LV4\n" +
            "1pOMyXZxm+qt/OknrCHQuxD9mXxwkKErF4FAQBz/7cGzrk+/8Sa5xFRMyg8ERmAc\n" +
            "KZwcxJhYaGodiatpvPyDAhO4BlBL/2RbTw1d3PkNw1wCTXyy1yVcYlo2S/ALZGpP\n" +
            "Wy7c1hEzd/ntaPQ33jTYdrzNfwVDZCZFSTcTWXUEaCrYt+QgwuQNq45uWyoPGiGh\n" +
            "FWoVCeLK4VJtAicylIGSey51YvAVmC7LbztAG7rGXHb3PvbbmDSlt8E0NUdiqr6T\n" +
            "DBDP+OT0aEhG3TdZd4PEhONlMQsi/bg1sAI6ZhkJ48xd66a+MdvyJ1BERKOccG/L\n" +
            "t30Gx43+Srup9wKkyTQ5ZOm9rIJRGiqzOU2mLr1BCpWQH8Fa76S1qPoAbCRuyhSS\n" +
            "iVrNomqSfjQS7sFLCTArP5rFlVyQ0s2aPe7CD7ZxiqJvKhu3bowD6XOsQ7xi24vb\n" +
            "j5z8OveD5cVttoKidmymuHhBwRnpUaMpuh0KSlI/XPUPXkFspgoI9xAk7amcRaFs\n" +
            "mpOuG3LrIetrW9ojR82DKzHUibOcbQLFqhsA4U8VJPcgDY3Tjzz21La7LYxC6Cc8\n" +
            "nCFLdSKZE+iei2a3BqWHBUdwpoR/5uQcGbM++WLFD2QKDdLYgGf0cqDRPwlwv0No\n" +
            "c68qxKUQGf43TYFy8KJObO0nNNEX6kjjxYAT+xOac2i7imHMjYBXSgTCmFc7nN3X\n" +
            "G4JCJJFj9QyqlI+6XP50FbHgJM2yil+hKAwWbp6+cNdDyNhVrVa/7pQwcQn6ZwsH\n" +
            "eSE3CkmI9q7FYcGV6MUFLbEPqvSrL0claaVB8dm6W++yU/QyHFjBMN/IpRDdUQ2A\n" +
            "WGKKFFhcvk2tI6QWpkUgHtuncs78iNDrqgmbTyN6bjBmCZ3vTv7cwV5kf4xxkbq8\n" +
            "kJN+EyfWNMjIvFpeBVF3YG/bLBTa+QBdQmIOXNnuazdHh8r5oiRLbxCBcbma+X/J\n" +
            "Rf9V0NJFdwxF1wm95jSj/ZivVyKUyVMQHHw22Q/7wqSknpDJ0x5Ruhq7rbHSbfNv\n" +
            "MLVYNu9lNVhF2KuhTiKi2xFA7+bai7yIF9mB9NUC9YDOkJYc6AqqOq/0IaHzdc95\n" +
            "UcCojoq8jP7tN3CNLWHZx/r3kUrZ7Cs1VBgR0XwKK2ko55k7ba2inkPRDJEzoDI8\n" +
            "RDrm7U87zAllWySsSFbWL88ulFE7U6SAEULMzOIO5n4tO2sBuv48uADWs7BCl57i\n" +
            "A2YEuqZc567lUgW4sCgURcXLZW6f9AKJpvB1Y7YudekX97USw3aKlSA9lb2sOIRE\n" +
            "YyMSvuBlOOMrm2O++wzfx8k+Ww6lkAEj6tRGYRSasigF+cyDjRioVV/q/LEl977V\n" +
            "1XXxRIw9SlOfWvjURsILoj1YINtFKXf2fPnGY3W6P+BIK91B2hBnVGG3EExHoxup\n" +
            "lD/wZM8NDhfudi3u7+bY7XTvEoP8CeesnD101MfHig==\n" +
            "-----END ENCRYPTED PRIVATE KEY-----";

        final UserKeyPair pair = new UserKeyPair();
        final UserPrivateKey privateKey = new UserPrivateKey();
        privateKey.setPrivateKey(pk);
        privateKey.setVersion(CryptoConstants.DEFAULT_VERSION);
        pair.setUserPrivateKey(privateKey);
        final UserPublicKey publicKey = new UserPublicKey();
        publicKey.setPublicKey(StringUtils.EMPTY);
        pair.setUserPublicKey(publicKey);
        Assert.assertTrue(Crypto.checkUserKeyPair(pair, "abcd"));
    }
}
