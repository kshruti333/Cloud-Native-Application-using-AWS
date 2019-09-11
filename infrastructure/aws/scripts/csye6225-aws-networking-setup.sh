#!/bin/bash

#Creating VPC
echo "Enter cidr-block in format. Ex: 191.130.30.30/16"
read cidr


{
	vpcid=$(aws ec2 create-vpc --cidr-block $cidr | jq -r '.Vpc.VpcId') &&
	echo $vpcid
} || {
	echo "VPC creation failed."
	exit 1
}

echo "Enter the stack name i.e name of the VPC"
read stackName
{
	aws ec2 create-tags --resources $vpcid --tags Key=Name,Value=$stackName &&
	echo VPC tagged "$stackName"
} || {
	echo "Failure in tagging VPC exiting script"
	exit 1
}

subNetCidrBlock1="191.130.31.30/24"
subNetCidrBlock2="191.130.32.30/24"
subNetCidrBlock3="191.130.33.30/24"

availabilityZone1="us-east-1a"
availabilityZone2="us-east-1b"
availabilityZone3="us-east-1c"



echo "Creating Public Subnet1..."
subnet_response1=$(aws ec2 create-subnet --cidr-block "$subNetCidrBlock1" --availability-zone "$availabilityZone1" --vpc-id "$vpcid" --output json)
subnetId1=$(echo -e "$subnet_response1" |  /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
echo SubnetID 1 is "$subnetId1" 

echo "Creating Public Subnet2..."
subnet_response2=$(aws ec2 create-subnet --cidr-block "$subNetCidrBlock2" --availability-zone "$availabilityZone2" --vpc-id "$vpcid" --output json)
subnetId2=$(echo -e "$subnet_response2" |  /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
echo Subnet Id 2 is "$subnetId2"

echo "Creating Public Subnet3..."
subnet_response3=$(aws ec2 create-subnet --cidr-block "$subNetCidrBlock3" --availability-zone "$availabilityZone3" --vpc-id "$vpcid" --output json)
subnetId3=$(echo -e "$subnet_response3" |  /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
echo Subnet id 3 is "$subnetId3"


##Create Internet Gateway
{
	gateid=$(aws ec2 create-internet-gateway | jq -r '.InternetGateway.InternetGatewayId') &&
	echo Gateway created with gateid "$gateid"
} || {
	echo "Gateway creation failed exiting script"
	exit 1
}

echo "Enter the Internet gateway name"
read gateWayName
{
	aws ec2 create-tags --resources $gateid --tags Key=Name,Value=$gateWayName &&
	echo Gateway tagged "$gateWayName"
} || {
	echo "Error in tagging gateway exiting script"
	exit 1
}

# attaching the gateway
{
	aws ec2 attach-internet-gateway --internet-gateway-id $gateid  --vpc-id $vpcid
	echo "Attached internet gateway"
} || {
	echo "Error attaching internet gateway. Exiting Script"
	exit 1
}
#creating route table
{
	rid=$(aws ec2 create-route-table --vpc-id $vpcid | jq -r '.RouteTable.RouteTableId') &&
	echo Route table created with id: "$rid"
} || {
	echo "Error creating route table. Exiting script"
	exit 1
}

#Creating Route
echo "Enter route table name"
read rtabName
{
	aws ec2 create-tags --resources $rid --tags Key=Name,Value=$rtabName &&
	echo Route table "$rtabName" created
} || {
	echo "Error creating Route table. Exiting Script"
	exit 1

}

#add route to subnet
echo "Adding subnets to route table..."
associate_response1=$(aws ec2 associate-route-table --route-table-id "$rid" --subnet-id "$subnetId1")
 
echo "$associate_response1"
associate_response2=$(aws ec2 associate-route-table --route-table-id "$rid" --subnet-id "$subnetId2")
associate_response3=$(aws ec2 associate-route-table --route-table-id "$rid" --subnet-id "$subnetId3")

echo "$associate_response2"
echo "$associate_response3"

echo "All three subnets added"


echo "Enter the destination for Route in format ex: 0.0.0.0/0"
read dest

result=$(aws ec2 create-route --route-table-id $rid --destination-cidr-block $dest --gateway-id $gateid)
# | jq -r '.Return')
echo "Attached"
#if [ $result == "true" ]
#then
#	 	echo "Script executed sucessfully"
#else
 #		echo "Network not created! Try Again."
#fi

#aws ec2 update-security-group-rule-descriptions-ingress --group-id | jq -r '..InternetGatewayId') --ip-permissions '[{"IpProtocol": "tcp", "FromPort": 22, "ToPort": 22, "IpRanges": [{"CidrIp": "203.0.113.0/16", "Description": "SSH access from ABC office"}]}]'



echo "Modifying Default Security GRoup Rules...."
temp=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values=${vpcid})
sgid=$(echo -e "$temp" | jq '.SecurityGroups[0].GroupId' | tr -d '"')
aws ec2 revoke-security-group-ingress --group-id $sgid --protocol "-1" --port -1 --source-group $sgid
aws ec2 revoke-security-group-egress --group-id $sgid --protocol "-1" --port -1 --cidr 0.0.0.0/0




result1=$(aws ec2 authorize-security-group-ingress --group-id $sgid --protocol tcp --port 22 --cidr 0.0.0.0/0)
result2=$(aws ec2 authorize-security-group-ingress --group-id $sgid --protocol tcp --port 80 --cidr 0.0.0.0/0) 
echo "$result1" 
echo "$result2"
echo "Updated the default security group rules" 

#if [ $result1 ] && [ $result2 ]
#then
#                echo "Script executed sucessfully"
#else
#                echo "Network not created! Try Again."
#fi

