/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.http;

import com.google.api.client.util.StreamingContent;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes HTTP request content into an output stream.
 *
 * <p>
 * Implementations don't need to be thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public interface HttpContent extends StreamingContent {

  /** Returns the content length or less than zero if not known. */
  long getLength() throws IOException;

  /**
   * Returns the content encoding (for example {@code "gzip"}) or {@code null} for none.
   *
   * @deprecated (scheduled to be removed in 1.15) Use {@link HttpEncoding} instead
   */
  @Deprecated
  String getEncoding();

  /** Returns the content type or {@code null} for none. */
  String getType();

  /**
   * Returns whether or not retry is supported on this content type.
   *
   * @since 1.4
   */
  boolean retrySupported();

  /**
   * {@inheritDoc}
   *
   * <p>
   * Upgrade warning: in prior version 1.13 the implementation of this method was allowed to close
   * the stream, but starting with 1.14 closing the stream is no longer allowed.
   * </p>
   */
  void writeTo(OutputStream out) throws IOException;
}