package com.medical.factory;

import com.medical.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that provides the appropriate UserCreationStrategy for a given role.
 * Strategies are automatically injected by Spring via the constructor.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Component
public class UserCreationFactory {

  private final Map<UserRole, IUserCreationStrategy> strategies;

  public UserCreationFactory(List<IUserCreationStrategy> strategyList) {
    this.strategies = strategyList.stream()
        .collect(Collectors.toMap(IUserCreationStrategy::getSupportedRole, Function.identity()));
  }

  /**
   * Returns the strategy for the given role.
   *
   * @param role the user roles
   * @return the matching strategy
   * @throws IllegalArgumentException if no strategy is found for the role
   */
  public IUserCreationStrategy getStrategy(UserRole role) {
    IUserCreationStrategy strategy = strategies.get(role);
    if (strategy == null) {
      throw new IllegalArgumentException("No strategy found for role: " + role);
    }
    return strategy;
  }
}
