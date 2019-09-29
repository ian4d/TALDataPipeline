package com.ianford.podcasts.tal.db.writer;

import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.function.Consumer;

public class RecordWriter implements Consumer<EpisodeRecord> {

    private static final Logger logger = LogManager.getLogger();

    private final Connection connection;

    /**
     * Constructor
     *
     * @param connection
     */
    public RecordWriter(Connection connection) {
        this.connection = connection;
    }

    /**
     * mysql> describe records; +----------------+-------------+------+-----+---------+-------+ | Field          | Type
     * | Null | Key | Default | Extra | +----------------+-------------+------+-----+---------+-------+ | episode_number
     * | int(11)     | NO   | PRI | NULL    |       | | act_number     | int(11)     | NO   | MUL | NULL |       | |
     * speaker_role   | varchar(50) | NO   |     | NULL    |       | | speaker_name     | varchar(150) | NO   | MUL |
     * NULL    |       | | timestamp      | varchar(50) | NO   | PRI | NULL    |       | | text | text        | YES  | |
     * NULL    |       | +----------------+-------------+------+-----+---------+-------+ 6 rows in set (0.01 sec)
     */
    @Override
    public void accept(EpisodeRecord episodeRecord) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into `tal`.`records` values(?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, episodeRecord.getEpisodeNumber());
            preparedStatement.setInt(2, episodeRecord.getActNumber());
            preparedStatement.setString(3, episodeRecord.getSpeakerRole());
            preparedStatement.setString(4, episodeRecord.getSpeakerName());
            preparedStatement.setString(5, episodeRecord.getStartTime());
            preparedStatement.setString(6, episodeRecord.getText());
            preparedStatement.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            // eat this entry
        } catch (SQLException e) {
            logger.error("SQL EXCEPTION", e);
        }
    }
}
