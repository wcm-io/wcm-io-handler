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

      // get link plugin configuration for current content page
      var currentPagePath = Granite.author.ContentFrame.getContentPath();
      var pluginConfigUrl = currentPagePath + ".wcmio-handler-richtext-rte-plugins-links-config.json";
      $.get({
        url: pluginConfigUrl,
        success: function(result) {
          dialogProperties.linkTypes = dialogProperties.linkTypes || result.linkTypes;
          dialogProperties.rootPaths = dialogProperties.rootPaths || result.rootPaths;
        },
        async: false
      });

      // fallback if JSON call was not successful or did not return link types
      dialogProperties.linkTypes = dialogProperties.linkTypes || {
        "internal": {
          value: "internal",
          value: "Internal"
        },
        "external": {
          value: "external",
          value: "External"
        }
      };
      dialogProperties.linkWindowTargetItems = dialogProperties.linkWindowTargetItems || {
        "_self": {
          value: "_self",
          text: "Same window"
        },
        "_blank": {
          value: "_blank",
          text: "New window"
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
    }

  });

})(window.jQuery);

// register plugin
CUI.rte.plugins.PluginRegistry.register("wcmio-links", wcmio.handler.richtext.rte.plugins.LinkPlugin);
// also register old plugin name - deprecated because policy editor seems to have problems with "." in resource names
CUI.rte.plugins.PluginRegistry.register("wcmio.handler.richtext.links", wcmio.handler.richtext.rte.plugins.LinkPlugin);
