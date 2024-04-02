package com.fontolan.springbatchexample.entrypoint.controller;

import com.fontolan.springbatchexample.entrypoint.controller.config.batch.ChunkBatchConfig;
import com.fontolan.springbatchexample.entrypoint.controller.config.batch.SimpleBatchConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/batch")
public class ExampleController {
    private final JobLauncher jobLauncher;
    private final Job jobBatch;
    private final Job jobChunkBatch;

    public ExampleController(
            JobLauncher jobLauncher,
            SimpleBatchConfig batchConfig,
            ChunkBatchConfig chunkBatchConfig
    ) {
        this.jobLauncher = jobLauncher;
        this.jobBatch = batchConfig.doSayHello();
        this.jobChunkBatch = chunkBatchConfig.doJobWithChunk();
    }

    @GetMapping("/simple-example")
    public ResponseEntity<Boolean> simpleBatch(@RequestParam("name") String name) throws Exception {
        var response = jobLauncher.run(jobBatch, new JobParametersBuilder()
                .addString("UUID", UUID.randomUUID().toString())
                .addString("name", name)
                .toJobParameters());

        return ResponseEntity.ok().body(true);
    }
    @GetMapping("/simple-chunk-example")
    public ResponseEntity<Boolean> chunkBatch(@RequestParam("name") String name) throws Exception {
        var response = jobLauncher.run(jobChunkBatch, new JobParametersBuilder()
                .addString("UUID", UUID.randomUUID().toString())
                .addString("name", name)
                .toJobParameters());

        return ResponseEntity.ok().body(true);
    }
}
