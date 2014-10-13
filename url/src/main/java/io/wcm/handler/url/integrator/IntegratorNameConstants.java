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

import aQute.bnd.annotation.ProviderType;

/**
 * Names used for integrator handling.
 * <p>
 * Conventions:
 * </p>
 * <ul>
 * <li>NT_ prefix stands for "node type"</li>
 * <li>NN_ prefix stands for "node name"</li>
 * <li>PN_ prefix stands for "property name"</li>
 * <li>RP_ prefix stands for "request property"</li>
 * <li>RA_ prefix stands for "request attribute"</li>
 * </ul>
 */
@ProviderType
public final class IntegratorNameConstants {

  private IntegratorNameConstants() {
    // holds constants only
  }

  /**
   * Integrator mode
   */
  public static final String PN_INTEGRATOR_MODE = "integratorMode";

  /**
   * Integrator protocol
   */
  public static final String PN_INTEGRATOR_PROTOCOL = "integratorProtocol";

}
