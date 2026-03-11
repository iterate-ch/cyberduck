package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.DatevAuthorizationCompleteRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevAuthorizationStartRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevAuthorizationUrl;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevMstAddableTaxYears;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevMstAuthorizationStartRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevMstCreateTaxYearRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevSyncConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevSyncConfigList;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevSyncFile;
import ch.cyberduck.core.sds.io.swagger.client.model.DatevSyncFileList;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatevApi {
  private ApiClient apiClient;
  private Map<String, String> headers;

  public DatevApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DatevApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public void setHeadersOverrides(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * Complete Datev DUO authorization
   * ### Description:   Complete the OAuth authorization for Datev DUO to configure Datev DUO sync for a room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition:  A OAuth authorization for Datev DUO is completed and a Datev sync configuration is created/updated.  ### Further Information:   None. 
   * @param body  (required)
   * @return DatevSyncConfig
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfig completeDatevDuoAuthorization(DatevAuthorizationCompleteRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling completeDatevDuoAuthorization");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/authorization/complete";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfig> localVarReturnType = new GenericType<DatevSyncConfig>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Complete Datev MST authorization
   * ### Description:  Complete the OAuth authorization for Datev MST to configure Datev MST sync for a room.  ### Precondition:  User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt; &amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition:  A OAuth authorization for Datev MST is completed and a Datev sync configuration is created/updated.  ### Further Information:  None. 
   * @param body  (required)
   * @return DatevSyncConfig
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfig completeDatevMstAuthorization(DatevAuthorizationCompleteRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling completeDatevMstAuthorization");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/authorization/complete";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfig> localVarReturnType = new GenericType<DatevSyncConfig>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create new Datev MST tax year
   * ### Description:  Create a Datev MST tax year.  ### Precondition:  Authenticated user.  ### Postcondition:  Datev MST tax year is created.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                 | |:----------------------------|:-----------|:----------------------------| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;   | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |            | System Error                | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;   | Datev communication failed  |  &lt;/details&gt;
   * @param body  (required)
   * @param configId Configuration ID (required)
   * @throws ApiException if fails to make API call
   */
  public void createMstTaxYear(DatevMstCreateTaxYearRequest body, Long configId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createMstTaxYear");
    }
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling createMstTaxYear");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}/taxyears"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Execute Datev DUO sync
   * ### Description:  Execute the synchronization for an existing Datev DUO sync configuration.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Datev DUO synchronization is executed.  ### Further Information: None.
   * @param configId Configuration ID (required)
   * @throws ApiException if fails to make API call
   */
  public void executeDatevDuoSync(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling executeDatevDuoSync");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/{config_id}/sync"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Execute Datev MST sync
   * ### Description:  Execute the synchronization for an existing Datev MST sync configuration.  ### Precondition:  User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt; &amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition:  Datev MST synchronization is executed.  ### Further Information:  None.
   * @param configId Configuration ID (required)
   * @throws ApiException if fails to make API call
   */
  public void executeDatevMstSync(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling executeDatevMstSync");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}/sync"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove Datev DUO sync configuration
   * ### Description: Remove an existing Datev DUO sync configuration.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Datev DUO sync configurations is removed.  ### Further Information: None.
   * @param configId Configuration ID (required)
   * @throws ApiException if fails to make API call
   */
  public void removeDatevDuoSyncConfig(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling removeDatevDuoSyncConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/{config_id}"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove Datev MST sync configuration
   * ### Description:  Remove an existing Datev MST sync configuration.  ### Precondition:  User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt; &amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition:  Datev MST sync configurations is removed.  ### Further Information:  None.
   * @param configId Configuration ID (required)
   * @throws ApiException if fails to make API call
   */
  public void removeDatevMstSyncConfig(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling removeDatevMstSyncConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request all addable Datev MST tax years
   * ### Description:  Retrieve all addable Datev MST tax years.  ### Precondition:  Authenticated user.  ### Postcondition:  Datev MST addable tax years are returned.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                 | |:----------------------------|:-----------|:----------------------------| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;   | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |            | System Error                | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;   | Datev communication failed  |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @return DatevMstAddableTaxYears
   * @throws ApiException if fails to make API call
   */
  public DatevMstAddableTaxYears requestAddableMstTaxYears(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestAddableMstTaxYears");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}/taxyears"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevMstAddableTaxYears> localVarReturnType = new GenericType<DatevMstAddableTaxYears>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Datev DUO sync configuration
   * ### Description:   Retrieve a Datev DUO sync configuration.  ### Precondition: Authenticated user.  ### Postcondition: Datev DUO sync configurations is returned.  ### Further Information: None.  ### Possible errors: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code   | Description | |:---|:---|:---| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;     | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |              | System Error | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;     | Datev communication failed |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @return DatevSyncConfig
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfig requestDatevDuoSyncConfig(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestDatevDuoSyncConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/{config_id}"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfig> localVarReturnType = new GenericType<DatevSyncConfig>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of Datev DUO sync configurations
   * ### Description:   Retrieve a list of Datev DUO sync configurations.  ### Precondition: Authenticated user.  ### Postcondition: List of Datev DUO sync configurations is returned.  ### Further Information: None.  ### Possible errors: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code   | Description | |:---|:---|:---| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;     | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |              | System Error | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;     | Datev communication failed |  &lt;/details&gt;
   * @param roomId Room ID (optional)
   * @return DatevSyncConfigList
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfigList requestDatevDuoSyncConfigs(Long roomId) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/datev/duo";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_id", roomId));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfigList> localVarReturnType = new GenericType<DatevSyncConfigList>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request information about a Datev DUO sync file
   * ### Description:   Retrieve a Datev DUO sync file.  ### Precondition: Authenticated user.  ### Postcondition: Datev DUO sync file is returned.  ### Further Information: None.  ### Possible errors: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                  | Error Code | Description                                 | |:---|:-----------|:--------------------------------------------| | &#x60;400 Bad Request&#x60;            | &#x60;-90711&#x60;   | File extension not allowed for Datev upload | | &#x60;400 Bad Request&#x60;            | &#x60;-90712&#x60;   | File too large for Datev upload             | | &#x60;400 Bad Request&#x60;            | &#x60;-90713&#x60;   | Document type unknown by Datev              | | &#x60;400 Bad Request&#x60;            | &#x60;-90714&#x60;   | Document not parseable by Datev             | | &#x60;500 Internal Server Error&#x60;  |            | System Error                                |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @param fileId File ID (required)
   * @return DatevSyncFile
   * @throws ApiException if fails to make API call
   */
  public DatevSyncFile requestDatevDuoSyncFile(Long configId, Long fileId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestDatevDuoSyncFile");
    }
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling requestDatevDuoSyncFile");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/{config_id}/files/{file_id}"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()))
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncFile> localVarReturnType = new GenericType<DatevSyncFile>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request information about Datev DUO sync files
   * ### Description:   Retrieve list of Datev DUO sync files.  ### Precondition: Authenticated user.  ### Postcondition: List of Datev DUO sync files is returned.  ### Further Information: None.  ### Possible errors: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                  | Error Code   | Description                                 | |:---|:---|:---| | &#x60;400 Bad Request&#x60;            | &#x60;-90711&#x60;     | File extension not allowed for Datev upload | | &#x60;400 Bad Request&#x60;            | &#x60;-90712&#x60;     | File too large for Datev upload             | | &#x60;500 Internal Server Error&#x60;  |              | System Error                                |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @return DatevSyncFileList
   * @throws ApiException if fails to make API call
   */
  public DatevSyncFileList requestDatevDuoSyncFiles(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestDatevDuoSyncFiles");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/{config_id}/files"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncFileList> localVarReturnType = new GenericType<DatevSyncFileList>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Datev MST sync configuration
   * ### Description:  Retrieve a Datev MST sync configuration.  ### Precondition:  Authenticated user.  ### Postcondition:  Datev MST sync configurations is returned.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                 | |:----------------------------|:-----------|:----------------------------| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;   | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |            | System Error                | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;   | Datev communication failed  |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @return DatevSyncConfig
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfig requestDatevMstSyncConfig(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestDatevMstSyncConfig");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfig> localVarReturnType = new GenericType<DatevSyncConfig>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of Datev MST sync configurations
   * ### Description:  Retrieve a list of Datev MST sync configurations.  ### Precondition:  Authenticated user.  ### Postcondition:  List of Datev MST sync configurations is returned.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                 | |:----------------------------|:-----------|:----------------------------| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;   | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |            | System Error                | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;   | Datev communication failed  |  &lt;/details&gt;
   * @param roomId Room ID (optional)
   * @return DatevSyncConfigList
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfigList requestDatevMstSyncConfigs(Long roomId) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/datev/mst";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_id", roomId));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfigList> localVarReturnType = new GenericType<DatevSyncConfigList>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request information about a Datev MST sync file
   * ### Description:  Retrieve a Datev MST sync file.  ### Precondition:  Authenticated user.  ### Postcondition:  Datev MST sync file is returned.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                                 | |:----------------------------|:-----------|:--------------------------------------------| | &#x60;400 Bad Request&#x60;           | &#x60;-90711&#x60;   | File extension not allowed for Datev upload | | &#x60;400 Bad Request&#x60;           | &#x60;-90712&#x60;   | File too large for Datev upload             | | &#x60;400 Bad Request&#x60;           | &#x60;-90713&#x60;   | Document type unknown by Datev              | | &#x60;400 Bad Request&#x60;           | &#x60;-90714&#x60;   | Document not parseable by Datev             | | &#x60;500 Internal Server Error&#x60; |            | System Error                                |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @param fileId File ID (required)
   * @return DatevSyncFile
   * @throws ApiException if fails to make API call
   */
  public DatevSyncFile requestDatevMstSyncFile(Long configId, Long fileId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestDatevMstSyncFile");
    }
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling requestDatevMstSyncFile");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}/files/{file_id}"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()))
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncFile> localVarReturnType = new GenericType<DatevSyncFile>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request information about Datev MST sync files
   * ### Description:  Retrieve list of Datev MST sync files.  ### Precondition:  Authenticated user.  ### Postcondition:  List of Datev MST sync files is returned.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                                 | |:----------------------------|:-----------|:--------------------------------------------| | &#x60;400 Bad Request&#x60;           | &#x60;-90711&#x60;   | File extension not allowed for Datev upload | | &#x60;400 Bad Request&#x60;           | &#x60;-90712&#x60;   | File too large for Datev upload             | | &#x60;500 Internal Server Error&#x60; |            | System Error                                |  &lt;/details&gt;
   * @param configId Configuration ID (required)
   * @return DatevSyncFileList
   * @throws ApiException if fails to make API call
   */
  public DatevSyncFileList requestDatevMstSyncFiles(Long configId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'configId' is set
    if (configId == null) {
      throw new ApiException(400, "Missing the required parameter 'configId' when calling requestDatevMstSyncFiles");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/{config_id}/files"
      .replaceAll("\\{" + "config_id" + "\\}", apiClient.escapeString(configId.toString()));

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncFileList> localVarReturnType = new GenericType<DatevSyncFileList>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Datev sync configuration for room
   * ### Description:  Retrieve a Datev sync configuration for a given room.  ### Precondition:  Authenticated user.  ### Postcondition:  Datev DUO or MST sync configuration is returned.  ### Further Information:  None.  ### Possible errors:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status                 | Error Code | Description                 | |:----------------------------|:-----------|:----------------------------| | &#x60;401 Forbidden&#x60;             | &#x60;-90706&#x60;   | Datev authorization invalid | | &#x60;500 Internal Server Error&#x60; |            | System Error                | | &#x60;502 Bad Gateway&#x60;           | &#x60;-90701&#x60;   | Datev communication failed  |  &lt;/details&gt;
   * @param roomId Room ID (required)
   * @return DatevSyncConfig
   * @throws ApiException if fails to make API call
   */
  public DatevSyncConfig requestDatevSyncConfigForRoom(Long roomId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestDatevSyncConfigForRoom");
    }
    // create path and map variables
    String localVarPath = "/v4/datev";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_id", roomId));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevSyncConfig> localVarReturnType = new GenericType<DatevSyncConfig>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Start Datev DUO authorization
   * ### Description: Start the OAuth authorization for Datev DUO to configure Datev DUO sync for a room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: A OAuth authorization for Datev DUO is started and the related authorization URL is returned.  ### Further Information:   None.
   * @param body  (required)
   * @return DatevAuthorizationUrl
   * @throws ApiException if fails to make API call
   */
  public DatevAuthorizationUrl startDatevDuoAuthorization(DatevAuthorizationStartRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling startDatevDuoAuthorization");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/duo/authorization/start";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevAuthorizationUrl> localVarReturnType = new GenericType<DatevAuthorizationUrl>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Start Datev MST authorization
   * ### Description:  Start the OAuth authorization for Datev MST to configure Datev MST sync for a room.  ### Precondition:  User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt; &amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition:  A OAuth authorization for Datev MST is started and the related authorization URL is returned.  ### Further Information:  None.
   * @param body  (required)
   * @return DatevAuthorizationUrl
   * @throws ApiException if fails to make API call
   */
  public DatevAuthorizationUrl startDatevMstAuthorization(DatevMstAuthorizationStartRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling startDatevMstAuthorization");
    }
    // create path and map variables
    String localVarPath = "/v4/datev/mst/authorization/start";

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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DatevAuthorizationUrl> localVarReturnType = new GenericType<DatevAuthorizationUrl>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
