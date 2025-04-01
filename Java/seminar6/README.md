# seminar-spring-data-jpa

Проект реализует вариант анкетирования.

Существует 5 базовых классов, вокруг которых строится работа:

- [Answer.java](src/main/java/ru/tinkoff/education/backend/academy/model/dto/Answer.java)
- [Form.java](src/main/java/ru/tinkoff/education/backend/academy/model/dto/Form.java)
- [Question.java](src/main/java/ru/tinkoff/education/backend/academy/model/dto/Question.java)
- [User.java](src/main/java/ru/tinkoff/education/backend/academy/model/dto/User.java)
- [UserAnswer.java](src/main/java/ru/tinkoff/education/backend/academy/model/dto/UserAnswer.java)

В пакете [dao](src/main/java/ru/tinkoff/education/backend/academy/dao) можно найти DAO интерфейсы
- [AnswerDao.java](src/main/java/ru/tinkoff/education/backend/academy/dao/AnswerDao.java)
- [FormDao.java](src/main/java/ru/tinkoff/education/backend/academy/dao/FormDao.java)
- [QuestionDao.java](src/main/java/ru/tinkoff/education/backend/academy/dao/QuestionDao.java)
- [UserAnswerDao.java](src/main/java/ru/tinkoff/education/backend/academy/dao/UserAnswerDao.java)
- [UserDao.java](src/main/java/ru/tinkoff/education/backend/academy/dao/UserDao.java)

3 имплементации данных интерфейсов:
- [jdbc](src/main/java/ru/tinkoff/education/backend/academy/dao/jdbc)
- [jpa](src/main/java/ru/tinkoff/education/backend/academy/dao/jpa)
- [datajpa](src/main/java/ru/tinkoff/education/backend/academy/dao/datajpa)

Предлагается прямо на семинаре реализовать DAO в пакетах jpa && datajpa для погружения в JPA & Spring Data JPA

jdbc же пакет может быть рассмотрен как уже что-то знакомое из предыдущего семинара и в конце можно 
сравнить полученные результаты.

Написанный функционал можно проверить, запустив написанные заранее тесты, которые лежат 
в соответствующих пакетах:
- [jdbc](src/test/java/ru/tinkoff/education/backend/academy/dao/jdbc)
- [jpa](src/test/java/ru/tinkoff/education/backend/academy/dao/jpa)
- [datajpa](src/test/java/ru/tinkoff/education/backend/academy/dao/datajpa)