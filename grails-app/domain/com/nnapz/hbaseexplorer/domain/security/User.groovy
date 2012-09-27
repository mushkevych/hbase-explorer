package com.nnapz.hbaseexplorer.domain.security

class User {

	static transients = [ "springSecurityService" ] 
	
	def springSecurityService

	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static constraints = {
		username blank: false, unique: true
		password blank: false
	}

	static mapping = {
		password column: '`password`'
	}

	transient Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

	transient Role getCurrentAuthority() {
		def authList = getAuthorities()?.iterator()?.next() ?: Role.findByAuthority('ROLE_USER')
	}
	
	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
}
