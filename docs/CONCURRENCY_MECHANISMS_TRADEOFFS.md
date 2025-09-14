# Concurrency Management in Ticket Booking Systems

## Overview

Concurrency management in ticket booking systems is critical to prevent race conditions, double bookings, and ensure data consistency when multiple users attempt to book the same seats simultaneously. This document explores various approaches to handle concurrency in ticket booking systems, their tradeoffs, and the specific approach implemented in our Evently project.

## 1. Database-Level Concurrency Mechanisms

### 1.1 Pessimistic Locking

**Description**: Locks database rows during read operations to prevent other transactions from modifying them until the lock is released.

**Example**:

```sql
-- Lock seats for update
SELECT * FROM seats WHERE id IN (1, 2, 3) FOR UPDATE;

-- Check availability and book
INSERT INTO tickets (booking_id, show_id, seat_id, price)
VALUES (123, 456, 1, 100.00);
```

**Pros**:

- Guarantees no conflicts during booking
- Simple to implement
- Strong consistency

**Cons**:

- Can cause deadlocks
- Poor performance under high concurrency
- Locks held for entire transaction duration
- Can lead to timeouts

### 1.2 Optimistic Locking

**Description**: Uses version fields to detect conflicts when updating records.

**Example**:

```sql
-- Check version before update
UPDATE seats SET version = version + 1, status = 'BOOKED'
WHERE id = 1 AND version = 5;

-- If affected rows = 0, conflict occurred
```

**Pros**:

- Better performance under low conflict scenarios
- No deadlocks
- Allows concurrent reads

**Cons**:

- Requires retry logic
- Can fail under high contention
- More complex implementation

### 1.3 Database Constraints

**Description**: Uses unique constraints to prevent duplicate bookings.

**Example**:

```sql
-- Unique constraint prevents double booking
ALTER TABLE tickets ADD CONSTRAINT uk_ticket_show_seat
UNIQUE (show_id, seat_id);
```

**Pros**:

- Database-enforced consistency
- Simple and reliable
- No application-level logic needed

**Cons**:

- Violations cause transaction rollbacks
- Requires retry mechanisms
- Limited flexibility

## 2. Application-Level Concurrency Mechanisms

### 2.1 Distributed Locking (Redis)

**Description**: Uses Redis as a distributed lock manager to coordinate access across multiple application instances.

**Example**:

```java
// Acquire lock
String lockKey = "seat_lock:" + seatId;
boolean acquired = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, "locked", Duration.ofSeconds(30));

if (acquired) {
    try {
        // Perform booking logic
        createBooking(seatId, userId);
    } finally {
        // Release lock
        redisTemplate.delete(lockKey);
    }
}
```

**Pros**:

- Works across multiple application instances
- Configurable timeout
- Good performance
- Prevents overbooking

**Cons**:

- Requires Redis infrastructure
- Single point of failure
- Network latency
- Lock expiration complexity

### 2.2 Two-Phase Booking (Reservation + Confirmation)

**Description**: Splits booking into two phases: reservation (temporary hold) and confirmation (permanent booking).

**Example**:

```java
// Phase 1: Create reservation
String reservationId = createReservation(userId, showId, seatIds, totalAmount);

// Phase 2: Confirm booking (within timeout)
if (confirmBooking(reservationId, paymentDetails)) {
    // Booking confirmed
} else {
    // Reservation expired, seats released
}
```

**Pros**:

- Prevents overbooking
- User-friendly experience
- Allows payment processing time
- Automatic cleanup of expired reservations

**Cons**:

- More complex implementation
- Requires timeout management
- Temporary resource consumption
- Potential for abandoned reservations

### 2.3 Queue-Based Processing

**Description**: Uses message queues to serialize booking requests and process them sequentially.

**Example**:

```java
// Send booking request to queue
bookingQueue.send(new BookingRequest(userId, showId, seatIds));

// Consumer processes requests one by one
@RabbitListener(queues = "booking.queue")
public void processBooking(BookingRequest request) {
    // Process booking atomically
    createBooking(request);
}
```

**Pros**:

- Guaranteed ordering
- Natural backpressure handling
- Fault tolerance
- Scalable

**Cons**:

- Asynchronous processing
- Increased latency
- Queue infrastructure required
- Complex error handling

## 3. Hybrid Approaches

### 3.1 Database + Cache Combination

**Description**: Combines database constraints with Redis caching for optimal performance.

**Example**:

```java
// Check Redis cache first
if (redisService.areSeatsAvailable(seatIds)) {
    // Check database constraints
    if (validateSeatsNotBooked(seatIds, showId)) {
        // Create booking
        createBooking(seatIds, userId);
    }
}
```

**Pros**:

- Fast cache lookups
- Database consistency
- Reduced database load
- Good performance

**Cons**:

- Cache invalidation complexity
- Potential cache-database inconsistency
- More infrastructure

## 4. Our Evently Project Approach

### 4.1 Implementation Details

Our Evently project implements a **hybrid approach** combining:

1. **Redis-based Distributed Locking** for seat reservations
2. **Database Constraints** for final consistency
3. **Two-Phase Booking** with reservation timeout
4. **Spring Transaction Management** for atomicity

### 4.2 Key Components

#### 4.2.1 SimpleRedisService

```java
@Service
public class SimpleRedisService {
    // Creates reservation with seat locking
    public String createReservation(Long userId, Long showId,
                                   List<Long> seatIds, String totalAmount) {
        // Check if seats are already locked
        for (Long seatId : seatIds) {
            if (exists("seat_lock:" + seatId)) {
                return null; // Seat unavailable
            }
        }

        // Lock all seats
        for (Long seatId : seatIds) {
            set("seat_lock:" + seatId, reservationId, TTL_SECONDS);
        }

        // Store reservation data
        setObject("reservation:" + reservationId, reservationData, TTL_SECONDS);
        return reservationId;
    }
}
```

#### 4.2.2 BookingWorkflowService

```java
@Service
public class BookingWorkflowService {

    // Phase 1: Create reservation
    public BookingCreateResponse createBooking(BookingCreateRequest request, Long userId) {
        // Validate seats not booked in database
        validateSeatsNotBooked(seatIds, request.getShowId());

        // Check Redis for seat locks
        if (!redisService.areSeatsAvailable(seatIds)) {
            throw new BadRequestException("Seats currently being reserved");
        }

        // Create reservation with seat locking
        String reservationId = redisService.createReservation(
            userId, request.getShowId(), seatIds, totalAmount.toString());

        return BookingCreateResponse.builder()
            .reservationId(reservationId)
            .expiresAt(Instant.now().plusSeconds(300)) // 5 minutes
            .build();
    }

    // Phase 2: Process payment and confirm booking
    @Transactional
    public BookingPaymentResponse processPayment(BookingPaymentRequest request, Long userId) {
        // Validate reservation
        ReservationData reservation = validateReservation(request.getReservationId(), userId);

        // Create database entities atomically
        DatabaseEntities entities = createDatabaseEntities(reservation, userId, request.getAmount());

        // Process Redis transaction (cleanup reservation)
        redisService.processBookingTransaction(request.getReservationId(), bookingData);

        return BookingPaymentResponse.builder()
            .bookingId(entities.booking.getId())
            .success(true)
            .build();
    }
}
```

### 4.3 Database Schema Constraints

Our database uses several constraints to ensure consistency:

```sql
-- Prevent duplicate seat bookings
CREATE UNIQUE INDEX uk_ticket_show_seat ON tickets(show_id, seat_id);

-- Ensure valid booking statuses
ALTER TABLE bookings ADD CONSTRAINT chk_bookings_status
CHECK (status IN ('CONFIRMED', 'CANCELLED', 'WAITLISTED'));

-- Prevent negative amounts
ALTER TABLE bookings ADD CONSTRAINT chk_bookings_total_amount
CHECK (total_amount >= 0);
```

### 4.4 Transaction Management

We use Spring's `@Transactional` annotation for atomic operations:

```java
@Transactional
public BookingPaymentResponse processPayment(BookingPaymentRequest request, Long userId) {
    // All database operations are atomic
    // If any operation fails, entire transaction rolls back
}
```

## 5. Tradeoffs of Our Approach

### 5.1 Advantages

1. **Prevents Overbooking**: Redis locks ensure seats can't be double-booked
2. **User Experience**: Two-phase booking allows time for payment processing
3. **Scalability**: Redis handles high concurrency well
4. **Consistency**: Database constraints provide final consistency guarantee
5. **Fault Tolerance**: Graceful degradation when Redis is unavailable
6. **Automatic Cleanup**: Expired reservations are automatically released

### 5.2 Disadvantages

1. **Infrastructure Complexity**: Requires Redis infrastructure
2. **Network Dependency**: Redis connectivity issues can affect booking
3. **Cache Invalidation**: Potential inconsistency between Redis and database
4. **Resource Consumption**: Temporary locks consume Redis memory
5. **Timeout Management**: Complex logic for handling expired reservations
6. **Debugging Complexity**: Distributed state makes debugging harder

### 5.3 Performance Characteristics

- **Low Latency**: Redis operations are fast (< 1ms)
- **High Throughput**: Can handle thousands of concurrent requests
- **Memory Usage**: Moderate Redis memory consumption for locks
- **Database Load**: Reduced by Redis caching
- **Network Overhead**: Additional Redis network calls

## 6. Alternative Approaches Considered

### 6.1 Pure Database Approach

- **Rejected**: Poor performance under high concurrency
- **Reason**: Database locks would become bottleneck

### 6.2 Pure Redis Approach

- **Rejected**: Risk of data loss if Redis fails
- **Reason**: Need database as source of truth

### 6.3 Queue-Based Approach

- **Rejected**: Too complex for current requirements
- **Reason**: Asynchronous processing adds complexity

## 7. Monitoring and Observability

### 7.1 Key Metrics

- Redis lock acquisition success rate
- Reservation expiration rate
- Database constraint violation rate
- Average booking processing time
- Redis connectivity status

### 7.2 Alerting

- Redis connectivity failures
- High reservation expiration rates
- Database constraint violations
- Unusual booking processing times

## 8. Future Improvements

### 8.1 Potential Enhancements

1. **Redis Cluster**: For high availability
2. **Circuit Breaker**: For Redis failure handling
3. **Metrics Dashboard**: For monitoring concurrency patterns
4. **Load Testing**: Regular performance validation
5. **Graceful Degradation**: Fallback to database-only mode

### 8.2 Scalability Considerations

- Redis memory usage monitoring
- Database connection pool sizing
- Application instance scaling
- Geographic distribution planning

## Conclusion

Our hybrid approach provides a good balance between performance, consistency, and complexity. The combination of Redis distributed locking, database constraints, and two-phase booking ensures we can handle high concurrency while maintaining data integrity. The tradeoffs are acceptable for our current scale and requirements, with clear paths for future improvements as the system grows.
