# Car Rental System

A simulated car rental system built with Java 21 using object-oriented principles.

## Overview

This system allows users to reserve cars of different types for a specified duration. It handles inventory management, order creation, and ensures thread-safe concurrent bookings.

## Features

- **Car Reservation** - Reserve a car by type, date, and duration
- **Multiple Car Types** - Sedan, SUV, and Van options
- **Limited Inventory** - Realistic inventory management per car type
- **Order Management** - Create, cancel, and complete rental orders
- **Price Calculation** - Automatic pricing based on car type and duration
- **Thread Safety** - Concurrent booking protection (exactly one succeeds)

## Car Types & Pricing

| Type  | Price per Day |
|-------|---------------|
| Sedan | $49.99        |
| SUV   | $79.99        |
| Van   | $99.99        |

## Project Structure

```
CarRental/
├── pom.xml
├── README.md
├── requirements.md
└── src/
    ├── main/java/org/carrental/
    │   ├── CarRental.java      # Main rental service
    │   ├── Main.java           # Application entry point
    │   ├── car/
    │   │   ├── Car.java        # Car entity
    │   │   └── CarType.java    # Enum: SEDAN, SUV, VAN
    │   └── order/
    │       └── Order.java      # Order record
    └── test/java/
        └── CarRentalTest.java  # Unit tests
```

## Requirements

- Java 21 or higher
- Maven 3.6+

## Getting Started

### Build the Project

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

## Usage Example

```java
import org.carrental.CarRental;
import org.carrental.car.Car;
import org.carrental.car.CarType;
import org.carrental.order.Order;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Example {
    public static void main(String[] args) {
        // Initialize car inventory
        Map<CarType, Queue<Car>> inventory = new HashMap<>();
        
        Queue<Car> sedans = new LinkedList<>();
        sedans.add(new Car(CarType.SEDAN, 10000));
        sedans.add(new Car(CarType.SEDAN, 15000));
        inventory.put(CarType.SEDAN, sedans);
        
        Queue<Car> suvs = new LinkedList<>();
        suvs.add(new Car(CarType.SUV, 5000));
        inventory.put(CarType.SUV, suvs);
        
        // Create rental service
        CarRental carRental = new CarRental(inventory);
        
        // Make a reservation
        Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
        Order order = carRental.reserve(CarType.SEDAN, startDate, 5);
        
        System.out.println("Order ID: " + order.orderId());
        System.out.println("Car Type: " + order.car().getCarType());
        System.out.println("Total Price: $" + order.calculatePrice());
        
        // Return the car
        int newMileage = order.car().getMileage() + 250;
        carRental.returnCar(order.orderId(), newMileage);
        
        // Or cancel the reservation
        // carRental.cancel(order.orderId());
    }
}
```

## API Reference

### CarRental

| Method | Description |
|--------|-------------|
| `reserve(CarType, Instant, int)` | Reserve a car of specified type for given days |
| `cancel(String orderId)` | Cancel an existing reservation |
| `returnCar(String orderId, int newMileage)` | Return a rented car with updated mileage |

### Order

| Method | Description |
|--------|-------------|
| `orderId()` | Get the unique order identifier |
| `car()` | Get the reserved car |
| `startTime()` | Get reservation start time |
| `endTime()` | Get reservation end time |
| `calculatePrice()` | Calculate total rental price |

### Car

| Method | Description |
|--------|-------------|
| `getId()` | Get unique car identifier |
| `getCarType()` | Get the car type (SEDAN, SUV, VAN) |
| `getMileage()` | Get current mileage |
| `setMileage(int)` | Update mileage after return |

## Design Decisions

### Thread Safety
All booking operations are synchronized to ensure that when multiple users try to reserve the last available car, exactly one succeeds.

### FIFO Car Allocation
Cars are stored in queues, ensuring fair allocation based on first-in-first-out principle, allows to use PriorityQueue if we want to implement more complex allocation based on mileage

### Immutable Orders
Orders are implemented as Java records, making them immutable after creation.

### Validation
- Car type cannot be null
- Start time cannot be null
- Rental days must be positive
- Return mileage cannot be less than current mileage

## Out of Scope

The following features are intentionally not implemented:
- Payment processing
- Rescheduling (use cancellation and rebooking instead)
- UI/rendering

## Future Enhancements

Based on TODO comments in the codebase:
- [ ] Add car state (AVAILABLE, MAINTENANCE)
- [ ] Add order state (RESERVED, ACTIVE, COMPLETED, CANCELLED)
- [ ] Store order history
- [ ] Offer alternative car type when requested type unavailable
- [ ] Implement pricing policy, e.g. discounts for longer rentals

## AI tools used:
- Copilot(Cloude Opus 4.5) in agent mode for code review and unit test implementation

## License

This project is for educational/assessment purposes.

---

*Built with Java 21 and Maven*

