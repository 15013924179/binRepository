package com.bin.meishikecan.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ReturnErrorJsonHandler extends DefaultResponseErrorHandler {


    public boolean hasError(ClientHttpResponse response) throws IOException {
        return super.hasError(response);
    }

    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.resolve(response.getRawStatusCode());
        if (statusCode == null) {
            byte[] body = this.getResponseBody(response);
            String message = this.getErrorMessage( body, this.getCharset(response));
            throw new UnknownHttpStatusCodeException(message, response.getRawStatusCode(), response.getStatusText(), response.getHeaders(), body, this.getCharset(response));
        } else {
            this.handleError(response, statusCode);
        }
    }

    private String getErrorMessage( @Nullable byte[] responseBody, @Nullable Charset charset) {
        if (ObjectUtils.isEmpty(responseBody)) {
            return "[no body]";
        } else {
            charset = charset == null ? StandardCharsets.UTF_8 : charset;
            int maxChars = 200;
            if (responseBody.length < maxChars * 2) {
                return  new String(responseBody, charset);
            } else {
                try {
                    Reader reader = new InputStreamReader(new ByteArrayInputStream(responseBody), charset);
                    CharBuffer buffer = CharBuffer.allocate(maxChars);
                    reader.read(buffer);
                    reader.close();
                    buffer.flip();
                    return buffer.toString() + "... (" + responseBody.length + " bytes)";
                } catch (IOException var9) {
                    throw new IllegalStateException(var9);
                }
            }
        }
    }

    protected void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        String statusText = response.getStatusText();
        HttpHeaders headers = response.getHeaders();
        byte[] body = this.getResponseBody(response);
        Charset charset = this.getCharset(response);
        String message = this.getErrorMessage(body, charset);
        switch(statusCode.series()) {
            case CLIENT_ERROR:
                throw HttpClientErrorException.create(message, statusCode, statusText, headers, body, charset);
            case SERVER_ERROR:
                throw HttpServerErrorException.create(message, statusCode, statusText, headers, body, charset);
            default:
                throw new UnknownHttpStatusCodeException(message, statusCode.value(), statusText, headers, body, charset);
        }
    }


}
