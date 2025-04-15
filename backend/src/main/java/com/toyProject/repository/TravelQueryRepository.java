package com.toyProject.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toyProject.dto.PopularTravelDto;
import com.toyProject.entity.Product;
import com.toyProject.entity.QParticipation;
import com.toyProject.entity.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.toyProject.entity.Participation.ParticipationStatus.*;


@Repository
@RequiredArgsConstructor
public class TravelQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PopularTravelDto> findPopularTravels(int limit) {
        QProduct product = QProduct.product;
        QParticipation participation = QParticipation.participation;

        return queryFactory
                .select(Projections.constructor(PopularTravelDto.class,
                        product.id,
                        product.productName,
                        product.imageUrl,
                        participation.count()
                ))
                .from(product)
                .leftJoin(participation).on(
                        participation.product.eq(product),
                        participation.status.in(JOINED, WAITING_PAYMENT, WAITING_LIST)
                )
                .groupBy(product.id)
                .orderBy(participation.count().desc())
                .limit(limit)
                .fetch();
    }
}
