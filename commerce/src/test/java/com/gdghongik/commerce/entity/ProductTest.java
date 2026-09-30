package com.gdghongik.commerce.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("Quantity 객체를 통해 상품 재고를 정상적으로 감소시킨다")
    void 재고_감소_성공() {
        // given
        Product product = new Product("기계식 키보드", 100000L, 10);

        // when - 이제 int 대신 Quantity 객체를 넘겨줍니다.
        product.decreaseStock(Quantity.of(3));

        // then
        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량이 0 이하이면 예외가 발생한다")
    void 수량이_0_이하이면_예외가_발생한다() {
        // given
        Product product = new Product("기계식 키보드", 129_000L, 10);

        // when & then, Quantity 객체 생성 시점에 이미 예외가 발생하여 Product까지 오지 않습니다.
        assertThatThrownBy(() -> product.decreaseStock(Quantity.of(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 예외가 발생한다")
    void 재고보다_많이_주문하면_예외가_발생한다() {
        // given
        Product product = new Product("무선 마우스", 45_000L, 3);

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(Quantity.of(4)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("판매 중이 아닌 상품은 재고를 줄일 수 없다")
    void 판매중이_아닌_상품은_재고를_줄일_수_없다() {
        // given
        Product product = new Product("단종된 USB 허브", 25_000L, 5);
        product.stopSelling();

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(Quantity.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("판매 중인 상품이 아닙니다.");
    }

    @Test
    @DisplayName("재고가 0이 되면 판매 상태가 SOLD_OUT 으로 바뀐다")
    void 재고가_0이_되면_품절_상태가_된다() {
        // given - 재고가 1개인 상품
        Product product = new Product("한정판 마우스패드", 19_000L, 1);

        // when
        product.decreaseStock(Quantity.of(1));

        // then
        assertThat(product.getStock()).isZero();
        assertThat(product.getStatus()).isEqualTo(SellingStatus.SOLD_OUT);
    }
}