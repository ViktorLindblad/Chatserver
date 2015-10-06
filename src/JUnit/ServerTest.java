package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import PDU.JOIN;
import PDU.PDU;
import Server.Server;

public class ServerTest {
	
	private static boolean isSetUpDone = true;
	private static Server server;

	
	@Before
	public void setup() {
		
		if(isSetUpDone) {
			isSetUpDone = false;
			server = new Server(1555,"itchy.cs.umu.se",1337);
			for(int i=1; i<=255; i++) {
				serverTestClient SC = new serverTestClient(server,""+i);
			}
		}
	}
	
	@Test
	public void toManyClients() {

		assertEquals(255,server.getNames().size());
	}
}
