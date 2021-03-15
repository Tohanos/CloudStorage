В процессе реализации -

1. Связь с базой данных для хранения пользователей - использована PostgreSQL. Скрипты для создания - в папке SQL. __Сделано, требует отладки, возможно требуется проработка идеи хранения IP адреса, с которого пользователь подключался в последний раз.__
2. Разработка идеи разделения Channels в Netty для передачи команд и данных. __Работа на ранней стадии.__
- хранение пула пользователей с привязкой к командному каналу (авторизация, добавление авторизованного пользователя в пул, удаление пользователя из пула при его отключении)
- создание канала данных только при необходимости (выполнение команд upload и download) - передача номера порта передачи данных пользователи через командный канал
- реализация конечного автомата для сервера - разрабока состояний и переходов между ними (IDLE, UPLOAD, DOWNLOAD)
3. Разработка клиента в соответствии с разработанным протоколом. Планирую использовать SWING __Работа на ранней стадии__
