package it.pagopa.pn.service.desk.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypeEnum {
    ESTIMATE_NOT_EXISTED("ESTIMATE_NOT_EXISTED", "La stima non è presente nel sistema"),
    REFERENCE_MONTH_NOT_CORRECT("REFERENCE_MONTH_NOT_CORRECT", "Il mese di riferimento non è nel formato corretto"),
    PA_ID_NOT_EXIST("PA_ID_NOT_EXIST", "La pa non è presente a sistema"),
    ON_BOARDING_DATE_INCOMPATIBLE("ON_BOARDING_DATE_INCOMPATIBLE","Data di onboarding della PA non compatibile"),
    BILLING_NOT_EXIST("BILLING_NOT_EXIST", "Fatturazione non presente a sistema"),
    OPERATION_NOT_ALLOWED("OPERATION_NOT_ALLOWED", "Operazione non consentita"),
    ESTIMATE_EXPIRED("ESTIMATE_EXPIRED", "Stima scaduta"),
    REFERENCE_YEAR_NOT_CORRECT("REFERENCE_YEAR_NOT_CORRECT", "L'anno di riferimento non è nel formato corretto"),
    PROFILATION_EXPIRED("PROFILATION_EXPIRED", "Profilazione scaduta"),
    PROFILATION_NOT_EXISTED("PROFILATION_NOT_EXISTED", "La profilazione non è presente nel sistema"),
    FUTURE_PROFILATION_NOT_EXIST("FUTURE_PROFILATION_NOT_EXIST", "Profilazione futura non esistente"),
    REF_YEAR_NOT_VALID("REF_YEAR_NOT_VALID", "L'anno di riferimento non è valido." ),
    BAD_REQUEST("BAD_REQUEST", "Campi obbligatori mancanti."),
    FILE_KEY_NOT_EXISTED("FILE_KEY_NOT_EXISTED", "File key non esistente"),
    MAPPER_ERROR("MAPPER_ERROR", "Non è stato possibile mappare l'oggetto richiesto"),
    REPORT_NOT_EXISTS("REPORT_NOT_EXISTS", "Il report non esiste"),
    STATUS_NOT_READY("STATUS_NOT_READY", "Il file non è pronto"),
    STATUS_NOT_IN_ERROR("STATUS_NOT_IN_ERROR", "Il report non è in uno stato di errore"),
    STATUS_NOT_CORRECT("STATUS_NOT_CORRECT", "Lo stato passato non è corretto"),
    INCONSISTENT_STATUS("INCONSISTENT_STATUS", "Lo stato della stima si trova in uno stato non consistente"),
    SCHEDULE_JOB_ERROR("SCHEDULE_JOB_ERROR", "Errore nella schedulazione del job"),

    DATA_VAULT_ENCRYPTION_ERROR("DATA_VAULT_ENCRYPTION_ERROR", "Servizio irraggiungibile od errore in fase di criptazione"),
    DATA_VAULT_DECRYPTION_ERROR("DATA_VAULT_DECRYPTION_ERROR", "Servizio irraggiungibile od errore in fase di decriptazione"),
    DEANONIMIZING_JOB_EXCEPTION("DEANONIMIZING_JOB_EXCEPTION", "Errore durante il flusso di deanonimizzazione");

    private final String title;
    private final String message;


    ExceptionTypeEnum(String title, String message) {
        this.title = title;
        this.message = message;
    }
}
