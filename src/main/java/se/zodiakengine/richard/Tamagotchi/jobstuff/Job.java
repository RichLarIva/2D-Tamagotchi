package se.zodiakengine.richard.Tamagotchi.jobstuff;

public enum Job {
    UNEMPLOYED("Unemployed", 0, JobType.UNEMPLOYED),
    PROFESSIONAL_HOT_DOG_MAKER("Professional Hot Dog Maker", 2, JobType.REAL),
    CHEF("Chef", 25, JobType.REAL),
    PIZZA_DELIVERY_DRIVER("Pizza Delivery Driver", 5, JobType.REAL),
    BARISTA("Barista", 3, JobType.REAL),

    JAVA_DEVELOPER("Java Developer", 50, JobType.REAL);

    private final String name;
    private final int salary;
    private final JobType jobType;

    Job(String name, int salary, JobType jobType) {
        this.name = name;
        this.salary = salary;
        this.jobType = jobType;
    }

    public JobType getJobType() {
        return jobType;
    }

    public String getName() {
        return name;
    }

    public int getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return name + " Salary: " + salary;
    }
}
