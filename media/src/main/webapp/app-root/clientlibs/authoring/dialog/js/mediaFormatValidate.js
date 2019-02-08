/*-
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
;(function ($, ns, channel, document, window, undefined) {
  "use strict";

  var MediaFormatValidate = function (config) {
    var self = this;
    self._pathfield = config.pathfield;
    self._$pathfield = $(config.pathfield);
  };

  /**
   * Validate selected asset reference against the given media formats.
   * Show alert warning when asset does not match.
   */
  MediaFormatValidate.prototype.validateMediaFormat = function (assetPath)  {
    var self = this;

    self._clearAlert();

    if (!assetPath) {
      return;
    }
    
    var resourcePath = self._getResourcePath();
    if (!resourcePath) {
      return;
    }
    
    var mediaFormats = self._$pathfield.data("wcmio-mediaformats");
    var mediaFormatsMandatory = self._$pathfield.data("wcmio-mediaformats-mandatory");
    var mediaCropAuto = self._$pathfield.data("wcmio-media-cropauto");
    if (!mediaFormats) {
      return;
    }

    var validateUrl = resourcePath + ".wcm-io-handler-media-mediaformat-validate.json";
    var params = {
        mediaFormats: mediaFormats,
        mediaFormatsMandatory: mediaFormatsMandatory,
        mediaCropAuto: mediaCropAuto,
        mediaRef: assetPath
    };
    $.get(validateUrl, params, function(result) {
      if (!result.valid) {
        self._showAlert("warning", result.reasonTitle, result.reason);
      }
    });
  };

  /**
   * Show validation alert.
   */
  MediaFormatValidate.prototype._showAlert = function (variant, header, content) {
    var self = this;
    self._clearAlert();
    
    var alert = new Coral.Alert();
    alert.header.innerHTML = header;
    alert.content.innerHTML = content;
    alert.variant = variant;
    
    self._$alert = $(alert);
    self._$pathfield.after(alert);
  }
  
  /**
   * Remove any validation alerts.
   */
  MediaFormatValidate.prototype._clearAlert = function ()  {
    var self = this;
    if (self._$alert) { 
      self._$alert.remove();
      delete self._$alert;
    }
  }
  
  /**
   * Get resource path that is currently edited in the dialog.
   */
  MediaFormatValidate.prototype._getResourcePath = function ()  {
    var $dataElement = this._$pathfield.closest("form.cq-dialog");
    if ($dataElement.length < 1) {
      return null;
    }

    var contentPath = $dataElement.attr("action").replace("_jcr_content", "jcr:content");
    if (!contentPath || contentPath.length < 1) {
      return null;
    }
    
    return contentPath;
  };
  
  ns.MediaFormatValidate = MediaFormatValidate;
  
}(Granite.$, wcmio.handler.media, jQuery(document), document, this));
