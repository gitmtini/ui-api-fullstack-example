

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.spongycastle.crypto.tls.EncryptionAlgorithm;
import org.spongycastle.util.encoders.Hex;

import com.google.common.base.Charsets;
import com.prelimtek.utils.crypto.JWTManager;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bitcoinj.core.ECKey;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.EllipticCurveProvider;
//import sun.security.ec.ECPrivateKeyImpl;
//import sun.security.internal.interfaces.TlsMasterSecret;

public class TestJWT {
	JWTManager mn = null;
	@Before
	public void init(){
		mn =  new JWTManager("kaniu");
		
	}
	
	@Test(expected=ExpiredJwtException.class)
	public void testTokenExpiration(){
		String jwt = mn.createJWT("testid", "ritho", "mysubject", 1);

		
		mn.parseJWT(jwt);
		
	}
	
	@Test
	public void testParseNewJwt(){

		String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMDU4NjYxMDcxNjI3NDEiLCJpYXQiOjE1NDY4NjE2MzYsInN1YiI6IlNvcGhpYSBBbGNiYWhqZWZpamRhIEdyZWVuZW1hbiIsImlzcyI6Im10aW5pIiwiZXhwIjozMDk1Nzk2ODcyfQ.GLlHUAib1OzVrog2uxr4MP3d2ooGRZfTZX2ivuQ-HMM";
		Claims claim = new JWTManager("kaniu").parseJWT(jwt);
		 
		    
		System.out.println(claim.getId());
		System.out.println(claim.getIssuer());
		System.out.println(claim.getSubject() );
		System.out.println(claim.getExpiration() );
		
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test()
	public void testInvalidToken(){
		String jwt = mn.createJWT("testid", "ritho", "mysubject", 1000);
		
		thrown.expect(SignatureException.class);
		thrown.expectMessage("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted");
		
		jwt = jwt+"randomstring";
		mn.parseJWT(jwt);
		
	}
	
	
	@Test
	public void testGoodToken() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		
		String jwt = mn.createJWT("somekey", "akili", "100 day JWT token" ,JWTManager.incrementDate(new Date(), 100));
		System.out.println(jwt);
		Claims cl = mn.parseJWT(jwt);
		System.out.println(cl.toString());
		
	}
	
	@Test
	public void testAsyncJWT() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException {
		//String publicKey = "029ba831e4f8f1e7e82af5d76c6fcc1c807b5b5ef7c579546ad68907de40e4273d";
		//String privateKey = "038137c1154634a84b3f9e3897309d3e1b2d66b160e8eb0e921ca2292a719985";
		
		KeyPair kp = EllipticCurveProvider.generateKeyPair(SignatureAlgorithm.ES256);
		//System.out.println(Base64.getEncoder().encodeToString(new ECPrivateKey(new BigInteger(kp.getPrivate().getEncoded())).getEncoded()));
		System.out.println(kp.getPrivate().getFormat());
		System.out.println(kp.getPrivate().getAlgorithm());
		String privateKey = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
		String publicKey = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
		
		System.out.println(privateKey);
		System.out.println(publicKey);
		
		String jwt = new JWTManager(SignatureAlgorithm.ES256 ,privateKey).createJWT("somekey", "akili", "100 day JWT token" ,JWTManager.incrementDate(new Date(), 100));
		System.out.println(jwt);
		Claims cl = new JWTManager(SignatureAlgorithm.ES256 ,publicKey).parseJWT(jwt);
		System.out.println(cl.toString());
		
	}
	
	@Test
	public void testAsyncJWT2() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, IOException{
		
		SignatureAlgorithm algo = SignatureAlgorithm.ES256;
		
		//KeyPair kp = EllipticCurveProvider.generateKeyPair(algo);
		//KeyPair kp = SecurityUtils.generateSECP256KeyPair();
		//byte[] privateKey = kp.getPrivate().getEncoded();
		//byte[] publicKey = kp.getPublic().getEncoded();
		
		ECPrivateKey ecKey = null;
		ECPublicKey ecPubKey = null;
		Key signingKey = null;
		
		try {
			
			ecKey = createECPrivateKey();
			ecPubKey = generatePublicKey(ecKey);
			signingKey = generateKeyZ(ecKey,ecPubKey, null);
			System.out.println("Format "+signingKey.getFormat());
		} catch (InvalidParameterSpecException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		byte[] privateKey  = ecKey.getEncoded();//.getGenerator().getS().toByteArray();//getEncoded();
		byte[] publicKey = ecPubKey.getEncoded();//ecKey.getPublicKey().getEncoded();
		String jwtToken=createJwt("somekey", "akili", "100 day JWT token" ,algo,signingKey);
		System.out.println(jwtToken);
		Jwt<Header,Claims> headerClaim = parse(jwtToken,ecPubKey);
		System.out.println(headerClaim.getBody().toString());
	
	}
	
	private String createJwt(String id, String subject, String issuer, SignatureAlgorithm signatureAlgorithm, byte[]apiKeySecretBytes){
		
		Date now = new Date();
		
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		//Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder()
		    							.setId(id)
		                                .setIssuedAt(now)
		                                .setSubject(subject)
		                                .setIssuer(issuer)
		                                .signWith(signatureAlgorithm, signingKey);
		    return builder.compact();
		 
	}
	
	private String createJwt(String id, String subject, String issuer, SignatureAlgorithm signatureAlgorithm, Key privateKey){
		
		Date now = new Date();
		
		//Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		//Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder()
		    							.setId(id)
		                                .setIssuedAt(now)
		                                .setSubject(subject)
		                                .setIssuer(issuer)
		                                .signWith(signatureAlgorithm, privateKey);
		    return builder.compact();
		 
	}
	
	private Jwt<Header,Claims> parse(String jwt, byte[] apiKeySecretBytes){
		
		Jwt<Header,Claims> headClaims = Jwts
	    		.parser()         
	    		.setSigningKey(apiKeySecretBytes)
	    		.parseClaimsJwt(jwt);
		
		Claims claims	= headClaims.getBody();
		Header header =  headClaims.getHeader();
		
		return headClaims;
	}
	
	private Jwt<Header,Claims> parse(String jwt, Key apiKeySecretBytes){
		
		Jwt<Header,Claims> headClaims = Jwts
	    		.parser()         
	    		.setSigningKey(apiKeySecretBytes)
	    		.parseClaimsJwt(jwt);
		
		Claims claims	= headClaims.getBody();
		Header header =  headClaims.getHeader();
		
		return headClaims;
	}
	
	public ECPrivateKey createECPrivateKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException, InvalidKeySpecException{
		 AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC","BC");// "SunEC");
	        parameters.init(new ECGenParameterSpec("secp256k1"));
	        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
		 ECPrivateKeySpec specPrivate = new ECPrivateKeySpec(new BigInteger(256,new SecureRandom()), ecParameters);
		 KeyFactory kf = KeyFactory.getInstance("EC");
		 ECPrivateKey privateKey = (ECPrivateKey)kf.generatePrivate(specPrivate);
		 
		 //privateKey.getPublicKey().getEncoded()
		
		 
		 return privateKey; 
	}
	
	public ECPublicKey generatePublicKey(ECPrivateKey privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException{
		KeyFactory kf = KeyFactory.getInstance("EC"); 

		 ECParameterSpec ecParameters = privateKey.getParams();
		 
		 BigInteger x = privateKey.getParams().getGenerator().getAffineX();
		 BigInteger y = privateKey.getParams().getGenerator().getAffineY();
		 ECPublicKeySpec specPublic = new ECPublicKeySpec(new ECPoint(x,y), ecParameters);		 
		 ECPublicKey publicKey = (ECPublicKey) kf.generatePublic(specPublic);
		 
		 return publicKey;
	}
	
	private static Key generateKeyZ(ECPrivateKey privateKey, ECPublicKey publicKey, SignatureAlgorithm algo) throws InvalidKeyException, NoSuchAlgorithmException {
		KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(privateKey);
        ka.doPhase(publicKey, true);
        
        SecretKey secret  = ka.generateSecret("TlsPremasterSecret");
        
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("ECDH");
			return factory.generateSecret(factory.getKeySpec(secret, ECPrivateKeySpec.class));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
		return null;
	}
	

	/*public EncryptionAlgorithm getBouncyCastleEncryptionAlgorithm() throws DSSException {
	    if (privateKey instanceof RSAPrivateKey) {
	        return EncryptionAlgorithm.RSA;
	    } else if (privateKey instanceof DSAPrivateKey) {
	        return EncryptionAlgorithm.DSA;
	    } else if (privateKey instanceof ECPrivateKey) {
	        return EncryptionAlgorithm.ECDSA;
	    } else if (EncryptionAlgorithm.RSA.getName().equals(privateKey.getAlgorithm())) {
	        return EncryptionAlgorithm.RSA;
	    } else if (EncryptionAlgorithm.DSA.getName().equals(privateKey.getAlgorithm())) {
	        return EncryptionAlgorithm.DSA;
	    } else if (EncryptionAlgorithm.ECDSA.getName().equals(privateKey.getAlgorithm())) {
	        return EncryptionAlgorithm.ECDSA;
	    } else {
	        throw new DSSException("Don't find algorithm for PrivateKey of type " + privateKey.getClass());
	    }
	}*/
	


}
