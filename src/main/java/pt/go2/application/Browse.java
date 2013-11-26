package pt.go2.application;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import pt.go2.fileio.Configuration;
import pt.go2.response.JsonResponse;

public class Browse extends AbstractHandler {

	public Browse(Configuration config, Resources vfs, BufferedWriter accessLog) {
		super(config, vfs, accessLog);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		mapper.writeValue(baos, vfs.browse());
		
		
		reply(exchange, new JsonResponse(baos.toByteArray()), false);
		
	}

}
