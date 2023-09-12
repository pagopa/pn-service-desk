package it.pagopa.pn.service.desk.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentMapperTest {

    @Test
    void initAttachment() {
        assertNotNull(AttachmentMapper.initAttachment("1234"));
    }
}