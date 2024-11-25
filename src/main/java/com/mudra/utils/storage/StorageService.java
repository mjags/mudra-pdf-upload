package com.mudra.utils.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.mudra.utils.dto.FileMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
@Slf4j
public class StorageService {

    @Value("${upload.path}")
    private String path;

    public void uploadFile(MultipartFile file) {

        try {
            String fileName = file.getOriginalFilename();
            InputStream is = file.getInputStream();
            Files.copy(is, Paths.get(path + fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", fileName);
        } catch (IOException e) {
            String msg = String.format("Failed to store file %f", file.getName());
            throw new StorageException(msg, e);
        }
    }

    public List<FileMetaInfo> getFileFromGcs() {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        String directory = "src/";
        List<FileMetaInfo> fileMetaInfoList = null;
        Page<Blob> blobPage = storage.list("mudra-ocr-telugu", Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(directory));
        if (blobPage != null) {
            fileMetaInfoList = new ArrayList<>();
            for (Blob blob : blobPage.iterateAll()) {
                if (!directory.equalsIgnoreCase(blob.getName())) {
                    FileMetaInfo metaInfo = new FileMetaInfo();
                    metaInfo.setName(blob.getName());
                    metaInfo.setCreatedTs(LocalDateTime.ofInstant(Instant.ofEpochMilli(blob.getCreateTime()),
                            TimeZone.getDefault().toZoneId()));
                    metaInfo.setFileSize(blob.getSize());
                    fileMetaInfoList.add(metaInfo);
                }
            }
        }
        return fileMetaInfoList;
    }

    public void uploadToGcs(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        BlobId blobId = constructBlobId("mudra-ocr-telugu", "src", fileName);
        if (blobId != null) {
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            Storage storage = StorageOptions.getDefaultInstance().getService();
            storage.create(blobInfo, file.getBytes());
            log.info("File uploaded successfully: {}", fileName);
        }
    }

    private BlobId constructBlobId(String bucketName, @Nullable String subdirectory,
                                   String fileName) {
        return Optional.ofNullable(subdirectory)
                .map(s -> BlobId.of(bucketName, subdirectory + "/" + fileName))
                .orElse(BlobId.of(bucketName, fileName));
    }
}