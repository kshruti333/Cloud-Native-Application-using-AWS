AWS Infrastructure Scripts
Two Scripts created for setting up the Virtual Private Cloud (VPC) and related resources
csye6225-aws-networking-setup.sh script asks for CIDR block IP address and names of the resources.
Creates 3 subnets in three different availability zones, creates internet gateway and attaches IG to the VPC.
Public Route Table is created and all subnets created are attached  to the route table.
Public route in the public route table with destination CIDR block 0.0.0.0/0 and target as internet gateway.
Modified the default security group created add new rules to only allow TCP traffic on port 22 and 80 from 0.0.0.0/0


