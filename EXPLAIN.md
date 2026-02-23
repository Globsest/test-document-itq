# Пример поискового запроса

## Пример запроса

Поиск документов по статусу и периоду дат создания (пагинация, сортировка по дате создания по убыванию):

```sql
SELECT d.id, d.number, d.author, d.title, d.status, d.created_at, d.updated_at, d.version
FROM documents d
WHERE d.status = 'SUBMITTED'
  AND d.created_at BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59'
ORDER BY d.created_at DESC
LIMIT 20 OFFSET 0;
```

В API это соответствует вызову вида:
`GET /api/documents/search?status=SUBMITTED&dateFrom=2024-01-01T00:00:00&dateTo=2024-12-31T23:59:59&page=0&size=20&sortBy=createdAt&sortDirection=DESC`

Период в API трактуется по дате создания документа (поле `created_at`).

## EXPLAIN (ANALYZE)

```text
Limit  (cost=... rows=20)
  ->  Sort  (cost=... rows=...)
        Sort Key: d.created_at DESC
        ->  Index Scan using idx_document_status on documents d
              Index Cond: (status = 'SUBMITTED'::text)
              Filter: (created_at >= '2024-01-01'::timestamp AND created_at <= '2024-12-31'::timestamp)
```

(Конкретные cost/rows зависят от объёма данных и статистики.)

## Пояснение по индексам

- **idx_document_status** — индекс по `status`. Позволяет быстро отобрать документы по статусу; затем по отфильтрованным строкам применяется условие по `created_at` и сортировка.
- **idx_document_created_at** — индекс по `created_at`. Может использоваться для сортировки и для фильтра по периоду. В зависимости от селективности (соотношения статусов) планировщик может выбрать сканирование по статусу или по дате.
- **idx_document_author** — для фильтра по автору (в т.ч. поиск по подстроке через LIKE при необходимости).

Составной индекс по `(status, created_at)` при наличии такого в схеме даст возможность выполнять и фильтр по статусу, и отбор по диапазону дат, и сортировку по `created_at` без отдельной сортировки. В текущей схеме Liquibase заданы отдельные индексы по `status` и по `created_at`, что уже даёт выигрыш по сравнению с полным сканированием таблицы.
