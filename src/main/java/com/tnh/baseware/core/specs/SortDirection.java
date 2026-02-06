package com.tnh.baseware.core.specs;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public enum SortDirection {

    ASC {
        public <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request) {
            return cb.asc(buildPath(root, request.getKey()));
        }
    },
    DESC {
        public <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request) {
            return cb.desc(buildPath(root, request.getKey()));
        }
    };

    public abstract <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request);

    private static <T> jakarta.persistence.criteria.Path<?> buildPath(Root<T> root, String key) {
        String[] parts = key.split("\\.");
        jakarta.persistence.criteria.Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }
}
