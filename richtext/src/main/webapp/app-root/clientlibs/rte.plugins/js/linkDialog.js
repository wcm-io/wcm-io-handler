/**
 * Custom link dialog that provides all field supported by the link handler.
 */
;(function($) {

  wcmio.handler.richtext.rte.plugins.LinkDialog = new Class({

    extend: CUI.rte.ui.cui.AbstractDialog,

    toString: "LinkDialog",

    construct: function() {
      var self = this;
      this.fields = {};
      Coral.templates.RichTextEditor["dlg_" + this.getDataType()] = function (config) {
        return self.buildDialog(config);
      };
    },

    // ============ define link dialog fields =============

    /**
     * Build link dialog fields using Coral 3 components.
     */
    buildDialog: function(config) {
      var frag = document.createDocumentFragment();

      // link type
      frag.appendChild(this.createColumnItem({
        name: "linkType",
        fn: this.createSelectField,
        selectItems: config.linkTypes
      }));

      // link properties
      this.addInternalLinkFields(frag, config);
      this.addInternalCrossContextLinkFields(frag, config);
      this.addExternalLinkFields(frag, config);
      this.addMediaLinkFields(frag, config);
      this.addAllLinkTypeFields(frag, config);

      // button bar
      frag.appendChild(this.createColumnItem({
        fn: this.createButtonBar,
        columnClass: "rte-dialog-column--rightAligned"
      }));

      return frag;
    },

    /**
     * Add fields for internal link (same site) type.
     */
    addInternalLinkFields: function(frag, config) {
      frag.appendChild(this.createColumnItem({
        name: "linkContentRef",
        linkType: "internal",
        fn: this.createPathField,
        placeholder: "Internal page",
        rootPath: config.rootPaths.internal || "/content"
      }));
    },

    /**
     * Add fields for internal link (other site) type.
     */
    addInternalCrossContextLinkFields: function(frag, config) {
      frag.appendChild(this.createColumnItem({
        name: "linkCrossContextContentRef",
        linkType: "internalCrossContext",
        fn: this.createPathField,
        placeholder: "Internal page (other site)",
        rootPath: config.rootPaths.internalCrossContext || "/content"
      }));
    },

    /**
     * Add fields for external link type.
     */
    addExternalLinkFields: function(frag, config) {
      frag.appendChild(this.createColumnItem({
        name: "linkExternalRef",
        linkType: "external",
        fn: this.createTextField,
        dataType: "url",
        placeholder: "URL"
      }));
    },

    /**
     * Add fields for media link type.
     */
    addMediaLinkFields: function(frag, config) {
      frag.appendChild(this.createColumnItem({
        name: "linkMediaRef",
        linkType: "media",
        fn: this.createPathField,
        placeholder: "Asset reference",
        rootPath: config.rootPaths.media || "/content/dam"
      }));
      frag.appendChild(this.createColumnItem({
        name: "linkMediaDownload",
        linkType: "media",
        fn: this.createCheckbox,
        label: "Download"
      }));
    },

    /**
     * Add fields visible for all link types.
     */
    addAllLinkTypeFields: function(frag, config) {
      frag.appendChild(this.createColumnItem({
        name: "linkWindowTarget",
        fn: this.createSelectField,
        selectItems: config.linkWindowTargetItems
      }));
    },

    /**
     * Create link dialog field item wrapped in column container and column.
     */
    createColumnItem: function(itemConfig) {
      if (!itemConfig.fn) {
        return;
      }

      // create wrapper divs - column container and container
      var columnContainer = document.createElement("div");
      columnContainer.className = "rte-dialog-columnContainer";

      var column = document.createElement("div");
      column.className = "rte-dialog-column";
      if (itemConfig.columnClass) {
        column.className += " " + itemConfig.columnClass;
      }
      columnContainer.appendChild(column);

      // create item
      var item = itemConfig.fn.apply(this, [itemConfig]);
      column.appendChild(item);

      // store reference to item in field of dialog object
      if (itemConfig.name) {
        this.fields[itemConfig.name] = item;
      }

      // allow to show/hide field based on link type
      if (itemConfig.linkType) {
        $(columnContainer).addClass("linkTypeShowHide");
        columnContainer.dataset.linkType = itemConfig.linkType;
      }

      return columnContainer;
    },

    /**
     * Create button bar with cancel and apply buttons.
     */
    createButtonBar: function(config) {
      var frag = document.createDocumentFragment();

      frag.appendChild(this.createButton({
        icon: "close",
        label: "dialog.cancel",
        dataType: "cancel"
      }));

      frag.appendChild(this.createButton({
        icon: "check",
        label: "dialog.apply",
        dataType: "apply",
        variant: "primary"
      }));

      return frag;
    },

    createButton: function(buttonConfig) {
      var button = document.createElement("button", "coral-button");
      button.setAttribute("is", "coral-button");
      button.setAttribute("icon", buttonConfig.icon);
      button.setAttribute("title", CUI.rte.Utils.i18n(buttonConfig.label));
      button.setAttribute("aria-label", buttonConfig.label);
      button.setAttribute("iconsize", "S");
      if (buttonConfig.variant) {
        button.setAttribute("variant", buttonConfig.variant);
      }
      button.setAttribute("type", "button");
      button.setAttribute("data-type", buttonConfig.dataType);
      button.setAttribute("tabindex", "-1");
      return button;
    },

    createPathField: function(pathfieldConfig) {
      var rootPath = pathfieldConfig.rootPath;
      var pickerSrc = "/mnt/overlay/granite/ui/content/coral/foundation/form/pathfield/picker.html?root=" + rootPath + "&filter=hierarchyNotFile&selectionCount=single";
      var suggestionSrc = "/mnt/overlay/granite/ui/content/coral/foundation/form/pathfield/suggestion{.offset,limit}.html?root=" + rootPath + "&filter=hierarchyNotFile{&query}";

      var pathfield = document.createElement("foundation-autocomplete");
      pathfield.setAttribute("pickersrc", pickerSrc);
      pathfield.setAttribute("placeholder", CUI.rte.Utils.i18n(pathfieldConfig.placeholder));

      var overlay = document.createElement("coral-overlay");
      overlay.className = "foundation-autocomplete-value foundation-picker-buttonlist";
      overlay.setAttribute("data-foundation-picker-buttonlist-src", suggestionSrc);
      pathfield.appendChild(overlay);

      var tagList = document.createElement("coral-taglist");
      tagList.setAttribute("foundation-autocomplete-value", "");
      tagList.setAttribute("name", "href");
      pathfield.appendChild(tagList);

      return pathfield;
    },

    createSelectField: function(selectConfig) {
      var select = document.createElement("coral-select");
      select.setAttribute("handle", "select");

      var firstValue;
      $.each(selectConfig.selectItems, function (elem, item) {
        var selectItem = document.createElement("coral-select-item");
        selectItem.setAttribute("value", item.value);
        selectItem.textContent = CUI.rte.Utils.i18n(item.text);
        select.appendChild(selectItem);
        firstValue = firstValue || item.value;
      });
      select.value = firstValue;

      return select;
    },

    createTextField: function(textConfig) {
      var textField = document.createElement("input", "coral-textfield");
      textField.setAttribute("placeholder", CUI.rte.Utils.i18n(textConfig.placeholder));
      return textField;
    },

    createCheckbox: function(checkboxConfig) {
      var checkbox = document.createElement("coral-checkbox");
      checkbox.label.innerHTML = CUI.rte.Utils.i18n(checkboxConfig.label);
      return checkbox;
    },

   // ============ link dialog implementation =============

    getDataType: function () {
      return "link";
    },

    initialize: function (config) {
      this.config = config;

      this.popoverContent = $(this.fields.linkType).closest("coral-popover-content");

      // initialize show/hide handler
      this.showHideLinkType();
      var self = this;
      $(this.fields.linkType).on("change", function() {
        self.showHideLinkType();
      });
    },

    showHideLinkType: function () {
      // get the selector to find the target elements. its stored as data-.. attribute
      var target = $(".linkTypeShowHide", this.popoverContent);
      var $target = $(target);

      if (target) {
        var value = this.fields.linkType.value;

        // make sure all unselected target elements are hidden.
        $target.not(".hide").addClass("hide");

        // unhide the target element that contains the selected value as data-showhidetargetvalue attribute
        $target.filter("[data-link-type='" + value + "']").removeClass("hide");
      }
    },

    dlgFromModel: function() {
      var objToEdit = this.objToEdit;
      if (!objToEdit) {
        return;
      }

      // check if link was stored with OOTB link plugin previously
      if (this._isLegacyLink(objToEdit)) {
        objToEdit = this._convertFromLegacyLink(objToEdit);
      }

      // populate fields
      $.each(this.fields, function (name, field) {
        var value = null;
        if (objToEdit.dom) {
          value = objToEdit.dom.dataset[name];
        }
        if (!value) {
          if (field.tagName.toLowerCase() == "coral-checkbox") {
            value = false;
          }
          else if (field.tagName.toLowerCase() == "coral-select") {
            value = field.items.getAll()[0].value;
          }
          else {
            value = "";
          }
        }
        if (field.tagName.toLowerCase() == "coral-checkbox") {
          field.checked = (value == true) || (value == "true");
        }
        else {
          field.value = value;
        }
      });

      this.showHideLinkType();
    },

    dlgToModel: function () {
      var self = this;
      var objToEdit = this.objToEdit;
      if (!objToEdit) {
        return;
      }

      // link properties are stored in data attributes
      objToEdit.href = "#";
      objToEdit.target = null;

      // store fields
      $.each(this.fields, function (name, field) {
        var value = field.value;
        if (field.tagName.toLowerCase() == "coral-checkbox") {
          value = field.checked ? "true" : null;
        }
        if (value == "") {
          value = null;
        }
        // we cannot use dataset here because objToEdit.dom may not exist yet
        objToEdit.attributes["data-" + self._camelCaseToHyphen(name)] = value;
      });

    },

    /**
     * Converts to HTML attribute name style - e.g. "myCamelCase" -> "my-camel-case".
     */
    _camelCaseToHyphen: function (camelCase) {
      return camelCase.replace(/[A-Z]/g, "-$&").toLowerCase();
    },

    /**
     * Checks of the given link object has stored link information in "legacy format" as used
     * by the OOTB link plugin, meaning a href value and no link type associated.
     */
    _isLegacyLink: function (objToEdit) {
      return (objToEdit.href != null) && (objToEdit.dom && objToEdit.dom.dataset.linkType == null);
    },

    /**
     * Converting legacy link properties to a simulated DOM object.
     */
    _convertFromLegacyLink: function (objToEdit) {
      var href = objToEdit.href;
      var target = objToEdit.target;

      var props = {};
      if (href.startsWith("/content/dam/")) {
        props.linkType = "media";
        props.linkMediaRef = href;
      }
      else if (href.startsWith("/content/")) {
        props.linkType = "internal";
        props.linkContentRef = href;
      }
      else {
        props.linkType = "external";
        props.linkExternalRef = href;
      }
      props.linkWindowTarget = target;

      return {
        dom: {
          dataset: props
        }
      };
    }

  });

})(window.jQuery);
