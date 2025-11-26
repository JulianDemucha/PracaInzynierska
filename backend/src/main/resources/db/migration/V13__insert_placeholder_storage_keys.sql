INSERT INTO storage_keys (id, key, mime_type, size_bytes, created_at)
VALUES
    (6767, 'placeholders/cover_placeholder.jpg', 'image/jpg', 185168, '2025-11-26 17:26:31'),
    (5000, 'placeholders/audio_placeholder.m4a', 'audio/mp4', 1745574, '2025-11-26 17:31:51')
ON CONFLICT (id)
    DO UPDATE SET
                  key = EXCLUDED.key,
                  mime_type = EXCLUDED.mime_type,
                  size_bytes = EXCLUDED.size_bytes,
                  created_at = EXCLUDED.created_at;