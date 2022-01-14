import java.util.Base64;

import org.bitcoinj.crypto.KeyCrypterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.prelimtek.client.es.ESDAOImpl;
import com.prelimtek.client.es.ESDAOImpl.ElasticsearchPersistenceException;
import com.prelimtek.utils.crypto.BitcoinCryptoUtils;
import com.prelimtek.utils.crypto.Wallet;
import com.prelimtek.utils.crypto.Wallet.WalletException;

import io.mtini.proto.MtiniWalletProtos;
import io.mtini.proto.MtiniWalletProtos.MtiniWallet;

public class TestWalletManagement {
	Wallet wallet = null;
	
	@Before
	public void init(){
		//create wallet
		CharSequence pass = BitcoinCryptoUtils.generatePassPhrase("4721", true);
		System.out.println(pass.toString().getBytes().length*8);
		//CharSequence pass = "passwordpassword";
		wallet = new Wallet("kaniu@mtini.io",null,pass);
		
		
	}
	
	
	@Test
	public void decryptPrivateKey() throws WalletException{
		CharSequence pass = BitcoinCryptoUtils.generatePassPhrase("4721", true);
		//CharSequence pass = "passwordpassword";
		String hexPrivateBytes=wallet.decryptPrivateKeyHex(pass);
	
	}
	
	@Test
	public void decryptPrivateKey1() throws WalletException{
		CharSequence pass = BitcoinCryptoUtils.generatePassPhrase("4721", true);
		//CharSequence pass = "passwordpassword";
		String hexPrivateBytes=wallet.decryptPrivateKeyHex(pass);

	
	}
	
	@Test
	public void esWalletDecrypt() throws WalletException, JSONException, InvalidProtocolBufferException{
		ESDAOImpl esDao =  new ESDAOImpl("http://localhost:9200");
		
		String address = Wallet.generateWalletAddress("kaniu@mtini.io", null);
		String index = "wallets_";
		boolean active = true;
		JSONArray jsonArrayWalletRes = esDao.getWallet(index, true, address);
		JSONObject jsonWalletObjectRes = jsonArrayWalletRes.length()==0?null:jsonArrayWalletRes.getJSONObject(0);
		String jsonStr = jsonWalletObjectRes.getString("encodedString");
		
		byte[] decoded = Base64.getDecoder().decode(jsonStr.getBytes(Charsets.UTF_8));
		//ByteString decodedJson = ByteString.copyFrom(decoded);
		MtiniWallet mwallet =  MtiniWallet.parseFrom(decoded);
		
		String decodedJson = new Gson().toJson(mwallet);//JsonFormat.printer().print(mwallet);
		System.out.println(decodedJson);
		
		Gson gson = new Gson();
		Wallet wallet = gson.fromJson(decodedJson,Wallet.class);
        
		assert mwallet.getPrivateKeyHex().equals(wallet.getPrivateKeyHex());
		
		assert mwallet.getPublicKeyHex().equals(wallet.getPublicKeyHex());

		System.out.println("encrypted? "+wallet.isEncrypted());
		
		CharSequence passPhrase = BitcoinCryptoUtils.generatePassPhrase("4721", true);
		wallet.decryptPrivateKeyHex(passPhrase);
		
		System.out.println("encrypted? "+wallet.isEncrypted());
		
	}
	
	
	@Test
	public void esWalletEncrypt_Persist_Decrypt() throws WalletException, JSONException, InvalidProtocolBufferException, ElasticsearchPersistenceException{
		ESDAOImpl esDao =  new ESDAOImpl("http://localhost:9200");
		String customerId = "";
		String walletIndex = "wallets_";
		String esIndex = walletIndex+customerId.toLowerCase();
		Gson gson = new Gson();
		
		//CREATE NEW WALLET
		CharSequence passPhrase = BitcoinCryptoUtils.generatePassPhrase("PASS", true);
		Wallet newWallet = new Wallet("kaniumtiniemail",null,passPhrase);
		String walletAddress = Wallet.generateWalletAddress(newWallet.getId(), newWallet.getId2());
		
		//PERSIST WALLET TO ES
		//To remote object
		String objJson = gson.toJson(newWallet);
        MtiniWalletProtos.MtiniWallet.Builder builder = MtiniWalletProtos.MtiniWallet.newBuilder();
        //JsonFormat.parser().merge(objJson, builder);
        //Gson gson = new Gson();
        builder = gson.fromJson(objJson,  MtiniWalletProtos.MtiniWallet.Builder .class);
        ByteString data = builder.build().toByteString();
        //To json encodeed Str
		JSONObject walletJsonData = new JSONObject();
		walletJsonData.put("encodedString", Base64.getEncoder().encodeToString(data.toByteArray()));
		walletJsonData.put("walletAddress", walletAddress);
		walletJsonData.put("timestamp", System.currentTimeMillis());
		walletJsonData.put("active", true);
		JSONObject esResult = esDao.putRecord(walletJsonData.toString(), esIndex, "wallet", walletAddress);
		System.out.println(esResult);
		
		
		//RETRIEVE WALLET FROM STORE
		JSONArray jsonArrayWalletRes = esDao.getWallet(walletIndex, true, walletAddress);
		JSONObject jsonWalletObjectRes = jsonArrayWalletRes.length()==0?null:jsonArrayWalletRes.getJSONObject(0);
		String jsonStr = jsonWalletObjectRes.getString("encodedString");
		
		byte[] decoded = Base64.getDecoder().decode(jsonStr.getBytes(Charsets.ISO_8859_1));//because was encoded by Base64.getEncoder().encodeToString()
		//ByteString decodedJson = ByteString.copyFrom(decoded);
		MtiniWallet mwallet =  MtiniWallet.parseFrom(decoded);
		
		String decodedJson = gson.toJson(mwallet);//JsonFormat.printer().print(mwallet);
		System.out.println(decodedJson);
		
		
		Wallet wallet = gson.fromJson(decodedJson,Wallet.class);
        
		assert newWallet.getPrivateKeyHex().equals(wallet.getPrivateKeyHex());
		
		assert newWallet.getPublicKeyHex().equals(wallet.getPublicKeyHex());

		System.out.println("encrypted? "+wallet.isEncrypted());
		
		//CharSequence passPhrase = BitcoinCryptoUtils.generatePassPhrase("4721", true);
		wallet.decryptPrivateKeyHex(passPhrase);
		
		System.out.println("encrypted? "+wallet.isEncrypted());
		
	}
	
	@Test
	public void esWalletDecrypt_2() throws WalletException, JSONException, InvalidProtocolBufferException{
		ESDAOImpl esDao =  new ESDAOImpl("http://localhost:9200");
		
		String address = Wallet.generateWalletAddress("kaniumtiniemail", null);
		String index = "wallets_";
		boolean active = true;
		JSONArray jsonArrayWalletRes = esDao.getWallet(index, true, address);
		JSONObject jsonWalletObjectRes = jsonArrayWalletRes.length()==0?null:jsonArrayWalletRes.getJSONObject(0);
		String jsonStr = jsonWalletObjectRes.getString("encodedString");
		
		byte[] decoded = Base64.getDecoder().decode(jsonStr.getBytes(Charsets.ISO_8859_1));
		//ByteString decodedJson = ByteString.copyFrom(decoded);
		MtiniWallet mwallet =  MtiniWallet.parseFrom(decoded);
		Gson gson = new Gson();
		String decodedJson = gson.toJson(mwallet);//JsonFormat.printer().print(mwallet);
		System.out.println(decodedJson);
		
		Wallet wallet = gson.fromJson(decodedJson,Wallet.class);
        
		assert mwallet.getPrivateKeyHex().equals(wallet.getPrivateKeyHex());
		
		assert mwallet.getPublicKeyHex().equals(wallet.getPublicKeyHex());

		System.out.println("encrypted? "+wallet.isEncrypted());
		
		CharSequence passPhrase = BitcoinCryptoUtils.generatePassPhrase("PASS", true);
		wallet.decryptPrivateKeyHex(passPhrase);
		
		System.out.println("encrypted? "+wallet.isEncrypted());
		
		passPhrase = BitcoinCryptoUtils.generatePassPhrase("BADPASS", true);
		try{
			wallet.decryptPrivateKeyHex(passPhrase);
			System.out.println("encrypted? "+wallet.isEncrypted());
			
		}catch(KeyCrypterException e){
			e.printStackTrace();
		}
		
	}
}
