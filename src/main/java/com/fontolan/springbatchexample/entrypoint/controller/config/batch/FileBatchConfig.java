package com.fontolan.springbatchexample.entrypoint.controller.config.batch;

import ch.qos.logback.core.subst.Tokenizer;
import com.fontolan.springbatchexample.core.domain.Person;
import jakarta.validation.ValidationException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.Validator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class FileBatchConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public Job doProcessFile() {
        return new JobBuilder("doProcessFile", jobRepository)
                .start(stepToProcessFile())
                .build();
    }

    public Step stepToProcessFile() {
        return new StepBuilder("stepToProcessFile", jobRepository)
                .<Person, Person>chunk(2, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    public ItemReader<Person> itemReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new PathResource("persons.csv"))
                .lineMapper(lineMapper())
                .linesToSkip(1)
                .targetType(Person.class)
                .build();
    }

    public LineMapper<Person> lineMapper() {
        return new LineMapper<Person>() {
            @Override
            public Person mapLine(String line, int lineNumber) throws Exception {
                String[] fields = line.split(",");
                return new Person(fields[0], fields[1], fields[2]);
            }
        };
    }

    public ItemProcessor<Person, Person> itemProcessor() {
        CompositeItemProcessor<Person, Person> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(
                List.of(
                    new ValidatingItemProcessor<>(validator())
                )
        );

        return compositeProcessor;
    }

    public Validator<Person> validator() {
        return new Validator<Person>() {
            @Override
            public void validate(Person value) throws ValidationException {
                if (!value.getEmail().contains("@")) {
                    throw new ValidationException("Email must not contain @");
                }
            }
        };
    }

    public ItemWriter<Person> itemWriter() {
        return items -> {
            for (Person item : items) {
                System.out.println("Writing item: " + item);
            }
        };
    }
}
