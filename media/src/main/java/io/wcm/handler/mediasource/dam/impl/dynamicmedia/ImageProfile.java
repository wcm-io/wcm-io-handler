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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Minimal wrapper for image profile.
 */
public interface ImageProfile {

  /**
   * Get defined smart cropping dimensions. Returns empty list of no definitions are found.
   * @return List of named smart cropping dimensions
   */
  @NotNull
  List<NamedDimension> getSmartCropDefinitions();

}
