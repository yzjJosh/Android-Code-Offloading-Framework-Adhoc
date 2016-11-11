package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import handler.GetAvailableServerRequestHandler;
import handler.RegisterServerRequestHandler;
import mobilecloud.api.deliverer.Deliverer;
import mobilecloud.api.deliverer.GetAvailableServerResponseDeliverer;
import mobilecloud.api.deliverer.RegisterServerResponseDeliverer;
import mobilecloud.api.receiver.GetAvailableServerRequestReceiver;
import mobilecloud.api.receiver.Receiver;
import mobilecloud.api.receiver.RegisterServerRequestReceiver;
import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.GetAvailableServerResponse;
import mobilecloud.api.response.RegisterServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.engine.host.Host;
import mobilecloud.server.IllegalRequestException;
import mobilecloud.server.IllegalResponseException;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class CentralServer {
	public TimedLRUCache<Host> cache;
	private final Map<String, Handler> handlers;
	private final Map<String, Receiver<Request>> receivers;
	private final Map<String, Deliverer<Response>> deliverers;

	public CentralServer(long timeout) {
		handlers = new ConcurrentHashMap<>();
		receivers = new ConcurrentHashMap<>();
		deliverers = new ConcurrentHashMap<>();

		cache = new TimedLRUCache<>(timeout);
		receivers.put(RegisterServerRequest.class.getName(), new RegisterServerRequestReceiver());
		receivers.put(GetAvailableServerRequest.class.getName(), new GetAvailableServerRequestReceiver());

		deliverers.put(GetAvailableServerResponse.class.getName(), new GetAvailableServerResponseDeliverer());
		deliverers.put(RegisterServerResponse.class.getName(), new RegisterServerResponseDeliverer());

		handlers.put(RegisterServerRequest.class.getName(), new RegisterServerRequestHandler(cache));
		handlers.put(GetAvailableServerRequest.class.getName(), new GetAvailableServerRequestHandler(cache));
	}

	public void serve(Socket s) throws Exception {
		AdvancedObjectInputStreamWrapper in = new AdvancedObjectInputStreamWrapper(
				new BufferedInputStream(s.getInputStream()));
		AdvancedObjectOutputStreamWrapper out = new AdvancedObjectOutputStreamWrapper(
				new BufferedOutputStream(s.getOutputStream()));
		String type = (String) in.get().readObject();
		Receiver<Request> receiver = receivers.get(type);
		if (receiver == null) {
			throw new IllegalRequestException(type);
		}
		Request req = receiver.receive(in, out);
		Handler handler = handlers.get(type);
		if (handler == null) {
			throw new IllegalRequestException(type);
		}
		
		Response resp = handler.handle(req);
		Deliverer<Response> deliverer = deliverers.get(resp.getClass().getName());
		if (deliverer == null) {
			throw new IllegalResponseException(resp.getClass().getName());
		}
		out.get().writeObject(resp.getClass().getName());
		out.get().flush();
		deliverer.deliver(resp, in, out);
	}

	public static void main(String[] args) {
		CentralServer centralServer = new CentralServer(Config.TIME_OUT);
		CentralServerThread centralServerThread = new CentralServerThread(Config.PORT, centralServer);
		centralServerThread.start();

	}
}
