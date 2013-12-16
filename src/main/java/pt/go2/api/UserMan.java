package pt.go2.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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
	public static final String USER_NEW_PASSWORD = "newpassword";

	private static final int KEY_LENGTH = 160;
	private static final int HASH_ITERATIONS = 10000;

	private final Path path;

	public UserMan(final Configuration config) {
		this.path = Paths.get(config.USERS_FOLDER);
	}

	public boolean exist(final String username) {
		final String filename = getUserDataFilename(username);
		return new File(filename).exists();
	}

	public boolean save(final Map<String, String> user) {

		try (final FileWriter file = new FileWriter(getUserDataFilename(user));) {

			final ObjectMapper mapper = new ObjectMapper();
			final String line = mapper.writeValueAsString(user);

			file.write(line);
			file.flush();

			return true;

		} catch (IOException e) {
			return false;
		}
	}

	private Map<String, String> load(final String username) {

		final File f = new File(getUserDataFilename(username));
		final ObjectMapper mapper = new ObjectMapper();

		final TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
		};

		try {
			return mapper.readValue(f, typeRef);
		} catch (IOException e) {
			return Collections.emptyMap();
		}
	}

	public boolean validate(final String username, final String validation) {

		final Map<String, String> user = load(username);

		if (user == null) {
			return false;
		}

		if (!validation.equals(user.get(USER_VALIDATION_TOKEN))) {
			return false;
		}

		user.remove(USER_VALIDATION_TOKEN);
		save(user);

		return true;
	}

	public boolean login(String username, String password) {

		final Map<String, String> user = load(username);
		return password.equals(user.get(USER_PASSWORD));
	}

	public boolean changePassword(Map<String, String> values) {

		final String username = values.get(UserMan.USER_NAME);
		final String password = values.get(UserMan.USER_PASSWORD);

		if (!login(username, password)) {
			return false;
		}
		final String newPassword = values.get(UserMan.USER_NEW_PASSWORD);
		changePassword(username, newPassword);
		return true;
	}

	public boolean changePassword(final String username,
			final String newPassword) {

		final Map<String, String> user = load(username);

		if (user == null) {
			return false;
		}

		user.put(USER_PASSWORD, newPassword);

		save(user);

		return true;
	}

	public boolean validateUserProperty(final String field, final String value) {
		switch (field) {
		case USER_NAME:
			return value.matches("^[a-zA-Z0-9]4,12$");
		default:
			return true;
		}
	}

	public List<String> getLoginFields() {
		return Arrays.asList(new String[] { UserMan.USER_NAME,
				UserMan.USER_PASSWORD });
	}

	public List<String> getPasswordChangeFields() {
		return Arrays.asList(new String[] { UserMan.USER_EMAIL,
				UserMan.USER_NAME, UserMan.USER_PASSWORD, USER_NEW_PASSWORD });
	}

	public List<String> getPasswordResetFields() {
		return Arrays.asList(new String[] { UserMan.USER_NAME });
	}

	public static List<String> getUserFields() {
		return Arrays
				.asList(new String[] { UserMan.USER_EMAIL, UserMan.USER_NAME,
						UserMan.USER_PASSWORD, UserMan.USER_TIMEZONE });
	}

	public boolean authenticate(final char[] passwd, byte[] stored, byte[] salt) {
		final byte[] encrypted = encrypt(passwd, salt);
		return encrypted == null ? false : Arrays.equals(stored, encrypted);
	}

	public byte[] encrypt(final char[] passwd, final byte[] salt) {

		try {
			return SecretKeyFactory
					.getInstance("PBKDF2WithHmacSHA1")
					.generateSecret(
							new PBEKeySpec(passwd, salt, HASH_ITERATIONS,
									KEY_LENGTH)).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return new byte[0];
		}
	}

	public byte[] salt() {
		try {
			final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			final byte[] salt = new byte[8];
			random.nextBytes(salt);
			return salt;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	private String getUserDataFilename(final String name) {
		return path.resolve(name).toString();
	}

	private String getUserDataFilename(final Map<String, String> user) {
		final String name = user.get(USER_NAME);
		return path.resolve(name).toString();
	}
}
