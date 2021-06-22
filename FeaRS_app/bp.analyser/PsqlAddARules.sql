-- LES = Left end side of association rule containing clusters IDs
-- RES = Right end side of association rule containing cluster ID
CREATE TABLE arules(
    id BIGINT primary key not null,
    les int8[] not null,
    res bigint not null,
    support double precision not null,
    confidence double precision not null,
    lift double precision not null,
    ar_count bigint not null,
    sensitivity int not null
);