package com.nnapz.hbaseexplorer.domain.security

class Role {

	static transients = [ "ROLES" ]
	
	static List<String> ROLES = ['ROLE_USER','ROLE_ADMIN', 'ROLE_ROOT']
	
	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}
}
