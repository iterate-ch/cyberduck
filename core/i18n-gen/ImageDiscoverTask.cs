using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using System;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace i18n_gen
{
    public class ImageDiscoverTask : Task
    {
        public static Regex ImageFiles = new(@"(?:\.png|\.tiff|\.gif|\.ico)$", RegexOptions.Compiled);

        [Output]
        public ITaskItem[] Resources { get; set; }

        [Required]
        public string SourcePath { get; set; }

        public override bool Execute()
        {
            var files = Directory.EnumerateFiles(SourcePath);
            var filtered = files.Where((Func<string, bool>)ImageFiles.IsMatch);
            var transform = filtered.Select(path =>
            {
                var filename = Path.GetFileNameWithoutExtension(path);
                int hdpiIndex = filename.IndexOf("@2x");
                bool isHDPI = false;
                if (hdpiIndex != -1)
                {
                    isHDPI = true;
                    filename = filename.Substring(0, hdpiIndex);
                }
                else if (Path.GetExtension(path) == ".tiff")
                {
                    isHDPI = true;
                }
                return (key: filename, isHDPI, path);
            });
            var lookup = transform.ToLookup(k => k.key);
            var resources = lookup.Select(k => k.Count() > 1 ? k.Single(s => !s.isHDPI) : k.Single());
            Resources = resources.Select(r => new TaskItem(r.path)).ToArray();
            return true;
        }
    }
}
