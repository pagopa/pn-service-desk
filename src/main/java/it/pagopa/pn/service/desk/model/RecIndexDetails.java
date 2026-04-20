package it.pagopa.pn.service.desk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecIndexDetails {

    @JsonProperty("recIndex")
    private Integer recIndex;

}
