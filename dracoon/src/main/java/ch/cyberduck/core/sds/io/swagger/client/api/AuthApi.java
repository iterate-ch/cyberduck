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
import ch.cyberduck.core.sds.io.swagger.client.model.AuthInitResources;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.OpenIdAuthResources;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordTokenValidateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordWithTokenRequest;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
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
   * ### Functional Description:   This is the second step of the OpenID Connect authentication.   The user hands over the authorization code and is logged in.  ### Precondition: Existing user with activated OpenID Connect authentication that is not locked.  ### Effects: User is logged in.  ### &amp;#9432; Further Information: See [http://openid.net/developers/specs](http://openid.net/developers/specs) for further information.
   * @param code Authorization code (required)
   * @param state Authentication state (required)
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
    String localVarPath = "/v4/auth/openid/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "code", code));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "state", state));

    
    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * Get authentication resources
   * ### Functional Description:   Provides information about authentication options.  ### Precondition:  None.  ### Effects:  None.  ### &amp;#9432; Further Information: The server identifies the relevant customer by the passed HTTP header &#x60;Origin&#x60;.   Use this call to customize the log-on form.    ### Important:  The default language and authentication method are always provided as topmost entry.  ### Currently supported languages (with ISO 639-1 code): * German (de) * English (en) * Spanish (es) * French (fr)
   * @return AuthInitResources
   * @throws ApiException if fails to make API call
   */
  public AuthInitResources getAuthInitResources() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/auth/resources";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * Get OpenID Connect authentication resources
   * ### Functional Description:   Provides information about OpenID Connect authentication options.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @return OpenIdAuthResources
   * @throws ApiException if fails to make API call
   */
  public OpenIdAuthResources getOpenIdAuthResources() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/auth/openid/resources";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * ## CAUTION: This API does not work with Swagger! Swagger can not handle the redirect to the OpenID Connect identity provider.  ### Functional Description: This is the first step of the OpenID Connect authentication.   The user is send to the OpenID Connect identity provider to authenticate himself and retrieve an Authorization Code.  ### Precondition: None.  ### Effects: User is redirected to OpenID Connect identity provider to authenticate himself.  ### &amp;#9432; Further Information: See [http://openid.net/developers/specs](http://openid.net/developers/specs) for further information.
   * @param issuer Issuer identifier of the OpenID Connect identity provider (required)
   * @param redirectUri Redirect URI to complete the OpenID Connect authentication (required)
   * @param language Language ID or ISO 639-1 code (**DEPRECATED**: will be removed) (optional)
   * @param test Flag to test the authentication parameters. If the request is valid, the API will respond with &#x60;204 No Content&#x60;. (optional)
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
      "application/json;charset=UTF-8"
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
   * ### Functional Description: Authenticates user and provides an authentication token (&#x60;X-Sds-Auth-Token&#x60;) that is required for most operations.  ### Precondition: Existing user that is not locked.  ### Effects: User is logged in.  ### &amp;#9432; Further Information: The provided token is valid for **2 hours**, every usage resets this period to 2 full hours again.   Logging off invalidates the token.    Setting the language parameter has no effect.  ### Available authentication methods  | Authentication Method:&lt;br&gt;**&#x60;authType&#x60;** | Description | | :--- | :--- | | **&#x60;sql&#x60;** | Log in with credentials stored in the database | | **&#x60;active_directory&#x60;** | Log in with Active Directory credentials | | **&#x60;radius&#x60;** | Log in with RADIUS username, PIN and token password.&lt;br&gt;Token (request parameter) may be set, otherwise this parameter is ignored. If &#x60;token&#x60; is set, &#x60;password&#x60; is optional. | | **&#x60;openid&#x60;** | Please use &#x60;POST /auth/openid/login&#x60; API to login with OpenID Connect identity |  ### Deprecated: Currently supported languages (with ISO 639-1 code): * German (de) * English (en) * Spanish (es) * French (fr)
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
    String localVarPath = "/v4/auth/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LoginResponse> localVarReturnType = new GenericType<LoginResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Ping
   * ### Functional Description: Test connection to DRACOON Server.  ### Precondition: None.  ### Effects: &#x60;200 OK&#x60; with current date string is returned if successful.  ### &amp;#9432; Further Information: None.
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
   * Request password reset
   * ### Functional Description:   Request an email with a request token for a certain user to reset his / her password.  ### Precondition: Registered user account.  ### Effects: Provided user receives email with reset token.  ### &amp;#9432; Further Information: None.  ### Currently supported languages (with ISO 639-1 code): * German (de) * English (en) * Spanish (es) * French (fr)
   * @param body body (required)
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Reset password
   * ### Functional Description:   Resets a user&#39;s password to a new value.  ### Precondition: User received a password reset token.  ### Effects: Newly transmitted password is set.  ### &amp;#9432; Further Information: None.
   * @param token Password reset token (required)
   * @param body body (required)
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
    String localVarPath = "/v4/auth/reset_password/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get information for password reset
   * ### Functional Description:   Request all information for a password change dialogue e.g. real name of user.  ### Precondition: User received a password reset token.  ### Effects: None.  ### &amp;#9432; Further Information: None.
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
      "application/json;charset=UTF-8"
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
