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
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using ExceptionReporting.Core;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core
{
    internal class CrashReporter
    {
        private static readonly CrashReporter instance = new CrashReporter();

        private CrashReporter()
        {
        }

        public static CrashReporter Instance
        {
            get { return instance; }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="e"></param>
        /// <returns>Crash report as string</returns>
        public void Write(Exception e)
        {
            ExceptionReportInfo info = new ExceptionReportInfo {MainException = e};
            ExceptionReportGenerator reportGenerator = new ExceptionReportGenerator(info);
            ExceptionReport report = reportGenerator.CreateExceptionReport();

            string crashDir = Path.Combine(Preferences.instance().getProperty("application.support.path"),
                                           "CrashReporter");
            Directory.CreateDirectory(crashDir);
            using (StreamWriter outfile = new StreamWriter(Path.Combine(crashDir, DateTime.Now.Ticks + ".txt")))
            {
                outfile.Write(report.ToString());
            }
            TaskDialog prompt = new TaskDialog();
            DialogResult result = prompt.ShowCommandBox(Locale.localizedString("Do you want to report the last crash?", "Crash"),
                                                        Locale.localizedString("Do you want to report the last crash?", "Crash"),
                                                        Locale.localizedString(
                                                            "The application %@ has recently crashed. To help improve it, you can send the crash log to the author.", "Crash").Replace("%@", Preferences.instance().getProperty("application.name")),
                                                        String.Format("{0}|{1}",
                                                                      Locale.localizedString("Send", "Crash"),
                                                                      Locale.localizedString("Don't Send", "Crash")),
                                                        false, SysIcons.Error);
            if (DialogResult.OK == result)
            {
                if (0 == prompt.CommandButtonResult)
                {
                    Post(report.ToString());
                }
            }
        }

        public void Post(string report)
        {
            Dictionary<string, object> postParameters = new Dictionary<string, object> {{"crashlog", report}};
            string revision = Preferences.instance().getProperty("application.revision");

            //this might take some time as the WebRequest tries to detect the proxy settings first
            this.MultipartFormDataPost(
                Preferences.instance().getProperty("website.crash") + String.Format("?revision={0}&os={1}",
                                                                                    revision, Environment.OSVersion),
                String.Format("{0} ({1})", Preferences.instance().getProperty("application.name"), revision),
                postParameters);
        }

        /// <summary>
        /// Post the data as a multipart form
        /// postParameters with a value of type byte[] will be passed in the form as a file, and value of type string will be
        /// passed as a name/value pair.
        /// </summary>
        private HttpWebResponse MultipartFormDataPost(string postUrl, string userAgent,
                                                      Dictionary<string, object> postParameters)
        {
            string formDataBoundary = "-----------------------------0xKhTmLbOuNdArY";
            string contentType = "multipart/form-data; boundary=" + formDataBoundary;

            byte[] formData = GetMultipartFormData(postParameters, formDataBoundary);

            return PostForm(postUrl, userAgent, contentType, formData);
        }

        /// <summary>
        /// Post a form
        /// </summary>
        private HttpWebResponse PostForm(string postUrl, string userAgent, string contentType, byte[] formData)
        {
            HttpWebRequest request = WebRequest.Create(postUrl) as HttpWebRequest;
            if (null == request)
            {
                throw new NullReferenceException("request is not a http request");
            }
            // Add these, as we're doing a POST
            request.Method = "POST";
            request.ContentType = contentType;
            request.UserAgent = userAgent;
            request.CookieContainer = new CookieContainer();

            // We need to count how many bytes we're sending. 
            request.ContentLength = formData.Length;
            using (Stream requestStream = request.GetRequestStream())
            {
                // Push it out there
                requestStream.Write(formData, 0, formData.Length);
                requestStream.Close();
            }
            return request.GetResponse() as HttpWebResponse;
        }

        /// <summary>
        /// Turn the key and value pairs into a multipart form.
        /// See http://www.ietf.org/rfc/rfc2388.txt for issues about file uploads
        /// </summary>
        private byte[] GetMultipartFormData(Dictionary<string, object> postParameters, string boundary)
        {
            Stream formDataStream = new MemoryStream();

            foreach (var param in postParameters)
            {
                if (param.Value is byte[])
                {
                    byte[] fileData = param.Value as byte[];

                    // Add just the first part of this param, since we will write the file data directly to the Stream
                    string header =
                        string.Format(
                            "--{0}\r\nContent-Disposition: form-data; name=\"{1}\"; filename=\"{2}\";\r\nContent-Type: application/octet-stream\r\n\r\n",
                            boundary, param.Key, param.Key);
                    formDataStream.Write(Encoding.UTF8.GetBytes(header), 0, header.Length);

                    // Write the file data directly to the Stream, rather than serializing it to a string.  This 
                    formDataStream.Write(fileData, 0, fileData.Length);
                }
                else
                {
                    string postData =
                        string.Format("--{0}\r\nContent-Disposition: form-data; name=\"{1}\"\r\n\r\n{2}\r\n", boundary,
                                      param.Key, param.Value);
                    formDataStream.Write(Encoding.UTF8.GetBytes(postData), 0, postData.Length);
                }
            }

            // Add the end of the request
            string footer = "\r\n--" + boundary + "--\r\n";
            formDataStream.Write(Encoding.UTF8.GetBytes(footer), 0, footer.Length);

            // Dump the Stream into a byte[]
            formDataStream.Position = 0;
            byte[] formData = new byte[formDataStream.Length];
            formDataStream.Read(formData, 0, formData.Length);
            formDataStream.Close();

            return formData;
        }
    }
}