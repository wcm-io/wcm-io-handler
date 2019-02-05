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
package io.wcm.handler.media.markup;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Controls whether the list of IPE cropping ratios is customized by the media handler
 * to match with the ratios of the selected media formats.
 * (If at all and in which WCM mode the ratio customization takes place depends on the
 * implementation of the markup builder and the media source.)
 */
@ProviderType
public enum IPERatioCustomize {

  /**
   * Set customized cropping ratios only when non are defined for the component.
   */
  AUTO,

  /**
   * Never set customized cropping ratios.
   */
  NEVER,

  /**
   * Always set customized cropping ratios, overwriting any existing ones.
   */
  ALWAYS

}
