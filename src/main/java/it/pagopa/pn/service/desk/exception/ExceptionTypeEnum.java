package it.pagopa.pn.service.desk.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypeEnum {

    BAD_REQUEST("BAD_REQUEST", "Campi obbligatori mancanti."),
    FILE_KEY_NOT_EXISTED("FILE_KEY_NOT_EXISTED", "File key non esistente"),
    MAPPER_ERROR("MAPPER_ERROR", "Non è stato possibile mappare l'oggetto richiesto"),
    ADDRESS_IS_NOT_VALID("ADDRESS_IS_NOT_VALID", "l'indirizzo non è valido");

    private final String title;
    private final String message;


    ExceptionTypeEnum(String title, String message) {
        this.title = title;
        this.message = message;
    }
}
