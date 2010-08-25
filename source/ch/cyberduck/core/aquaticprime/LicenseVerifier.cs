/*
	LicenseVerifier.cs
	AquaticPrime Framework

	Copyright (c) 2008, Kyle Kinkade
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification,
	are permitted provided that the following conditions are met:
    *   Redistributions of source code must retain the above copyright notice,
 	    this list of conditions and the following disclaimer.
	*   Redistributions in binary form must reproduce the above copyright notice,
		this list of conditions and the following disclaimer in the documentation and/or
		other materials provided with the distribution.
	*   Neither the name of Aquatic nor the names of its contributors may be used to 
		endorse or promote products derived from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
   FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
   IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Xml;

namespace Ch.Cyberduck.Core.Aquaticprime
{
    public sealed class LicenseVerifier
    {
        private static readonly string PublicKey =
            "0xAF026CFCF552C3D09A051124A596CEF7BBB26B15629504CD163B09675BE507C9C526ED3DBFCB91B78F718E0886A18400B56BC00E9213228CD6D6E9C84D8B6099AA3DE6E6F46F6CC7970982DE93A2A7318351FDFA25AE75B403996E50BB40643384214234E84EDA3E518772A4FF57FE29DD7C77A5EEB14C9023CA18FEC63236EF";

        private static readonly string PublicKeyBase64;

        private static readonly RSACryptoServiceProvider RSA = new RSACryptoServiceProvider();
        private static readonly LicenseVerifier _instance = new LicenseVerifier();

        static LicenseVerifier()
        {
            //if publicKey is Hex, convert to Base64
            if (IsHex(PublicKey))
                PublicKeyBase64 = Hex2B64(PublicKey);

            RSAParameters rsp = new RSAParameters();

            //set publicKey
            rsp.Modulus = Convert.FromBase64String(PublicKeyBase64);

            //we know that the exponent is supposed to be 3
            rsp.Exponent = Convert.FromBase64String("Aw==");
            RSA.ImportParameters(rsp);
        }

        private LicenseVerifier()
        {
        }

        public static LicenseVerifier Instance
        {
            get { return _instance; }
        }

        public bool VerifyLicenseData(String licenseFile)
        {
            XmlDocument xmlDoc = new XmlDocument();
            xmlDoc.XmlResolver = null; // prevent doctype resolving
            xmlDoc.Load(licenseFile);

            try
            {
                SortedList<string, string> dict = CreateDictionaryForLicenseData(xmlDoc);

                //retrieves Signature from SortedList, then removes it
                string signature = dict["Signature"];
                dict.Remove("Signature");

                IList<string> values = dict.Values;

                //append values together to form comparable signature
                StringBuilder dataString = new StringBuilder();
                foreach (string v in values)
                {
                    dataString.Append(v);
                }

                //create byte arrays of both signature and appended values
                byte[] signaturebytes = Convert.FromBase64String(signature);
                byte[] plainbytes = Encoding.UTF8.GetBytes(dataString.ToString());

                //then return whether or not license is valid
                return RSA.VerifyData(plainbytes, "SHA1", signaturebytes);
            }
            catch
            {
                return false;
            }
        }

        public String GetValue(String licenseFile, String property)
        {
            XmlDocument license = new XmlDocument {XmlResolver = null};
            license.Load(licenseFile);
            SortedList<string, string> data = CreateDictionaryForLicenseData(license);
            string value;
            data.TryGetValue(property, out value);
            return value;
        }

        private static SortedList<string, string> CreateDictionaryForLicenseData(XmlDocument xmlDoc)
        {
            SortedList<string, string> result = new SortedList<string, string>();
            XmlNode xnode = xmlDoc.LastChild.SelectSingleNode("dict");

            //return if node is null, or does not contain children, or contains an odd amount of children
            if (xnode == null || !xnode.HasChildNodes || (xnode.ChildNodes.Count%2 != 0))
                return result;

            //iterate through the nodes, adding them as key/value pair to SortedList
            for (int i = 0; i < xnode.ChildNodes.Count; i++)
                result.Add(xnode.ChildNodes[i].InnerText, xnode.ChildNodes[++i].InnerText);

            return result;
        }

        private static bool IsHex(string sHex)
        {
            Regex r = new Regex(@"^(0x)?[A-Fa-f0-9]+$");
            return r.IsMatch(sHex);
        }

        private static string Hex2B64(string sHex)
        {
            //removes the 0x from the string if it contains it
            sHex = sHex.Replace("0x", string.Empty);

            //tries to determine of the string is Hexidecimal
            if (!IsHex(sHex))
                return String.Empty;

            //creates a byte array for the Hex
            byte[] bytes = new byte[sHex.Length/2];

            //iterates through the string, parsing into bytes
            int b = 0;
            for (int i = 0; i < sHex.Length; i += 2)
            {
                bytes[b] = byte.Parse(sHex.Substring(i, 2), NumberStyles.HexNumber);
                b++;
            }

            //returns it as a Base64 string
            return Convert.ToBase64String(bytes);
        }
    }
}