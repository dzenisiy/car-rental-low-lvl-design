package org.carrental;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class CarRental {

    private Map<Car.CarType, Queue<Car>> cars;
    private Map<String, Order> activeOrders;

    public CarRental(Map<Car.CarType, Queue<Car>> cars) {
        this.cars = cars;
    }

    public Order reserve(Car.CarType carType, Instant startTime, int days) {
        synchronized (this) {
            Queue<Car> availableCars = cars.get(carType);
            if (availableCars == null || availableCars.isEmpty()) {
                throw new IllegalStateException("No cars of type " + carType + " are available");
            }

            Car car = getCar(availableCars);
            return generateOrder(car, startTime, days);
        }
    }

    private static Car getCar(Queue<Car> availableCars) {
        Car car = availableCars.poll();
        if (car == null) {
            throw new IllegalStateException("No cars are available");
        }

        return car;
    }

    private Order generateOrder(Car car, Instant startTime, int days) {
        Instant endTime = startTime.plusSeconds(days * 24 * 60 * 60);
        String orderId = generateOrderId();
        Order order = new Order(orderId, car, startTime, endTime);
        activeOrders.put(order.orderId(), order);
        car.markReserved();
        return order;
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    public void cancel(String orderId) {
        synchronized (this) {
            Order order = getOrder(orderId);

            activeOrders.remove(orderId);
            order.car().markAvailable();
        }
    }

    private Order getOrder(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " does not exist");
        }
        return order;
    }

    public Car takeCar(String orderId) {
        Order order = getOrder(orderId);

        order.car().markRented();
        return order.car();
    }

    public void returnCar(String orderId, int newMileage) {
        synchronized (this) {
            Order order = getOrder(orderId);
            Car car = order.car();
            car.markAvailable();

            cars.get(order.car().getCarType()).offer(order.car());
            activeOrders.remove(orderId);
        }
    }
}
