-- name: create-user!
-- creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- name: get-user
-- retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id

-- name: get-tags
select id, tag_name, description,
to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at,
to_char(updated_at, 'YYYY-MM-DD HH24:MI:SS') as updated_at
from tags;
-- name: get-notes
select id, content,
to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at,
to_char(updated_at, 'YYYY-MM-DD HH24:MI:SS') as updated_at
from notes order by updated_at desc offset 5 limit 5;
-- name: save-tag<!
insert into tags (tag_name, description, created_at, updated_at)
values (:tag_name, :description, :created_at, :updated_at);
-- name: save-note<!
insert into notes (content, created_at, updated_at)
values (:content, :created_at, :updated_at);
