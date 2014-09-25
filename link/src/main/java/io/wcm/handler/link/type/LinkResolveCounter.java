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
package io.wcm.handler.link.type;

/**
 * Counts number of recursive link resolve requests to detect endless loops.
 */
class LinkResolveCounter {

  private static final ThreadLocal<LinkResolveCounter> THREAD_LOCAL = new ThreadLocal<LinkResolveCounter>();

  /**
   * Maximum number of "recursion hops" allowed for link resolving.
   */
  private static final int MAX_COUNT = 5;

  private int count;

  /**
   * @return Counter value
   */
  public int getCount() {
    return this.count;
  }

  /**
   * Increase counter by 1.
   */
  public void increaseCount() {
    this.count++;
  }

  /**
   * Decrease counter by 1.
   */
  public void decreaseCount() {
    if (this.count == 0) {
      throw new RuntimeException("Cannot decrease, counter is already 0.");
    }
    this.count--;
  }

  /**
   * @return true if maximum of allowed recursion steps is reached.
   */
  public boolean isMaximumReached() {
    return (this.count > MAX_COUNT);
  }

  /**
   * @return Counter for current request/thread
   */
  public static LinkResolveCounter get() {
    return THREAD_LOCAL.get();
  }

  static ThreadLocal<LinkResolveCounter> getThreadLocal() {
    return THREAD_LOCAL;
  }

}
