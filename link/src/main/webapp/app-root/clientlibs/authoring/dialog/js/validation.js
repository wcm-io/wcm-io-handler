/*
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
/*
 * Registers Coral UI 3 validators.
 */
;(function(document, Granite, $, undefined) {
  "use strict";

  // Predefined patterns
  var pattern = {
    // Matches all strings that seem to have a proper URL scheme - e.g. starting with http://, https://, mailto:, tel:
    // It also allows anchor links staring with #
    url: /^(([^\/]+:|\/\/)|#).*$/
  };

  var foundationValidator = $(window).adaptTo("foundation-registry");

  var getValue = function(el) {
    return $(el).val();
  };

  // predefined "url" pattern validator
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-validation="wcmio.handler.link.url"]',
    validate: function(el) {
      var value = getValue(el);
      var valid = value.length === 0 || pattern.url.test(value);
      if (!valid) {
        return Granite.I18n.get("Please enter a valid URL.");
      }
    }
  });

  $(document).on("foundation-contentloaded", function(e) {
    showHide();
  });

  $(document).on("click", "coral-tab", function(e) {
    showHide();
  });

  function showHide() {
    var linkType = $('input[name*="./linkType"]').val();
    var showhideEl = $('.option-linktype-showhide-target---');

    if (linkType && showhideEl) {
      showhideEl.each(function(index) {
        if ($(this).data("showhidetargetvalue") === linkType) {
          $(this).removeClass("hide");
        }
      });
    }
  }

})(document, Granite, Granite.$);
