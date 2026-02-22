package org.carrental;

public class Car {
    private final CarType carType;
    private Status status;

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

    public void markAvailable() {
        this.status = Status.AVAILABLE;
    }

    public void markReserved() {
        this.status = Status.RESERVED;
    }

    public void markRented() {
        this.status = Status.RENTED;
    }

    public Car(CarType carType, Status status) {
        this.carType = carType;
        this.status = status;
    }
}
