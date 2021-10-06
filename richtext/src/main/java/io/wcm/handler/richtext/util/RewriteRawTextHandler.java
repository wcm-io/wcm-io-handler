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
package io.wcm.handler.richtext.util;

import org.osgi.annotation.versioning.ConsumerType;

import com.drew.lang.annotations.Nullable;

import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.richtext.RichTextRequest;

/**
 * Allows to modify the raw text of the {@link RichTextRequest} before it is processed in the {@link RichTextHandler}.
 * <p>
 * If used for {@link io.wcm.handler.richtext.spi.RichTextHandlerConfig} this interface has to be
 * implemented by a Sling Model class. The adaptables should be
 * {@link org.apache.sling.api.SlingHttpServletRequest} and {@link org.apache.sling.api.resource.Resource}.
 * </p>
 */
@ConsumerType
public interface RewriteRawTextHandler {

  /**
   * @param text The text that should rewrite by this handler.
   * @return The rewritten text.
   */
  @Nullable
  String rewriteText(@Nullable String text);
}
