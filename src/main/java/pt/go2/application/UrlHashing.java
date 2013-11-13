package pt.go2.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.go2.keystore.Uri;

public class UrlHashing {

	public VirtualFileSystem.Error run(InputStream bodyInputStream, VirtualFileSystem vfs)
	{
		try (final InputStream is = bodyInputStream;
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			final String postBody = br.readLine();

			if (postBody == null) {
				return VirtualFileSystem.Error.BAD_REQUEST;
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				return VirtualFileSystem.Error.BAD_REQUEST;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(postBody.substring(idx), true);

			if (uri == null) {
				return VirtualFileSystem.Error.BAD_REQUEST;
			}

			// Refuse banned

			if (vfs.isBanned(uri)) {
				return VirtualFileSystem.Error.FORBIDDEN_PHISHING;
			}

			// hash Uri

			final byte[] hashedUri = vfs.add(uri);

			if (hashedUri == null) {
				return VirtualFileSystem.Error.BAD_REQUEST;
			}

			return null;

		} catch (IOException e) {
			return VirtualFileSystem.Error.BAD_REQUEST;
		}
	}
	
}
