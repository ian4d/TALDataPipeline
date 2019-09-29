package com.ianford.podcasts.tal.db.writer;

import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Writes to the episodes table
 */
public class EpisodeWriter implements Consumer<EpisodeRecord> {
    private static final Logger logger = LogManager.getLogger();

    private final Connection connection;

    /**
     * Constructor
     *
     * @param connection
     */
    public EpisodeWriter(Connection connection) {
        this.connection = connection;
    }

    /**
     * mysql> describe episodes; +--------+--------------+------+-----+---------+-------+ | Field  | Type         | Null
     * | Key | Default | Extra | +--------+--------------+------+-----+---------+-------+ | number | int(11)      | NO |
     * PRI | NULL    |       | | title  | varchar(150) | NO   |     | NULL    |       |
     * +--------+--------------+------+-----+---------+-------+ 2 rows in set (0.00 sec)
     */
    @Override
    public void accept(EpisodeRecord episodeRecord) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into `tal`.`episodes` values(?, ?)");
            preparedStatement.setInt(1, episodeRecord.getEpisodeNumber());
            preparedStatement.setString(2, episodeRecord.getEpisodeTitle());
            preparedStatement.execute();
        } catch (SQLException e) {
            logger.debug("ERROR WRITING EPISODE", e);
        }
    }
}
