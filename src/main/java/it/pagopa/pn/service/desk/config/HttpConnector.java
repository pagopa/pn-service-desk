package it.pagopa.pn.service.desk.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
public class HttpConnector {

    private HttpConnector(){
        throw new IllegalCallerException("the constructor must not called");
    }

    public static Mono<PDDocument> downloadFile(String url) {

        log.info("Url to download: {}", url);

        Flux<DataBuffer> dataBufferFlux = WebClient.create()
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_PDF)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        return DataBufferUtils.join(dataBufferFlux)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new RandomAccessBuffer(bytes);
                })
                .map(randomAccess -> {
                    try {
                        return new PDFParser(randomAccess).getPDDocument();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


}
