##Description: 
csye6225-cf-networking.json file created to setup networking resources. 
Parameters are given in the csye6225-aws-cf-networkparameters.json file.
csye6225-aws-cf-networking-setup.sh file created to configure networking resources using AWS CloudFormation.
Script will first validate the template then stack gets created, script will wait till stack gets created and finally prints stack creation sucess or failure message.
csye6225-aws-cf-terminate-stack.sh file is created which will first find the stack and then delete the stack which the user wants to.

##Resources created: 
1) Virtual Private Cloud
2) 3 subnets created 
3) Internet gateway
4) Router
5) Route Tables

To run script :-

1) chmod 777 <script_name.sh> 
2) ./<script_name.sh>

