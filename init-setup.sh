#install pulumi
curl -fsSL https://get.pulumi.com | sh

#create a local state file directory
mkdir oci-stack-statefile
pulumi login file://oci-stack-statefile

#pulumi stack select
pulumi new https://github.com/ammbra/oci-resource-provider.git --force

sh setup.sh