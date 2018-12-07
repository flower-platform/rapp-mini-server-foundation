package rapp_mini_server_sample1;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import org.junit.Test;

public class RemoteObjectTest {

	private static final int N_TESTS = 13;
	
	private static final long TIMEOUT_MILLIS = 60 * 1000;
	
	
	private void startBrowser(String url) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
	    	try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}		
	}
	
	@Test
	public void remoteObjectTest() {
		RappMiniServerSample1Main main = new RappMiniServerSample1Main();
		main.start();
		startBrowser("http://localhost:8888/#/main");
		long tRef = System.currentTimeMillis();
		while (main.javaAppService.getTestResults().size() < N_TESTS && System.currentTimeMillis() - tRef < TIMEOUT_MILLIS) {
			try { Thread.sleep(1000); } catch (Exception e) { e.printStackTrace(); }
		}
		main.stop();
		System.out.println("\n\n******************** TEST RESULTS ****************************");
		System.out.println("Results received: " + main.javaAppService.getTestResults().size() + " of " + N_TESTS);
		for (Entry<String, String> result : main.javaAppService.getTestResults().entrySet()) {
			System.out.println(result.getKey() + ": " + result.getValue());
		}
	}
	
}
