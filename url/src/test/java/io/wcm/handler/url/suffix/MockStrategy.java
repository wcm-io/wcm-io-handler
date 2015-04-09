/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.handler.url.suffix;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

class MockStrategy implements SuffixStateKeepingStrategy {

  private final String incomingSuffix;
  private final List<String> suffixPartsToReturn;

  MockStrategy(String incomingSuffix, String... suffixPartsToReturn) {
    this.incomingSuffix = incomingSuffix;
    this.suffixPartsToReturn = Arrays.asList(suffixPartsToReturn);
  }

  @Override
  public List<String> getSuffixPartsToKeep(SuffixBuilder helper) {

    // the suffix from the helper should be the one we put in the context
    String suffix = helper.getRequest().getRequestPathInfo().getSuffix();
    assertEquals(suffix, incomingSuffix);

    // return the fixed suffix parts
    return suffixPartsToReturn;
  }

}
