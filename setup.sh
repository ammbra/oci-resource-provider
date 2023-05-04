read -p "Enter your name : " name
echo "Hi, $name. Let us be friends!"

read -p "Please provide your OCI region: "  -n 1 -r
echo    # (optional) move to a new line
if [[  -z  "$REPLY" ]]
then
pulumi config set oci:region $REPLY
fi

read -p "Please provide your tenancy OCID: "  -n 1 -r
echo    # (optional) move to a new line
if [[  -z  "$REPLY" ]]
then
pulumi config set oci:tenancyOcid $REPLY --secret
fi

read -p "Please provide your userOcid: "  -n 1 -r
echo    # (optional) move to a new line
if [[  -z  "$REPLY" ]]
then
pulumi config set oci:userOcid  --secret
fi

read -p "Please provide your oci fingerprint: "  -n 1 -r
echo    # (optional) move to a new line
if [[  -z  "$REPLY" ]]
then
pulumi config set oci:fingerprint $REPLY --secret
fi

read -p "Please provide parent compartment ocid: "  -n 1 -r
echo    # (optional) move to a new line
if [[  -z  "$REPLY" ]]
then
pulumi config set compartment_ocid $REPLY
fi

echo "Create cloudkey ssh keys at ~/.ssh"
ssh-keygen -b 2048 -t rsa -f cloudkey
ls ~/.ssh

cat ~/.ssh/cloudkey.pub | pulumi config set sshKey
pulumi config set compartment_ocid ~/.ssh/cloudkey

pulumi preview
pulumi up
read -p "Please provide the number of compute instances to create: " -n 1 -r
echo    # (optional) move to a new line
if [[  -z  "$REPLY" ]]
then
pulumi config set amount_vm $REPLY
fi