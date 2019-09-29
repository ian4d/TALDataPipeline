package com.ianford.podcasts.tal.db.reader;

import com.ianford.podcasts.model.BasicEpisodeRecord;
import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

public class EpisodeRecordResultSetHandler implements ResultSetHandler<Stream<EpisodeRecord>> {

    private static final Logger logger = LogManager.getLogger();

    /**
     * +----------------+--------------+------+-----+---------+-------+ | Field          | Type         | Null | Key |
     * Default | Extra | +----------------+--------------+------+-----+---------+-------+ | episode_number | int(11) |
     * NO   |     | NULL    |       | | act_number     | int(11)      | NO   |     | NULL    |       | | speaker_role |
     * varchar(50)  | NO   |     | NULL    |       | | speaker_name   | varchar(150) | NO   |     | NULL    | | |
     * timestamp      | varchar(50)  | NO   |     | NULL    |       | | text           | text         | NO   |     |
     * NULL    |       | +----------------+--------------+------+-----+---------+-------+
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */

    @Override
    public Stream<EpisodeRecord> handle(ResultSet resultSet) throws SQLException {
        Stream.Builder<EpisodeRecord> streamBuilder = Stream.builder();
        while (resultSet.next()) {
            EpisodeRecord record = new BasicEpisodeRecord();
            record.setActNumber(resultSet.getInt("act_number"));
            record.setEpisodeNumber(resultSet.getInt("episode_number"));
            record.setSpeakerName(resultSet.getString("speaker_name"));
            record.setSpeakerRole(resultSet.getString("speaker_role"));
            record.setStartTime(resultSet.getString("timestamp"));
            record.setText(resultSet.getString("text"));
            streamBuilder.accept(record);
        }
        return streamBuilder.build();
    }
}
