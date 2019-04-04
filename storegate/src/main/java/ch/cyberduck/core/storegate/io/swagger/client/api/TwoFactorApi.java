package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.PasswordProtected;
import ch.cyberduck.core.storegate.io.swagger.client.model.TwoFactorAuthentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-04-02T17:31:35.366+02:00")
public class TwoFactorApi {
  private ApiClient apiClient;

  public TwoFactorApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TwoFactorApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * 
   * 
   * @param id The transaction id (required)
   * @throws ApiException if fails to make API call
   */
  public void twoFactorCommit(String id) throws ApiException {

    twoFactorCommitWithHttpInfo(id);
  }

  /**
   * 
   * 
   * @param id The transaction id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> twoFactorCommitWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling twoFactorCommit");
    }
    
    // create path and map variables
    String localVarPath = "/v4/twofactor/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param id The transaction id (required)
   * @throws ApiException if fails to make API call
   */
  public void twoFactorDelete(String id) throws ApiException {

    twoFactorDeleteWithHttpInfo(id);
  }

  /**
   * 
   * 
   * @param id The transaction id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> twoFactorDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling twoFactorDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4/twofactor/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param password  (required)
   * @throws ApiException if fails to make API call
   */
  public void twoFactorDisable(PasswordProtected password) throws ApiException {

    twoFactorDisableWithHttpInfo(password);
  }

  /**
   * 
   * 
   * @param password  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> twoFactorDisableWithHttpInfo(PasswordProtected password) throws ApiException {
    Object localVarPostBody = password;
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling twoFactorDisable");
    }
    
    // create path and map variables
    String localVarPath = "/v4/twofactor/disable";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param password  (required)
   * @return TwoFactorAuthentication
   * @throws ApiException if fails to make API call
   */
  public TwoFactorAuthentication twoFactorPost(String password) throws ApiException {
    return twoFactorPostWithHttpInfo(password).getData();
      }

  /**
   * 
   * 
   * @param password  (required)
   * @return ApiResponse&lt;TwoFactorAuthentication&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<TwoFactorAuthentication> twoFactorPostWithHttpInfo(String password) throws ApiException {
    Object localVarPostBody = password;
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling twoFactorPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4/twofactor";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<TwoFactorAuthentication> localVarReturnType = new GenericType<TwoFactorAuthentication>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * 
   * @param id The transaction id (required)
   * @param code the code (required)
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean twoFactorVerify(String id, String code) throws ApiException {
    return twoFactorVerifyWithHttpInfo(id, code).getData();
      }

  /**
   * 
   * 
   * @param id The transaction id (required)
   * @param code the code (required)
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> twoFactorVerifyWithHttpInfo(String id, String code) throws ApiException {
    Object localVarPostBody = code;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling twoFactorVerify");
    }
    
    // verify the required parameter 'code' is set
    if (code == null) {
      throw new ApiException(400, "Missing the required parameter 'code' when calling twoFactorVerify");
    }
    
    // create path and map variables
    String localVarPath = "/v4/twofactor/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
