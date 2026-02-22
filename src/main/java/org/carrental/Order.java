package org.carrental;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(String orderId, Car car, Instant startTime, Instant endTime) {
    public BigDecimal calculatePrice() {
        final BigDecimal pricePerDay = switch (car.getCarType()) {
            case SEDAN -> new BigDecimal(49.99);
            case SUV -> new BigDecimal(79.99);
            case VAN -> new BigDecimal(99.99);
        };

        long days = (endTime.toEpochMilli() - startTime.toEpochMilli()) / (1000 * 60 * 60 * 24);

        return pricePerDay.multiply(BigDecimal.valueOf(days));
    }
}
