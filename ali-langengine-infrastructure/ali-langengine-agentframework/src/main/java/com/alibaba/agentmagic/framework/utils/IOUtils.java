/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentmagic.framework.utils;

import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;

import java.io.*;
import java.net.URL;

public class IOUtils {

  public static String read(String resourcePath) {
    return new String(IOUtils.readInputStream(IOUtils.class.getClassLoader().getResourceAsStream(resourcePath)));
  }

  public static byte[] readInputStream(InputStream inputStream) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[16 * 1024];
    try {
      int bytesRead = inputStream.read(buffer);
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead);
        bytesRead = inputStream.read(buffer);
      }
    } catch (Exception e) {
      throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, "couldn't read input stream", null);
    }
    return outputStream.toByteArray();
  }

  public static String readFileAsString(String filePath) {
    byte[] buffer = new byte[(int) getFile(filePath).length()];
    BufferedInputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(getFile(filePath)));
      inputStream.read(buffer);
    } catch (Exception e) {
      throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, "Couldn't read file " + filePath + ": " + e.getMessage(), null);
    } finally {
      IOUtils.closeSilently(inputStream);
    }
    return new String(buffer);
  }

  public static File getFile(String filePath) {
    URL url = IOUtils.class.getClassLoader().getResource(filePath);
    try {
      return new File(url.toURI());
    } catch (Exception e) {
      throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, e, null);
    }
  }

  public static void writeStringToFile(String content, String filePath) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(getFile(filePath)));
      outputStream.write(content.getBytes());
      outputStream.flush();
    } catch (Exception e) {
      throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, e, null);
    } finally {
      IOUtils.closeSilently(outputStream);
    }
  }

  public static void writeToFile(String content, String absFilePath) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(absFilePath));
      outputStream.write(content.getBytes());
      outputStream.flush();
    } catch (Exception e) {
      throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, e, null);
    } finally {
      IOUtils.closeSilently(outputStream);
    }
  }

  /**
   * Closes the given stream. The same as calling {@link InputStream#close()}, but errors while closing are silently ignored.
   */
  public static void closeSilently(InputStream inputStream) {
    try {
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (IOException ignore) {
      // Exception is silently ignored
    }
  }

  /**
   * Closes the given stream. The same as calling {@link OutputStream#close()} , but errors while closing are silently ignored.
   */
  public static void closeSilently(OutputStream outputStream) {
    try {
      if (outputStream != null) {
        outputStream.close();
      }
    } catch (IOException ignore) {
      // Exception is silently ignored
    }
  }
}
