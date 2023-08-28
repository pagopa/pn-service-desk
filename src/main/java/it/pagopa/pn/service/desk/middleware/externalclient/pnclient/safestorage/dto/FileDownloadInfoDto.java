/*
 * SafeStorage
 * __SafeStorage__: memorizza e __archivia a fini legali file non modificabili__.  Opzionalmente appone firma digitale e marcatura temporale. #### Elenco casi d'uso da soddisfare:   - Upload degli allegati di una notifica, già firmati dalle PA, da mantenere per 120gg. <br/>   - Upload, firma digitale e marcatura temporale degli atti opponibili a terzi.      Conservare 10 anni. <br/>   - Upload Avvisi di Avvenuta Ricezione. <br />   - Upload dei documenti digitali generati da pn-external-channels durante la consegna di messaggi      digitali e copie conformi dei documenti generati durante la consegna di corrispondenza cartacea.     Questi documenti vanno conservati 10 anni <br />   - Download dei file e verifica dei loro checksum. <br />   - In caso di contenzioso bisogna allungare il periodo di retention per file specifici.  #### Requisiti non funzionali:   - I file contenuti nel _SafeStorage_ devono essere \"reccuperabili\" in caso di disastro di      una regione. <br/>   - I file possono essere di dimensioni fino a 1GB, solitamente nell'ordine di 1MB ad eccezione dei     documenti allegati alle notifiche che spesso arrivano ai 50MB. <br/>   - SafeStorage deve garantire la validità della marcatura temporale nel tempo. <br/>  #### Parametri di runtime:   - pst: il tempo (secondi o minuti) in cui un presigned URL rimane valido.   - stayHotTime: quanto tempo un file che era stato archiviato e poi richiamato rimane \"hot\"     prima di essere nuovamente archiviato.  #### Operazioni da invocare per l'archiviazione di un nuovo file.   1. Invocare POST al path '/safe-storage/v1/files' (operation id: createFile ) <br/>   2. Invocare PUT sul presigned URL ottenuto (operation id: uploadFileContent ) <br/>   3. Quando il file è stato caricato e firmato/marcato verrà inserito un messaggio in una coda       SQS specifica per il client che ha richiesto l'operazione.  #### Operazioni da invocare per la lettura di un file esistente.   1. Invocare GET al path '/safe-storage/v1/files/{fileKey}' (operation id: getFile ) <br/>   2. Il file può essere hot (pronto al download) o cold (minuti o ore per il recupero del file)<br/>   3.      1. Se il fle è _hot_ la risposta contiene un url di download da usare entro ```pst``` (tempo          validità presigned url); `pst` è parametro di installazione di SafeStorage.      2. Se il file è _cold_ la risposta contiene un indicazione del tempo necessario a          renderlo _hot_. <br/>         Solo in questo caso, quando il file sarà pronto, verrà inserito un messaggio in una coda          SQS specifica per il client che ha richiesto l'operazione. Tale messaggio conterrà l'URL         di download.   4. L'url di download può essere utilizzato, entro la sua scadenza, per recuperare il contenuto       del file.
 *
 * The version of the OpenAPI document: v1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * FileDownloadInfoDto
 */
@JsonPropertyOrder({
  FileDownloadInfoDto.JSON_PROPERTY_URL,
  FileDownloadInfoDto.JSON_PROPERTY_RETRY_AFTER
})
@JsonTypeName("FileDownloadInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-08-04T17:16:41.599594600+02:00[Europe/Rome]")
@lombok.ToString
public class FileDownloadInfoDto {
  public static final String JSON_PROPERTY_URL = "url";
  private String url;

  public static final String JSON_PROPERTY_RETRY_AFTER = "retryAfter";
  private BigDecimal retryAfter;

  public FileDownloadInfoDto() { 
  }

  public FileDownloadInfoDto url(String url) {
    
    this.url = url;
    return this;
  }

   /**
   * URL preautorizzato a cui effettuare una richiesta GET per ottenere il  contenuto del documento. Presente solo se il documento è pronto per il download.
   * @return url
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "URL preautorizzato a cui effettuare una richiesta GET per ottenere il  contenuto del documento. Presente solo se il documento è pronto per il download.")
  @JsonProperty(JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getUrl() {
    return url;
  }


  @JsonProperty(JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUrl(String url) {
    this.url = url;
  }


  public FileDownloadInfoDto retryAfter(BigDecimal retryAfter) {
    
    this.retryAfter = retryAfter;
    return this;
  }

   /**
   * Stima del numero di secondi da aspettare prima che il contenuto del  documento sia scaricabile.
   * @return retryAfter
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "Stima del numero di secondi da aspettare prima che il contenuto del  documento sia scaricabile.")
  @JsonProperty(JSON_PROPERTY_RETRY_AFTER)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public BigDecimal getRetryAfter() {
    return retryAfter;
  }


  @JsonProperty(JSON_PROPERTY_RETRY_AFTER)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRetryAfter(BigDecimal retryAfter) {
    this.retryAfter = retryAfter;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileDownloadInfoDto fileDownloadInfo = (FileDownloadInfoDto) o;
    return Objects.equals(this.url, fileDownloadInfo.url) &&
        Objects.equals(this.retryAfter, fileDownloadInfo.retryAfter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, retryAfter);
  }

}

