package com.ianford.podcasts.tal.db.reader;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

/**
 * A DBUtils ResultSetHandler that returns a stream of all staff members who have contributed to TAL episodes
 */
public class StaffNameResultSetHandler implements ResultSetHandler<Stream<String>> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<String> handle(ResultSet resultSet) throws SQLException {
        logger.info("Handling results of acquiring staff names");
        Stream.Builder<String> streamBuilder = Stream.builder();
        while (resultSet.next()) {
            streamBuilder.accept(resultSet.getString("speaker_name"));
        }
        return streamBuilder.build();
    }
}
