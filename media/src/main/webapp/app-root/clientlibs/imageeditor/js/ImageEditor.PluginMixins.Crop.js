(function ($, ns, channel, window, undefined) {
    "use strict";

    /**
     * @ignore
     * @class
     * @alias wcmio.handler.media.editor.ImageEditor.PluginMixins.Crop
     * @type {Window.Class}
     */
    ns.editor.ImageEditor.PluginMixins.Crop = new Class({

        toString: "Crop",

        extend: Granite.author.editor.ImageEditor.PluginMixins.Crop,

        getAspectRatios : function () {          
          if ($.isPlainObject(this.config) && this.config.aspectRatiosFromMediaFormats) {
            // fetch media formats for current component
            // TODO: detect resource/component image is stored in 
            return [{"ratio": 9 / 16, "name": "16:9"}];
          }

          // fallback to default implementation
          return Granite.author.editor.ImageEditor.PluginMixins.Crop.prototype.getAspectRatios.apply(this);
        }

    });

    // register plugin
    CUI.imageeditor.plugins.PluginRegistry.register("crop", ns.editor.ImageEditor.PluginMixins.Crop);

}(jQuery, wcmio.handler.media, jQuery(document), this));
