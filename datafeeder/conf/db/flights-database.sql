
CREATE SEQUENCE flight_id_seq;

CREATE TABLE flight (
    id integer DEFAULT nextval('flight_id_seq'::regclass) NOT NULL CONSTRAINT pk_flight PRIMARY KEY,
    version smallint NOT NULL,

    create_dt timestamp without time zone NOT NULL,
    modify_dt timestamp without time zone NOT NULL,

    network smallint NOT NULL,
    pilot_number integer NOT NULL,

    callsign character varying(10) NOT NULL,
    aircraft_type character varying(10),
    reg_no character varying(10),
    planned_origin character varying(10),
    planned_destination character varying(10),

    status smallint NOT NULL,

    first_seen_report_id integer NOT NUL,
    first_seen_dt timestamp without time zone NOT NULL,

    last_seen_report_id integer NOT NUL,
    last_seen_dt timestamp without time zone NOT NULL,

    departure_report_id integer,
    departure_dt timestamp without time zone,
    departure_latitude real,
    departure_longitude real,
    origin_type smallint,
    origin_icao character varying(10),

    arrival_report_id integer NOT NUL,
    arrival_dt timestamp without time zone,
    arrival_latitude real,
    arrival_longitude real,
    origin_type smallint,
    origin_icao character varying(10),

    distance_flown real,
    flight_time real
);

CREATE INDEX flight_modify_dt on flight (modify_dt);
CREATE INDEX flight_pilot_number_first_seen on flight (pilot_number, first_seen_report_id);

ALTER TABLE ONLY flight
    ADD CONSTRAINT uk_flight_pilot_number_first_seen_network UNIQUE (pilot_number, first_seen_report_id, network);
