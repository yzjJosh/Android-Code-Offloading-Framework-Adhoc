package handler;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.GetAvailableServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.engine.host.Host;
import mobilecloud.server.handler.Handler;
import server.TimedLRUCache;
@Log4j2
public class GetAvailableServerRequestHandler implements Handler{
	
	private TimedLRUCache<Host> cache;
	public GetAvailableServerRequestHandler(TimedLRUCache<Host> cache) {
		this.cache = cache;
	}
	
	public Response handle(Request request) throws Exception {
		if(!(request instanceof GetAvailableServerRequest)) {
			 throw new IllegalArgumentException(request.toString());
		}
		
		GetAvailableServerResponse resp = new GetAvailableServerResponse();
		resp.setSuccess(true);
		List<Host> list = cache.getAll();
		log.info("Available Server:" + list);
		resp.setServerList(list);
		return resp;
	}
}
