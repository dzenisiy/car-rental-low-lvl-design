package org.carrental.order;

import org.carrental.car.Car;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record Order(String orderId, Car car, Instant startTime, Instant endTime) {

    // TODO: add order state (RESERVED, ACTIVE, COMPLETED, CANCELLED)

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
