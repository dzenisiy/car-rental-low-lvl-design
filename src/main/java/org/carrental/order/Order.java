package org.carrental.order;

import org.carrental.car.Car;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Order {

    private final String orderId;
    private final Car car;
    private final Instant startTime;
    private final Instant endTime;
    private OrderStatus status;

    public Order(String orderId, Car car, Instant startTime, Instant endTime, OrderStatus status) {
        this.orderId = orderId;
        this.car = car;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String orderId() { return orderId; }
    public Car car() { return car; }
    public Instant startTime() { return startTime; }
    public Instant endTime() { return endTime; }
    public OrderStatus status() { return status; }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal calculatePrice() {
        final BigDecimal pricePerDay = switch (car.getCarType()) {
            case SEDAN -> new BigDecimal("49.99");
            case SUV -> new BigDecimal("79.99");
            case VAN -> new BigDecimal("99.99");
        };

        long days = ChronoUnit.DAYS.between(startTime, endTime);

        return pricePerDay.multiply(BigDecimal.valueOf(days));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(car, order.car) && Objects.equals(orderId, order.orderId) && Objects.equals(endTime, order.endTime) && Objects.equals(startTime, order.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, car, startTime, endTime);
    }
}
