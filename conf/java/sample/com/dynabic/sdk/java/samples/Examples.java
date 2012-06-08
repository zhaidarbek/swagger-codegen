package com.dynabic.sdk.java.samples;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.dynabic.sdk.java.api.CustomersAPI;
import com.dynabic.sdk.java.model.CustomerRequest;
import com.dynabic.sdk.java.model.CustomerResponse;
import com.wordnik.swagger.runtime.common.APIInvoker;
import com.wordnik.swagger.runtime.common.DynabicUrlSigningSecurityHandler;

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

	static String clientKey = "19c7a0d97d2d4413aba5";
	static String privateKey = "19c7a0d97d2d4413aba5";
	static String apiServer = "http://stage-api.dynabic.com/billing/";
	static APIInvoker apiInvoker = APIInvoker
			.initialize(new DynabicUrlSigningSecurityHandler(clientKey, privateKey),
					apiServer, true);

	public static void main(String[] args) throws Exception {
//		getCustomer();
		createCustomer();
	}

	// GET
	private static void getCustomer() throws Exception {
		CustomersAPI.setApiInvoker(apiInvoker);
		CustomerResponse response = CustomersAPI.GetCustomer("14");
		System.out.println(mapper.writeValueAsString(response));
	}

	// POST
	private static void createCustomer() throws Exception {
		CustomersAPI.setApiInvoker(apiInvoker);
		CustomerRequest customer = new CustomerRequest();
		customer.setFirst_name("John");
		customer.setLast_name("Doe");
		customer.setEmail("hopefullysome@nonexisting.email");
		CustomerResponse response = CustomersAPI.AddCustomer("test", customer);
		System.out.println(mapper.writeValueAsString(response));
	}
}
