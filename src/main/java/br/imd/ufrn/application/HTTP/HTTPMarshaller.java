package br.imd.ufrn.application.HTTP;

import java.io.BufferedReader;
import java.io.IOException;

import br.imd.ufrn.HTTP.HTTPResponse;
import br.imd.ufrn.HTTP.HTTPUtils;

public class HTTPMarshaller {
    public static HTTPResponse getHTTPResponse(BufferedReader responseBuffer) {
        StringBuilder headersBuilder = new StringBuilder();
        String responseLine;

        try {
            responseLine = responseBuffer.readLine();
        } catch (IOException e) {
            System.out.println("IOException: Erro ao ler RequestLine da resposta do servidor! " + e.getMessage());
            return null;
        }

        if (responseLine == null) {
            return null;
        }

        HTTPResponse response = new HTTPResponse(responseLine);
        String line;
        try {
            while ((line = responseBuffer.readLine()) != null && !line.isEmpty()) {
                headersBuilder.append(line).append("\r\n");
                if (line.toLowerCase().startsWith("content-length:")) {
                    response.setContentLength(line);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: Erro ao ler os headers da resposta do servidor! " + e.getMessage());
            return null;
        }

        response.setHeaders(headersBuilder.toString());
        int contentLength = response.getContentLength();
        char[] body = new char[1024];

	  	if (contentLength > 0) {
            int totalRead = 0;

            while (totalRead < response.getContentLength()) {
                int read;
                try {
                    read = responseBuffer.read(
                        body,
                        totalRead,
                        response.getContentLength() - totalRead
                    );
                } catch (IOException e) {
                    System.out.println("IOException: Erro ao ler resposta do servidor! " + e.getMessage());
                    return null;
                }

                if (read == -1) {
                    break;
                }

                totalRead += read;
            }

            response.setBody(body);
        }

	  	return response;
   }
}
