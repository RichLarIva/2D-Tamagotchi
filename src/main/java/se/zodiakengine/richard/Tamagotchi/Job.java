package se.zodiakengine.richard.Tamagotchi;

public enum Job {
    UNEMPLOYED("Unemployed", 0),
    PROFESSIONAL_HOT_DOG_MAKER("Professional Hot Dog Maker", 2),
    CHEF("Chef", 25),
    PIZZA_DELIVERY_DRIVER("Pizza Delivery Driver", 5),
    BARISTA("Barista", 3),

    JAVA_DEVELOPER("Java Developer", 50);

    private final String name;
    private final int salary;

    Job(String name, int salary) {
        this.name = name;
        this.salary = salary;
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
