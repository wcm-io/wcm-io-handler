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

  var PathField = function (config) {
    var self = this;
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
   * Bind events to pathfield widget.
   */
  PathField.prototype._bindEvents = function () {
    var self = this;

    self._$pathfield.on("change", function(event) {
      var assetPath = self._$pathfield.val();
      self._validate.validateMediaFormat(assetPath);
    });

    self._$pathfield.on("assetselected", function (event) {
      if (self._pathfield.disabled) {
        return;
      }
      var assetPath = event.path;
      self._$pathfield.val(assetPath);
      self._validate.validateMediaFormat(assetPath);
    });
    
  };
  
  /**
   * Initializes file upload extension when dialog is loaded
   */
  channel.on("foundation-contentloaded", function (event) {
    $(event.target).find("foundation-autocomplete.cq-FileUpload.wcm-io-handler-media-pathfield").each(function() {
      Coral.commons.ready(this, function (pathfield) {
        // avoid double initialization if contentloaded event is fired twice e.g. in pageprops dialog
        if (!$(pathfield).data("js-initialized")) {
          new PathField({
            pathfield: pathfield
          });
          $(pathfield).data("js-initialized", true);
        }
      });
    });
  });

}(Granite.$, wcmio.handler.media, jQuery(document), document, this));
