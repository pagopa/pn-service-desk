package it.pagopa.pn.service.desk.encryption.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EncryptionModel {

    private String keyId;
    private String algorithm;
}
