package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.SessionEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class SessionsApi {
  private ApiClient apiClient;

  public SessionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SessionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete user session (log out)
   * Delete user session (log out)
   * @throws ApiException if fails to make API call
   */
  public void deleteSessions() throws ApiException {

    deleteSessionsWithHttpInfo();
  }

  /**
   * Delete user session (log out)
   * Delete user session (log out)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteSessionsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/sessions";

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


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Create user session (log in)
   * Create user session (log in)
   * @param username Username to sign in as (optional)
   * @param password Password for sign in (optional)
   * @param otp If this user has a 2FA device, provide its OTP or code here. (optional)
   * @param partialSessionId Identifier for a partially-completed login (optional)
   * @return SessionEntity
   * @throws ApiException if fails to make API call
   */
  public SessionEntity postSessions(String username, String password, String otp, String partialSessionId) throws ApiException {
    return postSessionsWithHttpInfo(username, password, otp, partialSessionId).getData();
      }

  /**
   * Create user session (log in)
   * Create user session (log in)
   * @param username Username to sign in as (optional)
   * @param password Password for sign in (optional)
   * @param otp If this user has a 2FA device, provide its OTP or code here. (optional)
   * @param partialSessionId Identifier for a partially-completed login (optional)
   * @return ApiResponse&lt;SessionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SessionEntity> postSessionsWithHttpInfo(String username, String password, String otp, String partialSessionId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/sessions";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (username != null)
      localVarFormParams.put("username", username);
if (password != null)
      localVarFormParams.put("password", password);
if (otp != null)
      localVarFormParams.put("otp", otp);
if (partialSessionId != null)
      localVarFormParams.put("partial_session_id", partialSessionId);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SessionEntity> localVarReturnType = new GenericType<SessionEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
