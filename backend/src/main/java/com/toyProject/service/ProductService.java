package com.toyProject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyProject.dto.*;
import com.toyProject.entity.*;
import com.toyProject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Qualifier;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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
                System.out.println("✅ Redis 캐시에서 인기 여행 가져옴");
                return cached;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 캐시에 없으면 DB에서 가져오기
        List<PopularTravelDto> freshData = travelQueryRepository.findPopularTravels(10);
        try {
            String jsonString = objectMapper.writeValueAsString(freshData);
            stringRedisTemplate.opsForValue().set("popular:travel", jsonString);
            System.out.println("📦 DB 조회 후 캐시 저장 완료");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return freshData;
    }

    public List<ProductDto> productListV2() {
        List<Product> products = productRepository.findAllWithTags();

        // Group by 로 참여 수 미리 가져오기
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
