using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Resources;
using System.Text;
using System.Text.RegularExpressions;

namespace i18n_gen
{
    public class ResourceGenTask : Task
    {
        private static readonly Regex StringsRegex = new("^\"?(.*?)\"?[ ]*=[ ]*\"(.*)\";?$", RegexOptions.Compiled);

        [Required]
        public string OutputPath { get; set; }

        [Output]
        public ITaskItem[] Resources { get; set; }

        [Required]
        public string SourcePath { get; set; }

        public override bool Execute()
        {
            var languageProjects = Directory.EnumerateDirectories(SourcePath, "*.lproj");
            var languages = languageProjects.Select(p => new { Lang = Path.GetFileNameWithoutExtension(p), Project = p });
            var strings = languages.SelectMany(x => Directory.EnumerateFiles(x.Project, "*.strings").Select(s => new
            {
                x.Lang,
                Table = Path.GetFileName(s),
                Path = s
            }));
            var tables = strings.Select(x => new
            {
                x.Lang,
                Table = Path.GetExtension(x.Table) == ".1" ? x.Table.Substring(0, x.Table.Length - 2) : x.Table,
                Path = Path.GetExtension(x.Path) == ".1" ? x.Table.Substring(0, x.Path.Length - 2) : x.Path
            }).GroupBy(x => x).Select(x => new
            {
                x.Key.Lang,
                Table = Path.GetFileNameWithoutExtension(x.Key.Table),
                Path = x.Count() > 1 ? x.Key.Path + ".1" : x.Key.Path
            });
            var lines = tables.SelectMany(x => ReadLines(x.Path, StringsRegex).Select(((string Key, string Property, string Value) l) => new
            {
                x.Lang,
                x.Table,
                l.Key,
                l.Property,
                l.Value
            }));
            var resources = new List<ITaskItem>();
            foreach (var lang in lines.GroupBy(x => x.Lang))
            {
                var targetfile = Path.Combine(OutputPath, "i18n." + lang.Key + ".resx");
                using var resxwriter = new ResXResourceWriter(targetfile);
                foreach (var property in lang)
                {
                    resxwriter.AddResource(property.Table + ":" + property.Property, property.Value);
                }
                resources.Add(new TaskItem(targetfile, new Hashtable()
                {
                    ["WithCulture"] = "false"
                }));
            }
            Resources = resources.ToArray();

            return true;
        }

        private string FilterParameter(StringReader reader)
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

        private string FilterString(string input)
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

        private IEnumerable<(string, string, string)> ReadLines(string path, Regex regex)
        {
            HashSet<string> skippedProperties = new HashSet<string>();
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
                        var value = match.Groups[2].Value.Replace("<br/>", Environment.NewLine);

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
