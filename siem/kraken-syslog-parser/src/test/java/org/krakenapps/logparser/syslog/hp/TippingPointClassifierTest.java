package org.krakenapps.logparser.syslog.hp;

import java.net.InetSocketAddress;
import java.util.Date;

import org.junit.Test;
import org.krakenapps.syslog.Syslog;

import static org.junit.Assert.*;

public class TippingPointClassifierTest {
	@Test
	public void testClassify() {
		InetSocketAddress remote = new InetSocketAddress(33333);
		String line = "7\t3\td2ee633d-514f-11e1-3f6d-e43760fdb01b\t00000001-0001-0001-0001-000000003886\t"
				+ "3886: HTTP: Cross Site Scripting in POST Request\t3886\ttcp\t119.205.194.173\t40635\t222.231.7.12\t80\t"
				+ "1\t3\t3\tInterpark_A\t17107965\t1335074090028\t";
		Syslog syslog = new Syslog(new Date(), remote, -1, 2, line);
		TippingPointClassifier classifier = new TippingPointClassifier();
		String identifier = classifier.classify(syslog);
		assertEquals("Interpark_A", identifier);
	}
}
