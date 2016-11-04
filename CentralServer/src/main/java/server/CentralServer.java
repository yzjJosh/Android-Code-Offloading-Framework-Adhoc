package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import handler.GetAvailableServerRequestHandler;
import handler.RegisterServerRequestHandler;
import mobilecloud.api.receiver.GetAvailableServerRequestReceiver;
import mobilecloud.api.receiver.Receiver;
import mobilecloud.api.receiver.RegisterServerRequestReceiver;
import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.Response;
import mobilecloud.engine.host.Host;
import mobilecloud.server.IllegalRequestException;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class CentralServer {
	public TimedLRUCache<Host> cache;
	private final Map<String, Handler> handlers;
	private final Map<String, Receiver> receivers;

	public CentralServer(long timeout) {
		handlers = new ConcurrentHashMap<>();
		receivers = new ConcurrentHashMap<>();
		cache = new TimedLRUCache<>(timeout);
		receivers.put(RegisterServerRequest.class.getName(), new RegisterServerRequestReceiver());
		receivers.put(GetAvailableServerRequest.class.getName(), new GetAvailableServerRequestReceiver());
		handlers.put(RegisterServerRequest.class.getName(), new RegisterServerRequestHandler(cache));
		handlers.put(GetAvailableServerRequest.class.getName(), new GetAvailableServerRequestHandler(cache));
	}

	public void serve(Socket s) throws Exception {
		ObjectInputStreamWrapper in = new AdvancedObjectInputStreamWrapper(new BufferedInputStream(s.getInputStream()));
		ObjectOutputStreamWrapper out = new ObjectOutputStreamWrapper(new BufferedOutputStream(s.getOutputStream()));
		String type = (String) in.get().readObject();
		Receiver receiver = receivers.get(type);
		if (receiver == null) {
			throw new IllegalRequestException(type);
		}
		Request req = receiver.receive(in, out);
		Handler handler = handlers.get(type);
		if (handler == null) {
			throw new IllegalRequestException(type);
		}
		Response resp = handler.handle(req);
		out.get().writeObject(resp);
		out.get().flush();
	}
}
