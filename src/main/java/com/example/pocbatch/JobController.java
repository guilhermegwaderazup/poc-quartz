package com.example.pocbatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobController {

    private final Scheduler scheduler;

    @PostMapping("/jobs")
    public Job createJob(@RequestParam final String cron, @RequestHeader("x-tenant-id") final String tenantId) throws SchedulerException {
        final JobDetail jobDetail = JobBuilder.newJob(PostJob.class)
                .withIdentity(UUID.randomUUID().toString(), tenantId)
                .storeDurably()
                .build();
        scheduler.addJob(jobDetail, true);
        final Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), tenantId)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        scheduler.scheduleJob(trigger);
        return getJobById(jobDetail.getKey().getName(), tenantId);
    }

    @PutMapping("/jobs/{id}")
    public Job update(@PathVariable final String id, @RequestParam final String cron, @RequestHeader("x-tenant-id") final String tenantId) throws SchedulerException {
        final JobDetail jobDetail = scheduler.getJobDetail(new JobKey(id, tenantId));
        final var triggers = scheduler.getTriggersOfJob(jobDetail.getKey()).stream().map(Trigger::getKey).toList();
        scheduler.unscheduleJobs(triggers);
        final Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString())
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        scheduler.scheduleJob(trigger);
        return getJobById(id, tenantId);
    }

    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable final String id) throws SchedulerException {
        scheduler.deleteJob(new JobKey(id));
    }

    @GetMapping("/jobs")
    public List<Job> getJobs() throws SchedulerException {
        final List<Job> response = new ArrayList<>();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
            List<JobTrigger> triggers = getTriggers(jobKey);
            response.add(new Job(jobKey.getName(), jobKey.getGroup(), triggers));
        }
        return response;
    }

    private List<JobTrigger> getTriggers(JobKey jobKey) throws SchedulerException {
        return scheduler.getTriggersOfJob(jobKey).stream()
                .map(t -> new JobTrigger(
                        t.getKey().getName(),
                        t.getNextFireTime().toInstant().atOffset(ZoneOffset.of("-03:00")),
                        Optional.ofNullable(t.getPreviousFireTime())
                                .map(date -> date.toInstant().atOffset(ZoneOffset.of("-03:00")))
                                .orElse(null)
                ))
                .toList();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable final String id, @RequestHeader("x-tenant-id") final String tenantId) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(new JobKey(id, tenantId));
        var triggers = getTriggers(jobDetail.getKey());
        return new Job(id, jobDetail.getKey().getGroup(), triggers);
    }

    record Job(
            String id,
            String group,
            List<JobTrigger> triggers
    ) {}

    record JobTrigger(
            String id,
            OffsetDateTime nextFireTime,
            OffsetDateTime previousFireTime
    ) {}

}
