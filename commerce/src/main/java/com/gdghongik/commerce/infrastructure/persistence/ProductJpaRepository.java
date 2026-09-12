package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    // TODO[W4-2]: 같은 상품을 변경하는 다른 트랜잭션이 기다리도록 비관적 쓰기 잠금을 사용하는 findByIdWithLock(Long id) 조회 메서드를 작성하세요.
    // 반환 타입은 Optional<Product>입니다.

}
