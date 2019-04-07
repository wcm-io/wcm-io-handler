## Media Handler Basic Concepts

The wcm.io Media Handler makes use of a set of basic concepts that helps to enable a flexible and powerful handling of images and other assets in your AEM application.

This page describes these basic concepts, the [usage][usage] page shows examples how to use them.


### The aspect of media handling

The approach of both the [AEM Core WCM Components][aem-core-wcm-components] and the classic AEM foundation components is it to build a single "Image" or "Adaptive Image" component that implements the image handling in a smart and flexible way. This is a good solution for a standalone image component. However, in most AEM applications there are lots of components that need to display images in some way, some of them editable, and some not. Having them all extend the image component is inflexible and sometimes not even possible. Embedding the image component has also its limitations, especially when the parameterizing of the image handling is done via content policies.

Thus the wcm.io Media Handler supports adding image capability to any AEM component as an "aspect", without the need to inherit from or embed any other AEM component to add the image functionality. And most parts of this aspect functionality can be configured in a central place in the AEM project so in each AEM component only very little image handling code is required.


### Media Formats

Digital Marketing web applications are usually built based on visual style guides created by an UX and design team. This style guide defines which types of images can be displayed in which part of the site and in which component. A style guide may limit the usage of images to a certain set of ratios and appearance in different responsive break points to ensure a consistent presentation thorough the whole site.

Thus it's common that for each AEM component the developer has to define a set of restrictions and responsive image definitions that are allowed and in which resolution they should be rendered. To have these definitions not scattered and repeated over all the project, the Media Handler introduces a concept called **media formats**. Whenever an asset is used within a component it's validated against the restrictions defined in the media format to make sure it's allowed to be used in this component (or can be transformed e.g. by creating virtual renditions or auto-cropping it).

Examples for restrictions that can be defined for a media format:

* File extensions, e.g. "png" and "jpeg"
* Ratio e.g. "16:9"
* Min/max width and height or fixed dimensions of images

The use of the media format concept is optional, but recommended.


### Virtual Renditions

In AEM Assets it's expected to have a high resolution version of each image that ensures best quality for all use cases. When rendering the web page the image might need to be used in other ratios or other resolutions then the version the was uploaded to AEM Assets. Especially for responsive images and mobile devices it's important to delivery images in the smallest possible resolution to save bandwith.

wcm.io Media Handler supports rendering virtual renditions of images on-the-fly. It does this in a smart way:

* It first checks of the original rendition of the asset if it matches the required output
* Then it checks if any of the renditions that where uploaded to AEM Assets matches
* If no match is found it builds virtual renditions on the fly e.g. by down-sampling the original rendition or existing renditions to the required size
* If the ratio does not match and auto-cropping is enabled for the component the rendition is additionally cropped to the requested ratio, stripping away superfluous parts of the image
* If manual cropping parameters are stored with the component created by the AEM in-place image editor those are respected as well
* If responsive images are used e.g. using a HTML5 `picture/source` or `img/srcset` the Media Handler builds a set of all required renditions for the different breakpoints on the fly

The URLs of these virtual renditions are based on the Asset URLs from `/content/dam` with additional selectors pointing to image handling servlets from the wcm.io Media Handler. These URLs are cached by default by the AEM dispatcher. If multiple components or pages are using the same image with the same resolution it's generated only once and than re-used from cache. If the HTML pages referencing the virtual renditions are re-published the virtual renditions are not flushed from the dispatcher cache. They are only flushed when the asset itself is re-published.


### Touch UI support

The wcm.io Media Handler integrates nicely into the AEM Touch UI:

* Can be used with the AEM-builtin File Upload Granite UI component.
* Alternatively the enhanced versions of the File Upload and Path Field Granite UI components provided by the Media Handler can be used. They give visual feedback when an asset is selected that does not match with the media format restrictions.
* The features of the AEM Image In-Place editor are supported.
* A customized version of the HTL Placeholder that gives visual feedback in the page when an asset is selected that does not match with the media format restrictions.


### Highly customizable

The Media Handler is highly customizable to adapt it to the needs of your project:

* Define your own media formats.
* Define responsive image handling.
* Support different media sources. By default AEM Assets and inline media (uploaded to the page) are supported, but you can also add other sources (e.g. external image libraries).
* Customize the markup that is used to display the images.
* Customize the property names used to store the asset references and metadata.
* Hook in custom logic into the media handling processing by using pre- and postprocessors.


[usage]: usage.html
[aem-core-wcm-components]: https://github.com/adobe/aem-core-wcm-components
