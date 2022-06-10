package cz.cuni.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * @author jachym.bartik
 */
interface DatabaseFunction<ReturnType> {

    ReturnType execute(Connection connection) throws SQLException, JsonProcessingException;

}
