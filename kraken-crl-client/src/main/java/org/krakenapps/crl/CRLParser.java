package org.krakenapps.crl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.krakenapps.ber.BERDecoder;
import org.krakenapps.ber.BERObject;
import org.krakenapps.ber.OIDParser;
import org.krakenapps.ber.UniversalTags;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class CRLParser {
	private static final int LENGTH_OF_REVOCATIONDATE_BYTES = 15;

	public CertificateRevocationList parse(byte[] crlBytes) {
		Buffer buffer = new ChainBuffer();
		buffer.addLast(crlBytes);

		return parse(buffer);
	}

	private CertificateRevocationList parse(Buffer buffer) {
		/* skip CRL BER Object, TBSCertList BER Object */
		BERDecoder.getBERObject(buffer);
		BERDecoder.getBERObject(buffer);

		/* get version(specified or omitted) */
		int version = getVersion(buffer);

		/* get signature */
		BERObject signatureObj = BERDecoder.getBERObject(buffer);
		buffer.skip(signatureObj.getLength());
		String signatureAlgorithmID = OIDParser.parse(signatureObj.getValue());
		Algorithm signatureAlgorithm = AlgorithmClassifier.getAlgorithm(signatureAlgorithmID);
		AlgorithmIdentifier signature = new AlgorithmIdentifier(signatureAlgorithmID, signatureAlgorithm);

		/* get issuer */
		BERObject issuerObj = BERDecoder.getBERObject(buffer);

		/* parse issuer */
		List<BERObject> rdnSequences = getRdnSequences(buffer, issuerObj.getLength());
		List<RelativeDistinguishedName> rdns = getRdns(rdnSequences);
		Issuer issuer = new Issuer(rdns);

		/* get thisUpdate, nextUpdate */
		Date thisUpdate = getUTCDate(buffer);
		Date nextUpdate = getUTCDate(buffer);

		/* get RevokedCertifate list */
		List<RevokedCertificate> rcList = parseRevokedCertificates(buffer);
		
		/* distinguish extensions */
		buffer.mark();
		int tagBytes = buffer.get();
		buffer.reset();
		
		BERObject obj = BERDecoder.getBERObject(buffer);
		BERObject algorithmIdentifierObj;
		if((tagBytes & 0xf0) == 0xa0) {
			/* skip CRL Extensions */
			int skipped = obj.getLength();
			buffer.skip(skipped);
			algorithmIdentifierObj = BERDecoder.getBERObject(buffer);
		}
		else 
			 algorithmIdentifierObj = obj;
		
		/* get AlgorithmIdentifier */
		buffer.skip(algorithmIdentifierObj.getLength());
		String algorithmID = OIDParser.parse(algorithmIdentifierObj.getValue());
		Algorithm algorithm = AlgorithmClassifier.getAlgorithm(algorithmID);
		
		TBSCertList tbsCertList = new TBSCertList(version, signature, issuer, thisUpdate, nextUpdate, rcList);
		AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(algorithmID, algorithm);
		
		return new CertificateRevocationList(tbsCertList, algorithmIdentifier);
	}

	private int getVersion(Buffer buffer) {
		buffer.mark();
		/* 5.1.2.1 */
		if (buffer.get() == 0x02 && buffer.get() == 0x01 && buffer.get() == 0x01) {
			return 2;
		} else {
			buffer.reset();
			return 0;
		}
	}

	private List<RelativeDistinguishedName> getRdns(List<BERObject> rdnSequences) {
		List<RelativeDistinguishedName> rdns = new ArrayList<RelativeDistinguishedName>();
		for (BERObject rdnSequence : rdnSequences) {
			rdns.add(parseRdnSequence(rdnSequence));
		}
		return rdns;
	}

	private List<BERObject> getRdnSequences(Buffer buffer, int end) {
		List<BERObject> rdnSequences = new ArrayList<BERObject>();
		int current = 0;
		while (current < end) {
			BERObject rdnSequence = BERDecoder.getBERObject(buffer);
			rdnSequences.add(rdnSequence);
			buffer.skip(rdnSequence.getLength());
			current += (rdnSequence.getLength() + 2);
		}

		return rdnSequences;
	}

	private static RelativeDistinguishedName parseRdnSequence(BERObject rdnSequence) {
		if (rdnSequence.getTag() != UniversalTags.SET)
			return null;

		byte[] value = rdnSequence.getValue();
		byte[] b = Arrays.copyOfRange(value, 2, value.length);
		String oid = OIDParser.parse(b);
		X500Attribute attr = X500AttributeClassifier.getAttribute(oid);

		int length = b[1];
		/* start: start point of String value */
		int start = length + 4;
		String rdnValue = new String(Arrays.copyOfRange(b, start, b.length));

		return new RelativeDistinguishedName(oid, attr, rdnValue);
	}

	private Date getUTCDate(Buffer buffer) {
		try {
			byte type = buffer.get();
			if (type != 0x17)
				return null;

			StringBuilder builder = new StringBuilder();
			int length = buffer.get();
			int i = 0;

			while (i < length) {
				builder.append((char) buffer.get());
				switch (i) {
				case 1:
				case 3:
					builder.append("-");
					break;
				case 5:
					builder.append(" ");
					break;
				case 7:
				case 9:
					builder.append(":");
					break;
				}
				i++;
			}

			SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.KOREA);
			return format.parse(builder.toString());
		} catch (ParseException e) {
			return null;
		}
	}

	private List<RevokedCertificate> parseRevokedCertificates(Buffer buffer) {
		BERObject rcsObj = BERDecoder.getBERObject(buffer);
		int totalLength = rcsObj.getLength();

		int i = 0;
		int rcNum = 0;

		List<RevokedCertificate> rcList = new ArrayList<RevokedCertificate>();
		BERObject rcObj;
		while (i < totalLength) {
			rcObj = BERDecoder.getBERObject(buffer);
			rcList.add(getRevokedCertificate(buffer, rcObj.getLength()));
			i += (rcObj.getLength() + 2);
			rcNum++;
		}
		
		return rcList;
	}

	private RevokedCertificate getRevokedCertificate(Buffer buffer, int length) {
		BERObject ucObj = BERDecoder.getBERObject(buffer);
		buffer.skip(ucObj.getLength());

		byte[] userCertificate = ucObj.getValue();
		Date revocationDate = getUTCDate(buffer);

		BERObject extensionObj = null;
		/* case: exist CRL Entry extensions */
		if (ucObj.getLength() + LENGTH_OF_REVOCATIONDATE_BYTES < length) {
			extensionObj = BERDecoder.getBERObject(buffer);
			buffer.skip(extensionObj.getLength());
		}

		return new RevokedCertificate(userCertificate, revocationDate, extensionObj);
	}
}