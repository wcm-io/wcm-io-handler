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
package io.wcm.handler.media.impl;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.servlets.HttpConstants;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Stream binary data stored in a nt:file or nt:resource node.
 * Optional support for Content-Disposition header ("download_attachment").
 */
@SlingServlet(
    resourceTypes = {
        JcrConstants.NT_FILE, JcrConstants.NT_RESOURCE
    },
    selectors = MediaFileServlet.SELECTOR,
    extensions = MediaFileServlet.EXTENSION,
    methods = HttpConstants.METHOD_GET)
public final class MediaFileServlet extends AbstractMediaFileServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "media_file";

  /**
   * Extension
   */
  public static final String EXTENSION = "file";

}
