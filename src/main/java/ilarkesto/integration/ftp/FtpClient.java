/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.integration.ftp;

import ilarkesto.base.Sys;
import ilarkesto.core.auth.LoginData;
import ilarkesto.core.auth.LoginDataProvider;
import ilarkesto.core.base.Filepath;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.io.IO.StringInputStream;
import ilarkesto.json.JsonObject;
import ilarkesto.swing.LoginPanel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClient {

	public static void main(String[] args) {
		LoginData login = LoginPanel.showDialog(null, "FTP to localhost",
			new File(Sys.getUsersHomePath() + "/.ilarkesto/ftp.localhost.properties"));

		FtpClient ftp = new FtpClient("localhost", login);
		ftp.connect();
		String dirname = "test_" + System.currentTimeMillis();
		ftp.createDir(dirname);
		ftp.deleteDir(dirname);
		ftp.close();
	}

	private static final Log log = Log.get(FtpClient.class);

	private String server;
	private Integer port;
	private LoginDataProvider login;

	private FTPClient client;

	private String chmodForCreatedDirs;
	private String chmodForUploadedFiles;
	private boolean ignoreChmodFailureForUploadedFiles;

	public FtpClient(String server, LoginDataProvider login) {
		super();
		this.server = server;
		this.login = login;
	}

	public void deleteDir(String path) {
		for (FTPFile file : listFiles(path)) {
			if (file.isDirectory()) {
				deleteDir(path + "/" + file.getName());
			} else {
				deleteFile(path + "/" + file.getName());
			}
		}
		deleteFile(path);
	}

	public void deleteFile(String path) {
		FTPFile file = getFile(path);
		if (file == null) return;

		log.info("Delete:", path);
		boolean deleted;
		try {
			deleted = file.isDirectory() ? client.removeDirectory(path) : client.deleteFile(path);
		} catch (IOException ex) {
			throw new RuntimeException("Deleting remote file failed: " + path, ex);
		}

		if (!deleted)
			throw new RuntimeException("Deleting remote file failed: " + path + " | " + client.getReplyString());
	}

	public void setChmodForCreatedDirs(String chmodForCreatedDirs) {
		this.chmodForCreatedDirs = chmodForCreatedDirs;
	}

	public void setChmodForUploadedFiles(String chmodForUploadedFiles, boolean ignoreChmodFailureForUploadedFiles) {
		this.chmodForUploadedFiles = chmodForUploadedFiles;
		this.ignoreChmodFailureForUploadedFiles = ignoreChmodFailureForUploadedFiles;
	}

	public boolean isFileExisting(String path) {
		return getFile(path) != null;
	}

	public List<FTPFile> listFiles(String path) {
		ArrayList<FTPFile> ret = new ArrayList<FTPFile>();
		try {
			for (FTPFile file : client.listFiles(path)) {
				String name = file.getName();
				if (name.equals(".")) continue;
				if (name.equals("..")) continue;
				ret.add(file);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return ret;
	}

	public List<FTPFile> listFilesSortedByTime(String path) {
		List<FTPFile> files = listFiles(path);
		return Utl.sort(files, FtpClient.FILES_BY_TIME_COMPARATOR);
	}

	public JsonObject downloadJson(String path) {
		String json = downloadText(path);
		return new JsonObject(json);
	}

	public String downloadText(String path) {
		log.debug("download:", path);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			boolean loaded = client.retrieveFile(path, out);
			if (!loaded) throw new RuntimeException("Downloading file failed: " + path);
		} catch (IOException ex) {
			throw new RuntimeException("Downloading file failed: " + path, ex);
		}
		try {
			return new String(out.toByteArray(), IO.UTF_8);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void downloadFile(String path, File destination) {
		downloadFile(path, destination, false);
	}

	public void downloadFile(String path, File destination, boolean deleteRemotey) {
		log.debug("download:", path, "->", destination.getPath());

		IO.createDirectory(destination.getParentFile());
		File tmpFile = new File(destination.getParent() + "/" + destination.getName() + ".~downloading");

		BufferedOutputStream out;
		try {
			out = new BufferedOutputStream(new FileOutputStream(tmpFile));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(
					"Downloading file failed. Writing local file failed: " + tmpFile.getAbsolutePath(), ex);
		}

		try {
			boolean loaded = client.retrieveFile(path, out);
			out.close();
			if (!loaded) throw new RuntimeException("Downloading file failed: " + path);
		} catch (IOException ex) {
			IO.deleteQuiet(tmpFile);
			throw new RuntimeException("Downloading file failed: " + path, ex);
		}

		try {
			IO.move(tmpFile, destination, true);
			if (deleteRemotey) deleteFile(path);
		} finally {
			IO.deleteQuiet(tmpFile);
		}

	}

	public void uploadText(String path, String text) {
		log.info("Upload:", path);
		boolean success;
		try {
			success = client.storeFile(path, new StringInputStream(text));
		} catch (Exception ex) {
			throw new RuntimeException("Uploading failed: " + path, ex);
		}
		if (!success) throw new RuntimeException("Uploading failed: " + path + " -> " + client.getReplyString());

		chmodAfterUpload(path);
	}

	public void uploadFiles(String path, File[] files) {
		if (!Str.isBlank(path)) createDir(path);
		for (File file : files) {
			String filePath = Str.isBlank(path) ? file.getName() : path + "/" + file.getName();
			if (file.isDirectory()) {
				createDir(filePath);
				uploadFiles(filePath, file.listFiles());
			} else {
				uploadFile(filePath, file);
			}
		}
	}

	public void uploadFileIfNotThere(String path, File file) {
		log.debug("Upload:", path);
		if (file == null || !file.exists()) return;

		FTPFile ftpFile = getFile(path);
		if (isSame(ftpFile, file)) {
			log.debug("  Skipping upload, already there:", path);
			return;
		}

		upload(path, file);
	}

	private boolean isSame(FTPFile ftpFile, File file) {
		if (ftpFile == null) return file == null;
		if (ftpFile.getSize() != file.length()) return false;
		return true;
	}

	public void uploadFile(String path, File file) {
		log.info("Upload:", path);
		if (file == null || !file.exists()) return;

		if (file.isDirectory()) {
			try {
				client.makeDirectory(path);
			} catch (IOException ex) {
				throw new RuntimeException("Creating directory failed: " + path + " | " + client.getReplyString(), ex);
			}
			return;
		}

		upload(path, file);
	}

	private void upload(String path, File file) {
		boolean uploaded;
		try {
			uploaded = client.storeFile(path, new BufferedInputStream(new FileInputStream(file)));
		} catch (IOException ex) {
			throw new RuntimeException(
					"Uploading failed: " + path + " <- " + file.getAbsolutePath() + " | " + client.getReplyString(),
					ex);
		}
		if (!uploaded) throw new RuntimeException("Uploading failed: " + path + " | " + client.getReplyString());

		chmodAfterUpload(path);
	}

	private void chmodAfterUpload(String path) {
		try {
			chmod(chmodForUploadedFiles, path);
		} catch (Exception ex) {
			if (ignoreChmodFailureForUploadedFiles) {
				log.info("command failed: chmod " + chmodForUploadedFiles + " " + path, ex);
			} else {
				log.error("command failed: chmod " + chmodForUploadedFiles + " " + path, ex);
			}
		}
	}

	public void executeCommand(String command) {
		log.info("Command:", command);

		boolean executed;
		try {
			executed = client.sendSiteCommand(command);
		} catch (IOException ex) {
			throw new RuntimeException("Command execution failed: " + command, ex);
		}
		if (!executed)
			throw new RuntimeException("Command execution failed: " + command + " | " + client.getReplyString());
	}

	public void createDir(String path) {
		if (path == null) return;
		if (existsDir(path)) return;
		log.info("Create dir:", path);
		boolean created;
		try {
			created = client.makeDirectory(path);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		if (!created) throw new RuntimeException("Creating directory failed: " + path);

		chmod(chmodForCreatedDirs, path);
	}

	public void chmod(String chmodExpression, String path) {
		if (chmodExpression == null) return;
		executeCommand("chmod " + chmodExpression + " " + path);
	}

	// public void changeDir(String path) {
	// log.debug("change dir:", path);
	// boolean changed;
	// try {
	// changed = client.changeWorkingDirectory(path);
	// } catch (IOException ex) {
	// throw new RuntimeException("Changing directory failed: " + path, ex);
	// }
	// if (!changed) throw new RuntimeException("Changing directory failed: " + path);
	// }

	public boolean existsFileOrDir(String path) {
		return getFile(path) != null;
	}

	public boolean existsDir(String path) {
		FTPFile file = getFile(path);
		if (file == null) return false;
		return file.isDirectory();
	}

	public FTPFile getFile(String path) {
		Filepath filepath = new Filepath(path);
		String parentPath = filepath.getParentAsString();
		String name = filepath.getLastElementName();
		for (FTPFile ftpFile : listFiles(parentPath)) {
			if (ftpFile.getName().equals(name)) return ftpFile;
		}
		return null;
	}

	public FtpClient setPort(Integer port) {
		this.port = port;
		return this;
	}

	public synchronized void close() {
		if (client == null) return;
		if (!client.isConnected()) return;
		try {
			client.disconnect();
		} catch (IOException ex) {
			log.error("FTP disconnect failed", ex);
		}
	}

	public synchronized void connect() {
		if (client != null && client.isConnected()) return;

		client = new FTPClient();

		log.info("Connecting", server);
		try {
			client.connect(server, port != null ? port.intValue() : client.getDefaultPort());
			if (!FTPReply.isPositiveCompletion(client.getReplyCode()))
				throw new RuntimeException("Nagative reply after connection: " + client.getReplyString());
		} catch (Exception ex) {
			log.error("FTP connection failed:", server, "->", ex);
			return;
		}

		LoginData loginData = login.getLoginData();
		try {
			if (!client.login(loginData.getLogin(), loginData.getPassword()))
				throw new RuntimeException("FTP-Login fehlgeschlagen: " + server);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		try {
			client.setFileType(FTPClient.BINARY_FILE_TYPE);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		client.enterLocalPassiveMode();
		client.setAutodetectUTF8(false);
		client.setCharset(Charset.forName(IO.UTF_8));
	}

	public static Comparator<FTPFile> FILES_BY_TIME_COMPARATOR = new Comparator<FTPFile>() {

		@Override
		public int compare(FTPFile a, FTPFile b) {
			return Utl.compare(a.getTimestamp(), b.getTimestamp());
		}

	};

}
