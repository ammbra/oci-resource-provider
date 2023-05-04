package com.jpg.hol.provider;

import com.pulumi.Config;
import com.pulumi.core.Output;
import com.pulumi.oci.Core.*;
import com.pulumi.oci.Core.inputs.*;

public class Network {
	public static Vcn createVcn(Config config, Output<String> compartmentId) {
		VcnArgs args = VcnArgs.builder()
				.cidrBlock(config.get("vcn_cidr_block").get())
				.compartmentId(compartmentId)
				.displayName(config.get("vcn_display_name").get())
				.dnsLabel(config.get("vcn_dns_label").get()).build();
		return new Vcn("hol_vcn", args);
	}

	public static InternetGateway createInternetGateway(Config config, Vcn vcn, Output<String> compartmentId) {
		InternetGatewayArgs args = InternetGatewayArgs.builder()
				.compartmentId(compartmentId)
				.displayName(config.get("internetgateway_name").get())
				.vcnId(vcn.id()).build();
		return new InternetGateway("hol_internet_gateway", args);

	}

	public static RouteTable createRoutetable(Config config, Vcn vcn, InternetGateway internetGateway, Output<String> compartmentId) {
		RouteTableRouteRuleArgs trafficToInternet = RouteTableRouteRuleArgs.builder()
				.description("traffic to/from internet")
				.destination("0.0.0.0/0").destinationType("CIDR_BLOCK")
				.networkEntityId(internetGateway.id()).build();

		RouteTableArgs args = RouteTableArgs.builder()
				.compartmentId(compartmentId)
				.displayName(config.get("hol_routetable_displayname").get())
				.routeRules(trafficToInternet)
				.vcnId(vcn.id()).build();
		return new RouteTable("route_table", args);
	}


	public static Subnet createSubnet(Config config, Vcn vcn, RouteTable routeTable, Output<String> compartmentId) {
		SubnetArgs args = SubnetArgs.builder()
				.compartmentId(compartmentId)
				.displayName(config.get("hol_subnet_displayname").get())
				.cidrBlock(config.get("hol_subnet_cidr").get())
				.prohibitInternetIngress(false)
				.dnsLabel("holendpoints")
				.routeTableId(routeTable.id())
				.vcnId(vcn.id())
				.build();
		return new Subnet("hol_subnet", args);
	}
}
