## File Format Support


### Supported file formats

The following file formats are supported by wcm.io Media Handler:

| File Extension | Mime Type       | Remarks |
|----------------|-----------------|----------
| `jpg`, `jpeg`  | `image/jpeg`    |         |
| `png`          | `image/png`     |         |
| `gif`          | `image/gif`     | If rescaled or transformed, rendered as JPEG. |
| `tif`, `tiff`  | `image/tiff`    | Always rendered as JPEG. |
| `svg`          | `image/svg+xml` | Scaling of vector images is done by the browser.<br/>No support for transformations (no cropping, no rotation). |


### Unit Tests with AEM Mocks

You can use all file formats also when writing unit tests for your application with [AEM Mocks][aem-mock].

If you want to use TIFF or SVG images in your unit test, you need to define additional plugin test dependencies on your classpath, see [Java ImageIO - Advanced Image File Format Support][aem-mock-usage-imageio].



[aem-mock]: https://wcm.io/testing/aem-mock/
[aem-mock-usage-imageio]: https://wcm.io/testing/aem-mock/usage-imageio.html
