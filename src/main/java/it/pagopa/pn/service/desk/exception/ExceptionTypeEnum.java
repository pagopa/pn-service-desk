package it.pagopa.pn.service.desk.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypeEnum {

    BAD_REQUEST("BAD_REQUEST", "Campi obbligatori mancanti."),
    FILE_KEY_NOT_EXISTED("FILE_KEY_NOT_EXISTED", "File key non esistente"),
    MAPPER_ERROR("MAPPER_ERROR", "Non è stato possibile mappare l'oggetto richiesto"),
    DATA_VAULT_DECRYPTION_ERROR("DATA_VAULT_DECRYPTION_ERROR", "Servizio di anonimizzazione non disponibile."),
    DOCUMENT_URL_NOT_FOUND("DOCUMENT_URL_NOT_FOUND", "Url allegato non disponibile"),
    ADDRESS_IS_NOT_VALID("ADDRESS_IS_NOT_VALID", "l'indirizzo non è valido"),
    OPERATION_ID_IS_PRESENT("OPERATION_ID_IS_PRESENT", "L'operation id è già presente per quel tiket id"),
    OPERATION_IS_NOT_PRESENT("OPERATION_IS_NOT_PRESENT", "L'operation non è presente"),
    ERROR_DURING_RECOVERING_FILE("ERROR_DURING_RECOVERING_FILE", "Errore durante il recupero del file"),
    SAFE_STORAGE_FILE_LOADING("SAFE_STORAGE_FILE_LOADING", "Caricamento in corso"),
    ADDRESS_IS_NOT_PRESENT("ADDRESS_IS_NOT_PRESENT", "L'indirizzo non è presente"),
    ERROR_ON_UPDATE_ETITY("ERROR_ON_UPDATE_ETITY", "Errore durante l'update"),
    ERROR_ON_DELIVERY_CLIENT("ERROR_ON_DELIVERY_CLIENT", "ERROR_ON_DELIVERY_CLIENT"),
    ERROR_ON_SEND_PAPER_CHANNEL_CLIENT("ERROR_ON_SEND_PAPER_CHANNEL_CLIENT", "ERROR_ON_SEND_PAPER_CHANNEL_CLIENT"),
    ERROR_ON_DELIVERY_PUSH_CLIENT("ERROR_ON_DELIVERY_PUSH_CLIENT", "ERROR_ON_DELIVERY_PUSH_CLIENT"),
    ERROR_ON_ADDRESS_MANAGER_CLIENT("ERROR_ON_ADDRESS_MANAGER_CLIENT", "ERROR_ON_ADDRESS_MANAGER_CLIENT");

    private final String title;
    private final String message;


    ExceptionTypeEnum(String title, String message) {
        this.title = title;
        this.message = message;
    }
}
