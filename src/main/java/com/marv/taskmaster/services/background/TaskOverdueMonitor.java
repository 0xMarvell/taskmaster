package com.marv.taskmaster.services.background;

import com.marv.taskmaster.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskOverdueMonitor {

    private final TaskRepository taskRepository;


    @Scheduled(cron = "0 */1 * * * *") // every min
    @Transactional
    public void checkAndMarkOverdueTasks() {
        log.info("Running background task: Checking for overdue tasks...");

        LocalDateTime now = LocalDateTime.now();

        // Execute bulk update
        int updatedCount = taskRepository.markOverdueTasks(now);

        if (updatedCount > 0) {
            log.info("Marked {} tasks as OVERDUE at {}", updatedCount, now);
        } else {
            log.debug("No new overdue tasks found.");
        }
    }
}