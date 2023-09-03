package com.example.test.r2;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

public class RequestWrapper2 extends HttpServletRequestWrapper {

  private final Charset encoding;
  private byte[] rawData;
  private Map<String, String[]> params = new HashMap<>();

  /**
   * Constructs a request object wrapping the given request.
   *
   * @param request The request to wrap
   *
   * @throws IllegalArgumentException if the request is null
   */
  public RequestWrapper2(HttpServletRequest request) {
    super(request);

    //Save Parameter
    this.params.putAll(request.getParameterMap());
    Map<String, String[]> parameterMap = request.getParameterMap();

    //Set Encoding
    String charEncoding = request.getCharacterEncoding();
    this.encoding = StringUtils.hasText(charEncoding) ? Charset.forName(charEncoding) : StandardCharsets.UTF_8;

    //Save Body
    try (ServletInputStream inputStream = request.getInputStream()) {
      this.rawData = inputStreamToByteArray(inputStream);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ServletInputStream getInputStream() {
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.rawData);

    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener listener) {

      }

      @Override
      public int read() {
        return byteArrayInputStream.read();
      }
    };
  }

  @Override
  public BufferedReader getReader() throws IOException {

    return new BufferedReader(new InputStreamReader(this.getInputStream(), this.encoding));
  }

  protected byte[] inputStreamToByteArray(InputStream is) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];

      for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
        os.write(buffer, 0, len);
      }
      os.flush();

      return os.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
