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
      $.get(pluginConfigUrl, function(result) {
        dialogProperties.linkTypes = dialogProperties.linkTypes || result.linkTypes;
        dialogProperties.rootPath = dialogProperties.rootPath || result.rootPaths.internal;
      });
      
      dialogProperties.targetItems = dialogProperties.targetItems || {
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
      tbGenerator.registerIcon("wcmio.handler.richtext.links#modifylink", "link");
      tbGenerator.registerIcon("wcmio.handler.richtext.links#unlink", "linkOff");
      // call the "super" method
      this.inherited(arguments);
    },
    
    _getAbsoluteParent: function (path, level) {
      var idx = 0;
      var length = path.length;
      while (level >= 0 && idx < length) {
        idx = path.indexOf("/", idx + 1);
        if (idx < 0) {
          idx = length;
        }
        level--;
      }
      return level >= 0 ? "" : path.substring(0, idx);
    }

  });

})(window.jQuery);

// register plugin
CUI.rte.plugins.PluginRegistry.register("wcmio.handler.richtext.links", wcmio.handler.richtext.rte.plugins.LinkPlugin);
