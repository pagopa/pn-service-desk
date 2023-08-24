package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadResponse;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;

public class OperationsFileKeyMapper {

    public static PnServiceDeskOperationFileKey getOperationFileKey (String fileKey, String operationId){

        PnServiceDeskOperationFileKey operationFileKey = new PnServiceDeskOperationFileKey();

        operationFileKey.setFileKey(fileKey);
        operationFileKey.setOperationId(operationId);

        return operationFileKey;
    }

    public static VideoUploadResponse getVideoUpload (FileCreationResponse fileCreationResponse){

        VideoUploadResponse videoUploadResponse = new VideoUploadResponse();

        videoUploadResponse.setUrl(fileCreationResponse.getUploadUrl());
        videoUploadResponse.setFileKey(fileCreationResponse.getKey());
        videoUploadResponse.setSecret(fileCreationResponse.getSecret());

        return videoUploadResponse;
    }
}
