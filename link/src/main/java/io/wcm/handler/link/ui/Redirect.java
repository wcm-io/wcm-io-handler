/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.handler.link.ui;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.wcm.api.WCMMode;

import io.wcm.handler.link.LinkHandler;
import io.wcm.sling.models.annotations.AemObject;

/**
 * Sets redirect header.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class Redirect {

  @SlingObject
  private Resource resource;
  @Self
  private LinkHandler linkHandler;
  @SlingObject
  private SlingHttpServletResponse response;
  @AemObject
  private WCMMode wcmMode;
  @ValueMapValue(optional = true)
  private String redirectStatus;

  private boolean renderPage = true;

  @PostConstruct
  protected void activate() throws IOException {
    // resolve link of redirect page
    String redirectUrl = linkHandler.get(resource).buildUrl();

    // in publish mode redirect to target
    if (wcmMode == WCMMode.DISABLED) {
      renderPage = false;
      if (StringUtils.isNotEmpty(redirectUrl)) {
        if (StringUtils.equals(redirectStatus, Integer.toString(HttpServletResponse.SC_MOVED_TEMPORARILY))) {
          response.sendRedirect(redirectUrl);
        }
        else {
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
          response.setHeader("Location", redirectUrl);
        }
      }
      else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    }
  }

  /**
   * @return true if redirect page should be rendered
   */
  public boolean isRenderPage() {
    return renderPage;
  }

}
