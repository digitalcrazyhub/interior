package com.example.littlebighome.gallery.controller;


import com.example.littlebighome.gallery.entity.Category;
import com.example.littlebighome.gallery.entity.Gallery;
import com.example.littlebighome.gallery.repository.CategoryRepository;
import com.example.littlebighome.gallery.repository.GalleryRepository;
import com.example.littlebighome.gallery.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    @Autowired private GalleryRepository galleryRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private FileStorageService fileStorageService;

    @GetMapping
    public List<Gallery> getAllGallery() {
        return galleryRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<?> createGallery(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId) {

        try {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category"));

            String[] fileInfo = fileStorageService.saveFile(file);

            Gallery gallery = new Gallery();
            gallery.setTitle(title);
            gallery.setDescription(description);
            gallery.setCategory(category);
            gallery.setImageName(fileInfo[0]);
            gallery.setImageUrl(fileInfo[1]);
            gallery.setOriginalFilename(file.getOriginalFilename());
            gallery.setFileType(file.getContentType());
            gallery.setFileSize(file.getSize());

            return ResponseEntity.ok(galleryRepository.save(gallery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGallery(@PathVariable Long id) {
        return galleryRepository.findById(id).map(gallery -> {
            fileStorageService.deleteFile(gallery.getImageName());
            galleryRepository.delete(gallery);
            return ResponseEntity.ok("{\"success\":true,\"message\":\"Deleted successfully\"}");
        }).orElse(ResponseEntity.notFound().build());
    }
}