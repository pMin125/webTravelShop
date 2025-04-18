package com.toyProject.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyProject.dto.PopularTravelDto;
import com.toyProject.dto.ProductDto;
import com.toyProject.entity.Participation;
import com.toyProject.entity.Product;
import com.toyProject.entity.Tag;
import com.toyProject.repository.ParticipationRepository;
import com.toyProject.repository.ProductRepository;
import com.toyProject.repository.TagRepository;
import com.toyProject.repository.TravelQueryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ParticipationRepository participationRepository;
    private final TagRepository tagRepository;
    private final TravelQueryRepository travelQueryRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String POPULAR_TRAVEL_KEY = "popular:travel";

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            ParticipationRepository participationRepository,
            TagRepository tagRepository,
            TravelQueryRepository travelQueryRepository,
            ObjectMapper objectMapper,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.productRepository = productRepository;
        this.participationRepository = participationRepository;
        this.tagRepository = tagRepository;
        this.travelQueryRepository = travelQueryRepository;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public Product addProduct(ProductDto dto) {
        Product product = Product.builder()
                .productName(dto.productName())
                .description(dto.description())
                .price(dto.price())
                .capacity(dto.capacity())
                .imageUrl(dto.imageUrl())
                .createdDate(dto.createdDate())
                .endDate(dto.endDate())
                .build();

        Set<Tag> tags = new HashSet<>();
        for (String name : dto.tagNames()) {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(new Tag(name)));
            tags.add(tag);
        }

        product.setTags(tags);
        return productRepository.save(product);
    }

    public Product productDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("ID에 맞는 상품이 없습니다. : " + productId));

        return product;
    }

    public List<PopularTravelDto> getPopularTravels() {
        String json = stringRedisTemplate.opsForValue().get("popular:travel");

        if (json != null) {
            try {
                List<PopularTravelDto> cached = objectMapper.readValue(json, new TypeReference<>() {});
                return cached;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<PopularTravelDto> freshData = travelQueryRepository.findPopularTravels(10);
        try {
            String jsonString = objectMapper.writeValueAsString(freshData);
            stringRedisTemplate.opsForValue().set("popular:travel", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return freshData;
    }

    public List<ProductDto> productListV2() {
        List<Product> products = productRepository.findAllWithTags();

        List<Object[]> counts = participationRepository.countGroupByProduct(Participation.ParticipationStatus.JOINED);
        Map<Long, Long> joinedCountMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<ProductDto> result = new ArrayList<>();

        for (Product product : products) {
            long joined = joinedCountMap.getOrDefault(product.getId(), 0L);

            List<String> tagNames = product.getTags().stream()
                    .map(tag -> tag.getName())
                    .collect(Collectors.toList());

            ProductDto dto = new ProductDto(
                    product.getId(),
                    product.getProductName(),
                    product.getDescription(),
                    product.getImageUrl(),
                    product.getPrice(),
                    product.getCapacity(),
                    joined,
                    product.getCreatedDate(),
                    product.getEndDate(),
                    tagNames
            );

            result.add(dto);
        }

        return result;
    }
}
