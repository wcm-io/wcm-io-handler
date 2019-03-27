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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaProcessor;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test {@link MediaHandlerImpl} methods.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class MediaHandlerImplTest {

  final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(MediaHandlerConfig.class, new TestMediaHandlerConfig(),
          Constants.SERVICE_RANKING, 1000);
      callbackContext.registerService(MediaFormatProvider.class, new TestMediaFormatProvider(),
          Constants.SERVICE_RANKING, 1000);
    }
  });

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  void testPipelining() {
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
    assertTrue(media.isValid());
    assertEquals("http://xyz/content/dummymedia.post1/item1/pre1.gif", media.getUrl());
    assertNotNull(media.getElement());
    assertEquals("http://xyz/content/dummymedia/item1/pre1.gif", media.getElement().getAttributeValue("src"));

    assertEquals("<img src=\"http://xyz/content/dummymedia/item1/pre1.gif\" />", media.getMarkup());
  }

  @Test
  void testDownload() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    // test pipelining and resolve link
    MediaRequest mediaRequest = new MediaRequest("/content/dummymedia/item1", new MediaArgs()
        .download(true)
        .urlMode(UrlModes.DEFAULT));
    Media media = mediaHandler.get(mediaRequest).build();

    // make sure initial media request is unmodified
    assertEquals("/content/dummymedia/item1", mediaRequest.getMediaRef());
    assertEquals(UrlModes.DEFAULT, mediaRequest.getMediaArgs().getUrlMode());

    // check preprocessed link reference
    assertEquals("/content/dummymedia/item1/pre1", media.getMediaRequest().getMediaRef());
    assertEquals(UrlModes.FULL_URL, media.getMediaRequest().getMediaArgs().getUrlMode());

    // check final link url and html element
    assertTrue(media.isValid());
    assertEquals("http://xyz/content/dummymedia.post1/item1/pre1.pdf", media.getUrl());
    assertNull(media.getElement());

    // check resolved media format list
    assertArrayEquals(new MediaFormat[] { DummyMediaFormats.DOWNLOAD }, media.getMediaRequest().getMediaArgs().getMediaFormats());
  }

  @Test
  void testMediaFormatResolving() {
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

  @Test
  void testFailedMediaFormatResolving() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    MediaRequest mediaRequest = new MediaRequest("/content/dummymedia/item1",
        new MediaArgs("invalid_media_format"));
    Media media = mediaHandler.get(mediaRequest).build();

    assertFalse(media.isValid());
    assertEquals(MediaInvalidReason.INVALID_MEDIA_FORMAT, media.getMediaInvalidReason());
  }

  @Test
  void testAllBuilderProps() {
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
        .contentDispositionAttachment(true)
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
    assertTrue(args.isContentDispositionAttachment());
    assertFalse(args.isDummyImage());
    assertEquals("/dummy/url", args.getDummyImageUrl());
  }

  @Test
  void testComponentProperties() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS, new String[] { "home_stage", "home_teaser" },
        MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY, true,
        MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP, true);

    Resource resource = context.create().resource("/content/test",
        "sling:resourceType", component.getPath(),
        MediaNameConstants.PN_MEDIA_REF, "/content/dummymedia/item1");

    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Media metadata = mediaHandler.get(resource).build();

    MediaFormat[] mediaFormats = metadata.getMediaRequest().getMediaArgs().getMediaFormats();
    String[] mediaFormatNames = metadata.getMediaRequest().getMediaArgs().getMediaFormatNames();

    assertEquals(2, mediaFormats.length);
    assertEquals(TestMediaFormats.HOME_STAGE, mediaFormats[0]);
    assertEquals(TestMediaFormats.HOME_TEASER, mediaFormats[1]);
    assertNull(mediaFormatNames);

    assertTrue(metadata.getMediaRequest().getMediaArgs().isAutoCrop());
    assertTrue(metadata.getMediaRequest().getMediaArgs().isMediaFormatsMandatory());
  }


  public static class TestMediaHandlerConfig extends MediaHandlerConfig {

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
      String mediaRef = request.getMediaRef() != null ? request.getMediaRef() + "/pre1" : null;
      MediaArgs mediaArgs = request.getMediaArgs().urlMode(UrlModes.FULL_URL);
      media.setMediaRequest(new MediaRequest(
          request.getResource(),
          mediaRef,
          mediaArgs,
          request.getRefProperty(),
          request.getCropProperty(),
          request.getRotationProperty()
          ));
      return media;
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestMediaSource extends MediaSource {

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
      if (media.getMediaRequest().getMediaArgs().isDownload()) {
        String mediaUrl = media.getMediaRequest().getMediaRef();
        media.setUrl("http://xyz" + mediaUrl + ".pdf");
        return media;
      }
      else {
        String mediaUrl = media.getMediaRequest().getMediaRef();
        media.setUrl("http://xyz" + mediaUrl + ".gif");
        return media;
      }
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
    static final MediaFormat HOME_STAGE = create("home_stage")
        .label("Home Stage")
        .width(960)
        .height(485)
        .extensions("gif", "jpg", "png", "swf")
        .build();

    /* home_teaser */
    static final MediaFormat HOME_TEASER = create("home_teaser")
        .label("Home Teaser")
        .width(206)
        .height(104)
        .extensions("gif", "jpg", "png")
        .renditionGroup("/apps/test/renditiongroup/home_teaser")
        .build();

  }

  public static class TestMediaFormatProvider extends MediaFormatProvider {

    TestMediaFormatProvider() {
      super(TestMediaFormats.class);
    }

  }

}
