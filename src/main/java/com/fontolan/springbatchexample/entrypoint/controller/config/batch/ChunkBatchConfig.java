package com.fontolan.springbatchexample.entrypoint.controller.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ChunkBatchConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    public Job doJobWithChunk() {
        return new JobBuilder("doJobWithChunk", jobRepository)
                .start(someFunctionWithChunk())
                .build();
    }

    public Step someFunctionWithChunk() {
        return new StepBuilder("someFunctionWithChunk", jobRepository)
                .<List<Double>, Double>chunk(1, transactionManager)
                .reader(doGetMyBills())
                .processor(doCalculateMyBills())
                .writer(doResponseAmountMyBills())
                .build();
    }

    public IteratorItemReader<List<Double>> doGetMyBills() {
        var list = Arrays.asList(1.2, 4.5, 900.50, 195.70);

        return new IteratorItemReader<List<Double>>(List.of(list).iterator());
    }

    public FunctionItemProcessor<List<Double>, Double> doCalculateMyBills() {
        return new FunctionItemProcessor<List<Double>, Double>(item -> item.stream().reduce(
               0.0, Double::sum));
    }

    public ItemWriter<Double> doResponseAmountMyBills() {
        return item -> item.forEach(System.out::println);
    }

}
