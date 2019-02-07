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
  };

  FileUploadExtension.prototype._bindEvents = function () {
    var self = this;

    self._$element.find("[coral-fileupload-clear]").on("click", function (e) {
      self._$pathfield.val("");
    });

    self._$element.on("assetselected", function (event) {
      if (self._element.disabled) {
        return;
      }
      if (!self._isMimeTypeAllowed(event.mimetype)) {
        return;
      }
      self._$pathfield.val(event.path);
    });

  };

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
