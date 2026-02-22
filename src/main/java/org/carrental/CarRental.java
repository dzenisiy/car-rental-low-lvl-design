package org.carrental;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CarRental {

    private Map<Car.CarType, Queue<Car>> cars;
    private List<Order> activeOrders;

    public CarRental(Map<Car.CarType, Queue<Car>> cars) {
        this.cars = cars;
    }

    public Order reserve(Car.CarType carType, Instant startTime, int days) {

    }

    public void cancel(String orderId) {

    }

    public Car takeCar(String orderId) {

    }

    public void returnCar(String orderId) {

    }
}
