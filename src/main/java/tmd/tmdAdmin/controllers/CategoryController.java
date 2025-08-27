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
import tmd.tmdAdmin.data.entities.*;
import tmd.tmdAdmin.data.repositories.CategoryRepository;
import tmd.tmdAdmin.data.repositories.DimensionRepository;
import tmd.tmdAdmin.data.repositories.ProductRepository;
import tmd.tmdAdmin.storage.FileStorageService;
import tmd.tmdAdmin.utils.ModelAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final ModelAttributes modelAttributes;
    private final DimensionRepository dimensionRepository;
    @GetMapping({"", "/"})
    public String category(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                          Model model,
                          HttpServletRequest request) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category album not found with ID: " + categoryId));

            modelAttributes.setModelAttributes(model, request, category.getName() + " Products | El Dahman", new String[]{"/css/gallery.css"});
            model.addAttribute("category", category);
            model.addAttribute("products", category.getProducts());

            return "view-category";
        }
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        modelAttributes.setModelAttributes(model, request, "Categories | El Dahman", new String[]{"/css/gallery.css"});
        return "category";
    }
    @GetMapping("/add/form")
    public String showAddCategoryForm(Model model, HttpServletRequest request) {
        modelAttributes.setModelAttributes(model, request, "Add New Category | El Dahman", null);
        model.addAttribute("category", new Category());
        return "add-category-form";
    }
    @PostMapping("")
    public String addGalleryType(@Valid @ModelAttribute Category category,
                                 BindingResult bindingResult,

                                 @RequestParam("albumProducts") MultipartFile[] albumProducts,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUri", request.getRequestURI()); // Keep currentUri for layout
            model.addAttribute("pageTitle", "Create New Album | El Dahman");
            return "add-category-form"; // Return to the form with errors
        }
        if (category.getId() == 0 && categoryRepository.findById(category.getId()).isPresent()) { // Only check for new albums
            bindingResult.rejectValue("name", "name.duplicate", "Category with this name already exists.");
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Create New Category | El Dahman");
            return "add-category-form";
        }

        String fileErrorMessage = null;
//        if (coverImageFile == null || coverImageFile.isEmpty()) {
//            fileErrorMessage = "Please upload a cover image for the album.";
//        } else if (!fileStorageService.isImageFile(coverImageFile)) {
//            fileErrorMessage = "Cover image must be a valid image file (JPEG, PNG, GIF, BMP, WEBP).";
//        } else
            if (albumProducts == null || albumProducts.length == 0 || albumProducts[0].isEmpty()) {
            fileErrorMessage = "Please upload at least one image for the album content.";
        } else {
            for (MultipartFile file : albumProducts) {
                if (!file.isEmpty() && !fileStorageService.isImageFile(file)) {
                    fileErrorMessage = "One or more content images are not valid image files (JPEG, PNG, GIF, BMP, WEBP).";
                    break;
                }
            }
        }

        if (fileErrorMessage != null) {
            model.addAttribute("errorMessage", fileErrorMessage); // Add error to model for display
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Create New Category | El Dahman");
            return "add-category-form"; // Return to the form with file errors
        }

        try {
//            String coverImagePath = fileStorageService.storeGalleryTypeCover(coverImageFile);
//            galleryType.setPath(coverImagePath); // Set the path directly to GalleryType.path
            List<Dimension> savedDims = new ArrayList<>();
            for (Dimension dim : category.getDimensions()) {
                Dimension saved = dimensionRepository.save(dim);
                savedDims.add(saved);
            }
            category.setDimensions(savedDims);


            Category savedCategory = categoryRepository.save(category); // galleryType.id will be set after save

            // 3. Handle Album Content Image Uploads
            for (MultipartFile file : albumProducts) {
                if (!file.isEmpty()) {
                    // This creates a 'Gallery' entity (which represents an image)
                    // We can pass original filename as caption for Gallery.name
                    Products product = fileStorageService.storeProductsforCategory(file, savedCategory, file.getOriginalFilename());
                    productRepository.save(product); // Save the image entity
                    savedCategory.addProduct(product); // Add image to album's list
                }
            }

            categoryRepository.save(savedCategory); // Re-save to ensure image list is updated

            redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + savedCategory.getName() + "' and images added successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }
        return "redirect:/category";
    }
    @GetMapping("/update/form")
    public String showEditGalleryTypeForm(@RequestParam("itemId") Integer categoryId, Model model, HttpServletRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + categoryId));

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Edit Album | El Dahman");
        model.addAttribute("category", category);
        return "edit-category-form";
    }
    @PostMapping("/update")
    public String updateGalleryType(@Valid @ModelAttribute Category category,
                                    BindingResult bindingResult,

                                    RedirectAttributes redirectAttributes,
                                    Model model,
                                    HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Edit Album | El Dahman");

            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new RuntimeException("Category  not found with ID: " + category.getId()));

            return "edit-gallery-form";
        }

        String fileErrorMessage = null;


        if (fileErrorMessage != null) {
            model.addAttribute("errorMessage", fileErrorMessage);
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Edit Category | El Dahman");
            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new RuntimeException("Category album not found with ID: " + category.getId()));

            return "edit-gallery-form";
        }


        try {
            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + category.getId()));

            existingCategory.setName(category.getName());
            existingCategory.setNameAr(category.getNameAr());
            existingCategory.setNameRu(category.getNameRu());
            existingCategory.setDescription(category.getDescription());
            existingCategory.setDescriptionAr(category.getDescriptionAr());
            existingCategory.setDescriptionRu(category.getDescriptionRu());
            existingCategory.setMaterial(category.getMaterial());

            List<Dimension> savedDims = new ArrayList<>();
            for (Dimension dim : category.getDimensions()) {
                Dimension saved = dimensionRepository.save(dim);
                savedDims.add(saved);
            }
            existingCategory.setDimensions(savedDims);

//            if (newCoverImageFile != null && !newCoverImageFile.isEmpty()) {
//                if (existingGalleryType.getPath() != null && !existingGalleryType.getPath().isEmpty()) {
//                    fileStorageService.deleteFile(existingGalleryType.getPath());
//                }
//                String newCoverImagePath = fileStorageService.storeGalleryTypeCover(newCoverImageFile);
//                existingGalleryType.setPath(newCoverImagePath);
//            }

            categoryRepository.save(existingCategory);

            redirectAttributes.addFlashAttribute("successMessage", "Category '" + existingCategory.getName() + "' updated successfully!");
        }  catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during album update: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/category";
    }
    @PostMapping("/delete")
    public String deleteGalleryType(@RequestParam("itemId") Integer categoryId,
                                    RedirectAttributes redirectAttributes) {
        try {
            Category categoryToDelete = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

            // Delete all associated files from disk first


            categoryRepository.delete(categoryToDelete);

            redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + categoryToDelete.getName() + "' and all its images deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during album deletion: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/category";
    }
    @GetMapping("/products/add/form/{categoryId}")
    public String showAddImagesToAlbumForm(@PathVariable("categoryId") Integer categoryId,
                                           Model model,
                                           HttpServletRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Add Products to " + category.getName() + " | El Dahman");
        model.addAttribute("category", category);
        return "add-products-to-category";
    }
    @PostMapping("/products/add/{categoryId}")
    public String addImagesToAlbum(@PathVariable("categoryId") Integer categoryId,
                                   @RequestParam("newProductImage") MultipartFile[] newProductsImages,
                                   RedirectAttributes redirectAttributes,
                                   Model model, // For returning to form with errors
                                   HttpServletRequest request) { // For currentUri

        String fileErrorMessage = null;
        if (newProductsImages == null || newProductsImages.length == 0 || newProductsImages[0].isEmpty()) {
            fileErrorMessage = "Please select images to add.";
        } else {
            for (MultipartFile file : newProductsImages) {
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
            Category category = categoryRepository.findById(categoryId) // Fetch galleryType again
                    .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + categoryId));
            model.addAttribute("galleryType", category);
            return "add-images-to-album-form";
        }

        try {
            Category existingCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

            if (!newProductsImages[0].isEmpty()) {
                for (MultipartFile file : newProductsImages) {
                    if (!file.isEmpty()) {
                        Products albumProduct = fileStorageService.storeProductsforCategory(file, existingCategory, file.getOriginalFilename());
                        productRepository.save(albumProduct);
                        existingCategory.addProduct(albumProduct);
                    }
                }
                categoryRepository.save(existingCategory);

                redirectAttributes.addFlashAttribute("successMessage", newProductsImages.length + " images added to '" + existingCategory.getName() + "' successfully!");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/category?categoryId=" + categoryId;
    }
    @PostMapping("/deleteProduct")
    public String deleteImage(@RequestParam("productId") Integer productId,
                              @RequestParam("categoryId") Integer categoryId,
                              RedirectAttributes redirectAttributes) {
        try {
            Products productToDelete = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

            // Remove from category first
            category.getProducts().remove(productToDelete);
            // Delete file from disk using the path stored in Gallery.path
            fileStorageService.deleteFile(productToDelete.getPath());
            productRepository.delete(productToDelete);
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete Product file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during Product deletion: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/view-category/" + categoryId;
    }

}
