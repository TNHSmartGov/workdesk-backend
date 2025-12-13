package com.tnh.baseware.core.specs;

import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public enum Operator {

    EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var value = request.getFieldType().parse(request.getValue().toString());
            var key = this.getPath(root, request);
            return cb.and(cb.equal(key, value), predicate);
        }
    },

    NOT_EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var value = request.getFieldType().parse(request.getValue().toString());
            var key = this.getPath(root, request);
            return cb.and(cb.notEqual(key, value), predicate);
        }
    },

    LIKE {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = this.getPath(root, request);
            return cb.and(cb.like(cb.upper(key), "%" + request.getValue().toString().toUpperCase() + "%"), predicate);
        }
    },
    GREATER_THAN {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var value = request.getFieldType().parse(request.getValue().toString());
            @SuppressWarnings("rawtypes")
            Expression<Comparable> key = this.getPath(root, request);
            return cb.and(cb.greaterThan(key, (Comparable) value), predicate);
        }
    },
    LESS_THAN {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var value = request.getFieldType().parse(request.getValue().toString());
            @SuppressWarnings("rawtypes")
            Expression<Comparable> key = this.getPath(root, request);
            return cb.and(cb.lessThan(key, (Comparable) value), predicate);
        }
    },
    IN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var values = request.getValues();
            var inClause = cb.in(this.getPath(root, request));
            for (var value : values) {
                inClause.value(request.getFieldType().parse(value.toString()));
            }
            return cb.and(inClause, predicate);
        }
    },
    NOT_IN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var value = request.getValue();
            var inClause = cb.in(this.getPath(root, request));
            inClause.value(request.getFieldType().parse(value.toString()));
            return cb.and(cb.not(inClause), predicate);
        }
    },

    BETWEEN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            var value = request.getFieldType().parse(request.getValue().toString());
            var valueTo = request.getFieldType().parse(request.getValueTo().toString());
            if (request.getFieldType() == FieldType.DATE) {
                var startDate = (Instant) value;
                var endDate = (Instant) valueTo;
                Expression<Instant> key = this.getPath(root, request);
                return cb.and(cb.and(cb.greaterThanOrEqualTo(key, startDate), cb.lessThanOrEqualTo(key, endDate)),
                        predicate);
            }

            if (request.getFieldType() != FieldType.CHAR && request.getFieldType() != FieldType.BOOLEAN) {
                var start = (Number) value;
                var end = (Number) valueTo;
                Expression<Number> key = this.getPath(root, request);
                return cb.and(cb.and(cb.ge(key, start), cb.le(key, end)), predicate);
            }

            log.debug(LogStyleHelper.debug("Can not use between for {} field type."), request.getFieldType());
            return predicate;
        }
    };

    public abstract <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate);

    public <T, V> Path<V> getPath(Root<T> root, FilterRequest request) {
        var keys = request.getKey().split("\\.");
        Path<V> path = root.get(keys[0]);
        for (var i = 1; i < keys.length; i++) {
            path = path.get(keys[i]);
        }
        return path;
    }
}
