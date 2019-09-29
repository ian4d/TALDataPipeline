package com.ianford.tal.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class PrepareDBStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final String query;
    private final Connection connection;

    public PrepareDBStep(String query, Connection connection) {
        this.query = query;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            connection.nativeSQL(query);
        } catch (SQLException e) {
            logger.error("Error while preparing data layer", e);
        }
    }
}
