package pt.go2.fileio;

import java.util.List;

import pt.go2.response.AbstractResponse;

public interface FileSystemInterface {

	void start();

	void stop();

	AbstractResponse getFile(final String filename);

	List<String> browse();

}
