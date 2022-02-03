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
                ".ico" => (x.key, x.path, Path.ChangeExtension(x.key, ".ico"), 5),
                ".tiff" => (x.key, x.path, Path.ChangeExtension(x.key, ".tiff"), 4),
                ".png" => (x.key, x.path, Path.ChangeExtension(x.key, ".png"), x.isHDPI ? 2 : 3),
                ".gif" => (x.key, x.path, Path.ChangeExtension(x.key, ".gif"), 1),
                _ => (x.key, x.path, Path.GetFileName(x.path), 0)
            });
            var lookup = prioritize.ToLookup(x => x.key);
            var resources = lookup.Select(x => x.OrderByDescending(x => x.Item4).Aggregate((x, y) => new FileInfo(x.path).Length > new FileInfo(y.path).Length ? y : x));
            Resources = resources.Select(r => new TaskItem(r.path, new Hashtable
            {
                ["LogicalName"] = r.Item3
            })).ToArray();
            return true;
        }
    }
}
