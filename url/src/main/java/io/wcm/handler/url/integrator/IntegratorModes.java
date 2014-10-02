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

/**
 * Default integrator modes sufficient for the most usecases.
 */
public enum IntegratorModes implements IntegratorMode {

  /**
   * Simple mode. All URLs are automatically externalized to full URLs (if site URL is configured), and
   * the protocol is detected automatically.
   */
  SIMPLE {

    @Override
    public String getId() {
      return "simple";
    }

    @Override
    public boolean isUseUrlPlaceholders() {
      return false;
    }

    @Override
    public boolean isDetectProtocol() {
      // in simple mode the protocol is detected automatically or read from the integrator page
      return true;
    }

  },

  /**
   * Extended mode. Placeholders are used when externalizing all URLs, they have to be replaced by the
   * integrating application. No automatic proctocol detection takes place.
   */
  EXTENDED {

    @Override
    public String getId() {
      return "extended";
    }

    @Override
    public boolean isUseUrlPlaceholders() {
      return true;
    }

    @Override
    public boolean isDetectProtocol() {
      // in extended mode the protocol has to be defined externally when replacing the URL placeholders
      return false;
    }

  };

}
