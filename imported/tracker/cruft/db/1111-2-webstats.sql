
CREATE TABLE web_stats (
    dt timestamp without time zone NOT NULL,
    page character varying(50) NOT NULL,
    addr character varying(20),
    name character varying(20)
);
