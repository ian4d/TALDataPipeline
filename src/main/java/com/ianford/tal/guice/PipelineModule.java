package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.tal.Pipeline;
import com.ianford.tal.steps.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to provide and configure the pipeline that runs all of our pipeline steps
 */
public class PipelineModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring PipelineModule");
    }

    /**
     * Provides the object that will be invoked to run our steps
     *
     * @param pipelineStepList A List of steps to execute
     * @return Pipeline
     */
    @Provides
    @Exposed
    Pipeline providePipeline(List<PipelineStep> pipelineStepList) {
        return new Pipeline(pipelineStepList);
    }

    /**
     * Provides steps that will be executed as part of the pipeline
     */
    @Provides
    List<PipelineStep> providePipelineSteps(DownloadEpisodeStep downloadEpisodeStep
//                                            PrepareDBStep prepareDBStep,
//                                            PersistModelStep persistModelStep,
//                                            BuildContributorModelStep buildContributorModelStep,
//                                            BuildNLPModelStep buildNLPModelStep,
//                                            ExportContributorPagesStep exportContributorPagesStep,
//                                            BuildEpisodeModelStep buildEpisodeModelStep
    ) {
        List<PipelineStep> steps = new ArrayList<>();

        // Download any new/missing episodes
        steps.add(downloadEpisodeStep);

        // Prepare DB
        //steps.add(prepareDBStep);

        // Store Data
        //steps.add(persistModelStep);

        // Build the Contributor Model
        //steps.add(buildContributorModelStep);

        // Export pages for Jekyll
        //steps.add(exportContributorPagesStep);

        //steps.add(buildNLPModelStep);

//        steps.add(buildEpisodeModelStep);

        return steps;
    }
}
