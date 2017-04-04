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

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * Default implementation of configuration options of {@link MediaHandlerConfig} interface.
 */
@Component(service = MediaHandlerConfig.class, property = {
    Constants.SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE,
    ContextAwareService.PROPERTY_ACCEPTS_CONTEXT_PATH_EMPTY + ":Boolean=true"
})
public final class DefaultMediaHandlerConfig extends MediaHandlerConfig {

  // inherit all from {@link MediaHandlerConfig}

}
