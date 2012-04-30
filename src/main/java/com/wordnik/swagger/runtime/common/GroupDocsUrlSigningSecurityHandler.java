package com.wordnik.swagger.runtime.common;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.jersey.core.util.Base64;

public class GroupDocsUrlSigningSecurityHandler implements SecurityHandler {

	private static final String ENC = "UTF-8";
	private static final String SIGN_ALG = "HmacSHA1";
    private String privateKey;

    public GroupDocsUrlSigningSecurityHandler(String privateKey) {
        this.privateKey = privateKey;
    }

    public void populateSecurityInfo(StringBuilder resourceURL, Map<String, String> httpHeaders) {
    	try {
			URL url = new URL(resourceURL.toString());
			String pathAndQuery = url.getFile();
			String signature = sign(pathAndQuery);
			resourceURL.append((url.getQuery() == null ? "?" : "&")).append("signature=").append(signature);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }
    
    private String sign(String toSign){
		try {
			Mac mac = Mac.getInstance(SIGN_ALG);
			mac.init(new SecretKeySpec(privateKey.getBytes(ENC), SIGN_ALG));
			String signature = new String(Base64.encode(mac.doFinal(toSign.getBytes(ENC))), ENC);
			return encode(signature.replace("=", ""));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return null;
	}

    private static String encode(String value){
        try{
            return URLEncoder.encode(value, ENC).replaceAll("\\+", "%20");
        }catch(UnsupportedEncodingException uee){
            throw new RuntimeException(uee.getMessage());
        }
    }

}
