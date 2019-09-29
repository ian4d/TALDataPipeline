package com.ianford.podcasts.tal.db.reader;

import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.function.Function;
import java.util.stream.Stream;

public class EpisodeRecordStreamFunction implements Function<Integer, Stream<EpisodeRecord>> {

    private static final Logger logger = LogManager.getLogger();

    private final QueryRunner queryRunner;
    private final ResultSetHandler<Stream<EpisodeRecord>> resultSetHandler;

    public EpisodeRecordStreamFunction(QueryRunner queryRunner,
                                       ResultSetHandler<Stream<EpisodeRecord>> resultSetHandler) {
        this.queryRunner = queryRunner;
        this.resultSetHandler = resultSetHandler;
    }

    @Override
    public Stream<EpisodeRecord> apply(Integer episodeNumber) {
        try {
            return queryRunner.query("SELECT * FROM tal.full_view WHERE episode_number=? ORDER BY timestamp", resultSetHandler, episodeNumber);
        } catch (SQLException e) {
            logger.error("Exception while running query", e);
        }
        return Stream.empty();
    }
}
