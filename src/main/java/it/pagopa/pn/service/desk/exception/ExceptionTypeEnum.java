package it.pagopa.pn.service.desk.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypeEnum {

    BAD_REQUEST("BAD_REQUEST", "Campi obbligatori mancanti."),
    FILE_KEY_NOT_EXISTED("FILE_KEY_NOT_EXISTED", "File key non esistente"),
    MAPPER_ERROR("MAPPER_ERROR", "Non è stato possibile mappare l'oggetto richiesto"),
    DATA_VAULT_DECRYPTION_ERROR("DATA_VAULT_DECRYPTION_ERROR", "Anonymization service not available "),
    DOCUMENT_URL_NOT_FOUND("DOCUMENT_URL_NOT_FOUND", "Url allegato non disponibile"),
    ADDRESS_IS_NOT_VALID("ADDRESS_IS_NOT_VALID", "l'indirizzo non è valido"),
    OPERATION_ID_IS_PRESENT("OPERATION_ID_IS_PRESENT", "The operation id is already present for the ticket id "),
    OPERATION_IS_NOT_PRESENT("OPERATION_IS_NOT_PRESENT", "L'operation non è presente"),
    ERROR_CONTENT_TYPE("ERROR_CONTENT_TYPE", "contentType non valido"),
    ERROR_DURING_VIDEO_UPLOAD("ERROR_DURING_VIDEO_UPLOAD", "Errore durante l'upload del video"),
    ERROR_PRESIGNED_URL_VIDEO_UPLOAD("ERROR_PRESIGNED_URL_UPLOAD", "ERROR_PRESIGNED_URL_UPLOAD"),
    ERROR_DURING_RECOVERING_FILE("ERROR_DURING_RECOVERING_FILE", "Errore durante il recupero del file"),
    ERROR_DURING_PAPER_SEND("ERROR_DURING_PAPER_SEND", "Errore durante la chiamata send di paperchannel "),
    SAFE_STORAGE_FILE_LOADING("SAFE_STORAGE_FILE_LOADING", "Caricamento in corso"),
    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "The searched entity is not present in the Database "),
    PAPERCHANNEL_STATUS_CODE_EMPTY("PAPERCHANNEL_STATUS_CODE_EMPTY", "Lo status code inviato dal microservizio Paperchannel è mancante"),
    NO_ATTACHMENT_AVAILABLE("NO_ATTACHMENT_AVAILABLE","There are no attachments available for this operation "),
    ADDRESS_IS_NOT_PRESENT("ADDRESS_IS_NOT_PRESENT", "L'indirizzo non è presente"),
    NO_UNREACHABLE_NOTIFICATION("NO_UNREACHABLE_NOTIFICATION", "There are no unreachable notification for the taxId provided "),
    ERROR_ON_RADD_INQUIRY("ERROR_ON_RADD_INQUIRY", "An error occurred while calling the service inquiry api "),
    ERROR_SAFE_STORAGE_BODY_NULL("ERROR_SAFE_STORAGE_BODY_NULL", "Safe Storage response is full for this file key "),
    ERROR_ON_UPDATE_ENTITY("ERROR_ON_UPDATE_ETITY", "An error occurred while updating the entity "),
    ERROR_ON_UPDATE_ETITY("ERROR_ON_UPDATE_ETITY", "Errore durante l'update"),
    API_KEY_EMPTY("API_KEY_EMPTY", "Non è stato inserito alcun valore per l'header x-api-key"),
    API_KEY_NOT_PRESENT("API_KEY_NOT_PRESENT", "ApiKey non censito."),
    ERROR_ON_DELIVERY_CLIENT("ERROR_ON_DELIVERY_CLIENT", "ERROR_ON_DELIVERY_CLIENT"),
    ERROR_ON_SEND_PAPER_CHANNEL_CLIENT("ERROR_ON_SEND_PAPER_CHANNEL_CLIENT", "ERROR_ON_SEND_PAPER_CHANNEL_CLIENT"),
    ERROR_ON_DELIVERY_PUSH_CLIENT("ERROR_ON_DELIVERY_PUSH_CLIENT", "ERROR_ON_DELIVERY_PUSH_CLIENT"),
    ERROR_GET_UNREACHABLE_NOTIFICATION("ERROR_GET_UNREACHABLE_NOTIFICATION","ERROR_GET_UNREACHABLE_NOTIFICATION"),
    ERROR_ON_ADDRESS_MANAGER_CLIENT("ERROR_ON_ADDRESS_MANAGER_CLIENT", "ERROR_ON_ADDRESS_MANAGER_CLIENT");

    private final String title;
    private final String message;


    ExceptionTypeEnum(String title, String message) {
        this.title = title;
        this.message = message;
    }
}
