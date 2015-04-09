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
package io.wcm.handler.url.suffix;

import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.ESCAPE_DELIMITER;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.SUFFIX_PART_DELIMITER;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.hexCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;

/**
 * Unit test for {@link SuffixParser}
 */
public class SuffixParserTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private static final String DEFAULT_RESOURCE_TYPE = "test/resourceType";

  private static final String ESCAPED_SLASH = ESCAPE_DELIMITER + hexCode('/');

  private SuffixParser getParserWithIncomingSuffix(final String urlEncodedSuffix) {
    return this.getParserWithIncommingSuffix(urlEncodedSuffix, null);
  }

  private SuffixParser getParserWithIncommingSuffix(final String urlEncodedSuffix, Page currentPage) {
    // simulate current page and suffix in this test's context context
    setContextAttributes(urlEncodedSuffix, currentPage);

    // create a UrlSuffixHelper that doesn't keep any state
    return new SuffixParser(context.request());
  }

  private void setContextAttributes(final String urlEncodedSuffix, Page currentPage) {
    String decodedSuffix = null;
    if (urlEncodedSuffix != null) {
      try {
        decodedSuffix = URLDecoder.decode(urlEncodedSuffix, CharEncoding.UTF_8);
      }
      catch (UnsupportedEncodingException ex) {
        throw new RuntimeException("Unsupported encoding.", ex);
      }
    }
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix(decodedSuffix);

    if (currentPage != null) {
      context.currentPage(currentPage);
    }
  }

  private Resource createResource(String path) {
    return this.createResource(path, DEFAULT_RESOURCE_TYPE);
  }

  private Resource createResource(String path, String resourceType) {
    return context.create().resource(path, ImmutableValueMap.builder()
        .put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, resourceType)
        .build());
  }

  /**
   * Test method for {@link SuffixParser#getPart(String, String)}
   */
  @Test
  public void testGetPartStringString() {
    // create UrlSuffixHelper with single-part suffix in request
    String suffix = "/abc=def";
    SuffixParser parser = getParserWithIncomingSuffix(suffix);
    // reading existing key with and without default value should return the right value
    assertEquals("def", parser.getPart("abc", "default"));
    assertEquals("def", parser.getPart("abc", null));
    // reading a non-existing key should return the default value (which can be null!)
    assertEquals("default", parser.getPart("def", "default"));
    assertNull(parser.getPart("def", null));


    // create UrlSuffixHelper with null suffix in request
    parser = getParserWithIncomingSuffix(null);
    // reading any key should return the default value (which can be null!)
    assertEquals("default", parser.getPart("abc", "default"));
    assertNull(parser.getPart("abc", null));


    // create UrlSuffixHelper with empty suffix in request
    parser = getParserWithIncomingSuffix("");
    // reading any key should return the default value (which can be null!)
    assertEquals("default", parser.getPart("abc", "default"));
    assertNull(parser.getPart("abc", null));


    // create UrlSuffixHelper with empty *value* in request
    parser = getParserWithIncomingSuffix("/abc=");
    // reading the key should return the empty string
    assertEquals("", parser.getPart("abc", "default"));


    // create UrlSuffixHelper with additional extension in suffix
    parser = getParserWithIncomingSuffix("/abc=def.html");
    // reading the key should return the empty string
    assertEquals("def", parser.getPart("abc", "default"));
  }

  /**
   * Test method for {@link SuffixParser#getPart(String, boolean)}
   */
  @Test
  public void testGetPartStringBoolean() {
    // create UrlSuffixHelper with single-part suffix in request
    SuffixParser parser = getParserWithIncomingSuffix("/abc=true");
    // reading existing key with any default value should return the right value
    assertEquals(true, parser.getPart("abc", true));
    assertEquals(true, parser.getPart("abc", false));
    // reading a non-existing key should return the default value
    assertEquals(true, parser.getPart("def", true));
    assertEquals(false, parser.getPart("def", false));


    // create UrlSuffixHelper with null suffix in request
    parser = getParserWithIncomingSuffix(null);
    // reading any key should return the default value
    assertEquals(true, parser.getPart("def", true));
    assertEquals(false, parser.getPart("def", false));


    // create UrlSuffixHelper with empty suffix in request
    parser = getParserWithIncomingSuffix("");
    // reading any key should return the default value
    assertEquals(true, parser.getPart("def", true));
    assertEquals(false, parser.getPart("def", false));


    // create UrlSuffixHelper with invalid boolean value
    parser = getParserWithIncomingSuffix("/abc=Ger");
    // reading the key should return *false*, as default value is only used if parameter is not set
    assertEquals(true, parser.getPart("abc", true));
    assertEquals(false, parser.getPart("abc", false));
  }

  /**
   * Test method for {@link SuffixParser#getPart(String, int)}
   */
  @Test
  public void testGetPartStringInt() {
    // create UrlSuffixHelper with single-part suffix in request
    SuffixParser parser = getParserWithIncomingSuffix("/abc=123");
    // reading existing key with any default value should return the right value
    assertEquals(123, parser.getPart("abc", 123));
    // reading a non-existing key should return the default value
    assertEquals(456, parser.getPart("def", 456));


    // create UrlSuffixHelper with null suffix in request
    parser = getParserWithIncomingSuffix(null);
    // reading any key should return the default value
    assertEquals(123, parser.getPart("def", 123));


    // create UrlSuffixHelper with empty suffix in request
    parser = getParserWithIncomingSuffix("");
    // reading any key should return the default value
    assertEquals(true, parser.getPart("def", true));
    assertEquals(false, parser.getPart("def", false));


    // create UrlSuffixHelper with invalid value in request
    parser = getParserWithIncomingSuffix("/abc=def");
    // reading any key should return the default value
    assertEquals(123, parser.getPart("abc", 123));
    assertEquals(123, parser.getPart("def", 123));


    // create UrlSuffixHelper with empty value in request
    parser = getParserWithIncomingSuffix("/abc=");
    // reading any key should return the default value
    assertEquals(123, parser.getPart("abc", 123));
    assertEquals(123, parser.getPart("def", 123));
  }

  /**
   * Test method for {@link SuffixParser#getPart(String, long)}
   */
  @Test
  public void testGetPartStringLong() {
    // create UrlSuffixHelper with single-part suffix in request
    SuffixParser parser = getParserWithIncomingSuffix("/abc=123");
    // reading existing key with any default value should return the right value
    assertEquals(123L, parser.getPart("abc", 123L));
    // reading a non-existing key should return the default value
    assertEquals(456L, parser.getPart("def", 456L));


    // create UrlSuffixHelper with null suffix in request
    parser = getParserWithIncomingSuffix(null);
    // reading any key should return the default value
    assertEquals(123L, parser.getPart("def", 123L));


    // create UrlSuffixHelper with empty suffix in request
    parser = getParserWithIncomingSuffix("");
    // reading any key should return the default value
    assertEquals(true, parser.getPart("def", true));
    assertEquals(false, parser.getPart("def", false));


    // create UrlSuffixHelper with invalid value in request
    parser = getParserWithIncomingSuffix("/abc=def");
    // reading any key should return the default value
    assertEquals(123L, parser.getPart("abc", 123L));
    assertEquals(123L, parser.getPart("def", 123L));


    // create UrlSuffixHelper with empty value in request
    parser = getParserWithIncomingSuffix("/abc=");
    // reading any key should return the default value
    assertEquals(123L, parser.getPart("abc", 123L));
    assertEquals(123L, parser.getPart("def", 123L));
  }

  /**
   * Test method for {@link SuffixParser#getResource()}
   */
  @Test
  public void testGetResource() {

    // create a page and a resource within the page
    Page currentPage = context.create().page("/content/a", "template", "title");
    Resource targetResource = createResource("/content/a/jcr:content/b/c");

    // get the existing page with relative path suffix
    SuffixParser parser = getParserWithIncommingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c", currentPage);
    Resource suffixResource = parser.getResource();
    // check that the target resource is found
    assertNotNull(suffixResource);
    assertEquals(targetResource.getPath(), suffixResource.getPath());


    // non-existing resource in suffix
    parser = getParserWithIncommingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "d", currentPage);
    suffixResource = parser.getResource();
    // should return null
    assertNull(suffixResource);


    // don't crash with null-suffix
    parser = getParserWithIncommingSuffix(null, currentPage);
    assertNull(parser.getResource());
  }

  /**
   * Test method for {@link SuffixParser#getResource(String)}
   */
  @Test
  public void testGetResourceString() {

    // create a page that and a resource that within the page
    String resourceType = "theResourceType";
    Page basePage = context.create().page("/content/a", "template", "title");
    String basePath = basePage.getContentResource().getPath();
    Resource targetResource = createResource("/content/a/jcr:content/b/c", resourceType);

    // get the resource by path (relative to the page) and resource type filter
    SuffixParser parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c");
    Resource suffixResource = parser.getResource(basePath);

    // check that the right target resource is found
    assertNotNull(suffixResource);
    assertEquals(targetResource.getPath(), suffixResource.getPath());


    // don't crash if a non-existing path specified in suffix
    parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "c" + ESCAPED_SLASH + "d");
    assertNull(parser.getResource(basePath));

    // don't crash with null suffix
    parser = getParserWithIncomingSuffix(null);
    assertNull(parser.getResource(basePath));


    // don't crash if a non-existing path is specified as base path
    parser = getParserWithIncommingSuffix("b" + ESCAPED_SLASH + "c", basePage);
    assertNull(parser.getResource("/does/not/exist"));

    // don't crash if a null path is specified as base path - use the current page as base
    parser = getParserWithIncommingSuffix("b" + ESCAPED_SLASH + "c", basePage);
    assertNotNull(parser.getResource((String)null));
  }


  /**
   * Test method for {@link SuffixParser#getResource(Filter)}
   */
  @Test
  public void testGetResourceFilterOfResource() {
    // create a page and a resource within the page
    String resourceType = "theResourceType";
    Filter<Resource> filter = new ResourceTypeFilter(resourceType);
    Page currentPage = context.create().page("/content/a", "template", "title");
    Resource targetResource = createResource("/content/a/jcr:content/b/c", resourceType);

    // get the resource by path (relative to current page) and resource type filter
    SuffixParser parser = getParserWithIncommingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c", currentPage);
    Resource suffixResource = parser.getResource(filter);
    // check that the right target resource is found
    assertNotNull(suffixResource);
    assertEquals(targetResource.getPath(), suffixResource.getPath());


    // get the suffix resource with the wrong resource type
    suffixResource = parser.getResource(new ResourceTypeFilter("wrong resourcetype"));
    // check that no resource is found, despite the path being correct
    assertNull(suffixResource);


    // don't crash if a non-existing path specified in suffix
    parser = getParserWithIncommingSuffix(ESCAPED_SLASH + "c" + ESCAPED_SLASH + "d", currentPage);
    assertNull(parser.getResource(filter));


    // don't crash with null suffix
    parser = getParserWithIncommingSuffix(null, currentPage);
    assertNull(parser.getResource(filter));
  }

  /**
   * Test method for {@link SuffixParser#getResource(Filter, String)}
   */
  @Test
  public void testGetResourceFilterOfResourceString() {
    // create a page that and a resource that within the page
    final String resourceType = "theResourceType";
    Page basePage = context.create().page("/content/a", "template", "title");
    String basePath = basePage.getContentResource().getPath();
    Resource targetResource = createResource("/content/a/jcr:content/b/c", resourceType);

    // filter that only includes resources named "c";
    Filter<Resource> cFilter = new Filter<Resource>() {

      @Override
      public boolean includes(Resource pResource) {
        return pResource.getPath().endsWith("/c");
      }

    };

    // get the resource by path (relative to the page) using the "c" filter
    SuffixParser parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c");
    Resource suffixResource = parser.getResource(cFilter, basePath);
    // check that the /content/a/jcr:content/b/c is found
    assertNotNull(suffixResource);
    assertEquals(targetResource.getPath(), suffixResource.getPath());


    // get the "b" resource (relative to the page) using the "c" filter
    parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "b");
    suffixResource = parser.getResource(cFilter, basePath);
    // that resource doesn't match the filter
    assertNull(suffixResource);


    // get a non existing resource (relative to the page) using the "c" filter
    parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "1" + ESCAPED_SLASH + "c");
    suffixResource = parser.getResource(cFilter, basePath);
    // that resource would match the filter but doesn't exist
    assertNull(suffixResource);
  }

  /**
   * Test method for {@link SuffixParser#getResources(Filter, String)}
   */
  @Test
  public void testGetResources() {
    // create a page with 4 resources
    final String resourceType = "theResourceType";
    Page basePage = context.create().page("/content/a", "template", "title");
    String basePath = basePage.getContentResource().getPath();
    Resource resourceBC = createResource(basePath + "/b/c", resourceType);
    Resource resourceBD = createResource(basePath + "/b/d", resourceType);
    Resource resourceCC = createResource(basePath + "/c/c", resourceType);
    Resource resourceCD = createResource(basePath + "/c/d", resourceType);

    // filter that only includes resources named "c";
    Filter<Resource> cFilter = new Filter<Resource>() {

      @Override
      public boolean includes(Resource pResource) {
        return pResource.getPath().endsWith("/c");
      }

    };

    // get these resources from suffix
    SuffixParser parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c"
        + SUFFIX_PART_DELIMITER + "b" + ESCAPED_SLASH + "d"
        + SUFFIX_PART_DELIMITER + "c" + ESCAPED_SLASH + "c"
        + SUFFIX_PART_DELIMITER + "c" + ESCAPED_SLASH + "d");
    List<Resource> suffixResources = parser.getResources(cFilter, basePath);
    // check that the two resources named c are found
    assertNotNull(suffixResources);
    assertEquals(2, suffixResources.size());
    assertEquals(resourceBC.getPath(), suffixResources.get(0).getPath());
    assertEquals(resourceCC.getPath(), suffixResources.get(1).getPath());


    // test that all four resources are found if no filter is used
    parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c"
        + SUFFIX_PART_DELIMITER + "b" + ESCAPED_SLASH + "d"
        + SUFFIX_PART_DELIMITER + "c" + ESCAPED_SLASH + "c"
        + SUFFIX_PART_DELIMITER + "c" + ESCAPED_SLASH + "d");
    List<Resource> allResources = parser.getResources(null, basePath);
    // check that all resources are found
    assertNotNull(allResources);
    assertEquals(4, allResources.size());
    assertEquals(resourceBC.getPath(), allResources.get(0).getPath());
    assertEquals(resourceBD.getPath(), allResources.get(1).getPath());
    assertEquals(resourceCC.getPath(), allResources.get(2).getPath());
    assertEquals(resourceCD.getPath(), allResources.get(3).getPath());


    // test with non-existent resources in suffix
    parser = getParserWithIncomingSuffix(ESCAPED_SLASH + "b" + ESCAPED_SLASH + "c"
        + SUFFIX_PART_DELIMITER + "e" + ESCAPED_SLASH + "c"
        + SUFFIX_PART_DELIMITER + "e" + ESCAPED_SLASH + "d");
    suffixResources = parser.getResources(cFilter, basePath);
    // check that an only the existing resource b/c is found
    assertNotNull(suffixResources);
    assertEquals(1, suffixResources.size());
    assertEquals(resourceBC.getPath(), suffixResources.get(0).getPath());
  }

}
