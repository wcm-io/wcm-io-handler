/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import static io.wcm.handler.media.impl.AbstractMediaFileServlet.HEADER_CONTENT_DISPOSITION;
import static io.wcm.handler.media.impl.AssetRenditionContentDispositionFilter.ALLOW_EMPTY_MIME;
import static io.wcm.handler.media.impl.AssetRenditionContentDispositionFilter.BLACK_LIST_MIME_TYPE_CONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.Filter;
import javax.servlet.FilterChain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionContentDispositionFilterTest {

  @Rule
  public AemContext context = new AemContext();

  private Filter underTest;

  private Asset asset;
  private Rendition rendition;

  @Mock
  private FilterChain filterChain;

  @Before
  public void setUp() throws Exception {
    // setup filter with JPEG on the blacklist
    underTest = context.registerInjectActivateService(new AssetRenditionContentDispositionFilter(),
        BLACK_LIST_MIME_TYPE_CONFIG, new String[] { ContentType.JPEG },
        ALLOW_EMPTY_MIME, false);

    asset = context.create().asset("/content/dam/test/asset1.png", 10, 10, ContentType.PNG);
    rendition = context.create().assetRendition(asset, "rendition1", 5, 5, ContentType.JPEG);

  }

  @Test
  public void testAsset_NoHeader() throws Exception {
    context.currentResource(asset.getPath());
    underTest.doFilter(context.request(), context.response(), filterChain);
    assertNull(context.response().getHeader(HEADER_CONTENT_DISPOSITION));
  }

  @Test
  public void testSafeRendition() throws Exception {
    context.currentResource(asset.getOriginal().getPath());
    underTest.doFilter(context.request(), context.response(), filterChain);
    assertEquals("inline", context.response().getHeader(HEADER_CONTENT_DISPOSITION));
  }

  @Test
  public void testUnsafeRendition() throws Exception {
    context.currentResource(rendition.getPath());
    underTest.doFilter(context.request(), context.response(), filterChain);
    assertNull(context.response().getHeader(HEADER_CONTENT_DISPOSITION));
  }

}
