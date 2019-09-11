echo Enter Stack name
#read stack name
read sn
#create stack
{
  validresp=$(aws cloudformation validate-template --template-body file://lambdafunction.json) &&
  echo "Template validated"
} || {
  echo "$validresp"
  echo "Invalid Template"
  exit 1
}


DOMAIN_NAME1=$(aws route53 list-hosted-zones --query HostedZones[0].Name --output text) 
CD_DOMAIN="code-deploy."${DOMAIN_NAME1%?}
echo $CD_DOMAIN

DOMAINNAME2=${DOMAIN_NAME1%?}
echo $DOMAINNAME2

export circleciuser=circleci

AccountId=$(aws iam get-user|python -c "import json as j,sys;o=j.load(sys.stdin);print o['User']['Arn'].split(':')[4]")
echo "AccountId: $AccountId"

SNSTOPIC_ARN="arn:aws:sns:us-east-1:$AccountId:SNSTopicResetPassword"
echo "SNSTOPIC_ARN: $SNSTOPIC_ARN"

createres=$(aws cloudformation create-stack  --stack-name $sn --capabilities CAPABILITY_NAMED_IAM --template-body file://lambdafunction.json  --parameters ParameterKey=Bucket,ParameterValue=$CD_DOMAIN ParameterKey=SNSTOPICARN,ParameterValue=$SNSTOPIC_ARN ParameterKey=DOMAINNAME,ParameterValue=$DOMAINNAME2 ParameterKey=circleci,ParameterValue=$circleciuser)
echo Creating stack "$sn". Please wait...
resp=$(aws cloudformation wait stack-create-complete --stack-name $sn)
if [[ -z "$resp" ]]; then
  echo Stack "$sn" sucessfully created
else
  echo "$resp"
  exit 1
fi
