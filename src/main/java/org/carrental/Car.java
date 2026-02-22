package org.carrental;

public class Car {
    private final CarType carType;
    private Status status;

    public boolean isAvailable() {
    }

    enum CarType {
        SEDAN, SUV, VAN
    }

    enum Status {
        AVAILABLE, RESERVED, RENTED
    }

    public CarType getCarType() {
        return carType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Car(CarType carType, Status status) {
        this.carType = carType;
        this.status = status;
    }
}
