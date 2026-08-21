create table instrument
(
    symbol   varchar(20)  not null,
    name     varchar(200) not null,
    isin     varchar(12),
    wkn      varchar(6),
    currency varchar(3)   not null,
    exchange varchar(40),
    type     varchar(20)  not null,
    constraint pk_instrument primary key (symbol)
);

create unique index ux_instrument_isin on instrument (isin);
create unique index ux_instrument_wkn on instrument (wkn);
create index ix_instrument_name on instrument (name);
