package pt.go2.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

public class JsonResponse extends AbstractResponse {

	final ObjectMapper mapper = new ObjectMapper();
	final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	final byte[] body;

	public JsonResponse(final Object response) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(baos, response );
		this.body = baos.toByteArray();
	}

	@Override
	public int getHttpStatus() {
		return 200;
	}

	@Override
	public byte[] run(final HttpExchange exchange) {
		return body;
	}

	@Override
	public String getMimeType() {
		return MIME_APP_JSON;
	}

}
