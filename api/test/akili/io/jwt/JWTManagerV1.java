package akili.io.jwt;

/**
import javax.annotation.Nonnull;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Calendar;
import java.util.Date; 


 * Is a simplified class for creating expirable JWT tokens.
 * Default expiration is 2hrs.
 * Default signing algorithm is HS256
 * Note: when using ES (elliptic curve) such ES256 signingkey should be a private key.
 * @author kndungu

public class JWTManagerV1 {
	
	//The JWT signature algorithm we will be using to sign the token
    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	
	//2hrs 
	long expirationMillis  = 720000;
	
   private  String signingKey = null;
	public JWTManagerV1(String _signingKey){
		signingKey =_signingKey;
	}
	
	public JWTManagerV1(SignatureAlgorithm algo, String _signingKey){
		signatureAlgorithm = algo;
		signingKey =_signingKey;
	}
	
	public SignatureAlgorithm getSignatureAlgorithm(){
		return signatureAlgorithm;
	}
	
	public String createJWT(String id, String issuer, String subject, long ttlMillis) {

		long nowMillis = System.currentTimeMillis();
	    long expMillis = nowMillis + ttlMillis;
	    Date expire = new Date(expMillis);
	
	    return createJWT( id,  issuer,  subject,  expire) ;
	}

	public String createJWT(@Nonnull String id, @Nonnull String issuer, String subject, Date expire ) {

	    long nowMillis = System.currentTimeMillis();
	    Date now = new Date(nowMillis);
	 
	    //We will sign our JWT with our ApiKey secret
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(signingKey);
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	 
	    //Let's set the JWT Claims
	    JwtBuilder builder = Jwts.builder()
	    							.setId(id)
	                                .setIssuedAt(now)
	                                .setSubject(subject)
	                                .setIssuer(issuer)
	                                .signWith(signatureAlgorithm, signingKey);
	 
	    //if it has been specified, let's add the expiration
	    if(expire!=null)
	    	builder.setExpiration(expire);
	 
	    //TODO add header based on data and expiration for verification
	    
	    //Builds the JWT and serializes it to a compact, URL-safe string
	    return builder.compact();
	}
	
	public String createJWT(String id, String issuer, String subject, String payload, long ttlMillis) {

			long nowMillis = System.currentTimeMillis();
		    long expMillis = nowMillis + ttlMillis;
		    Date expire = new Date(expMillis);
		
		    return createJWT( id,  issuer,  subject,  payload,  expire) ;
	}
	
	public String createJWT(@Nonnull String id, @Nonnull String issuer, String subject, String payload, Date expire) {
		 
	 
	    long nowMillis = System.currentTimeMillis();
	    Date now = new Date(nowMillis);
	 
	    //We will sign our JWT with our ApiKey secret
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(signingKey);
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	 
	    //Let's set the JWT Claims
	    JwtBuilder builder = Jwts.builder().setId(id)
	                                .setIssuedAt(now)
	                                .setSubject(subject)
	                                .setPayload(payload)
	                                .setIssuer(issuer)
	                                .signWith(signatureAlgorithm, signingKey);
	 
	    //if it has been specified, let's add the expiration
	    if(expire!=null)
	    	builder.setExpiration(expire);
	 
	    //Builds the JWT and serializes it to a compact, URL-safe string
	    return builder.compact();
	}

	
	public Claims parseJWT(String jwt) {
		
		System.out.println(jwt);
		 
	    //This line will throw an exception if it is not a signed JWS (as expected)
	    Claims claims = Jwts
	    		.parser()         
	    		.setSigningKey(DatatypeConverter.parseBase64Binary(signingKey))
	    		.parseClaimsJws(jwt)
	    		.getBody();
	    
	    System.out.println("ID: " + claims.getId());
	    System.out.println("Subject: " + claims.getSubject());
	    System.out.println("Issuer: " + claims.getIssuer());
	    System.out.println("Expiration: " + claims.getExpiration());
	    System.out.println("Audience: " + claims.getAudience());

	    return claims;
	}
	
	public static Claims parseJWT(String signingKey, String jwt) {
		
		System.out.println(jwt);
		 
	    //This line will throw an exception if it is not a signed JWS (as expected)
	    Claims claims = Jwts.parser()         
	       .setSigningKey(DatatypeConverter.parseBase64Binary(signingKey))
	       .parseClaimsJws(jwt).getBody();
	    
	    System.out.println("ID: " + claims.getId());
	    System.out.println("Subject: " + claims.getSubject());
	    System.out.println("Issuer: " + claims.getIssuer());
	    System.out.println("Expiration: " + claims.getExpiration());
	    System.out.println("Audience: " + claims.getAudience());
	    
	    return claims;
	}
	
	//UTILITIES
	
	public static Date incrementDate(Date date, int days){
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date); 
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	
	public static Date incrementMillis(Date date, int millis){
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date); 
		cal.add(Calendar.MILLISECOND , millis);
		return cal.getTime();
	}
	
	public static Date incrementHours(Date date, int hours){
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date); 
		cal.add(Calendar.HOUR , hours);
		return cal.getTime();
	}

}
*/