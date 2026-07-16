package com.taxin60sec.backend.jobs;
import java.time.Instant; import java.util.Map;
public final class JobContracts { private JobContracts(){} public record JobRequest(String type,String subjectId,Instant scheduledAt,Map<String,String> payload){} public interface JobScheduler { void schedule(JobRequest request); void retry(String jobId); } public interface ReminderJob { void execute(JobRequest request); } public interface ExpiredTokenCleanupJob { void execute(); } public interface SummaryJob { void execute(Instant forDate); } }
