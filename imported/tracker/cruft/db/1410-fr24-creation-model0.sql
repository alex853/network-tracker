drop table fr24

CREATE SEQUENCE fr24_id_seq;

CREATE TABLE fr24
(
  id  int DEFAULT nextval('fr24_id_seq'::regclass) NOT NULL,
  version smallint DEFAULT 0 NOT NULL,
  dt timestamp without time zone,
  fr_id character varying(10),
  lat_s character varying(10),
  lon_s character varying(10),
  heading_s character varying(10),
  alt_s character varying(10),
  gs_s character varying(10),
  squawk_s character varying(10),
  radar character varying(10),
  type character varying(10),
  reg_number character varying(10),
  unknown_number character varying(20),
  origin_iata character varying(10),
  destination_iata character varying(10),
  flight_number character varying(10),
  unknown_1 character varying(10),
  unknown_2 character varying(10),
  callsign character varying(10),
  unknown_3 character varying(10),
  CONSTRAINT pk_fr24 PRIMARY KEY (id)
);
