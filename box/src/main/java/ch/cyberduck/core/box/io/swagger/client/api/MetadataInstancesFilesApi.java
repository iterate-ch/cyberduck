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

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class MetadataInstancesFilesApi {
  private ApiClient apiClient;

  public MetadataInstancesFilesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MetadataInstancesFilesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove metadata instance from file
   * Deletes a piece of file metadata.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteFilesIdMetadataIdId(String fileId, String scope, String templateKey) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling deleteFilesIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling deleteFilesIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling deleteFilesIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()))
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
   * List metadata instances on file
   * Retrieves all metadata for a given file.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @return Metadatas
   * @throws ApiException if fails to make API call
   */
  public Metadatas getFilesIdMetadata(String fileId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFilesIdMetadata");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/metadata"
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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Metadatas> localVarReturnType = new GenericType<Metadatas>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get metadata instance on file
   * Retrieves the instance of a metadata template that has been applied to a file.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @return Metadata
   * @throws ApiException if fails to make API call
   */
  public Metadata getFilesIdMetadataIdId(String fileId, String scope, String templateKey) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFilesIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling getFilesIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling getFilesIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()))
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
   * Create metadata instance on file
   * Applies an instance of a metadata template to a file.  In most cases only values that are present in the metadata template will be accepted, except for the &#x60;global.properties&#x60; template which accepts any key-value pair.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @param body  (optional)
   * @return Metadata
   * @throws ApiException if fails to make API call
   */
  public Metadata postFilesIdMetadataIdId(String fileId, String scope, String templateKey, Map<String, String> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling postFilesIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling postFilesIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling postFilesIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()))
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
   * Update metadata instance on file
   * Updates a piece of metadata on a file.  The metadata instance can only be updated if the template has already been applied to the file before. When editing metadata, only values that match the metadata template schema will be accepted.  The update is applied atomically. If any errors occur during the application of the operations, the metadata instance will not be changed.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @param body  (optional)
   * @return Metadata
   * @throws ApiException if fails to make API call
   */
  public Metadata putFilesIdMetadataIdId(String fileId, String scope, String templateKey, List<AMetadataInstanceUpdateOperation> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling putFilesIdMetadataIdId");
    }
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling putFilesIdMetadataIdId");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling putFilesIdMetadataIdId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/metadata/{scope}/{template_key}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()))
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
