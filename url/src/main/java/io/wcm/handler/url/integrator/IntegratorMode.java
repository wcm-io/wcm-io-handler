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
package io.wcm.handler.url.integrator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * How an integrator template sohuld behave when externalizing URLs.
 */
@ProviderType
public interface IntegratorMode {

  /**
   * @return Integrator mode ID (is stored as identifier in repository)
   */
  String getId();

  /**
   * @return true if placeholders should be used for externalizing URLs instead of site urls
   */
  boolean isUseUrlPlaceholders();

  /**
   * @return true if the prototcol to be used for externalizing URLs in integrator mode is detected automatically
   *         or defined in the integrator page itself.
   */
  boolean isDetectProtocol();

}
