# DDD 4계층 구조와 DIP 구현

이번주는 DB와 나머지 간 연결을 완전히 끊어 더미 DB와 연결하여도 아무런 문제가 발생하지 않게 코드를 리팩토링 하였다.

## 1. DDD 4계층 구조

- 소프트웨어를 역할에 따라 네 가지 계층으로 분리합니다.
  - Presentation: 사용자의 요청을 받는 역할을 하며 Controller가 이에 해당합니다.
  - Application: 시스템의 명령 순서를 정하며 Service가 이 역할을 담당합니다.
  - Domain: 비즈니스 규칙을 지키는 핵심 계층으로 Order, Product 등이 포함됩니다.
  - Infrastructure: JPA나 DB와 같은 기술을 이용해 데이터를 실제로 처리합니다.

## 2. 의존관계 역전(DIP)과 포트/어댑터 패턴

- 가장 중요한 핵심 규칙인 도메인 계층이 외부 기술(DB 등)에 의존하지 않게 만드는 것이 목표입니다. 이를 위해 도메인 계층에 인터페이스를 만들고, 모듈들이 이 인터페이스에 의존하게 합니다.
  - Port: 도메인 계층에서 요구사항을 명시한 인터페이스입니다.
  - Adapter: 인프라스트럭처 계층에서 외부 기술(JPA 등)을 사용해 Port 인터페이스를 실제로 구현합니다.

## 3. 실제 코드 적용

- Domain 계층: `ProductRepository`, `OrderRepository`를 인터페이스로 만들어 `save`, `findById`, `findAll`과 같은 필요 기능의 형태만 정의합니다.
- Infrastructure 계층:
  - `ProductJpaRepository`, `OrderJpaRepository`를 생성하여 Spring Data JPA 기술을 결합합니다.
  - `ProductRepositoryAdapter`, `OrderRepositoryAdapter` 클래스를 생성해 도메인 인터페이스를 구현(implements)합니다.
  - 어댑터 클래스 내부에서 JpaRepository를 주입받아 실제 DB 조작을 위임(`return jpaRepository.save()` 등)합니다.
