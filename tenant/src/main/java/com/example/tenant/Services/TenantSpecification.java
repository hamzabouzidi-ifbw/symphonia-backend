package com.example.tenant.Services;

import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TenantSpecification {

    public static Specification<Tenant> tenantNameContains(String name) {
        return (root, query, builder) ->
                name == null ? null : builder.like(builder.lower(root.get("tenantName")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Tenant> contextNameContains(String context) {
        return (root, query, builder) ->
                context == null ? null : builder.like(builder.lower(root.get("contextName")), "%" + context.toLowerCase() + "%");
    }

    public static Specification<Tenant> codeContains(String code) {
        return (root, query, builder) ->
                code == null ? null : builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%");
    }

    public static Specification<Tenant> hasPrefix(Integer prefix) {
        return (root, query, builder) ->
                prefix == null ? null : builder.equal(root.get("extensionPrefix"), prefix);
    }

    public static Specification<Tenant> createdAfter(LocalDateTime date) {
        return (root, query, builder) ->
                date == null ? null : builder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Tenant> hasTimezone(String timezone) {
        return (root, query, builder) ->
                timezone == null ? null : builder.equal(root.get("timezone"), timezone);
    }
}
