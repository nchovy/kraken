package org.krakenapps.ca;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.krakenapps.api.Environment;
import org.krakenapps.ca.impl.CertificateAuthorityImpl;

public class ClientCAUsageTest {
	public void PrimaryTest() throws IOException {
		Environment.setKrakenSystemProperties(".");

		// TODO: check prerequisite - kraken.data.dir, CA file exists?
		System.out.println(System.getProperty("kraken.data.dir"));

		CertificateAuthority ca = new CertificateAuthorityImpl();
		
		File caRootDir = ca.getCARootDir();

		String caCN = "local";
		String caPassword = "kraken";
		String caPkPass = "kraken";

		File caFile = getCAKeyStoreFile(caRootDir, caCN);
		assertTrue(caFile.exists());

		// TODO: new private keypair from local CA
		PrivateCertificateCreationContext ctx = createCertificate(ca, caCN, caPassword, caPkPass, "stania",
				"stania", "staniapass", 365);
		X509Certificate certficate = ctx.getCertficiate();
		byte[] encodedEncryptedPrivateKey = ctx.getEncodedEncryptedPrivateKey();

		// TODO: export pkcs#8 key and cert from keypair
		FileOutputStream pkfos = null;
		FileOutputStream certfos = null;
		try {
			pkfos = new FileOutputStream(new File(caRootDir, "stania.key"));
			pkfos.write(encodedEncryptedPrivateKey);

			File certFile = new File(caRootDir, "stania.crt");

			certfos = new FileOutputStream(certFile);
			certfos.write(certficate.getEncoded());

			System.out.println(certFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pkfos != null)
				pkfos.close();
			if (certfos != null)
				certfos.close();
		}

		// TODO: save it as pfx;
		// TODO: export pfx and leave cert
	}

	private enum DNField {
		CN, OU, O, L, ST, C
	};

	// http://www.bouncycastle.org/specifications.html Signature Algorithms
	private enum SigAlgorithm {
		MD2withRSA, MD5withRSA, SHA1withRSA, SHA224withRSA, SHA256withRSA, SHA384withRSA, SHA512withRSA
	};

	private class PrivateCertificateCreationContext {
		private Certificate caCert = null;
		private RSAPrivateKey caKey = null;
		private ArrayList<Map.Entry<String, String>> dnItems = new ArrayList<Map.Entry<String, String>>();
		private SigAlgorithm sigAlgorithm;
		private Date notBefore;
		private Date notAfter;
		private X509Certificate certificate;
		private byte[] encryptedPrivKey;
		private CertificateAuthority ca;

		public PrivateCertificateCreationContext(CertificateAuthority ca) {
			this.ca = ca;
		}

		public void openRootCA(String caCN, String caKeystorePass, String caPrivKeyPass) {
			File caFile = getCAKeyStoreFile(ca.getCARootDir(), caCN);
			if (caFile == null || !caFile.exists()) {
				throw new IllegalArgumentException("CA keystore not found");
			}

			FileInputStream fs = null;

			try {
				fs = new FileInputStream(caFile);
				KeyStore store = KeyStore.getInstance("JKS");
				store.load(fs, caKeystorePass.toCharArray());
				caCert = store.getCertificate("ca");
				caKey = (RSAPrivateKey) store.getKey("ca-key", caPrivKeyPass.toCharArray());

			} catch (FileNotFoundException e1) {
				throw new IllegalArgumentException("CA keystore cannot be opened. perhaps password mismatching.");
			} catch (Exception e) {
				throw new IllegalArgumentException("CA keystore cannot be opened. perhaps password mismatching.");
			} finally {
				if (fs != null)
					try {
						fs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		public void addCertDNItem(DNField field, String value) {
			if (value.contains(","))
				throw new IllegalArgumentException("value should not contain comma");
			dnItems.add(new SimpleEntry<String, String>(field.toString(), value));
		}

		public void setSigAlgorithm(SigAlgorithm sa) {
			this.sigAlgorithm = sa;
		}

		public void setDuration(int days) {
			notBefore = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(notBefore);
			cal.add(Calendar.DAY_OF_YEAR, days);
			notAfter = cal.getTime();
		}

		public void create(String pkPassword) throws Exception {
			// TODO generate DN
			String dn = generateDN(dnItems);

			// TODO generate keypair
			KeyPair keyPair = generateKeyPair();

			// TODO generate create cert
			try {
				certificate = ca.createCertificate((java.security.cert.X509Certificate) caCert, caKey, keyPair, dn,
						new HashMap<String, String>(), notBefore, notAfter, sigAlgorithm.toString());
			} catch (Exception e) {
				throw e;
			} finally {
				caKey = null;
			}

			// TODO save PKCS#8
			saveEncryptedPrivateKey(keyPair.getPrivate(), pkPassword);
		}

		private void saveEncryptedPrivateKey(PrivateKey privKey, String pkPassword) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// code from http://stackoverflow.com/questions/5127379/how-to-generate-a-rsa-keypair-with-a-privatekey-encrypted-with-password
			byte[] encodedPrivKey = privKey.getEncoded();
			int hashIterationCount = 20;
			Random random = new Random();
			byte[] salt = new byte[8];
			random.nextBytes(salt);

			try {
				PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, hashIterationCount);
				PBEKeySpec pbeKeySpec = new PBEKeySpec(pkPassword.toCharArray());
				String pbeAlgorithm = "PBEWithSHA1AndDESede";
				SecretKeyFactory keyFac = SecretKeyFactory.getInstance(pbeAlgorithm);
				SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

				Cipher pbeCipher = Cipher.getInstance(pbeAlgorithm);

				pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

				byte[] ciphertext = pbeCipher.doFinal(encodedPrivKey);

				AlgorithmParameters algparams = AlgorithmParameters.getInstance(pbeAlgorithm);
				algparams.init(pbeParamSpec);
				EncryptedPrivateKeyInfo encInfo = new EncryptedPrivateKeyInfo(algparams, ciphertext);

				encryptedPrivKey = encInfo.getEncoded();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (InvalidParameterSpecException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private KeyPair generateKeyPair() {
			try {
				KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
				return keyPairGen.generateKeyPair();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("RSA: no such algorithm");
			} catch (NoSuchProviderException e) {
				throw new IllegalStateException("BC: no such provider");
			}
		}

		private String generateDN(ArrayList<Entry<String, String>> items) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < items.size(); ++i) {
				if (i != 0)
					buf.append(", ");
				Entry<String, String> entry = items.get(i);
				buf.append(entry.getKey() + "=" + entry.getValue());
			}

			return buf.toString();
		}

		public X509Certificate getCertficiate() {
			return certificate;
		}

		public byte[] getEncodedEncryptedPrivateKey() {
			return encryptedPrivKey;
		}
	}

	private PrivateCertificateCreationContext createCertificate(
			CertificateAuthority ca,
			String caCN,
			String caKeystorePass,
			String caPrivKeyPass,
			String issueeCN,
			String keypairAlias,
			String pkPassword,
			int days) {
		PrivateCertificateCreationContext ctx = new PrivateCertificateCreationContext(ca);

		try {
			ctx.openRootCA(caCN, caKeystorePass, caPrivKeyPass);
			ctx.addCertDNItem(DNField.CN, issueeCN);
			ctx.setSigAlgorithm(SigAlgorithm.SHA512withRSA);
			ctx.setDuration(days);
			ctx.create(pkPassword);

			return ctx;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private File getCAKeyStoreFile(File home, String caCN) {
		return new File(home, caCN + "/CA.jks");
	}

}
