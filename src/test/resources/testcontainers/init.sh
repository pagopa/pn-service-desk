echo "### CREATE PN-SERVICE-DESK QUEUES ###"

queues="local-service-desk-safestorage-inputs"

for qn in  $( echo $queues | tr " " "\n" ) ; do

    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn


done

echo "### CREATE PN-SERVICE-DESK TABLES ###"


echo "Initialization terminated"
