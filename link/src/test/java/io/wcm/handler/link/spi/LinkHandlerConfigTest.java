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
package io.wcm.handler.link.spi;

import static io.wcm.handler.link.spi.LinkHandlerConfig.DEFAULT_ROOT_PATH_CONTENT;
import static io.wcm.handler.link.spi.LinkHandlerConfig.DEFAULT_ROOT_PATH_MEDIA;
import static io.wcm.handler.link.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static io.wcm.handler.link.testcontext.AppAemContext.ROOTPATH_CONTENT_OTHER_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.type.InternalCrossContextLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.link.type.MediaLinkType;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class LinkHandlerConfigTest {

  private final AemContext context = AppAemContext.newAemContext();

  private LinkHandlerConfig underTest;
  private Page contentPage;
  private Page contentPageOtherSite;
  private Page xfPage;
  private Page editableTemplatePage;

  @BeforeEach
  void setUp() {
    underTest = AdaptTo.notNull(context.request(), LinkHandlerConfig.class);
    contentPage = context.create().page(ROOTPATH_CONTENT + "/page1");
    contentPageOtherSite = context.create().page(ROOTPATH_CONTENT_OTHER_SITE + "/page2");
    xfPage = context.create().page("/content/experience-fragments/page3");
    editableTemplatePage = context.create().page("/conf/app1/settings/wcm/templates/template1/structure");
  }

  @Test
  void testGetLinkRootPath_Internal() {
    assertEquals(ROOTPATH_CONTENT, underTest.getLinkRootPath(contentPage, InternalLinkType.ID));
    assertEquals(ROOTPATH_CONTENT_OTHER_SITE, underTest.getLinkRootPath(contentPageOtherSite, InternalLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_CONTENT, underTest.getLinkRootPath(xfPage, InternalLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_CONTENT, underTest.getLinkRootPath(editableTemplatePage, InternalLinkType.ID));
  }

  @Test
  void testGetLinkRootPath_InternalCrossContext() {
    assertEquals(DEFAULT_ROOT_PATH_CONTENT, underTest.getLinkRootPath(contentPage, InternalCrossContextLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_CONTENT, underTest.getLinkRootPath(contentPageOtherSite, InternalCrossContextLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_CONTENT, underTest.getLinkRootPath(xfPage, InternalCrossContextLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_CONTENT, underTest.getLinkRootPath(editableTemplatePage, InternalCrossContextLinkType.ID));
  }

  @Test
  void testGetLinkRootPath_Media() {
    assertEquals(DEFAULT_ROOT_PATH_MEDIA, underTest.getLinkRootPath(contentPage, MediaLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_MEDIA, underTest.getLinkRootPath(contentPageOtherSite, MediaLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_MEDIA, underTest.getLinkRootPath(xfPage, MediaLinkType.ID));
    assertEquals(DEFAULT_ROOT_PATH_MEDIA, underTest.getLinkRootPath(editableTemplatePage, MediaLinkType.ID));
  }

  @Test
  void testGetLinkRootPath_Invalid() {
    assertNull(underTest.getLinkRootPath(contentPage, "invalid"));
  }

}
