using System;
using System.Collections.Specialized;
using System.Configuration;
using System.IO;
using System.Net;
using System.Xml;
using Windows.Storage;

namespace Ch.Cyberduck.Core.Preferences;

public class LocalSharedFileSettingsProvider : SettingsProvider
{
    private const string APPLICATIONSETTINGS_ELEMENT_NAME = "applicationSettings";
    private const string CONFIGURATION_ELEMENT_NAME = "configuration";
    private const string NAME_ATTRIBUTE_NAME = "name";
    private const string SERIALIZEAS_ATTRIBUTE_NAME = "serializeAs";
    private const string SETTING_ELEMENT_NAME = "setting";
    private const string SETTING_SELECTOR = "setting[@name='{0}']";
    private const string SETTINGSCLASSTYPE_CONTEXT_KEY = "SettingsClassType";
    private const string USERSETTINGS_ELEMENT_NAME = "userSettings";
    private const string VALUE_ELEMENT_NAME = "value";
    private readonly FileInfo userConfig;

    public override string ApplicationName { get; set; }

    public LocalSharedFileSettingsProvider()
    {
        // store in Packaged cache folder (to ensure clearing after uninstall)
        // store in roaming app data, if not packaged
        var configDirectory = EnvironmentInfo.Packaged
            ? ApplicationData.Current.LocalCacheFolder.Path
            : Path.Combine(EnvironmentInfo.AppDataPath, EnvironmentInfo.DataFolderName);
        
        userConfig = new(Path.Combine(configDirectory, $"{EnvironmentInfo.ProductName}.user.config"));
    }

    public override SettingsPropertyValueCollection GetPropertyValues(SettingsContext context, SettingsPropertyCollection collection)
    {
        SettingsPropertyValueCollection values = new();
        XmlDocument dom = Open();

        XmlNode userSettings = default;
        XmlNode appSettings = default;

        foreach (SettingsProperty property in collection)
        {
            XmlNode node;
            if (IsUserSetting(property))
            {
                node = TryGetUserSettingsClassNode(dom, context, ref userSettings);
            }
            else
            {
                node = TryGetApplicationSettingsClassNode(dom, context, ref appSettings);
            }

            SettingsPropertyValue value = new(property);
            XmlNode setting = node?.SelectSingleNode(string.Format(SETTING_SELECTOR, property.Name));
            if (setting is not null)
            {
                if (!Enum.TryParse<SettingsSerializeAs>(setting.Attributes[SERIALIZEAS_ATTRIBUTE_NAME].Value, out var serializeAs))
                {
                    serializeAs = SettingsSerializeAs.String;
                }

                var valueString = setting[VALUE_ELEMENT_NAME]?.InnerXml;
                if (serializeAs == SettingsSerializeAs.String)
                {
                    valueString = WebUtility.HtmlDecode(valueString);
                }

                value.SerializedValue = valueString;
            }
            else if (property.DefaultValue != null)
            {
                value.SerializedValue = property.DefaultValue;
            }
            else
            {
                value.PropertyValue = null;
            }

            value.IsDirty = false;
            values.Add(value);
        }

        return values;
    }

    public override void Initialize(string name, NameValueCollection config)
    {
        if (string.IsNullOrWhiteSpace(name))
        {
            name = nameof(LocalSharedFileSettingsProvider);
        }

        base.Initialize(name, config);
    }

    public override void SetPropertyValues(SettingsContext context, SettingsPropertyValueCollection collection)
    {
        XmlDocument dom = Open();

        var classConfig = GetOrCreateUserSettingsClassNode(dom, context);
        // reset configuration/userSettings/ClassName,
        // doesn't touch configuration/userSettings/OtherClassName merged in Dom
        classConfig.RemoveAll();
        foreach (SettingsPropertyValue propertyValue in collection)
        {
            if (!IsUserSetting(propertyValue.Property))
            {
                continue;
            }

            if (!propertyValue.IsDirty && propertyValue.SerializedValue == propertyValue.Property.DefaultValue)
            {
                continue;
            }

            var valueNode = GetValueNode(classConfig, propertyValue.Property);
            SerializeToXmlElement(valueNode, propertyValue.Property, propertyValue);
        }

        userConfig.Directory.Create();
        using var stream = userConfig.Open(FileMode.Create, FileAccess.Write, FileShare.Read);
        using var writer = XmlWriter.Create(stream);
        dom.WriteTo(writer);
    }

    private XmlNode GetOrCreateUserSettingsClassNode(XmlDocument dom, SettingsContext context)
    {
        if (dom[CONFIGURATION_ELEMENT_NAME] is not XmlNode configurationRoot)
        {
            configurationRoot = dom.CreateElement(CONFIGURATION_ELEMENT_NAME);
            dom.AppendChild(configurationRoot);
        }

        if (configurationRoot[USERSETTINGS_ELEMENT_NAME] is not XmlNode userSettings)
        {
            userSettings = dom.CreateElement(USERSETTINGS_ELEMENT_NAME);
            configurationRoot.AppendChild(userSettings);
        }

        var nodeName = context[SETTINGSCLASSTYPE_CONTEXT_KEY].ToString();
        if (userSettings[nodeName] is not XmlNode classSetting)
        {
            classSetting = dom.CreateElement(nodeName);
            userSettings.AppendChild(classSetting);
        }

        return classSetting;
    }

    private XmlDocument Open()
    {
        XmlDocument dom = new();
        if (userConfig.Exists)
        {
            try
            {
                using var stream = userConfig.OpenRead();
                dom.Load(stream);
            }
            catch { }
        }

        return dom;
    }

    private void SerializeToXmlElement(XmlNode valueXml, SettingsProperty setting, SettingsPropertyValue value)
    {
        string serializedValue = value.SerializedValue as string;

        if (serializedValue == null && setting.SerializeAs == SettingsSerializeAs.Binary)
        {
            // SettingsPropertyValue returns a byte[] in the binary serialization case. We need to
            // encode this - we use base64 since SettingsPropertyValue understands it and we won't have
            // to special case while deserializing.
            byte[] buf = value.SerializedValue as byte[];
            if (buf != null)
            {
                serializedValue = Convert.ToBase64String(buf);
            }
        }

        serializedValue ??= string.Empty;

        // We need to escape string serialized values
        if (setting.SerializeAs == SettingsSerializeAs.String)
        {
            serializedValue = WebUtility.HtmlEncode(serializedValue);
        }

        valueXml.InnerXml = serializedValue;

        // Hack to remove the XmlDeclaration that the XmlSerializer adds. 
        XmlNode unwanted = null;
        foreach (XmlNode child in valueXml.ChildNodes)
        {
            if (child.NodeType == XmlNodeType.XmlDeclaration)
            {
                unwanted = child;
                break;
            }
        }
        if (unwanted != null)
        {
            valueXml.RemoveChild(unwanted);
        }
    }

    private static XmlNode GetValueNode(XmlNode classConfig, SettingsProperty property)
    {
        var dom = classConfig.OwnerDocument;
        if (classConfig.SelectSingleNode(string.Format(SETTING_SELECTOR, property.Name)) is not XmlNode settingNode)
        {
            settingNode = dom.CreateElement(SETTING_ELEMENT_NAME);
            var nameAttribute = dom.CreateAttribute(NAME_ATTRIBUTE_NAME);
            nameAttribute.Value = property.Name;
            settingNode.Attributes.Append(nameAttribute);
            var serializeAsAttribute = dom.CreateAttribute(SERIALIZEAS_ATTRIBUTE_NAME);
            serializeAsAttribute.Value = property.SerializeAs.ToString();
            settingNode.Attributes.Append(serializeAsAttribute);
            classConfig.AppendChild(settingNode);
        }

        if (settingNode[VALUE_ELEMENT_NAME] is not XmlNode valueNode)
        {
            valueNode = dom.CreateElement(VALUE_ELEMENT_NAME);
            settingNode.AppendChild(valueNode);
        }

        return valueNode;
    }

    private static bool IsUserSetting(SettingsProperty property)
    {
        bool isUser = property.Attributes[typeof(UserScopedSettingAttribute)] is UserScopedSettingAttribute;
        bool isApp = property.Attributes[typeof(ApplicationScopedSettingAttribute)] is ApplicationScopedSettingAttribute;

        if (isUser && isApp)
        {
            throw new ConfigurationErrorsException("Property can't be User and Application scoped");
        }

        if (!(isUser || isApp))
        {
            throw new ConfigurationErrorsException("Property requires one of UserScopedSettingAttribute or ApplicationScopedSettingAttribute");
        }

        return isUser;
    }

    private static XmlNode TryGetApplicationSettingsClassNode(XmlDocument dom, SettingsContext context, ref XmlNode cache)
    {
        return TryGetSectionClassNode(dom, APPLICATIONSETTINGS_ELEMENT_NAME, context, ref cache);
    }

    private static XmlNode TryGetUserSettingsClassNode(XmlDocument dom, SettingsContext context, ref XmlNode cache)
    {
        return TryGetSectionClassNode(dom, USERSETTINGS_ELEMENT_NAME, context, ref cache);
    }

    private static XmlNode TryGetSectionClassNode(XmlDocument dom, string settings, SettingsContext context, ref XmlNode cache)
    {
        if (cache is not null)
        {
            return cache;
        }

        return cache = dom[CONFIGURATION_ELEMENT_NAME]?[settings]?[context[SETTINGSCLASSTYPE_CONTEXT_KEY].ToString()];
    }
}
