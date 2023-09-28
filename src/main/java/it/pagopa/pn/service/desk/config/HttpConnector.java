package it.pagopa.pn.service.desk.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
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
        log.info("Url to download: {}", url);
        try {
            return WebClient
                    .builder()
                    .codecs(codecs ->
                            codecs.defaultCodecs()
                                    .maxInMemorySize(-1)
                    )
                    .build()
                    .get()
                    .uri(new URI(url))
                    .accept(MediaType.APPLICATION_PDF)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .flatMap(bytes -> {
                        try {
                            return Mono.just(PDDocument.load(bytes));
                        } catch (IOException e) {
                            return Mono.error(e);
                        }
                    });
        } catch (URISyntaxException e) {
            return Mono.error(e);
        }
    }
}
