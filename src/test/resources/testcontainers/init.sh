echo "### CREATE QUEUES ###"
queues="local-service-desk-safestorage-inputs local-service-desk-internal local-delivery-push-inputs"
for qn in  $( echo $queues | tr " " "\n" ) ; do
    echo creating queue $qn ...
    aws --profile default --region us-east-1 --endpoint-url http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn
done

echo " - Create pn-paper-channel TABLES"

echo "### CREATE PN-SERVICE-DESK TABLES ###"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name ServiceDeskOperationsDynamoTable \
    --attribute-definitions \
        AttributeName=operationId,AttributeType=S \
        AttributeName=recipientInternalId,AttributeType=S \
    --key-schema \
        AttributeName=operationId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"recipientInternalId-index\",
            \"KeySchema\": [{\"AttributeName\":\"recipientInternalId\",\"KeyType\":\"HASH\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"



aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name ServiceDeskAddress  \
    --attribute-definitions \
        AttributeName=operationId,AttributeType=S \
    --key-schema \
        AttributeName=operationId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name ServiceDeskOperationFileKey \
    --attribute-definitions \
        AttributeName=fileKey,AttributeType=S \
		AttributeName=operationId,AttributeType=S \
    --key-schema \
        AttributeName=fileKey,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"operationId-index\",
            \"KeySchema\": [{\"AttributeName\":\"operationId\",\"KeyType\":\"HASH\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name ClientDynamoTable \
    --attribute-definitions \
        AttributeName=apiKey,AttributeType=S \
    --key-schema \
        AttributeName=apiKey,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \

aws  --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb put-item \
    --table-name ClientDynamoTable  \
    --item '{"apiKey":{"S":"ZenDesk"},"clientId":{"S":"clientId"}}'


echo "Initialization terminated"