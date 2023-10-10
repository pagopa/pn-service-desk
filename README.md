# pn-service-desk

Per triggerare il flusso asincrono di <b>Validation</b> è necessario effettuare le seguenti azioni:
- Rimuovere l'array di attachments presente nell'entità <i>pn-ServiceDeskOperations</i> e relativo all'operationId di interesse
- Pushare il seguente evento su coda interna<br>
<i>awslocal sqs send-message --queue-url  http://localstack:4566/000000000000/internal-queue --message-attributes "{\"eventType\":{\"DataType\": \"String\", \"StringValue\":\"VALIDATION_OPERATIONS_EVENTS\"}}" --message-body "{\"operationId\": \"operationId\"}" </i>