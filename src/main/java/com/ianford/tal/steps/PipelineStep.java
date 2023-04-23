package com.ianford.tal.steps;

import com.ianford.tal.model.PipelineConfig;

import java.io.IOException;

public interface PipelineStep {
    void run(PipelineConfig pipelineConfig) throws IOException;
}
