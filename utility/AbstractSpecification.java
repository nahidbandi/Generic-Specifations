package com.x.infrastructure.utility;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public abstract class AbstractSpecification {

    public <T> Specification<T> equal(final NodePath nodePath, final Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            final Criteria criteria = initializeCriteria(root, nodePath, value);
            return criteriaBuilder.equal(criteria.getPath(), criteria.getValue());
        };
    }

    public <T> Specification<T> like(final NodePath nodePath, final Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            final Criteria criteria = initializeCriteria(root, nodePath, value);
            return criteriaBuilder.like(criteria.getPath(), Queries.toLikeOperand((String) criteria.getValue()));
        };
    }

    public <T> Specification<T> in(final NodePath nodePath, final Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            final Criteria criteria = initializeCriteria(root, nodePath, value);
            return criteriaBuilder.in(criteria.getPath()).value(criteria.getValue());
        };
    }

    public <T> Specification<T> isNotNull(final NodePath nodePath) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            final Criteria criteria = initializeCriteria(root, nodePath, null);
            return criteriaBuilder.isNotNull(criteria.getPath());
        };
    }


    public <T> Specification<T> isNull(final Expression<?> expression, final Class<T> type) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isNull(expression);
    }


    public <T> Specification<T> between(final NodePath nodePath, final LocalDateTime from, final LocalDateTime to) {

        Specification<T> specification = null;
        if (Empty.isNotEmpty(from) && Empty.isNotEmpty(to)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> {
                final Criteria criteria = initializeCriteria(root, nodePath, null);
                return criteriaBuilder.between(criteria.getPath(), from, to);
            };
        } else if (Empty.isNotEmpty(from)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> {
                final Criteria criteria = initializeCriteria(root, nodePath, null);
                return criteriaBuilder.greaterThanOrEqualTo(criteria.getPath(), from);
            };
        } else if (Empty.isNotEmpty(to)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> {
                final Criteria criteria = initializeCriteria(root, nodePath, null);
                return criteriaBuilder.lessThanOrEqualTo(criteria.getPath(), to);
            };
        }
        return specification;
    }


    private <T> Specification<T> between(final List<String> paths, final Long from, final Long to, final Class<T> type) {
        Specification<T> specification;
        if (Empty.isNotEmpty(from) && Empty.isNotEmpty(to)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(initializePath(root, paths), from, to);
        } else if (Empty.isNotEmpty(from)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(initializePath(root, paths), from);
        } else {
            specification = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(initializePath(root, paths), to);
        }
        return specification;
    }

    private <T> Specification<T> between(final List<String> paths, final BigDecimal from, final BigDecimal to, final Class<T> type) {
        Specification<T> specification;
        if (Empty.isNotEmpty(from) && Empty.isNotEmpty(to)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(initializePath(root, paths), from, to);
        } else if (Empty.isNotEmpty(from)) {
            specification = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(initializePath(root, paths), from);
        } else {
            specification = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(initializePath(root, paths), to);
        }
        return specification;
    }


    private Criteria initializeCriteria(Root root, final NodePath nodePath, final Object value) {
        Path<Object> entityPath = initializePath(root, nodePath);
        Criteria criteria = new Criteria();
        criteria.setPath(entityPath);
        criteria.setValue(value);
        return criteria;
    }


    private Path initializePath(Path<Object> root, final NodePath nodePath) {
        Path<Object> entityPath = root;
        if (nodePath.isParent()) {
            entityPath = (entityPath == null) ? initializePath(root.get(nodePath.getPath()), nodePath.getChild()) : initializePath(entityPath.get(nodePath.getPath()), nodePath.getChild());
        } else if (nodePath.isLeaf()) {
            entityPath = (entityPath != null) ? entityPath.get(nodePath.getPath()) : root.get(nodePath.getPath());
        }
        return entityPath;
    }


    private Path initializePath(Root root, List<String> paths) {
        Path<Object> entityPath = null;
        for (String path : paths) {
            entityPath = (entityPath != null) ? entityPath.get(path) : root.get(path);
        }
        return entityPath;
    }

    private Path initializePath(Root root, List<String> paths, String currentPath) {
        Path<Object> entityPath = null;
        if (!paths.isEmpty()) {
            for (String path : paths) {
                entityPath = (entityPath != null) ? entityPath.get(path) : root.get(path);
            }
            entityPath = entityPath.get(currentPath);
        } else {
            entityPath = root.get(currentPath);
        }
        return entityPath;
    }


    @Getter
    @Setter
    public static class InitializeParam<T> {

        private final NodePath nodePath;
        private final T value;

        private InitializeParam(NodePath nodePath, T value) {
            this.nodePath = nodePath;
            this.value = value;
        }

        public static <T> InitializeParam of(NodePath nodePath, T value) {
            return new InitializeParam<>(nodePath, value);
        }
    }


    public <T> Specification<T> initializeSpecification(final NodePath nodePath, InitializeParam... InitializeParams) {
        Specification<T> specification = isNotNull(nodePath);
        for (InitializeParam param : InitializeParams) {
            if (param != null && param.getValue() != null && Numbers.isGreaterThanZero((Number) param.getValue())) {
                specification = specification.and(equal(param.getNodePath(), param.getValue()));
            }
        }
        return specification;
    }

}
