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

import static io.wcm.handler.media.format.MediaFormatBuilder.create;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.config.spi.ApplicationProvider;
import io.wcm.config.spi.annotations.Application;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaProcessor;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.spi.helpers.AbstractMediaFormatProvider;
import io.wcm.handler.media.spi.helpers.AbstractMediaHandlerConfig;
import io.wcm.handler.media.spi.helpers.AbstractMediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
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

  static final String APP_ID = "/apps/mediaHandlerImplTestApp";

  @Rule
  public final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(ApplicationProvider.class, new TestApplicationProvider(),
          ImmutableValueMap.of(Constants.SERVICE_RANKING, 1));
      callbackContext.registerService(MediaFormatProvider.class, new TestMediaFormatProvider());
    }
  });

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  public void testPipelining() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    // test pipelining and resolve link
    MediaRequest mediaRequest = new MediaRequest("/content/dummymedia/item1", new MediaArgs().urlMode(UrlModes.DEFAULT));
    Media media = mediaHandler.get(mediaRequest).build();

    // make sure initial media request is unmodified
    assertEquals("/content/dummymedia/item1", mediaRequest.getMediaRef());
    assertEquals(UrlModes.DEFAULT, mediaRequest.getMediaArgs().getUrlMode());

    // check preprocessed link reference
    assertEquals("/content/dummymedia/item1/pre1", media.getMediaRequest().getMediaRef());
    assertEquals(UrlModes.FULL_URL, media.getMediaRequest().getMediaArgs().getUrlMode());

    // check final link url and html element
    assertEquals(true, media.isValid());
    assertEquals("http://xyz/content/dummymedia.post1/item1/pre1.gif", media.getUrl());
    assertNotNull(media.getElement());
    assertEquals("http://xyz/content/dummymedia/item1/pre1.gif", media.getElement().getAttributeValue("src"));

    assertEquals("<img src=\"http://xyz/content/dummymedia/item1/pre1.gif\" />", media.getMarkup());
    assertEquals("http://xyz/content/dummymedia.post1/item1/pre1.gif", media.toString());
  }

  @Test
  public void testMediaFormatResolving() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    MediaRequest mediaRequest = new MediaRequest("/content/dummymedia/item1",
        new MediaArgs("home_stage", "home_teaser"));
    Media metadata = mediaHandler.get(mediaRequest).build();

    MediaFormat[] mediaFormats = metadata.getMediaRequest().getMediaArgs().getMediaFormats();
    String[] mediaFormatNames = metadata.getMediaRequest().getMediaArgs().getMediaFormatNames();

    assertEquals(2, mediaFormats.length);
    assertEquals(TestMediaFormats.HOME_STAGE, mediaFormats[0]);
    assertEquals(TestMediaFormats.HOME_TEASER, mediaFormats[1]);
    assertNull(mediaFormatNames);
  }

  @Test(expected = RuntimeException.class)
  public void testFailedMediaFormatResolving() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    MediaRequest mediaRequest = new MediaRequest("/content/dummymedia/item1",
        new MediaArgs("invalid_media_format"));
    mediaHandler.get(mediaRequest).build();
  }

  @Test
  public void testAllBuilderProps() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    MediaFormat[] mediaFormats = {
        TestMediaFormats.HOME_STAGE, TestMediaFormats.HOME_TEASER
    };
    String[] fileExtensions = {
        "ext1", "ext2"
    };

    Media media = mediaHandler.get("/content/dummymedia/item1")
        .mandatoryMediaFormats(mediaFormats)
        .fileExtensions(fileExtensions)
        .fixedDimension(200, 100)
        .forceDownload(true)
        .altText("alt")
        .dummyImage(false)
        .dummyImageUrl("/dummy/url")
        .build();

    MediaArgs args = media.getMediaRequest().getMediaArgs();
    assertArrayEquals(mediaFormats, args.getMediaFormats());
    assertTrue(args.isMediaFormatsMandatory());
    assertArrayEquals(fileExtensions, args.getFileExtensions());
    assertEquals(200, args.getFixedWidth());
    assertEquals(100, args.getFixedHeight());
    assertTrue(args.isForceDownload());
    assertFalse(args.isDummyImage());
    assertEquals("/dummy/url", args.getDummyImageUrl());
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
    public List<Class<? extends MediaProcessor>> getPreProcessors() {
      return ImmutableList.<Class<? extends MediaProcessor>>of(TestPreProcessor.class);
    }

    @Override
    public List<Class<? extends MediaSource>> getSources() {
      return ImmutableList.<Class<? extends MediaSource>>of(TestMediaSource.class);
    }

    @Override
    public List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders() {
      return ImmutableList.<Class<? extends MediaMarkupBuilder>>of(TestMediaMarkupBuilder.class);
    }

    @Override
    public List<Class<? extends MediaProcessor>> getPostProcessors() {
      return ImmutableList.<Class<? extends MediaProcessor>>of(TestPostProcessor.class);
    }

  };


  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestPreProcessor implements MediaProcessor {

    @Override
    public Media process(Media media) {
      MediaRequest request = media.getMediaRequest();
      String mediaRef = request.getMediaRef() + "/pre1";
      MediaArgs mediaArgs = request.getMediaArgs().urlMode(UrlModes.FULL_URL);
      media.setMediaRequest(new MediaRequest(
          request.getResource(),
          mediaRef,
          mediaArgs,
          request.getRefProperty(),
          request.getCropProperty()
          ));
      return media;
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
    public Media resolveMedia(Media media) {
      String mediaUrl = media.getMediaRequest().getMediaRef();
      media.setUrl("http://xyz" + mediaUrl + ".gif");
      return media;
    }

    @Override
    public void enableMediaDrop(HtmlElement<?> element, MediaRequest mediaRequest) {
      // not supported
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestMediaMarkupBuilder implements MediaMarkupBuilder {

    @Override
    public boolean accepts(Media media) {
      return StringUtils.endsWith(media.getUrl(), ".gif");
    }

    @Override
    public HtmlElement<?> build(Media media) {
      return new Image(media.getUrl());
    }

    @Override
    public boolean isValidMedia(HtmlElement<?> element) {
      return true;
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestPostProcessor implements MediaProcessor {

    @Override
    public Media process(Media media) {
      String mediaUrl = StringUtils.replace(media.getUrl(), "/dummymedia/", "/dummymedia.post1/");
      media.setUrl(mediaUrl);
      return media;
    }

  }

  public static final class TestMediaFormats {

    private TestMediaFormats() {
      // constants only
    }

    /* home_stage */
    public static final MediaFormat HOME_STAGE = create("home_stage", APP_ID)
        .label("Home Stage")
        .width(960)
        .height(485)
        .extensions("gif", "jpg", "png", "swf")
        .build();

    /* home_teaser */
    public static final MediaFormat HOME_TEASER = create("home_teaser", APP_ID)
        .label("Home Teaser")
        .width(206)
        .height(104)
        .extensions("gif", "jpg", "png")
        .renditionGroup("/apps/test/renditiongroup/home_teaser")
        .build();

  }

  @Component(immediate = true)
  @Service(MediaFormatProvider.class)
  public static class TestMediaFormatProvider extends AbstractMediaFormatProvider {

    public TestMediaFormatProvider() {
      super(TestMediaFormats.class);
    }

  }

}
