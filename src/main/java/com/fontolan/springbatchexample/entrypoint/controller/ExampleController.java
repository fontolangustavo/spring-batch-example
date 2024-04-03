package com.fontolan.springbatchexample.entrypoint.controller;

import com.fontolan.springbatchexample.entrypoint.controller.config.batch.ChunkBatchConfig;
import com.fontolan.springbatchexample.entrypoint.controller.config.batch.FileBatchConfig;
import com.fontolan.springbatchexample.entrypoint.controller.config.batch.SimpleBatchConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/batch")
public class ExampleController {
    private static final String UPLOAD_DIR = "/path/temp/upload";
    private final JobLauncher jobLauncher;
    private final Job jobBatch;
    private final Job jobChunkBatch;
    private final Job jobFileBatch;

    public ExampleController(
            JobLauncher jobLauncher,
            SimpleBatchConfig batchConfig,
            ChunkBatchConfig chunkBatchConfig,
            FileBatchConfig fileBatchConfig
    ) {
        this.jobLauncher = jobLauncher;
        this.jobBatch = batchConfig.doSayHello();
        this.jobChunkBatch = chunkBatchConfig.doJobWithChunk();
        this.jobFileBatch = fileBatchConfig.doProcessFile();
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

    @PostMapping("/file-chunk-example")
    public ResponseEntity<Boolean> fileBatch(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) throws Exception {
        Resource resource = new InputStreamResource(file.getInputStream());

        byte[] bytes = file.getBytes();
        Path path = Paths.get(file.getOriginalFilename());
        Files.write(path, bytes);

        var response = jobLauncher.run(jobFileBatch, new JobParametersBuilder()
                .addString("UUID", UUID.randomUUID().toString())
                .addString("input.file.name", file.getOriginalFilename())
                .addString("input.file.path", path.toFile().getAbsolutePath())
                .addString("name", name)
                .toJobParameters());

        return ResponseEntity.ok().body(true);
    }
}
