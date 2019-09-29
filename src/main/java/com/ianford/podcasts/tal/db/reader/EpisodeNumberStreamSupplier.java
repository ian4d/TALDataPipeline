package com.ianford.podcasts.tal.db.reader;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EpisodeNumberStreamSupplier implements Supplier<Stream<String>> {

    private static final Logger logger = LogManager.getLogger();

    private final QueryRunner queryRunner;
    private final ResultSetHandler<Stream<String>> resultSetHandler;

    public EpisodeNumberStreamSupplier(QueryRunner queryRunner,
                                       ResultSetHandler<Stream<String>> resultSetHandler) {
        this.queryRunner = queryRunner;
        this.resultSetHandler = resultSetHandler;
    }

    @Override
    public Stream<String> get() {
        try {
            return queryRunner.query("SELECT DISTINCT episode_number FROM tal.full_view", resultSetHandler);
        } catch (SQLException e) {
            logger.error("Exception while executing query", e);
        }
        return Stream.empty();
    }
}
