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
package io.wcm.handler.richtext.util;

/**
 * Definition of XHTML resources.
 */
enum XHtmlResource {

  /**
   * DTD XHTML 1.0 Strict
   */
  XHTML1_STRICT("-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "xhtml1-strict.dtd"),

  /**
   * DTD XHTML 1.0 Transitional
   */
  XHTML1_TRANSITIONAL("-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "xhtml1-transitional.dtd"),

  /**
   * DTD XHTML 1.0 Frameset
   */
  XHTML1_FRAMESET("-//W3C//DTD XHTML 1.0 Frameset//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd", "xhtml1-frameset.dtd"),

  /**
   * ENTITIES Latin 1 for XHTML
   */
  ENTITIES_LAT1("-//W3C//ENTITIES Latin 1 for XHTML//EN", "xhtml-lat1.ent", "xhtml-lat1.ent"),

  /**
   * ENTITIES Symbols for XHTML
   */
  ENTITIES_SYMBOL("-//W3C//ENTITIES Symbols for XHTML//EN", "xhtml-symbol.ent", "xhtml-symbol.ent"),

  /**
   * ENTITIES Special for XHTML
   */
  ENTITIES_SPECIAL("-//W3C//ENTITIES Special for XHTML//EN", "xhtml-special.ent", "xhtml-special.ent");

  private final String publicId;
  private final String systemId;
  private final String filename;

  XHtmlResource(String publicId, String systemId, String filename) {
    this.publicId = publicId;
    this.systemId = systemId;
    this.filename = filename;
  }

  /**
   * Public Id
   * @return Public Id
   */
  public String getPublicId() {
    return this.publicId;
  }

  /**
   * System Id
   * @return System Id
   */
  public String getSystemId() {
    return this.systemId;
  }

  /**
   * Local filename
   * @return Local filename
   */
  public String getFilename() {
    return this.filename;
  }

}
