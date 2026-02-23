package org.carrental;

import org.carrental.car.Car;
import org.carrental.car.CarType;
import org.carrental.order.Order;
import org.carrental.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for CarRental system.
 * Tests all requirements including concurrency, pricing, and error handling.
 */
class CarRentalTest {

    private CarRental carRental;
    private Map<CarType, Queue<Car>> carInventory;

    @BeforeEach
    void setUp() {
        // Initialize car inventory with different types
        carInventory = new HashMap<>();

        Queue<Car> sedans = new PriorityQueue<>();
        sedans.add(new Car(CarType.SEDAN, 10000));
        sedans.add(new Car(CarType.SEDAN, 15000));
        sedans.add(new Car(CarType.SEDAN, 20000));

        Queue<Car> suvs = new PriorityQueue<>();
        suvs.add(new Car(CarType.SUV, 5000));
        suvs.add(new Car(CarType.SUV, 8000));

        Queue<Car> vans = new PriorityQueue<>();
        vans.add(new Car(CarType.VAN, 30000));

        carInventory.put(CarType.SEDAN, sedans);
        carInventory.put(CarType.SUV, suvs);
        carInventory.put(CarType.VAN, vans);

        carRental = new CarRental(carInventory);
    }

    // ==================== Happy Path Tests ====================

    @Test
    void testReserveCarSuccessfully() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        int days = 5;

        Order order = carRental.reserve(CarType.SEDAN, startTime, days);

        assertNotNull(order);
        assertNotNull(order.orderId());
        assertEquals(CarType.SEDAN, order.car().getCarType());
        assertEquals(startTime, order.startTime());
        assertEquals(startTime.plus(days, ChronoUnit.DAYS), order.endTime());
    }

    @Test
    void testReserveMultipleCarsOfSameType() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        Order order1 = carRental.reserve(CarType.SEDAN, startTime, 3);
        Order order2 = carRental.reserve(CarType.SEDAN, startTime, 3);
        Order order3 = carRental.reserve(CarType.SEDAN, startTime, 3);

        assertNotNull(order1);
        assertNotNull(order2);
        assertNotNull(order3);

        // Verify all orders are different
        assertNotEquals(order1, order2);
        assertNotEquals(order2, order3);
        assertNotEquals(order1.car(), order2.car());
    }

    @Test
    void testReserveDifferentCarTypes() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        Order sedanOrder = carRental.reserve(CarType.SEDAN, startTime, 5);
        Order suvOrder = carRental.reserve(CarType.SUV, startTime, 5);
        Order vanOrder = carRental.reserve(CarType.VAN, startTime, 5);

        assertEquals(CarType.SEDAN, sedanOrder.car().getCarType());
        assertEquals(CarType.SUV, suvOrder.car().getCarType());
        assertEquals(CarType.VAN, vanOrder.car().getCarType());
    }

    @Test
    void testCancelReservation() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        int availableBeforeCancel = carInventory.get(CarType.SEDAN).size();

        // Cancel the reservation
        carRental.cancel(order.orderId());

        // Car should be back in inventory
        assertEquals(availableBeforeCancel + 1, carInventory.get(CarType.SEDAN).size());
    }

    @Test
    void testReturnCar() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SUV, startTime, 5);

        int originalMileage = order.car().getMileage();
        int newMileage = originalMileage + 500;

        int availableBeforeReturn = carInventory.get(CarType.SUV).size();

        // Return the car with updated mileage
        carRental.returnCar(order.orderId(), newMileage);

        // Car should be back in inventory
        assertEquals(availableBeforeReturn + 1, carInventory.get(CarType.SUV).size());

        // Verify mileage was updated
        assertEquals(newMileage, order.car().getMileage());
    }

    // ==================== Price Calculation Tests ====================

    @Test
    void testPriceCalculationForSedan() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        BigDecimal expectedPrice = new BigDecimal("49.99").multiply(BigDecimal.valueOf(5));
        assertEquals(expectedPrice, order.calculatePrice());
    }

    @Test
    void testPriceCalculationForSUV() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SUV, startTime, 3);

        BigDecimal expectedPrice = new BigDecimal("79.99").multiply(BigDecimal.valueOf(3));
        assertEquals(expectedPrice, order.calculatePrice());
    }

    @Test
    void testPriceCalculationForVan() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.VAN, startTime, 7);

        BigDecimal expectedPrice = new BigDecimal("99.99").multiply(BigDecimal.valueOf(7));
        assertEquals(expectedPrice, order.calculatePrice());
    }

    @Test
    void testPriceCalculationForOneDay() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 1);

        BigDecimal expectedPrice = new BigDecimal("49.99");
        assertEquals(expectedPrice, order.calculatePrice());
    }

    // ==================== Error Handling Tests ====================

    @Test
    void testReserveWithNullCarType() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.reserve(null, startTime, 5);
        });

        assertTrue(exception.getMessage().contains("CarType cannot be null"));
    }

    @Test
    void testReserveWithNullStartTime() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.reserve(CarType.SEDAN, null, 5);
        });

        assertTrue(exception.getMessage().contains("Start time cannot be null"));
    }

    @Test
    void testReserveWithZeroDays() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.reserve(CarType.SEDAN, startTime, 0);
        });

        assertTrue(exception.getMessage().contains("Days must be positive"));
    }

    @Test
    void testReserveWithNegativeDays() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.reserve(CarType.SEDAN, startTime, -5);
        });

        assertTrue(exception.getMessage().contains("Days must be positive"));
    }

    @Test
    void testReserveWhenNoAvailableCars() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        // Reserve all 3 sedans
        carRental.reserve(CarType.SEDAN, startTime, 5);
        carRental.reserve(CarType.SEDAN, startTime, 5);
        carRental.reserve(CarType.SEDAN, startTime, 5);

        // Try to reserve a 4th sedan
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            carRental.reserve(CarType.SEDAN, startTime, 5);
        });

        assertTrue(exception.getMessage().contains("No cars of type SEDAN are available"));
    }

    @Test
    void testCancelNonExistentOrder() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.cancel("non-existent-order-id");
        });

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void testReturnNonExistentOrder() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.returnCar("non-existent-order-id", 50000);
        });

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void testReturnCarWithLowerMileage() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        int originalMileage = order.car().getMileage();
        int lowerMileage = originalMileage - 100;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            carRental.returnCar(order.orderId(), lowerMileage);
        });

        assertTrue(exception.getMessage().contains("cannot be less than current mileage"));
    }

    // ==================== Order Status Tests ====================

    @Test
    void testReserveOrderHasReservedStatus() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        assertEquals(OrderStatus.RESERVED, order.status());
    }

    @Test
    void testStartRentalTransitionsToInProgress() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        carRental.startRental(order.orderId());

        assertEquals(OrderStatus.IN_PROGRESS, order.status());
    }

    @Test
    void testCancelOrderHasCancelledStatus() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        carRental.cancel(order.orderId());

        assertEquals(OrderStatus.CANCELLED, order.status());
    }

    @Test
    void testReturnCarOrderHasCompletedStatus() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        carRental.returnCar(order.orderId(), order.car().getMileage());

        assertEquals(OrderStatus.COMPLETED, order.status());
    }

    @Test
    void testStartRentalOnNonExistentOrder() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            carRental.startRental("non-existent-order-id")
        );

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void testStartRentalOnAlreadyInProgressOrder() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);
        carRental.startRental(order.orderId());

        Exception exception = assertThrows(IllegalStateException.class, () ->
            carRental.startRental(order.orderId())
        );

        assertTrue(exception.getMessage().contains("expected status RESERVED but was IN_PROGRESS"));
    }

    @Test
    void testStartRentalOnCancelledOrder() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);
        carRental.cancel(order.orderId());

        Exception exception = assertThrows(java.lang.IllegalArgumentException.class, () ->
            carRental.startRental(order.orderId())
        );

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    // ==================== Concurrency Tests ====================

    @Test
    void testConcurrentReservationOnlyOneSucceeds() throws InterruptedException {
        // Create inventory with only 1 car
        Map<CarType, Queue<Car>> limitedInventory = new HashMap<>();
        Queue<Car> singleCar = new LinkedList<>();
        singleCar.add(new Car(CarType.SEDAN, 10000));
        limitedInventory.put(CarType.SEDAN, singleCar);

        CarRental limitedCarRental = new CarRental(limitedInventory);

        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        int numberOfThreads = 10;
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Create multiple threads trying to reserve the same car
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    latch.await(); // Wait for signal to start
                    Order order = limitedCarRental.reserve(CarType.SEDAN, startTime, 5);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            }).start();
        }

        // Signal all threads to start
        latch.countDown();

        // Wait for all threads to complete
        completionLatch.await();

        // Exactly 1 should succeed, rest should fail
        assertEquals(1, successCount.get(), "Exactly one reservation should succeed");
        assertEquals(numberOfThreads - 1, failureCount.get(), "All other reservations should fail");
    }

    // ==================== Edge Cases ====================

//   TODO: Good idea for extension, introduce a discount for a long term rental
//    @Test
//    void testReserveLongTermRental() {
//        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
//        int days = 365; // One year rental
//
//        Order order = carRental.reserve(CarType.VAN, startTime, days);
//
//        assertNotNull(order);
//        assertEquals(startTime.plus(days, ChronoUnit.DAYS), order.endTime());
//
//        // Verify price calculation for long term
//        BigDecimal expectedPrice = new BigDecimal("99.99").multiply(BigDecimal.valueOf(365));
//        assertEquals(expectedPrice, order.calculatePrice());
//    }

    @Test
    void testReturnCarWithSameMileage() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startTime, 5);

        int originalMileage = order.car().getMileage();

        // Should not throw exception when returning with same mileage
        assertDoesNotThrow(() -> {
            carRental.returnCar(order.orderId(), originalMileage);
        });
    }

    @Test
    void testMultipleCancellationsAndReservations() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        // Reserve, cancel, reserve again
        Order order1 = carRental.reserve(CarType.SUV, startTime, 5);
        carRental.cancel(order1.orderId());

        Order order2 = carRental.reserve(CarType.SUV, startTime, 5);

        // The same car might be reused
        assertEquals(order2.car(), order1.car());
    }

    @Test
    void testLeastMileageTaken() {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);

        Order order1 = carRental.reserve(CarType.SEDAN, startTime, 5);
        assertEquals(10000, order1.car().getMileage());

        Order order2 = carRental.reserve(CarType.VAN, startTime, 5);
        assertEquals(30000, order2.car().getMileage());

        Order order3 = carRental.reserve(CarType.SUV, startTime, 5);
        assertEquals(5000, order3.car().getMileage());
    }
}

