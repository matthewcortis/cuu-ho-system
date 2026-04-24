package com.backend.cuutro.repository.specification;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.FilterLogicType;
import com.backend.cuutro.constant.enums.FilterOperation;
import com.backend.cuutro.constant.enums.SortDirection;
import com.backend.cuutro.dto.request.BaseFilterRequest;
import com.backend.cuutro.dto.request.FilterCriteria;
import com.backend.cuutro.dto.request.SortCriteria;
import com.backend.cuutro.entities.LoaiSuCoEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;

import jakarta.persistence.criteria.Path;

public final class LoaiSuCoSpecifications {

	private static final Map<String, Class<?>> FILTERABLE_FIELDS;

	static {
		Map<String, Class<?>> fields = new HashMap<>();
		fields.put("id", Long.class);
		fields.put("ten", String.class);
		fields.put("iconUrl", String.class);
		fields.put("moTa", String.class);
		fields.put("trangThai", Boolean.class);
		fields.put("createdAt", Instant.class);
		FILTERABLE_FIELDS = Collections.unmodifiableMap(fields);
	}

	private LoaiSuCoSpecifications() {
	}

	public static Specification<LoaiSuCoEntity> withFilter(BaseFilterRequest filterRequest) {
		if (filterRequest == null || filterRequest.getFilters() == null || filterRequest.getFilters().isEmpty()) {
			return (root, query, cb) -> cb.conjunction();
		}

		Specification<LoaiSuCoEntity> specification = null;
		for (FilterCriteria criteria : filterRequest.getFilters()) {
			if (criteria == null) {
				continue;
			}
			Specification<LoaiSuCoEntity> currentSpec = fromCriteria(criteria);
			if (specification == null) {
				specification = currentSpec;
				continue;
			}

			FilterLogicType logicType = criteria.getLogicType() == null
					? FilterLogicType.AND
					: criteria.getLogicType();
			specification = logicType == FilterLogicType.OR
					? specification.or(currentSpec)
					: specification.and(currentSpec);
		}

		if (specification == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return specification;
	}

	public static Sort toSort(BaseFilterRequest filterRequest) {
		if (filterRequest == null || filterRequest.getSorts() == null || filterRequest.getSorts().isEmpty()) {
			return Sort.by(Sort.Direction.DESC, "id");
		}

		List<Sort.Order> orders = filterRequest.getSorts().stream()
				.filter(sortCriteria -> sortCriteria != null && StringUtils.hasText(sortCriteria.getFieldName()))
				.map(sortCriteria -> {
					String fieldName = sortCriteria.getFieldName().trim();
					validateFilterField(fieldName);
					Sort.Direction direction = sortCriteria.getDirection() == SortDirection.DESC
							? Sort.Direction.DESC
							: Sort.Direction.ASC;
					return new Sort.Order(direction, fieldName);
				})
				.toList();

		if (orders.isEmpty()) {
			return Sort.by(Sort.Direction.DESC, "id");
		}
		return Sort.by(orders);
	}

	private static Specification<LoaiSuCoEntity> fromCriteria(FilterCriteria criteria) {
		String fieldName = normalizeFieldName(criteria.getFieldName());
		validateFilterField(fieldName);

		if (criteria.getOperation() == null) {
			throw new InvalidFieldException("operation is required for fieldName: " + fieldName);
		}

		return (root, query, cb) -> {
			Path<?> fieldPath = root.get(fieldName);
			Class<?> fieldType = FILTERABLE_FIELDS.get(fieldName);
			FilterOperation operation = criteria.getOperation();

			return switch (operation) {
				case EQUALS -> cb.equal(fieldPath, parseSingleValue(fieldType, criteria.getValue(), fieldName));
				case LESS_THAN -> buildLessThanPredicate(cb, fieldPath, fieldType, criteria.getValue(), fieldName);
				case LESS_THAN_OR_EQUAL ->
					buildLessThanOrEqualPredicate(cb, fieldPath, fieldType, criteria.getValue(), fieldName);
				case GREATER_THAN -> buildGreaterThanPredicate(cb, fieldPath, fieldType, criteria.getValue(), fieldName);
				case GREATER_THAN_OR_EQUAL ->
					buildGreaterThanOrEqualPredicate(cb, fieldPath, fieldType, criteria.getValue(), fieldName);
				case LIKE -> {
					validateStringField(fieldType, fieldName, operation);
					yield cb.like(fieldPath.as(String.class), "%" + parseStringValue(criteria.getValue(), fieldName) + "%");
				}
				case NOT_LIKE ->
					{
						validateStringField(fieldType, fieldName, operation);
						yield cb.notLike(fieldPath.as(String.class),
								"%" + parseStringValue(criteria.getValue(), fieldName) + "%");
					}
				case ILIKE -> {
					validateStringField(fieldType, fieldName, operation);
					yield cb.like(
							cb.lower(fieldPath.as(String.class)),
							"%" + parseStringValue(criteria.getValue(), fieldName).toLowerCase(Locale.ROOT) + "%");
				}
				case NOT_ILIKE -> {
					validateStringField(fieldType, fieldName, operation);
					yield cb.notLike(
							cb.lower(fieldPath.as(String.class)),
							"%" + parseStringValue(criteria.getValue(), fieldName).toLowerCase(Locale.ROOT) + "%");
				}
				case IN -> fieldPath.in(parseCollectionValues(fieldType, criteria.getValue(), fieldName));
				case NOT_IN -> cb.not(fieldPath.in(parseCollectionValues(fieldType, criteria.getValue(), fieldName)));
			};
		};
	}

	private static void validateFilterField(String fieldName) {
		if (!FILTERABLE_FIELDS.containsKey(fieldName)) {
			throw new InvalidFieldException("Unsupported fieldName: " + fieldName);
		}
	}

	private static String normalizeFieldName(String fieldName) {
		if (!StringUtils.hasText(fieldName)) {
			throw new InvalidFieldException("fieldName is required");
		}
		return fieldName.trim();
	}

	private static void validateStringField(Class<?> fieldType, String fieldName, FilterOperation operation) {
		if (fieldType != String.class) {
			throw new InvalidFieldException(
					"Operation " + operation + " is only supported for string fieldName: " + fieldName);
		}
	}

	private static jakarta.persistence.criteria.Predicate buildLessThanPredicate(
			jakarta.persistence.criteria.CriteriaBuilder cb,
			Path<?> fieldPath,
			Class<?> fieldType,
			Object rawValue,
			String fieldName) {
		if (fieldType == Long.class) {
			return cb.lessThan(fieldPath.as(Long.class), (Long) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == Instant.class) {
			return cb.lessThan(fieldPath.as(Instant.class), (Instant) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == String.class) {
			return cb.lessThan(cb.lower(fieldPath.as(String.class)),
					parseStringValue(rawValue, fieldName).toLowerCase(Locale.ROOT));
		}
		throw new InvalidFieldException("Operation LESS_THAN is not supported for fieldName: " + fieldName);
	}

	private static jakarta.persistence.criteria.Predicate buildLessThanOrEqualPredicate(
			jakarta.persistence.criteria.CriteriaBuilder cb,
			Path<?> fieldPath,
			Class<?> fieldType,
			Object rawValue,
			String fieldName) {
		if (fieldType == Long.class) {
			return cb.lessThanOrEqualTo(fieldPath.as(Long.class), (Long) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == Instant.class) {
			return cb.lessThanOrEqualTo(fieldPath.as(Instant.class),
					(Instant) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == String.class) {
			return cb.lessThanOrEqualTo(cb.lower(fieldPath.as(String.class)),
					parseStringValue(rawValue, fieldName).toLowerCase(Locale.ROOT));
		}
		throw new InvalidFieldException("Operation LESS_THAN_OR_EQUAL is not supported for fieldName: " + fieldName);
	}

	private static jakarta.persistence.criteria.Predicate buildGreaterThanPredicate(
			jakarta.persistence.criteria.CriteriaBuilder cb,
			Path<?> fieldPath,
			Class<?> fieldType,
			Object rawValue,
			String fieldName) {
		if (fieldType == Long.class) {
			return cb.greaterThan(fieldPath.as(Long.class), (Long) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == Instant.class) {
			return cb.greaterThan(fieldPath.as(Instant.class), (Instant) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == String.class) {
			return cb.greaterThan(cb.lower(fieldPath.as(String.class)),
					parseStringValue(rawValue, fieldName).toLowerCase(Locale.ROOT));
		}
		throw new InvalidFieldException("Operation GREATER_THAN is not supported for fieldName: " + fieldName);
	}

	private static jakarta.persistence.criteria.Predicate buildGreaterThanOrEqualPredicate(
			jakarta.persistence.criteria.CriteriaBuilder cb,
			Path<?> fieldPath,
			Class<?> fieldType,
			Object rawValue,
			String fieldName) {
		if (fieldType == Long.class) {
			return cb.greaterThanOrEqualTo(fieldPath.as(Long.class), (Long) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == Instant.class) {
			return cb.greaterThanOrEqualTo(fieldPath.as(Instant.class),
					(Instant) parseSingleValue(fieldType, rawValue, fieldName));
		}
		if (fieldType == String.class) {
			return cb.greaterThanOrEqualTo(cb.lower(fieldPath.as(String.class)),
					parseStringValue(rawValue, fieldName).toLowerCase(Locale.ROOT));
		}
		throw new InvalidFieldException("Operation GREATER_THAN_OR_EQUAL is not supported for fieldName: " + fieldName);
	}

	private static Object parseSingleValue(Class<?> fieldType, Object rawValue, String fieldName) {
		if (rawValue == null) {
			throw new InvalidFieldException("value is required for fieldName: " + fieldName);
		}
		if (fieldType == String.class) {
			return parseStringValue(rawValue, fieldName);
		}
		if (fieldType == Boolean.class) {
			return parseBooleanValue(rawValue, fieldName);
		}
		if (fieldType == Long.class) {
			return parseLongValue(rawValue, fieldName);
		}
		if (fieldType == Instant.class) {
			return parseInstantValue(rawValue, fieldName);
		}
		return rawValue;
	}

	private static List<Object> parseCollectionValues(Class<?> fieldType, Object rawValue, String fieldName) {
		List<Object> rawValues = new ArrayList<>();
		if (rawValue == null) {
			rawValues = List.of();
		} else if (rawValue instanceof List<?> listValue) {
			for (Object item : listValue) {
				rawValues.add(item);
			}
		} else if (rawValue.getClass().isArray()) {
			int length = Array.getLength(rawValue);
			for (int i = 0; i < length; i++) {
				rawValues.add(Array.get(rawValue, i));
			}
		} else {
			rawValues.add(rawValue);
		}
		if (rawValues.isEmpty()) {
			throw new InvalidFieldException("value list is required for fieldName: " + fieldName);
		}
		return rawValues.stream()
				.map(value -> parseSingleValue(fieldType, value, fieldName))
				.toList();
	}

	private static String parseStringValue(Object rawValue, String fieldName) {
		String value = String.valueOf(rawValue);
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException("value is required for fieldName: " + fieldName);
		}
		return value.trim();
	}

	private static Boolean parseBooleanValue(Object rawValue, String fieldName) {
		if (rawValue instanceof Boolean booleanValue) {
			return booleanValue;
		}
		String value = String.valueOf(rawValue).trim();
		if ("true".equalsIgnoreCase(value)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(value)) {
			return Boolean.FALSE;
		}
		throw new InvalidFieldException("Invalid boolean value for fieldName: " + fieldName);
	}

	private static Long parseLongValue(Object rawValue, String fieldName) {
		if (rawValue instanceof Number number) {
			return number.longValue();
		}
		try {
			return Long.valueOf(String.valueOf(rawValue).trim());
		} catch (NumberFormatException ex) {
			throw new InvalidFieldException("Invalid number value for fieldName: " + fieldName, ex);
		}
	}

	private static Instant parseInstantValue(Object rawValue, String fieldName) {
		if (rawValue instanceof Instant instantValue) {
			return instantValue;
		}
		try {
			return Instant.parse(String.valueOf(rawValue).trim());
		} catch (Exception ex) {
			throw new InvalidFieldException("Invalid ISO-8601 datetime value for fieldName: " + fieldName, ex);
		}
	}
}
