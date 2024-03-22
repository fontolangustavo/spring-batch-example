package com.fontolan.springbatchexample;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {
    @Bean
    public Job doSayHello(JobRepository jobRepository, Step step) {
        return new JobBuilder("doSayHello", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step someFunctionToSayHello(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("someFunctionToSayHello", jobRepository)
            .tasklet(
                (StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("Hello Spring Batch");
                    return RepeatStatus.FINISHED;
                },
                transactionManager
            )
            .build();
    }

}
