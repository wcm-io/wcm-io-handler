/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.metadata;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Striped;

/**
 * Synchronized the generation of rendition metadata through the ways (metadata service, workflow process)
 * and across threads by using Guava's striped locking. It's used in weak mode because from all the
 * assets in the systems only a few will be processed at any time.
 */
@Component(service = AssetSynchonizationService.class)
public final class AssetSynchonizationService {

  private static final int STRIPE_COUNT = 100;

  private static final Logger log = LoggerFactory.getLogger(AssetSynchonizationService.class);

  private Striped<Lock> lazyWeakLock;

  @Activate
  private void activate() {
    lazyWeakLock = Striped.lazyWeakLock(STRIPE_COUNT);
  }

  @Deactivate
  private void deactivate() {
    lazyWeakLock = null;
  }

  /**
   * Get lock for asset.
   * @param assetPath Asset path
   * @return Lock
   */
  public Lock getLock(String assetPath) {
    return new LockWithLogging(lazyWeakLock.get(assetPath), assetPath);
  }


  /**
   * Wrap the lock instance to apply trace logging on the methods that are actually used.
   */
  private static final class LockWithLogging implements Lock {

    private final Lock delegate;
    private final String assetPath;

    private LockWithLogging(Lock delegate, String assetPath) {
      this.delegate = delegate;
      this.assetPath = assetPath;
    }

    @Override
    public void lock() {
      log.trace("Lock {}", assetPath);
      this.delegate.lock();
    }

    @Override
    public void unlock() {
      log.trace("Unlock {}", assetPath);
      this.delegate.unlock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Condition newCondition() {
      throw new UnsupportedOperationException();
    }

  }

}
