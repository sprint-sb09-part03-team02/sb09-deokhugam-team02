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
  @DisplayName("활동왕 유저를 동적으로 조회하고 커서 기반 페이징이 적용된다")
  void findPowerUsersDynamic_Pagination_Success() {
    // given
    for (int i = 1; i <= 5; i++) {
      User user = createUser("user" + i + "@test.com", "활동왕" + i);
      createPowerUser(user, i);
    }

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
      .containsExactly(3, 4, 5);

    assertThat(result.get(0).getUser().getNickname()).isEqualTo("활동왕3");
  }


  private User createUser(String email, String nickname) {
    return userRepository.save(User.builder()
      .email(email)
      .nickname(nickname)
      .password("password123")
      .build());
  }

  private void createPowerUser(User user, int rank) {
    powerUserRepository.save(PowerUser.builder()
      .user(user)
      .periodType(Period.WEEKLY)
      .rankOrder(rank)
      .score(100.0)
      .calculatedDate(targetDate)
      .build());
  }
}
