# TAL Data Pipeline

This package executes a data pipeline that downloads transcripts of the podcast This American Life and then prepares
them for use in other projects.

## Components

### Pipeline Runner

The pipeline runner accepts a collection of ordered steps and runs them sequentially.

### Pipeline Steps

Each step has a small set of responsibilities expected to be executed in sequence as part of the data pipeline.