package com.example.pocbatch;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@Slf4j
public class PostJob extends QuartzJobBean {

    @Autowired
    private PostRepository postRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting job");
        postRepository.deleteAll();
        final RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Post[]> response = restTemplate.getForEntity("https://jsonplaceholder.typicode.com/posts", Post[].class);
        Post[] posts = response.getBody();
        if (posts == null) throw new JobExecutionException("Posts is null", false);
        postRepository.saveAll(Arrays.stream(posts)
                .peek(p -> p.setTimestamp(OffsetDateTime.now().minusSeconds(ThreadLocalRandom.current().nextLong(1_000_000))))
                .toList());
        log.info("Job finished");
    }
}
