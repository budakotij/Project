CREATE TABLE users (
    user_name               VARCHAR(40) NOT NULL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    gender                  VARCHAR(20) NOT NULL,
    date_of_birth           DATE,
    email                   VARCHAR(255) UNIQUE,
    email_verified          BOOL NOT NULL DEFAULT false,
    phone_number            VARCHAR(15) UNIQUE,
    phone_number_verified   BOOL NOT NULL DEFAULT false,
    password_hash           VARCHAR(255) NOT NULL,
    profile_image           VARCHAR(500),
    user_type               VARCHAR(50) NOT NULL,
    registration_date       TIMESTAMP NOT NULL,
    is_active               BOOL NOT NULL DEFAULT true,
    roles                   TEXT[]
);

CREATE TABLE TRIPS (
    id                      VARCHAR(100) PRIMARY KEY,
    organiser               VARCHAR(40) REFERENCES users(user_name) NOT NULL,
    trip_tags               TEXT[],
    trip_status             VARCHAR(40) NOT NULL,
    posted_on               TIMESTAMP NOT NULL,
    pickup_on               TIMESTAMP NOT NULL,
    drop_on                 TIMESTAMP NOT NULL,
    pickup_at               VARCHAR(200) NOT NULL,
    pickup_location         public.GEOGRAPHY(Point, 4326) NOT NULL,
    drop_at                 VARCHAR(200) NOT NULL,
    drop_location           public.GEOGRAPHY(Point, 4326) NOT NULL,
    description             VARCHAR(4000) NOT NULL,
    destination             VARCHAR(200) NOT NULL,
    destination_location    public.GEOGRAPHY(Point, 4326),
    itinerary               JSONB,
    images                  TEXT[],
    maximum_occupancy       SMALLINT,
    available_occupancy     SMALLINT
);

CREATE TABLE TRIP_STAFF (
    id                  VARCHAR(100) PRIMARY KEY,
    trip_id             VARCHAR(100) REFERENCES trips(id) NOT NULL,
    staff_user_name     VARCHAR(40) REFERENCES users(user_name) NOT NULL,
    staff_type          VARCHAR(50) NOT NULL
);

CREATE TABLE TRIP_TRAVELLERS (
    id                      VARCHAR(100) PRIMARY KEY,
    trip_id                 VARCHAR(100) REFERENCES trips(id) NOT NULL,
    traveller_user_name     VARCHAR(40) REFERENCES users(user_name) NOT NULL,
    registered_on           TIMESTAMP NOT NULL
);

CREATE TABLE ANNOUNCEMENTS (
    id              VARCHAR(100) PRIMARY KEY,
    trip_id         VARCHAR(100) REFERENCES trips(id) NOT NULL,
    posted_by       VARCHAR(40) REFERENCES users(user_name) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(2000),
    images          TEXT[]
);