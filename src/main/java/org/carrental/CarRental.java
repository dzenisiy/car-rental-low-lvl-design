package org.carrental;

import org.carrental.car.Car;
import org.carrental.car.CarType;
import org.carrental.order.Order;
import org.carrental.order.OrderStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.time.temporal.ChronoUnit;

public class CarRental {

    private final Map<CarType, Queue<Car>> cars;
    private final Map<String, Order> activeOrders = new HashMap<>();
    //TODO: store order history

    public CarRental(Map<CarType, Queue<Car>> cars) {
        this.cars = cars;
    }

    public Order reserve(CarType carType, Instant startTime, int days) {
        if (carType == null) {
            throw new IllegalArgumentException("CarType cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive, got: " + days);
        }

        synchronized (this) {
            Queue<Car> availableCars = cars.get(carType);
            if (availableCars == null || availableCars.isEmpty()) {
                //TODO: offer a car of another type
                throw new IllegalStateException("No cars of type " + carType + " are available");
            }

            Car car = getCar(availableCars);
            return generateOrder(car, startTime, days);
        }
    }

    private Car getCar(Queue<Car> availableCars) {
        Car car = availableCars.poll();
        if (car == null) {
            throw new IllegalStateException("No cars are available");
        }

        return car;
    }

    private Order generateOrder(Car car, Instant startTime, int days) {
        Instant endTime = startTime.plus(days, ChronoUnit.DAYS);
        String orderId = generateOrderId();
        Order order = new Order(orderId, car, startTime, endTime, OrderStatus.RESERVED);
        activeOrders.put(order.orderId(), order);
        return order;
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    public void startRental(String orderId) {
        synchronized (this) {
            Order order = getOrder(orderId);
            if (order.status() != OrderStatus.RESERVED) {
                throw new IllegalStateException(
                        "Cannot start rental for order " + orderId + ": expected status RESERVED but was " + order.status()
                );
            }
            order.setStatus(OrderStatus.IN_PROGRESS);
        }
    }

    public void cancel(String orderId) {
        synchronized (this) {
            Order order = getOrder(orderId);
            order.setStatus(OrderStatus.CANCELLED);
            returnCarToStorage(order.car());
            activeOrders.remove(orderId);
        }
    }

    private Order getOrder(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " does not exist");
        }

        return order;
    }

    public void returnCar(String orderId, int newMileage) {
        synchronized (this) {
            Order order = getOrder(orderId);
            Car car = order.car();
            if (newMileage < car.getMileage()) {
                throw new IllegalArgumentException(
                    "New mileage (" + newMileage + ") cannot be less than current mileage (" + car.getMileage() + ")"
                );
            }
            car.setMileage(newMileage);

            order.setStatus(OrderStatus.COMPLETED);
            returnCarToStorage(car);
            activeOrders.remove(orderId);
        }
    }

    private void returnCarToStorage(Car car) {
        if (!cars.get(car.getCarType()).offer(car)) {
            throw new IllegalStateException("Failed to return car to storage: " + car.getId());
        }
    }
}
