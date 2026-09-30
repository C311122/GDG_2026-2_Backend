package com.gdghongik.commerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(OrderStatus status) {
        this.status = status;
    }

    public static Order place(OrderItem item) {
        Order order = new Order(OrderStatus.CREATED);
        order.addItem(item);
        return order;
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("주문 항목이 있어야 주문할 수 있습니다.");
        }

        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("주문 확정 이후에는 항목을 변경할 수 없습니다.");
        }

        this.orderItems.add(item);
        item.assignTo(this);
    }

    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송이 시작된 주문은 취소할 수 없습니다.");
        }

        this.status = OrderStatus.CANCELED;
    }

    public Money totalAmount() {
        return orderItems.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.ZERO, Money::add);
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public void cancelItem(OrderItem item) {
        // 1. 상태 검증: 배송 단계 이후에는 항목을 취소할 수 없음
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송이 시작된 주문은 부분 취소할 수 없습니다.");
        }

        // 2. 소속 검증: 해당 항목이 현재 주문에 포함되어 있는지 확인
        if (!this.orderItems.contains(item)) {
            throw new IllegalArgumentException("이 주문에 포함된 항목이 아닙니다.");
        }

        // 3. 불변식 보호: 주문에는 항목이 1개 이상이어야 한다.
        // 현재 항목이 1개뿐인데 삭제를 시도하면 빈 주문이 되어 불변식이 깨지게 됩니다.
        // 따라서 부분 취소를 막고 전체 주문 취소(cancel)를 유도하도록 예외를 던집니다.
        if (this.orderItems.size() <= 1) {
            throw new IllegalStateException("주문 항목이 1개만 남은 상태에서는 부분 취소를 할 수 없습니다. 전체 주문 취소를 이용해주세요.");
        }

        this.orderItems.remove(item);
    }

    public void ship() {
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        this.status = OrderStatus.DELIVERED;
    }
}
