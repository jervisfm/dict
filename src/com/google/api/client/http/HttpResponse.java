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

import com.google.api.client.util.Charsets;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.LoggingInputStream;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * HTTP response.
 *
 * <p>
 * Callers should call {@link #disconnect} when the HTTP response object is no longer needed.
 * However, {@link #disconnect} does not have to be called if the response stream is properly
 * closed. Example usage:
 * </p>
 *
 * <pre>
   HttpResponse response = request.execute();
   try {
     // process the HTTP response object
   } finally {
     response.disconnect();
   }
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HttpResponse {

  /** HTTP response content or {@code null} before {@link #getContent()}. */
  private InputStream content;

  /** Content encoding or {@code null}. */
  private final String contentEncoding;

  /** Content type or {@code null} for none. */
  private final String contentType;

  /** Parsed content-type/media type or {@code null} if content-type is null. */
  private final HttpMediaType mediaType;

  /** HTTP headers. */
  private final HttpHeaders headers;

  /** Low-level HTTP response. */
  LowLevelHttpResponse response;

  /** Status code. */
  private final int statusCode;

  /** Status message or {@code null}. */
  private final String statusMessage;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** HTTP request. */
  private final HttpRequest request;

  /**
   * Determines the limit to the content size that will be logged during {@link #getContent()}.
   *
   * <p>
   * Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   * </p>
   *
   * <p>
   * If the content size is greater than this limit then it will not be logged.
   * </p>
   *
   * <p>
   * Can be set to {@code 0} to disable content logging. This is useful for example if content has
   * sensitive data such as authentication information.
   * </p>
   *
   * <p>
   * Defaults to {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   */
  private int contentLoggingLimit;

  /**
   * Determines whether logging should be enabled on this response.
   *
   * <p>
   * Defaults to {@link HttpRequest#isLoggingEnabled()}.
   * </p>
   */
  private boolean loggingEnabled;

  /** Signals whether the content has been read from the input stream. */
  private boolean contentRead;

  HttpResponse(HttpRequest request, LowLevelHttpResponse response) throws IOException {
    this.request = request;
    transport = request.getTransport();
    headers = request.getResponseHeaders();
    contentLoggingLimit = request.getContentLoggingLimit();
    loggingEnabled = request.isLoggingEnabled();
    this.response = response;
    contentEncoding = response.getContentEncoding();
    int code = response.getStatusCode();
    statusCode = code;
    String message = response.getReasonPhrase();
    statusMessage = message;
    Logger logger = HttpTransport.LOGGER;
    boolean loggable = loggingEnabled && logger.isLoggable(Level.CONFIG);
    StringBuilder logbuf = null;
    if (loggable) {
      logbuf = new StringBuilder();
      logbuf.append("-------------- RESPONSE --------------").append(StringUtils.LINE_SEPARATOR);
      String statusLine = response.getStatusLine();
      if (statusLine != null) {
        logbuf.append(statusLine);
      } else {
        logbuf.append(code);
        if (message != null) {
          logbuf.append(' ').append(message);
        }
      }
      logbuf.append(StringUtils.LINE_SEPARATOR);
    }

    // headers
    headers.fromHttpResponse(response, loggable ? logbuf : null);

    // Retrieve the content-type directly from the headers as response.getContentType() is outdated
    // and e.g. not set by BatchUnparsedResponse.FakeLowLevelHttpResponse
    String contentType = response.getContentType();
    if (contentType == null) {
      contentType = headers.getContentType();
    }
    this.contentType = contentType;
    mediaType = contentType == null ? null : new HttpMediaType(contentType);

    // log from buffer
    if (loggable) {
      logger.config(logbuf.toString());
    }
  }

  /**
   * Returns the limit to the content size that will be logged during {@link #getContent()}.
   *
   * <p>
   * Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   * </p>
   *
   * <p>
   * If the content size is greater than this limit then it will not be logged.
   * </p>
   *
   * <p>
   * Can be set to {@code 0} to disable content logging. This is useful for example if content has
   * sensitive data such as authentication information.
   * </p>
   *
   * <p>
   * Defaults to {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   *
   * @since 1.7
   */
  public int getContentLoggingLimit() {
    return contentLoggingLimit;
  }

  /**
   * Set the limit to the content size that will be logged during {@link #getContent()}.
   *
   * <p>
   * Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   * </p>
   *
   * <p>
   * If the content size is greater than this limit then it will not be logged.
   * </p>
   *
   * <p>
   * Can be set to {@code 0} to disable content logging. This is useful for example if content has
   * sensitive data such as authentication information.
   * </p>
   *
   * <p>
   * Defaults to {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   *
   * @since 1.7
   */
  public HttpResponse setContentLoggingLimit(int contentLoggingLimit) {
    Preconditions.checkArgument(
        contentLoggingLimit >= 0, "The content logging limit must be non-negative.");
    this.contentLoggingLimit = contentLoggingLimit;
    return this;
  }

  /**
   * Returns whether logging should be enabled on this response.
   *
   * <p>
   * Defaults to {@link HttpRequest#isLoggingEnabled()}.
   * </p>
   *
   * @since 1.9
   */
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  /**
   * Sets whether logging should be enabled on this response.
   *
   * <p>
   * Defaults to {@link HttpRequest#isLoggingEnabled()}.
   * </p>
   *
   * @since 1.9
   */
  public HttpResponse setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
    return this;
  }

  /**
   * Returns the content encoding or {@code null} for none.
   *
   * @since 1.5
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Returns the content type or {@code null} for none.
   *
   * @since 1.5
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Returns the parsed Content-Type in form of a {@link HttpMediaType} or {@code null} if no
   * content-type was set.
   *
   * @since 1.10
   */
  public HttpMediaType getMediaType() {
    return mediaType;
  }

  /**
   * Returns the HTTP response headers.
   *
   * @since 1.5
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns whether received a successful HTTP status code {@code >= 200 && < 300} (see
   * {@link #getStatusCode()}).
   *
   * @since 1.5
   */
  public boolean isSuccessStatusCode() {
    return HttpStatusCodes.isSuccess(statusCode);
  }

  /**
   * Returns the HTTP status code or {@code 0} for none.
   *
   * @since 1.5
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Returns the HTTP status message or {@code null} for none.
   *
   * @since 1.5
   */
  public String getStatusMessage() {
    return statusMessage;
  }

  /**
   * Returns the HTTP transport.
   *
   * @since 1.5
   */
  public HttpTransport getTransport() {
    return transport;
  }

  /**
   * Returns the HTTP request.
   *
   * @since 1.5
   */
  public HttpRequest getRequest() {
    return request;
  }

  /**
   * Returns the content of the HTTP response.
   * <p>
   * The result is cached, so subsequent calls will be fast.
   * <p>
   * Callers should call {@link InputStream#close} after the returned {@link InputStream} is no
   * longer needed. Example usage:
   *
   * <pre>
     InputStream is = response.getContent();
     try {
       // Process the input stream..
     } finally {
       is.close();
     }
   * </pre>
   * <p>
   * {@link HttpResponse#disconnect} does not have to be called if the content is closed.
   *
   * @return input stream content of the HTTP response or {@code null} for none
   * @throws IOException I/O exception
   */
  public InputStream getContent() throws IOException {
    if (!contentRead) {
      InputStream lowLevelResponseContent = this.response.getContent();
      if (lowLevelResponseContent != null) {
        // Flag used to indicate if an exception is thrown before the content is successfully
        // processed.
        boolean contentProcessed = false;
        try {
          // gzip encoding (wrap content with GZipInputStream)
          String contentEncoding = this.contentEncoding;
          if (contentEncoding != null && contentEncoding.contains("gzip")) {
            lowLevelResponseContent = new GZIPInputStream(lowLevelResponseContent);
          }
          // logging (wrap content with LoggingInputStream)
          Logger logger = HttpTransport.LOGGER;
          if (loggingEnabled && logger.isLoggable(Level.CONFIG)) {
            lowLevelResponseContent = new LoggingInputStream(
                lowLevelResponseContent, logger, Level.CONFIG, contentLoggingLimit);
          }
          content = lowLevelResponseContent;
          contentProcessed = true;
        } catch (EOFException e) {
          // this may happen for example on a HEAD request since there no actual response data read
          // in GZIPInputStream
        } finally {
          if (!contentProcessed) {
            lowLevelResponseContent.close();
          }
        }
      }
      contentRead = true;
    }
    return content;
  }

  /**
   * Writes the content of the HTTP response into the given destination output stream.
   *
   * <p>
   * Sample usage:
   *
   * <pre>
     HttpRequest request = requestFactory.buildGetRequest(
         new GenericUrl("https://www.google.com/images/srpr/logo3w.png"));
     OutputStream outputStream = new FileOutputStream(new File("/tmp/logo3w.png"));
     try {
       HttpResponse response = request.execute();
       response.download(outputStream);
     } finally {
       outputStream.close();
     }
    </pre>
   *
   * </p>
   *
   * <p>
   * This method closes the content of the HTTP response from {@link #getContent()}.
   * </p>
   *
   * <p>
   * This method does not close the given output stream.
   * </p>
   *
   * @param outputStream destination output stream
   * @throws IOException I/O exception
   * @since 1.9
   */
  public void download(OutputStream outputStream) throws IOException {
    InputStream inputStream = getContent();
    IOUtils.copy(inputStream, outputStream);
  }

  /**
   * Closes the content of the HTTP response from {@link #getContent()}, ignoring any content.
   */
  public void ignore() throws IOException {
    InputStream content = getContent();
    if (content != null) {
      content.close();
    }
  }

  /**
   * Close the HTTP response content using {@link #ignore}, and disconnect using
   * {@link LowLevelHttpResponse#disconnect()}.
   *
   * @since 1.4
   */
  public void disconnect() throws IOException {
    ignore();
    response.disconnect();
  }

  /**
   * Returns the HTTP response content parser to use for the content type of this HTTP response or
   * {@code null} for none.
   *
   * @deprecated (scheduled to be removed in 1.14) Use {@link #getRequest()}.
   *             {@link HttpRequest#getParser()} instead
   */
  @Deprecated
  public HttpParser getParser() {
    return request.getParser(contentType);
  }

  /**
   * Parses the content of the HTTP response from {@link #getContent()} and reads it into a data
   * class of key/value pairs using the parser returned by {@link #getParser()}.
   *
   * <p>
   * <b>Upgrade Warning:</b> Prior to version 1.11 this method would throw an
   * {@link IllegalArgumentException} when no Content-Type is present in the response and the
   * deprecated HttpParsers were used. Since 1.11 this method will return {@code null} as documented
   * when the server responds with a status code indicating that there is no content, or when a HEAD
   * request was made to the server.
   * </p>
   *
   * <p>
   * <b>Reference:</b> http://tools.ietf.org/html/rfc2616#section-4.3
   * </p>
   *
   * @return parsed data class or {@code null} for no content
   * @throws IOException I/O exception
   * @throws IllegalArgumentException if no parser is defined for the given content type
   */
  @SuppressWarnings("deprecation")
  public <T> T parseAs(Class<T> dataClass) throws IOException {
    // Return null if we can be sure that the response contains no content.
    if (!hasMessageBody()) {
      return null;
    }

    // Check if we have an ObjectParser that we can use
    ObjectParser objectParser = request.getParser();
    if (objectParser != null) {
      return objectParser.parseAndClose(getContent(), getContentCharset(), dataClass);
    }

    // Otherwise fall back to the deprecated implementation
    HttpParser parser = getParser();
    if (parser == null) {
      ignore();
      Preconditions.checkArgument(contentType != null, "Missing Content-Type header in response");
      throw new IllegalArgumentException("No parser defined for Content-Type: " + contentType);
    }
    return parser.parse(this, dataClass);
  }

  /**
   * Returns {@code true} if this response contains a message body. Implemented according to {@href
   *  http://tools.ietf.org/html/rfc2616#section-4.3}.
   */
  private boolean hasMessageBody() {
    if (getRequest().getRequestMethod().equals(HttpMethods.HEAD)) {
      return false;
    }

    int statusCode = getStatusCode();
    if (statusCode / 100 == 1) { // 1xx
      return false;
    }
    if (statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
        || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
      return false;
    }

    return true;
  }

  /**
   * Parses the content of the HTTP response from {@link #getContent()} and reads it into a data
   * type of key/value pairs using the parser returned by {@link #getParser()}.
   *
   * @return parsed data type instance or {@code null} for no content
   * @throws IOException I/O exception
   * @throws IllegalArgumentException if no parser is defined for this response
   * @since 1.10
   */
  public Object parseAs(Type dataType) throws IOException {
    // Check if we have an ObjectParser that we can use
    ObjectParser objectParser = request.getParser();
    Preconditions.checkArgument(objectParser != null, "No ObjectParser defined for response");
    return objectParser.parseAndClose(getContent(), getContentCharset(), dataType);
  }

  /**
   * Parses the content of the HTTP response from {@link #getContent()} and reads it into a string.
   *
   * <p>
   * Since this method returns {@code ""} for no content, a simpler check for no content is to check
   * if {@link #getContent()} is {@code null}.
   * </p>
   *
   * <p>
   * All content is read from the input content stream rather than being limited by the
   * Content-Length. For the character set, it follows the specification by parsing the "charset"
   * parameter of the Content-Type header or by default {@code "ISO-8859-1"} if the parameter is
   * missing.
   * </p>
   *
   * @return parsed string or {@code ""} for no content
   * @throws IOException I/O exception
   */
  public String parseAsString() throws IOException {
    InputStream content = getContent();
    if (content == null) {
      return "";
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(content, out);
    return out.toString(getContentCharset().name());
  }

  /**
   * Returns the {@link Charset} specified in the Content-Type of this response or the
   * {@code "ISO-8859-1"} charset as a default.
   *
   * @since 1.10
   * */
  public Charset getContentCharset() {
    return mediaType == null || mediaType.getCharsetParameter() == null
        ? Charsets.ISO_8859_1 : mediaType.getCharsetParameter();
  }
}