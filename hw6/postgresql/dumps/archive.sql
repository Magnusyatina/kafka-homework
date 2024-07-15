create table logs (
    id bigserial primary key not null,
    client_id bigint not null default 0,
    message varchar default null,
    update_date_time timestamp with time zone default now()
);

create function logs_update_trigger_function() returns trigger as $$
    begin
        new.update_date_time = now();
        return new;
    end;
$$ language 'plpgsql';

create trigger logs_update_trigger before update on logs for each row execute function logs_update_trigger_function();

CREATE OR REPLACE FUNCTION random_between(low INT ,high INT)
    RETURNS INT AS
$$
BEGIN
    RETURN floor(random()* (high-low + 1) + low);
END;
$$ language 'plpgsql';

do $$
    declare
        record record;
    begin
        for record in select * from generate_series(1, 1000)
        loop
            insert into logs (client_id, message)
            values (random_between(1, 20),
                    case
                        when cast(mod(random_between(1, 1000), 10) as integer) = 1 then 'Ошибка при входе в систему'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 2 then 'Неавторизоанный доступ к ресурсу'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 3 then 'Непредвиденная ошибка'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 4 then 'Ошибка при отправке сообщения в сторонний сервис'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 5 then 'Редактирование записи о пользователе'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 6 then 'Добавление нового товара в корзину'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 7 then 'Совершена подписка на обновления'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 8 then 'Проверка логов журнала'
                        when cast(mod(random_between(1, 1000), 10) as integer) = 9 then 'Критическая ошибка'
                        else 'Неизвестное сообщение'
                        end);
        end loop;
    end
$$;