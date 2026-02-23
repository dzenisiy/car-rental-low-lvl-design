package org.carrental.car;

import java.util.Objects;
import java.util.UUID;

public class Car implements Comparable<Car> {
    private final String id;
    private final CarType carType;
    private int mileage;

    //TODO: add car state (AVAILABLE, MAINTENANCE)

    public Car(CarType carType, int mileage) {
        this.carType = carType;
        this.mileage = mileage;
        id =  UUID.randomUUID().toString();
    }

    public CarType getCarType() {
        return carType;
    }


    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(Car other) {
        return Integer.compare(this.mileage, other.mileage);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return mileage == car.mileage && Objects.equals(id, car.id) && carType == car.carType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, carType, mileage);
    }
}
