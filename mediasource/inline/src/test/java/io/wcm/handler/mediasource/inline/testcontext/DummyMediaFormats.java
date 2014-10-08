/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License) Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing) software
 * distributed under the License is distributed on an "AS IS" BASIS)
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND) either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.mediasource.inline.testcontext;

import static io.wcm.handler.media.format.MediaFormatBuilder.create;
import static io.wcm.handler.mediasource.inline.testcontext.AppAemContext.APPLICATION_ID;
import io.wcm.handler.media.format.MediaFormat;

public final class DummyMediaFormats {

  private DummyMediaFormats() {
    // constants only
  }

  /* home_stage */
  public static final MediaFormat HOME_STAGE = create("home_stage", APPLICATION_ID)
      .label("Home Stage")
      .width(960)
      .height(485)
      .extensions("gif", "jpg", "png", "swf")
      .build();

  /* home_teaser */
  public static final MediaFormat HOME_TEASER = create("home_teaser", APPLICATION_ID)
      .label("Home Teaser")
      .width(206)
      .height(104)
      .extensions("gif", "jpg", "png")
      .renditionGroup("/apps/test/renditiongroup/home_teaser")
      .build();
  public static final MediaFormat HOME_TEASER_SCALE1 = create("home_teaser_scale1", APPLICATION_ID)
      .label("Home Teaser (scaled)")
      .width(158)
      .height(80)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/home_teaser")
      .internal(true)
      .build();

  /* holzauto_cutout */
  public static final MediaFormat HOLZAUTO_CUTOUT_LARGE = create("holzauto_cutout_large", APPLICATION_ID)
      .label("Carline Cutout large")
      .width(595)
      .height(229)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/holzauto_cutout")
      .build();
  public static final MediaFormat HOLZAUTO_CUTOUT_2TO6 = create("holzauto_cutout_2to6", APPLICATION_ID)
      .label("Carline Cutout (scaled)")
      .width(305)
      .height(117)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/holzauto_cutout")
      .internal(true)
      .build();
  public static final MediaFormat HOLZAUTO_CUTOUT_7TO12 = create("holzauto_cutout_7to12", APPLICATION_ID)
      .label("Carline Cutout (scaled)")
      .width(190)
      .height(73)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/holzauto_cutout")
      .internal(true)
      .build();
  public static final MediaFormat HOLZAUTO_CUTOUT_13PLUS = create("holzauto_cutout_13plus", APPLICATION_ID)
      .label("Carline Cutout small")
      .width(120)
      .height(46)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/holzauto_cutout")
      .build();

  /* holzauto_banner */
  public static final MediaFormat HOLZAUTO_BANNER = create("holzauto_banner", APPLICATION_ID)
      .label("Showroom Banner")
      .width(194)
      .height(40)
      .extensions("gif", "jpg", "png")
      .build();

  /* showroom_standard */
  public static final MediaFormat SHOWROOM_STANDARD = create("showroom_standard", APPLICATION_ID)
      .label("Showroom Stage")
      .width(1055)
      .height(500)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .build();
  public static final MediaFormat SHOWROOM_STAGE = create("showroom_stage", APPLICATION_ID)
      .label("Showroom Stage")
      .width(960)
      .height(455)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .ranking(20)
      .build();
  public static final MediaFormat SHOWROOM_CONTROLS = create("showroom_controls", APPLICATION_ID)
      .label("Showroom Thumbnail")
      .width(84)
      .height(40)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .build();
  public static final MediaFormat SHOWROOM_CONTROLS_SCALE1 = create("showroom_controls_scale1", APPLICATION_ID)
      .label("Showroom Thumbnail (scaled)")
      .width(64)
      .height(30)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH = create("showroom_controls_scale1_onlywidth", APPLICATION_ID)
      .label("Showroom Thumbnail (scaled)")
      .width(64)
      .extensions("gif", "jpg", "png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1 = create("showroom_controls_scale1_onlywidth_ratio1", APPLICATION_ID)
      .label("Showroom Thumbnail (scaled)")
      .width(64)
      .ratio(64, 30)
      .extensions("gif", "jpg", "png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2 = create("showroom_controls_scale1_onlywidth_ratio2", APPLICATION_ID)
      .label("Showroom Thumbnail (scaled)")
      .width(64)
      .ratio(64, 64)
      .extensions("gif", "jpg", "png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT = create("showroom_controls_scale1_onlyheight", APPLICATION_ID)
      .label("Showroom Thumbnail (scaled)")
      .height(30)
      .extensions("gif", "jpg", "png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_FLYOUT_FEATURE = create("showroom_flyout_feature", APPLICATION_ID)
      .label("Showroom Flyout Feature")
      .width(205)
      .height(97)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .build();
  public static final MediaFormat SHOWROOM_FEATURE_GALLERY = create("showroom_feature_gallery", APPLICATION_ID)
      .label("Showroom Feature Gallery")
      .width(570)
      .height(270)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .build();
  public static final MediaFormat SHOWROOM_PRINT_IMAGE = create("showroom_print_image", APPLICATION_ID)
      .label("Showroom Print")
      .width(215)
      .height(102)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_TRIMLEVEL_NAVIGATION = create("showroom_trimlevel_navigation", APPLICATION_ID)
      .label("Showroom Trimlevel Navigation")
      .width(215)
      .height(103)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .build();
  public static final MediaFormat SHOWROOM_TRIMLEVEL_NAVIGATION_SMALL = create("showroom_trimlevel_navigation_small", APPLICATION_ID)
      .label("Showroom Trimlevel Navigation (scaled)")
      .width(178)
      .height(85)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();
  public static final MediaFormat SHOWROOM_TRIMLEVEL_GALLERY = create("showroom_trimlevel_gallery", APPLICATION_ID)
      .label("Showroom Trimlevel Gallery")
      .width(685)
      .height(325)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .build();
  public static final MediaFormat SHOWROOM_TRIMLEVEL_COMPARE_FLYOUT = create("showroom_trimlevel_compare_flyout", APPLICATION_ID)
      .label("Showroom Trimlevel Flyout")
      .width(137)
      .height(65)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/showroom_standard")
      .internal(true)
      .build();

  /* showroom_campaign */
  public static final MediaFormat SHOWROOM_CAMPAIGN = create("showroom_campaign", APPLICATION_ID)
      .label("Showroom Campaign Special")
      .width(960)
      .height(455)
      .extensions("gif", "jpg", "png", "swf")
      .ranking(10)
      .build();

  /* feature_flash */
  public static final MediaFormat FEATURE_FLASH = create("feature_flash", APPLICATION_ID)
      .label("Feature Flash Demo")
      .width(570)
      .height(270)
      .extensions("gif", "jpg", "png", "swf")
      .build();

  /* wallpaper */
  public static final MediaFormat WALLPAPER = create("wallpaper", APPLICATION_ID)
      .label("Wallpaper")
      .maxWidth(2000)
      .maxHeight(2000)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/wallpaper")
      .ranking(-1) // do not allow auto-matching
      .build();
  public static final MediaFormat WALLPAPER_1024_768 = create("wallpaper_1024_768", APPLICATION_ID)
      .label("Wallpaper 4:3")
      .width(1024)
      .height(768)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/wallpaper")
      .build();
  public static final MediaFormat WALLPAPER_1280_1024 = create("wallpaper_1280_1024", APPLICATION_ID)
      .label("Wallpaper 5:4")
      .width(1280)
      .height(1024)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/wallpaper")
      .build();
  public static final MediaFormat WALLPAPER_1680_1050 = create("wallpaper_1680_1050", APPLICATION_ID)
      .label("Wallpaper 16:9")
      .width(1680)
      .height(1050)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/wallpaper")
      .build();
  public static final MediaFormat WALLPAPER_1440_900 = create("wallpaper_1440_900", APPLICATION_ID)
      .label("Wallpaper 16:9")
      .width(1440)
      .height(900)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/wallpaper")
      .build();

  /* material_tile */
  public static final MediaFormat MATERIAL_TILE = create("material_tile", APPLICATION_ID)
      .label("Colors and Wheels Tile")
      .width(84)
      .height(40)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/material_tile")
      .build();
  public static final MediaFormat MATERIAL_TILE_SCALE1 = create("material_tile_scale1", APPLICATION_ID)
      .label("Colors and Wheels Tile (scaled)")
      .width(64)
      .height(30)
      .extensions("gif","jpg","png")
      .renditionGroup("/apps/test/renditiongroup/material_tile")
      .internal(true)
      .build();

  /* editorial_stage_large */
  public static final MediaFormat EDITORIAL_STAGE_LARGE = create("editorial_stage_large", APPLICATION_ID)
      .label("Editorial Stage Large")
      .width(960)
      .height(335)
      .extensions("gif","jpg","png","swf")
      .build();

  /* editorial_stage_medium */
  public static final MediaFormat EDITORIAL_STAGE_MEDIUM = create("editorial_stage_medium", APPLICATION_ID)
      .label("Editorial Stage Medium")
      .width(705)
      .height(335)
      .extensions("gif","jpg","png","swf")
      .build();

  /* editorial_stage_small */
  public static final MediaFormat EDITORIAL_STAGE_SMALL = create("editorial_stage_small", APPLICATION_ID)
      .label("Editorial Stage Small")
      .width(960)
      .height(150)
      .extensions("gif","jpg","png","swf")
      .build();

  /* editorial_standard */
  public static final MediaFormat EDITORIAL_STANDARD = create("editorial_standard", APPLICATION_ID)
      .label("Editorial Standard")
      .width(1055)
      .height(500)
      .extensions("gif","jpg","png","swf")
      .renditionGroup("/apps/test/renditiongroup/editorial_standard")
      .build();
  public static final MediaFormat EDITORIAL_1COL = create("editorial_1col", APPLICATION_ID)
      .label("Editorial Standard 1 Column")
      .width(215)
      .height(102)
      .extensions("gif","jpg","png","swf")
      .renditionGroup("/apps/test/renditiongroup/editorial_standard")
      .build();
  public static final MediaFormat EDITORIAL_2COL = create("editorial_2col", APPLICATION_ID)
      .label("Editorial Standard 2 Columns")
      .width(450)
      .height(213)
      .extensions("gif","jpg","png","swf")
      .renditionGroup("/apps/test/renditiongroup/editorial_standard")
      .build();
  public static final MediaFormat EDITORIAL_3COL = create("editorial_3col", APPLICATION_ID)
      .label("Editorial Standard 3 Columns")
      .width(685)
      .height(325)
      .extensions("gif","jpg","png","swf")
      .renditionGroup("/apps/test/renditiongroup/editorial_standard")
      .build();
  public static final MediaFormat SPECIAL_4COL = create("special_4col", APPLICATION_ID)
      .label("Flash Special")
      .width(960)
      .height(455)
      .extensions("gif","jpg","png","swf")
      .renditionGroup("/apps/test/renditiongroup/editorial_standard")
      .ranking(30)
      .build();

  /* download */
  public static final MediaFormat DOWNLOAD = create("download", APPLICATION_ID)
      .label("Download")
      .extensions("pdf", "zip", "exe")
      .build();


}
