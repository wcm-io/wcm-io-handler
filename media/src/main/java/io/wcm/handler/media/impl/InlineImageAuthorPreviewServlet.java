/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.media.impl;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * This servlet provides a preview image using the 'img' selector for preview in the edit dialog
 * for inline images stored in the component's resource.
 * It is only active if a configuration for a list of resource types is present. It should only be
 * configured on author instances, not required on publish instances.
 */
@Designate(ocd = InlineImageAuthorPreviewServlet.Config.class, factory = true)
@Component(service = Servlet.class, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    "sling.servlet.extensions=" + FileExtension.JPEG,
    "sling.servlet.extensions=jpeg", // alternative JPEG extension
    "sling.servlet.extensions=" + FileExtension.PNG,
    "sling.servlet.extensions=" + FileExtension.GIF,
    "sling.servlet.extensions=" + FileExtension.SVG,
    "sling.servlet.selectors=" + InlineImageAuthorPreviewServlet.SELECTOR,
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public final class InlineImageAuthorPreviewServlet extends AbstractMediaFileServlet {
  private static final long serialVersionUID = 1L;

  @ObjectClassDefinition(
      name = "wcm.io Media Handler Inline Image Author Preview Servlet",
      description = "Configures resource types for editable components that may store inline images. "
          + "This servlet provides a preview image using the 'img' selector for preview in the edit dialog.")
  @interface Config {

    @AttributeDefinition(
        name = "Resource types",
        description = "List of resource types for which previews for inline image should be available on author instances.")
    String[] sling_servlet_resourceTypes() default {};

  }

  /**
   * Selector used by the FileUpload Granite UI component for displaying a preview image.
   */
  public static final String SELECTOR = "img";

  @Override
  @SuppressWarnings({ "null", "unused" })
  protected Resource getBinaryDataResource(SlingHttpServletRequest request) {
    // get node that stores the inline media (if available)
    MediaHandlerConfig config = AdaptTo.notNull(request, MediaHandlerConfig.class);
    return request.getResource().getChild(config.getMediaInlineNodeName());
  }

}
