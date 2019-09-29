package com.ianford.podcasts.tal.db.writer;

import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Writes to the acts table
 */
public class ActWriter implements Consumer<EpisodeRecord> {

    private static final Logger logger = LogManager.getLogger();

    private final Connection connection;

    private final QueryRunner queryRunner;

    /**
     * Constructor
     *
     * @param connection
     * @param queryRunner
     */
    public ActWriter(Connection connection, QueryRunner queryRunner) {
        this.connection = connection;
        this.queryRunner = queryRunner;
    }

    /**
     * mysql> describe acts; +----------------+--------------+------+-----+---------+-------+ | Field          | Type |
     * Null | Key | Default | Extra | +----------------+--------------+------+-----+---------+-------+ | episode_number
     * | int(11)      | NO   | PRI | NULL    |       | | act_number     | int(11)      | NO   | PRI | NULL    |       |
     * | name           | varchar(150) | NO   |     | NULL    |       | +----------------+--------------+------+-----+---------+-------+
     * 3 rows in set (0.00 sec)
     */
    public void accept(EpisodeRecord episodeRecord) {
        try {
            // TODO: Replace with QueryRunner
            /*
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into `tal`.`acts` values(?, ?, ?)");
            preparedStatement.setInt(1, episodeRecord.getEpisodeNumber());
            preparedStatement.setInt(2, episodeRecord.getActNumber());
            preparedStatement.setString(3, episodeRecord.getActName());
            preparedStatement.execute();
            */


            queryRunner.insert("INSERT INTO `tal`.`acts` values(?, ?, ?)",
                    null,
                    episodeRecord.getEpisodeNumber(),
                    episodeRecord.getActNumber(),
                    episodeRecord.getActName()
            );

        } catch (SQLException e) {
            logger.debug("Error writing act", e);
        }
    }
}
