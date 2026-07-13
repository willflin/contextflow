-- Phase 6: learning unit sense deferrals
-- Phase 6：词义学习队列延后记录

create table user_learning_unit_sense_deferrals (
    id bigint not null auto_increment,
    user_id bigint not null,
    learning_unit_sense_id bigint not null,
    penalty_score decimal(8,4) not null,
    reason varchar(64) not null,
    deferred_until datetime(6) not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    primary key (id),
    constraint uk_user_sense_deferral unique (user_id, learning_unit_sense_id),
    constraint fk_user_sense_deferrals_user foreign key (user_id) references users (id),
    constraint fk_user_sense_deferrals_sense foreign key (learning_unit_sense_id) references learning_unit_senses (id)
);

create index idx_user_sense_deferrals_active
    on user_learning_unit_sense_deferrals (user_id, deferred_until);
