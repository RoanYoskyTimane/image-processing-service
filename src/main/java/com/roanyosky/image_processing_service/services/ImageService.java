package com.roanyosky.image_processing_service.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.roanyosky.image_processing_service.dtos.*;
import com.roanyosky.image_processing_service.entities.Image;
import com.roanyosky.image_processing_service.filters.SepiaFilter;
import com.roanyosky.image_processing_service.mappers.ImageMapper;
import com.roanyosky.image_processing_service.repositories.ImageRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    public List<ImageDto> getAllImages(){
        return imageRepository.findAll().stream().map(imageMapper::toDto).collect(Collectors.toList());
    }

    public ImageDto getImageById(UUID id){
        Image image = imageRepository.findById(id).orElseThrow(()->new RuntimeException("A imagem nao foi encontrada"+id));
        return imageMapper.toDto(image);
    }

    public ImageDto createImage(ImageCreateDto imageCreateDto){
        Image image = imageMapper.toEntity(imageCreateDto);
        imageRepository.save(image);

        return imageMapper.toDto(image);
    }

    public ImageDto updateImage(UUID id,ImageUpdateDto imageUpdateDto){
        Image image = imageRepository.findById(id).orElseThrow(()->new RuntimeException("A imagem nao foi encontrada"+id));
        imageMapper.updateDto(imageUpdateDto, image);
        return imageMapper.toDto(imageRepository.save(image));
    }

    public void deleteImage(UUID id){
        Image image = imageRepository.findById(id).orElseThrow(()-> new RuntimeException("A imagem nao foi encontrada"+id));
        imageRepository.delete(image);
    }

    public HashMap<String, Integer> findingMeasurement (MultipartFile uploadFile){
        try{
            // 1. Wrap the input stream in a BufferedInputStream for efficient reading
            InputStream is = new BufferedInputStream(uploadFile.getInputStream());

            // 2. Extract metadata without loading the full image into memory (Performance Efficient)
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            int width = 0;
            int height = 0;

            // 3. Iterate through metadata directories to find dimensions
            // Note: Different formats (JPG, PNG, WebP) store dimensions in different directories.
            for (Directory directory : metadata.getDirectories()) {
                // Check for JPEG specific dimensions
                if (directory.containsTag(JpegDirectory.TAG_IMAGE_WIDTH)) {
                    width = directory.getInt(JpegDirectory.TAG_IMAGE_WIDTH);
                    height = directory.getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
                    break;
                }
                // Check for PNG specific dimensions
                else if (directory.containsTag(PngDirectory.TAG_IMAGE_WIDTH)){
                    width = directory.getInt(PngDirectory.TAG_IMAGE_HEIGHT);
                    height = directory.getInt(PngDirectory.TAG_IMAGE_WIDTH);
                    break;
                }
                // Fallback for other formats (GIF, BMP, etc.) using generic tags if available
                else if (directory.getName().contains("Header") && directory.containsTag(1)) {
                    // Many directories use Tag 1 for Width and Tag 2 for Height
                    width = directory.getInt(1);
                    height = directory.getInt(2);
                }
            }

            HashMap<String, Integer> measurement = new HashMap<>();

            measurement.put("width",width);
            measurement.put("height", height);
            return measurement;

        } catch (ImageProcessingException | IOException | MetadataException e) {
            throw new RuntimeException(e);
        }
    }


    public ImageProcessingResult imageTransfomation(TransformDto transformDto, byte[]data){
        try{
            //1. Prepares the input and output streams
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            //2. Start of Thumbnailator pipeline
            // The size() is a starting point;
            var builder = Thumbnails.of(inputStream);


            //Resize
            if (transformDto.getResize() != null){
                System.out.println("The width"+transformDto.getResize().getWidth());
                System.out.println("The height:"+transformDto.getResize().getHeight());
                builder.size(transformDto.getResize().getWidth(),transformDto.getResize().getHeight());
            }
            else{
                builder.scale(1.0);
            }

            //Cropping
            if(transformDto.getCrop() != null){
                CropDto crop = transformDto.getCrop();
                builder.sourceRegion(crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight());
            }

            //Rotation
            if (transformDto.getRotate() != null){
                builder.rotate(transformDto.getRotate());
            }

            //Filters
            if (transformDto.getFilters() != null){
                if (Boolean.TRUE.equals(transformDto.getFilters().getGrayscale())) {
                    builder.imageType(BufferedImage.TYPE_BYTE_GRAY);
                }
                if (Boolean.TRUE.equals(transformDto.getFilters().getSepia())) {
                    builder.addFilter(new SepiaFilter());
                }
            }

            //Set output format
            String outputFormat = (transformDto.getFormat() != null) ? transformDto.getFormat() : "jpg";
            builder.outputFormat(outputFormat);

            //4. Perfoms the transformation
            builder.toOutputStream(outputStream);
            ImageProcessingResult finalResult = new ImageProcessingResult();
            finalResult.setImageData(outputStream.toByteArray());
            finalResult.setFormat(outputFormat);
            return finalResult;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<ImageDto> getAllImagesPaginated(int page, int size, Integer userId){
        //Creates a PageRequest object (page index, size)
        Pageable pageable = PageRequest.of(page,size);

        //Fetch the paginated data from Database
        Page<Image> imagePage = imageRepository.findImageByOwnerId(userId, pageable);

        //Map the page of entities to a Page of Dtos
        return imagePage.map(imageMapper::toDto);
    }

    public void deleteImage(String key, Integer userId)
    {
        Image imageToDelete = imageRepository.findImageByR2KeyAndOwnerId(key, userId);
        imageRepository.delete(imageToDelete);
    }
}
