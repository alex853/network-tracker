--drop table fr24_flight;

CREATE SEQUENCE fr24_flight_id_seq;

CREATE TABLE fr24_flight
(
  id  int DEFAULT nextval('fr24_flight_id_seq'::regclass) NOT NULL,
  version smallint DEFAULT 0 NOT NULL,
  dof timestamp without time zone,
  flight_number character varying(10),
  origin_iata character varying(10),
  destination_iata character varying(10),
  type character varying(10),
  reg_number character varying(10),
  callsign character varying(10),
  CONSTRAINT pk_fr24_flight PRIMARY KEY (id)
);

CREATE INDEX fr24_flight_dof_flight_number_idx ON fr24_flight(dof, flight_number);
