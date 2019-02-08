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

  var FileUploadExtension = function (config) {
    var self = this;
    self._element = config.element;
    self._$element = $(config.element);
    self._pathfield = config.pathfield;
    self._$pathfield = $(config.pathfield);
    self._bindEvents();
    
    // enable asset validation
    self._validate = new ns.MediaFormatValidate({
      pathfield: self._pathfield
    });
    // validate on load
    var assetPath = self._$pathfield.val();
    self._validate.validateMediaFormat(assetPath);
  };
  
  /**
   * Bind events to existing fileupload widget and pathfield widget.
   */
  FileUploadExtension.prototype._bindEvents = function () {
    var self = this;

    self._$element.find("[coral-fileupload-clear]").on("click", function (e) {
      self._$pathfield.val("");
      self._validate.validateMediaFormat(null);
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
      self._validate.validateMediaFormat(assetPath);
    });

    self._$pathfield.on("change", function(event) {
      var assetPath = self._$pathfield.val();
      self._triggerAssetSelected(assetPath);
    });

    self._$pathfield.on("assetselected", function (event) {
      var assetPath = event.path;
      self._triggerAssetSelected(assetPath);
    });
    
    self._element.on("coral-fileupload:load", function (event) {
      self._$pathfield.val("");
      self._validate.validateMediaFormat(null);
    });

  };
  
  /**
   * Trigger 'assetselected' event on the fileupload widget.
   */
  FileUploadExtension.prototype._triggerAssetSelected = function (assetPath)  {
    var self = this;
    var thumbnailUrl = assetPath + "/jcr:content/renditions/cq5dam.thumbnail.319.319.png";
    self._$element.trigger($.Event("assetselected", {
      path: assetPath,
      group: "media",
      mimetype: self._detectMimeType(assetPath),
      param: {},
      thumbnail: $("<img src='" + thumbnailUrl + "'>")
    }));
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

}(Granite.$, wcmio.handler.media, jQuery(document), document, this));
