package rapp_mini_server_sample1;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowerplatform.rapp_mini_server.remote_object.JavaRemoteObjectBase;
import com.flowerplatform.rapp_mini_server.shared.RemoteObject;
import com.flowerplatform.rapp_mini_server.shared.ResultCallback;

public class RappServiceClientTest {

	JavaRemoteObjectBase remoteObjectBase = new JavaRemoteObjectBase();

	public void test() throws JsonProcessingException {
		
		
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("00000000")
				.setRemoteAddress("localhost:9001")
				.setInstanceName("rappService")
				.setRequestSender(remoteObjectBase);

		List<RappServiceFile> files = Arrays.asList(new RappServiceFile[] { 
				new RappServiceFile("test-rapp.ino", 
						  "#include <Arduino.h>\r\n"
						+ "void setup() { Serial.begin(115200); Serial.println(\"\\n\\nStarted\");}\r\n"
						+ "void loop() { }"
				) 
		});
		ObjectMapper mapper = new ObjectMapper();
		String filesJson = mapper.writer().writeValueAsString(files);
		System.out.println(filesJson);
		
		ro.invokeMethod("buildRappForESP8266", new Object[] { "test-rapp", filesJson }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Java direct: " + result);
			}
		});
		
	}

	public static void main(String[] args) throws Exception {
		new RappServiceClientTest().test();
	}
	
}
