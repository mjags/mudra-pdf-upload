package com.mudra.utils.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileMetaInfo {
    String name;
    LocalDateTime createdTs;
    float fileSize;
}
