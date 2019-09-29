package com.ianford.podcasts.tal.db.writer;

import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Writes to the person table
 */
public class PersonWriter implements Consumer<EpisodeRecord> {

    private static final Logger logger = LogManager.getLogger();

    private final Connection connection;

    /**
     * Constructor
     *
     * @param connection
     */
    public PersonWriter(Connection connection) {
        this.connection = connection;
    }

    /**
     * mysql> describe people; +-------+--------------+------+-----+---------+----------------+ | Field | Type         |
     * Null | Key | Default | Extra          | +-------+--------------+------+-----+---------+----------------+ | id |
     * int(11)      | NO   | PRI | NULL    | auto_increment | | name  | varchar(150) | YES  |     | NULL    | |
     * +-------+--------------+------+-----+---------+----------------+ 2 rows in set (0.00 sec)
     */
    @Override
    public void accept(EpisodeRecord episodeRecord) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("insert into `tal`.`people` values(?)");
            preparedStatement.setString(1, episodeRecord.getSpeakerName());
            preparedStatement.execute();
        } catch (SQLException e) {
            logger.debug("Error writing person", e);
        }
    }
}
