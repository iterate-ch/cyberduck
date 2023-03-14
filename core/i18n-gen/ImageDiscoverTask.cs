using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using System.Collections;
using System.IO;
using System.Linq;

namespace i18n_gen
{
    public class ImageDiscoverTask : Task
    {
        [Output]
        public ITaskItem[] Resources { get; set; }

        [Required]
        public ITaskItem[] ImageResources { get; set; }

        public override bool Execute()
        {
            var files = ImageResources.Select(x => x.ItemSpec);
            var key = files.Select(path =>
            {
                var filename = Path.GetFileNameWithoutExtension(path);
                int hdpiIndex = filename.IndexOf("@2x");
                if (hdpiIndex != -1)
                {
                    filename = filename.Substring(0, hdpiIndex);
                }
                return (key: filename, isHDPI: hdpiIndex != -1, path);
            });
            var prioritize = key.Select(x => Path.GetExtension(x.path) switch
            {
                string ext => ext switch
                {
                    ".ico" => (x.key, ext, x.path, priority: 5),
                    ".tiff" => (x.key, ext, x.path, priority: 4),
                    ".png" => (x.key, ext, x.path, priority: x.isHDPI ? 3 : 2),
                    ".gif" => (x.key, ext, x.path, priority: 1),
                    _ => (x.key, ext, x.path, priority: 0)
                },
            }).ToLookup(x => (x.key, x.ext)).Select(g => (g.Count() > 1 ? g.OrderByDescending(d => d.priority).AsEnumerable() : g).First());
            var lookup = prioritize.ToLookup(x => x.key);
            var resources = lookup.Select(x => x.OrderByDescending(x => x.priority).Aggregate((x, y) => new FileInfo(x.path).Length > new FileInfo(y.path).Length ? y : x));
            Resources = resources.Select(r => new TaskItem(r.path, new Hashtable
            {
                ["LogicalName"] = $"{r.key}{r.ext}"
            })).ToArray();
            return true;
        }
    }
}
