package org.carrental;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(Car car, String orderId, Instant startTime, Instant endTime) {
    public BigDecimal calculatePrice() {

    }
}
