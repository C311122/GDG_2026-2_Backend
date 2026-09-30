package com.gdghongik.commerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 바깥에서 값을 마음대로 바꿀 수 없도록 @Setter 를 두지 않습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private long price;

    private int stock;

    @Enumerated(EnumType.STRING)
    private SellingStatus status;

    public Product(String name, long price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = SellingStatus.SELLING;
    }

    public void stopSelling() {
        this.status = SellingStatus.STOPPED;
    }

    /**
     * 재고를 quantity 객체의 수량만큼 줄입니다.
     * Quantity 생성 시점에 이미 1개 이상임이 검증되므로,
     * Product 내부에서는 음수/0에 대한 방어 로직을 작성할 필요가 없습니다.
     */
    public void decreaseStock(Quantity quantity) {
        if (this.status != SellingStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다.");
        }

        // quantity.value()를 꺼내서 비교 및 연산 수행
        if (this.stock < quantity.value()) {
            throw new IllegalStateException("재고가 부족합니다. 남은 재고=" + this.stock);
        }

        this.stock -= quantity.value();

        if (this.stock == 0) {
            this.status = SellingStatus.SOLD_OUT;
        }
    }

}