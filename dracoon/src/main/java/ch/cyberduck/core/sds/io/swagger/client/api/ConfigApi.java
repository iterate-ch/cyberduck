package ch.cyberduck.core.sds.io.swagger.client.api;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.ConfigOptionList;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneralSettings;
import ch.cyberduck.core.sds.io.swagger.client.model.InfrastructureProperties;
import ch.cyberduck.core.sds.io.swagger.client.model.SystemDefaults;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
public class ConfigApi {
  private ApiClient apiClient;

  public ConfigApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ConfigApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get authentication settings
   * ### Functional Description:   Retrieve the settings of authentication configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: This API is deprecated and will be removed in the future.   cf. &#x60;GET /system/config/settings/auth&#x60; and &#x60;GET /public/system/info&#x60;  ### Configuration settings for various authentication methods  ### Authentication Methods  | Authentication Method | Description | Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their credentials stored in the database. | &#x60;true or false&#x60; | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their Active Directory credentials. | &#x60;true or false&#x60; | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their RADIUS username, their PIN and a token password. | &#x60;true or false&#x60; | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their OpenID Connect identity. | &#x60;true or false&#x60; | | **&#x60;default_auth_method&#x60;** (**&#x60;DEPRECATED&#x60;**) | **Default authentication method** is determined by its priority.&lt;br&gt;Smaller values have higher priority.&lt;br&gt;cf. &#x60;GET /config/info/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | **&#x60;sql&#x60;**, **&#x60;active_directory&#x60;**, **&#x60;radius&#x60;** or **&#x60;openid&#x60;** |
   * @param xSdsAuthToken Authentication token (optional)
   * @return ConfigOptionList
   * @throws ApiException if fails to make API call
   */
  public ConfigOptionList getAuthSettings(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/authSettings";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ConfigOptionList> localVarReturnType = new GenericType<ConfigOptionList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get information about general settings
   * ### Functional Description:   DRACOON general settings configuration entry point.   Returns a list of configurable general settings.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether _TripleCrypt™ technology_ is enabled.&lt;br&gt;Can only be enabled once; disabling is not possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is not possible. | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
   * @param xSdsAuthToken Authentication token (optional)
   * @return GeneralSettings
   * @throws ApiException if fails to make API call
   */
  public GeneralSettings getGeneralSettingsInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/info/general";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<GeneralSettings> localVarReturnType = new GenericType<GeneralSettings>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get information about infrastructure properties
   * ### Functional Description:   DRACOON infrastructure properties entry point.   Returns a list of read-only infrastructure properties.    ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; | 
   * @param xSdsAuthToken Authentication token (optional)
   * @return InfrastructureProperties
   * @throws ApiException if fails to make API call
   */
  public InfrastructureProperties getInfrastructurePropertiesInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/info/infrastructure";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<InfrastructureProperties> localVarReturnType = new GenericType<InfrastructureProperties>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get information about default values
   * ### Functional Description:   DRACOON system defaults configuration entry point.   Returns a list of configurable system default values.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60; | 
   * @param xSdsAuthToken Authentication token (optional)
   * @return SystemDefaults
   * @throws ApiException if fails to make API call
   */
  public SystemDefaults getSystemDefaultsInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/info/defaults";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<SystemDefaults> localVarReturnType = new GenericType<SystemDefaults>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get system settings
   * ### Functional Description:   DRACOON configuration entry point.   Returns a list of configurable system settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: This API is deprecated and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention If &#x60;eula_active&#x60; is true, but not accepted yet, or password must be changed, only the following two values are returned: * **&#x60;allow_system_global_weak_password&#x60;** * **&#x60;eula_active&#x60;**  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;branding_server_branding_id&#x60;** | The branding UUID, which corresponds to _BRANDING-QUALIFIER_ in the new branding server.&lt;br&gt;cf. &#x60;GET /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingQualifier&#x60;** | &#x60;String&#x60; | | **&#x60;branding_portal_url&#x60;** | Access URL to to the Branding Portal&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingProviderUrl&#x60;** | &#x60;String&#x60; | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of **TripleCrypt™ Technology**&lt;br&gt;Can only be enabled once; disabling is not possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged on login.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver&#x60;** | Email server to send emails.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.host&#x60;** | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;mailserver_authentication_necessary&#x60;** | Set to &#x60;true&#x60; if the email server requires authentication.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.authenticationEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_password&#x60;** | **Password is no longer returned.**&lt;br&gt;Check **&#x60;mailserver_password_set&#x60;** to determine whether password is set. |  | | **&#x60;mailserver_password_set&#x60;** | Indicates if a password is set for the mailserver (because **&#x60;mailserver_password&#x60;** is always returned empty).&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.passwordDefined&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_port&#x60;** | Email server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;mailserver_username&#x60;** | User name for email server&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;User name for authentication&#x60; | | **&#x60;mailserver_use_ssl&#x60;** | Email server requires SSL connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_starttls&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_use_starttls&#x60;** | Email server requires StartTLS connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_ssl&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.starttlsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;globally_allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS **system-wide** (read-only).&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.smsConfigEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;use_s3_storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is not possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.useS3Storage&#x60;** | &#x60;true or false&#x60; | | **&#x60;s3_default_region&#x60;** |Suggested S3 region (read-only)&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.s3DefaultRegion&#x60;** | &#x60;Region name&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;branding_server_customer&#x60;** (**&#x60;DEPRECATED&#x60;**) | The UUID of the branding server customer, which corresponds to customer key in the branding server. | &#x60;String&#x60; | | **&#x60;branding_server_url&#x60;** (**&#x60;DEPRECATED&#x60;**) | Access URL to to the Branding Server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer. | &#x60;String&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return ConfigOptionList
   * @throws ApiException if fails to make API call
   */
  public ConfigOptionList getSystemSettings(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/settings";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ConfigOptionList> localVarReturnType = new GenericType<ConfigOptionList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Change authentication setting
   * ### Functional Description: Change one or more settings of authentication configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: One or more global authentication setting gets changed.  ### &amp;#9432; Further Information: This API is deprecated and will be removed in the future.   cf. &#x60;PUT /system/config/settings/auth&#x60;  ### Configuration settings for various authentication methods  ### Authentication Methods  | Authentication Method | Description | Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their credentials stored in the database. | &#x60;true or false&#x60; | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their Active Directory credentials. | &#x60;true or false&#x60; | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their RADIUS username, their PIN and a token password. | &#x60;true or false&#x60; | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.&lt;br&gt;This option must be activated to allow users to log in with their OpenID Connect identity. | &#x60;true or false&#x60; | | **&#x60;default_auth_method&#x60;** (**&#x60;DEPRECATED&#x60;**) | **Default authentication method** is determined by its priority.&lt;brSmaller values have higher priority. | **&#x60;sql&#x60;**, **&#x60;active_directory&#x60;**, **&#x60;radius&#x60;** or **&#x60;openid&#x60;** |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setAuthSettings(ConfigOptionList body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setAuthSettings");
    }
    
    // create path and map variables
    String localVarPath = "/v4/config/authSettings";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Change system settings
   * ### Functional Description: DRACOON configuration entry point.   Change configurable settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: One or more global settings gets changed.  ### &amp;#9432; Further Information: This API is deprecated and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention Only visible for _Config Manager_ of Provider Customer.  ### Settings  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;branding_server_branding_id&#x60;** | The branding UUID, which corresponds to _BRANDING-QUALIFIER_ in the new branding server.&lt;br&gt;cf. &#x60;PUT /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingQualifier&#x60;** | &#x60;String&#x60; | | **&#x60;branding_portal_url&#x60;** | Access URL to to the Branding Portal&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingProviderUrl&#x60;** | &#x60;String&#x60; | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of **TripleCrypt™ Technology**&lt;br&gt;Can only be enabled once; disabling is not possible.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged on login.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver&#x60;** | Email server to send emails.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.host&#x60;** | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;mailserver_authentication_necessary&#x60;** | Set to &#x60;true&#x60; if the email server requires authentication.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.authenticationEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_password&#x60;** | Password for email server&lt;br&gt;cf. &#x60;PUT /system/config/settings/password&#x60; **&#x60;MailServerConfig.password&#x60;** | &#x60;Password for authentication&#x60; | | **&#x60;mailserver_port&#x60;** | Email server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;mailserver_username&#x60;** | User name for email server&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;User name for authentication&#x60; | | **&#x60;mailserver_use_ssl&#x60;** | Email server requires SSL connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_starttls&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_use_starttls&#x60;** | Email server requires StartTLS connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_ssl&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.starttlsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;branding_server_customer&#x60;** (**&#x60;DEPRECATED&#x60;**) | The UUID of the branding server customer, which corresponds to customer key in the branding server. | &#x60;String&#x60; | | **&#x60;branding_server_url&#x60;** (**&#x60;DEPRECATED&#x60;**) | Access URL to to the Branding Server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer. | &#x60;String&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setSystemSetting(ConfigOptionList body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setSystemSetting");
    }
    
    // create path and map variables
    String localVarPath = "/v4/config/settings";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
