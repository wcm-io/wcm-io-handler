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
package io.wcm.handler.url.impl.clientlib;

import org.jetbrains.annotations.NotNull;

/**
 * Rewrites links to static resources from client libraries that are in "allowProxy" mode to /etc.clientlibs.
 */
public interface ClientlibProxyRewriter {

  /**
   * Checks if the given path is (potential) a static resource path from a client library.
   * If this is the case, it is checked if the client library is located below /apps or /libs
   * and has the "allowProxy" mode enabled.
   * If yes, the path is rewritten to /etc.clientlibs. Otherwise it is returned unchanged.
   * @param path Static resource path
   * @return Rewritten path or original path
   */
  @NotNull
  String rewriteStaticResourcePath(@NotNull String path);

}
