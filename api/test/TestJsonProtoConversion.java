import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel.TenantModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.ImageModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.NotesModel;
import io.mtini.rest.model.ModelProtoJsonCodecFactory;
import io.mtini.rest.model.ModelProtoJsonCodecFactory.ModelProtoJsonCodec;


public class TestJsonProtoConversion {
	
	
	private ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
	
	ImageModel imageProto;
	NotesModel notesProto;
	EstateModel estateProto;
	TenantModel tenantProto;
	
	@Before
	public void initData() {
		
		imageProto = ImageModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setModelId(UUID.randomUUID().toString())
				.build();
		
				
		notesProto = NotesModel.newBuilder().setDate(System.currentTimeMillis())
				.setModelId(UUID.randomUUID().toString())
				.setNoteText("My notes")
				.build();
		
		tenantProto = TenantModel.newBuilder()
				.setBalance(0.00)
				.setBuildingNumber("B")
				.setContacts("5551212")
				.setCurrency("KSH")
				.setNotes("Note")
				.setEstateId(UUID.randomUUID().toString())
				.setId(UUID.randomUUID().toString())
				.build();
		
		estateProto = EstateModel.newBuilder()
				.setAddress("makutano")
				.setContacts("5551212")
				.setDescription("my estate")
				.setId(UUID.randomUUID().toString())
				.setName("estate1")
				.addTenantData( tenantProto)
				.build();
		
	}
	
	@Test
	public void testConvertImageProto() throws InvalidProtocolBufferException, JSONException {
		
		ModelProtoJsonCodec<ImageModel> imageModelCodec = modelCodecFactory.getCodec(ImageModel.class);
		
		JSONObject imageJsonData  = imageModelCodec.encode(imageProto);
		
		Assert.assertNotNull(imageJsonData);

		ImageModel protoObj = imageModelCodec.decode(imageJsonData);
		
		Assert.assertNotNull(imageProto.getId());
		Assert.assertEquals(imageProto.getId(), protoObj.getId());
	}
	
	@Test
	public void testConvertNotesProto() throws JSONException, InvalidProtocolBufferException {
		
		ModelProtoJsonCodec<NotesModel> notesModelCodec = modelCodecFactory.getCodec(NotesModel.class);
		
		JSONObject noteJsonData  = notesModelCodec.encode(notesProto);
		
		Assert.assertNotNull(noteJsonData);

		NotesModel protoObj = notesModelCodec.decode(noteJsonData);
		
		Assert.assertEquals(notesProto.getNoteText(), protoObj.getNoteText());
	}
	
	@Test
	public void testConvertTenantProto() throws InvalidProtocolBufferException, JSONException {
		
		
		ModelProtoJsonCodec<TenantModel> tenantModelCodec = modelCodecFactory.getCodec(TenantModel.class);
		
		JSONObject tenantJsonData  = tenantModelCodec.encode(tenantProto);
		
		Assert.assertNotNull(tenantJsonData);

		TenantModel protoObj = tenantModelCodec.decode(tenantJsonData);
		
		Assert.assertNotNull(tenantProto.getId());
		Assert.assertEquals(tenantProto.getId(), protoObj.getId());
		
	}
	
	@Test
	public void testConvertEstateProto() throws InvalidProtocolBufferException, JSONException {
		
		
		ModelProtoJsonCodec<EstateModel> estateModelCodec = modelCodecFactory.getCodec(EstateModel.class);
		
		JSONObject estateJsonData  = estateModelCodec.encode(estateProto);
		
		Assert.assertNotNull(estateJsonData);

		EstateModel protoObj = estateModelCodec.decode(estateJsonData);
		
		Assert.assertNotNull(estateProto.getId());
		Assert.assertEquals(estateProto.getId(), protoObj.getId());
		
	}
	
}