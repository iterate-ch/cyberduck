package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.InlineResponse400;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.RadiusChallengeResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.RecoverUserNameRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordTokenValidateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordWithTokenRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.14.0&lt;/h3&gt;  ### Description:   This is the second step of the OpenID Connect authentication.   The user hands over the authorization code and is logged in.  ### Precondition: Existing user with activated OpenID Connect authentication that is **NOT** locked.  ### Postcondition: User is logged in.  ### Further Information: None.
   * @param code Authorization code (required)
   * @param state Authentication state (required)
   * @param idToken Identity token (optional)
   * @return LoginResponse
   * @throws ApiException if fails to make API call
   * @deprecated
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Complete OpenID Connect authentication Documentation</a>
   */
  @Deprecated
  public LoginResponse completeOpenIdLogin(String code, String state, String idToken) throws ApiException {
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
    String localVarPath = "/v4/auth/openid/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "code", code));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "id_token", idToken));
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
   * Initiate OpenID Connect authentication
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.14.0&lt;/h3&gt;  ### Description: This is the first step of the OpenID Connect authentication.   The user is send to the OpenID Connect identity provider to authenticate himself and retrieve an authorization code.  ### Precondition: None.  ### Postcondition: User is redirected to OpenID Connect identity provider to authenticate himself.  ### Further Information: None.
   * @param issuer Issuer identifier of the OpenID Connect identity provider (required)
   * @param redirectUri Redirect URI to complete the OpenID Connect authentication (required)
   * @param language Language ID or ISO 639-1 code (required)
   * @param test Flag to test the authentication parameters.  If the request is valid, the API will respond with &#x60;204 No Content&#x60;. (required)
   * @throws ApiException if fails to make API call
   * @deprecated
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Initiate OpenID Connect authentication Documentation</a>
   */
  @Deprecated
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
    // verify the required parameter 'language' is set
    if (language == null) {
      throw new ApiException(400, "Missing the required parameter 'language' when calling initiateOpenIdLogin");
    }
    // verify the required parameter 'test' is set
    if (test == null) {
      throw new ApiException(400, "Missing the required parameter 'test' when calling initiateOpenIdLogin");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/openid/login";

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
   * Authenticate user (Login)
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.13.0&lt;/h3&gt;  ### Description: Authenticates user and provides an authentication token (&#x60;X-Sds-Auth-Token&#x60;) that is required for the most operations.  ### Precondition: Existing user that is **NOT** locked.  ### Postcondition: User is logged in.  ### Further Information: The provided token is valid for **two hours**, every usage resets this period to two full hours again.   Logging off invalidates the token.    ### Available authentication methods: &lt;details open style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Authentication Method (&#x60;authType&#x60;) | Description | | :--- | :--- | | &#x60;basic&#x60; | Log in with credentials stored in the database &lt;br&gt;Formerly known as &#x60;sql&#x60;.| | &#x60;active_directory&#x60; | Log in with Active Directory credentials | | &#x60;radius&#x60; | Log in with RADIUS username, PIN and token password.&lt;br&gt;Token (request parameter) may be set, otherwise this parameter is ignored. If token is set, password is optional. | | &#x60;openid&#x60; | Please use &#x60;POST /auth/openid/login&#x60; API to login with OpenID Connect identity |  &lt;/details&gt;
   * @param body  (required)
   * @return LoginResponse
   * @throws ApiException if fails to make API call
   * @deprecated
   * Remote Authentication Dial In User Service (RADIUS)
   * @see <a href="https://tools.ietf.org/html/rfc2865">Authenticate user (Login) Documentation</a>
   */
  @Deprecated
  public LoginResponse login(LoginRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling login");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LoginResponse> localVarReturnType = new GenericType<LoginResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Ping
   * ### Description: Test connection to DRACOON Core Service.  ### Precondition: None.  ### Postcondition: &#x60;200 OK&#x60; with current date string is returned if successful.  ### Further Information: None.
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String ping() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/auth/ping";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Recover username
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.13.0&lt;/h3&gt;  ### Description:   Request an email with the user names of all accounts connected to the email.  ### Precondition: Valid email address.  ### Postcondition: An email is sent to the provided address, with a list of account user names connected to it.  ### Further Information: None. 
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void recoverUserName(RecoverUserNameRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling recoverUserName");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/recover_username";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request password reset
   * ### Description:   Request an email with a password reset token for a certain user to reset password.  ### Precondition: Registered user account.  ### Postcondition: Provided user receives email with password reset token.  ### Further Information: None.
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
    String localVarPath = "/v4/auth/reset_password";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Reset password
   * ### Description:   Resets user&#x27;s password.  ### Precondition: User received a password reset token.  ### Postcondition: User&#x27;s password is reset to the provided password.  ### Further Information: Forbidden characters in passwords: [&#x60;&amp;&#x60;, &#x60;&#x27;&#x60;, &#x60;&lt;&#x60;, &#x60;&gt;&#x60;]
   * @param body  (required)
   * @param token Password reset token (required)
   * @throws ApiException if fails to make API call
   */
  public void resetPassword(ResetPasswordWithTokenRequest body, String token) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling resetPassword");
    }
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling resetPassword");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/reset_password/{token}"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Validate information for password reset
   * ### Description:   Request all information for a password change dialogue e.g. real name of user.  ### Precondition: User received a password reset token.  ### Postcondition: Context information is returned.  ### Further Information: None.
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
    String localVarPath = "/v4/auth/reset_password/{token}"
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
