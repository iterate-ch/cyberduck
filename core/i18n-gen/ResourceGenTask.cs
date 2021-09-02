using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Resources;

namespace i18n_gen
{
    public class ResourceGenTask : Task
    {
        [Required]
        public string OutputPath { get; set; }

        [Output]
        public ITaskItem[] Resources { get; set; }

        [Required]
        public string SourcePath { get; set; }

        public override bool Execute()
        {
            HashSet<string> langs = new();
            List<ITaskItem> resources = new();
            foreach (var lang in CommonResource.ParseLines(SourcePath).GroupBy(x => x.Lang))
            {
                langs.Add(lang.Key);
                var filename = "i18n." + lang.Key + ".resources";
                var targetfile = Path.Combine(OutputPath, filename);
                if (File.Exists(targetfile))
                {
                    File.Delete(targetfile);
                }
                using var resxwriter = new ResourceWriter(targetfile);
                foreach (var property in lang)
                {
                    resxwriter.AddResource(property.Table + ":" + property.Key, property.Value);
                }
                resources.Add(new TaskItem(targetfile));
            }

            var i18nFile = "i18n.resources";
            var i18nPath = Path.Combine(OutputPath, i18nFile);
            using (var resxwriter = new ResourceWriter(i18nPath))
            {
                resxwriter.AddResource("Locales", string.Join(" ", langs));
                resources.Add(new TaskItem(i18nPath));
            }

            Resources = resources.ToArray();

            return true;
        }
    }
}
