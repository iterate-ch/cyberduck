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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.dracoon.sdk.crypto.model.UserPrivateKey;
import com.dracoon.sdk.crypto.model.UserPublicKey;

public class CryptoDecryptPrivateKeyTest {

    @Before
    public void setUnsafeProperty() {
        System.setProperty("org.bouncycastle.asn1.allow_unsafe_integer", "true");
    }

    @Test
    public void testDecryptPrivateKey() throws Exception {

        final String pk = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n" +
            "MIIFnDCBxQYJKoZIhvcNAQUNMIG3MIGVBgkqhkiG9w0BBQwwgYcEgYAzxG7mDxu4\n" +
            "TYtue+YQykOFgXlbFSJ9wbvVhVlbJP/16jewb4DhMZ/EWGzufLougL1WCwMc0BGM\n" +
            "wiozMk3Xl7kYCILBe78dsfS8bDZG9bMyS3PBuUn5Zuo4hhNMMtrJ4qYIOXYKsjml\n" +
            "rq5aipZXy3d/nfaHpGYTLGJTvSyZKDaGaQICJxAwHQYJYIZIAWUDBAEqBBDBxJJq\n" +
            "F0v+NbVvALtyDqOyBIIE0Kx8ce2gVTbC4FAowW/cV2texJWMZDe/i/NcTYvXide7\n" +
            "d8abj23zxWMT6+h89d27mXOD5BQMyXZSXMNAzHoMq2T0/uJwHNbm9VSXIZWrJ1wT\n" +
            "AHaipyUY5oQ3L0eu5CWqXISspRL7xlLAuIptpHunJIcvMvrK1o9xpJoEiZWUmq67\n" +
            "1tkDBH3hokcWU9u2YSwQsuZGHY1S0Z9QTiCry1nL8iz1ceRSee6J+ysjnbpxPsO1\n" +
            "UnNoKLIYYSm8Fep6O635Tc/XkytMF5GpJvLJNYZXuxTIRGUGcnk82hEwmTPp9ANc\n" +
            "4vycg7c4M7VMqPZsGjQtoYZLE0EQ243BZQdKG7pob9HIt+zTLmBtbMPrTsxCMEeS\n" +
            "eX5BrK7RDmHz4bM+Jv9KsO7xjWX4lZRXgRAkiMt1tV+IRVfsjDKEJzzyfvzIhWXe\n" +
            "rnK4A8o+jubXLxrUMFWdRvb5iGDCGxmb2JrjiFDXNIudMpB3vFWEulfvMw7cdGDo\n" +
            "C7FurwwycJhtRAvJFAZ7C//pyH8Gl8XjDjY/F44ORxQP9qfACier4vy5go2JMljM\n" +
            "3YQB5m3xeXXz5pzkTX0ept1JKAAovedAP27+7WS0ziuUS1g1Er2cRrFKHCi9Sb0W\n" +
            "QqZqeWBloG/t4axCd8De/YTXTX4dgYD+BiTzBTRA33JTYrkl9b8MrbEaZgwHWsr1\n" +
            "6+YgQjYTRPQmiV8NBTm3/zbFV+Y/KBgd+P12ISKfONLZzhzE5+7CE0gAEotb1fCY\n" +
            "o6ds0WJim2rT0Sfb5WA/xTtlerM7mYK0troOhO30NIUUmg4wtS6SsRqrS1KzuVx0\n" +
            "AS7cyI9Zb0Rz+oc/MhnLf51hx+c3wBBnBdgrxtXRrBbtRIlRUSzhtJBcpELwQGFu\n" +
            "4wB3Tjil3HwF4JjTRUBy4TUf41W8aQtkoi8JOCnS6gy87fbiEBay7Pi/0DOF7HSI\n" +
            "iyZhKfOMJunTlTdmIbg/d024tVJqMnVZrTKeLwlJX2M9qncIQKJB7SYzfSdbvV92\n" +
            "A4AXKKwQoXg/jiC3u4XSiqQ10lVBKsantH1Gargm2uYNMJTJhu3qaec19wbvREuA\n" +
            "X2bHNQ3NZJThUEcgfCQdcoIWWVfuWWVBd+v8Ta9/vPHG6za9IG5wWMkB+fV5wE6H\n" +
            "GbFlV9shwWZLLN0VYAAir31r5DAwAJiQ3lhregBgUYkNMRdBhsn3jLGzPxByGw/3\n" +
            "QXRJUq0+ZnKlI1y83XsfDTrsyY3jgua1P4MOrk5Khkpz6pcz3nGIsAXpsymPfFYm\n" +
            "rTXEaVVe9F+5UM8LzIQH7TyGL7c4mGTnUTBY3LE27whIusvZDq21maRU+8A84bFn\n" +
            "xCvFHXQs53NV3Tb4ZHl5SrdJllC/MYrj4xau3g1cRS0xJSUaW7FLa2AgC9/Ps8uL\n" +
            "7nVnJ6nKLBMejBfrK0+uFqGuGDP4jOl8H26V+dycY/5ecBYhF9Bts5aKEFhkqibG\n" +
            "2Qt3tuP/WvAzJAAFw+YYTKUyW8YvDh1hf74cD8RxiNYZ0UuVoqADxbycNZ3+26rC\n" +
            "lnXEetmYD961oHZclzis4CUcDbLRbgcmv/fvpoj9OIgBOumB/1ku0ZmicXvlJ6wD\n" +
            "S+2l6QSAHZF3rmD0D7lSGMu0rdF648h8HkLsoABONdoHJrCE5ehnBvSLd34Hdwt7\n" +
            "-----END ENCRYPTED PRIVATE KEY-----\n";

        final UserPrivateKey privateKey = new UserPrivateKey(UserKeyPair.Version.RSA2048, pk.toCharArray());
        final UserPublicKey publicKey = new UserPublicKey(UserKeyPair.Version.RSA2048, "pubkey".toCharArray());
        final UserKeyPair pair = new UserKeyPair(privateKey, publicKey);

        Assert.assertTrue(Crypto.checkUserKeyPair(pair, "abcdabc1".toCharArray()));
    }
}
