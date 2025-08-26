package tmd.tmdAdmin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tmd.tmdAdmin.data.entities.Gallery;
import tmd.tmdAdmin.data.entities.GalleryType;
import tmd.tmdAdmin.data.repositories.GalleryRepository;
import tmd.tmdAdmin.data.repositories.GalleryTypeRepository;
import tmd.tmdAdmin.storage.FileStorageService;
import tmd.tmdAdmin.utils.ModelAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/gallery")
public class GalleryController {

    private final GalleryRepository galleryRepository;
    private final GalleryTypeRepository galleryTypeRepository;
    private final FileStorageService fileStorageService;
    private final ModelAttributes modelAttributes;


    @GetMapping({"", "/"})
    public String gallery(@RequestParam(value = "galleryTypeId", required = false) Integer galleryTypeId,
                          Model model,
                          HttpServletRequest request) {
        if (galleryTypeId != null) {
            GalleryType galleryType = galleryTypeRepository.findById(galleryTypeId)
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));

            modelAttributes.setModelAttributes(model, request, galleryType.getName() + " Images | El Dahman", new String[]{"/css/gallery.css"});
            model.addAttribute("galleryType", galleryType);
            model.addAttribute("images", galleryType.getImages());

            return "view-gallery";
        }
        List<GalleryType> galleries = galleryTypeRepository.findAll();
        model.addAttribute("galleries", galleries);
        modelAttributes.setModelAttributes(model, request, "Galleries | El Dahman", new String[]{"/css/gallery.css"});
        return "gallery";
    }


    @GetMapping("/add/form")
    public String showAddGalleryForm(Model model, HttpServletRequest request) {
        modelAttributes.setModelAttributes(model, request, "Add New Gallery | El Dahman", null);
        model.addAttribute("galleryType", new GalleryType());
        return "add-gallery-form";
    }

    @PostMapping("")
    public String addGalleryType(@Valid @ModelAttribute GalleryType galleryType,
                                 BindingResult bindingResult,
                                 @RequestParam("coverImageFile") MultipartFile coverImageFile,
                                 @RequestParam("albumImageFiles") MultipartFile[] albumImageFiles,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUri", request.getRequestURI()); // Keep currentUri for layout
            model.addAttribute("pageTitle", "Create New Album | El Dahman");
            return "add-gallery-form"; // Return to the form with errors
        }
        if (galleryType.getId() == 0 && galleryTypeRepository.findByName(galleryType.getName()).isPresent()) { // Only check for new albums
            bindingResult.rejectValue("name", "name.duplicate", "Album with this name already exists.");
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Create New Album | El Dahman");
            return "add-gallery-form";
        }

        String fileErrorMessage = null;
        if (coverImageFile == null || coverImageFile.isEmpty()) {
            fileErrorMessage = "Please upload a cover image for the album.";
        } else if (!fileStorageService.isImageFile(coverImageFile)) {
            fileErrorMessage = "Cover image must be a valid image file (JPEG, PNG, GIF, BMP, WEBP).";
        } else if (albumImageFiles == null || albumImageFiles.length == 0 || albumImageFiles[0].isEmpty()) {
            fileErrorMessage = "Please upload at least one image for the album content.";
        } else {
            for (MultipartFile file : albumImageFiles) {
                if (!file.isEmpty() && !fileStorageService.isImageFile(file)) {
                    fileErrorMessage = "One or more content images are not valid image files (JPEG, PNG, GIF, BMP, WEBP).";
                    break;
                }
            }
        }

        if (fileErrorMessage != null) {
            model.addAttribute("errorMessage", fileErrorMessage); // Add error to model for display
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Create New Album | El Dahman");
            return "add-gallery-form"; // Return to the form with file errors
        }

        try {
            String coverImagePath = fileStorageService.storeGalleryTypeCover(coverImageFile);
            galleryType.setPath(coverImagePath); // Set the path directly to GalleryType.path


            GalleryType savedGalleryType = galleryTypeRepository.save(galleryType); // galleryType.id will be set after save

            // 3. Handle Album Content Image Uploads
            for (MultipartFile file : albumImageFiles) {
                if (!file.isEmpty()) {
                    // This creates a 'Gallery' entity (which represents an image)
                    // We can pass original filename as caption for Gallery.name
                    Gallery albumImage = fileStorageService.storeGalleryContentImage(file, savedGalleryType, file.getOriginalFilename());
                    galleryRepository.save(albumImage); // Save the image entity
                    savedGalleryType.addImage(albumImage); // Add image to album's list
                }
            }
            galleryTypeRepository.save(savedGalleryType); // Re-save to ensure image list is updated

            redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + savedGalleryType.getName() + "' and images added successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }
        return "redirect:/gallery";
    }

    @PostMapping("/image/delete")
    public String deleteImage(@RequestParam("imageId") Integer imageId,
                              @RequestParam("galleryTypeId") Integer galleryTypeId,
                              RedirectAttributes redirectAttributes) {
        try {
            Gallery imageToDelete = galleryRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));

            // Delete file from disk using the path stored in Gallery.path
            fileStorageService.deleteFile(imageToDelete.getPath());
            galleryRepository.delete(imageToDelete);

            redirectAttributes.addFlashAttribute("successMessage", "Image deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete image file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during image deletion: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/viewGallery/" + galleryTypeId;
    }

    @PostMapping("/delete")
    public String deleteGalleryType(@RequestParam("itemId") Integer galleryTypeId,
                                    RedirectAttributes redirectAttributes) {
        try {
            GalleryType galleryTypeToDelete = galleryTypeRepository.findById(galleryTypeId)
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));

            // Delete all associated files from disk first
            if (galleryTypeToDelete.getPath() != null && !galleryTypeToDelete.getPath().isEmpty()) {
                fileStorageService.deleteFile(galleryTypeToDelete.getPath());
            }
            for (Gallery image : galleryTypeToDelete.getImages()) {
                fileStorageService.deleteFile(image.getPath());
            }

            galleryTypeRepository.delete(galleryTypeToDelete);

            redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + galleryTypeToDelete.getName() + "' and all its images deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete album files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during album deletion: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/gallery";
    }

    @GetMapping("/update/form")
    public String showEditGalleryTypeForm(@RequestParam("itemId") Integer galleryTypeId, Model model, HttpServletRequest request) {
        GalleryType galleryType = galleryTypeRepository.findById(galleryTypeId)
                .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Edit Album | El Dahman");
        model.addAttribute("galleryType", galleryType);
        return "edit-gallery-form";
    }

    @PostMapping("/update")
    public String updateGalleryType(@Valid @ModelAttribute GalleryType galleryType,
                                    BindingResult bindingResult,
                                    @RequestParam(value = "newCoverImageFile", required = false) MultipartFile newCoverImageFile,
                                    RedirectAttributes redirectAttributes,
                                    Model model,
                                    HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Edit Album | El Dahman");

            GalleryType existingGalleryType = galleryTypeRepository.findById(galleryType.getId())
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryType.getId()));
            galleryType.setPath(existingGalleryType.getPath());
            return "edit-gallery-form";
        }

        String fileErrorMessage = null;
        if (newCoverImageFile != null && !newCoverImageFile.isEmpty()) {
            if (!fileStorageService.isImageFile(newCoverImageFile)) {
                fileErrorMessage = "New cover image must be a valid image file (JPEG, PNG, GIF, BMP, WEBP).";
            }
        }

        if (fileErrorMessage != null) {
            model.addAttribute("errorMessage", fileErrorMessage);
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Edit Album | El Dahman");
            GalleryType existingGalleryType = galleryTypeRepository.findById(galleryType.getId())
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryType.getId()));
            galleryType.setPath(existingGalleryType.getPath());
            return "edit-gallery-form";
        }


        try {
            GalleryType existingGalleryType = galleryTypeRepository.findById(galleryType.getId())
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryType.getId()));

            existingGalleryType.setName(galleryType.getName());
            existingGalleryType.setNameAr(galleryType.getNameAr());
            existingGalleryType.setNameRu(galleryType.getNameRu());

            if (newCoverImageFile != null && !newCoverImageFile.isEmpty()) {
                if (existingGalleryType.getPath() != null && !existingGalleryType.getPath().isEmpty()) {
                    fileStorageService.deleteFile(existingGalleryType.getPath());
                }
                String newCoverImagePath = fileStorageService.storeGalleryTypeCover(newCoverImageFile);
                existingGalleryType.setPath(newCoverImagePath);
            }

            galleryTypeRepository.save(existingGalleryType);

            redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + existingGalleryType.getName() + "' updated successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload new cover image: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during album update: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/gallery";
    }

    @GetMapping("/images/add/form/{galleryTypeId}")
    public String showAddImagesToAlbumForm(@PathVariable("galleryTypeId") Integer galleryTypeId,
                                           Model model,
                                           HttpServletRequest request) {
        GalleryType galleryType = galleryTypeRepository.findById(galleryTypeId)
                .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Add Images to " + galleryType.getName() + " | El Dahman");
        model.addAttribute("galleryType", galleryType);
        return "add-images-to-album-form";
    }

    @PostMapping("/images/add/{galleryTypeId}")
    public String addImagesToAlbum(@PathVariable("galleryTypeId") Integer galleryTypeId,
                                   @RequestParam("newAlbumImageFiles") MultipartFile[] newAlbumImageFiles,
                                   RedirectAttributes redirectAttributes,
                                   Model model, // For returning to form with errors
                                   HttpServletRequest request) { // For currentUri

        String fileErrorMessage = null;
        if (newAlbumImageFiles == null || newAlbumImageFiles.length == 0 || newAlbumImageFiles[0].isEmpty()) {
            fileErrorMessage = "Please select images to add.";
        } else {
            for (MultipartFile file : newAlbumImageFiles) {
                if (!file.isEmpty() && !fileStorageService.isImageFile(file)) {
                    fileErrorMessage = "One or more selected files are not valid image files (JPEG, PNG, GIF, BMP, WEBP).";
                    break;
                }
            }
        }

        if (fileErrorMessage != null) {
            model.addAttribute("errorMessage", fileErrorMessage);
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Add Images to Album | El Dahman");
            GalleryType galleryType = galleryTypeRepository.findById(galleryTypeId) // Fetch galleryType again
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));
            model.addAttribute("galleryType", galleryType);
            return "add-images-to-album-form";
        }

        try {
            GalleryType existingGalleryType = galleryTypeRepository.findById(galleryTypeId)
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));

            if (!newAlbumImageFiles[0].isEmpty()) {
                for (MultipartFile file : newAlbumImageFiles) {
                    if (!file.isEmpty()) {
                        Gallery albumImage = fileStorageService.storeGalleryContentImage(file, existingGalleryType, file.getOriginalFilename());
                        galleryRepository.save(albumImage);
                        existingGalleryType.addImage(albumImage);
                    }
                }
                galleryTypeRepository.save(existingGalleryType);

                redirectAttributes.addFlashAttribute("successMessage", newAlbumImageFiles.length + " images added to '" + existingGalleryType.getName() + "' successfully!");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/gallery?galleryTypeId=" + galleryTypeId;
    }
}
