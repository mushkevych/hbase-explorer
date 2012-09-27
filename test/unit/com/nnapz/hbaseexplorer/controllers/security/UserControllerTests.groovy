package com.nnapz.hbaseexplorer.controllers.security

import grails.plugins.springsecurity.SpringSecurityService;
import org.junit.*

import com.nnapz.hbaseexplorer.controllers.security.UserController;
import com.nnapz.hbaseexplorer.domain.security.Role;
import com.nnapz.hbaseexplorer.domain.security.User;
import com.nnapz.hbaseexplorer.domain.security.UserRole;

import grails.test.mixin.*

@TestFor(UserController)
@Mock([User,Role, UserRole])
class UserControllerTests {

	static boolean passwdEncoded;
	
	@Before
	public void beforeTest(){
		passwdEncoded = false
		User.metaClass.static.encodePassword = { ->
			passwdEncoded = true
		}
		
		User.metaClass.getCurrentAuthority = { ->
			return new Role(authority:"ROLE_USER")	
		}
		
		Role.metaClass.static.findByAuthority = { auth ->
			return new Role(authority:"ROLE_USER")
		}
		
		UserRole.metaClass.static.removeAll = { User user -> } 
	}

    def populateValidParams(params) {

      assert params != null
	  params.username = "test-user"
	  params.password = "password"
	   
      // TODO: Populate valid properties like...
      //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/user/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.userInstanceList.size() == 0
        assert model.userInstanceTotal == 0
    }

    void testCreate() {
       def model = controller.create()

       assert model.userInstance != null
    }

    void testSave() {
        controller.save()

        assert model.userInstance != null
        assert view == '/user/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/user/list'
        assert controller.flash.message != null
        assert User.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/user/list'


        populateValidParams(params)
        def user = new User(params)

        assert user.save() != null

        params.id = user.id

        def model = controller.show()

        assert model.userInstance == user
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/user/list'


        populateValidParams(params)
        def user = new User(params)

        assert user.save() != null

        params.id = user.id

        def model = controller.edit()

        assert model.userInstance == user
    }

	@Ignore
    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/user/list'

        response.reset()


        populateValidParams(params)
        def user = new User(params)

        assert user.save() != null

        // test invalid parameters in update
        params.id = user.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/user/list"
        assert model.userInstance != null

        user.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/user/show/$user.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        user.clearErrors()

        populateValidParams(params)
        params.id = user.id
        params.version = -1
        controller.update()

        assert view == "/user/edit"
        assert model.userInstance != null
        assert model.userInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/user/list'

        response.reset()

        populateValidParams(params)
        def user = new User(params)

        assert user.save() != null
        assert User.count() == 1

        params.id = user.id

        controller.delete()

        assert User.count() == 0
        assert User.get(user.id) == null
        assert response.redirectedUrl == '/user/list'
    }
}
