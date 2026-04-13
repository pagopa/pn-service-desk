package it.pagopa.pn.service.desk.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpConnectorTest extends BaseTest.WithMockServer {

    private static ClientAndServer mockServer;

    @BeforeAll
    static void startMockServer() {
        mockServer = startClientAndServer(9998);
    }

    @AfterAll
    static void stopMockServer() {
        mockServer.stop();
    }

    @ExtendWith(MockitoExtension.class)
    @Test
    void testDownloadFile() {
        String path = "/filedownload.pdf";
        String fileUrl = "http://localhost:9998" + path;

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("GET")
                        .withPath(path))
                .respond(response()
                        .withStatusCode(200));


        assertDoesNotThrow(() -> HttpConnector.downloadFile (fileUrl));
    }

    @Test
    void testDownloadFile404NotFound() {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);
        Flux<DataBuffer> dataBufferFlux = Flux.just(
                createDataBufferWithBytes("testData1".getBytes()),
                createDataBufferWithBytes("testData2".getBytes()),
                createDataBufferWithBytes("testData3".getBytes())
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class)).thenReturn(dataBufferFlux);


        String url = "https://www.example.com/nonexistent.pdf";
        Mono<PDDocument> document = HttpConnector.downloadFile(url);

        StepVerifier.create(document)
                .expectErrorMatches(throwable ->
                        throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }
    @Test
    void testDownloadFileErrorRetriveFile() throws Exception {
        String path = "/filedownloadError.pdf";
        String fileUrl = "http://localhost:9998" + path;


        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("GET")
                        .withPath(path))
                .respond(response()
                        .withBody("[37, 80, 68, 70, 45, 49, 46, 52, 10, 37, 97, 176, 200, 208, 10, 109, 49, 32, 49, 32, 111, 98, 106, 10, 60, 60, 32]")
                        .withHeader("Content-Type", "application/pdf")
                        .withStatusCode(200));

        Mono<PDDocument> document = HttpConnector.downloadFile(fileUrl);

        StepVerifier.create(document)
                .expectErrorMatches(throwable ->
                        throwable instanceof PnGenericException &&
                                ((PnGenericException) throwable).getExceptionType().equals(ExceptionTypeEnum.ERROR_DURING_RECOVERING_FILE))
                .verify();
    }

    public static DataBuffer createDataBufferWithBytes(byte[] bytes) {
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        return bufferFactory.wrap(bytes);
    }

}
