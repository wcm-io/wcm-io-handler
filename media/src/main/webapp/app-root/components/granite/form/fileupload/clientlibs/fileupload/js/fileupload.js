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

  var FILEUPLOAD_EVENT_NAMESPACE = ".fileupload";
  var ALERT_CLASS = "wcm-io-handler-media-mediaformat-alert";

  var FileUploadExtension = function (config) {
    var self = this;
    self._element = config.element;
    self._$element = $(config.element);
    self._pathfield = config.pathfield;
    self._$pathfield = $(config.pathfield);
    self._bindEvents();
    
    // validate on load
    var assetPath = self._$pathfield.val();
    self._validateMediaFormat(assetPath);
  };
  
  /**
   * Bind events to existing fileupload widget and pathfield widget.
   */
  FileUploadExtension.prototype._bindEvents = function () {
    var self = this;

    self._$element.find("[coral-fileupload-clear]").on("click", function (e) {
      self._$pathfield.val("");
      self._validateMediaFormat(null);
    });

    self._$element.on("assetselected", function (event) {
      var assetPath = event.path;
      if (self._element.disabled) {
        return;
      }
      if (!self._isMimeTypeAllowed(event.mimetype)) {
        return;
      }
      self._$pathfield.val(assetPath);
      self._validateMediaFormat(assetPath);
    });

    self._$pathfield.on("change", function(event) {
      var assetPath = self._$pathfield.val();
      var thumbnailUrl = assetPath + "/jcr:content/renditions/cq5dam.thumbnail.319.319.png";
      self._$element.trigger($.Event("assetselected", {
        path: assetPath,
        group: "media",
        mimetype: self._detectMimeType(assetPath),
        param: {},
        thumbnail: $("<img src='" + thumbnailUrl + "'>")
      }));
    });

    self._element.on("coral-fileupload:load", function (event) {
      self._$pathfield.val("");
      self._validateMediaFormat(null);
    });

  };
  
  /**
   * Check if the given mime type is allowed for the file upload widget.
   */
  FileUploadExtension.prototype._isMimeTypeAllowed = function (mimeType)  {
    var isAllowed = false;
    var mimeTypes = this._element.accept.split(",");
    if (mimeTypes == "") {
      return true;
    }
    mimeTypes.some(function (allowedMimeType) {
      if (allowedMimeType === mimeType || allowedMimeType === "*" || (new RegExp(allowedMimeType)).test(mimeType)) {
        isAllowed = true;
        return true;
      }
    });
    return isAllowed;
  };
  
  /**
   * Detect mime type from the file extension.
   */
  FileUploadExtension.prototype._detectMimeType = function (assetPath)  {
    var fileExtension = assetPath.substring(assetPath.lastIndexOf('.')+1, assetPath.length);
    if (fileExtension == "jpg" || fileExtension == "jpeg") {
      return "image/jpeg";
    }
    if (fileExtension == "png") {
      return "image/png";
    }
    if (fileExtension == "gif") {
      return "image/gif";
    }
    return null;
  };
  
  /**
   * Validate selected asset reference against the given media formats.
   * Show alert warning when asset does not match.
   */
  FileUploadExtension.prototype._validateMediaFormat = function (assetPath)  {
    var self = this;

    self._clearAlert();

    if (!assetPath) {
      return;
    }
    
    var resourcePath = self._getResourcePath();
    if (!resourcePath) {
      return;
    }
    
    var mediaFormats = self._$element.data("wcmio-mediaformats");
    var mediaFormatsMandatory = self._$element.data("wcmio-mediaformats-mandatory");
    var mediaCropAuto = self._$element.data("wcmio-media-cropauto");
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
        self._showAlert("warning", Granite.I18n.get("Asset invalid"), result.reason);
      }
    });
  };

  /**
   * Show validation alert.
   */
  FileUploadExtension.prototype._showAlert = function (variant, header, content) {
    var self = this;
    self._clearAlert();
    
    var alert = new Coral.Alert();
    alert.header.innerHTML = header;
    alert.content.innerHTML = content;
    alert.variant = variant;
    $(alert).addClass(ALERT_CLASS);
    
    self._$pathfield.after(alert);
  }
  
  /**
   * Remove any validation alerts.
   */
  FileUploadExtension.prototype._clearAlert = function ()  {
    $("." + ALERT_CLASS).remove();
  }
  
  /**
   * Get resource path that is currently edited in the dialog.
   */
  FileUploadExtension.prototype._getResourcePath = function ()  {
    var $dataElement = this._$element.closest("form.cq-dialog");
    if ($dataElement.length < 1) {
      return null;
    }

    var contentPath = $dataElement.attr("action").replace("_jcr_content", "jcr:content");
    if (!contentPath || contentPath.length < 1) {
      return null;
    }
    
    return contentPath;
  };
  
  /**
   * Initializes file upload extension when dialog is loaded
   */
  channel.on("foundation-contentloaded", function (event) {
    var pathfield = $(event.target).find("foundation-autocomplete.wcmio-handler-media-fileupload-pathfield").get(0);
    if (pathfield) {
      $(event.target).find("coral-fileupload.cq-FileUpload").each(function() {
        Coral.commons.ready(this, function (fileUpload) {
          new FileUploadExtension({
            element: fileUpload,
            pathfield: pathfield
          });
        });
      });
    }
  });

}(Granite.$, Granite, jQuery(document), document, this));
