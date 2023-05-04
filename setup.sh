# clone the git repo
git clone

#install pulumi
curl -fsSL https://get.pulumi.com | sh

#create a local state file directory
mkdir oci-stack-statefile
pulumi login file://oci-stack-statefile

#pulumi stack select
pulumi stack select prod

read -p "Enter your name : " name
echo "Hi, $name. Let us be friends!"

read -p "Please provide your OCI region: " oci_region
echo    # (optional) move to a new line
if [[  -z  "$oci_region" ]]
then
pulumi config set oci:region $oci_region
fi

read -p "Please provide your tenancy OCID: " tenancyOcid
echo    # (optional) move to a new line
if [[  -z  "$tenancyOcid" ]]
then
pulumi config set oci:tenancyOcid $tenancyOcid --secret
fi

read -p "Please provide your userOcid: " userOcid
echo    # (optional) move to a new line
if [[  -z  "$userOcid" ]]
then
pulumi config set oci:userOcid  --secret
fi

read -p "Please provide your oci fingerprint: " oci_fingerprint
echo    # (optional) move to a new line
if [[  -z  "$oci_fingerprint" ]]
then
pulumi config set oci:fingerprint $oci_fingerprint --secret
fi

read -p "Please provide parent compartment ocid: " compartment_ocid
echo    # (optional) move to a new line
if [[  -z  "$compartment_ocid" ]]
then
pulumi config set compartment_ocid $compartment_ocid
fi

echo "Create cloudkey ssh keys at ~/.ssh"
ssh-keygen -b 2048 -t rsa -f cloudkey
ls ~/.ssh

cat ~/.ssh/cloudkey.pub | pulumi config set sshKey
pulumi config set compartment_ocid ~/.ssh/cloudkey


read -p "Please provide the number of compute instances to create: " amount_vm
echo    # (optional) move to a new line
if [[  -z  "$amount_vm" ]]
then
pulumi config set amount_vm $amount_vm
fi

