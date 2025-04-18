package com.toyProject.toyProject.service;

import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import com.toyProject.repository.ParticipationRepository;
import com.toyProject.repository.ProductRepository;
import com.toyProject.repository.UserEntityRepository;
import com.toyProject.service.ParticipantService;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParticipationServiceTest {

//     @Autowired
//     private ParticipantService participationService;

//     @Autowired
//     private ParticipationRepository participationRepository;

//     @Autowired
//     private UserEntityRepository userRepository;

//     @Autowired
//     private ProductRepository productRepository;
//     @Autowired
//     private RedisTemplate<String, Object> redisTemplate;
//     // 필드 선언
//     private UserEntity user;
//     private Product product;
//     @BeforeEach
//     void setup() {
//         // 테스트 시작 전, 이전 테스트에서 남은 Redis 락 키 삭제
//         Set<String> keys = redisTemplate.keys("lock:participate:*");
//         if (keys != null && !keys.isEmpty()) {
//             redisTemplate.delete(keys);
//         }

//         // 테스트용 유저 생성 (필요한 필드 모두 포함)
//         user = userRepository.save(UserEntity.builder()
//                 .username("tester")
//                 .email("test@example.com")
//                 .nickName("testnick")   // NOT NULL 필드
//                 .password("1234")
//                 .build());

//         // 테스트용 상품 생성
//         product = productRepository.findByProductName("ass")
//                 .orElseGet(() -> productRepository.save(
//                         Product.builder()
//                                 .productName("Test Product")
//                                 .capacity(10)
//                                 .build()
//                 ));
//     }

//     @Test
//     void testConcurrentParticipate() throws InterruptedException {
//         int threadCount = 20;
//         ExecutorService executor = Executors.newFixedThreadPool(threadCount);

//         CountDownLatch readyLatch = new CountDownLatch(threadCount);
//         CountDownLatch startLatch = new CountDownLatch(1);
//         CountDownLatch doneLatch = new CountDownLatch(threadCount);

//         List<Future<Boolean>> futures = new ArrayList<>();

//         for (int i = 0; i < threadCount; i++) {
// //            final int idx = i;
// //            futures.add(executor.submit(() -> {
// //                readyLatch.countDown();
// //                startLatch.await();
// //
// //                // 각 스레드마다 별도 유저 생성
// //                UserEntity user = userRepository.save(UserEntity.builder()
// //                        .username("user" + idx)
// //                        .email("user" + idx + "@example.com")
// //                        .nickName("nick" + idx)
// //                        .password("1234")
// //                        .build());
// //                try {
// //                    return participationService.participate(user, product.getId());
// //                } catch (Exception e) {
// //                    e.printStackTrace();
// //                    return false;
// //                } finally {
// //                    doneLatch.countDown();
// //                }
// //            }));
//         }

//         readyLatch.await();
//         startLatch.countDown();
//         doneLatch.await();

//         long successCount = futures.stream().filter(f -> {
//             try {
//                 return f.get();
//             } catch (Exception e) {
//                 return false;
//             }
//         }).count();

//         long participationCount = participationRepository.count();

//         System.out.println("Success Count: " + successCount);
//         System.out.println("DB Participation Count: " + participationCount);

//         assertEquals(1, successCount, "정원이 1인 상품에는 1명만 참여해야 합니다.");
//         assertEquals(1, participationCount, "DB에는 1건만 저장되어야 합니다.");

//         executor.shutdown();
//     }
}