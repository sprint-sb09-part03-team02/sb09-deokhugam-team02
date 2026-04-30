package com.deokhugam.deokhugam_server.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(PowerUserRepositoryImplTest.TestQueryDslConfig.class)
@ActiveProfiles("test")
class PowerUserRepositoryImplTest {

  @TestConfiguration
  @EnableJpaAuditing
  static class TestQueryDslConfig {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(entityManager);
    }
  }

  @Autowired private PowerUserRepository powerUserRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager em;

  private final LocalDate targetDate = LocalDate.of(2026, 4, 30);

  @Test
  @DisplayName("파워 유저 조회: DESC 정렬 시 커서보다 큰 순위 숫자들이 내림차순으로 반환된다")
  void findPowerUsersDynamic_Pagination_Success() {
    // given
    int totalCount = 5;
    setupPowerUsers(totalCount);

    em.flush();
    em.clear();

    // when
    int cursor = 2;
    int limit = 2;
    List<PowerUser> result = powerUserRepository.findPowerUsersDynamic(
      Period.WEEKLY, cursor, null, "DESC", limit, targetDate
    );

    // then
    assertThat(result).hasSize(limit + 1)
      .extracting(PowerUser::getRankOrder)
      .containsExactly(5, 4, 3);

    assertThat(result.get(0).getUser().getNickname()).isEqualTo("활동왕5");
  }

  @Test
  @DisplayName("파워 유저 조회: 커서가 null이면 첫 번째 페이지부터 반환된다")
  void findPowerUsersDynamic_FirstPage_Success() {
    // given
    setupPowerUsers(3);
    em.flush();
    em.clear();

    // when
    List<PowerUser> result = powerUserRepository.findPowerUsersDynamic(
      Period.WEEKLY, null, null, "DESC", 2, targetDate
    );

    // then
    assertThat(result).extracting(PowerUser::getRankOrder)
      .contains(3, 2);
  }

  private void setupPowerUsers(int count) {
    for (int i = 1; i <= count; i++) {
      User user = userRepository.save(User.builder()
        .email("user" + i + "@test.com")
        .nickname("활동왕" + i)
        .password("password123")
        .build());

      powerUserRepository.save(PowerUser.builder()
        .user(user)
        .periodType(Period.WEEKLY)
        .rankOrder(i)
        .score(100.0)
        .calculatedDate(targetDate)
        .build());
    }
  }
}
