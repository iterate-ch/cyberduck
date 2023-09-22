package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.ExportJobResponse;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileExportRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileShareExportRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaFolderExportRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaShareExportRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaShareItemExportRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class ExportApi {
  private ApiClient apiClient;

  public ExportApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ExportApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Cancel export job
   * 
   * @param id  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String exportCancelExportJob(String id) throws ApiException {
    return exportCancelExportJobWithHttpInfo(id).getData();
      }

  /**
   * Cancel export job
   * 
   * @param id  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> exportCancelExportJobWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling exportCancelExportJob");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/{id}"
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download archives created by export job
   * 
   * @param id  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String exportDownload(String id) throws ApiException {
    return exportDownloadWithHttpInfo(id).getData();
      }

  /**
   * Download archives created by export job
   * 
   * @param id  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> exportDownloadWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling exportDownload");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/download/{id}"
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates an export job for a media folder
   * 
   * @param request  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportExportAlbum(MediaFolderExportRequest request) throws ApiException {
    return exportExportAlbumWithHttpInfo(request).getData();
      }

  /**
   * Creates an export job for a media folder
   * 
   * @param request  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportExportAlbumWithHttpInfo(MediaFolderExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling exportExportAlbum");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/albums";

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

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates an export job for a list of files
   * 
   * @param request  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportExportFiles(FileExportRequest request) throws ApiException {
    return exportExportFilesWithHttpInfo(request).getData();
      }

  /**
   * Creates an export job for a list of files
   * 
   * @param request  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportExportFilesWithHttpInfo(FileExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling exportExportFiles");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/files";

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

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create an export job for a file share
   * 
   * @param request  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportExportShare(FileShareExportRequest request) throws ApiException {
    return exportExportShareWithHttpInfo(request).getData();
      }

  /**
   * Create an export job for a file share
   * 
   * @param request  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportExportShareWithHttpInfo(FileShareExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling exportExportShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/share";

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

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates an export job for a list of shared files
   * 
   * @param shareid The share id (required)
   * @param request  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportExportShareFiles(String shareid, FileExportRequest request) throws ApiException {
    return exportExportShareFilesWithHttpInfo(shareid, request).getData();
      }

  /**
   * Creates an export job for a list of shared files
   * 
   * @param shareid The share id (required)
   * @param request  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportExportShareFilesWithHttpInfo(String shareid, FileExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling exportExportShareFiles");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling exportExportShareFiles");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/shares/{shareid}/files"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates an export job for a shared album
   * 
   * @param request  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportExportSharedAlbum(MediaShareExportRequest request) throws ApiException {
    return exportExportSharedAlbumWithHttpInfo(request).getData();
      }

  /**
   * Creates an export job for a shared album
   * 
   * @param request  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportExportSharedAlbumWithHttpInfo(MediaShareExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling exportExportSharedAlbum");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/share/album";

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

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates an export for a list files in a shared album
   * 
   * @param shareid The share id (required)
   * @param request  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportExportSharedAlbumFiles(String shareid, MediaShareItemExportRequest request) throws ApiException {
    return exportExportSharedAlbumFilesWithHttpInfo(shareid, request).getData();
      }

  /**
   * Creates an export for a list files in a shared album
   * 
   * @param shareid The share id (required)
   * @param request  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportExportSharedAlbumFilesWithHttpInfo(String shareid, MediaShareItemExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling exportExportSharedAlbumFiles");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling exportExportSharedAlbumFiles");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/shares/{shareid}/album/files"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Returns export job, poll this to see progess och the export job
   * 
   * @param id  (required)
   * @return ExportJobResponse
   * @throws ApiException if fails to make API call
   */
  public ExportJobResponse exportGetExportJob(String id) throws ApiException {
    return exportGetExportJobWithHttpInfo(id).getData();
      }

  /**
   * Returns export job, poll this to see progess och the export job
   * 
   * @param id  (required)
   * @return ApiResponse&lt;ExportJobResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExportJobResponse> exportGetExportJobWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling exportGetExportJob");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/export/{id}"
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ExportJobResponse> localVarReturnType = new GenericType<ExportJobResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
