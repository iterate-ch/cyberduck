package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ClassificationPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.GuestUsersPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.MfaPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.PasswordPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateClassificationPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateGuestUsersPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateMfaPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdatePasswordPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateVirusProtectionPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.VirusProtectionPoliciesConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemPoliciesConfigApi {
  private ApiClient apiClient;

  public SystemPoliciesConfigApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SystemPoliciesConfigApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Change classification policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.30.0&lt;/h3&gt;  ### Description: Change current classification policies: * &#x60;shareClassificationPolicies&#x60;  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Classification policies are changed.  ### Further Information: &#x60;classificationRequiresSharePassword&#x60;: When a node has this classification or higher, it cannot be shared without a password. If the node is an encrypted file this policy has no effect. &#x60;0&#x60; means no password will be enforced.  
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ClassificationPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public ClassificationPoliciesConfig changeClassificationPoliciesConfig(UpdateClassificationPoliciesConfig body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changeClassificationPoliciesConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/classifications";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ClassificationPoliciesConfig> localVarReturnType = new GenericType<ClassificationPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Change guest user policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.40.0&lt;/h3&gt;  ### Description: Change current guest user policies.    ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Guest user policies are changed.    ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return GuestUsersPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public GuestUsersPoliciesConfig changeGuestUsersPoliciesConfig(UpdateGuestUsersPoliciesConfig body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changeGuestUsersPoliciesConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/guest_users";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<GuestUsersPoliciesConfig> localVarReturnType = new GenericType<GuestUsersPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Change MFA policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.37.0&lt;/h3&gt;  ### Description: Change current multi-factor authentication policies.    ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Multi-factor authentication policies are changed.    ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return MfaPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public MfaPoliciesConfig changeMfaPoliciesConfig(UpdateMfaPoliciesConfig body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changeMfaPoliciesConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/mfa";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MfaPoliciesConfig> localVarReturnType = new GenericType<MfaPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Change password policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.14.0&lt;/h3&gt;  ### Description:   Change current password policies for any password types:   * &#x60;login&#x60; * &#x60;shares&#x60; * &#x60;encryption&#x60;  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Password policies get changed.  ### Further Information: None.  ### Available password policies: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | Value | Recommended Value | Password Type | | :--- | :--- | :--- | :--- | :--- | | &#x60;mustContainCharacters&#x60; | Characters which a password must contain:&lt;br&gt;&lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60; - at least one alphabetical character (&#x60;uppercase&#x60; **OR** &#x60;lowercase&#x60;)&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;br&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60; - at least one uppercase character&lt;pre&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60; - at least one lowercase character&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60; - at least one numeric character&lt;pre&gt;0 1 2 3 4 5 6 7 8 9&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60; - at least one special character (letters and digits excluded)&lt;pre&gt;! \&quot; # $ % ( ) * + , - . / : ; &#x3D; ? @ [ \\ ] ^ _ { &amp;#124; } ~&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60; - none of the above&lt;/li&gt;&lt;li&gt;&#x60;all&#x60; - combination of &#x60;uppercase&#x60;, &#x60;lowercase&#x60;, &#x60;numeric&#x60; and &#x60;special&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60;&lt;/li&gt;&lt;li&gt;&#x60;all&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;uppercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60;&lt;/li&gt;&lt;/ul&gt;  | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;numberOfCharacteristicsToEnforce&#x60; | Number of characteristics to enforce.&lt;br&gt;e.g. from &#x60;[\&quot;uppercase\&quot;, \&quot;lowercase\&quot;, \&quot;numeric\&quot;, \&quot;special\&quot;]&#x60;&lt;br&gt;all 4 character sets can be enforced; but also only 2 of them | &#x60;Integer between 0 and 4&#x60;&lt;br&gt;&lt;br&gt;default:&lt;ul&gt;&lt;li&gt;&#x60;none&#x60; - &#x60;0&#x60;&lt;/li&gt;&lt;li&gt;&#x60;all&#x60; - &#x60;4&#x60;&lt;/li&gt;&lt;li&gt;otherwise - amount of distinct values&lt;br&gt;cf. &#x60;mustContainCharacters&#x60; matrix&lt;/li&gt;&lt;/ul&gt; | &#x60;3&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;minLength&#x60; | Minimum number of characters a password must contain. | &#x60;Integer between 1 and 1024&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;: &#x60;12&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;: &#x60;12&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;: &#x60;14&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectDictionaryWords&#x60; | Determines whether a password must **NOT** contain word(s) from a dictionary.&lt;br&gt;In &#x60;core-service.properties&#x60; a path to directory with dictionary files (&#x60;*.txt&#x60;) can be defined&lt;br&gt;cf. &#x60;policies.passwords.dictionary.directory&#x60;.&lt;br&gt;&lt;br&gt;If this rule gets enabled &#x60;policies.passwords.dictionary.directory&#x60; must be defined and contain dictionary files.&lt;br&gt;Otherwise, the rule will not have any effect on password validation process. | &#x60;true or false&#x60; | &#x60;true&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectUserInfo&#x60; | Determines whether a password must **NOT** contain user info.&lt;br&gt;Affects user&#x27;s **first name**, **last name**, **email** and **user name**. | &#x60;true or false&#x60; | &#x60;true&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectKeyboardPatterns&#x60; | Determines whether a password must **NOT** contain keyboard patterns.&lt;br&gt;e.g. &#x60;qwertz&#x60;, &#x60;asdf&#x60; (min. 4 character pattern) | &#x60;true or false&#x60; | &#x60;true&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;numberOfArchivedPasswords&#x60; | Number of passwords to archive. | &#x60;Integer between 0 and 10&#x60;&lt;br&gt;Set &#x60;0&#x60; to disable password history. | &#x60;3&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;passwordExpiration.enabled&#x60; | Determines whether password expiration is enabled.&lt;br&gt;Password expiration policy can only be enabled in context with &#x60;enforceLoginPasswordChange&#x60;. | &#x60;true or false&#x60; | &#x60;false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;maxPasswordAge&#x60; | Maximum allowed password age (in **days**) | &#x60;positive Integer&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;userLockout.enabled&#x60; | Determines whether user lockout is enabled. | &#x60;true or false&#x60; | &#x60;true&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;maxNumberOfLoginFailures&#x60; | Maximum allowed number of failed login attempts. | &#x60;positive Integer&#x60; | &#x60;5&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;lockoutPeriod&#x60; | Amount of **minutes** a user has to wait to make another login attempt&lt;br&gt;after &#x60;maxNumberOfLoginFailures&#x60; has been exceeded. | &#x60;positive Integer&#x60; | &#x60;10&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; |  &lt;/details&gt;  ### Deprecated password policies: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | Value | Recommended Value | Password Type | | :--- | :--- | :--- | :--- | :--- | | &lt;del&gt;&#x60;enforceLoginPasswordChange&#x60;&lt;/del&gt; | Determines whether a login password change should be enforced for all users.&lt;br&gt;Only takes effect, if login password policies get stricter.&lt;br&gt;Use &#x60;POST /system/config/policies/passwords/enforce_change&#x60; API to enforce a login password change. | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; |  &lt;/details&gt;  ### &#x60;mustContainCharacters&#x60; matrix: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  |  | &#x60;alpha&#x60; | &#x60;uppercase&#x60; | &#x60;lowercase&#x60; | &#x60;numeric&#x60; | &#x60;special&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | | &#x60;alpha&#x60; | &#x60;alpha&#x60; | &#x60;uppercase&#x60; | &#x60;lowercase&#x60; | &#x60;alpha&#x60;&lt;br&gt;&#x60;numeric&#x60; | &#x60;alpha&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | &#x60;uppercase&#x60; | &#x60;uppercase&#x60; | &#x60;uppercase&#x60; | &#x60;uppercase&#x60;&lt;br&gt;&#x60;lowercase&#x60; | &#x60;uppercase&#x60;&lt;br&gt;&#x60;numeric&#x60; | &#x60;uppercase&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | &#x60;lowercase&#x60; | &#x60;lowercase&#x60; | &#x60;uppercase&#x60;&lt;br&gt;&#x60;lowercase&#x60; | &#x60;lowercase&#x60; | &#x60;lowercase&#x60;&lt;br&gt;&#x60;numeric&#x60; | &#x60;lowercase&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | &#x60;numeric&#x60; | &#x60;alpha&#x60;&lt;br&gt;&#x60;numeric&#x60; | &#x60;uppercase&#x60;&lt;br&gt;&#x60;numeric&#x60; | &#x60;lowercase&#x60;&lt;br&gt;&#x60;numeric&#x60; | &#x60;numeric&#x60; | &#x60;numeric&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | &#x60;special&#x60; | &#x60;alpha&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;uppercase&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;lowercase&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;numeric&#x60;&lt;br&gt;&#x60;special&#x60; | &#x60;special&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | &#x60;all&#x60; | &#x60;all&#x60; | &#x60;all&#x60; | &#x60;all&#x60; | &#x60;all&#x60; | &#x60;all&#x60; | &#x60;all&#x60; | &#x60;none&#x60; | | &#x60;none&#x60; | &#x60;none&#x60; | &#x60;none&#x60; |  &#x60;none&#x60; | &#x60;none&#x60; | &#x60;none&#x60; | &#x60;none&#x60; | &#x60;none&#x60; |  &lt;/details&gt;
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PasswordPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public PasswordPoliciesConfig changePasswordPoliciesConfig(UpdatePasswordPoliciesConfig body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changePasswordPoliciesConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/passwords";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<PasswordPoliciesConfig> localVarReturnType = new GenericType<PasswordPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Change virus protection policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.44.0&lt;/h3&gt;  ### Description: Change current virus protection policy.    ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Virus Protection policy is changed.    ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return VirusProtectionPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public VirusProtectionPoliciesConfig changeVirusProtectionPoliciesConfig(UpdateVirusProtectionPoliciesConfig body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changeVirusProtectionPoliciesConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/virus_protection";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<VirusProtectionPoliciesConfig> localVarReturnType = new GenericType<VirusProtectionPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Enforce login password change for all users
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.24.0&lt;/h3&gt;  ### Description:   Enforce login password change for all users.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Login password change is enforced. Every user has to change their login password at next login.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void enforceLoginPasswordChange(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/passwords/enforce_change";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request classification policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.30.0&lt;/h3&gt;  ### Description:   Retrieve a list of classification policies: * &#x60;shareClassificationPolicies&#x60;  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of configured classification policies is returned.  ### Further Information: &#x60;classificationRequiresSharePassword&#x60;: When a node has this classification or higher, it cannot be shared without a password. If the node is an encrypted file this policy has no effect. &#x60;0&#x60; means no password will be enforced. 
   * @param xSdsAuthToken Authentication token (optional)
   * @return ClassificationPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public ClassificationPoliciesConfig requestClassificationPoliciesConfig(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/classifications";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ClassificationPoliciesConfig> localVarReturnType = new GenericType<ClassificationPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request guest user policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.40.0&lt;/h3&gt;  ### Description:   Retrieve guest user policies.    ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Guest user policies are returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return GuestUsersPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public GuestUsersPoliciesConfig requestGuestUsersPoliciesConfig(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/guest_users";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<GuestUsersPoliciesConfig> localVarReturnType = new GenericType<GuestUsersPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request MFA policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.37.0&lt;/h3&gt;  ### Description:   Retrieve a list of multi-factor authentication policies.    ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of configured multi-factor authentication policies is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return MfaPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public MfaPoliciesConfig requestMfaPoliciesConfig(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/mfa";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MfaPoliciesConfig> localVarReturnType = new GenericType<MfaPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request password policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.14.0&lt;/h3&gt;  ### Description:   Retrieve a list of configured password policies for all password types:   * &#x60;login&#x60; * &#x60;shares&#x60; * &#x60;encryption&#x60;  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of configured password policies is returned.  ### Further Information: None.  ### Available password policies: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | Value | Password Type | | :--- | :--- | :--- | :--- | | &#x60;mustContainCharacters&#x60; | Characters which a password must contain:&lt;br&gt;&lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60; - at least one alphabetical character (&#x60;uppercase&#x60; **OR** &#x60;lowercase&#x60;)&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;br&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60; - at least one uppercase character&lt;pre&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60; - at least one lowercase character&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60; - at least one numeric character&lt;pre&gt;0 1 2 3 4 5 6 7 8 9&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60; - at least one special character (letters and digits excluded)&lt;pre&gt;! \&quot; # $ % ( ) * + , - . / : ; &#x3D; ? @ [ \\ ] ^ _ { &amp;#124; } ~&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60; - none of the above&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;numberOfCharacteristicsToEnforce&#x60; | Number of characteristics to enforce.&lt;br&gt;e.g. from &#x60;[\&quot;uppercase\&quot;, \&quot;lowercase\&quot;, \&quot;numeric\&quot;, \&quot;special\&quot;]&#x60;&lt;br&gt;all 4 character sets can be enforced; but also only 2 of them | &#x60;Integer between 0 and 4&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;minLength&#x60; | Minimum number of characters a password must contain. | &#x60;Integer between 1 and 1024&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectDictionaryWords&#x60; | Determines whether a password must **NOT** contain word(s) from a dictionary.&lt;br&gt;In &#x60;core-service.properties&#x60; a path to directory with dictionary files (&#x60;*.txt&#x60;) can be defined&lt;br&gt;cf. &#x60;policies.passwords.dictionary.directory&#x60;.&lt;br&gt;&lt;br&gt;If this rule gets enabled &#x60;policies.passwords.dictionary.directory&#x60; must be defined and contain dictionary files.&lt;br&gt;Otherwise, the rule will not have any effect on password validation process. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectUserInfo&#x60; | Determines whether a password must **NOT** contain user info.&lt;br&gt;Affects user&#x27;s **first name**, **last name**, **email** and **user name**. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectKeyboardPatterns&#x60; | Determines whether a password must **NOT** contain keyboard patterns.&lt;br&gt;e.g. &#x60;qwertz&#x60;, &#x60;asdf&#x60; (min. 4 character pattern) | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;numberOfArchivedPasswords&#x60; | Number of passwords to archive.&lt;br&gt;Value &#x60;0&#x60; means that password history is disabled. | &#x60;Integer between 0 and 10&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;passwordExpiration.enabled&#x60; | Determines whether password expiration is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;maxPasswordAge&#x60; | Maximum allowed password age (in **days**) | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;userLockout.enabled&#x60; | Determines whether user lockout is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;maxNumberOfLoginFailures&#x60; | Maximum allowed number of failed login attempts. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;lockoutPeriod&#x60; | Amount of **minutes** a user has to wait to make another login attempt&lt;br&gt;after &#x60;maxNumberOfLoginFailures&#x60; has been exceeded. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; |  &lt;/details&gt;
   * @param xSdsAuthToken Authentication token (optional)
   * @return PasswordPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public PasswordPoliciesConfig requestPasswordPoliciesConfig(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/passwords";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<PasswordPoliciesConfig> localVarReturnType = new GenericType<PasswordPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request password policies for a certain password type
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.14.0&lt;/h3&gt;  ### Description:   Retrieve a list of configured password policies for a certain password type:   * &#x60;login&#x60; * &#x60;shares&#x60; * &#x60;encryption&#x60;  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of configured password policies is returned.  ### Further Information: None.  ### Available password policies: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | Value | Password Type | | :--- | :--- | :--- | :--- | | &#x60;mustContainCharacters&#x60; | Characters which a password must contain:&lt;br&gt;&lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60; - at least one alphabetical character (&#x60;uppercase&#x60; **OR** &#x60;lowercase&#x60;)&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;br&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60; - at least one uppercase character&lt;pre&gt;A B C D E F G H I J K L M N O P Q R S T U V W X Y Z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60; - at least one lowercase character&lt;pre&gt;a b c d e f g h i j k l m n o p q r s t u v w x y z&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60; - at least one numeric character&lt;pre&gt;0 1 2 3 4 5 6 7 8 9&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60; - at least one special character (letters and digits excluded)&lt;pre&gt;! \&quot; # $ % ( ) * + , - . / : ; &#x3D; ? @ [ \\ ] ^ _ { &amp;#124; } ~&lt;/pre&gt;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60; - none of the above&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;alpha&#x60;&lt;/li&gt;&lt;li&gt;&#x60;uppercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;lowercase&#x60;&lt;/li&gt;&lt;li&gt;&#x60;numeric&#x60;&lt;/li&gt;&lt;li&gt;&#x60;special&#x60;&lt;/li&gt;&lt;li&gt;&#x60;none&#x60;&lt;/li&gt;&lt;/ul&gt; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;numberOfCharacteristicsToEnforce&#x60; | Number of characteristics to enforce.&lt;br&gt;e.g. from &#x60;[\&quot;uppercase\&quot;, \&quot;lowercase\&quot;, \&quot;numeric\&quot;, \&quot;special\&quot;]&#x60;&lt;br&gt;all 4 character sets can be enforced; but also only 2 of them | &#x60;Integer between 0 and 4&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;minLength&#x60; | Minimum number of characters a password must contain. | &#x60;Integer between 1 and 1024&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectDictionaryWords&#x60; | Determines whether a password must **NOT** contain word(s) from a dictionary.&lt;br&gt;In &#x60;core-service.properties&#x60; a path to directory with dictionary files (&#x60;*.txt&#x60;) can be defined&lt;br&gt;cf. &#x60;policies.passwords.dictionary.directory&#x60;.&lt;br&gt;&lt;br&gt;If this rule gets enabled &#x60;policies.passwords.dictionary.directory&#x60; must be defined and contain dictionary files.&lt;br&gt;Otherwise, the rule will not have any effect on password validation process. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectUserInfo&#x60; | Determines whether a password must **NOT** contain user info.&lt;br&gt;Affects user&#x27;s **first name**, **last name**, **email** and **user name**. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;rejectKeyboardPatterns&#x60; | Determines whether a password must **NOT** contain keyboard patterns.&lt;br&gt;e.g. &#x60;qwertz&#x60;, &#x60;asdf&#x60; (min. 4 character pattern) | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;li&gt;&#x60;shares&#x60;&lt;/li&gt;&lt;li&gt;&#x60;encryption&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;numberOfArchivedPasswords&#x60; | Number of passwords to archive.&lt;br&gt;Value &#x60;0&#x60; means that password history is disabled. | &#x60;Integer between 0 and 10&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;passwordExpiration.enabled&#x60; | Determines whether password expiration is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;maxPasswordAge&#x60; | Maximum allowed password age (in **days**) | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;userLockout.enabled&#x60; | Determines whether user lockout is enabled. | &#x60;true or false&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;maxNumberOfLoginFailures&#x60; | Maximum allowed number of failed login attempts. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;lockoutPeriod&#x60; | Amount of **minutes** a user has to wait to make another login attempt&lt;br&gt;after &#x60;maxNumberOfLoginFailures&#x60; has been exceeded. | &#x60;positive Integer&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;login&#x60;&lt;/li&gt;&lt;/ul&gt; |  &lt;/details&gt;
   * @param passwordType Password type (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PasswordPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public PasswordPoliciesConfig requestPasswordPoliciesForPasswordType(String passwordType, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'passwordType' is set
    if (passwordType == null) {
      throw new ApiException(400, "Missing the required parameter 'passwordType' when calling requestPasswordPoliciesForPasswordType");
    }
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/passwords/{password_type}"
      .replaceAll("\\{" + "password_type" + "\\}", apiClient.escapeString(passwordType.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<PasswordPoliciesConfig> localVarReturnType = new GenericType<PasswordPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request virus protection policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.44.0&lt;/h3&gt;  ### Description: Retrieve the virus protection policy.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Virus Protection policy is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return VirusProtectionPoliciesConfig
   * @throws ApiException if fails to make API call
   */
  public VirusProtectionPoliciesConfig requestVirusProtectionPoliciesConfig(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/system/config/policies/virus_protection";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<VirusProtectionPoliciesConfig> localVarReturnType = new GenericType<VirusProtectionPoliciesConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
