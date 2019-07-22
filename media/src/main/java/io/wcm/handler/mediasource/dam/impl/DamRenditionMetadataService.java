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
package io.wcm.handler.mediasource.dam.impl;

import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;

/**
 * @deprecated This service is deprecated and replaced by @link {@link RenditionMetadataListenerService}.
 *             But: If you've referenced this class from your unit test to generate rendition metadata during
 *             the test runs, you can remove the reference completely and instead update to
 *             <code>io.wcm.testing.wcm-io-mock.handler</code> 1.2.0 or higher;
 *             the listener service is there registered automatically, and able to operate in all run modes.
 */
@Deprecated
public final class DamRenditionMetadataService {

  // keep this only as deprecation warning for unit test referencing the original class

}
