package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.AuthInitResources;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.OpenIdAuthResources;
import ch.cyberduck.core.sds.io.swagger.client.model.RadiusChallengeResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordTokenValidateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordWithTokenRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-03T10:55:56.129+02:00")
public class AuthApi {
  private ApiClient apiClient;

  public AuthApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AuthApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Complete OpenID Connect authentication
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;This is the second step of the OpenID Connect authentication. (The user hands over the Authorization Code and is logged in.)&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Existing user with activated OpenID Connect authentication that is not locked.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; User is logged in.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; See &lt;a href&#x3D;\&quot;http://openid.net/developers/specs\&quot;&gt;http://openid.net/developers/specs&lt;/a&gt; for further information.&lt;/p&gt;&lt;/div&gt;
   * @param code Authorization code. (required)
   * @param state Authentication state. (required)
   * @return LoginResponse
   * @throws ApiException if fails to make API call
   */
  public LoginResponse completeOpenIdLogin(String code, String state) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'code' is set
    if (code == null) {
      throw new ApiException(400, "Missing the required parameter 'code' when calling completeOpenIdLogin");
    }
    
    // verify the required parameter 'state' is set
    if (state == null) {
      throw new ApiException(400, "Missing the required parameter 'state' when calling completeOpenIdLogin");
    }
    
    // create path and map variables
    String localVarPath = "/auth/openid/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "code", code));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "state", state));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LoginResponse> localVarReturnType = new GenericType<LoginResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get authentication resources [DEPRECATED]
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;Provides information about authentication options.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The server identifies the relevant customer by the passed HTTP header \&quot;Origin\&quot;. Use this call to customize the log-on form.&lt;br/&gt;&lt;u&gt;Important:&lt;/u&gt; The default language and authentication method are always provided as topmost entry.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Currently supported languages (with ISO 639-1 code):&lt;/strong&gt;&lt;br&gt;&lt;/p&gt;&lt;ul&gt;&lt;li&gt;German (de)&lt;/li&gt;&lt;li&gt;English (en)&lt;/li&gt;&lt;li&gt;Spanish (es)&lt;/li&gt;&lt;li&gt;French (fr)&lt;/li&gt;&lt;/ul&gt;&lt;/div&gt;
   * @return AuthInitResources
   * @throws ApiException if fails to make API call
   */
  public AuthInitResources getAuthInitResources() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/auth/resources";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AuthInitResources> localVarReturnType = new GenericType<AuthInitResources>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get OpenID Connect authentication resources [DEPRECATED]
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;Provides information about OpenID Connect authentication options.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @return OpenIdAuthResources
   * @throws ApiException if fails to make API call
   */
  public OpenIdAuthResources getOpenIdAuthResources() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/auth/openid/resources";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<OpenIdAuthResources> localVarReturnType = new GenericType<OpenIdAuthResources>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Initiate OpenID Connect authentication
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;This is the first step of the OpenID Connect authentication. (The user is send to the OpenID Connect identity provider to authenticate himself and retrieve an Authorization Code.)&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; User is redirected to OpenID Connect identity provider to authenticate himself.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; See &lt;a href&#x3D;\&quot;http://openid.net/developers/specs\&quot;&gt;http://openid.net/developers/specs&lt;/a&gt; for further information.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Caution:&lt;/strong&gt; &lt;b style&#x3D;&#39;color: red;&#39;&gt;This API does not work with Swagger!&lt;/b&gt; Swagger can not handle the redirect to the OpenID Connect identity provider.&lt;/p&gt;&lt;/div&gt;
   * @param issuer Issuer identifier of the OpenID Connect identity provider. (required)
   * @param redirectUri Redirect URI to complete the OpenID Connect authentication. (required)
   * @param language Language ID or ISO 639-1 code. (optional)
   * @param test Flag to test the authentication parameters. (If the request is valid, the API will responde with 204.) (optional)
   * @throws ApiException if fails to make API call
   */
  public void initiateOpenIdLogin(String issuer, String redirectUri, String language, Boolean test) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'issuer' is set
    if (issuer == null) {
      throw new ApiException(400, "Missing the required parameter 'issuer' when calling initiateOpenIdLogin");
    }
    
    // verify the required parameter 'redirectUri' is set
    if (redirectUri == null) {
      throw new ApiException(400, "Missing the required parameter 'redirectUri' when calling initiateOpenIdLogin");
    }
    
    // create path and map variables
    String localVarPath = "/auth/openid/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "issuer", issuer));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "redirect_uri", redirectUri));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "language", language));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "test", test));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Authenticate user
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;Authenticates user and provides an authentication token that is required for most operations.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Existing user that is not locked.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; User is logged in.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The provided token is valid for &lt;em&gt;two hours&lt;/em&gt;, every usage resets this period to two full hours again. Logging off invalidates the token.&lt;br/&gt;&lt;u&gt;Important:&lt;/u&gt; If auth type \&quot;radius\&quot; is used, a token (request parameter) may be set, otherwise this parameter is ignored. If the token is set, &lt;b&gt;password&lt;/b&gt; is optional for this auth type.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Currently supported languages (with ISO 639-1 code):&lt;/strong&gt;&lt;br&gt;&lt;/p&gt;&lt;ul&gt;&lt;li&gt;German (de)&lt;/li&gt;&lt;li&gt;English (en)&lt;/li&gt;&lt;li&gt;Spanish (es)&lt;/li&gt;&lt;li&gt;French (fr)&lt;/li&gt;&lt;/ul&gt;&lt;/div&gt;
   * @param body User credentials (required)
   * @return LoginResponse
   * @throws ApiException if fails to make API call
   */
  public LoginResponse login(LoginRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling login");
    }
    
    // create path and map variables
    String localVarPath = "/auth/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LoginResponse> localVarReturnType = new GenericType<LoginResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Request password reset
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt;Request an email with a request token for a certain user to reset his/her password.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Registered user account.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Provided user receives email with reset token.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Currently supported languages (with ISO 639-1 code):&lt;/strong&gt;&lt;br&gt;&lt;/p&gt;&lt;ul&gt;&lt;li&gt;German (de)&lt;/li&gt;&lt;li&gt;English (en)&lt;/li&gt;&lt;li&gt;Spanish (es)&lt;/li&gt;&lt;li&gt;French (fr)&lt;/li&gt;&lt;/ul&gt;&lt;/div&gt;
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void requestPasswordReset(ResetPasswordRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling requestPasswordReset");
    }
    
    // create path and map variables
    String localVarPath = "/auth/reset_password";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Reset password
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt;Resets a user&#39;s password to a new value.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User received a password reset token.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Newly transmitted password is set.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param token Password reset token (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void resetPassword(String token, ResetPasswordWithTokenRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling resetPassword");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling resetPassword");
    }
    
    // create path and map variables
    String localVarPath = "/auth/reset_password/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get information for password reset
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt;Request all information for a password change dialogue (e.g. real name of user).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User received a password reset token.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param token Password reset token (required)
   * @return ResetPasswordTokenValidateResponse
   * @throws ApiException if fails to make API call
   */
  public ResetPasswordTokenValidateResponse validateResetPasswordToken(String token) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling validateResetPasswordToken");
    }
    
    // create path and map variables
    String localVarPath = "/auth/reset_password/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ResetPasswordTokenValidateResponse> localVarReturnType = new GenericType<ResetPasswordTokenValidateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
