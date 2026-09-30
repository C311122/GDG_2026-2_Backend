package com.gdghongik.commerce.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private OrderItem keyboardItem;
    private OrderItem mouseItem;

    @BeforeEach
    void setUp() {
        keyboardItem = OrderItem.create(1L, "기계식 키보드", Money.of(129_000L), Quantity.of(2));
        mouseItem = OrderItem.create(2L, "무선 마우스", Money.of(45_000L), Quantity.of(3));
    }

    @Test
    @DisplayName("주문하면 주문 항목이 하나 생긴다")
    void 주문하면_항목이_하나_생긴다() {
        Order order = Order.place(keyboardItem);

        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("항목 없이는 주문할 수 없다")
    void 항목_없이는_주문할_수_없다() {
        assertThatThrownBy(() -> Order.place(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목이 있어야 주문할 수 있습니다.");
    }

    @Test
    @DisplayName("주문에 항목을 더 추가할 수 있다")
    void 주문에_항목을_추가할_수_있다() {
        Order order = Order.place(keyboardItem);

        order.addItem(mouseItem);

        assertThat(order.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName("배송이 시작되면 항목을 추가할 수 없다")
    void 배송이_시작되면_항목을_추가할_수_없다() {
        Order order = Order.place(keyboardItem);
        order.ship();

        assertThatThrownBy(() -> order.addItem(mouseItem))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("주문 확정 이후에는 항목을 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 총액은 항목 금액의 합과 같다")
    void 주문_총액은_항목_금액의_합과_같다() {
        Order order = Order.place(keyboardItem);
        order.addItem(mouseItem);

        Money expected = Money.of(129_000L * 2 + 45_000L * 3);

        assertThat(order.totalAmount()).isEqualTo(expected);
    }

    @Test
    @DisplayName("주문을 취소할 수 있다")
    void 주문을_취소할_수_있다() {
        Order order = Order.place(keyboardItem);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("배송이 시작된 주문은 취소할 수 없다")
    void 배송이_시작된_주문은_취소할_수_없다() {
        Order order = Order.place(keyboardItem);
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송이 시작된 주문은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("배송이 완료된 주문도 취소할 수 없다")
    void 배송이_완료된_주문도_취소할_수_없다() {
        Order order = Order.place(keyboardItem);
        order.deliver();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("주문 항목 목록을 바깥에서 수정할 수 없다")
    void 주문항목_목록을_바깥에서_수정할_수_없다() {
        Order order = Order.place(keyboardItem);

        assertThatThrownBy(() -> order.getOrderItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ---------------- 부분 취소 테스트 추가 ----------------

    @Test
    @DisplayName("주문 항목이 2개 이상일 때 하나를 부분 취소할 수 있다")
    void 부분_취소_성공() {
        // given: 2개의 항목을 가진 주문 생성
        Order order = Order.place(keyboardItem);
        order.addItem(mouseItem);

        // when: 키보드 항목만 부분 취소
        order.cancelItem(keyboardItem);

        // then: 마우스 항목만 1개 남아야 함
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems()).containsOnly(mouseItem);
    }

    @Test
    @DisplayName("주문 항목이 1개일 때 부분 취소하면 불변식 유지를 위해 예외가 발생한다")
    void 부분_취소_실패_항목1개() {
        // given: 1개의 항목만 가진 주문 생성
        Order order = Order.place(keyboardItem);

        // when & then: 유일한 항목을 지우려 하면 예외 발생
        assertThatThrownBy(() -> order.cancelItem(keyboardItem))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("전체 주문 취소를 이용해주세요");
    }

    @Test
    @DisplayName("배송이 시작된 주문은 부분 취소할 수 없다")
    void 부분_취소_실패_배송시작() {
        // given
        Order order = Order.place(keyboardItem);
        order.addItem(mouseItem);
        order.ship(); // 배송 시작

        // when & then
        assertThatThrownBy(() -> order.cancelItem(mouseItem))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배송이 시작된 주문은 부분 취소할 수 없습니다");
    }

    @Test
    @DisplayName("해당 주문에 포함되지 않은 항목은 부분 취소할 수 없다")
    void 부분_취소_실패_다른주문항목() {
        // given: 키보드만 주문함
        Order order = Order.place(keyboardItem);
        order.addItem(OrderItem.create(3L, "마우스패드", Money.of(15_000L), Quantity.of(1)));

        // when & then: 주문에 없는 mouseItem을 취소하려고 시도
        assertThatThrownBy(() -> order.cancelItem(mouseItem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포함된 항목이 아닙니다");
    }
}