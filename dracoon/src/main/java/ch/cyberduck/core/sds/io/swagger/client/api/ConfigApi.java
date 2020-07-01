package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ConfigOptionList;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneralSettingsInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.InfrastructureProperties;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationChannelList;
import ch.cyberduck.core.sds.io.swagger.client.model.PasswordPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagList;
import ch.cyberduck.core.sds.io.swagger.client.model.SystemDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
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
   * ### &amp;#128640; Since version 4.6.0  ### Functional Description: Returns a list of configurable general settings.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; | | **&#x60;homeRoomsActive&#x60;** | Determines whether each AD user has a personal home room | &#x60;true or false&#x60; | | **&#x60;homeRoomParentId&#x60;** | Defines a node under which all personal home rooms are located. NULL if &#x60;homeRoomsActive&#x60; is &#x60;false&#x60; | &#x60;Long&#x60; | | **&#x60;mediaServerEnabled&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether weak password (cf. _Password Policy_) is allowed.&lt;br&gt;Use &#x60;GET /system/config/policies/passwords&#x60; API to get configured password policies. | &#x60;true or false&#x60; |  ---  ### &#x60;DEPRECATED&#x60; Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
   * @param xSdsAuthToken Authentication token (optional)
   * @return GeneralSettingsInfo
   * @throws ApiException if fails to make API call
   */
  public GeneralSettingsInfo getGeneralSettingsInfo(String xSdsAuthToken) throws ApiException {
    return getGeneralSettingsInfoWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get general settings
   * ### &amp;#128640; Since version 4.6.0  ### Functional Description: Returns a list of configurable general settings.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable general settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;sharePasswordSmsEnabled&#x60;** | Determines whether sending of share passwords via SMS is allowed. | &#x60;true or false&#x60; | | **&#x60;cryptoEnabled&#x60;** | Determines whether client-side encryption is enabled.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;emailNotificationButtonEnabled&#x60;** | Determines whether email notification button is enabled. | &#x60;true or false&#x60; | | **&#x60;eulaEnabled&#x60;** | Determines whether EULA is enabled.&lt;br&gt;Each user has to confirm the EULA at first login. | &#x60;true or false&#x60; | | **&#x60;useS3Storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible. | &#x60;true or false&#x60; | | **&#x60;s3TagsEnabled&#x60;** | Determines whether S3 tags are enabled | &#x60;true or false&#x60; | | **&#x60;homeRoomsActive&#x60;** | Determines whether each AD user has a personal home room | &#x60;true or false&#x60; | | **&#x60;homeRoomParentId&#x60;** | Defines a node under which all personal home rooms are located. NULL if &#x60;homeRoomsActive&#x60; is &#x60;false&#x60; | &#x60;Long&#x60; | | **&#x60;mediaServerEnabled&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether media server is enabled.&lt;br&gt;Returns boolean value dependent on conjunction of **&#x60;mediaServerConfigEnabled&#x60;** AND **&#x60;mediaServerEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;weakPasswordEnabled&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether weak password (cf. _Password Policy_) is allowed.&lt;br&gt;Use &#x60;GET /system/config/policies/passwords&#x60; API to get configured password policies. | &#x60;true or false&#x60; |  ---  ### &#x60;DEPRECATED&#x60; Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<GeneralSettingsInfo> localVarReturnType = new GenericType<GeneralSettingsInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get infrastructure properties
   * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of read-only infrastructure properties.    ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; | | **&#x60;s3EnforceDirectUpload&#x60;** | Enforce direct upload to S3 | &#x60;true or false&#x60; | 
   * @param xSdsAuthToken Authentication token (optional)
   * @return InfrastructureProperties
   * @throws ApiException if fails to make API call
   */
  public InfrastructureProperties getInfrastructurePropertiesInfo(String xSdsAuthToken) throws ApiException {
    return getInfrastructurePropertiesInfoWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get infrastructure properties
   * ### &amp;#128640; Since version 4.6.0  ### Functional Description:   Returns a list of read-only infrastructure properties.    ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Source: &#x60;api.properties&#x60;  ### Read-only infrastructure properties  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;smsConfigEnabled&#x60;** | Determines whether sending of share passwords via SMS is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;mediaServerConfigEnabled&#x60;** | Determines whether media server is **system-wide** enabled. | &#x60;true or false&#x60; | | **&#x60;s3DefaultRegion&#x60;** | Suggested S3 region | &#x60;Region name&#x60; | | **&#x60;s3EnforceDirectUpload&#x60;** | Enforce direct upload to S3 | &#x60;true or false&#x60; | 
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Get password policies
   * ### &amp;#128640; Since version 4.14.0  ### Functional Description:   Retrieve a list of configured password policies for all password types:   * &#x60;login&#x60; * &#x60;shares&#x60; * &#x60;encryption&#x60;  ### Precondition: Authenticated user.  ### Effects: None.  ### Further Information:  ### Available password policies  | Name | Description | Value | Password Type | | :--- | :--- | :--- | :--- | | **&#x60;mustContainCharacters&#x60;** | Characters which a password must contain:&lt;br&gt;&lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60; - at least one alphabetical character (&#x60;uppercase&#x60; **OR** &#x60;lowercase&#x60;)&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;br&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60; - at least one uppercase character&lt;pre&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60; - at least one lowercase character&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60; - at least one numeric character&lt;pre&gt;0 1 2 3 4 5 6 7 8 9&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60; - at least one special character (letters and digits excluded)&lt;pre&gt;! \&quot; # $ % ( ) * + , - . / : ; &#x3D; ? @ [ \\ ] ^ _ { &amp;#124; } ~&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60; - none of the above&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;numberOfCharacteristicsToEnforce&#x60;** | Number of characteristics to enforce.&lt;br&gt;e.g. from &#x60;[\&quot;uppercase\&quot;, \&quot;lowercase\&quot;, \&quot;numeric\&quot;, \&quot;special\&quot;]&#x60;&lt;br&gt;all 4 character sets can be enforced; but also only 2 of them | &#x60;Integer between 0 and 4&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;minLength&#x60;** | Minimum number of characters a password must contain. | &#x60;Integer between 1 and 1024&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;rejectDictionaryWords&#x60;** | Determines whether a password must **NOT** contain word(s) from a dictionary.&lt;br&gt;In &#x60;api.properties&#x60; a path to directory with dictionary files (&#x60;*.txt&#x60;) can be defined&lt;br&gt;cf. &#x60;policies.passwords.dictionary.directory&#x60;.&lt;br&gt;&lt;br&gt;If this rule gets enabled &#x60;policies.passwords.dictionary.directory&#x60; must be defined and contain dictionary files.&lt;br&gt;Otherwise, the rule will not have any effect on password validation process. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;rejectUserInfo&#x60;** | Determines whether a password must **NOT** contain user info.&lt;br&gt;Affects user&#39;s **first name**, **last name**, **email** and **user name**. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;rejectKeyboardPatterns&#x60;** | Determines whether a password must **NOT** contain keyboard patterns.&lt;br&gt;e.g. &#x60;qwertz&#x60;, &#x60;asdf&#x60; (min. 4 character pattern) | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;numberOfArchivedPasswords&#x60;** | Number of passwords to archive.&lt;br&gt;Value &#x60;0&#x60; means that password history is disabled. | &#x60;Integer between 0 and 10&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;passwordExpiration.enabled&#x60;** | Determines whether password expiration is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;maxPasswordAge&#x60;** | Maximum allowed password age (in **days**) | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;userLockout.enabled&#x60;** | Determines whether user lockout is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;maxNumberOfLoginFailures&#x60;** | Maximum allowed number of failed login attempts. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;lockoutPeriod&#x60;** | Amount of **minutes** a user has to wait to make another login attempt&lt;br&gt;after **&#x60;maxNumberOfLoginFailures&#x60;** has been exceeded. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return PasswordPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public PasswordPoliciesConfig getPasswordPoliciesConfigInfo(String xSdsAuthToken) throws ApiException {
    return getPasswordPoliciesConfigInfoWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get password policies
   * ### &amp;#128640; Since version 4.14.0  ### Functional Description:   Retrieve a list of configured password policies for all password types:   * &#x60;login&#x60; * &#x60;shares&#x60; * &#x60;encryption&#x60;  ### Precondition: Authenticated user.  ### Effects: None.  ### Further Information:  ### Available password policies  | Name | Description | Value | Password Type | | :--- | :--- | :--- | :--- | | **&#x60;mustContainCharacters&#x60;** | Characters which a password must contain:&lt;br&gt;&lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60; - at least one alphabetical character (&#x60;uppercase&#x60; **OR** &#x60;lowercase&#x60;)&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;br&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60; - at least one uppercase character&lt;pre&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60; - at least one lowercase character&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60; - at least one numeric character&lt;pre&gt;0 1 2 3 4 5 6 7 8 9&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60; - at least one special character (letters and digits excluded)&lt;pre&gt;! \&quot; # $ % ( ) * + , - . / : ; &#x3D; ? @ [ \\ ] ^ _ { &amp;#124; } ~&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60; - none of the above&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;numberOfCharacteristicsToEnforce&#x60;** | Number of characteristics to enforce.&lt;br&gt;e.g. from &#x60;[\&quot;uppercase\&quot;, \&quot;lowercase\&quot;, \&quot;numeric\&quot;, \&quot;special\&quot;]&#x60;&lt;br&gt;all 4 character sets can be enforced; but also only 2 of them | &#x60;Integer between 0 and 4&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;minLength&#x60;** | Minimum number of characters a password must contain. | &#x60;Integer between 1 and 1024&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;rejectDictionaryWords&#x60;** | Determines whether a password must **NOT** contain word(s) from a dictionary.&lt;br&gt;In &#x60;api.properties&#x60; a path to directory with dictionary files (&#x60;*.txt&#x60;) can be defined&lt;br&gt;cf. &#x60;policies.passwords.dictionary.directory&#x60;.&lt;br&gt;&lt;br&gt;If this rule gets enabled &#x60;policies.passwords.dictionary.directory&#x60; must be defined and contain dictionary files.&lt;br&gt;Otherwise, the rule will not have any effect on password validation process. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;rejectUserInfo&#x60;** | Determines whether a password must **NOT** contain user info.&lt;br&gt;Affects user&#39;s **first name**, **last name**, **email** and **user name**. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;rejectKeyboardPatterns&#x60;** | Determines whether a password must **NOT** contain keyboard patterns.&lt;br&gt;e.g. &#x60;qwertz&#x60;, &#x60;asdf&#x60; (min. 4 character pattern) | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;numberOfArchivedPasswords&#x60;** | Number of passwords to archive.&lt;br&gt;Value &#x60;0&#x60; means that password history is disabled. | &#x60;Integer between 0 and 10&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;passwordExpiration.enabled&#x60;** | Determines whether password expiration is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;maxPasswordAge&#x60;** | Maximum allowed password age (in **days**) | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;userLockout.enabled&#x60;** | Determines whether user lockout is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;maxNumberOfLoginFailures&#x60;** | Maximum allowed number of failed login attempts. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;lockoutPeriod&#x60;** | Amount of **minutes** a user has to wait to make another login attempt&lt;br&gt;after **&#x60;maxNumberOfLoginFailures&#x60;** has been exceeded. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;PasswordPoliciesConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PasswordPoliciesConfig> getPasswordPoliciesConfigInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/info/policies/passwords";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<PasswordPoliciesConfig> localVarReturnType = new GenericType<PasswordPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of configured S3 tags
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description: Retrieve all configured S3 tags.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: An empty list is returned if no S3 tags are found / configured.
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {};
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description:   Returns a list of configurable system settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention If &#x60;eula_active&#x60; is true, but **NOT** accepted yet, or password **MUST** be changed, only the following two values are returned: * **&#x60;allow_system_global_weak_password&#x60;** * **&#x60;eula_active&#x60;**  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;globally_allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS **system-wide** (read-only).&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.smsConfigEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;use_s3_storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.useS3Storage&#x60;** | &#x60;true or false&#x60; | | **&#x60;s3_default_region&#x60;** |Suggested S3 region (read-only)&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.s3DefaultRegion&#x60;** | &#x60;Region name&#x60; |  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;**&lt;br&gt;Use &#x60;GET /system/config/policies/passwords&#x60; API to get configured password policies. | &#x60;true or false&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |  ---  ### &#x60;DEPRECATED&#x60; Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
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
   * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description:   Returns a list of configurable system settings.  ### Precondition: Right _\&quot;read global config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention If &#x60;eula_active&#x60; is true, but **NOT** accepted yet, or password **MUST** be changed, only the following two values are returned: * **&#x60;allow_system_global_weak_password&#x60;** * **&#x60;eula_active&#x60;**  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;GET /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60; | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;GET /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;globally_allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS **system-wide** (read-only).&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.smsConfigEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;use_s3_storage&#x60;** | Defines if S3 is used as storage backend.&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.useS3Storage&#x60;** | &#x60;true or false&#x60; | | **&#x60;s3_default_region&#x60;** |Suggested S3 region (read-only)&lt;br&gt;cf. &#x60;GET /system/config/settings/infrastructure&#x60; **&#x60;InfrastructureProperties.s3DefaultRegion&#x60;** | &#x60;Region name&#x60; |  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;GET /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;**&lt;br&gt;Use &#x60;GET /system/config/policies/passwords&#x60; API to get configured password policies. | &#x60;true or false&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |  ---  ### &#x60;DEPRECATED&#x60; Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Get list of notification channels 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of configured notification channels.  ### Precondition: Authenticated user.  ### Effects: List of notification channels is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return NotificationChannelList
   * @throws ApiException if fails to make API call
   */
  public NotificationChannelList requestNotificationChannelsInfo(String xSdsAuthToken) throws ApiException {
    return requestNotificationChannelsInfoWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get list of notification channels 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of configured notification channels.  ### Precondition: Authenticated user.  ### Effects: List of notification channels is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;NotificationChannelList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NotificationChannelList> requestNotificationChannelsInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/config/info/notifications/channels";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<NotificationChannelList> localVarReturnType = new GenericType<NotificationChannelList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Change system settings
   * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description: Change configurable settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: One or more global settings gets changed.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention Only visible for _Config Manager_ of Provider Customer.  ### Settings  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; |  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;**&lt;br&gt;Use &#x60;PUT /system/config/policies/passwords&#x60; API to change configured password policies. | &#x60;true or false&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |  ---  ### &#x60;DEPRECATED&#x60; Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
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
   * ## &amp;#9888; Deprecated since version 4.6.0  ### Functional Description: Change configurable settings.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: One or more global settings gets changed.  ### &amp;#9432; Further Information: This API is **&#x60;DEPRECATED&#x60;** and will be removed in the future.   Check for every settings key new corresponding API and key below.  ### Attention Only visible for _Config Manager_ of Provider Customer.  ### Settings  ### Configurable settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;dblog&#x60;** | Write logs to local database.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;default_downloadshare_expiration_period&#x60;** | Default expiration period for Download Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.downloadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_file_upload_expiration_date&#x60;** | Default expiration period for all uploaded files in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.fileDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;default_language&#x60;** | Define which language should be default.&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.languageDefault&#x60;** | cf. &#x60;GET /public/system/info&#x60; - **&#x60;SystemInfo.languageDefault&#x60;** | | **&#x60;default_uploadshare_expiration_period&#x60;** | Default expiration period for Upload Shares in days&lt;br&gt;cf. &#x60;PUT /system/config/settings/defaults&#x60; **&#x60;SystemDefaults.uploadShareDefaultExpirationPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable. | | **&#x60;enable_client_side_crypto&#x60;** | Activation status of client-side encryption&lt;br&gt;Can only be enabled once; disabling is **NOT** possible.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.cryptoEnabled&#x60;** | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;eula_active&#x60;** | Each user has to confirm the EULA at first login.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.eulaEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;eventlog_retention_period&#x60;** | Retention period (in days) of event log entries&lt;br&gt;After that period, all entries are deleted.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.retentionPeriod&#x60;** | &#x60;Integer between 0 and 9999&#x60;&lt;br&gt;If set to &#x60;0&#x60;: no logs are deleted&lt;br&gt;Recommended value: &#x60;7&#x60; | | **&#x60;ip_address_logging&#x60;** | Determines whether a user&#39;s IP address is logged.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/eventlog&#x60; **&#x60;EventlogConfig.logIpEnabled&#x60;**&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.logIpEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog&#x60;** | Write logs to a syslog interface.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.enabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;syslog_host&#x60;** | Syslog server (IP or FQDN)&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.host&#x60;** | &#x60;DNS name or IPv4 of a syslog server&#x60; | | **&#x60;syslog_port&#x60;** | Syslog server port&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.port&#x60;** | &#x60;Valid port number&#x60; | | **&#x60;syslog_protocol&#x60;** | Protocol to connect to syslog server.&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;cf. &#x60;PUT /system/config/settings/syslog&#x60; **&#x60;SyslogConfig.protocol&#x60;** | &#x60;TCP or UDP&#x60; | | **&#x60;enable_email_notification_button&#x60;** | Enable mail notification button.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.emailNotificationButtonEnabled&#x60;** | &#x60;true or false&#x60; | | **&#x60;allow_share_password_sms&#x60;** | Allow sending of share passwords via SMS.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.sharePasswordSmsEnabled&#x60;** | &#x60;true or false&#x60; |  ### &#x60;DEPRECATED&#x60; settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;allow_system_global_weak_password&#x60;** **(&#x60;DEPRECATED&#x60;)** | Determines whether weak password (cf. _Password Policy_ below) is allowed.&lt;br&gt;cf. &#x60;PUT /system/config/settings/general&#x60; **&#x60;GeneralSettings.weakPasswordEnabled&#x60;**&lt;br&gt;Use &#x60;PUT /system/config/policies/passwords&#x60; API to change configured password policies. | &#x60;true or false&#x60; | | **&#x60;email_from&#x60;** (**&#x60;DEPRECATED&#x60;**) | Sender of system-generated emails&lt;br&gt;Only visible for _Config Manager_ of Provider Customer.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_sales&#x60;** (**&#x60;DEPRECATED&#x60;**) | Contact email address for customers to request more user licenses or data volume.&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;email_to_support&#x60;** (**&#x60;DEPRECATED&#x60;**) | Support email address for users&lt;br&gt;**Moved to branding** | &#x60;Valid email address&#x60; | | **&#x60;file_size_js&#x60;** (**&#x60;DEPRECATED&#x60;**) | Maximum file size (in bytes) for downloads of encrypted files with JavaScript.&lt;br&gt;Bigger files will require a JavaApplet. | &#x60;Integer&#x60;&lt;br&gt;Recommended value: &#x60;10485760&#x60; (&#x3D;&#x60;10MB&#x60;) | | **&#x60;system_name&#x60;** (**&#x60;DEPRECATED&#x60;**) | System name&lt;br&gt;**Moved to branding; use &#x60;product.title&#x60;** | &#x60;Display name of the DRACOON&#x60; |  ---  ### &#x60;DEPRECATED&#x60; Password Policy  * A weak password has to fulfill the following criteria:       * is at least 8 characters long       * contains letters and numbers * A strong password has to fulfill the following criteria in addition:       * contains at least one special character       * contains upper and lower case characters 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<Void> setSystemSettingWithHttpInfo(ConfigOptionList body, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
