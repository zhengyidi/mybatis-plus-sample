drop table if exists t_company ;
create table t_company
(
    id bigint,
    name varchar(255) not null,
    create_time datetime null,
    update_time datetime null,
    version int null,
    deleted int null
);

create unique index t_company_id_uindex
    on t_company (id);

alter table t_company
    add constraint t_company_pk
        primary key (id);

alter table t_company modify id bigint auto_increment;

drop table if exists t_user ;
create table t_user
(
    id bigint,
    name varchar(255) not null,
    company_id bigint null,
    create_time datetime null,
    update_time datetime null,
    version int null,
    deleted int null
);

create unique index t_user_id_uindex
    on t_user (id);

alter table t_user
    add constraint t_user_pk
        primary key (id);

alter table t_user modify id bigint auto_increment;