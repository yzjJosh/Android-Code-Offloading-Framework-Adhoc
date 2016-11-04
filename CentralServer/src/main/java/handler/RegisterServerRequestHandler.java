package handler;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.RegisterServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.engine.host.Host;
import mobilecloud.server.handler.Handler;
import server.TimedLRUCache;

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
		String serverIp = req.getServerIp();
		cache.add(new Host(serverIp, serverPort));
		Response resp = new RegisterServerResponse();
		resp.setSuccess(true);
		return resp;
	}
}
