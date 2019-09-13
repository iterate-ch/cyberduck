package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.ConfigOptionList;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneralSettingsInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.InfrastructureProperties;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagList;
import ch.cyberduck.core.sds.io.swagger.client.model.SystemDefaults;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:08:20.178+02:00")
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
   * Get general settings
   * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of configurable general settings.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | (**&#x60;DEPRECATED&#x60;**) Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; | | **&#x60;homeRoomsActive&#x60;** | Determines whether each AD user has a personal home room | &#x60;true or false&#x60; | | **&#x60;homeRoomParentId&#x60;** | Defines a node under which all personal home rooms are located. NULL if &#x60;homeRoomsActive&#x60; is &#x60;false&#x60; | &#x60;Long&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
   * @param xSdsAuthToken Authentication token (optional)
   * @return GeneralSettingsInfo
   * @throws ApiException if fails to make API call
   */
  public GeneralSettingsInfo getGeneralSettingsInfo(String xSdsAuthToken) throws ApiException {
      return getGeneralSettingsInfoWithHttpInfo(xSdsAuthToken).getData();
  }

    /**
     * Get general settings
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of configurable general settings.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;mediaServerEnabled&#x60;** | (**&#x60;DEPRECATED&#x60;**) Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** | Determines whether weak password (cf. _Password Policy_) is allowed. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; | | **&#x60;homeRoomsActive&#x60;** | Determines whether each AD user has a personal home room | &#x60;true or false&#x60; | | **&#x60;homeRoomParentId&#x60;** | Defines a node under which all personal home rooms are located. NULL if &#x60;homeRoomsActive&#x60; is &#x60;false&#x60; | &#x60;Long&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters
   * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;GeneralSettingsInfo&gt;
   * @throws ApiException if fails to make API call
     */
    public ApiResponse<GeneralSettingsInfo> getGeneralSettingsInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
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

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<GeneralSettingsInfo> localVarReturnType = new GenericType<GeneralSettingsInfo>() {
        };
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get infrastructure properties
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of read-only infrastructure properties.    ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return InfrastructureProperties
   * @throws ApiException if fails to make API call
   */
    public InfrastructureProperties getInfrastructurePropertiesInfo(String xSdsAuthToken) throws ApiException {
        return getInfrastructurePropertiesInfoWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get infrastructure properties
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of read-only infrastructure properties.    ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;InfrastructureProperties&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<InfrastructureProperties> getInfrastructurePropertiesInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/config/info/infrastructure";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<InfrastructureProperties> localVarReturnType = new GenericType<InfrastructureProperties>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get list of configured S3 tags
     * ### &amp;#128640; Since version 4.9.0  ### Functional Description: Retrieve all configured S3 tags.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: An empty list is returned if no S3 tags are found / configured.
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return S3TagList
     * @throws ApiException if fails to make API call
     */
    public S3TagList getS3TagsInfo(String xSdsAuthToken) throws ApiException {
        return getS3TagsInfoWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get list of configured S3 tags
     * ### &amp;#128640; Since version 4.9.0  ### Functional Description: Retrieve all configured S3 tags.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: An empty list is returned if no S3 tags are found / configured.
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;S3TagList&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<S3TagList> getS3TagsInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/config/info/s3_tags";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get default values
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of configurable system default values.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;nonmemberViewerDefault&#x60;** | Defines if new users get the role NONMEMBER_VIEWER by default | &#x60;true or false&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return SystemDefaults
     * @throws ApiException if fails to make API call
     */
    public SystemDefaults getSystemDefaultsInfo(String xSdsAuthToken) throws ApiException {
        return getSystemDefaultsInfoWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get default values
     * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of configurable system default values.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable default values  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; | | **&#x60;downloadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Download Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;uploadShareDefaultExpirationPeriod&#x60;** | Default expiration period for Upload Shares in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;fileDefaultExpirationPeriod&#x60;** | Default expiration period for all uploaded files in _days_. | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;nonmemberViewerDefault&#x60;** | Defines if new users get the role NONMEMBER_VIEWER by default | &#x60;true or false&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;SystemDefaults&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SystemDefaults> getSystemDefaultsInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/config/info/defaults";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<SystemDefaults> localVarReturnType = new GenericType<SystemDefaults>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get system settings
     * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description:   Returns a list of configurable system settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention If &#x60;eula_active&#x60; is true, but **NOT** accepted yet, or password **MUST** be changed, only the following two values are returned: * **&#x60;allow_system_global_weak_password&#x60;** * **&#x60;eula_active&#x60;**  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;branding_server_branding_id&#x60;** | The branding UUID, which corresponds to _BRANDING-QUALIFIER_ in the new branding server.&lt;br&gt;cf. &#x60;GET /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingQualifier&#x60;** | &#x60;String&#x60; | | **&#x60;branding_portal_url&#x60;** | Access URL to to the Branding Portal&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingProviderUrl&#x60;** | &#x60;String&#x60; | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver&#x60;** | Email server to send emails.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.host&#x60;** | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;mailserver_authentication_necessary&#x60;** | Set to &#x60;true&#x60; if the email server requires authentication.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.authenticationEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_password&#x60;** | **Password is no longer returned.**&lt;br&gt;Check **&#x60;mailserver_password_set&#x60;** to determine whether password is set. |  | | **&#x60;mailserver_password_set&#x60;** | Indicates if a password is set for the mailserver (because **&#x60;mailserver_password&#x60;** is always returned empty).&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.passwordDefined&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_port&#x60;** | Email server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;mailserver_username&#x60;** | User ame for email server&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;Username for authentication&#x60; | | **&#x60;mailserver_use_ssl&#x60;** | Email server requires SSL connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_starttls&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_use_starttls&#x60;** | Email server requires StartTLS connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_ssl&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.starttlsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;globally_allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS **system-wide** (read-only).&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.smsConfigEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;use_s3_storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.useS3Storage&#x60;** | &#x60;true or false&#x60; | | **&#x60;s3_default_region&#x60;** |Suggested S3 region (read-only)&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.s3DefaultRegion&#x60;** | &#x60;Region name&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;branding_server_customer&#x60;** (**&#x60;DEPRECATED&#x60;**) | The UUID of the branding server customer, which corresponds to customer key in the branding server. | &#x60;String&#x60; | | **&#x60;branding_server_url&#x60;** (**&#x60;DEPRECATED&#x60;**) | Access URL to to the Branding Server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer. | &#x60;String&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |
     * @param xSdsAuthToken Authentication token (optional)
   * @return ConfigOptionList
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ConfigOptionList getSystemSettings(String xSdsAuthToken) throws ApiException {
        return getSystemSettingsWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get system settings
     * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description:   Returns a list of configurable system settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention If &#x60;eula_active&#x60; is true, but **NOT** accepted yet, or password **MUST** be changed, only the following two values are returned: * **&#x60;allow_system_global_weak_password&#x60;** * **&#x60;eula_active&#x60;**  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;branding_server_branding_id&#x60;** | The branding UUID, which corresponds to _BRANDING-QUALIFIER_ in the new branding server.&lt;br&gt;cf. &#x60;GET /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingQualifier&#x60;** | &#x60;String&#x60; | | **&#x60;branding_portal_url&#x60;** | Access URL to to the Branding Portal&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingProviderUrl&#x60;** | &#x60;String&#x60; | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver&#x60;** | Email server to send emails.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.host&#x60;** | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;mailserver_authentication_necessary&#x60;** | Set to &#x60;true&#x60; if the email server requires authentication.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.authenticationEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_password&#x60;** | **Password is no longer returned.**&lt;br&gt;Check **&#x60;mailserver_password_set&#x60;** to determine whether password is set. |  | | **&#x60;mailserver_password_set&#x60;** | Indicates if a password is set for the mailserver (because **&#x60;mailserver_password&#x60;** is always returned empty).&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.passwordDefined&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_port&#x60;** | Email server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;mailserver_username&#x60;** | User ame for email server&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;Username for authentication&#x60; | | **&#x60;mailserver_use_ssl&#x60;** | Email server requires SSL connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_starttls&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_use_starttls&#x60;** | Email server requires StartTLS connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_ssl&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;GET /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.starttlsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;globally_allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS **system-wide** (read-only).&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.smsConfigEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;use_s3_storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.useS3Storage&#x60;** | &#x60;true or false&#x60; | | **&#x60;s3_default_region&#x60;** |Suggested S3 region (read-only)&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.s3DefaultRegion&#x60;** | &#x60;Region name&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;branding_server_customer&#x60;** (**&#x60;DEPRECATED&#x60;**) | The UUID of the branding server customer, which corresponds to customer key in the branding server. | &#x60;String&#x60; | | **&#x60;branding_server_url&#x60;** (**&#x60;DEPRECATED&#x60;**) | Access URL to to the Branding Server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer. | &#x60;String&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;ConfigOptionList&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<ConfigOptionList> getSystemSettingsWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/config/settings";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<ConfigOptionList> localVarReturnType = new GenericType<ConfigOptionList>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change system settings
     * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description: Change configurable settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: One or more global settings gets changed.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention Only visible for _Config Manager_ of Provider Customer.  ### Settings  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;branding_server_branding_id&#x60;** | The branding UUID, which corresponds to _BRANDING-QUALIFIER_ in the new branding server.&lt;br&gt;cf. &#x60;PUT /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingQualifier&#x60;** | &#x60;String&#x60; | | **&#x60;branding_portal_url&#x60;** | Access URL to to the Branding Portal&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingProviderUrl&#x60;** | &#x60;String&#x60; | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver&#x60;** | Email server to send emails.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.host&#x60;** | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;mailserver_authentication_necessary&#x60;** | Set to &#x60;true&#x60; if the email server requires authentication.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.authenticationEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_password&#x60;** | Password for email server&lt;br&gt;cf. &#x60;PUT /system/config/settings/password&#x60; **&#x60;MailServerConfig.password&#x60;** | &#x60;Password for authentication&#x60; | | **&#x60;mailserver_port&#x60;** | Email server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;mailserver_username&#x60;** | Username for email server&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;Username for authentication&#x60; | | **&#x60;mailserver_use_ssl&#x60;** | Email server requires SSL connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_starttls&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_use_starttls&#x60;** | Email server requires StartTLS connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_ssl&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.starttlsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;branding_server_customer&#x60;** (**&#x60;DEPRECATED&#x60;**) | The UUID of the branding server customer, which corresponds to customer key in the branding server. | &#x60;String&#x60; | | **&#x60;branding_server_url&#x60;** (**&#x60;DEPRECATED&#x60;**) | Access URL to to the Branding Server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer. | &#x60;String&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void setSystemSetting(ConfigOptionList body, String xSdsAuthToken) throws ApiException {

        setSystemSettingWithHttpInfo(body, xSdsAuthToken);
    }

    /**
     * Change system settings
     * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description: Change configurable settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: One or more global settings gets changed.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention Only visible for _Config Manager_ of Provider Customer.  ### Settings  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;branding_server_branding_id&#x60;** | The branding UUID, which corresponds to _BRANDING-QUALIFIER_ in the new branding server.&lt;br&gt;cf. &#x60;PUT /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingQualifier&#x60;** | &#x60;String&#x60; | | **&#x60;branding_portal_url&#x60;** | Access URL to to the Branding Portal&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/branding&#x60; **&#x60;BrandingConfig.brandingProviderUrl&#x60;** | &#x60;String&#x60; | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver&#x60;** | Email server to send emails.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.host&#x60;** | &#x60;DNS name or IPv4 of an email server&#x60; | | **&#x60;mailserver_authentication_necessary&#x60;** | Set to &#x60;true&#x60; if the email server requires authentication.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.authenticationEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_password&#x60;** | Password for email server&lt;br&gt;cf. &#x60;PUT /system/config/settings/password&#x60; **&#x60;MailServerConfig.password&#x60;** | &#x60;Password for authentication&#x60; | | **&#x60;mailserver_port&#x60;** | Email server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;mailserver_username&#x60;** | Username for email server&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;Username for authentication&#x60; | | **&#x60;mailserver_use_ssl&#x60;** | Email server requires SSL connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_starttls&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.username&#x60;** | &#x60;true or false&#x60; | | **&#x60;mailserver_use_starttls&#x60;** | Email server requires StartTLS connection?&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;Requires **&#x60;mailserver_use_ssl&#x60;** to be &#x60;false&#x60;&lt;br&gt;cf. &#x60;PUT /system/config/settings/mail_server&#x60; **&#x60;MailServerConfig.starttlsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; |  ---  ### Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;branding_server_customer&#x60;** (**&#x60;DEPRECATED&#x60;**) | The UUID of the branding server customer, which corresponds to customer key in the branding server. | &#x60;String&#x60; | | **&#x60;branding_server_url&#x60;** (**&#x60;DEPRECATED&#x60;**) | Access URL to to the Branding Server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer. | &#x60;String&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |
     *
     * @param body          body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> setSystemSettingWithHttpInfo(ConfigOptionList body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if(body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling setSystemSetting");
        }

        // create path and map variables
        String localVarPath = "/v4/config/settings";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
            "application/json;charset=UTF-8"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};


        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
