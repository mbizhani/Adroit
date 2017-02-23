package org.devocative.adroit.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InitDB {
	private static final Logger logger = LoggerFactory.getLogger(InitDB.class);

	private String driver;
	private String url;
	private String username;
	private String password;

	private List<String> scrips = new ArrayList<>();
	private String delimiter = ";";

	private Connection connection;

	// ------------------------------

	public InitDB setDriver(String driver) {
		this.driver = driver;
		return this;
	}

	public InitDB setUrl(String url) {
		this.url = url;
		return this;
	}

	public InitDB setUsername(String username) {
		this.username = username;
		return this;
	}

	public InitDB setPassword(String password) {
		this.password = password;
		return this;
	}

	public InitDB setDelimiter(String delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	public Connection getConnection() {
		return connection;
	}

	// ---------------

	public InitDB addScript(String file) {
		scrips.add(file);
		return this;
	}

	public void build() {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, username, password);
			logger.info("Connection Created!");

			for (String scrip : scrips) {
				logger.info("SCRIPT: {}", scrip);

				File f = new File(scrip);
				String sql = new String(Files.readAllBytes(f.toPath()));
				String[] statements = sql.split(delimiter);
				for (String statement : statements) {
					statement = statement.trim();
					if (!statement.isEmpty()) {
						logger.info("Execute: {}", statement);
						Statement st = connection.createStatement();
						st.execute(statement);
						st.close();
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
