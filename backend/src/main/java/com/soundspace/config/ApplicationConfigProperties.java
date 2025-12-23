package com.soundspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ApplicationConfigProperties(
        JwtConfig jwt,
        StorageConfig storage,
        ViewsConfig views,
        MediaConfig media,
        CookieConfig cookie
) {

    public record JwtConfig(
            String secret,
            long expirationSeconds,
            long refreshExpirationSeconds
    ) {}

    public record StorageConfig(
            String root
    ) {}

    public record ViewsConfig(
            int cooldownSeconds,
            long bufferFlushRateMs
    ) {}

    public record MediaConfig(
            AvatarConfig avatar,
            CoverConfig cover,
            AudioConfig audio
    ) {
        public record AvatarConfig(
                String targetExtension,
                int width,
                int height,
                double quality,
                String directory,
                Long defaultAvatarId
        ) {}

        public record CoverConfig(
                String targetExtension,
                int width,
                int height,
                double quality,
                Long defaultCoverId,
                String albumDirectory,
                String playlistDirectory,
                String songDirectory
        ) {}

        public record AudioConfig(
                String targetExtension,
                String targetDirectory,
                Long defaultAudioId,
                Long uploadMaxBytes
        ) {}
    }

    public record CookieConfig(
            boolean secure,
            boolean httpOnly,
            String sameSite,
            String domain
    ) {}
}