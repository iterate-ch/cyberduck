// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;
using ch.cyberduck.core;
using ch.cyberduck.core.preferences;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <summary>
    /// Uses the application settings mechanism to persist UI settings like form postition and state
    /// </summary>
    /// <see cref="http://stackoverflow.com/questions/495380/c-how-to-make-a-form-remember-its-bounds-and-windowstate-taking-dual-monitor-s"/>
    public sealed class PersistentFormHandler
    {
        /// <summary>
        /// Instantiates new persistent form handler.
        /// </summary>
        /// <param name="windowType">The <see cref="Type.FullName"/> will be used as <see cref="Name"/>.</param>
        /// <param name="defaultWindowState">Default state of the window.</param>
        /// <param name="defaultWindowBounds">Default bounds of the window.</param>
        public PersistentFormHandler(Type windowType, int defaultWindowState, Rectangle defaultWindowBounds)
            : this(windowType, null, defaultWindowState, defaultWindowBounds)
        {
        }

        /// <summary>
        /// Instantiates new persistent form handler.
        /// </summary>
        /// <param name="windowType">The <see cref="Type.FullName"/> will be used as base <see cref="Name"/>.</param>
        /// <param name="id">Use this if you need to separate windows of same type. Will be appended to <see cref="Name"/>.</param>
        /// <param name="defaultWindowState">Default state of the window.</param>
        /// <param name="defaultWindowBounds">Default bounds of the window.</param>
        public PersistentFormHandler(Type windowType, string id, int defaultWindowState, Rectangle defaultWindowBounds)
        {
            Name = string.IsNullOrEmpty(id)
                       ? windowType.Name + "."
                       : windowType.Name + ":" + id + ".";

            DefaultWindowState = defaultWindowState;
            DefaultWindowBounds = defaultWindowBounds;
        }

        private Rectangle DefaultWindowBounds { get; set; }

        private int DefaultWindowState { get; set; }

        /// <summary>The form identifier in storage.</summary>
        public string Name { get; private set; }

        /// <summary>Gets and sets the window state. (int instead of enum so that it can be in a BI layer, and not require a reference to WinForms)</summary>
        public int WindowState
        {
            get { return Get("WindowState", DefaultWindowState); }
            set { Set("WindowState", value); }
        }

        /// <summary>Gets and sets the window bounds. (X, Y, Width and Height)</summary>
        public Rectangle WindowBounds
        {
            get { return Get("WindowBounds", DefaultWindowBounds); }
            set { Set("WindowBounds", value); }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="pObject"></param>
        /// <returns></returns>
        public String SerializeObject<T>(T pObject)
        {
            try
            {
                XmlSerializer xs = new XmlSerializer(typeof (T));
                XmlWriterSettings writerSettings = new XmlWriterSettings
                                                       {OmitXmlDeclaration = true, Encoding = Encoding.UTF8};
                StringBuilder sb = new StringBuilder();
                using (XmlWriter xmlWriter = XmlWriter.Create(new StringWriter(sb),
                                                              writerSettings))
                {
                    xs.Serialize(xmlWriter, pObject);
                }
                return sb.ToString();
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="xml"></param>
        /// <param name="fallback"></param>
        /// <returns></returns>
        public T DeserializeObject<T>(String xml, T fallback)
        {
            try
            {
                XmlSerializer xs = new XmlSerializer(typeof (T));
                using (XmlReader reader = XmlReader.Create(new StringReader(xml)))
                {
                    T obj = (T) xs.Deserialize(reader);
                    return obj;
                }
            }
            catch (Exception)
            {
                return fallback;
            }
        }

        /// <summary>
        /// Adds the given <paramref key="value"/> to the collection of values that will be
        /// stored in database on <see cref="Save"/>.
        /// </summary>
        /// <typeparam key="T">Type of object.</typeparam>
        /// <param name="key">The key you want to use for this value.</param>
        /// <param name="value">The value to store.</param>
        public void Set<T>(string key, T value)
        {
            string xmlString = SerializeObject(value);
            if (xmlString == null) {
                return;
            }
            string base64String = Convert.ToBase64String(Encoding.UTF8.GetBytes(xmlString));
            PreferencesFactory.get().setProperty("ui." + Name + key, base64String);
        }

        /// <summary>
        /// Same as <see cref="Get{T}(string,T)"/>, but uses default(<typeparamref name="T"/>) as fallback value.
        /// </summary>
        /// <typeparam name="T">Type of object</typeparam>
        /// <param name="key">The key used on <see cref="Set{T}"/>.</param>
        /// <returns>The stored object, or the default(<typeparamref name="T"/>) object if something went wrong.</returns>
        public T Get<T>(string key)
        {
            return Get(key, default(T));
        }

        /// <summary>
        /// Gets the value identified by the given <paramref name="key"/>.
        /// </summary>
        /// <typeparam name="T">Type of object</typeparam>
        /// <param name="key">The key used on <see cref="Set{T}"/>.</param>
        /// <param name="fallback">Value to return if the given <paramref name="key"/> could not be found.
        /// In other words, if you haven't used <see cref="Set{T}"/> yet.</param>
        /// <returns>The stored object, or the <paramref name="fallback"/> object if something went wrong.</returns>
        public T Get<T>(string key, T fallback)
        {
            String val = PreferencesFactory.get().getProperty("ui." + Name + key);
            if (null == val)
            {
                return fallback;
            }
            String xml = Encoding.UTF8.GetString(Convert.FromBase64String(val));
            return DeserializeObject(xml, fallback);
        }
    }
}