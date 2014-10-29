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
package io.wcm.handler.url.impl.modes;

import java.util.Set;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

/**
 * Default mode: Does generate a externalized URL without any protocol and hostname,
 * independent of any setting in context-specific configuration.
 */
public final class NoHostnameUrlMode extends AbstractUrlMode {

  @Override
  public String getId() {
    return "NO_HOSTNAME";
  }

  @Override
  public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {
    return null;
  }

  @Override
  public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Resource targetResource) {
    return null;
  }

}
