package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.AccessToken;
import ch.cyberduck.core.box.io.swagger.client.model.OAuth2Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class AuthorizationApi {
  private ApiClient apiClient;

  public AuthorizationApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AuthorizationApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Authorize user
   * Authorize a user by sending them through the [Box](https://box.com) website and request their permission to act on their behalf.  This is the first step when authenticating a user using OAuth 2.0. To request a user&#x27;s authorization to use the Box APIs on their behalf you will need to send a user to the URL with this format.
   * @param responseType The type of response we&#x27;d like to receive. (required)
   * @param clientId The Client ID of the application that is requesting to authenticate the user. To get the Client ID for your application, log in to your Box developer console and click the **Edit Application** link for the application you&#x27;re working with. In the OAuth 2.0 Parameters section of the configuration page, find the item labelled &#x60;client_id&#x60;. The text of that item is your application&#x27;s Client ID. (required)
   * @param redirectUri The URL to which Box redirects the browser after the user has granted or denied the application permission. This URL must match the redirect URL in the configuration of your application. It must be a valid HTTPS URL and it needs to be able to handle the redirection to complete the next step in the OAuth 2.0 flow. (optional)
   * @param state A custom string of your choice. Box will pass the same string to the redirect URL when authentication is complete. This parameter can be used to identify a user on redirect, as well as protect against hijacked sessions and other exploits. (optional)
   * @param scope A comma-separated list of application scopes you&#x27;d like to authenticate the user for. This defaults to all the scopes configured for the application in its configuration page. (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String getAuthorize(String responseType, String clientId, String redirectUri, String state, String scope) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'responseType' is set
    if (responseType == null) {
      throw new ApiException(400, "Missing the required parameter 'responseType' when calling getAuthorize");
    }
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling getAuthorize");
    }
    // create path and map variables
    String localVarPath = "/authorize";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "response_type", responseType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "client_id", clientId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "redirect_uri", redirectUri));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "state", state));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "scope", scope));



    final String[] localVarAccepts = {
      "text/html"
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
   * Revoke access token
   * Revoke an active Access Token, effectively logging a user out that has been previously authenticated.
   * @param clientId  (optional)
   * @param clientSecret  (optional)
   * @param token  (optional)
   * @throws ApiException if fails to make API call
   */
  public void postOauth2Revoke(String clientId, String clientSecret, String token) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/oauth2/revoke";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    if (clientId != null)
      localVarFormParams.put("client_id", clientId);
    if (clientSecret != null)
      localVarFormParams.put("client_secret", clientSecret);
    if (token != null)
      localVarFormParams.put("token", token);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request access token
   * Request an Access Token using either a client-side obtained OAuth 2.0 authorization code or a server-side JWT assertion.  An Access Token is a string that enables Box to verify that a request belongs to an authorized session. In the normal order of operations you will begin by requesting authentication from the [authorize](#get-authorize) endpoint and Box will send you an authorization code.  You will then send this code to this endpoint to exchange it for an Access Token. The returned Access Token can then be used to to make Box API calls.
   * @param grantType  (optional)
   * @param clientId  (optional)
   * @param clientSecret  (optional)
   * @param code  (optional)
   * @param refreshToken  (optional)
   * @param assertion  (optional)
   * @param subjectToken  (optional)
   * @param subjectTokenType  (optional)
   * @param actorToken  (optional)
   * @param actorTokenType  (optional)
   * @param scope  (optional)
   * @param resource  (optional)
   * @param boxSubjectType  (optional)
   * @param boxSubjectId  (optional)
   * @return AccessToken
   * @throws ApiException if fails to make API call
   */
  public AccessToken postOauth2Token(String grantType, String clientId, String clientSecret, String code, String refreshToken, String assertion, String subjectToken, String subjectTokenType, String actorToken, String actorTokenType, String scope, String resource, String boxSubjectType, String boxSubjectId) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/oauth2/token";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    if (grantType != null)
      localVarFormParams.put("grant_type", grantType);
    if (clientId != null)
      localVarFormParams.put("client_id", clientId);
    if (clientSecret != null)
      localVarFormParams.put("client_secret", clientSecret);
    if (code != null)
      localVarFormParams.put("code", code);
    if (refreshToken != null)
      localVarFormParams.put("refresh_token", refreshToken);
    if (assertion != null)
      localVarFormParams.put("assertion", assertion);
    if (subjectToken != null)
      localVarFormParams.put("subject_token", subjectToken);
    if (subjectTokenType != null)
      localVarFormParams.put("subject_token_type", subjectTokenType);
    if (actorToken != null)
      localVarFormParams.put("actor_token", actorToken);
    if (actorTokenType != null)
      localVarFormParams.put("actor_token_type", actorTokenType);
    if (scope != null)
      localVarFormParams.put("scope", scope);
    if (resource != null)
      localVarFormParams.put("resource", resource);
    if (boxSubjectType != null)
      localVarFormParams.put("box_subject_type", boxSubjectType);
    if (boxSubjectId != null)
      localVarFormParams.put("box_subject_id", boxSubjectId);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AccessToken> localVarReturnType = new GenericType<AccessToken>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Refresh access token
   * Refresh an Access Token using its client ID, secret, and refresh token.
   * @param grantType  (optional)
   * @param clientId  (optional)
   * @param clientSecret  (optional)
   * @param refreshToken  (optional)
   * @return AccessToken
   * @throws ApiException if fails to make API call
   */
  public AccessToken postOauth2TokenRefresh(String grantType, String clientId, String clientSecret, String refreshToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/oauth2/token#refresh";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    if (grantType != null)
      localVarFormParams.put("grant_type", grantType);
    if (clientId != null)
      localVarFormParams.put("client_id", clientId);
    if (clientSecret != null)
      localVarFormParams.put("client_secret", clientSecret);
    if (refreshToken != null)
      localVarFormParams.put("refresh_token", refreshToken);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AccessToken> localVarReturnType = new GenericType<AccessToken>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
