# ChatServer: Руководство по запуску и использованию

## Описание
ChatServer — это многопользовательский серверный чат, который позволяет пользователям регистрироваться, авторизовываться и обмениваться сообщениями как между собой, так и со всеми подключенными пользователями. Сервер написан на Java и использует сокеты для обработки клиентских подключений.

## Требования
Java 8+ - Убедитесь, что у вас установлена версия Java 8 или выше. Если Java не установлена, скачайте и установите ее с официального сайта Oracle или через пакетный менеджер (например, apk, apt, yum).
Файл login.txt - Сервер использует файл login.txt для хранения зарегистрированных логинов и паролей. Этот файл будет создан автоматически, если не существует.

## Запуск сервера
1. Компиляция исходного кода
Если у вас есть исходный код программы, выполните следующие шаги для компиляции:

Перейдите в каталог с исходными файлами:

cd /path/to/source
javac ChatServer.java

Убедитесь, что все зависимости (например, jackson-databind) добавлены в classpath. Если вы используете Maven или Gradle, они автоматически добавят необходимые библиотеки.

2. Запуск скомпилированного класса
Запустите скомпилированный класс ChatServer:

java ChatServer

3. Запуск с JAR-файлом
Если вы собрали проект в JAR-файл (или получили его заранее):

Перейдите в каталог с вашим JAR-файлом:

cd /path/to/jar

Запустите JAR-файл:

java -jar your_jar_file.jar

4. Выбор порта для сервера
После запуска программы вас попросят ввести порт для сервера:

Введите порт для запуска чата, либо exit для выхода из приложения
Введите номер порта (например, 8080). Сервер начнет прослушивать этот порт для клиентских подключений.

## Использование программы

Регистрация пользователя

После подключения к серверу новый клиент должен отправить команду регистрации логина:
json
{"type":"RegLogin","message":"username"}

Если логин уникален и соответствует правилам (не короче 3 символов, не длиннее 16 символов, не содержит пробелов и не начинается с '@'), сервер ответит:

json
{"type":"OK","message":"Логин установлен"}

Далее клиент должен зарегистрировать пароль:

json
{"type":"RegPass","message":"password"}

Если пароль корректный, сервер сохранит логин и пароль в файл login.txt и подтвердит успешную регистрацию.

Авторизация пользователя
Для авторизации клиент отправляет логин и пароль:

json
{"type":"Auth","message":"username password"}

Если пара логин/пароль корректна, сервер ответит:

json
{"type":"OK","message":"Вы успешно подключены к чату"}

Обмен сообщениями

Личное сообщение: Клиент может отправить сообщение другому пользователю:

json
{"type":"Message","recipient":"username","message":"Hello!"}

Сообщение всем: Клиент может отправить сообщение всем пользователям:

json
{"type":"MessageAll","message":"Hello, everyone!"}

Завершение работы клиента

Клиент может завершить свою работу и выйти из чата:

json
{"type":"Exit"}

## Управление сервером

Завершение работы сервера: В консоли сервера введите команду exit. Сервер корректно завершит свою работу, отключив всех клиентов.

## Особенности

Файл login.txt: Этот файл используется для хранения логинов и паролей. При регистрации нового пользователя файл автоматически обновляется.
Обработка ошибок: Сервер проверяет корректность логина и пароля, а также обрабатывает ошибки при отправке и получении сообщений.
Одновременные подключения: Сервер поддерживает одновременные подключения множества клиентов благодаря многопоточности.

## Примечания

Для взаимодействия с сервером вы можете использовать любой TCP-клиент (например, Telnet, Netcat или собственное клиентское приложение).
Все сообщения отправляются в формате JSON.


------------------------------------


# ChatClient: Руководство по использованию

## Описание

ChatClient — это клиент для подключения к многопользовательскому серверному чату. Он позволяет пользователям регистрироваться, авторизовываться и обмениваться сообщениями с другими пользователями чата. Программа написана на Java и использует сокеты для подключения к серверу.

## Требования

Java 8+ - Убедитесь, что у вас установлена версия Java 8 или выше. Если Java не установлена, скачайте и установите её с официального сайта Oracle или через пакетный менеджер (например, apk, apt, yum).
Сервер ChatServer - Клиент должен подключаться к запущенному серверу ChatServer, который поддерживает функционал чата.

## Запуск клиента

Скомпилируйте код клиента:
javac ChatClient.java

Запустите скомпилированный класс ChatClient:
java ChatClient

При запуске программы вам будет предложено ввести хост и порт для подключения к чату. После успешного подключения следуйте дальнейшим инструкциям на экране.

## Использование программы

Регистрация пользователя
При первом подключении введите 2, чтобы зарегистрировать нового пользователя.
Введите логин. Сервер проверит корректность логина и ответит сообщением.
Введите пароль. После успешной регистрации вы сможете продолжить использование чата.

Авторизация пользователя
Если вы уже зарегистрированы, введите 1 и укажите свой логин и пароль для авторизации.
После успешной авторизации вы сможете начать обмен сообщениями с другими пользователями.

Отправка сообщений
Личное сообщение: Для отправки личного сообщения используйте символ @ перед логином адресата, например:

@username Привет!

Сообщение всем: Для отправки сообщения всем пользователям чата просто введите текст сообщения без символа @.

Выход из чата: Для выхода из программы введите команду exit.

## Особенности

Асинхронная обработка сообщений: Клиент получает и отображает сообщения в реальном времени с сервера.
Обработка ошибок: Программа обрабатывает ошибки подключения, регистрации, авторизации и обмена сообщениями.
JSON-формат сообщений: Все сообщения отправляются и принимаются в формате JSON.

## Примечания

Для работы клиента необходимо наличие активного подключения к серверу ChatServer.
Программа автоматически отключает клиента от сервера при завершении работы.


------------------------------------

### В папке out/artifacts можно найти уже скомпилированные jar файлы, как для сервера, так и для клиента уже готовые для запуска
