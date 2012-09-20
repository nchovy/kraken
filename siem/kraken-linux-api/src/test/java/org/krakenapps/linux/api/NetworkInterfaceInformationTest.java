package org.krakenapps.linux.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class NetworkInterfaceInformationTest {
	@Test
	public void getNetworklInterfaceInformationTest() {
		List<NetworkInterfaceInformation> informations = null;

		try {
			informations = NetworkInterfaceInformation.getNetworkInterfaceInformations(new File(
					"src/test/resources/NetworkInterfaces"));
		} catch (IOException e) {
			Assert.fail("cannot not found dev file");
		}
		for (NetworkInterfaceInformation nii : informations) {
			if (nii.getName().equals("lo")) {
				assertEquals(1557111, nii.getRxBytes());
				assertEquals(8191, nii.getRxPackets());
			} else if (nii.getName().equals("eth0")) {
				assertEquals(2768360, nii.getRxBytes());
				assertEquals(45390, nii.getRxPackets());
				assertEquals(258966074, nii.getTxBytes());
				assertEquals(87851, nii.getTxPackets());
			}
		}
	}
}
