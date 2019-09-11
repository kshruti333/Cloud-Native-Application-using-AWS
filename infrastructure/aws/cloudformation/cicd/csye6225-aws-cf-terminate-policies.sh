#!/bin/bash
echo Enter stack name to be deleted
read sn
#aws cloudformation wait stack-exists $sn
{
  aws cloudformation describe-stacks --stack-name $sn &&
  echo stack "$sn" found
} || {
  echo cannot find stack "$sn".Exiting Script
  exit 1
}
#aws cloudformation delete stack
{
    aws cloudformation delete-stack --stack-name $sn &&
    echo Deleting stack "$sn". Please wait...
    resp=$(aws cloudformation wait stack-delete-complete --stack-name $sn)
    if [ $? -eq 0 ]
    then
            echo Stack "$sn" sucessfully terminated
    else
      echo "$resp"
      exit 1
    fi

} || {
  echo Cannot delete stack "$sn"
  exit 1
}
