-- auto-generated definition
create table song
(
    id          int auto_increment
        primary key,
    song_name   varchar(255) null,
    upload_date datetime     null,
    path        varchar(255) not null,
    constraint song_name
        unique (song_name)
);

