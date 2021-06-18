using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace i18n_gen
{
    public static class CommonResource
    {
        private static readonly Regex FilenameFilter = new("\\.strings(?:\\.1)?$", RegexOptions.Compiled);
        private static readonly Regex StringsRegex = new("^\"?(.*?)\"?\\s*=\\s*\"(.*)\";?$", RegexOptions.Compiled);

        public static IEnumerable<(string Lang, string Table, string Key, string Property, string Value)> ParseLines(string sourcePath) =>
            Directory.EnumerateDirectories(sourcePath, "*.lproj")
            .Select(p => (Lang: Path.GetFileNameWithoutExtension(p), Project: p))
            .SelectMany(x => Directory.EnumerateFiles(x.Project).Where((Func<string, bool>)FilenameFilter.IsMatch).Select(s => (
                x.Lang,
                Table: Path.GetFileName(s),
                Path: s
            )))
            .Select(x => (
                x.Lang,
                Table: Path.GetExtension(x.Table) == ".1" ? x.Table.Substring(0, x.Table.Length - 2) : x.Table,
                Path: Path.GetExtension(x.Path) == ".1" ? x.Path.Substring(0, x.Path.Length - 2) : x.Path
            )).GroupBy(x => x).Select(x => (
                x.Key.Lang,
                Table: Path.GetFileNameWithoutExtension(x.Key.Table),
                Path: x.Count() > 1 ? x.Key.Path + ".1" : x.Key.Path
            ))
            .SelectMany(x => ReadLines(x.Path, StringsRegex).Select(((string Key, string Property, string Value) l) => (
                x.Lang,
                x.Table,
                l.Key,
                l.Property,
                l.Value
            )));

        private static string FilterParameter(StringReader reader)
        {
            var result = 0;
            while (reader.Peek() != -1)
            {
                var c = (char)reader.Read();
                if (char.IsDigit(c))
                {
                    result *= 10;
                    result += c - '0';
                }
                else if (c == '}')
                {
                    break;
                }
            }
            return $"P{result + 1}";
        }

        private static string FilterString(string input)
        {
            var builder = new StringBuilder();
            var reader = new StringReader(input);
            while (reader.Peek() != -1)
            {
                var c = (char)reader.Read();
                if (char.IsLetterOrDigit(c))
                {
                    builder.Append(c);
                }
                else if (c == '{')
                {
                    builder.Append(FilterParameter(reader));
                }
            }

            return builder.ToString();
        }

        private static IEnumerable<(string, string, string)> ReadLines(string path, Regex regex)
        {
            HashSet<string> skippedProperties = new();
            using var reader = new StreamReader(path);
            while (!reader.EndOfStream)
            {
                Match match = regex.Match(reader.ReadLine());
                if (match.Success)
                {
                    var title = match.Groups[1].Value;
                    if (string.IsNullOrWhiteSpace(title))
                    {
                        continue;
                    }
                    if (char.IsDigit(title[0]))
                    {
                        continue;
                    }
                    var fancyTitle = FilterString(CultureInfo.InvariantCulture.TextInfo.ToTitleCase(title));

                    if (!skippedProperties.Contains(fancyTitle))
                    {
                        skippedProperties.Add(fancyTitle);
                        var value = match.Groups[2].Value;

                        if (string.IsNullOrWhiteSpace(value))
                        {
                            continue;
                        }

                        yield return (title, fancyTitle, value);
                    }
                }
            }
        }
    }
}
