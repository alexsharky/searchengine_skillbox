Search Engine Project от SkillBox 
Описание проекта: Данный проект представляет собой локальную поисковую систему, которая осуществляет индексацию веб-страниц и позволяет выполнять поиск по ним. Он поддерживает индексацию сайтов, заданных в конфигурации, и предоставляет API для запуска индексации, остановки индексации, поиска и получения статистики. 
Поиск осуществляется с помощью лемматизатора на русском и английском языке.

Порядок запуска приложения:
1.	В application.yaml прописать свой username, password и url к своей БД(предварительно создать пустую БД, в моем случае MySQL Workbench - БД search_engine). Так же прописать список web страниц, которые будем индексировать, в моем случае сайты Лента, СкиллБокс и ПлэйБэк.
2.	В pom.xml файле проверить версии зависимостей соответствующей вашей версии Java. В случае, если база лемматизации не подгрузиться в pom файле, рекомендуется скачать(ссылка в ТЗ) и добавить в ручную в проект.
3.	Запусить Application.java, обновить свою БД для проверки, если все ОК, открываем localhost:8080 и через Web-интерфейс запускаем индексацию(Start Indexing). В среде разработке по логам будет ясно запустился ли парсинг, так же при обновлении web-страницы мы увидим изменения.
4.	Как только все страницы проиндексируются(самая быстрая ПлэйБэк), можно делать запросы и проверять все ли корректно работает.

Конфигурация:
1.	indexing-settings – Список сайтов для индексации, берет из application.yaml.
2.	search-bot-settings – настройка индексации. Без него ожидания не будет, выставить мин и макс так же можно в application.yaml.
3.	search-settings – Вывод поискового запроса.

   
Стек используемых технологий:

Среда разработки: IDE

Java 20: Язык программирования, используемый для разработки.

Spring Boot 3.4.1: Фреймворк для создания приложений на Java, который упрощает разработку и настройку.

Spring Data JPA: Библиотека для работы с базами данных с использованием JPA (Java Persistence API).

MySQL: Система управления базами данных, используемая для хранения данных.

Lucene: Библиотека для полнотекстового поиска и индексации.

Jsoup: Библиотека для парсинга HTML и извлечения данных из веб-страниц.

Lombok: Библиотека, упрощающая написание Java-кода, позволяя избегать шаблонного кода.

 
