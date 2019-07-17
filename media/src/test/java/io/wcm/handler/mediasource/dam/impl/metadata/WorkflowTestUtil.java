/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.metadata;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;
import static io.wcm.handler.mediasource.dam.impl.metadata.WorkflowProcessUtil.RT_WORKFLOW_PACKAGE;
import static org.apache.jackrabbit.vault.packaging.JcrPackage.NN_VLT_DEFINITION;
import static org.apache.jackrabbit.vault.packaging.JcrPackage.NT_VLT_PACKAGE_DEFINITION;
import static org.apache.jackrabbit.vault.packaging.JcrPackageDefinition.NN_FILTER;
import static org.apache.jackrabbit.vault.packaging.JcrPackageDefinition.PN_ROOT;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import org.apache.sling.api.resource.Resource;

import io.wcm.testing.mock.aem.junit5.AemContext;

final class WorkflowTestUtil {

  public static final String PKG_ROOT = "/var/workflow/packages";

  private WorkflowTestUtil() {
    // static methods only
  }

  /**
   * Create workflow package page with paths as filter definition.
   * @param context AEM context
   * @param path Package page path
   * @param filterPaths List of paths for filter
   * @retour Package page resource
   */
  public static Resource createPackage(AemContext context, String path, String... filterPaths) {
    Resource page = context.create().resource(path,
        JCR_PRIMARYTYPE, NT_PAGE,
        PROPERTY_RESOURCE_TYPE, RT_WORKFLOW_PACKAGE);
    Resource pageContent = context.create().resource(page, JCR_CONTENT,
        JCR_PRIMARYTYPE, "cq:PageContent");
    Resource vltDef = context.create().resource(pageContent, NN_VLT_DEFINITION,
        JCR_PRIMARYTYPE, NT_VLT_PACKAGE_DEFINITION);
    for (int i = 0; i < filterPaths.length; i++) {
      context.create().resource(vltDef, NN_FILTER + "/item" + i,
          PN_ROOT, filterPaths[i]);
    }
    return page;
  }

}
