package cz.matfyz.wrappercassandradb;

import com.datastax.oss.driver.api.core.CqlSession;

public class CassandraProvider {

    private final CassandraSettings settings;
    private CqlSession session;

    public CassandraProvider(CassandraSettings settings) {
        this.settings = settings;
    }

    public CqlSession getSession() {
        try {
            if (session == null || session.isClosed()) {
                session = settings.createSession();
            }
            return session;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create or retrieve CQLSession", ex);
        }
        
    }

    public void close() {
        try {
            if (session != null && !session.isClosed()) {
                session.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't close the Session", ex);
        }
    }
}
