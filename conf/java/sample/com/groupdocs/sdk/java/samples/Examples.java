package com.groupdocs.sdk.java.samples;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.groupdocs.sdk.java.api.AntAPI;
import com.groupdocs.sdk.java.api.StorageAPI;
import com.groupdocs.sdk.java.model.AnnotationInfo;
import com.groupdocs.sdk.java.model.AnnotationReplyInfo;
import com.groupdocs.sdk.java.model.CreateAnnotationResponse;
import com.groupdocs.sdk.java.model.ListAnnotationsResponse;
import com.groupdocs.sdk.java.model.Rectangle;
import com.groupdocs.sdk.java.model.UploadResponse;
import com.groupdocs.sdk.java.samples.Examples;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.wordnik.swagger.runtime.common.APIInvoker;
import com.wordnik.swagger.runtime.common.GroupDocsUrlSigningSecurityHandler;

public class Examples {

	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.getDeserializationConfig().set(
				Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.getSerializationConfig().set(
				SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES,
				false);
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
				false);
	}

	static String privateKey = "<PRIVATE_KEY>";
	static String userId = "<CLIENT_ID>";
	static String fileId = "<FILE_ID>";
	static String apiServer = "https://dev-api.groupdocs.com/v2.0/";
	static APIInvoker apiInvoker = APIInvoker
			.initialize(new GroupDocsUrlSigningSecurityHandler(privateKey),
					apiServer, true);

	public static void main(String[] args) throws Exception {
		// createAnnotation();
//		listAnnotations();
		uploadFile();
	}

	// multipart file upload
	private static void uploadFile() throws Exception {
		StorageAPI.setApiInvoker(apiInvoker);
		File postData = new File(Examples.class.getClassLoader().getResource("com/groupdocs/sdk/java/samples/test.docx").toURI());
		UploadResponse response = StorageAPI.Upload(userId, "test.docx", "test DOC file ", postData);
		System.out.println(mapper.writeValueAsString(response));
	}

	// GET
	private static void listAnnotations() throws Exception {
		AntAPI.setApiInvoker(apiInvoker);
		ListAnnotationsResponse response = AntAPI.ListAnnotations(userId,
				fileId);
		System.out.println(mapper.writeValueAsString(response));
	}

	// POST
	private static void createAnnotation() throws Exception {
		AntAPI.setApiInvoker(apiInvoker);
		AnnotationInfo postData = new AnnotationInfo();

		Rectangle box = new Rectangle();
		box.setH(100d);
		box.setW(100d);
		box.setX(100d);
		box.setY(100d);

		AnnotationReplyInfo reply = new AnnotationReplyInfo();
		reply.setText("test message from java client library");

		postData.setType("Text");
		postData.setBox(box);
		postData.setReplies(Arrays.asList(new AnnotationReplyInfo[] { reply }));

		CreateAnnotationResponse response = AntAPI.CreateAnnotation(userId,
				fileId, postData);
		System.out.println(mapper.writeValueAsString(response));
	}
}
