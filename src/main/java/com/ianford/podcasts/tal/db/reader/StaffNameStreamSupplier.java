package com.ianford.podcasts.tal.db.reader;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides a Stream of names of staff members for the podcast
 */
public class StaffNameStreamSupplier implements Supplier<Stream<String>> {

    private static final Logger logger = LogManager.getLogger();

    private final QueryRunner queryRunner;
    private final ResultSetHandler<Stream<String>> resultSetHandler;

    /**
     * Constructor
     *
     * @param queryRunner      Used to run SQL queries
     * @param resultSetHandler Used to convert SQL result set into Stream of Strings
     */
    public StaffNameStreamSupplier(QueryRunner queryRunner, ResultSetHandler resultSetHandler) {
        this.queryRunner = queryRunner;
        this.resultSetHandler = resultSetHandler;
    }


    @Override
    public Stream<String> get() {
        try {
            return queryRunner.query("SELECT DISTINCT speaker_name FROM tal.staff_view", resultSetHandler);
        } catch (SQLException e) {
            logger.error("Exception while executing query", e);
        }
        return Stream.empty();
    }
}
