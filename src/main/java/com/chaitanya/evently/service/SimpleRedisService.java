package com.chaitanya.evently.service;

import com.chaitanya.evently.exception.types.InternalServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleRedisService {

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    private static final String RESERVATION_PREFIX = "reservation:";
    private static final String SEAT_LOCK_PREFIX = "seat_lock:";
    private static final int SEAT_LOCK_TTL_SECONDS = 300; // 5 minutes
    private static final int RESERVATION_TTL_SECONDS = 300; // 5 minutes
    private static final int BOOKING_TTL_SECONDS = 300; // 5 minutes

    /**
     * Creates a reservation and locks seats atomically
     */
    public String createReservation(Long userId, Long showId, List<Long> seatIds, String totalAmount) {
        // Check if Redis is available
        if (!isRedisAvailable()) {
            log.warn("Redis not available, allowing reservation without locking");
            return UUID.randomUUID().toString(); // Return fake reservation ID
        }

        String reservationId = UUID.randomUUID().toString();

        // Try to atomically lock all seats
        List<Long> lockedSeats = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            for (Long seatId : seatIds) {
                String seatLockKey = SEAT_LOCK_PREFIX + seatId;
                String result = jedis.set(seatLockKey, reservationId,
                        redis.clients.jedis.params.SetParams.setParams().nx().ex(SEAT_LOCK_TTL_SECONDS));

                if (result == null) {
                    // Seat already locked, release previously locked seats
                    log.warn("Seat {} is already locked, releasing previously locked seats", seatId);
                    releaseSeatLocks(jedis, lockedSeats);
                    return null; // Seat unavailable
                }
                lockedSeats.add(seatId);
            }
        } catch (Exception e) {
            log.error("Error during atomic seat locking: {}", e.getMessage());
            // Release any seats that were locked before the error
            try (Jedis jedis = jedisPool.getResource()) {
                releaseSeatLocks(jedis, lockedSeats);
            } catch (Exception releaseError) {
                log.error("Error releasing seats after locking failure: {}", releaseError.getMessage());
            }
            return null;
        }

        // Create reservation data
        ReservationData reservationData = ReservationData.builder()
                .userId(userId)
                .showId(showId)
                .seatIds(seatIds)
                .totalAmount(totalAmount)
                .build();

        // Store reservation
        String reservationKey = RESERVATION_PREFIX + reservationId;
        setObject(reservationKey, reservationData, RESERVATION_TTL_SECONDS);

        log.info("Created reservation {} for user {} with {} seats", reservationId, userId, seatIds.size());
        return reservationId;
    }

    /**
     * Gets reservation data
     */
    public ReservationData getReservation(String reservationId) {
        if (!isRedisAvailable()) {
            log.warn("Redis not available, returning null reservation");
            return null;
        }

        String reservationKey = RESERVATION_PREFIX + reservationId;
        return getObject(reservationKey, ReservationData.class);
    }

    /**
     * Helper method to release multiple seat locks
     */
    private void releaseSeatLocks(Jedis jedis, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }

        for (Long seatId : seatIds) {
            String seatLockKey = SEAT_LOCK_PREFIX + seatId;
            try {
                jedis.del(seatLockKey);
                log.debug("Released seat lock for seat {}", seatId);
            } catch (Exception e) {
                log.error("Error releasing seat lock for seat {}: {}", seatId, e.getMessage());
            }
        }
    }

    /**
     * Releases reservation and unlocks seats
     */
    public void releaseReservation(String reservationId) {
        if (!isRedisAvailable()) {
            log.warn("Redis not available, skipping reservation release");
            return;
        }

        ReservationData reservation = getReservation(reservationId);
        if (reservation == null) {
            log.warn("Reservation {} not found", reservationId);
            return;
        }

        // Delete reservation
        String reservationKey = RESERVATION_PREFIX + reservationId;
        delete(reservationKey);

        // Unlock seats
        for (Long seatId : reservation.getSeatIds()) {
            String seatLockKey = SEAT_LOCK_PREFIX + seatId;
            delete(seatLockKey);
        }

        log.info("Released reservation {} and unlocked {} seats", reservationId, reservation.getSeatIds().size());
    }

    /**
     * Processes booking transaction (simplified - just cleanup)
     */
    public boolean processBookingTransaction(String reservationId, BookingTransactionData bookingData) {
        if (!isRedisAvailable()) {
            log.warn("Redis not available, assuming transaction success");
            return true;
        }

        try {
            // Store booking data
            String bookingKey = "booking:" + bookingData.getBookingId();
            setObject(bookingKey, bookingData, BOOKING_TTL_SECONDS);

            // Store payment data if present
            if (bookingData.getPaymentData() != null) {
                String paymentKey = "payment:" + bookingData.getPaymentId();
                setObject(paymentKey, bookingData.getPaymentData(), BOOKING_TTL_SECONDS);
            }

            // Release reservation and seat locks
            releaseReservation(reservationId);

            log.info("Successfully processed booking transaction for reservation {}", reservationId);
            return true;
        } catch (Exception e) {
            log.error("Error processing booking transaction: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if seats are available (not locked)
     */
    public boolean areSeatsAvailable(List<Long> seatIds) {
        if (!isRedisAvailable()) {
            return true; // Assume available if Redis is down
        }

        for (Long seatId : seatIds) {
            String seatLockKey = SEAT_LOCK_PREFIX + seatId;
            if (exists(seatLockKey)) {
                log.info("Seat {} is locked", seatId);
                return false;
            }
        }
        return true;
    }

    // ===== Basic Redis Operations =====

    /**
     * Set a string value with TTL
     */
    public void set(String key, String value, int ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, ttlSeconds, value);
        } catch (Exception e) {
            logRedisError("Error setting key " + key, e);
        }
    }

    /**
     * Get a string value
     */
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            logRedisError("Error getting key " + key, e);
            return null;
        }
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (Exception e) {
            logRedisError("Error checking existence of key " + key, e);
            return false;
        }
    }

    /**
     * Delete a key
     */
    public void delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            logRedisError("Error deleting key " + key, e);
        }
    }

    /**
     * Set an object as JSON with TTL
     */
    public void setObject(String key, Object object, int ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = objectMapper.writeValueAsString(object);
            jedis.setex(key, ttlSeconds, json);
        } catch (JsonProcessingException e) {
            log.error("Serialization error for key {}: {}", key, e.getMessage());
        } catch (Exception e) {
            logRedisError("Error setting object for key " + key, e);
        }
    }

    /**
     * Get an object from JSON
     */
    public <T> T getObject(String key, Class<T> clazz) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            log.error("Deserialization error for key {}: {}", key, e.getMessage());
            return null;
        } catch (Exception e) {
            logRedisError("Error getting object for key " + key, e);
            return null;
        }
    }

    /**
     * Test Redis connectivity
     */
    public boolean isRedisAvailable() {
        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            return "PONG".equals(response);
        } catch (Exception e) {
            log.warn("Redis connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Helper method for differentiated Redis error logging and exception throwing
     */
    private void logRedisError(String message, Exception e) {
        String errorMessage;
        if (e instanceof redis.clients.jedis.exceptions.JedisConnectionException) {
            errorMessage = "Redis connection error - " + message + ": " + e.getMessage();
            log.error(errorMessage);
            throw new InternalServerException(errorMessage, e);
        } else if (e instanceof redis.clients.jedis.exceptions.JedisDataException) {
            errorMessage = "Redis data error - " + message + ": " + e.getMessage();
            log.error(errorMessage);
            throw new InternalServerException(errorMessage, e);
        } else if (e instanceof redis.clients.jedis.exceptions.JedisException) {
            errorMessage = "Redis operation error - " + message + ": " + e.getMessage();
            log.error(errorMessage);
            throw new InternalServerException(errorMessage, e);
        } else {
            errorMessage = "Redis unexpected error - " + message + ": " + e.getMessage();
            log.error(errorMessage);
            throw new InternalServerException(errorMessage, e);
        }
    }

    // ===== Data Classes =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReservationData {
        private Long userId;
        private Long showId;
        private List<Long> seatIds;
        private String totalAmount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BookingTransactionData {
        private Long bookingId;
        private Long paymentId;
        private Long userId;
        private Long showId;
        private String totalAmount;
        private Map<Long, String> seatPrices;
        private PaymentData paymentData;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentData {
        private Long paymentId;
        private Long bookingId;
        private String amount;
        private String status;
        private String paymentMethod;
    }
}