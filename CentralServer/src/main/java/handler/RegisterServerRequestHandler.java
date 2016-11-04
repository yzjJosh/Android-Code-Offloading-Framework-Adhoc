package handler;

import lombok.extern.log4j.Log4j2;
import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.RegisterServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.engine.host.Host;
import mobilecloud.server.handler.Handler;
import server.TimedLRUCache;
@Log4j2
public class RegisterServerRequestHandler implements Handler{
	
	public TimedLRUCache<Host> cache;
	public RegisterServerRequestHandler(TimedLRUCache<Host> cache) {
		this.cache = cache;
	}
	
	public Response handle(Request request) throws Exception {
		if(!(request instanceof RegisterServerRequest)) {
			 throw new IllegalArgumentException(request.toString());
		}
		
		RegisterServerRequest req = (RegisterServerRequest) request;
		int serverPort = req.getServerPort();
		String serverIp = req.getFromIp();
		Host host = new Host(serverIp, serverPort); 
		cache.add(host);
		log.info("New Host added: " + host);
		Response resp = new RegisterServerResponse();
		resp.setSuccess(true);
		return resp;
	}
}
