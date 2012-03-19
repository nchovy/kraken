/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.ipmanager;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class IpQueryCondition extends QueryCondition {
	private int orgId;
	private Integer agentId;

	public IpQueryCondition(int orgId) {
		this.orgId = orgId;
	}

	public int getOrgId() {
		return orgId;
	}

	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	@Override
	public Predicate getPredicate(CriteriaBuilder cb, Root<?> root) {
		Predicate p = cb.equal(root.join("agent").get("orgId"), orgId);

		if (agentId != null)
			p = cb.and(p, cb.equal(root.join("agent").get("id"), agentId));

		return p;
	}

}
