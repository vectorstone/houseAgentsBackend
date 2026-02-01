package com.house.agents;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to verify that virtual threads are being used properly in JDK 21.
 * Virtual threads allow for high concurrency with minimal resource overhead.
 */
@SpringBootTest
@Slf4j
public class VirtualThreadTest {

    @Autowired
    private ExecutorService executorService;

    @Test
    public void testVirtualThreadsPerformance() {
        // Create a large number of concurrent tasks to demonstrate virtual thread efficiency
        int taskCount = 1000;
        AtomicInteger virtualThreadCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<?>[] futures = IntStream.range(0, taskCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    // Check if we're running on a virtual thread
                    Thread currentThread = Thread.currentThread();
                    if (currentThread.isVirtual()) {
                        virtualThreadCount.incrementAndGet();
                    }
                    
                    // Simulate some work
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);
        
        // Wait for all tasks to complete
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify all tasks ran on virtual threads
        assertEquals(taskCount, virtualThreadCount.get(), 
                "All tasks should have run on virtual threads");
        
        log.info("Completed {} virtual thread tasks in {}ms", taskCount, duration);
        log.info("Virtual threads provide excellent scalability and performance!");
        
        // With virtual threads, 1000 tasks should complete reasonably fast
        // Traditional thread pools would struggle with this many concurrent tasks
        assertTrue(duration < 5000, "Tasks should complete within 5 seconds with virtual threads");
    }
}
