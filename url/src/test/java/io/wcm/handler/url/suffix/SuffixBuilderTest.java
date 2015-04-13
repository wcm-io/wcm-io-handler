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
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.encodeKeyValuePart;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.encodeResourcePathPart;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.hexCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;

public class SuffixBuilderTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private static final String NASTY_NODE_NAME = "_$_%_%25_=_#_&_";
  private static final String ENCODED_NASTY_NODE_NAME = encodeResourcePathPart(NASTY_NODE_NAME);
  private static final String NASTY_STRING_VALUE = "$_._%_%25_/_=_#_?_&_ !";
  private static final String ENCODED_NASTY_STRING_VALUE = encodeKeyValuePart(NASTY_STRING_VALUE);
  private static final String DEFAULT_RESOURCE_TYPE = "test/resourceType";

  private static final String URL_ENCODED_SLASH = "%" + hexCode('/');
  private static final String URL_ENCODED_PERCENT = "%" + hexCode('%');
  private static final String DOUBLE_URL_ENCODED_SLASH = URL_ENCODED_PERCENT + hexCode('/');

  private static final String ESCAPED_SLASH = ESCAPE_DELIMITER + hexCode('/');
  private static final String ESCAPED_DOT = ESCAPE_DELIMITER + hexCode('.');

  private SuffixBuilder getBuilder() {
    return getBuilderWithIncomingSuffix(null);
  }

  private SuffixBuilder getBuilderWithIncomingSuffix(final String urlEncodedSuffix) {
    return this.getBuilderWithIncommingSuffix(urlEncodedSuffix, null);
  }

  private SuffixBuilder getBuilderWithIncommingSuffix(final String urlEncodedSuffix, Page currentPage) {
    // simulate current page and suffix in this test's context context
    setContextAttributes(urlEncodedSuffix, currentPage);

    // create a UrlSuffixHelper that doesn't keep any state
    return new SuffixBuilder();
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
    context.requestPathInfo().setSuffix(decodedSuffix);

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

  @Test
  public void testPageEmptySuffix() {
    // construct a "resetting" suffix that does not add any elements to the suffix
    String suffix = getBuilder().build();
    // should return empty string, cause there is no suffix in the simulated request
    assertNotNull(suffix);
    assertTrue(StringUtils.isEmpty(suffix));
  }

  @Test
  public void testPageStringString() {
    // construct suffix with key/value-pair
    String key = "abc";
    String value = "def";
    String suffix = getBuilder().put(key, value).build();
    // suffix should contain key=value
    assertEquals("abc=def", suffix);


    // construct suffix with empty value
    key = "abc";
    value = "";
    suffix = getBuilder().put(key, value).build();
    // suffix should contain key=
    assertEquals("abc=", suffix);


    // construct suffix with null value
    key = "abc";
    value = null;
    suffix = getBuilder().put(key, value).build();
    // suffix should be empty
    assertEquals("", suffix);

  }

  @Test
  public void testPageStringInt() {
    // construct suffix with numerical key/value-pair
    String key = "abc";
    int value = 123;
    String suffix = getBuilder().put(key, value).build();
    // suffix should contain key=value
    assertEquals("abc=123", suffix);
  }


  @Test
  public void testPageStringLong() {
    // construct suffix with numerical key/value-pair
    String key = "abc";
    long value = 123456789012345L;
    String suffix = getBuilder().put(key, value).build();
    // suffix should contain key=value
    assertEquals("abc=123456789012345", suffix);
  }

  @Test
  public void testPageStringBoolean() {
    // construct suffix with boolean key/value-pair
    String key = "abc";
    boolean value = true;
    String suffix = getBuilder().put(key, value).build();
    // suffix should contain key=true
    assertEquals("abc=true", suffix);

    value = false;
    suffix = getBuilder().put(key, value).build();
    // suffix should contain key=false
    assertEquals("abc=false", suffix);
  }


  @Test
  public void testPageResourceResource() {
    // construct suffix pointing to a resource
    Page page = context.create().page("/content/a", "template", "title");
    Resource baseResource = page.getContentResource();
    Resource targetResource = context.create().resource("/content/a/jcr:content/b/c");

    String suffix = getBuilder().resource(targetResource, baseResource).build();
    // suffix should contain relative path without leading slash
    assertEquals("b" + ESCAPED_SLASH + "c", suffix);


    // construct suffix for base resource
    suffix = getBuilder().resource(baseResource, baseResource).build();
    // should be .
    assertEquals(ESCAPED_DOT, suffix);


    // construct suffix for a null resource
    try {
      suffix = getBuilder().resource(null, baseResource).build();
      fail("expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    // construct suffix for a null base resource
    try {
      suffix = getBuilder().resource(targetResource, null).build();
      fail("expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    // construct suffix with an invalid base resource
    try {
      baseResource = createResource("/content/b");
      suffix = getBuilder().resource(baseResource, null).build();
      fail("expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void testPageResourceResourceStringString() {
    // construct suffix pointing to a resource with key/value-pair
    Resource targetResource = createResource("/content/a/b/c");
    Resource baseResource = createResource("/content/a");
    String key = "abc";
    String value = "def";
    String suffix = getBuilder().resource(targetResource, baseResource).put(key, value).build();
    // suffix should contain both suffix parts, separated with / (resource path first)
    assertEquals("b" + ESCAPED_SLASH + "c" + SUFFIX_PART_DELIMITER + "abc=def", suffix);

  }

  @Test
  public void testPageResourceResourceStringInt() {
    // construct suffix pointing to a resource with a numeric key/value-pair
    Resource targetResource = createResource("/content/a/b/c");
    Resource baseResource = createResource("/content/a");
    String key = "abc";
    int value = 123;
    String suffix = getBuilder().resource(targetResource, baseResource).put(key, value).build();
    // suffix should contain both suffix parts, separated with / (resource path first)
    assertEquals("b" + ESCAPED_SLASH + "c" + SUFFIX_PART_DELIMITER + "abc=123", suffix);
  }

  @Test
  public void testPageResourceResourceStringBoolean() {
    // construct suffix pointing to a resource with a boolean key/value-pair
    Resource targetResource = createResource("/content/a/b/c");
    Resource baseResource = createResource("/content/a");
    String key = "abc";
    boolean value = true;
    String suffix = getBuilder().resource(targetResource, baseResource).put(key, value).build();
    // should contain both suffix parts, separated with / (resource path first)
    assertEquals("b" + ESCAPED_SLASH + "c" + SUFFIX_PART_DELIMITER + "abc=true", suffix);
  }

  @Test
  public void testPageSortedMapOfStringString() {
    // construct suffix to a resource with multiple key/value-pairs
    ValueMap map = ImmutableValueMap.builder()
        .put("abc", 123)
        .put("ghi", 789)
        .put("def", 456)
        .build();
    String suffix = getBuilder().putAll(map).build();
    // suffix should contain all entries, in alphabetical order separated with /
    assertEquals("abc=123" + SUFFIX_PART_DELIMITER + "def=456" + SUFFIX_PART_DELIMITER + "ghi=789", suffix);
  }

  /**
   * tests escaping/unescaping functionality by constructing complex suffix with
   * {@link SuffixBuilder#resources(List, Resource)} and then decomposing it using
   * {@link SuffixParser#get(String, String)} and {@link SuffixParser#getResource(Filter)}
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testEscapingNastyCharacters() throws UnsupportedEncodingException {
    // both key and value may contain url-unsafe characters, and / = which are used as delimiters
    String nastyKey1 = NASTY_STRING_VALUE + "1";
    String nastyValue1 = NASTY_STRING_VALUE + "1";
    String nastyKey2 = NASTY_STRING_VALUE + "2";
    String nastyValue2 = NASTY_STRING_VALUE + "2";

    ValueMap keyValueMap = ImmutableValueMap.builder()
        .put(nastyKey1, nastyValue1)
        .put(nastyKey2, nastyValue2)
        .build();

    // create resources with nasty (but valid) node name
    Page basePage = context.create().page("/content/a", "template", "title");
    Resource baseResource = basePage.getContentResource();
    String resourceType1 = "resourceType1";
    ResourceTypeFilter filterType1 = new ResourceTypeFilter(resourceType1);
    Resource nastyResource1 = createResource("/content/a/jcr:content/b/c" + NASTY_NODE_NAME + "1", resourceType1);
    String resourceType2 = "resourceType2";
    ResourceTypeFilter filterType2 = new ResourceTypeFilter(resourceType2);
    Resource nastyResource2 = createResource("/content/a/jcr:content/b/c" + NASTY_NODE_NAME + "2", resourceType2);

    // construct suffix with all keys, values and paths properly escaped
    String suffix = getBuilder().resources(Arrays.asList(nastyResource1, nastyResource2), baseResource).putAll(keyValueMap).build();
    assertNotNull(suffix);

    // create SuffixHelper with that suffix, decode it and simulate request to the base page
    String suffixWithExtension = "/" + suffix + ".html";
    SuffixParser parser = getParserWithIncommingSuffix(suffixWithExtension, basePage);
    // both nasty keys should be found and decoded
    assertEquals(nastyValue1, parser.get(nastyKey1, String.class));
    assertEquals(nastyValue2, parser.get(nastyKey2, String.class));
    // both nasty resources should be found and decoded
    assertNotNull("resource 1 invalid", parser.getResource(filterType1));
    assertEquals(nastyResource1.getPath(), parser.getResource(filterType1).getPath());
    assertNotNull("resource 2 invalid", parser.getResource(filterType2));
    assertEquals(nastyResource2.getPath(), parser.getResource(filterType2).getPath());


    // suffix may be used without extension (leads to problem if . is not escaped in suffix parts)
    String suffixWithoutExtension = "/" + suffix;
    parser = getParserWithIncommingSuffix(suffixWithoutExtension, basePage);
    // both nasty keys should be found and decoded
    assertEquals(nastyValue1, parser.get(nastyKey1, String.class));
    assertEquals(nastyValue2, parser.get(nastyKey2, String.class));
    // both nasty resources should be found and decoded
    assertEquals(nastyResource1.getPath(), parser.getResource(filterType1).getPath());
    assertEquals(nastyResource2.getPath(), parser.getResource(filterType2).getPath());

  }

  /**
   * tests escaping/unescaping functionality with keys/values/resources suffixes with slashed.
   * The slashes have to be double-escaped as well to void filtered out/misinterpreded by webserver/dispatcher-
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testEscapingWithSlashes() throws UnsupportedEncodingException {
    // both key and value may contain url-unsafe characters, and / = which are used as delimiters
    String slashKey1 = "my/key1";
    String slashValue1 = "my/value1";
    String slashKey2 = "my/key2";
    String slashValue2 = "my/value2";

    ValueMap keyValueMap = ImmutableValueMap.builder()
        .put(slashKey1, slashValue1)
        .put(slashKey2, slashValue2)
        .build();

    // create resources with valid node name
    Page basePage = context.create().page("/content/a", "template", "title");
    Resource baseResource = basePage.getContentResource();
    String resourceType1 = "resourceType1";
    ResourceTypeFilter filterType1 = new ResourceTypeFilter(resourceType1);
    Resource slashResource1 = createResource("/content/a/jcr:content/b/c1", resourceType1);
    String resourceType2 = "resourceType2";
    ResourceTypeFilter filterType2 = new ResourceTypeFilter(resourceType2);
    Resource slashResource2 = createResource("/content/a/jcr:content/b/c2", resourceType2);

    // construct suffix with all keys, values and paths properly escaped
    String suffix = getBuilder().resources(Arrays.asList(slashResource1, slashResource2), baseResource).putAll(keyValueMap).build();
    assertNotNull("suffix empty", suffix);

    // ensure that no slash, not single nor double-escaped found in suffix
    assertTrue("un-escaped slash found", StringUtils.contains(suffix, SUFFIX_PART_DELIMITER)); // "/" is suffix part delimiter
    assertFalse("single-escaped slash found", StringUtils.contains(suffix, URL_ENCODED_SLASH));
    assertFalse("double-escaped slash found", StringUtils.contains(suffix, DOUBLE_URL_ENCODED_SLASH));

    // create SuffixHelper with that suffix, decode it and simulate request to the base page
    String suffixWithExtension = "/" + suffix + ".html";
    SuffixParser parser = getParserWithIncommingSuffix(suffixWithExtension, basePage);
    // both nasty keys should be found and decoded
    assertEquals(slashValue1, parser.get(slashKey1, String.class));
    assertEquals(slashValue2, parser.get(slashKey2, String.class));
    // both nasty resources should be found and decoded
    assertNotNull("resource 1 invalid", parser.getResource(filterType1));
    assertEquals(slashResource1.getPath(), parser.getResource(filterType1).getPath());
    assertNotNull("resource 2 invalid", parser.getResource(filterType2));
    assertEquals(slashResource2.getPath(), parser.getResource(filterType2).getPath());


    // suffix may be used without extension (leads to problem if . is not escaped in suffix parts)
    String suffixWithoutExtension = "/" + suffix;
    parser = getParserWithIncommingSuffix(suffixWithoutExtension, basePage);
    // both nasty keys should be found and decoded
    assertEquals(slashValue1, parser.get(slashKey1, String.class));
    assertEquals(slashValue2, parser.get(slashKey2, String.class));
    // both nasty resources should be found and decoded
    assertEquals(slashResource1.getPath(), parser.getResource(filterType1).getPath());
    assertEquals(slashResource2.getPath(), parser.getResource(filterType2).getPath());

  }

  @Test
  public void testUrlSuffixHelperContext() {

    // create a context with suffix
    String incomingSuffix = "/b" + SUFFIX_PART_DELIMITER + "abc=def";
    setContextAttributes(incomingSuffix, null);

    // create a suffix builder for that context, and check that suffix and currentPage from the context are used
    SuffixBuilder builder = new SuffixBuilder();

    // Because the default SuffixStateKeepingStrategy is to discard everything,
    // a new suffix created with this builder should be empty if no additional info is added
    assertEquals("", builder.build());
  }

  @Test
  public void testUrlSuffixHelperContextSuffixStateKeepingStrategy() {

    // create a context with incoming suffix
    final String incomingSuffix = "/suffix";
    setContextAttributes(incomingSuffix, null);

    // create a mock strategy that checks returns fixed suffix parts
    MockStrategy strategy = new MockStrategy(incomingSuffix, "abc", "def");
    SuffixBuilder builder = new SuffixBuilder(context.request(), strategy);

    // construct a suffix that doesn't add any new info, but should keep the incoming parts
    String outgoingSuffix = builder.build();
    // ensure the strategy was called to filter/manipulate the incoming suffix
    assertEquals("abc" + SUFFIX_PART_DELIMITER + "def", outgoingSuffix);
  }

  @Test
  public void testUrlSuffixHelperContextFilterOfString() {

    // create a context with incoming suffix
    final String incomingSuffix = "/abc" + SUFFIX_PART_DELIMITER + "def=ghi" + SUFFIX_PART_DELIMITER + "jkl=123" + SUFFIX_PART_DELIMITER + "mno=true";
    setContextAttributes(incomingSuffix, null);

    // create a builder with a mock filter that counts how often is called
    // and includes the 2nd and 4th part in the suffix
    EventElementsMockFilter mockFilter = new EventElementsMockFilter();

    // make sure the filter was called for all parts when constructing a new suffix
    String outgoingSuffix = new SuffixBuilder(context.request(), mockFilter).build();
    assertEquals(4, mockFilter.getTestedElements().size());

    // only the 2nd and 4th parts should pass the filter
    assertEquals("abc" + SUFFIX_PART_DELIMITER + "jkl=123", outgoingSuffix);


    // make sure named parameters can be re-added or overwritten
    assertEquals("abc" + SUFFIX_PART_DELIMITER + "def=true" + SUFFIX_PART_DELIMITER + "jkl=123",
        new SuffixBuilder(context.request(), mockFilter).put("def", true).build());
    assertEquals("abc" + SUFFIX_PART_DELIMITER + "jkl=456",
        new SuffixBuilder(context.request(), mockFilter).put("jkl", 456).build());

    // make soure resource parts can be appended
    Page basePage = context.create().page("/content/a", "template", "title");
    Resource resource = createResource(basePage.getContentResource().getPath() + "/def");
    // both the existing abc and b should be in the constructed suffix
    assertEquals("abc" + SUFFIX_PART_DELIMITER + "def" + SUFFIX_PART_DELIMITER + "jkl=123",
        new SuffixBuilder(context.request(), mockFilter).resource(resource, basePage.getContentResource()).build());
  }

  /**
   * creates a suffix with three existing resources and three different named parts for the following tests. <br>
   * Contains double-escaped characters in resource name and value.
   * @return "a/b/c_%2524_%2525_%252525_%253d_#_&_/abc=true/ghi=123/jkl=%2524_%252e_%25_%2525_%2F_%253d_%23_%3F_%26_%21"
   */
  private String prepareStateKeepingSuffix() {

    // If you change anything in the suffix, adjust the expected strings in all test cases below!

    Page currentPage = context.create().page("/content/a", "template", "title");
    Resource resourceA = createResource(currentPage.getContentResource().getPath() + "/a");
    Resource resourceAA = createResource(resourceA.getPath() + "/a");
    Resource resourceB = createResource(currentPage.getContentResource().getPath() + "/b");
    Resource resourceC = createResource(currentPage.getContentResource().getPath() + "/c" + NASTY_NODE_NAME);
    List<Resource> resources = Arrays.asList(resourceAA, resourceB, resourceC);

    ValueMap map = ImmutableValueMap.builder()
        .put("abc", true)
        .put("ghi", 123)
        .put("jkl", NASTY_STRING_VALUE)
        .build();

    SuffixBuilder builder = getBuilder();
    return builder.resources(resources, currentPage.getContentResource()).putAll(map).build();
  }

  @Test
  public void testThatDiscardsAllSuffixState() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder should clear the whole suffix
    SuffixBuilder builder = SuffixBuilder.thatDiscardsAllSuffixState();
    assertEquals("", builder.build());
  }

  @Test
  public void testThatKeepsResourceParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder should clear the whole suffix
    SuffixBuilder builder = SuffixBuilder.thatKeepsResourceParts(context.request());
    assertEquals("a" + ESCAPED_SLASH + "a" + SUFFIX_PART_DELIMITER + "b" + SUFFIX_PART_DELIMITER + "c" + ENCODED_NASTY_NODE_NAME, builder.build());
  }

  @Test
  public void testThatKeepsNamedParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder keeps only some named parts
    SuffixBuilder builder = SuffixBuilder.thatKeepsNamedParts(context.request(), "abc", "ghi");
    assertEquals("abc=true" + SUFFIX_PART_DELIMITER + "ghi=123", builder.build());
  }

  @Test
  public void testThatKeepsNamedPartsAndResources() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder keeps only some named parts
    SuffixBuilder builder = SuffixBuilder.thatKeepsNamedPartsAndResources(context.request(), "abc", "ghi");
    assertEquals("a" + ESCAPED_SLASH + "a" + SUFFIX_PART_DELIMITER + "b" + SUFFIX_PART_DELIMITER + "c" + ENCODED_NASTY_NODE_NAME + SUFFIX_PART_DELIMITER
        + "abc=true" + SUFFIX_PART_DELIMITER + "ghi=123", builder.build());
  }

  @Test
  public void testThatKeepsAllParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    String incomingSuffix = prepareStateKeepingSuffix();
    setContextAttributes(incomingSuffix, null);

    // this suffix builder keeps all parts
    SuffixBuilder builder = SuffixBuilder.thatKeepsAllParts(context.request());
    assertEquals(incomingSuffix, builder.build());
  }

  @Test
  public void testThatDiscardsResourceParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder keeps discards all resource parts, and keeps all key/value
    SuffixBuilder builder = SuffixBuilder.thatDiscardsResourceParts(context.request());
    assertEquals("abc=true" + SUFFIX_PART_DELIMITER + "ghi=123"
        + SUFFIX_PART_DELIMITER + "jkl=" + ENCODED_NASTY_STRING_VALUE, builder.build());
  }

  @Test
  public void testThatDiscardsNamedParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder keeps resources parts, and removes the "jkl" part
    SuffixBuilder builder = SuffixBuilder.thatDiscardsNamedParts(context.request(), "jkl");
    assertEquals("a" + ESCAPED_SLASH + "a" + SUFFIX_PART_DELIMITER + "b" + SUFFIX_PART_DELIMITER + "c" + ENCODED_NASTY_NODE_NAME + SUFFIX_PART_DELIMITER
        + "abc=true" + SUFFIX_PART_DELIMITER + "ghi=123", builder.build());
  }

  @Test
  public void testThatDiscardsResourceAndNamedParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder keeps resources parts, and removes the "jkl" part
    SuffixBuilder builder = SuffixBuilder.thatDiscardsResourceAndNamedParts(context.request(), "jkl");
    assertEquals("abc=true" + SUFFIX_PART_DELIMITER + "ghi=123", builder.build());
  }

  @Test
  public void thatDiscardsSpecificResourceAndNamedParts() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder discards only the a/a resource and the "jkl" named-part
    SuffixBuilder builder = SuffixBuilder.thatDiscardsSpecificResourceAndNamedParts(context.request(), "a/a", "jkl");
    assertEquals("b" + SUFFIX_PART_DELIMITER + "c" + ENCODED_NASTY_NODE_NAME
        + SUFFIX_PART_DELIMITER + "abc=true" + SUFFIX_PART_DELIMITER + "ghi=123", builder.build());
  }

  @Test
  public void thatDiscardsSpecificResourceAndNamedPartsNasty() {
    // prepare complexing incoming suffix with resource and key/value parts
    setContextAttributes(prepareStateKeepingSuffix(), null);

    // this suffix builder discards only the nasty-node-name resource and the "jkl" named-part
    SuffixBuilder builder = SuffixBuilder.thatDiscardsSpecificResourceAndNamedParts(context.request(), "c" + NASTY_NODE_NAME, "jkl");
    assertEquals("a" + ESCAPED_SLASH + "a" + SUFFIX_PART_DELIMITER + "b" + SUFFIX_PART_DELIMITER + "abc=true" + SUFFIX_PART_DELIMITER
        + "ghi=123", builder.build());
  }

}
