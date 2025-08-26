package tmd.tmdAdmin.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tmd.tmdAdmin.data.entities.Gallery; // User's Gallery entity (for individual images)
import tmd.tmdAdmin.data.entities.GalleryType; // User's GalleryType entity (for albums)
import tmd.tmdAdmin.data.entities.Videos;
import tmd.tmdAdmin.data.entities.VideosType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final String uploadSubDir = "gallery/"; // Specific sub-directory for all gallery uploads

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        // Ensure the specific gallery subdirectory also exists
        Path galleryUploadPath = this.fileStorageLocation.resolve(uploadSubDir);
        Files.createDirectories(galleryUploadPath);

        System.out.println("Base file upload directory: " + this.fileStorageLocation.toString());
        System.out.println("Gallery image upload directory: " + galleryUploadPath.toString());
    }

    /**
     * Internal method to save a MultipartFile to disk and return its generated unique filename.
     * @param file The MultipartFile to store.
     * @return The unique filename generated for the stored file.
     * @throws IOException If file storage fails.
     */
    private String saveFileToDisk(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IOException("Filename contains invalid path sequence " + originalFilename);
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            fileExtension = originalFilename.substring(dotIndex);
        }
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        // Resolve target location within the specific gallery subdirectory
        Path targetLocation = this.fileStorageLocation.resolve(uploadSubDir).resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return storedFilename;
    }


    /**
     * Stores a file for a GalleryType (album cover) and returns its relative web path.
     * This path is intended to be set directly to GalleryType.path.
     * @param file The MultipartFile for the cover image.
     * @return The relative web path (e.g., "gallery/uuid.jpg").
     * @throws IOException If file storage fails.
     */
    public String storeGalleryTypeCover(MultipartFile file) throws IOException {
        String storedFilename = saveFileToDisk(file);
        return uploadSubDir + storedFilename; // e.g., "gallery/uuid.jpg"
    }

    /**
     * Stores a file for an individual Gallery (content image) and returns a Gallery entity.
     * This populates Gallery.name and Gallery.path.
     * @param file The MultipartFile for the content image.
     * @param galleryType The GalleryType (album) this image belongs to.
     * @param imageCaption The desired caption for the image (defaults to original filename).
     * @return A Gallery entity with its name and path set, linked to its GalleryType.
     * @throws IOException If file storage fails.
     */
    public Gallery storeGalleryContentImage(MultipartFile file, GalleryType galleryType, String imageCaption) throws IOException {
        String storedFilename = saveFileToDisk(file);
        String relativeWebPath = uploadSubDir + storedFilename;

        // Create a Gallery entity, setting only name and path as per your table structure
        Gallery galleryImage = new Gallery();
        galleryImage.setName(imageCaption != null && !imageCaption.isEmpty() ? imageCaption : file.getOriginalFilename());
        galleryImage.setPath(relativeWebPath); // This is the user's 'path' field
        galleryImage.setGalleryType(galleryType); // Link to the galleryType (album)

        return galleryImage;
    }
    public Videos storeGalleryContentVideo(MultipartFile file, VideosType videosType, String videoCaption) throws IOException {
        String storedFilename = saveFileToDisk(file);
        String relativeWebPath = uploadSubDir + storedFilename;

        // Create a Gallery entity, setting only name and path as per your table structure
        Videos video = new Videos();
        video.setName(videoCaption != null && !videoCaption.isEmpty() ? videoCaption : file.getOriginalFilename());
        video.setPath(relativeWebPath); // This is the user's 'path' field
        video.setVideosType(videosType); // Link to the galleryType (album)

        return video;
    }

    /**
     * Loads a file as a Spring Resource based on its full relative web path.
     * @param fullRelativeWebPath The full path stored in the database (e.g., "gallery/uuid.jpg").
     * @return Resource representing the file.
     * @throws RuntimeException If the file is not found or an error occurs.
     */
    public Resource loadFileAsResource(String fullRelativeWebPath) {
        try {
            if (fullRelativeWebPath == null || !fullRelativeWebPath.startsWith(uploadSubDir)) {
                throw new IOException("Invalid or non-gallery path provided: " + fullRelativeWebPath);
            }
            // Extract the actual filename from the full path by removing the uploadSubDir prefix
            String filenameOnDisk = fullRelativeWebPath.substring(uploadSubDir.length());

            Path filePath = this.fileStorageLocation.resolve(uploadSubDir).resolve(filenameOnDisk).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found " + filenameOnDisk + " at " + filePath.toString());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading file from path " + fullRelativeWebPath, ex);
        }
    }

    /**
     * Deletes a file from the file system based on its relative web path.
     * @param fullRelativeWebPath The full path stored in the database (e.g., "gallery/uuid.jpg").
     * @throws IOException If file deletion fails.
     */
    public void deleteFile(String fullRelativeWebPath) throws IOException {
        if (fullRelativeWebPath == null || fullRelativeWebPath.isEmpty()) {
            System.out.println("Cannot delete file: path is null or empty.");
            return;
        }
        if (!fullRelativeWebPath.startsWith(uploadSubDir)) {
            System.out.println("Attempted to delete a file outside gallery directory: " + fullRelativeWebPath);
            return; // Prevent deleting arbitrary files
        }

        String filenameOnDisk = fullRelativeWebPath.substring(uploadSubDir.length());
        Path filePath = this.fileStorageLocation.resolve(uploadSubDir).resolve(filenameOnDisk).normalize();
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("Deleted file: " + fullRelativeWebPath + " (disk filename: " + filenameOnDisk + ")");
        } else {
            System.out.println("File not found for deletion: " + fullRelativeWebPath + " (disk filename: " + filenameOnDisk + ")");
        }
    }
}
