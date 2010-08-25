// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using System;
using System.Collections;
using System.Diagnostics;

namespace Ch.Cyberduck.Core
{
    internal class RFC2253
    {
        private static string hexvalid = "0123456789abcdefABCDEF";
        private static string special = ",=+<>#;";

        internal static ArrayList
            parse(string data)
        {
            ArrayList results = new ArrayList();
            ArrayList current = new ArrayList();
            int pos = 0;
            while (pos < data.Length)
            {
                current.Add(parseNameComponent(data, ref pos));
                eatWhite(data, ref pos);
                if (pos < data.Length && data[pos] == ',')
                {
                    ++pos;
                }
                else if (pos < data.Length && data[pos] == ';')
                {
                    ++pos;
                    results.Add(current);
                    current = new ArrayList();
                }
                else if (pos < data.Length)
                {
                    throw new ParseException("expected ',' or ';' at `" + data.Substring(pos) + "'");
                }
            }
            if (current.Count > 0)
            {
                results.Add(current);
            }

            return results;
        }

        internal static ArrayList
            parseStrict(string data)
        {
            ArrayList results = new ArrayList();
            int pos = 0;
            while (pos < data.Length)
            {
                results.Add(parseNameComponent(data, ref pos));
                eatWhite(data, ref pos);
                if (pos < data.Length &&
                    (data[pos] == ',' || data[pos] == ';'))
                {
                    ++pos;
                }
                else if (pos < data.Length)
                {
                    throw new ParseException("expected ',' or ';' at `" + data.Substring(pos) + "'");
                }
            }
            return results;
        }

        public static string
            unescape(string data)
        {
            if (data.Length == 0)
            {
                return data;
            }

            if (data[0] == '"')
            {
                if (data[data.Length - 1] != '"')
                {
                    throw new ParseException("unescape: missing \"");
                }
                //
                // Return the string without quotes.
                //
                return data.Substring(1, data.Length - 2);
            }

            //
            // Unescape the entire string.
            //
            string result = "";
            if (data[0] == '#')
            {
                int pos = 1;
                while (pos < data.Length)
                {
                    result += unescapeHex(data, pos);
                    pos += 2;
                }
            }
            else
            {
                int pos = 0;
                while (pos < data.Length)
                {
                    if (data[pos] != '\\')
                    {
                        result += data[pos];
                        ++pos;
                    }
                    else
                    {
                        ++pos;
                        if (pos >= data.Length)
                        {
                            throw new ParseException("unescape: invalid escape sequence");
                        }
                        if (special.IndexOf(data[pos]) != -1 || data[pos] != '\\' || data[pos] != '"')
                        {
                            result += data[pos];
                            ++pos;
                        }
                        else
                        {
                            result += unescapeHex(data, pos);
                            pos += 2;
                        }
                    }
                }
            }
            return result;
        }

        private static int
            hexToInt(char v)
        {
            if (v >= '0' && v <= '9')
            {
                return v - '0';
            }
            if (v >= 'a' && v <= 'f')
            {
                return 10 + (v - 'a');
            }
            if (v >= 'A' && v <= 'F')
            {
                return 10 + (v - 'A');
            }
            throw new ParseException("unescape: invalid hex pair");
        }

        private static char
            unescapeHex(string data, int pos)
        {
            Debug.Assert(pos < data.Length);
            if (pos + 2 >= data.Length)
            {
                throw new ParseException("unescape: invalid hex pair");
            }
            return (char) (hexToInt(data[pos])*16 + hexToInt(data[pos + 1]));
        }

        private static RDNPair
            parseNameComponent(string data, ref int pos)
        {
            RDNPair result = parseAttributeTypeAndValue(data, ref pos);
            while (pos < data.Length)
            {
                eatWhite(data, ref pos);
                if (pos < data.Length && data[pos] == '+')
                {
                    ++pos;
                }
                else
                {
                    break;
                }
                RDNPair p = parseAttributeTypeAndValue(data, ref pos);
                result.value += "+";
                result.value += p.key;
                result.value += '=';
                result.value += p.value;
            }
            return result;
        }

        private static RDNPair
            parseAttributeTypeAndValue(string data, ref int pos)
        {
            RDNPair p = new RDNPair();
            p.key = parseAttributeType(data, ref pos);
            eatWhite(data, ref pos);
            if (pos >= data.Length)
            {
                throw new ParseException("invalid attribute type/value pair (unexpected end of data)");
            }
            if (data[pos] != '=')
            {
                throw new ParseException("invalid attribute type/value pair (missing =). remainder: " +
                                         data.Substring(pos));
            }
            ++pos;
            p.value = parseAttributeValue(data, ref pos);
            return p;
        }

        private static string
            parseAttributeType(string data, ref int pos)
        {
            eatWhite(data, ref pos);
            if (pos >= data.Length)
            {
                throw new ParseException("invalid attribute type (expected end of data)");
            }

            string result = "";

            //
            // RFC 1779.
            // <key> ::= 1*( <keychar> ) | "OID." <oid> | "oid." <oid>
            // <oid> ::= <digitstring> | <digitstring> "." <oid>
            // RFC 2253:
            // attributeType = (ALPHA 1*keychar) | oid
            // keychar    = ALPHA | DIGIT | "-"
            // oid        = 1*DIGIT *("." 1*DIGIT)
            //
            // In section 4 of RFC 2253 the document says:
            // Implementations MUST allow an oid in the attribute type to be
            // prefixed by one of the character strings "oid." or "OID.".
            //
            // Here we must also check for "oid." and "OID." before parsing
            // according to the ALPHA KEYCHAR* rule.
            // 
            // First the OID case.
            //
            if (Char.IsDigit(data[pos]) ||
                (data.Length - pos >= 4 && (data.Substring(pos, 4) == "oid." ||
                                            data.Substring(pos, 4) == "OID.")))
            {
                if (!Char.IsDigit(data[pos]))
                {
                    result += data.Substring(pos, 4);
                    pos += 4;
                }

                while (true)
                {
                    // 1*DIGIT
                    while (pos < data.Length && Char.IsDigit(data[pos]))
                    {
                        result += data[pos];
                        ++pos;
                    }
                    // "." 1*DIGIT
                    if (pos < data.Length && data[pos] == '.')
                    {
                        result += data[pos];
                        ++pos;
                        // 1*DIGIT must follow "."
                        if (pos < data.Length && !Char.IsDigit(data[pos]))
                        {
                            throw new ParseException("invalid attribute type (expected end of data)");
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else if (Char.IsUpper(data[pos]) ||
                     Char.IsLower(data[pos]))
            {
                //
                // The grammar is wrong in this case. It should be ALPHA
                // KEYCHAR* otherwise it will not accept "O" as a valid
                // attribute type.
                //
                result += data[pos];
                ++pos;
                // 1* KEYCHAR
                while (pos < data.Length &&
                       (Char.IsDigit(data[pos]) ||
                        Char.IsUpper(data[pos]) ||
                        Char.IsLower(data[pos]) ||
                        data[pos] == '-'))
                {
                    result += data[pos];
                    ++pos;
                }
            }
            else
            {
                throw new ParseException("invalid attribute type");
            }
            return result;
        }

        private static string
            parseAttributeValue(string data, ref int pos)
        {
            eatWhite(data, ref pos);
            string result = "";
            if (pos >= data.Length)
            {
                return result;
            }

            //
            // RFC 2253
            // # hexstring
            //
            if (data[pos] == '#')
            {
                result += data[pos];
                ++pos;
                while (true)
                {
                    string h = parseHexPair(data, ref pos, true);
                    if (h.Length == 0)
                    {
                        break;
                    }
                    result += h;
                }
            }
                //
                // RFC 2253
                // QUOTATION *( quotechar | pair ) QUOTATION ; only from v2
                // quotechar     = <any character except "\" or QUOTATION >
                //
            else if (data[pos] == '"')
            {
                result += data[pos];
                ++pos;
                while (true)
                {
                    if (pos >= data.Length)
                    {
                        throw new ParseException("invalid attribute value (unexpected end of data)");
                    }
                    // final terminating "
                    if (data[pos] == '"')
                    {
                        result += data[pos];
                        ++pos;
                        break;
                    }
                        // any character except '\'
                    else if (data[pos] != '\\')
                    {
                        result += data[pos];
                        ++pos;
                    }
                        // pair '\'
                    else
                    {
                        result += parsePair(data, ref pos);
                    }
                }
            }
                //
                // RFC 2253
                // * (stringchar | pair)
                // stringchar = <any character except one of special, "\" or QUOTATION >
                //
            else
            {
                while (pos < data.Length)
                {
                    if (data[pos] == '\\')
                    {
                        result += parsePair(data, ref pos);
                    }
                    else if (special.IndexOf(data[pos]) == -1 && data[pos] != '"')
                    {
                        result += data[pos];
                        ++pos;
                    }
                    else
                    {
                        break;
                    }
                }
            }
            return result;
        }

        //
        // RFC2253:
        // pair       = "\" ( special | "\" | QUOTATION | hexpair )
        //
        private static string
            parsePair(string data, ref int pos)
        {
            string result = "";

            Debug.Assert(data[pos] == '\\');
            result += data[pos];
            ++pos;

            if (pos >= data.Length)
            {
                throw new ParseException("invalid escape format (unexpected end of data)");
            }

            if (special.IndexOf(data[pos]) != -1 || data[pos] != '\\' ||
                data[pos] != '"')
            {
                result += data[pos];
                ++pos;
                return result;
            }
            return parseHexPair(data, ref pos, false);
        }

        //
        // RFC 2253
        // hexpair    = hexchar hexchar
        //
        private static string
            parseHexPair(string data, ref int pos, bool allowEmpty)
        {
            string result = "";
            if (pos < data.Length && hexvalid.IndexOf(data[pos]) != -1)
            {
                result += data[pos];
                ++pos;
            }
            if (pos < data.Length && hexvalid.IndexOf(data[pos]) != -1)
            {
                result += data[pos];
                ++pos;
            }
            if (result.Length != 2)
            {
                if (allowEmpty && result.Length == 0)
                {
                    return result;
                }
                throw new ParseException("invalid hex format");
            }
            return result;
        }

        //
        // RFC 2253:
        //
        // Implementations MUST allow for space (' ' ASCII 32) characters to be
        // present between name-component and ',', between attributeTypeAndValue
        // and '+', between attributeType and '=', and between '=' and
        // attributeValue.  These space characters are ignored when parsing.
        //
        private static void
            eatWhite(string data, ref int pos)
        {
            while (pos < data.Length && data[pos] == ' ')
            {
                ++pos;
            }
        }

        internal class ParseException : Exception
        {
            internal string reason;

            internal ParseException()
            {
            }

            internal ParseException(string reason)
            {
                this.reason = reason;
            }

            internal string
                name()
            {
                return "RFC2253::ParseException";
            }
        }

        internal struct RDNPair
        {
            internal string key;
            internal string value;
        } ;
    }
}