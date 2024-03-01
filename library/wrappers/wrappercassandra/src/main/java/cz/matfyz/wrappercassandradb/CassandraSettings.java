package cz.matfyz.wrappercassandradb;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.net.InetSocketAddress;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class CassandraSettings {

    private String host;
    private int port; 
    private String database;
    private String username;
    private String password;
    private String datacenter = "datacenter1"; 

    public CassandraSettings(String host, int port, String database, String username, String password, String datacenter) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.datacenter = datacenter;
    }

    public CassandraSettings() {}

    public CqlSession createSession() {
        CqlSessionBuilder sessionBuilder = CqlSession.builder();
        sessionBuilder.addContactPoint(new InetSocketAddress(host, port))
                      .withLocalDatacenter(datacenter);

        if (username != null && password != null) {
            sessionBuilder.withAuthCredentials(username, password);
        }

        if (database != null) {
            sessionBuilder.withKeyspace(database);
        }

        return sessionBuilder.build();
    }
    // Getters and Setters
    public String gethost() {
        return host;
    }

    public void sethost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }
}