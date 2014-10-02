/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.commons.jcr;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Utility methods for handling nt:resource and nt:file data in JCR.
 */
public final class JcrBinary {

  private JcrBinary() {
    // static util methods only
  }

  /**
   * Checks if the given resource is a node with primary type nt:file.
   * @param resource Resource
   * @return true if resource is nt:file node
   */
  public static boolean isNtFile(Resource resource) {
    return isNt(resource, JcrConstants.NT_FILE);
  }

  /**
   * Checks if the given resource is a node with primary type nt:file.
   * @param resource Resource
   * @return true if resource is nt:file node
   */
  public static boolean isNtResource(Resource resource) {
    return isNt(resource, JcrConstants.NT_RESOURCE);
  }

  /**
   * Checks if the given resource is a node with primary type nt:file.
   * @param resource Resource
   * @return true if resource is nt:file node
   */
  public static boolean isNtFileOrResource(Resource resource) {
    return isNtFile(resource) || isNtResource(resource);
  }

  /**
   * Get mime type from the referenced nt:file or nt:resource node.
   * @param resource Resource pointing to JCR node with primary type nt:file or nt:resource
   * @return Mime type or null if no mime type set or if node is not of type nt:resource or nt:file
   */
  public static String getMimeType(Resource resource) {
    if (isNtResource(resource)) {
      return resource.getValueMap().get(JcrConstants.JCR_MIMETYPE, String.class);
    }
    else if (isNtFile(resource)) {
      return getMimeType(resource.getChild(JcrConstants.JCR_CONTENT));
    }
    return null;
  }

  /**
   * Checks if the given resource is a node with the given node type name
   * @param resource Resource
   * @param nodeTypeName Node type name
   * @return true if resource is of the given node type
   */
  private static boolean isNt(Resource resource, String nodeTypeName) {
    if (resource != null) {
      return StringUtils.equals(resource.getResourceType(), nodeTypeName);
    }
    return false;
  }

}
