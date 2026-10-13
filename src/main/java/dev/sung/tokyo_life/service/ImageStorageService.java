package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.model.StoredImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000L;

    private final Path uploadDirectory;

    public ImageStorageService(
            @Value("${app.upload.directory}") String directory
    ) {
        this.uploadDirectory = Path.of(directory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 저장 폴더를 만들 수 없습니다.",
                    e
            );
        }
    }

    public StoredImage store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "이미지 파일을 선택해주세요."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "이미지는 한 장당 5MB 이하로 업로드해주세요."
            );
        }

        String originalFilename = getOriginalFilename(file);
        ValidatedImage validated = readAndValidate(file);

        String extension = validated.format();
        String contentType = "jpg".equals(extension)
                ? "image/jpeg"
                : "image/png";

        // 이미지를 다시 인코딩해 저장할 데이터 생성
        byte[] imageBytes;

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(
                    validated.image(),
                    extension,
                    output
            );

            if (!written) {
                throw new IllegalStateException(
                        "이미지 저장 형식을 처리할 수 없습니다."
                );
            }

            imageBytes = output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 변환 중 오류가 발생했습니다.",
                    e
            );
        } finally {
            validated.image().flush();
        }

        if (imageBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "변환된 이미지가 5MB를 초과합니다. "
                            + "사진 크기를 줄여서 업로드해주세요."
            );
        }

        String storageKey = UUID.randomUUID() + "." + extension;
        Path target = uploadDirectory.resolve(storageKey);

        try {
            Files.write(
                    target,
                    imageBytes,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 파일을 저장할 수 없습니다.",
                    e
            );
        }

        return new StoredImage(
                storageKey,
                originalFilename,
                contentType,
                (long) imageBytes.length
        );
    }

    private String getOriginalFilename(MultipartFile file) {

        String name = file.getOriginalFilename();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 이름을 확인할 수 없습니다."
            );
        }

        // 브라우저가 경로를 보내더라도 파일 이름 부분만 보관
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);

        if (name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException(
                    "파일 이름은 1~255자여야 합니다."
            );
        }

        return name;
    }

    private ValidatedImage readAndValidate(MultipartFile file) {

        try (
                InputStream input = file.getInputStream();
                ImageInputStream imageInput =
                        ImageIO.createImageInputStream(input)
        ) {
            if (imageInput == null) {
                throw new IllegalArgumentException(
                        "이미지를 읽을 수 없습니다."
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "JPEG 또는 PNG 이미지를 업로드해주세요."
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput);

                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                if ("jpeg".equals(format)) {
                    format = "jpg";
                }

                if (!"jpg".equals(format) && !"png".equals(format)) {
                    throw new IllegalArgumentException(
                            "JPEG 또는 PNG 이미지만 지원합니다."
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException(
                            "이미지는 최대 2,000만 픽셀까지 지원합니다."
                    );
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "정상적인 이미지 파일이 아닙니다."
                    );
                }

                return new ValidatedImage(image, format);

            } finally {
                reader.dispose();
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "이미지를 읽을 수 없습니다. 파일을 확인해주세요.",
                    e
            );
        }
    }

    private record ValidatedImage(
            BufferedImage image,
            String format
    ) {
    }
    public void delete(String storageKey) {

        if (storageKey == null
                || !storageKey.matches(
                "[0-9a-fA-F-]{36}\\.(jpg|png)"
        )) {
            throw new IllegalArgumentException(
                    "올바른 이미지 저장 키가 아닙니다."
            );
        }

        Path target = uploadDirectory.resolve(storageKey).normalize();

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 파일을 삭제할 수 없습니다.",
                    e
            );
        }
    }

    public Resource load(String storageKey) {

        if (storageKey == null
                || !storageKey.matches("[0-9a-fA-F-]{36}\\.(jpg|png)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path target = uploadDirectory.resolve(storageKey).normalize();

        if (!Files.isRegularFile(target) || !Files.isReadable(target)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "이미지 파일을 찾을 수 없습니다."
            );
        }

        return new FileSystemResource(target);
    }
}