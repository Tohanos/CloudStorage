В процессе реализации -

1. Связь с базой данных для хранения пользователей - использована PostgreSQL. Скрипты для создания - в папке SQL. __Сделано, требует отладки, возможно требуется проработка идеи хранения IP адреса, с которого пользователь подключался в последний раз.__
2. Разработка идеи разделения Channels в Netty для передачи команд и данных. __Работа на ранней стадии.__
- хранение пула пользователей с привязкой к командному каналу (авторизация, добавление авторизованного пользователя в пул, удаление пользователя из пула при его отключении)
- создание канала данных только при необходимости (выполнение команд upload и download) - передача номера порта передачи данных пользователи через командный канал
- реализация конечного автомата для сервера - разрабока состояний и переходов между ними (IDLE, UPLOAD, DOWNLOAD)
3. Разработка клиента в соответствии с разработанным протоколом. Планирую использовать SWING __Работа на ранней стадии__


TODO - реализовать основной функционал

Основной функционал:
1. ААА - обязательно аутентификация и авторизация
2. Смена пароля, удаление аккаунта
3. Загрузка, скачивание файлов
4. 1 репозиторий - 1 юзер
5. Копирование, перемещение, удаление, сортировка файлов. Создание папок
6. Поиск файлов
7. Пометка на удаление / корзина
8. Ограничение на размер

Дополнительно по желанию:
*   1. Шифрование паролей
**  2. Древовидная структура (опция)
*** 3. Сбор статистики (на выбор)

