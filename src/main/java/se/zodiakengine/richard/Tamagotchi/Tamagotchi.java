package se.zodiakengine.richard.Tamagotchi;

import se.zodiakengine.richard.Tamagotchi.JobStuff.Job;
import se.zodiakengine.richard.Tamagotchi.JobStuff.JobType;

public class Tamagotchi {
    private final String name;

    private final int tiredLevel;
    private int fullLevel;
    private int funLevel;
    private Job job;

    public Tamagotchi(String name) {
        this.name = name;
        this.fullLevel = 10;
        this.funLevel = 10;
        this.tiredLevel = 0;
        this.job = Job.UNEMPLOYED;
    }

    public int getFullLevel() {
        return fullLevel;
    }

    public void setFullLevel(int fullLevel) {
        this.fullLevel = fullLevel;
    }

    public int getFunLevel() {
        return funLevel;
    }

    public void setFunLevel(int funLevel) {
        this.funLevel = funLevel;
    }

    public void increaseFun() {
        this.funLevel++;
        this.fullLevel--;
    }

    public void increaseFullness() {
        this.fullLevel++;
        this.funLevel--;
    }


    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + " FULLNESS LEVEL: " + this.fullLevel + " FUN LEVEL: " + this.funLevel;
    }

    public void changeJob(Job newJob) {
        this.job = newJob;
    }

    public Job getJob() {
        return job;
    }

    public JobType getJobType() {
        return job.getJobType();
    }
}
