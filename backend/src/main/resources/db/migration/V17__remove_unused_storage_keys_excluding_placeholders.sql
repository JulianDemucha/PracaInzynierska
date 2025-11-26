DELETE FROM storage_keys sk
WHERE NOT EXISTS (
    SELECT 1 FROM app_users au WHERE au.avatar_storage_key_id = sk.id
)
  AND NOT EXISTS (
    SELECT 1 FROM albums a WHERE a.cover_storage_key_id = sk.id
)
  AND NOT EXISTS (
    SELECT 1 FROM songs s WHERE s.audio_storage_key_id = sk.id
)
  AND NOT EXISTS (
    SELECT 1 FROM songs s2 WHERE s2.cover_storage_key_id = sk.id
)
  AND sk.id NOT IN (5000, 6767);
