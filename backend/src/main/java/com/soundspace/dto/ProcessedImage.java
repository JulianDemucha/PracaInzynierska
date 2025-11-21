package com.soundspace.dto;

public record ProcessedImage(byte[] bytes, String filename, String contentType) {}