package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.AMetadataInstanceUpdateOperation;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Metadata;
import ch.cyberduck.core.box.io.swagger.client.model.Metadatas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class MetadataInstancesFoldersApi {
  private ApiClient apiClient;

  public MetadataInstancesFoldersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MetadataInstancesFoldersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove metadata instance from folder
   * Deletes a piece of folder metadata.
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteFoldersIdMetadataIdId(String folderId, String scope, String templateKey) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling deleteFoldersIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling deleteFoldersIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling deleteFoldersIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()))
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List metadata instances on folder
   * Retrieves all metadata for a given folder. This can not be used on the root folder with ID &#x60;0&#x60;.
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @return Metadatas
   * @throws ApiException if fails to make API call
   */
  public Metadatas getFoldersIdMetadata(String folderId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling getFoldersIdMetadata");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}/metadata"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Metadatas> localVarReturnType = new GenericType<Metadatas>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get metadata instance on folder
   * Retrieves the instance of a metadata template that has been applied to a folder. This can not be used on the root folder with ID &#x60;0&#x60;.
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @return Metadata
   * @throws ApiException if fails to make API call
   */
  public Metadata getFoldersIdMetadataIdId(String folderId, String scope, String templateKey) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling getFoldersIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling getFoldersIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling getFoldersIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()))
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Metadata> localVarReturnType = new GenericType<Metadata>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create metadata instance on folder
   * Applies an instance of a metadata template to a folder.  In most cases only values that are present in the metadata template will be accepted, except for the &#x60;global.properties&#x60; template which accepts any key-value pair.  To display the metadata template in the Box web app the enterprise needs to be configured to enable **Cascading Folder Level Metadata** for the user in the admin console.
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @param body  (optional)
   * @return Metadata
   * @throws ApiException if fails to make API call
   */
  public Metadata postFoldersIdMetadataIdId(String folderId, String scope, String templateKey, Map<String, String> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling postFoldersIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling postFoldersIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling postFoldersIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()))
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Metadata> localVarReturnType = new GenericType<Metadata>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update metadata instance on folder
   * Updates a piece of metadata on a folder.  The metadata instance can only be updated if the template has already been applied to the folder before. When editing metadata, only values that match the metadata template schema will be accepted.  The update is applied atomically. If any errors occur during the application of the operations, the metadata instance will not be changed.
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @param body  (optional)
   * @return Metadata
   * @throws ApiException if fails to make API call
   */
  public Metadata putFoldersIdMetadataIdId(String folderId, String scope, String templateKey, List<AMetadataInstanceUpdateOperation> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling putFoldersIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling putFoldersIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling putFoldersIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()))
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json-patch+json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Metadata> localVarReturnType = new GenericType<Metadata>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
