package it.pagopa.pn.service.desk.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class HttpConnector {

    private HttpConnector(){
        throw new IllegalCallerException("the constructor must not called");
    }

    public static Mono<PDDocument> downloadFile(String url) {
        log.debug("Url to download: {}", url);
        try {
            Flux<DataBuffer> dataBufferFlux = WebClient.create()
                    .get()
                    .uri(new URI(url))
                    .accept(MediaType.APPLICATION_PDF)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .doOnError(ex -> log.error("Error in WebClient", ex));
            return DataBufferUtils.join(dataBufferFlux)
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return bytes;
                    })
                    .map(randomAccess -> {
                        try {
                            return PDDocument.load(randomAccess);
                        } catch (IOException ex) {
                            log.error("Error during download", ex);
                            throw new RuntimeException(ex);
                        }
                    });
        } catch (URISyntaxException ex) {
            log.error("error in URI ", ex);
            return Mono.error(ex);
        }

    }
}
