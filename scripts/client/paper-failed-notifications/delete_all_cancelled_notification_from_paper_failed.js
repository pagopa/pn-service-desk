const AWS = require('aws-sdk');
const arguments = process.argv ;
  
if(arguments.length<=2){
  console.error("Specify AWS profile as argument")
  process.exit(1)
}

const awsProfile = arguments[2]

console.log("Using profile "+awsProfile)

let credentials = null

process.env.AWS_SDK_LOAD_CONFIG=1
if(awsProfile.indexOf('sso_')>=0){ // sso profile
  credentials = new AWS.SsoCredentials({profile:awsProfile});
  AWS.config.credentials = credentials;
} else { // IAM profile
  credentials = new AWS.SharedIniFileCredentials({profile: awsProfile});
  AWS.config.credentials = credentials;
}
AWS.config.update({region: 'eu-south-1'});


const docClient = new AWS.DynamoDB.DocumentClient();
const TABLE_NAME_TIMELINES = "pn-NotificationsMetadata";
const TABLE_NAME_PAPER_FAILED = "pn-PaperNotificationFailed";

const params = {
  TableName: TABLE_NAME_TIMELINES,
  FilterExpression: "notificationStatus = :notificationStatus",
  ExpressionAttributeValues: {
    ":notificationStatus": "CANCELLED",
  },
};

docClient.scan(params, onScan);
var count = 0;

function onScan(err, data) {
  if (err) {
    console.error("Unable to scan the table. Error JSON:", JSON.stringify(err, null, 2));
  } else {
    // console.log("Scan succeeded.");
    data.Items.forEach(function(itemdata) {
      console.log("Item :", ++count,JSON.stringify(itemdata));
      const iun_recipient = itemdata['iun_recipientId'];
      const iun_recipient_split = iun_recipient.split("##");
      const iun = iun_recipient_split[0];
      const recipientId = iun_recipient_split[1];
      deletePaperFailed(recipientId, iun);
    });

    // continue scanning if we have more items
    if (typeof data.LastEvaluatedKey != "undefined") {
      // console.log("Scanning for more...");
      params.ExclusiveStartKey = data.LastEvaluatedKey;
      docClient.scan(params, onScan);
    }
  }
}

function deletePaperFailed(recipientId, iun) {
  const params = {
    TableName: TABLE_NAME_PAPER_FAILED,
    Key: {
      'recipientId': recipientId,
      'iun': iun,
    }
  };

  console.log("Deleting iun :", iun, "recipient: ", recipientId);
  docClient.delete(params, function(err, data) {
    if (err) {
      console.log("Error", err);
    } else {
      console.log("Success", data);
    }
  });
}

