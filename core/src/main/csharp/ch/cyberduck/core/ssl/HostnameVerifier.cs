// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using System.Globalization;
using System.Security.Cryptography.X509Certificates;
using java.lang;
using java.util;
using org.apache.logging.log4j;
using ArrayList = System.Collections.ArrayList;
using Exception = System.Exception;
using String = System.String;
using X509Certificate = java.security.cert.X509Certificate;

namespace Ch.Cyberduck.Core.Ssl
{
    public class HostnameVerifier
    {
        private static readonly Logger Log = LogManager.getLogger(typeof (HostnameVerifier).FullName);

        /// <summary>
        // RFC2818 - HTTP Over TLS, Section 3.1
        // http://www.ietf.org/rfc/rfc2818.txt
        // 
        // 1.  if present MUST use subjectAltName dNSName as identity
        // 1.1.    if multiples entries a match of any one is acceptable
        // 1.2.    wildcard * is acceptable
        // 2.  URI may be an IP address -> subjectAltName.iPAddress
        // 2.1.    exact match is required
        // 3.  Use of the most specific Common Name (CN=) in the Subject
        // 3.1    Existing practice but DEPRECATED
        /// </summary>
        /// <param name="javaCert"></param>
        /// <param name="cert"></param>
        /// <param name="targetHost"></param>
        /// <returns></returns>
        ///todo We should get rid of the java certificate parameter. Means to find an easy way to get the subjectAltNames (see http://www.java2s.com/Open-Source/CSharp/2.6.4-mono-.net-core/System.Net/System/Net/ServicePointManager.cs.htm)        
        public static bool CheckServerIdentity(X509Certificate javaCert,
                                               X509Certificate2 cert, string targetHost)
        {
            try
            {
                /*
                 SubjectAltName ::= GeneralNames

                 GeneralNames :: = SEQUENCE SIZE (1..MAX) OF GeneralName

                 GeneralName ::= CHOICE {
                  otherName                       [0]     OtherName,
                  rfc822Name                      [1]     IA5String,
                  dNSName                         [2]     IA5String,
                  x400Address                     [3]     ORAddress,
                  directoryName                   [4]     Name,
                  ediPartyName                    [5]     EDIPartyName,
                  uniformResourceIdentifier       [6]     IA5String,
                  iPAddress                       [7]     OCTET STRING,
                  registeredID                    [8]     OBJECT IDENTIFIER}

                 SubjectAltName is of form \"rfc822Name=<email>,
                 dNSName=<host name>, uri=<http://host.com/>,
                 ipaddress=<address>, guid=<globally unique id>

                */

                Collection ext = javaCert.getSubjectAlternativeNames();
                // subjectAltName
                if (null != ext && ext.size() > 0)
                {
                    for (Iterator i = ext.iterator(); i.hasNext();)
                    {
                        List item = (List) i.next();
                        Integer type = (Integer) item.get(0);
                        switch (type.intValue())
                        {
                            case 0:
                                continue; // SubjectAltName of type OtherName not
                            case 1:
                                continue; // rfc822Name

                            case 2:
                                if (Match(targetHost, (String) item.get(1))) //dNSName
                                {
                                    return true;
                                }
                                break;
                            case 3:
                                continue; // x400Address
                            case 4:
                                continue; // directoryName
                            case 5:
                                continue; // ediPartyName
                            case 6:
                                //todo shouldn't we handle uri as well? check spec.
                                continue; // uri                                
                            case 7:
                                if (targetHost.Equals((String) item.get(1))) // ipaddress, exact match required
                                {
                                    return true;
                                }
                                break;
                            default:
                                continue;
                        }
                    }
                }
                // Common Name (CN=)
                return Match(GetCommonName(cert), targetHost);
            }
            catch (Exception e)
            {
                Log.error("ERROR processing certificate: {0}", e);
                return false;
            }
        }

        /// <summary>
        /// Get CN from certificate
        /// </summary>
        /// <param name="cert"></param>
        /// <returns>only the first CN found</returns>
        private static string GetCommonName(X509Certificate2 cert)
        {
            ArrayList dn = RFC2253.parseStrict(cert.SubjectName.Name);
            for (int i = 0; i < dn.Count; ++i)
            {
                RFC2253.RDNPair p = (RFC2253.RDNPair) dn[i];
                if ("CN".Equals(p.key))
                {
                    return RFC2253.unescape(p.value);
                }
            }
            return null;
        }

        public static bool Match(string hostname, string pattern)
        {
            // check if this is a pattern
            int index = pattern.IndexOf('*');
            if (index == -1)
            {
                // not a pattern, do a direct case-insensitive comparison
                return (String.Compare(hostname, pattern, true, CultureInfo.InvariantCulture) == 0);
            }

            // check pattern validity
            // A "*" wildcard character MAY be used as the left-most name component in the certificate.

            // unless this is the last char (valid)
            if (index != pattern.Length - 1)
            {
                // then the next char must be a dot .'.
                if (pattern[index + 1] != '.')
                    return false;
            }

            // only one (A) wildcard is supported
            int i2 = pattern.IndexOf('*', index + 1);
            if (i2 != -1)
                return false;

            // match the end of the pattern
            string end = pattern.Substring(index + 1);
            int length = hostname.Length - end.Length;
            // no point to check a pattern that is longer than the hostname
            if (length <= 0)
                return false;

            if (String.Compare(hostname, length, end, 0, end.Length, true, CultureInfo.InvariantCulture) != 0)
                return false;

            // special case, we start with the wildcard
            if (index == 0)
            {
                // ensure we hostname non-matched part (start) doesn't contain a dot
                int i3 = hostname.IndexOf('.');
                return ((i3 == -1) || (i3 >= (hostname.Length - end.Length)));
            }

            // match the start of the pattern
            string start = pattern.Substring(0, index);
            return (String.Compare(hostname, 0, start, 0, start.Length, true, CultureInfo.InvariantCulture) == 0);
        }
    }
}
