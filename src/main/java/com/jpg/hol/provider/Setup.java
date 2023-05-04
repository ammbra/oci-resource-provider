package com.jpg.hol.provider;

import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.oci.Core.*;
import com.pulumi.oci.Core.inputs.*;
import com.pulumi.oci.Identity.Compartment;
import com.pulumi.oci.Identity.CompartmentArgs;
import com.pulumi.oci.Identity.IdentityFunctions;
import com.pulumi.oci.Identity.inputs.GetAvailabilityDomainsArgs;

import java.util.Map;


public class Setup {
    public static void main(String[] args) {
        Pulumi.run(ctx -> {
            var config = ctx.config();

            Compartment holCompartment = new Compartment("hol-compartment",
                    CompartmentArgs.builder()
                            .enableDelete(true)
                            .compartmentId(config.get("compartment_ocid").get())
                            .description("A compartment dedicated to running hands on labs").build()
            );

            ctx.export("Child compartment has id ", holCompartment.id());
            //create vcn
            Vcn vcn = Network.createVcn(config, holCompartment.id());

            //create an internet gateway
            InternetGateway internetGateway = Network.createInternetGateway(config,vcn, holCompartment.id());

            //create routetable
            RouteTable routeTable= Network.createRoutetable(config,vcn,internetGateway, holCompartment.id());

            //create a subnet within the vcn
            Subnet subnet = Network.createSubnet(config, vcn, routeTable, holCompartment.id());

            var firstAvailabilityDomain = IdentityFunctions.getAvailabilityDomains(GetAvailabilityDomainsArgs.builder()
                    .compartmentId(holCompartment.compartmentId())
                    .build()).applyValue(result -> result.availabilityDomains().get(0).name());

            var firstShape = CoreFunctions.getShapes(GetShapesArgs.builder().compartmentId(holCompartment.id())
                            .availabilityDomain(firstAvailabilityDomain)
                    .build()).applyValue(result -> result.shapes().stream().filter(res -> res.name().contains("VM.Standard.B1.1")).findFirst().get().name());


            var compatibleImage = CoreFunctions.getImages(GetImagesArgs.builder()
                    .compartmentId(holCompartment.id())
                    .operatingSystem(config.get("hol_operating_system").get())
                    .operatingSystemVersion(config.get("hol_operating_system_version").get())
                    .shape(firstShape).sortBy("TIMECREATED").sortOrder("DESC").build())
                    .applyValue(result -> result.images().get(0).id());
            ctx.export("Image found ", compatibleImage);
            int amount = config.getInteger("amount_vm").orElse(1);

            for (int i = 1; i <= amount; i++) {
                // Create OCI compute instance.
                Instance instance = new Instance("instance" + i, InstanceArgs.builder()
                        // Provide the correct shape, image, and SSH authorized key.
                        .compartmentId(holCompartment.id())
                        .shape(firstShape)
                        .availabilityDomain(firstAvailabilityDomain)
                        .metadata(Map.of("ssh_authorized_keys", config.get("sshKey").get()))
                        .agentConfig(InstanceAgentConfigArgs.builder().isMonitoringDisabled(false).build())
                        .preserveBootVolume(true)
                        .createVnicDetails(InstanceCreateVnicDetailsArgs.builder()
                                .subnetId(subnet.id())
                                .build())
                        .sourceDetails(InstanceSourceDetailsArgs.builder()
                                .sourceType("image").sourceId(compatibleImage).build()
                        ).launchOptions(InstanceLaunchOptionsArgs.builder()
                                .bootVolumeType("PARAVIRTUALIZED")
                                .networkType("PARAVIRTUALIZED")
                                .build())
                        .build()
                );
                var sshKeyFile = config.get("ssh-private-key-file").get();
                //Export the public IP address and SSH username for logging in.
                var login = Output.of("on instance %s you can use ssh -i %s -o IdentityAgent=none opc@%s".formatted(instance.displayName(), sshKeyFile, instance.publicIp().applyValue( ip -> ip)));
                ctx.export("To login", login);
            }
        });
    }
}
