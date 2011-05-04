package org.krakenapps.ipmanager;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public abstract class QueryCondition {
	public abstract Predicate getPredicate(CriteriaBuilder cb, Root<?> root);
}
