-- Recreate deterministic sample data on every application startup
-- This file is executed by Spring Boot after Flyway (see application.yml)
-- 0) Clean database and reset sequences
TRUNCATE TABLE emails,
refunds,
payments,
tickets,
bookings,
shows,
events,
seats,
venues,
users RESTART IDENTITY CASCADE;

-- 1) Users
INSERT INTO
    users (
        full_name,
        email,
        password,
        created_at,
        updated_at
    )
VALUES
    (
        'Alice Johnson',
        'alice@example.com',
        'password123',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'Bob Smith',
        'bob@example.com',
        'password456',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- 2) Venues
INSERT INTO
    venues (name, address, capacity, created_at, updated_at)
VALUES
    (
        'Grand Hall',
        '123 Main St, Metropolis',
        500,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'City Arena',
        '456 Center Ave, Gotham',
        800,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- 3) Seats (20 seats per venue, Section A, Row R1, Seat 1..20)
INSERT INTO
    seats (
        venue_id,
        section,
        row,
        seat_number,
        created_at,
        updated_at
    )
SELECT
    v.id,
    'A' AS section,
    'R1' AS row,
    gs :: text AS seat_number,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    venues v
    JOIN generate_series(1, 20) AS gs ON TRUE;

-- 4) Events
INSERT INTO
    events (
        title,
        description,
        category,
        status,
        created_at,
        updated_at
    )
VALUES
    (
        'Rock Night',
        'An electrifying evening of rock music',
        'MUSIC',
        'LIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'Tech Conference',
        'Talks and workshops on cutting-edge tech',
        'CONFERENCE',
        'LIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- 5) Shows (1 per event per venue)
INSERT INTO
    shows (
        venue_id,
        event_id,
        start_timestamp,
        duration_minutes,
        status,
        created_at,
        updated_at
    )
SELECT
    v.id,
    e.id,
    CURRENT_TIMESTAMP + INTERVAL '3 days',
    120,
    'LIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    venues v
    CROSS JOIN events e
WHERE
    v.name = 'Grand Hall'
    AND e.title = 'Rock Night';

INSERT INTO
    shows (
        venue_id,
        event_id,
        start_timestamp,
        duration_minutes,
        status,
        created_at,
        updated_at
    )
SELECT
    v.id,
    e.id,
    CURRENT_TIMESTAMP + INTERVAL '4 days',
    120,
    'LIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    venues v
    CROSS JOIN events e
WHERE
    v.name = 'City Arena'
    AND e.title = 'Rock Night';

INSERT INTO
    shows (
        venue_id,
        event_id,
        start_timestamp,
        duration_minutes,
        status,
        created_at,
        updated_at
    )
SELECT
    v.id,
    e.id,
    CURRENT_TIMESTAMP + INTERVAL '5 days',
    180,
    'LIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    venues v
    CROSS JOIN events e
WHERE
    v.name = 'Grand Hall'
    AND e.title = 'Tech Conference';

INSERT INTO
    shows (
        venue_id,
        event_id,
        start_timestamp,
        duration_minutes,
        status,
        created_at,
        updated_at
    )
SELECT
    v.id,
    e.id,
    CURRENT_TIMESTAMP + INTERVAL '6 days',
    180,
    'LIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    venues v
    CROSS JOIN events e
WHERE
    v.name = 'City Arena'
    AND e.title = 'Tech Conference';

-- 6) Bookings, Payments
WITH booking_specs AS (
    SELECT
        *
    FROM
        (
            VALUES
                (
                    'alice@example.com',
                    'Rock Night',
                    'Grand Hall',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '1 second',
                    ARRAY ['1'] :: text []
                ),
                (
                    'bob@example.com',
                    'Rock Night',
                    'Grand Hall',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '2 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'alice@example.com',
                    'Rock Night',
                    'Grand Hall',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '3 seconds',
                    ARRAY ['4','5','6'] :: text []
                ),
                (
                    'bob@example.com',
                    'Rock Night',
                    'City Arena',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '4 seconds',
                    ARRAY ['1'] :: text []
                ),
                (
                    'alice@example.com',
                    'Rock Night',
                    'City Arena',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '5 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'bob@example.com',
                    'Rock Night',
                    'City Arena',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '6 seconds',
                    ARRAY ['4','5','6'] :: text []
                ),
                (
                    'alice@example.com',
                    'Tech Conference',
                    'Grand Hall',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '7 seconds',
                    ARRAY ['1'] :: text []
                ),
                (
                    'bob@example.com',
                    'Tech Conference',
                    'Grand Hall',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '8 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'alice@example.com',
                    'Tech Conference',
                    'Grand Hall',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '9 seconds',
                    ARRAY ['4','5','6'] :: text []
                ),
                (
                    'bob@example.com',
                    'Tech Conference',
                    'City Arena',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '10 seconds',
                    ARRAY ['1'] :: text []
                ),
                (
                    'alice@example.com',
                    'Tech Conference',
                    'City Arena',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '11 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'bob@example.com',
                    'Tech Conference',
                    'City Arena',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '12 seconds',
                    ARRAY ['4','5','6'] :: text []
                )
        ) AS t(
            user_email,
            event_title,
            venue_name,
            total_amount,
            created_at_ts,
            seat_numbers
        )
),
resolved AS (
    SELECT
        u.id AS user_id,
        sh.id AS show_id,
        v.id AS venue_id,
        bs.total_amount,
        bs.created_at_ts,
        bs.seat_numbers
    FROM
        booking_specs bs
        JOIN users u ON u.email = bs.user_email
        JOIN events e ON e.title = bs.event_title
        JOIN venues v ON v.name = bs.venue_name
        JOIN shows sh ON sh.event_id = e.id
        AND sh.venue_id = v.id
),
ins_bookings AS (
    INSERT INTO
        bookings (
            user_id,
            show_id,
            status,
            total_amount,
            created_at,
            updated_at
        )
    SELECT
        r.user_id,
        r.show_id,
        'CONFIRMED',
        r.total_amount,
        r.created_at_ts,
        r.created_at_ts
    FROM
        resolved r RETURNING id,
        user_id,
        show_id,
        total_amount,
        created_at
)
INSERT INTO
    payments (
        booking_id,
        amount,
        status,
        created_at,
        updated_at
    )
SELECT
    id,
    total_amount,
    'SUCCESS',
    created_at,
    created_at
FROM
    ins_bookings;

-- 7) Tickets
WITH booking_resolved AS (
    SELECT
        u.id AS user_id,
        sh.id AS show_id,
        v.id AS venue_id,
        bs.total_amount,
        bs.created_at_ts,
        bs.seat_numbers
    FROM
        (
            VALUES
                (
                    'alice@example.com',
                    'Rock Night',
                    'Grand Hall',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '1 second',
                    ARRAY ['1'] :: text []
                ),
                (
                    'bob@example.com',
                    'Rock Night',
                    'Grand Hall',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '2 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'alice@example.com',
                    'Rock Night',
                    'Grand Hall',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '3 seconds',
                    ARRAY ['4','5','6'] :: text []
                ),
                (
                    'bob@example.com',
                    'Rock Night',
                    'City Arena',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '4 seconds',
                    ARRAY ['1'] :: text []
                ),
                (
                    'alice@example.com',
                    'Rock Night',
                    'City Arena',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '5 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'bob@example.com',
                    'Rock Night',
                    'City Arena',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '6 seconds',
                    ARRAY ['4','5','6'] :: text []
                ),
                (
                    'alice@example.com',
                    'Tech Conference',
                    'Grand Hall',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '7 seconds',
                    ARRAY ['1'] :: text []
                ),
                (
                    'bob@example.com',
                    'Tech Conference',
                    'Grand Hall',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '8 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'alice@example.com',
                    'Tech Conference',
                    'Grand Hall',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '9 seconds',
                    ARRAY ['4','5','6'] :: text []
                ),
                (
                    'bob@example.com',
                    'Tech Conference',
                    'City Arena',
                    100.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '10 seconds',
                    ARRAY ['1'] :: text []
                ),
                (
                    'alice@example.com',
                    'Tech Conference',
                    'City Arena',
                    200.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '11 seconds',
                    ARRAY ['2','3'] :: text []
                ),
                (
                    'bob@example.com',
                    'Tech Conference',
                    'City Arena',
                    300.00 :: DECIMAL(10, 2),
                    CURRENT_TIMESTAMP + INTERVAL '12 seconds',
                    ARRAY ['4','5','6'] :: text []
                )
        ) AS bs(
            user_email,
            event_title,
            venue_name,
            total_amount,
            created_at_ts,
            seat_numbers
        )
        JOIN users u ON u.email = bs.user_email
        JOIN events e ON e.title = bs.event_title
        JOIN venues v ON v.name = bs.venue_name
        JOIN shows sh ON sh.event_id = e.id
        AND sh.venue_id = v.id
),
booking_join AS (
    SELECT
        b.id AS booking_id,
        br.venue_id,
        br.seat_numbers
    FROM
        booking_resolved br
        JOIN bookings b ON b.user_id = br.user_id
        AND b.show_id = br.show_id
        AND b.total_amount = br.total_amount
        AND b.created_at = br.created_at_ts
),
expanded AS (
    SELECT
        bj.booking_id,
        s.id AS seat_id
    FROM
        booking_join bj
        JOIN LATERAL unnest(bj.seat_numbers) AS sn ON TRUE
        JOIN seats s ON s.venue_id = bj.venue_id
        AND s.section = 'A'
        AND s.row = 'R1'
        AND s.seat_number = sn
)
INSERT INTO
    tickets (
        booking_id,
        seat_id,
        price,
        created_at,
        updated_at
    )
SELECT
    booking_id,
    seat_id,
    100.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    expanded;
