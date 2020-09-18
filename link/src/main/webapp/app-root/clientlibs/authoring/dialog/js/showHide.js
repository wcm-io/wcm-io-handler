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

  $(document).on("foundation-contentloaded", function(e) {
    var showhideEl = $('.option-linktype-showhide-target---');
    var linkType = $('input[name*="./linkType"]').val();
    if (linkType && showhideEl) {
      showHide(linkType, showhideEl);
    }
  });

  $(document).on("click", "coral-tab", function(e) {
    $(document).trigger("foundation-contentloaded");
  });

  function showHide(linkType, showhideEl) {
    showhideEl.each(function(index) {
      if ($(this).data("showhidetargetvalue") === linkType) {
        $(this).removeClass("hide");
      }
    });
  };

})(document, Granite, Granite.$);