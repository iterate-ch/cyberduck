package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body36;
import ch.cyberduck.core.box.io.swagger.client.model.Body37;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.TaskAssignment;
import ch.cyberduck.core.box.io.swagger.client.model.TaskAssignments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class TaskAssignmentsApi {
  private ApiClient apiClient;

  public TaskAssignmentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TaskAssignmentsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Unassign task
   * Deletes a specific task assignment.
   * @param taskAssignmentId The ID of the task assignment. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteTaskAssignmentsId(String taskAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'taskAssignmentId' is set
    if (taskAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'taskAssignmentId' when calling deleteTaskAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/task_assignments/{task_assignment_id}"
      .replaceAll("\\{" + "task_assignment_id" + "\\}", apiClient.escapeString(taskAssignmentId.toString()));

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
   * Get task assignment
   * Retrieves information about a task assignment.
   * @param taskAssignmentId The ID of the task assignment. (required)
   * @return TaskAssignment
   * @throws ApiException if fails to make API call
   */
  public TaskAssignment getTaskAssignmentsId(String taskAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'taskAssignmentId' is set
    if (taskAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'taskAssignmentId' when calling getTaskAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/task_assignments/{task_assignment_id}"
      .replaceAll("\\{" + "task_assignment_id" + "\\}", apiClient.escapeString(taskAssignmentId.toString()));

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

    GenericType<TaskAssignment> localVarReturnType = new GenericType<TaskAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List task assignments
   * Lists all of the assignments for a given task.
   * @param taskId The ID of the task. (required)
   * @return TaskAssignments
   * @throws ApiException if fails to make API call
   */
  public TaskAssignments getTasksIdAssignments(String taskId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'taskId' is set
    if (taskId == null) {
      throw new ApiException(400, "Missing the required parameter 'taskId' when calling getTasksIdAssignments");
    }
    // create path and map variables
    String localVarPath = "/tasks/{task_id}/assignments"
      .replaceAll("\\{" + "task_id" + "\\}", apiClient.escapeString(taskId.toString()));

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

    GenericType<TaskAssignments> localVarReturnType = new GenericType<TaskAssignments>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Assign task
   * Assigns a task to a user.  A task can be assigned to more than one user by creating multiple assignments.
   * @param body  (optional)
   * @return TaskAssignment
   * @throws ApiException if fails to make API call
   */
  public TaskAssignment postTaskAssignments(Body36 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/task_assignments";

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

    GenericType<TaskAssignment> localVarReturnType = new GenericType<TaskAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update task assignment
   * Updates a task assignment. This endpoint can be used to update the state of a task assigned to a user.
   * @param taskAssignmentId The ID of the task assignment. (required)
   * @param body  (optional)
   * @return TaskAssignment
   * @throws ApiException if fails to make API call
   */
  public TaskAssignment putTaskAssignmentsId(String taskAssignmentId, Body37 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'taskAssignmentId' is set
    if (taskAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'taskAssignmentId' when calling putTaskAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/task_assignments/{task_assignment_id}"
      .replaceAll("\\{" + "task_assignment_id" + "\\}", apiClient.escapeString(taskAssignmentId.toString()));

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

    GenericType<TaskAssignment> localVarReturnType = new GenericType<TaskAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
