package com.fontolan.springbatchexample.entrypoint.controller.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SimpleBatchConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    public Job doSayHello() {
        return new JobBuilder("doSayHello", jobRepository)
                .start(someFunctionToSayHello())
                .next(anotherFunctionToSayHello())
                .next(otherFunctionToSayUUID())
                .build();
    }

    public Step someFunctionToSayHello() {
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

    public Step anotherFunctionToSayHello() {
        return new StepBuilder("anotherFunctionToSayHello", jobRepository)
                .tasklet(
                        (StepContribution contribution, ChunkContext chunkContext) -> {
                            System.out.println("Hello from another function Spring Batch");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager
                )
                .build();
    }

    public Step otherFunctionToSayUUID() {
        return new StepBuilder("otherFunctionToSayUUID", jobRepository)
                .tasklet(
                        (StepContribution contribution, ChunkContext chunkContext) -> {
                            System.out.println("Hello from other function to say my UUID");
                            System.out.println("My UUID: " + chunkContext.getStepContext().getJobParameters().get("UUID"));
                            System.out.println("My Name: " + chunkContext.getStepContext().getJobParameters().get("name"));
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager
                )
                .build();
    }
}
