package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.AuthConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.BrandingConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.EventlogConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneralSettings;
import ch.cyberduck.core.sds.io.swagger.client.model.InfrastructureProperties;
import ch.cyberduck.core.sds.io.swagger.client.model.MailServerConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.SyslogConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.SystemDefaults;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateBrandingConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateEventlogConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateGeneralSettings;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateMailServerConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateSyslogConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateSystemDefaults;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-05-17T14:22:07.810+02:00")
public class SystemSettingsConfigApi {
  private ApiClient apiClient;

  public SystemSettingsConfigApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SystemSettingsConfigApi(ApiClient apiClient) {
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
   * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON authentication configuration entry point.   Returns a list of configurable authentication methods.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: Authentication methods are sorted by priority attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.   Priority **MUST** be a positive value.  ### Configurable authentication settings  | Authentication Method | Description | | :--- | :--- | | **&#x60;basic&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database.&lt;br&gt;Formerly known as **&#x60;sql&#x60;**. | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. | 
   * @param xSdsAuthToken Authentication token (optional)
   * @return AuthConfig
   * @throws ApiException if fails to make API call
   */
  public AuthConfig getAuthConfig(String xSdsAuthToken) throws ApiException {
      return getAuthConfigWithHttpInfo(xSdsAuthToken).getData();
  }

    /**
     * Get authentication settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON authentication configuration entry point.   Returns a list of configurable authentication methods.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: Authentication methods are sorted by priority attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.   Priority **MUST** be a positive value.  ### Configurable authentication settings  | Authentication Method | Description | | :--- | :--- | | **&#x60;basic&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database.&lt;br&gt;Formerly known as **&#x60;sql&#x60;**. | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;AuthConfig&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthConfig> getAuthConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/auth";

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

        GenericType<AuthConfig> localVarReturnType = new GenericType<AuthConfig>() {};
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
  /**
   * Get branding settings
   * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   DRACOON branding configuration entry point.   Returns a list of configurable branding settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable branding settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;brandingProviderUrl&#x60;** | Access URL to to the Branding Portal | &#x60;String&#x60; | | **&#x60;brandingQualifier&#x60;** | Branding UUID | &#x60;String&#x60; | 
   * @param xSdsAuthToken Authentication token (optional)
   * @return BrandingConfig
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public BrandingConfig getBrandingConfig(String xSdsAuthToken) throws ApiException {
      return getBrandingConfigWithHttpInfo(xSdsAuthToken).getData();
  }

    /**
     * Get branding settings
     * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   DRACOON branding configuration entry point.   Returns a list of configurable branding settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable branding settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;brandingProviderUrl&#x60;** | Access URL to to the Branding Portal | &#x60;String&#x60; | | **&#x60;brandingQualifier&#x60;** | Branding UUID | &#x60;String&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;BrandingConfig&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BrandingConfig> getBrandingConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/branding";

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

        GenericType<BrandingConfig> localVarReturnType = new GenericType<BrandingConfig>() {};
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get eventlog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON eventlog configuration entry point.   Returns a list of configurable eventlog settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable eventlog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Determines whether eventlog is enabled. | &#x60;true or false&#x60; | | **&#x60;retentionPeriod&#x60;** | Retention period (in _days_) of eventlog entries.&lt;br&gt;After that period, all entries are deleted. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return EventlogConfig
   * @throws ApiException if fails to make API call
   */
    public EventlogConfig getEventlogConfig(String xSdsAuthToken) throws ApiException {
        return getEventlogConfigWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get eventlog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON eventlog configuration entry point.   Returns a list of configurable eventlog settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable eventlog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Determines whether eventlog is enabled. | &#x60;true or false&#x60; | | **&#x60;retentionPeriod&#x60;** | Retention period (in _days_) of eventlog entries.&lt;br&gt;After that period, all entries are deleted. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;EventlogConfig&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<EventlogConfig> getEventlogConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/eventlog";

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

        GenericType<EventlogConfig> localVarReturnType = new GenericType<EventlogConfig>() {};
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get general settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON general settings configuration entry point.   Returns a list of configurable general settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters
   * @param xSdsAuthToken Authentication token (optional)
   * @return GeneralSettings
   * @throws ApiException if fails to make API call
   */
    public GeneralSettings getGeneralSettings(String xSdsAuthToken) throws ApiException {
        return getGeneralSettingsWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get general settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON general settings configuration entry point.   Returns a list of configurable general settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;GeneralSettings&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<GeneralSettings> getGeneralSettingsWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/general";

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
     * Get infrastructure properties
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON infrastructure properties entry point.   Returns a list of read-only infrastructure properties.    ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return InfrastructureProperties
   * @throws ApiException if fails to make API call
   */
    public InfrastructureProperties getInfrastructureProperties(String xSdsAuthToken) throws ApiException {
        return getInfrastructurePropertiesWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get infrastructure properties
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON infrastructure properties entry point.   Returns a list of read-only infrastructure properties.    ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;InfrastructureProperties&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<InfrastructureProperties> getInfrastructurePropertiesWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/infrastructure";

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
     * Get mail server settings
     * ## &amp;#9888; Deprecated since version 4.11.0  ### Functional Description:   DRACOON email server configuration entry point.   Returns a list of configurable email server settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable email server settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;host&#x60;** | Email server host | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;port&#x60;** | Email server port | &#x60;Valid port number&#x60; | | **&#x60;username&#x60;** | User name for email server | &#x60;User name for authentication&#x60; | | **&#x60;passwordDefined&#x60;** | Determines whether _password_ is defined for email server. | &#x60;true or false&#x60; | | **&#x60;authenticationEnabled&#x60;** | Determines whether email server requires _authentication_. | &#x60;true or false&#x60; | | **&#x60;sslEnabled&#x60;** | Determines whether email server requires _SSL_ connection.&lt;br&gt;Requires **&#x60;starttlsEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false &#x60; | | **&#x60;starttlsEnabled&#x60;** | Determines whether email server requires _StartTLS_ connection.&lt;br&gt;Requires **&#x60;sslEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
     * @return MailServerConfig
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public MailServerConfig getMailServerConfig(String xSdsAuthToken) throws ApiException {
      return getMailServerConfigWithHttpInfo(xSdsAuthToken).getData();
  }

    /**
     * Get mail server settings
     * ## &amp;#9888; Deprecated since version 4.11.0  ### Functional Description:   DRACOON email server configuration entry point.   Returns a list of configurable email server settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable email server settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;host&#x60;** | Email server host | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;port&#x60;** | Email server port | &#x60;Valid port number&#x60; | | **&#x60;username&#x60;** | User name for email server | &#x60;User name for authentication&#x60; | | **&#x60;passwordDefined&#x60;** | Determines whether _password_ is defined for email server. | &#x60;true or false&#x60; | | **&#x60;authenticationEnabled&#x60;** | Determines whether email server requires _authentication_. | &#x60;true or false&#x60; | | **&#x60;sslEnabled&#x60;** | Determines whether email server requires _SSL_ connection.&lt;br&gt;Requires **&#x60;starttlsEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false &#x60; | | **&#x60;starttlsEnabled&#x60;** | Determines whether email server requires _StartTLS_ connection.&lt;br&gt;Requires **&#x60;sslEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;MailServerConfig&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<MailServerConfig> getMailServerConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/mail_server";

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

        GenericType<MailServerConfig> localVarReturnType = new GenericType<MailServerConfig>() {};
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get syslog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON syslog configuration entry point.   Returns a list of configurable syslog settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable syslog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Determines whether syslog is enabled. | &#x60;true or false&#x60; | | **&#x60;host&#x60;** | Syslog server (IP or FQDN) | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;port&#x60;** | Syslog server port | &#x60;Valid port number&#x60; | | **&#x60;protocol&#x60;** | Protocol to connect to syslog server | &#x60;TCP or UDP&#x60; | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return SyslogConfig
   * @throws ApiException if fails to make API call
     */
    public SyslogConfig getSyslogConfig(String xSdsAuthToken) throws ApiException {
        return getSyslogConfigWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get syslog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON syslog configuration entry point.   Returns a list of configurable syslog settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable syslog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Determines whether syslog is enabled. | &#x60;true or false&#x60; | | **&#x60;host&#x60;** | Syslog server (IP or FQDN) | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;port&#x60;** | Syslog server port | &#x60;Valid port number&#x60; | | **&#x60;protocol&#x60;** | Protocol to connect to syslog server | &#x60;TCP or UDP&#x60; | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;SyslogConfig&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SyslogConfig> getSyslogConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/syslog";

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

        GenericType<SyslogConfig> localVarReturnType = new GenericType<SyslogConfig>() {};
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get system defaults
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON system defaults configuration entry point.   Returns a list of configurable system default values.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return SystemDefaults
   * @throws ApiException if fails to make API call
     */
    public SystemDefaults getSystemDefaults(String xSdsAuthToken) throws ApiException {
        return getSystemDefaultsWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get system defaults
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON system defaults configuration entry point.   Returns a list of configurable system default values.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60; |
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;SystemDefaults&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SystemDefaults> getSystemDefaultsWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/defaults";

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
     * Change authentication settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON authentication configuration entry point.   Change configurable authentication settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more authentication methods gets changed.  ### &amp;#9432; Further Information: Authentication methods are sorted by priority attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.   Priority **MUST** be a positive value.  ### Configurable authentication settings  | Authentication Method | Description | | :--- | :--- | | **&#x60;basic&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database. | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. | | **&#x60;sql&#x60;** | cf. **&#x60;basic&#x60;** |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return AuthConfig
   * @throws ApiException if fails to make API call
     */
    public AuthConfig updateAuthConfig(AuthConfig body, String xSdsAuthToken) throws ApiException {
        return updateAuthConfigWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change authentication settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON authentication configuration entry point.   Change configurable authentication settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more authentication methods gets changed.  ### &amp;#9432; Further Information: Authentication methods are sorted by priority attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.   Priority **MUST** be a positive value.  ### Configurable authentication settings  | Authentication Method | Description | | :--- | :--- | | **&#x60;basic&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database. | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. | | **&#x60;sql&#x60;** | cf. **&#x60;basic&#x60;** |
     *
     * @param body          body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;AuthConfig&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthConfig> updateAuthConfigWithHttpInfo(AuthConfig body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateAuthConfig");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/auth";

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

        GenericType<AuthConfig> localVarReturnType = new GenericType<AuthConfig>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change branding settings
     * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   DRACOON branding configuration entry point.   Change configurable branding settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more branding settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable branding settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;brandingProviderUrl&#x60;** | Access URL to to the Branding Portal | &#x60;String&#x60; | | **&#x60;brandingQualifier&#x60;** | Branding UUID | &#x60;String&#x60; | | **&#x60;resetBrandingProviderUrl&#x60;** | Set &#x60;true&#x60; to reset **&#x60;brandingProviderUrl&#x60;** | &#x60;true or false&#x60; | | **&#x60;resetBrandingQualifier&#x60;** | Set &#x60;true&#x60; to reset **&#x60;brandingQualifier&#x60;** | &#x60;true or false&#x60; |
   * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
   * @return BrandingConfig
   * @throws ApiException if fails to make API call
   * @deprecated
     */
    @Deprecated
    public BrandingConfig updateBrandingConfig(UpdateBrandingConfig body, String xSdsAuthToken) throws ApiException {
        return updateBrandingConfigWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change branding settings
     * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   DRACOON branding configuration entry point.   Change configurable branding settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more branding settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable branding settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;brandingProviderUrl&#x60;** | Access URL to to the Branding Portal | &#x60;String&#x60; | | **&#x60;brandingQualifier&#x60;** | Branding UUID | &#x60;String&#x60; | | **&#x60;resetBrandingProviderUrl&#x60;** | Set &#x60;true&#x60; to reset **&#x60;brandingProviderUrl&#x60;** | &#x60;true or false&#x60; | | **&#x60;resetBrandingQualifier&#x60;** | Set &#x60;true&#x60; to reset **&#x60;brandingQualifier&#x60;** | &#x60;true or false&#x60; |
     *
     * @param body          body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;BrandingConfig&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BrandingConfig> updateBrandingConfigWithHttpInfo(UpdateBrandingConfig body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateBrandingConfig");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/branding";

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

        GenericType<BrandingConfig> localVarReturnType = new GenericType<BrandingConfig>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change eventlog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON eventlog configuration entry point.   Change configurable eventlog settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more eventlog settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable eventlog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Determines whether eventlog is enabled. | &#x60;true or false&#x60; | | **&#x60;retentionPeriod&#x60;** | Retention period (in _days_) of eventlog entries.&lt;br&gt;After that period, all entries are deleted. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: 7 | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return EventlogConfig
     * @throws ApiException if fails to make API call
     */
    public EventlogConfig updateEventlogConfig(UpdateEventlogConfig body, String xSdsAuthToken) throws ApiException {
        return updateEventlogConfigWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change eventlog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON eventlog configuration entry point.   Change configurable eventlog settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more eventlog settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable eventlog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Determines whether eventlog is enabled. | &#x60;true or false&#x60; | | **&#x60;retentionPeriod&#x60;** | Retention period (in _days_) of eventlog entries.&lt;br&gt;After that period, all entries are deleted. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: 7 | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
     * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;EventlogConfig&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<EventlogConfig> updateEventlogConfigWithHttpInfo(UpdateEventlogConfig body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateEventlogConfig");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/eventlog";

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

        GenericType<EventlogConfig> localVarReturnType = new GenericType<EventlogConfig>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change general settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON general settings configuration entry point.   Change configurable general settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more general settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | Determines whether media server is enabled.&lt;br&gt;**CANNOT** be enabled if media server configuration is disabled in &#x60;api.properties&#x60;.&lt;br&gt;Check **&#x60;mediaServerConfigEnabled&#x60;** with &#x60;GET /system/config/settings/infrastructure&#x60;. | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return GeneralSettings
   * @throws ApiException if fails to make API call
     */
    public GeneralSettings updateGeneralSettings(UpdateGeneralSettings body, String xSdsAuthToken) throws ApiException {
        return updateGeneralSettingsWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change general settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON general settings configuration entry point.   Change configurable general settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more general settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | Determines whether media server is enabled.&lt;br&gt;**CANNOT** be enabled if media server configuration is disabled in &#x60;api.properties&#x60;.&lt;br&gt;Check **&#x60;mediaServerConfigEnabled&#x60;** with &#x60;GET /system/config/settings/infrastructure&#x60;. | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters
     * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;GeneralSettings&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<GeneralSettings> updateGeneralSettingsWithHttpInfo(UpdateGeneralSettings body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateGeneralSettings");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/general";

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

        GenericType<GeneralSettings> localVarReturnType = new GenericType<GeneralSettings>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change mail server settings
     * ## &amp;#9888; Deprecated since version 4.11.0  ### Functional Description:   DRACOON email server configuration entry point.   Change configurable email server settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more email server settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable email server settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;host&#x60;** | Email server host | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;port&#x60;** | Email server port | &#x60;Valid port number&#x60; | | **&#x60;username&#x60;** | User name for email server | &#x60;User name for authentication&#x60; | | **&#x60;password&#x60;** | Password for email server | &#x60;String&#x60; | | **&#x60;authenticationEnabled&#x60;** | Determines whether email server requires _authentication_. | &#x60;true or false&#x60; | | **&#x60;sslEnabled&#x60;** | Determines whether email server requires _SSL_ connection.&lt;br&gt;Requires **&#x60;starttlsEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false &#x60; | | **&#x60;starttlsEnabled&#x60;** | Determines whether email server requires _StartTLS_ connection.&lt;br&gt;Requires **&#x60;sslEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false&#x60; | | **&#x60;resetUsername&#x60;** | Set &#x60;true&#x60; to reset email server **&#x60;username&#x60;** | &#x60;true or false&#x60; | | **&#x60;resetPassword&#x60;** | Set &#x60;true&#x60; to reset email server **&#x60;password&#x60;** | &#x60;true or false&#x60; |
     * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
   * @return MailServerConfig
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public MailServerConfig updateMailServerConfig(UpdateMailServerConfig body, String xSdsAuthToken) throws ApiException {
        return updateMailServerConfigWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change mail server settings
     * ## &amp;#9888; Deprecated since version 4.11.0  ### Functional Description:   DRACOON email server configuration entry point.   Change configurable email server settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more email server settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable email server settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;host&#x60;** | Email server host | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;port&#x60;** | Email server port | &#x60;Valid port number&#x60; | | **&#x60;username&#x60;** | User name for email server | &#x60;User name for authentication&#x60; | | **&#x60;password&#x60;** | Password for email server | &#x60;String&#x60; | | **&#x60;authenticationEnabled&#x60;** | Determines whether email server requires _authentication_. | &#x60;true or false&#x60; | | **&#x60;sslEnabled&#x60;** | Determines whether email server requires _SSL_ connection.&lt;br&gt;Requires **&#x60;starttlsEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false &#x60; | | **&#x60;starttlsEnabled&#x60;** | Determines whether email server requires _StartTLS_ connection.&lt;br&gt;Requires **&#x60;sslEnabled&#x60;** to be &#x60;false&#x60;. | &#x60;true or false&#x60; | | **&#x60;resetUsername&#x60;** | Set &#x60;true&#x60; to reset email server **&#x60;username&#x60;** | &#x60;true or false&#x60; | | **&#x60;resetPassword&#x60;** | Set &#x60;true&#x60; to reset email server **&#x60;password&#x60;** | &#x60;true or false&#x60; |
     * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;MailServerConfig&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<MailServerConfig> updateMailServerConfigWithHttpInfo(UpdateMailServerConfig body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateMailServerConfig");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/mail_server";

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

        GenericType<MailServerConfig> localVarReturnType = new GenericType<MailServerConfig>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change syslog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON syslog configuration entry point.   Change configurable syslog settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more syslog settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable syslog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Set &#x60;true&#x60; to enable syslog. | &#x60;true or false&#x60; | | **&#x60;host&#x60;** | Syslog server (IP or FQDN) | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;port&#x60;** | Syslog server port | &#x60;Valid port number&#x60; | | **&#x60;protocol&#x60;** | Protocol to connect to syslog server | &#x60;TCP or UDP&#x60; | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return SyslogConfig
     * @throws ApiException if fails to make API call
     */
    public SyslogConfig updateSyslogConfig(UpdateSyslogConfig body, String xSdsAuthToken) throws ApiException {
        return updateSyslogConfigWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change syslog settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON syslog configuration entry point.   Change configurable syslog settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more syslog settings gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable syslog settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;enabled&#x60;** | Set &#x60;true&#x60; to enable syslog. | &#x60;true or false&#x60; | | **&#x60;host&#x60;** | Syslog server (IP or FQDN) | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;port&#x60;** | Syslog server port | &#x60;Valid port number&#x60; | | **&#x60;protocol&#x60;** | Protocol to connect to syslog server | &#x60;TCP or UDP&#x60; | | **&#x60;logIpEnabled&#x60;** | Determines whether user’s IP address is logged. | &#x60;true or false&#x60; |
     * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;SyslogConfig&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SyslogConfig> updateSyslogConfigWithHttpInfo(UpdateSyslogConfig body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateSyslogConfig");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/syslog";

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

        GenericType<SyslogConfig> localVarReturnType = new GenericType<SyslogConfig>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change system defaults
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON system defaults configuration entry point.   Change configurable system default values.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more system default values gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return SystemDefaults
     * @throws ApiException if fails to make API call
     */
    public SystemDefaults updateSystemDefaults(UpdateSystemDefaults body, String xSdsAuthToken) throws ApiException {
        return updateSystemDefaultsWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Change system defaults
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   DRACOON system defaults configuration entry point.   Change configurable system default values.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: One or more system default values gets changed.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. |
     * @param body body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;SystemDefaults&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SystemDefaults> updateSystemDefaultsWithHttpInfo(UpdateSystemDefaults body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateSystemDefaults");
        }

        // create path and map variables
        String localVarPath = "/v4/system/config/settings/defaults";

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

        GenericType<SystemDefaults> localVarReturnType = new GenericType<SystemDefaults>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
