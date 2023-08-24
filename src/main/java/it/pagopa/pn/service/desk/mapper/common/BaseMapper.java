package it.pagopa.pn.service.desk.mapper.common;

public interface BaseMapper<E, D>{

    E toEntity(D dto);
    D toDTO(E entity);

}
