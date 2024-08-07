/*
 * DeepBox
 * DeepBox API Documentation
 *
 * OpenAPI spec version: 1.0
 * Contact: info@deepcloud.swiss
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.cyberduck.core.deepbox.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Pagination;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
/**
 * DeepBoxes
 */



public class DeepBoxes {
  @JsonProperty("deepBoxes")
  private List<DeepBox> deepBoxes = null;

  @JsonProperty("pagination")
  private Pagination pagination = null;

  @JsonProperty("size")
  private Integer size = null;

  public DeepBoxes deepBoxes(List<DeepBox> deepBoxes) {
    this.deepBoxes = deepBoxes;
    return this;
  }

  public DeepBoxes addDeepBoxesItem(DeepBox deepBoxesItem) {
    if (this.deepBoxes == null) {
      this.deepBoxes = new ArrayList<>();
    }
    this.deepBoxes.add(deepBoxesItem);
    return this;
  }

   /**
   * Get deepBoxes
   * @return deepBoxes
  **/
  @Schema(description = "")
  public List<DeepBox> getDeepBoxes() {
    return deepBoxes;
  }

  public void setDeepBoxes(List<DeepBox> deepBoxes) {
    this.deepBoxes = deepBoxes;
  }

  public DeepBoxes pagination(Pagination pagination) {
    this.pagination = pagination;
    return this;
  }

   /**
   * Get pagination
   * @return pagination
  **/
  @Schema(description = "")
  public Pagination getPagination() {
    return pagination;
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

  public DeepBoxes size(Integer size) {
    this.size = size;
    return this;
  }

   /**
   * Get size
   * @return size
  **/
  @Schema(description = "")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeepBoxes deepBoxes = (DeepBoxes) o;
    return Objects.equals(this.deepBoxes, deepBoxes.deepBoxes) &&
        Objects.equals(this.pagination, deepBoxes.pagination) &&
        Objects.equals(this.size, deepBoxes.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deepBoxes, pagination, size);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeepBoxes {\n");
    
    sb.append("    deepBoxes: ").append(toIndentedString(deepBoxes)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
