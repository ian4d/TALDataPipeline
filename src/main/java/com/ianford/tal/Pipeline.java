package com.ianford.tal;

import com.ianford.tal.steps.PipelineStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

/**
 * Used to run all of the provided pipeline steps
 */
public class Pipeline {

    private static final Logger logger = LogManager.getLogger();

    private final Collection<PipelineStep> pipelineSteps;

    /**
     * Constructor
     *
     * @param pipelineSteps A Collection of steps to execute in the pipeline
     */
    public Pipeline(Collection<PipelineStep> pipelineSteps) {
        this.pipelineSteps = pipelineSteps;
    }

    /**
     * Runs the pipeline
     */
    public void runPipeline() {
        logger.info("Running pipeline");
        pipelineSteps.forEach(PipelineStep::run);
        logger.info("Pipeline complete");
    }
}
