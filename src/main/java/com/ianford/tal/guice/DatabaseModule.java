package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ianford.podcasts.io.FileLoader;
import com.ianford.podcasts.io.FileSaver;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.podcasts.tal.db.reader.*;
import com.ianford.podcasts.tal.db.writer.*;
import com.ianford.podcasts.tal.file.EpisodeRecordListBuilder;
import com.ianford.tal.data.DatabaseProperties;
import com.ianford.tal.guice.constants.NamedInjections;
import com.ianford.tal.steps.PersistModelStep;
import com.ianford.tal.steps.PrepareDBStep;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides utilities for interacting with our Database
 */
public class DatabaseModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring DatabaseModule");
    }

    /**
     * Provides a DataSource used by the database connection
     *
     * @param dbName     Name of the database
     * @param dbUser     Name of the user who will access data
     * @param dbPassword Password for the DB
     * @param dbHost     Host for the DB
     * @return DataSource
     */
    @Provides
    DataSource provideDataSource(@Named(DatabaseProperties.DATABASE_NAME) String dbName,
                                 @Named(DatabaseProperties.USERNAME) String dbUser,
                                 @Named(DatabaseProperties.PASSWORD) String dbPassword,
                                 @Named(DatabaseProperties.HOST) String dbHost) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName(dbHost);
        dataSource.setDatabaseName(dbName);
        dataSource.setUser(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }


    /**
     * Provides an object that can be used to execute queries against the database
     *
     * @param dataSource A datasource to execute queries against
     */
    @Provides
    QueryRunner provideQueryRunner(DataSource dataSource) {
        return new QueryRunner(dataSource);
    }

    /**
     * Provides a connection to our DB
     *
     * @param dbName     The name of the database
     * @param dbUser     The user who will access the data
     * @param dbPassword The password used to access the db
     * @return Connection
     * @throws SQLException
     */
    @Provides
    @Singleton
    Connection provideConnection(
            @Named(DatabaseProperties.DATABASE_NAME) String dbName,
            @Named(DatabaseProperties.USERNAME) String dbUser,
            @Named(DatabaseProperties.PASSWORD) String dbPassword) throws SQLException {
        String connectionString = String.format("jdbc:mysql://localhost/%s", dbName);
        return DriverManager.getConnection(connectionString, dbUser, dbPassword);
    }

    /**
     * Provides a consumer used to write records to our DB
     */
    @Provides
    @Singleton
    @Exposed
    DatabaseWriter provideDBWriter(Connection connection,
                                   QueryRunner queryRunner) {
        return new DatabaseWriter(Arrays.asList(
                new ActWriter(connection, queryRunner),
                new PersonWriter(connection),
                new EpisodeWriter(connection),
                new RecordWriter(connection)
        ));
    }


    @Exposed
    @Provides
    @Named(NamedInjections.EPISODE_RECORD_RESULT_SET_HANDLER)
    ResultSetHandler<Stream<EpisodeRecord>> provideEpisodeRecordResultSetHandler() {
        return new EpisodeRecordResultSetHandler();
    }

    /**
     * Supplies a stream of staff names
     *
     * @param queryRunner
     * @return
     */
    @Exposed
    @Provides
    @Named(NamedInjections.NAME_STREAM_SUPPLIER)
    Supplier<Stream<String>> provideStaffNameStreamSupplier(QueryRunner queryRunner) {
        return new StaffNameStreamSupplier(queryRunner, new StaffNameResultSetHandler());
    }

    /**
     * Function that returns a stream of EpisodeRecords for the provided staff member name
     *
     * @param queryRunner
     * @return
     */
    @Exposed
    @Provides
    @Named(NamedInjections.RECORD_STREAM_FUNCTION)
    Function<String, Stream<EpisodeRecord>> provideStaffRecordStreamFunction(QueryRunner queryRunner,
                                                                             @Named(NamedInjections.EPISODE_RECORD_RESULT_SET_HANDLER) ResultSetHandler<Stream<EpisodeRecord>> episodeRecordResultSetHandler) {
        return new StaffRecordStreamFunction(queryRunner, episodeRecordResultSetHandler);
    }


    @Exposed
    @Provides
    @Named(NamedInjections.WORD_STREAM_SUPPLIER)
    Supplier<Stream<String>> provideStaffWordStreamSuppler(
            @Named(NamedInjections.NAME_STREAM_SUPPLIER) Supplier<Stream<String>> staffNameStreamSupplier,
            @Named(NamedInjections.RECORD_STREAM_FUNCTION) Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction) {
        // TODO: Define actual blacklist, improve on this method
        return new StaffWordStreamSupplier(staffNameStreamSupplier, staffRecordStreamFunction, new ArrayList<>());
    }


    @Exposed
    @Provides
    @Named(NamedInjections.EPISODE_NUMBER_SUPPLIER)
    Supplier<Stream<String>> provideEpisodeNumberStreamSupplier(QueryRunner queryRunner) {
        return new EpisodeNumberStreamSupplier(queryRunner, new EpisodeNumberResultSetHandler());
    }


    @Exposed
    @Provides
    @Named(NamedInjections.EPISODE_RECORD_FUNCTION)
    Function<Integer, Stream<EpisodeRecord>> provideEpisodeRecordFunction(QueryRunner queryRunner,
                                                                          @Named(NamedInjections.EPISODE_RECORD_RESULT_SET_HANDLER) ResultSetHandler<Stream<EpisodeRecord>> episodeRecordResultSetHandler) {
        return new EpisodeRecordStreamFunction(queryRunner, episodeRecordResultSetHandler);
    }

    /**
     * @param episodeRecordListBuilder Compiles a List of EpisodeRecords to work with
     * @param dbWriter                 Writes to a DB
     * @return
     */
    @Provides
    @Exposed
    @Singleton
    PersistModelStep providePersistModelStep(EpisodeRecordListBuilder episodeRecordListBuilder,
                                             DatabaseWriter dbWriter) {
        return new PersistModelStep(episodeRecordListBuilder,
                dbWriter);
    }

    @Provides
    @Exposed
    @Singleton
    PrepareDBStep providePrepareDBStep(FileLoader fileLoader, Connection connection) {
        return new PrepareDBStep(fileLoader.apply("./sql/prepare_db.sql"),
                connection);
    }

    @Provides
    @Exposed
    @Singleton
    @Named(NamedInjections.FILE_SAVER)
    BiConsumer<String, String> provideFileSaver() {
        return new FileSaver();
    }
}
