/**
 * Extends /libs/cq/gui/components/authoring/rte/coralui3/js/RTE.CQLinkPlugin.js
 * with a link dialog similar to wcm.io Link Handler link dialog.
 */
;(function($) {

  wcmio.handler.richtext.rte.plugins.LinkPlugin = new Class({

    extend: CUI.rte.plugins.CQLinkPlugin,

    notifyPluginConfig: function(config) {

      config.linkDialogConfig = config.linkDialogConfig || {};
      config.linkDialogConfig.dialogProperties = config.linkDialogConfig.dialogProperties || {};
      var dialogProperties = config.linkDialogConfig.dialogProperties;

      var currentPagePath = this.detectCurrentPagePath();
      if (currentPagePath) {
        var pluginConfigUrl = currentPagePath + ".wcmio-handler-richtext-rte-plugins-links-config.json";
        $.get({
          url: pluginConfigUrl,
          success: function(result) {
            dialogProperties.linkTypes = dialogProperties.linkTypes || result.linkTypes;
            dialogProperties.rootPaths = dialogProperties.rootPaths || result.rootPaths;
          },
          async: false
        });
      }

      // fallback if JSON call was not successful or did not return link types
      dialogProperties.linkTypes = dialogProperties.linkTypes || {
        "internal": {
          value: "internal",
          text: Granite.I18n.get("io.wcm.handler.link.components.granite.form.linkRefContainer.internal.type")
        },
        "external": {
          value: "external",
          text: Granite.I18n.get("io.wcm.handler.link.components.granite.form.linkRefContainer.external.type")
        }
      };
      dialogProperties.linkWindowTargetItems = dialogProperties.linkWindowTargetItems || {
        "_self": {
          value: "_self",
          text: Granite.I18n.get("io.wcm.handler.link.components.granite.form.linkRefContainer.linkWindowTarget._self")
        },
        "_blank": {
          value: "_blank",
          text: Granite.I18n.get("io.wcm.handler.link.components.granite.form.linkRefContainer.linkWindowTarget._blank")
        }
      };

      // call the "super" method
      this.inherited(arguments);
    },

    getDialogClass: function() {
      return wcmio.handler.richtext.rte.plugins.LinkDialog;
    },

    initializeUI: function (tbGenerator) {
      // register icon for this plugins toolbar actions
      tbGenerator.registerIcon("wcmio-links#modifylink", "link");
      tbGenerator.registerIcon("wcmio-links#unlink", "linkOff");
      tbGenerator.registerIcon("wcmio.handler.richtext.links#modifylink", "link");
      tbGenerator.registerIcon("wcmio.handler.richtext.links#unlink", "linkOff");
      // call the "super" method
      this.inherited(arguments);
    },

    /**
     * Detects the current page path. May return null.
     */ 
    detectCurrentPagePath: function() {
      
      // try get get current page path from ContentFrame (works for IPE and in component edit dialogs)
      if (Granite && Granite.author && Granite.author.ContentFrame) {
        return Granite.author.ContentFrame.getContentPath();
      }
      
      // if we are in page properties dialog - try to get the item URL parameter
      return this.getParameterByName("item");
    },

    /**
     * Gets named request parameter from current URL.
     */
    getParameterByName: function(name) {
      name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
      var regexS = "[?&]" + name + "=([^&#]*)";
      var regex = new RegExp(regexS, "g");
      var match = regex.exec(window.location.search);
      var result = null;
      if (match) {
        result = decodeURIComponent(match[1].replace(/\+/g, " "));
      }
      return result;
    }

  });

})(window.jQuery);

// register plugin
CUI.rte.plugins.PluginRegistry.register("wcmio-links", wcmio.handler.richtext.rte.plugins.LinkPlugin);
// also register old plugin name - deprecated because policy editor seems to have problems with "." in resource names
CUI.rte.plugins.PluginRegistry.register("wcmio.handler.richtext.links", wcmio.handler.richtext.rte.plugins.LinkPlugin);
