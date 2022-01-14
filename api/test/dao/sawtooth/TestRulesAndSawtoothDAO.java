package dao.sawtooth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import com.prelimtek.client.sawtooth.SawtoothDAO;
import com.prelimtek.utils.blockchain.SawtoothUtils;
import com.prelimtek.utils.crypto.Wallet;
import com.ptek.utils.json.JSONUtilities;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

import io.diy.api.props.Configurations;
import io.mtini.proto.MtiniWalletProtos.MtiniWallet;
import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel.TenantModel;
import io.mtini.proto.eat.EstateAccountProtos.PaymentSchedule;
import io.mtini.proto.eat.EstateAccountProtos.TenantStatus;

public class TestRulesAndSawtoothDAO {

	SawtoothDAO dao;
	Configurations config = Configurations.init();
	ECKey privateKey;
	byte[] publicKeyBytes;
	LedgerEntries entries;

	String estateId1 = UUID.randomUUID().toString();
	String tenantId1 = UUID.randomUUID().toString();

	@Before
	public void init() throws NoSuchAlgorithmException, ClientHandlerException, UniformInterfaceException,
			JSONException, IOException {

		dao = new SawtoothDAO(config.PERSISTENCE_SERVICE_URL_ARRAY[0]);
		/*
		 * Wallet wallet = Wallet.newBuilder().build(); wallet.generateKeyPairs(null);
		 * byte[] privateKeyBytes = wallet.getPrivateKeyBytes(); privateKey =
		 * ECKey.fromPrivate(privateKeyBytes);
		 */
		String privateKeyHex = "8aa754e6d81c5d50058df6a724bb4713b35f62242880ae7160968a4081ac6bb2";
		privateKey = ECKey.fromPrivate(Hex.decode(privateKeyHex));
		System.out.println(privateKey.getPrivateKeyAsHex());
		publicKeyBytes = ByteString.copyFromUtf8(privateKey.getPublicKeyAsHex()).toByteArray();

		entries = LedgerEntries.newBuilder().addEstateData(EstateModel.newBuilder().setName("Estate 1")
				.setAddress("1st Street").setId(estateId1)
				.addTenantData(TenantModel.newBuilder().setId(tenantId1).setEstateId(estateId1).setName("tenant1")
						.setContacts("+14155551212").setBuildingNumber("R1").setPaySchedule(PaymentSchedule.monthly)
						.setRent(3000).setCurrency("USD").setStatus(TenantStatus.new_tenant)))
				.setOperation(EstateAccountProtos.Operation.ADD_ESTATE).build();

		ByteString batchBytes = toBatches(entries);

		ClientResponse response = dao.sendBatch(batchBytes);

		JSONObject swtResponse = dao.processPersistResponse(response.getEntity(String.class).getBytes());

		System.out.println("swtResponse = " + swtResponse.toString());

		String status = (String) JSONUtilities.findByName(swtResponse, "status");
		System.out.println("status = " + status);

		Assert.assertTrue("expected PENDING ; got " + status, "PENDING".contentEquals(status));

	}

	public ByteString toBatches(LedgerEntries _entries) throws UnsupportedEncodingException, NoSuchAlgorithmException {

		// String dataAddress =
		// SawtoothUtils.calculateAddress(SawtoothUtils.FAMILY,publicKeyBytes);

		ByteString batches = SawtoothUtils.createBatchListByteString(privateKey, _entries.toByteString());

		return batches;

	}

	// write estate
	@Test
	public void testJSONUtilities() throws NoSuchAlgorithmException, IOException, ClientHandlerException,
			UniformInterfaceException, JSONException {

		entries = LedgerEntries.newBuilder()
				.addEstateData(EstateModel.newBuilder().setName("Estate 2").setAddress("2nd Street")
						.setId(UUID.randomUUID().toString()))
				.setOperation(EstateAccountProtos.Operation.ADD_ESTATE).build();

		ByteString batchBytes = toBatches(entries);

		ClientResponse response = dao.sendBatch(batchBytes);

		JSONObject swtResponse = dao.processPersistResponse(response.getEntity(String.class).getBytes());

		System.out.println("swtResponse = " + swtResponse.toString());

		String status = (String) JSONUtilities.findByName(swtResponse, "status");
		System.out.println("status = " + status);
		Assert.assertNotNull("expects status is not null", status);
		Assert.assertTrue("expects status is string", status instanceof String);

		String link = (String) JSONUtilities.findByName(swtResponse, "link");
		System.out.println("link = " + link);
		Assert.assertNotNull("expects link is not null", link);
		Assert.assertTrue("expects link is string", link instanceof String);

		String id = (String) JSONUtilities.findByName(swtResponse, "id");
		System.out.println("id = " + id);
		Assert.assertNotNull("expects id is not null", id);
		Assert.assertTrue("expects id is string", id instanceof String);

		Object data = JSONUtilities.findByName(swtResponse, "data");
		System.out.println("data = " + data);
		Assert.assertTrue("expects jsonobject", data instanceof JSONObject);

		Object blah = JSONUtilities.findByName(swtResponse, "blah");
		System.out.println("blah = " + blah);
		Assert.assertNull("expects no value for blah", blah);

		System.out.println("statusCode = " + response.getStatus());

		if (response.getStatus() == 200 || response.getStatus() == 202) {
			// committed or pending

		} else {
			// TODO search for failed transactions and retry?

		}

		Assert.assertTrue("expected PENDING ; got " + status, "PENDING".contentEquals(status));

	}

	@Test
	public void testChangeCurrency() throws NoSuchAlgorithmException, IOException, ClientHandlerException,
			UniformInterfaceException, JSONException {

		entries = LedgerEntries.newBuilder()
				.addEstateData(EstateModel.newBuilder().addTenantData(
						entries.toBuilder().getEstateDataBuilder(0).getTenantDataBuilder(0).setCurrency("KSH")))
				.setOperation(EstateAccountProtos.Operation.EDIT_TENANT).build();

		ByteString batchBytes = toBatches(entries);

		ClientResponse response = dao.sendBatch(batchBytes);

		JSONObject swtResponse = dao.processPersistResponse(response.getEntity(String.class).getBytes());

		System.out.println("swtResponse = " + swtResponse.toString());

		String status = (String) JSONUtilities.findByName(swtResponse, "status");
		System.out.println("status = " + status);

		if (response.getStatus() == 200 || response.getStatus() == 202) {
			// committed or pending

		} else {
			// TODO search for failed transactions and retry?

		}

		Assert.assertTrue("expected PENDING ; got " + status, "PENDING".contentEquals(status));

	}

	@Test
	public void testMakeEarlyPayment() throws NoSuchAlgorithmException, IOException, ClientHandlerException,
			UniformInterfaceException, JSONException {

		entries = LedgerEntries.newBuilder()
				.addEstateData(EstateModel.newBuilder().addTenantData(
						entries.toBuilder().getEstateDataBuilder(0).getTenantDataBuilder(0).setRent(1000)))
				.setOperation(EstateAccountProtos.Operation.EDIT_ESTATE).build();

		ByteString batchBytes = toBatches(entries);

		ClientResponse response = dao.sendBatch(batchBytes);

		JSONObject swtResponse = dao.processPersistResponse(response.getEntity(String.class).getBytes());

		System.out.println("swtResponse = " + swtResponse.toString());

		String status = (String) JSONUtilities.findByName(swtResponse, "status");

		System.out.println("status = " + status);

		if (response.getStatus() == 200 || response.getStatus() == 202) {
			// committed or pending

		} else {
			// TODO search for failed transactions and retry?

		}

		Assert.assertTrue("expected PENDING ; got " + status, "PENDING".contentEquals(status));
	}

	@Test
	public void testMakeLatePayment() {
	}

	@Test
	public void testMakeNoPayment_UpdateSchedule() {
	}

	String topic = "KaniuASawtoothRes";

	@Test
	public void testMakeGoodTransactionWithZMQResultMonitor() throws NoSuchAlgorithmException, IOException,
			ClientHandlerException, UniformInterfaceException, JSONException, InterruptedException {

		entries = LedgerEntries.newBuilder()
				.addEstateData(EstateModel.newBuilder().addTenantData(
						entries.toBuilder().getEstateDataBuilder(0).getTenantDataBuilder(0).setRent(1000)))
				.setOperation(EstateAccountProtos.Operation.EDIT_ESTATE).build();

		ByteString batchBytes = toBatches(entries);

		ClientResponse response = dao.sendBatch(batchBytes);

		JSONObject swtResponse = dao.processPersistResponse(response.getEntity(String.class).getBytes());

		System.out.println("swtResponse = " + swtResponse.toString());

		String status = (String) JSONUtilities.findByName(swtResponse, "status");
		String result_url = (String) JSONUtilities.findByName(swtResponse, "link");
		System.out.println("status = " + status);
		System.out.println("statusCode = " + response.getStatus());
		Assert.assertTrue("expected PENDING ; got " + status, "PENDING".contentEquals(status));

		if (response.getStatus() == 200 || response.getStatus() == 202) {
			// committed or pending
			if (status.contentEquals("PENDING")) {
				// start zmq push for results
				ZMQ.Context context = ZMQ.context(1);

				// Socket to talk to server
				System.out.println("Connecting to testMakeGoodTransactionWithZMQResultMonitor server…");

				ZMQ.Socket requester = context.socket(ZMQ.PUSH);
				// requester.setIdentity("Kaniu".getBytes());
				requester.bind("tcp://localhost:5562");

				final LocalDateTime endTime = LocalDateTime.now().plusMinutes(1);
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						LocalDateTime now = LocalDateTime.now();
						System.out.println("Time : " + now.toLocalTime());
						// TODO Auto-generated method stub
						requester.sendMore(topic);

						try {
							JSONObject res = dao.getResults(result_url);

							String status = (String) JSONUtilities.findByName(res, "status");

							requester.send(res.toString(), 0);

							if (!status.contentEquals("PENDING") || now.isAfter(endTime)) {
								// stop
								requester.close();
								context.term();
								cancel();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				};

				Timer timer = new Timer("sawtoothTimer");
				timer.scheduleAtFixedRate(task, 500L, 1000L);
				
				Thread.sleep(5000);

			}

		} else {
			// TODO search for failed transactions and retry?

		}

	}
	
	
	@Test
	public void testMakeInvalidTransactionWithZMQResultMonitor() throws NoSuchAlgorithmException, IOException,
			ClientHandlerException, UniformInterfaceException, JSONException, InterruptedException {

		entries = LedgerEntries.newBuilder()
				.addEstateData(EstateModel.newBuilder().addTenantData(
						entries.toBuilder().getEstateDataBuilder(0).getTenantDataBuilder(0).setCurrency("KSH")))
				.setOperation(EstateAccountProtos.Operation.EDIT_TENANT).build();

		ByteString batchBytes = toBatches(entries);

		ClientResponse response = dao.sendBatch(batchBytes);

		JSONObject swtResponse = dao.processPersistResponse(response.getEntity(String.class).getBytes());

		System.out.println("swtResponse = " + swtResponse.toString());

		String status = (String) JSONUtilities.findByName(swtResponse, "status");
		String result_url = (String) JSONUtilities.findByName(swtResponse, "link");
		System.out.println("status = " + status);
		System.out.println("statusCode = " + response.getStatus());
		Assert.assertTrue("expected PENDING ; got " + status, "PENDING".contentEquals(status));

		if (response.getStatus() == 200 || response.getStatus() == 202) {
			// committed or pending
			if (status.contentEquals("PENDING")) {
				// start zmq push for results
				ZMQ.Context context = ZMQ.context(1);

				// Socket to talk to server
				System.out.println("Connecting to testMakeInvalidTransactionWithZMQResultMonitor server…");

				ZMQ.Socket requester = context.socket(ZMQ.PUSH);
				// requester.setIdentity("Kaniu".getBytes());
				requester.bind("tcp://localhost:5562");

				final LocalDateTime endTime = LocalDateTime.now().plusMinutes(1);
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						LocalDateTime now = LocalDateTime.now();
						System.out.println("Time : " + now.toLocalTime());
						// TODO Auto-generated method stub
						requester.sendMore(topic);

						try {
							JSONObject res = dao.getResults(result_url);

							String status = (String) JSONUtilities.findByName(res, "status");

							requester.send(res.toString(), 0);

							if (!status.contentEquals("PENDING") || now.isAfter(endTime)) {
								// stop
								requester.close();
								context.term();
								cancel();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				};

				Timer timer = new Timer("sawtoothTimer");
				timer.scheduleAtFixedRate(task, 500L, 2000L);
				
				Thread.sleep(10000);

			}

		} else {
			// TODO search for failed transactions and retry?

		}

	}

	@Test
	public void testRecieveTrandactionDataZMQ_SUB() {

		ZMQ.Context context = ZMQ.context(1);

		// Socket to talk to server
		System.out.println("Connecting to testRecieveTrandactionDataZMQ_SUB …");

		String identity = "Kaniu" + Math.random();
		ZMQ.Socket requester = context.socket(ZMQ.SUB);
		requester.connect("tcp://localhost:5562");
		requester.setIdentity(identity.getBytes());
		requester.subscribe(topic.getBytes(ZMQ.CHARSET));

		while (true) {
			final String address = new String(requester.recv(0));
			// Read message contents
			final String contents = new String(requester.recv(0));

			System.out.println("Receiving -> " + address + " : " + contents);
		}
		// requester.close();
		// context.term();
	}

	@After
	public void cleanup() {
		// dao.
	}

}
