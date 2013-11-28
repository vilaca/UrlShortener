package pt.go2.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.go2.fileio.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserMan {

	public static final String USER_NAME = "username";
	public static final String USER_EMAIL = "email";
	public static final String USER_PASSWORD = "password";
	public static final String USER_TIMEZONE = "timezone";
	public static final String USER_VALIDATION_TOKEN = "token";
	public static final String USER_CREATION_DATE = "date";

	private final Path path;

	public UserMan(final Configuration config) {
		this.path = Paths.get(config.USERS_FOLDER);
	}

	public boolean userExists(final String username) {
		final String filename = getUserDataFilename(username);
		return new File(filename).exists();
	}

	public boolean createUser(String username, final Map<String, String> user) {

		try (final FileWriter file = new FileWriter(
				getUserDataFilename(username));) {

			final ObjectMapper mapper = new ObjectMapper();
			final String line = mapper.writeValueAsString(user);

			file.write(line);
			file.write(System.getProperty("line.separator"));
			file.flush();

			return true;

		} catch (IOException e) {
			return false;
		}
	}

	public void login(final String username, final String password) {
	}

	public void logout() {
	}

	public Map<String, String> viewUser(final String username) {

		final File f = new File(getUserDataFilename(username));
		final ObjectMapper mapper = new ObjectMapper();

		final TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
		};

		try {
			final Map<String, String> user = mapper.readValue(f, typeRef);
			return user;
		} catch (IOException e) {
			return null;
		}
	}

	public boolean updateUser(final String username,
			final Map<String, String> user) {

		try (final FileWriter file = new FileWriter(username);) {

			final ObjectMapper mapper = new ObjectMapper();
			final String line = mapper.writeValueAsString(user);
			file.write(line);
			file.write(System.getProperty("line.separator"));
			file.flush();
			return true;

		} catch (IOException e) {
			return false;
		}
	}

	public boolean validate(final String field, final String value) {
		switch (field) {
		case USER_NAME:
			return value.matches("^[a-zA-Z0-9]4,12$");
		default:
			return true;
		}
	}

	public static List<String> getUserFields() {
		return Arrays
				.asList(new String[] { UserMan.USER_EMAIL, UserMan.USER_NAME,
						UserMan.USER_PASSWORD, UserMan.USER_TIMEZONE });
	}
	
	private String getUserDataFilename(final String name) {
		final String filename = path.resolve(name).toString();
		return filename;
	}
}
