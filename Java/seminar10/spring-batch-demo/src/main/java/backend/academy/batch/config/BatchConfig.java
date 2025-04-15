package backend.academy.batch.config;

import java.time.LocalDateTime;
import javax.sql.DataSource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;


@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchConfig {

    private final JdbcClient client;

    @Value("${app.input.file}")
    private String inputFilePath;

    @Bean
    public Job fraudPreventionJob(JobRepository jobRepository, Step processData) {
        return new JobBuilder("fraudPreventionJob", jobRepository)
            .start(processData)
            .listener(new JobExecutionListener() {
                @Override
                public void beforeJob(JobExecution jobExecution) {
                    log.info("Job has started! Current status: {}.", jobExecution.getStatus());
                }

                @Override
                public void afterJob(JobExecution jobExecution) {
                    log.info("Job has finished! Current status: {}.", jobExecution.getStatus());
                }
            })
            .build();
    }

    @Bean
    public Step processData(
        JobRepository jobRepository,
        ItemReader<AccountScore> reader,
        ItemWriter<AccountScore> writer,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("readDataFromCsv", jobRepository)
            .<AccountScore, AccountScore>chunk(10, transactionManager)
            .reader(reader)
            .processor(accountScore -> accountScore.setUpdatedAt(LocalDateTime.now()))
            .processor(score -> {
                client
                    .sql("insert into accounts (account_number) values (:accountNumber) on conflict do nothing")
                    .param("accountNumber", score.getAccountNumber())
                    .update();

                final var accountId = client
                    .sql("select account_id from accounts where account_number = :accountNumber")
                    .param("accountNumber", score.getAccountNumber())
                    .query(Long.class)
                    .single();

                return score.setAccountId(accountId);
            })
            .writer(writer)
            .build();
    }

    @Bean
    public ItemReader<AccountScore> reader() {
        return new FlatFileItemReaderBuilder<AccountScore>()
            .name("accountScoreReader")
            .resource(new FileSystemResource(inputFilePath))
            .delimited()
            .names("accountNumber", "score")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(AccountScore.class);
            }})
            .linesToSkip(1)
            .build();
    }

    @Bean
    public ItemWriter<AccountScore> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AccountScore>()
            .dataSource(dataSource)
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("""
                 insert into account_scores (account_id, score, updated_at)
                 values (:accountId, :score, :updatedAt)
                 on conflict (account_id) do update set score = :score, updated_at = :updatedAt
                 """)
            .build();
    }

    @Data
    @Accessors(chain = true)
    public static class AccountScore {

        private Long accountId;
        private String accountNumber;
        private int score;
        private LocalDateTime updatedAt;

    }

}
