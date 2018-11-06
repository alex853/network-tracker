
CREATE SEQUENCE flt_pilot_status_id_seq;

CREATE TABLE flt_pilot_status (
    id integer DEFAULT nextval('flt_pilot_status_id_seq'::regclass) NOT NULL CONSTRAINT pk_pilot_status PRIMARY KEY,
    version smallint NOT NULL,

    create_dt timestamp NOT NULL,
    modify_dt timestamp NOT NULL,

    pilot_number integer NOT NULL,

    last_processed_report_id integer NOT NULL,
    last_processed_dt timestamp NOT NULL,

    curr_flight_id integer
);

CREATE SEQUENCE flt_flight_id_seq;

CREATE TABLE flt_flight (
    id integer DEFAULT nextval('flt_flight_id_seq'::regclass) NOT NULL CONSTRAINT pk_flt_flight PRIMARY KEY,
    version smallint NOT NULL,

    create_dt timestamp NOT NULL,
    modify_dt timestamp NOT NULL,

    --network smallint NOT NULL,
    pilot_number integer NOT NULL,

    callsign character varying(10),
    aircraft_type character varying(10),
    reg_no character varying(10),
    planned_departure character varying(10),
    planned_destination character varying(10),

    status smallint NOT NULL,

    first_seen_report_id integer NOT NULL,
    first_seen_dt timestamp NOT NULL,

    last_seen_report_id integer NOT NULL,
    last_seen_dt timestamp NOT NULL,

    takeoff_report_id integer,
    takeoff_dt timestamp,
    takeoff_latitude real,
    takeoff_longitude real,
    takeoff_type smallint,
    takeoff_icao character varying(10),

    landing_report_id integer,
    landing_dt timestamp,
    landing_latitude real,
    landing_longitude real,
    landing_type smallint,
    landing_icao character varying(10),

    distance_flown real,
    flight_time real
);

--CREATE INDEX flt_flight_modify_dt on flight (modify_dt);
--CREATE INDEX flight_pilot_number_first_seen on flight (pilot_number, first_seen_report_id);

ALTER TABLE ONLY flt_pilot_status
    ADD CONSTRAINT uk_flt_pilot_status_pilot_number UNIQUE (pilot_number);

ALTER TABLE ONLY flt_flight
    ADD CONSTRAINT uk_flt_flight_pilot_number_first_seen_network UNIQUE (pilot_number, first_seen_report_id);

ALTER TABLE ONLY flt_pilot_status
    ADD CONSTRAINT fk_flt_pilot_status_curr_flight_id FOREIGN KEY (curr_flight_id) REFERENCES flt_flight(id);

