## Dynamic Media

wcm.io Media Handler optionally supports the [Dynamic Media][aem-dynamic-media] feature of AEM for:

* Rendering renditions including resizing and cropping
* Delivery via Dynamic Media CDN
* AI-based Smart Cropping

Only the "Scene7" mode is supported (see [Setting Up Dynamic Media][aem-dynamic-media-administration]). The old and deprecated "hybrid" mode is not supported.

The dynamic media support automatically gets active when the instance was set up with the additional `dynamicmedia_scene7` run mode, and the assets are replicated to the Dynamic Media servers.


### Dynamic Media concept

The integration with dynamic media builds on the [general concepts][general-concepts] of the Media Handler using media formats and a unified media handling API to resolve the renditions for each use case.

If dynamic media is active, the media handler returns rendition URLs pointing to the Dynamic Media delivery servers instead of the Servlet the renders the renditions inside the publish instance without dynamic media. From the [supported file formats][file-format-support] dynamic media is used for JPEG, PNG and TIFF images. It is not used for GIF images (potentially animated) or SVG images (which can be scaled by the browser itself).

It is not required to create image profiles or image presets in AEM for the basic functionality. It's also not required to configure anything in the component instance edit dialogs or content policies.


### Smart Cropping

To enable smart cropping you need to create an [image profile][aem-image-profiles] in AEM, enable "Smart Crop" an assign this profile to the asset folders with the assets you want to use (the profile association is inherited to sub folders). Within the image profile, create a cropping entry with a unique name for each rendition you have defined in the media formats, or you are using dynamically for the different breakpoints when using responsive images. If you have already uploaded the assets to the folder before assigning the profile, or change the profile later, you may need to re-run the "DAM Update Asset" workflow on them.

During the media resolution process when the media handler has detected the required renditions with their sizes and cropping to fit the output media format/ratio, it checks if the asset has an image profile assigned, and if this profile contains and named cropping preset for the target resolution. If this is the case, the cropping preset is used displaying the pre-rendered smart cropping cutout of the original image.

See also [this video][aem-smart-crop-video] for general information about smart cropping.


### System configuration

Make sure to configure the service user mapping for dynamic media as described in the [system configuration][configuration].



[aem-dynamic-media]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/dynamic-media.html
[aem-dynamic-media-administration]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/administering-dynamic-media.html
[aem-image-profiles]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/image-profiles.html
[aem-smart-crop-video]: https://experienceleague.adobe.com/docs/experience-manager-learn/assets/dynamic-media/smart-crop-feature-video-use.html
[general-concepts]: general-concepts.html
[file-format-support]: file-format-support.html
[configuration]: configuration.html