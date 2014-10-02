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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.wcm.config.api.annotations.Application;
import io.wcm.config.spi.ApplicationProvider;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.AbstractMediaHandlerConfig;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaHandlerConfig;
import io.wcm.handler.media.MediaMarkupBuilder;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaMetadataProcessor;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaReference;
import io.wcm.handler.media.MediaSource;
import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.media.source.AbstractMediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableList;

/**
 * Test {@link MediaHandlerImpl} methods.
 */
public class MediaHandlerImplTest {

  static final String APP_ID = "mediaHandlerImplTestApp";

  @Rule
  public final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(ApplicationProvider.class, new TestApplicationProvider(),
          ImmutableValueMap.of(Constants.SERVICE_RANKING, 1));
    }
  });

  @Test
  public void testPipelining() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    // test pipelining and resolve link
    MediaReference mediaReference = new MediaReference("/content/dummymedia/item1", MediaArgs.urlMode(UrlModes.DEFAULT));
    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(mediaReference);

    // make sure initial media reference is unmodified
    assertEquals("/content/dummymedia/item1", mediaReference.getMediaRef());
    assertEquals(UrlModes.DEFAULT, mediaReference.getMediaArgs().getUrlMode());
    assertEquals("/content/dummymedia/item1", mediaMetadata.getOriginalMediaReference().getMediaRef());
    assertEquals(UrlModes.DEFAULT, mediaMetadata.getOriginalMediaReference().getMediaArgs().getUrlMode());

    // check preprocessed link reference
    assertEquals("/content/dummymedia/item1/pre1", mediaMetadata.getMediaReference().getMediaRef());
    assertEquals(UrlModes.FULL_URL, mediaMetadata.getMediaReference().getMediaArgs().getUrlMode());

    // check final link url and html element
    assertEquals(true, mediaMetadata.isValid());
    assertEquals("http://xyz/content/dummymedia.post1/item1/pre1.gif", mediaMetadata.getMediaUrl());
    assertNotNull(mediaMetadata.getMedia());
    assertEquals("http://xyz/content/dummymedia/item1/pre1.gif", mediaMetadata.getMedia().getAttributeValue("src"));

  }

  public static class TestApplicationProvider implements ApplicationProvider {
    @Override
    public String getApplicationId() {
      return APP_ID;
    }
    @Override
    public String getLabel() {
      return null;
    }
    @Override
    public boolean matches(Resource resource) {
      return true;
    }
  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  }, adapters = MediaHandlerConfig.class)
  @Application(APP_ID)
  public static class TestMediaHandlerConfig extends AbstractMediaHandlerConfig {

    @Override
    public List<Class<? extends MediaMetadataProcessor>> getMediaMetadataPreProcessors() {
      return ImmutableList.<Class<? extends MediaMetadataProcessor>>of(TestMediaMetadataPreProcessor.class);
    }

    @Override
    public List<Class<? extends MediaSource>> getMediaSources() {
      return ImmutableList.<Class<? extends MediaSource>>of(TestMediaSource.class);
    }

    @Override
    public List<Class<? extends MediaMarkupBuilder>> getMediaMarkupBuilders() {
      return ImmutableList.<Class<? extends MediaMarkupBuilder>>of(TestMediaMarkupBuilder.class);
    }

    @Override
    public List<Class<? extends MediaMetadataProcessor>> getMediaMetadataPostProcessors() {
      return ImmutableList.<Class<? extends MediaMetadataProcessor>>of(TestMediaMetadataPostProcessor.class);
    }

  };


  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestMediaMetadataPreProcessor implements MediaMetadataProcessor {

    @Override
    public MediaMetadata process(MediaMetadata mediaMetadata) {
      String mediaRef = mediaMetadata.getMediaReference().getMediaRef() + "/pre1";
      mediaMetadata.getMediaReference().setMediaRef(mediaRef);
      mediaMetadata.getMediaReference().getMediaArgs().setUrlMode(UrlModes.FULL_URL);
      return mediaMetadata;
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestMediaSource extends AbstractMediaSource {

    @Override
    public String getId() {
      return "dummy";
    }

    @Override
    public boolean accepts(String mediaRef) {
      return StringUtils.startsWith(mediaRef, "/content/dummymedia/");
    }

    @Override
    public String getPrimaryMediaRefProperty() {
      return MediaNameConstants.PN_MEDIA_REF;
    }

    @Override
    public MediaMetadata resolveMedia(MediaMetadata mediaMetadata) {
      String mediaUrl = mediaMetadata.getMediaReference().getMediaRef();
      mediaMetadata.setMediaUrl("http://xyz" + mediaUrl + ".gif");
      return mediaMetadata;
    }

    @Override
    public void enableMediaDrop(HtmlElement<?> element, MediaReference mediaReference) {
      // not supported
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestMediaMarkupBuilder implements MediaMarkupBuilder {

    @Override
    public boolean accepts(MediaMetadata mediaMetadata) {
      return StringUtils.endsWith(mediaMetadata.getMediaUrl(), ".gif");
    }

    @Override
    public HtmlElement<?> build(MediaMetadata mediaMetadata) {
      return new Image(mediaMetadata.getMediaUrl());
    }

    @Override
    public boolean isValidMedia(HtmlElement<?> element) {
      return true;
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestMediaMetadataPostProcessor implements MediaMetadataProcessor {

    @Override
    public MediaMetadata process(MediaMetadata mediaMetadata) {
      String mediaUrl = StringUtils.replace(mediaMetadata.getMediaUrl(), "/dummymedia/", "/dummymedia.post1/");
      mediaMetadata.setMediaUrl(mediaUrl);
      return mediaMetadata;
    }

  }

}
