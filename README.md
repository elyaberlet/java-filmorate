# java-filmorate
   [Схема базы данных](database-schema.png)

---

# Описание схемы базы данных

## Краткое описание

Схема базы данных предназначена для приложения, работающего с пользователями, фильмами, жанрами, рейтингами и системой дружбы.  
Она включает основные сущности и таблицы-связки, реализующие отношения 1:М, М:1 и М:М.

---

## Таблицы и связи

### user
Хранит данные о пользователях.

**Связи:**
- 1:М с таблицей friendship
- М:М с film через film_like

### film
Информация о фильмах.

**Связи:**
- М:1 с genre
- М:1 с rating
- М:М с user через film_like

### genre
Справочник жанров.

**Связь:**
- 1:М с film

### rating
Справочник возрастных рейтингов.

**Связь:**
- 1:М с film

### friendship
Реализует дружбу между пользователями.

**Связи:**
- М:М между user и user
- М:1 со status

### status
Статусы дружбы.

**Связь:**
- 1:М с friendship

### film_like
Лайки фильмов пользователями.

**Связь:**
- М:М между user и film

---

## Примеры запросов

### Film

**1. Поиск информации о фильме по id**
```sql
SELECT *
FROM film
WHERE film_id = 1324;
```

**2. Получить 5 самых популярных фильмов в жанре комедия**
```sql
SELECT f.name
FROM film f
LEFT JOIN genre g ON f.genre_id = g.genre_id
LEFT JOIN film_like fl ON f.film_id = fl.film_id
WHERE LOWER(g.genre_name) = LOWER('комедия') OR g.genre_name = 'comedy'
GROUP BY f.film_id, f.name
ORDER BY COUNT(fl.user_id) DESC
LIMIT 5;
```

**3. Фильмы после 2023 года**
```sql
SELECT 
    f.name AS film_name,
    g.genre_name AS genre,
    f.duration AS duration_minutes
FROM film f
LEFT JOIN genre g ON f.genre_id = g.genre_id
WHERE EXTRACT(YEAR FROM f.release_date) > 2023
ORDER BY f.name;
```

---

### User

**1. Получение пользователя по id**
```sql
SELECT 
    user_id,
    name,
    email,
    login,
    birthday
FROM user
WHERE user_id = 643;
```

**2. Получение списка друзей пользователя**
```sql
SELECT 
    u.user_id,
    u.name,
    u.email,
    u.login,
    u.birthday
FROM user u
JOIN friendship f ON u.user_id = f.friend_id
WHERE f.user_id = ? AND f.status_id = 1;
```

**3. Получение общих друзей двух пользователей**
```sql
SELECT 
    u.user_id,
    u.name,
    u.email,
    u.login,
    u.birthday
FROM user u
WHERE u.user_id IN (
    SELECT f.friend_id
    FROM friendship f
    WHERE f.user_id = ? AND f.status_id = 1
)
AND u.user_id IN (
    SELECT f.friend_id
    FROM friendship f
    WHERE f.user_id = ? AND f.status_id = 1
);
```

---
