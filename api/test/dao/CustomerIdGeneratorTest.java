package dao;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.sun.jersey.core.util.Base64;

public class CustomerIdGeneratorTest {

	
	String id1 = "kaniu@somewhere.com6505551212";
	String id2 = "kaniu@somewhere.com6505551212blahblahblahblah";
	String id3 = "1kaniu@somewhere.com6505551212whatis the meaning of this???? Whatever man.";
	
	
	@Test
	public void messageDigestTest() throws NoSuchAlgorithmException {
		
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		digest.reset();
		
		
		BigInteger hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA512");
		System.out.println(hashVal);
		
		digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA256");
		System.out.println(hashVal);
		System.out.println("SHA256 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA256 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		digest = MessageDigest.getInstance("SHA1");
		digest.reset();		
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA1");
		System.out.println(hashVal);
		System.out.println("SHA1 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA1 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		
		hashVal = new BigInteger(digest.digest(id1.getBytes(Charsets.UTF_8)));
		System.out.println("SHA1-UTF8");
		System.out.println(hashVal);
		
		digest = MessageDigest.getInstance("MD2");
		digest.reset();		
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("MD2");
		System.out.println(hashVal);
		System.out.println("MD2 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("MD2 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		hashVal = new BigInteger(digest.digest(id1.getBytes(Charsets.UTF_8)));
		System.out.println("MD2-UTF8");
		System.out.println(hashVal);
		
		digest = MessageDigest.getInstance("MD5");
		digest.reset();		
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("MD5");
		System.out.println(hashVal);
		System.out.println("MD5 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("MD5 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		hashVal = new BigInteger(digest.digest(id1.getBytes(Charsets.UTF_8)));
		System.out.println("MD5-UTF8");
		System.out.println(hashVal);
		
		hashVal = new BigInteger(id1.getBytes(Charsets.UTF_8));
		System.out.println("UTF8");
		System.out.println(hashVal);
		
	
		
		hashVal = new BigInteger(Base64.encode(id1.getBytes(Charsets.UTF_8)));
		System.out.println("Base64-UTF8");
		System.out.println(hashVal);
		
		hashVal = new BigInteger(Hex.encodeHexString(id1.getBytes(Charsets.UTF_8)).getBytes());
		System.out.println("HEX");
		System.out.println(hashVal);
		
		
		
		
		//hashVal = new BigInteger(UUID.fromString(id1).toString().getBytes());
		String uuid = UUID.randomUUID().toString();
		System.out.println("UUID");
		System.out.println(uuid);
		
		hashVal = new BigInteger(uuid.getBytes());
		System.out.println("UUID bigint");
		System.out.println(hashVal);
		
		
	}
	
	
	@Test
	public void messageDigestTest2() throws NoSuchAlgorithmException {
		
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		digest.reset();
		
		id1 = id2;
		
		BigInteger hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA512");
		System.out.println(hashVal);
		
		digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA256");
		System.out.println(hashVal);
		System.out.println("SHA256 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA256 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		digest = MessageDigest.getInstance("SHA1");
		digest.reset();		
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA1");
		System.out.println(hashVal);
		System.out.println("SHA1 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA1 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		
		hashVal = new BigInteger(digest.digest(id1.getBytes(Charsets.UTF_8)));
		System.out.println("SHA1-UTF8");
		System.out.println(hashVal);
		
		
		hashVal = new BigInteger(id1.getBytes(Charsets.UTF_8));
		System.out.println("UTF8");
		System.out.println(hashVal);
		
	
		
		hashVal = new BigInteger(Base64.encode(id1.getBytes(Charsets.UTF_8)));
		System.out.println("Base64-UTF8");
		System.out.println(hashVal);
		
		hashVal = new BigInteger(Hex.encodeHexString(id1.getBytes(Charsets.UTF_8)).getBytes());
		System.out.println("HEX");
		System.out.println(hashVal);
		
		
		
		
		//hashVal = new BigInteger(UUID.fromString(id1).toString().getBytes());
		String uuid = UUID.randomUUID().toString();
		System.out.println("UUID");
		System.out.println(uuid);
		
		hashVal = new BigInteger(uuid.getBytes());
		System.out.println("UUID bigint");
		System.out.println(hashVal);
		
		
	}
	
	
	@Test
	public void messageDigestTest3() throws NoSuchAlgorithmException {
		
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		digest.reset();
		
		id1 = id3;
		
		BigInteger hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA512");
		System.out.println(hashVal);
		
		digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA256");
		System.out.println(hashVal);
		System.out.println("SHA256 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA256 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		digest = MessageDigest.getInstance("SHA1");
		digest.reset();		
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA1");
		System.out.println(hashVal);
		System.out.println("SHA1 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA1 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		
		hashVal = new BigInteger(digest.digest(id1.getBytes(Charsets.UTF_8)));
		System.out.println("SHA1-UTF8");
		System.out.println(hashVal);
		
		
		hashVal = new BigInteger(id1.getBytes(Charsets.UTF_8));
		System.out.println("UTF8");
		System.out.println(hashVal);
		
	
		
		hashVal = new BigInteger(Base64.encode(id1.getBytes(Charsets.UTF_8)));
		System.out.println("Base64-UTF8");
		System.out.println(hashVal);
		
		hashVal = new BigInteger(Hex.encodeHexString(id1.getBytes(Charsets.UTF_8)).getBytes());
		System.out.println("HEX");
		System.out.println(hashVal);
		
		
		
		
		//hashVal = new BigInteger(UUID.fromString(id1).toString().getBytes());
		String uuid = UUID.randomUUID().toString();
		System.out.println("UUID");
		System.out.println(uuid);
		
		hashVal = new BigInteger(uuid.getBytes());
		System.out.println("UUID bigint");
		System.out.println(hashVal);
		
		
	}
	
	
	@Test
	public void messageDigestTestDeflation() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
		
		Deflater compresser = new Deflater();
		compresser.setInput(id3.getBytes("UTF-8"));
		compresser.finish();
		byte[] output = new byte[100];
	     int compressedDataLength = compresser.deflate(output);
	     compresser.end();
	     System.out.println("compression length = "+compressedDataLength);
	     System.out.println("compression output hex = "+Hex.encodeHexString(output));
	     System.out.println("compression output base64 = "+new String(Base64.encode(output)));
	     System.out.println("compression output hex = "+new BigInteger(Hex.encodeHexString(output).getBytes()));
	     System.out.println("compression output base64 = "+new BigInteger(Base64.encode(output)));
	     
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		digest.reset();
		
		id1 = id3;
		
		BigInteger hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA512");
		System.out.println(hashVal);
		
		digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA256");
		System.out.println(hashVal);
		System.out.println("SHA256 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA256 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		digest = MessageDigest.getInstance("SHA1");
		digest.reset();		
		hashVal = new BigInteger(digest.digest(id1.getBytes()));
		System.out.println("SHA1");
		System.out.println(hashVal);
		System.out.println("SHA1 HEX");
		System.out.println(Hex.encodeHexString(digest.digest(id1.getBytes())));
		System.out.println("SHA1 HEX BIGINT");
		System.out.println(new BigInteger(Hex.encodeHexString(digest.digest(id1.getBytes())).getBytes()));
		
		
		
		hashVal = new BigInteger(digest.digest(id1.getBytes(Charsets.UTF_8)));
		System.out.println("SHA1-UTF8");
		System.out.println(hashVal);
		
		
		hashVal = new BigInteger(id1.getBytes(Charsets.UTF_8));
		System.out.println("UTF8");
		System.out.println(hashVal);
		
	
		
		hashVal = new BigInteger(Base64.encode(id1.getBytes(Charsets.UTF_8)));
		System.out.println("Base64-UTF8");
		System.out.println(hashVal);
		
		hashVal = new BigInteger(Hex.encodeHexString(id1.getBytes(Charsets.UTF_8)).getBytes());
		System.out.println("HEX");
		System.out.println(hashVal);
		
		
		
		
		//hashVal = new BigInteger(UUID.fromString(id1).toString().getBytes());
		String uuid = UUID.randomUUID().toString();
		System.out.println("UUID");
		System.out.println(uuid);
		
		hashVal = new BigInteger(uuid.getBytes());
		System.out.println("UUID bigint");
		System.out.println(hashVal);
		
		
	}
	@Test 
	public void randoimNumbers() {
		Random random = new Random();
		//random.setSeed(2147483647);
		//Random random = new Random(2147483647);
		
		System.out.println(random.nextInt(2147483647));
		System.out.println(random.nextInt(2147483647));
		System.out.println(random.nextInt(2147483647));
		System.out.println(random.nextInt(2147483647));
		System.out.println("");
		System.out.println(random.nextInt(2000000000));
		System.out.println(random.nextInt(2000000000));
		System.out.println(random.nextInt(2000000000));
		System.out.println(random.nextInt(2000000000));
		System.out.println(random.nextInt(2000000000));
		System.out.println("9");
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println(random.nextInt(999999999));
		System.out.println("5");
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println(random.nextInt(99999));
		System.out.println("3");
		System.out.println(random.nextInt(999));
		System.out.println(random.nextInt(999));
		System.out.println(random.nextInt(999));
		System.out.println(random.nextInt(999));
		
				
	}
}
