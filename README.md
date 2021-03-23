Для запуска проекта необходимо сделать следующее:
1. Установить PostgreSQL
2. Запустить SQL скрипты из папки /server/SQLScripts
3. Запустить сервер.

На текущий момент реализована авторизация, хранение пользователей в базе, сделан каркас для выполнения команд загрузки/скачивания.

Идея разделения каналов на командный и канал данных - сделано через bind.
Идея разделения файлов на фрагменты - создан класс FileChunk, экземпляры которого планируется передавать сериализованными через канал данных. Класс FileChunk содержит всю информацию для склейки файла.

__*Добавлено 22.03.2021:*__ Все манипуляции по работе с файлами вынесены в класс FileAssembler. Сделана форма для авторизации и логика взаимодействия её с сервером.
В таблицу users базы данных добавлена строка userrights, в которой хранятся права доступа каждого пользователя.
Реализован DataHandler для принятия файлов - требует наладки - возможно, придётся сделать сериализацию как описано в мануале.




В процессе реализации -

1. Связь с базой данных для хранения пользователей - использована PostgreSQL. Скрипты для создания - в папке SQL. __Сделано, требует отладки, возможно требуется проработка идеи хранения IP адреса, с которого пользователь подключался в последний раз.__
2. Разработка идеи разделения Channels в Netty для передачи команд и данных. __Работа на ранней стадии.__
- хранение пула пользователей с привязкой к командному каналу (авторизация, добавление авторизованного пользователя в пул, удаление пользователя из пула при его отключении)
- создание канала данных только при необходимости (выполнение команд upload и download) - передача номера порта передачи данных пользователи через командный канал
- реализация конечного автомата для сервера - разрабока состояний и переходов между ними (IDLE, UPLOAD, DOWNLOAD)
3. Разработка клиента в соответствии с разработанным протоколом. Планирую использовать SWING __Работа на ранней стадии__


TODO - реализовать основной функционал

Основной функционал:
1. ААА - обязательно аутентификация и авторизация   - 
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

