package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.Resources;
import pt.go2.response.JsonResponse;

@Page(requireLogin = true, path = "api/view/")
public class View extends AbstractHandler {

	@Injected
	protected Resources vfs;
	
	@Override
	public void handle() throws IOException {
		reply(new JsonResponse(vfs.browse()));
	}
}
