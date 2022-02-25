package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.service.JobService;
import cz.cuni.matfyz.server.entity.Job;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class JobController
{
    @Autowired
    private JobService jobService;

    @GetMapping("/jobs")
    public List<Job> getAllJobs()
    {
        return jobService.findAll();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable String id)
    {
        return null; // TODO
    }
}